package com.chongchao.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WechatLoginRequest(
        @NotBlank(message = "微信登录凭证不能为空") String code,
        @Size(max = 64, message = "昵称不能超过 64 个字符") String nickname,
        @Size(max = 1000, message = "头像地址过长") String avatarUrl
) {
}

