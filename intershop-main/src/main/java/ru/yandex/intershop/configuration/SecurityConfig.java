package ru.yandex.intershop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .anonymous(anonymous -> anonymous
                        .principal("anonymousUser")
                        .authorities("ROLE_ANONYMOUS"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/",
                                "/login",
                                "/main/items",
                                "/items/**",
                                "/images/**").permitAll()
                        .anyExchange().authenticated())
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .formLogin(form -> form
                        .loginPage("/login"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((webFilterExchange, authentication) -> webFilterExchange.getExchange().getSession()
                                .flatMap(WebSession::invalidate)
                                .then(Mono.defer(() -> {
                                    ServerHttpResponse response = webFilterExchange.getExchange().getResponse();

                                    response.getCookies().remove("JSESSIONID");
                                    response.setStatusCode(HttpStatus.FOUND);
                                    response.getHeaders().setLocation(URI.create("/main/items"));
                                    return response.setComplete();
                                }))))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}