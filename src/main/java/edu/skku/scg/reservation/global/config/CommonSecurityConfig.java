package edu.skku.scg.reservation.global.config;

import edu.skku.scg.reservation.domain.auth.jwt.JwtAuthenticationFilter;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class CommonSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApplicationContext applicationContext;
    private final HandlerExceptionResolver exceptionResolver;

    CommonSecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ApplicationContext applicationContext,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.applicationContext = applicationContext;
        this.exceptionResolver = exceptionResolver;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/error",
                        "/favicon.ico"
                );
    }

    @Bean
    public SecurityFilterChain commonFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(getPublicUrls()).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            exceptionResolver.resolveException(request, response, null, new BusinessException(ErrorCode.UNAUTHENTICATED));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            exceptionResolver.resolveException(request, response, null, new BusinessException(ErrorCode.ACCESS_DENIED));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private RequestMatcher[] getPublicUrls() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();

        return handlerMethods.entrySet().stream()
                .filter(entry -> entry.getValue().hasMethodAnnotation(PublicApi.class))
                .flatMap(entry -> {
                    RequestMappingInfo info = entry.getKey();

                    Set<String> patterns;
                    if (info.getPathPatternsCondition() != null) {
                        patterns = info.getPathPatternsCondition().getPatternValues();
                    } else if (info.getPatternsCondition() != null) {
                        patterns = info.getPatternsCondition().getPatterns();
                    } else {
                        patterns = Set.of();
                    }

                    Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

                    if (methods.isEmpty()) {
                        return patterns.stream().map(builder::matcher);
                    }

                    return patterns.stream()
                            .flatMap(pattern -> methods.stream()
                                    .map(method -> builder.matcher(HttpMethod.valueOf(method.name()), pattern)));
                })
                .toArray(RequestMatcher[]::new);
    }
}