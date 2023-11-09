package uk.gov.hmcts.juror.standard.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uk.gov.hmcts.juror.standard.components.JwtAuthenticationEntryPoint;
import uk.gov.hmcts.juror.standard.components.filters.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;


    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationProvider authenticationProvider,
                          JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    @Order(1)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(requestMatcherRegistry -> {
                requestMatcherRegistry.requestMatchers(AntPathRequestMatcher.antMatcher("/auth/**")).permitAll();
                requestMatcherRegistry.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exc -> exc.authenticationEntryPoint(authenticationEntryPoint))
            .authenticationProvider(authenticationProvider)
            .build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return web -> web
            .ignoring()
            .requestMatchers(
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/health/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/info"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/metrics/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/metrics"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/swagger-resources/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/v3/api-docs/**"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/swagger-ui.html"),
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/swagger-ui/**")
            );
    }
}
