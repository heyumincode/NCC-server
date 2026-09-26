package com.chongchao.mvp.controller;

import com.chongchao.mvp.common.ApiResponse;
import com.chongchao.mvp.dto.WechatLoginRequest;
import com.chongchao.mvp.dto.WechatLoginResponse;
import com.chongchao.mvp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat/login")
    public ApiResponse<WechatLoginResponse> login(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}

