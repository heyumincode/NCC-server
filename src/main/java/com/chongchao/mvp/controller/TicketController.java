package com.chongchao.mvp.controller;

import com.chongchao.mvp.auth.RequestAttributes;
import com.chongchao.mvp.common.ApiResponse;
import com.chongchao.mvp.domain.AppUser;
import com.chongchao.mvp.dto.ReserveTicketRequest;
import com.chongchao.mvp.dto.TicketResponse;
import com.chongchao.mvp.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/reservations")
    public ApiResponse<TicketResponse> reserve(
            @RequestAttribute(RequestAttributes.APP_USER) AppUser appUser,
            @Valid @RequestBody ReserveTicketRequest request
    ) {
        return ApiResponse.ok(ticketService.reserve(appUser, request));
    }

    @GetMapping
    public ApiResponse<List<TicketResponse>> list(
            @RequestAttribute(RequestAttributes.APP_USER) AppUser appUser
    ) {
        return ApiResponse.ok(ticketService.list(appUser));
    }

    @GetMapping("/{id}")
    public ApiResponse<TicketResponse> detail(
            @RequestAttribute(RequestAttributes.APP_USER) AppUser appUser,
            @PathVariable long id
    ) {
        return ApiResponse.ok(ticketService.detail(appUser, id));
    }
}

