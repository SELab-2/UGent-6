package com.ugent.pidgeon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2ClientConfig {

    @Value("${azure.activedirectory.client-id}")
    private String clientId;

    @Value("${azure.activedirectory.b2c.client-secret}")
    private String clientSecret;

    @Value("${azure.activedirectory.tenant-id}")
    private String tenantId;



    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("azure")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tokenUri("https://login.microsoftonline.com/"+tenantId+"/oauth2/v2.0/token")
                .authorizationUri("https://login.microsoftonline.com/"+tenantId+"/oauth2/v2.0/authorize")
                .redirectUri("{baseUrl}")
                .scope("openid", "profile", "email")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientName("Azure")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}