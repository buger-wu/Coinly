package com.coinly.business.book.controller;

import com.coinly.business.book.dto.CreateBookRequest;
import com.coinly.business.book.dto.UpdateBookRequest;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.service.BookService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public CommonResponse<BookEntity> createBook(@Valid @RequestBody CreateBookRequest request) {
        Long userId = UserContext.getUserId();

        BookEntity book = new BookEntity();
        book.setUserId(userId);
        book.setName(request.getName());
        book.setDescription(request.getDescription());
        book.setCreateTime(LocalDateTime.now());
        book.setUpdateTime(LocalDateTime.now());

        bookService.save(book);
        return CommonResponse.success(book);
    }

    @GetMapping
    public CommonResponse<List<BookEntity>> getBookList() {
        Long userId = UserContext.getUserId();
        List<BookEntity> books = bookService.lambdaQuery()
                .eq(BookEntity::getUserId, userId)
                .list();
        return CommonResponse.success(books);
    }

    @GetMapping("/{id}")
    public CommonResponse<BookEntity> getBookById(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        BookEntity book = bookService.lambdaQuery()
                .eq(BookEntity::getId, id)
                .eq(BookEntity::getUserId, userId)
                .one();

        if (book == null) {
            throw new BusinessException("账本不存在");
        }
        return CommonResponse.success(book);
    }

    @PutMapping("/{id}")
    public CommonResponse<Void> updateBook(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
        Long userId = UserContext.getUserId();
        BookEntity book = bookService.lambdaQuery()
                .eq(BookEntity::getId, id)
                .eq(BookEntity::getUserId, userId)
                .one();

        if (book == null) {
            throw new BusinessException("账本不存在");
        }

        if (request.getName() != null) {
            book.setName(request.getName());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        book.setUpdateTime(LocalDateTime.now());

        bookService.updateById(book);
        return CommonResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteBook(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        BookEntity book = bookService.lambdaQuery()
                .eq(BookEntity::getId, id)
                .eq(BookEntity::getUserId, userId)
                .one();

        if (book == null) {
            throw new BusinessException("账本不存在");
        }

        bookService.removeById(id);
        return CommonResponse.success("删除成功", null);
    }
}