package com.coinly.business.auth.controller;

import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.service.BookService;
import com.coinly.business.cache.TokenBlacklistService;
import com.coinly.business.category.service.CategoryService;
import com.coinly.business.security.login.LoginAttemptService;
import com.coinly.business.user.dto.LoginRequest;
import com.coinly.business.user.dto.LoginResponse;
import com.coinly.business.user.dto.RegisterRequest;
import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.service.UserService;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import com.coinly.common.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@Tag(name = "认证模块")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.expiration:604800000}")
    private long jwtExpiration;

    public AuthController(UserService userService, BookService bookService, CategoryService categoryService,
                          BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils,
                          TokenBlacklistService tokenBlacklistService,
                          LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * 注册接口。
     *
     * <p>注册成功后直接返回 JWT Token，前端无需再调登录接口。
     *
     * @param request 用户名 + 密码
     * @return 用户信息 + JWT Token
     * @throws BusinessException 用户名已存在时抛出
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @Transactional
    public CommonResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 1. 校验用户名唯一
        if (userService.lambdaQuery().eq(UserEntity::getUsername, request.getUsername()).exists()) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户（BCrypt 加密密码）
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userService.save(user);

        // 3. 创建默认账本 "日常开销"
        BookEntity defaultBook = new BookEntity();
        defaultBook.setUserId(user.getId());
        defaultBook.setName("日常开销");
        defaultBook.setCreateTime(LocalDateTime.now());
        defaultBook.setUpdateTime(LocalDateTime.now());
        bookService.save(defaultBook);

        // 4. 初始化默认分类（从 sys_default_category 复制到 biz_category）
        categoryService.initDefaultCategories(user.getId());

        // 5. 生成 JWT Token 返回
        String token = jwtUtils.generateToken(user.getId());
        return CommonResponse.success(new LoginResponse(user.getId(), user.getUsername(), user.getNickname(), token));
    }

    /**
     * 登录接口。
     *
     * <p>用户名或密码错误时统一返回 "用户名或密码错误"，不区分具体原因（安全考虑）。
     *
     * @param request 用户名 + 密码
     * @return 用户信息 + JWT Token
     * @throws BusinessException 用户名或密码错误时抛出
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // V9: 检查账号是否被锁定
        if (loginAttemptService.isLocked(request.getUsername())) {
            throw new BusinessException("账号已被锁定，请15分钟后再试");
        }

        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, request.getUsername())
                .one();

        // 用户不存在或密码不匹配，统一返回相同错误信息
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            long failCount = loginAttemptService.recordFailure(request.getUsername());
            log.warn("登录失败: username={}, 失败次数={}", request.getUsername(), failCount);
            throw new BusinessException("用户名或密码错误");
        }

        // V9: 登录成功，清除失败计数
        loginAttemptService.recordSuccess(request.getUsername());

        String token = jwtUtils.generateToken(user.getId());
        return CommonResponse.success(new LoginResponse(user.getId(), user.getUsername(), user.getNickname(), token));
    }

    /**
     * V7: 退出登录。
     * 将当前 Token 加入 Redis 黑名单，后续请求将被拦截器拒绝。
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 加入黑名单，TTL = JWT 剩余有效期
            tokenBlacklistService.blacklist(token, jwtExpiration / 1000);
        }
        return CommonResponse.success("退出成功", null);
    }
}