package com.chongchao.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyTicketRequest(
        @NotBlank(message = "票码不能为空") String payload,
        @NotBlank(message = "请填写入口名称")
        @Size(max = 40, message = "入口名称过长") String gateName
) {
}

