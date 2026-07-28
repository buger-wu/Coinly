package com.coinly.business.category.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @Size(max = 50, message = "分类名称长度不能超过50")
    private String name;

    private Integer type;

    private Long parentId;
}