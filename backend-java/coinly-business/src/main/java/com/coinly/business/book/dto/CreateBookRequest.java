package com.coinly.business.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookRequest {

    @NotBlank(message = "账本名称不能为空")
    @Size(max = 50, message = "账本名称长度不能超过50")
    private String name;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;
}