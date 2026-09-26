package com.chongchao.mvp.controller;

import com.chongchao.mvp.auth.RequestAttributes;
import com.chongchao.mvp.common.ApiResponse;
import com.chongchao.mvp.dto.StaffLoginRequest;
import com.chongchao.mvp.dto.StaffLoginResponse;
import com.chongchao.mvp.dto.VerifyTicketRequest;
import com.chongchao.mvp.dto.VerifyTicketResponse;
import com.chongchao.mvp.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping("/login")
    public ApiResponse<StaffLoginResponse> login(@Valid @RequestBody StaffLoginRequest request) {
        return ApiResponse.ok(staffService.login(request));
    }

    @PostMapping("/verify")
    public ApiResponse<VerifyTicketResponse> verify(
            @RequestAttribute(RequestAttributes.STAFF_NAME) String staffName,
            @Valid @RequestBody VerifyTicketRequest request
    ) {
        return ApiResponse.ok(staffService.verify(staffName, request));
    }
}

