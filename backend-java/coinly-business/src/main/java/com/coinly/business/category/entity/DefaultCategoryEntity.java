package com.coinly.business.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_default_category")
public class DefaultCategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer type;

    private Long parentId;

    private Integer sortOrder;
}