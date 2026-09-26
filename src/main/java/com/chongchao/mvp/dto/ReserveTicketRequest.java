package com.chongchao.mvp.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReserveTicketRequest(
        @NotNull(message = "请选择赛事") Long matchEventId,
        @NotBlank(message = "请填写观赛人姓名")
        @Size(max = 64, message = "观赛人姓名过长") String attendeeName,
        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不正确") String attendeePhone,
        @AssertTrue(message = "请先阅读并同意预约规则和隐私告知") boolean acceptedRules
) {
}

