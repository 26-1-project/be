package com.softy.be.auth.service;

public record KakaoUserProfile(
        String providerUserId,
        String nickname
) {
}
