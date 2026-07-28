package com.coinly.business.auth.controller;

import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.service.BookService;
import com.coinly.business.category.service.CategoryService;
import com.coinly.business.user.dto.LoginRequest;
import com.coinly.business.user.dto.LoginResponse;
import com.coinly.business.user.dto.RegisterRequest;
import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.service.UserService;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import com.coinly.common.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 认证 Controller：注册、登录。
 *
 * @see com.coinly.business.category.service.CategoryService#initDefaultCategories(Long)
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserService userService, BookService bookService, CategoryService categoryService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.passwordEncoder = passwordEncoder;
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
        String token = JwtUtils.generateToken(user.getId());
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
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userService.lambdaQuery()
                .eq(UserEntity::getUsername, request.getUsername())
                .one();

        // 用户不存在或密码不匹配，统一返回相同错误信息
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = JwtUtils.generateToken(user.getId());
        return CommonResponse.success(new LoginResponse(user.getId(), user.getUsername(), user.getNickname(), token));
    }
}