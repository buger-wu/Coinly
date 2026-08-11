package com.coinly.common.exception;

import com.coinly.common.domain.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>通过 {@code @RestControllerAdvice} 统一捕获所有 Controller 抛出的异常，
 * 转换为 {@link CommonResponse} 格式返回给前端，避免异常堆栈直接泄漏。
 *
 * <p>处理顺序（从具体到通用）：
 * <ol>
 *   <li>{@link BusinessException}：业务异常，返回 400 + 具体错误信息。</li>
 *   <li>{@link MethodArgumentNotValidException}：参数校验失败，返回 400 + 字段错误聚合信息。</li>
 *   <li>{@link Exception}：未预期异常，返回 500 + "服务器内部错误"。</li>
 * </ol>
 *
 * @see BusinessException
 * @see CommonResponse
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常：根据业务 code 返回对应 HTTP 状态码 + 错误信息。
     * 401 未授权返回 HTTP 401，其余业务异常返回 HTTP 400。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = e.getCode() == 401 ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(CommonResponse.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数校验失败：聚合所有字段错误信息，用逗号分隔。
     * 例如："用户名不能为空, 密码长度必须在6-50之间"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, message));
    }

    /**
     * 兜底处理：所有未捕获的异常统一返回 500，避免堆栈泄漏。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("未捕获异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail(500, "服务器内部错误"));
    }
}