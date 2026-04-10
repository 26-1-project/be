package com.softy.be.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class KakaoOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authUri;
    private final String tokenUri;
    private final String userInfoUri;

    public KakaoOAuthClient(
            @Value("${oauth.kakao.client-id}") String clientId,
            @Value("${oauth.kakao.client-secret}") String clientSecret,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri,
            @Value("${oauth.kakao.auth-uri:https://kauth.kakao.com/oauth/authorize}") String authUri,
            @Value("${oauth.kakao.token-uri:https://kauth.kakao.com/oauth/token}") String tokenUri,
            @Value("${oauth.kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}") String userInfoUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.authUri = authUri;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
    }

    public URI buildAuthorizeUri(String state) {
        return UriComponentsBuilder.fromUriString(authUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build(true)
                .toUri();
    }

    public String exchangeCodeForAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(tokenUri, HttpMethod.POST, requestEntity, JsonNode.class);

        JsonNode responseBody = response.getBody();
        if (responseBody == null || responseBody.path("access_token").isMissingNode()) {
            throw new IllegalStateException("\uCE74\uCE74\uC624 \uC561\uC138\uC2A4 \uD1A0\uD070\uC744 \uAC00\uC838\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4");
        }
        return responseBody.path("access_token").asText();
    }

    public KakaoUserProfile getUserProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, requestEntity, JsonNode.class);

        JsonNode responseBody = response.getBody();
        if (responseBody == null || responseBody.path("id").isMissingNode()) {
            throw new IllegalStateException("\uCE74\uCE74\uC624 \uC0AC\uC6A9\uC790 \uC815\uBCF4\uB97C \uAC00\uC838\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4");
        }

        String providerUserId = responseBody.path("id").asText();
        String nickname = responseBody.path("properties").path("nickname").asText("kakao_" + providerUserId);

        return new KakaoUserProfile(providerUserId, nickname);
    }
}
