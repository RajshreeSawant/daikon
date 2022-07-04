package org.talend.daikon.spring.reactive.sat.config;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.util.StringUtils;
import org.talend.daikon.spring.reactive.sat.authentication.Auth0ReactiveAuthenticationManager;
import org.talend.daikon.spring.reactive.sat.authentication.Auth0ReactiveAuthenticationProvider;
import org.talend.daikon.spring.reactive.sat.authentication.TalendJwtConverter;
import org.talend.daikon.spring.reactive.sat.introspection.AuthUserDetailsConverterIntrospector;

import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
public class ReactiveAuthenticationManagerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveAuthenticationManagerFactory.class);

    private final Mono<ReactiveAuthenticationManager> opaqueReactiveAuthenticationManager;

    private final Mono<ReactiveAuthenticationManager> jwtReactiveAuthenticationManager;

    private final Mono<ReactiveAuthenticationManager> auth0ReactiveAuthenticationManager;

    public ReactiveAuthenticationManagerFactory(OAuth2ResourceServerProperties oauth2Properties,
            List<Auth0ReactiveAuthenticationProvider> auth0Providers) {
        this.opaqueReactiveAuthenticationManager = Mono.justOrEmpty(opaqueReactiveAuthenticationManager(oauth2Properties));
        this.jwtReactiveAuthenticationManager = Mono.justOrEmpty(jwtReactiveAuthenticationManager(oauth2Properties));
        this.auth0ReactiveAuthenticationManager = Mono
                .justOrEmpty(auth0ReactiveAuthenticationManager(oauth2Properties, auth0Providers));
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new TalendJwtConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    private ReactiveAuthenticationManager jwtReactiveAuthenticationManager(OAuth2ResourceServerProperties oauth2Properties) {
        if (StringUtils.hasText(oauth2Properties.getJwt().getJwkSetUri())) {
            JwtReactiveAuthenticationManager manager = new JwtReactiveAuthenticationManager(
                    new NimbusReactiveJwtDecoder(oauth2Properties.getJwt().getJwkSetUri()));
            manager.setJwtAuthenticationConverter(grantedAuthoritiesExtractor());
            return manager;
        }
        LOGGER.debug(
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri has no value: no JWT token authentication will be available.");
        return null;
    }

    private ReactiveAuthenticationManager auth0ReactiveAuthenticationManager(OAuth2ResourceServerProperties oauth2Properties,
            List<Auth0ReactiveAuthenticationProvider> providers) {
        return new Auth0ReactiveAuthenticationManager(oauth2Properties, providers);
    }

    private ReactiveAuthenticationManager opaqueReactiveAuthenticationManager(OAuth2ResourceServerProperties oauth2Properties) {
        if (StringUtils.hasText(oauth2Properties.getOpaquetoken().getIntrospectionUri())) {
            NimbusReactiveOpaqueTokenIntrospector nimbusReactiveOpaqueTokenIntrospector = new NimbusReactiveOpaqueTokenIntrospector(
                    oauth2Properties.getOpaquetoken().getIntrospectionUri(),
                    Optional.ofNullable(oauth2Properties.getOpaquetoken().getClientId()).orElse(""),
                    Optional.ofNullable(oauth2Properties.getOpaquetoken().getClientSecret()).orElse(""));
            return new OpaqueTokenReactiveAuthenticationManager(
                    new AuthUserDetailsConverterIntrospector(nimbusReactiveOpaqueTokenIntrospector));
        }
        LOGGER.debug(
                "spring.security.oauth2.resourceserver.opaque-token.introspection-uri has no value: no opaque token authentication will be available");
        return null;
    }
}
