package edu.skku.scg.reservation.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SwaggerSecurityConfig {

    private final String swaggerId;
    private final String swaggerPassword;

    public SwaggerSecurityConfig(
            @Value("${swagger.user.id}") String swaggerId,
            @Value("${swagger.user.password}") String swaggerPassword) {
        this.swaggerId = swaggerId;
        this.swaggerPassword = swaggerPassword;
    }

    @Bean
    @Profile("dev")
    @Order(1)
    public SecurityFilterChain swaggerDevFilterChain(HttpSecurity http) throws Exception {
        UserDetails admin = User.builder()
                .username(swaggerId)
                .password("{noop}" + swaggerPassword)
                .roles("ADMIN")
                .build();
        UserDetailsService inMemoryUserDetailsService = new InMemoryUserDetailsManager(admin);

        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .userDetailsService(inMemoryUserDetailsService)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Profile("local")
    @Order(1)
    public SecurityFilterChain swaggerLocalFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}