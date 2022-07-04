package org.talend.daikon.spring.reactive.sat.introspection;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.talend.daikon.spring.auth.common.model.userdetails.UserDetailsConverter;

import reactor.core.publisher.Mono;

public class AuthUserDetailsConverterIntrospector implements ReactiveOpaqueTokenIntrospector {

    private final ReactiveOpaqueTokenIntrospector delegate;

    public AuthUserDetailsConverterIntrospector(ReactiveOpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
        return delegate.introspect(token).map(principal -> UserDetailsConverter.convert(principal.getAttributes()));
    }
}
