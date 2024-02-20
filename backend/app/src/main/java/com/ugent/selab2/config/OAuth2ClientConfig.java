package com.ugent.selab2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("azure")
                .clientId("39136cda-f02f-4305-9b08-45f132bab07e")
                .clientSecret("i1n8Q~57EDI.E2iLxzkW3Q.ixEtVIM4jwN7eDbxK")
                .tokenUri("https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d/oauth2/v2.0/token")
                .authorizationUri("https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d/oauth2/v2.0/authorize")
                .redirectUri("{baseUrl}")
                .scope("openid", "profile", "email")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientName("Azure")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}