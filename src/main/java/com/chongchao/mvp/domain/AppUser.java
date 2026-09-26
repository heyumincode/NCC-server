package com.chongchao.mvp.domain;

public record AppUser(long id, String wechatOpenId, String nickname, String avatarUrl, boolean active) {
}

