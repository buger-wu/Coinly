package com.coinly.business.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.book.entity.BookEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<BookEntity> {
}