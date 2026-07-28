package com.coinly.business.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.mapper.BookMapper;
import com.coinly.business.book.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, BookEntity> implements BookService {
}