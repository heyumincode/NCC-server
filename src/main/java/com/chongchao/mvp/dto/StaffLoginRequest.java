package com.chongchao.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffLoginRequest(
        @NotBlank(message = "请填写工作人员姓名")
        @Size(max = 64, message = "工作人员姓名过长") String staffName,
        @NotBlank(message = "请填写核销口令") String passcode
) {
}

