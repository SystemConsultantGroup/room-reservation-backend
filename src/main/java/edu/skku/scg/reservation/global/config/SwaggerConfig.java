package edu.skku.scg.reservation.global.config;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String cookieName = AuthConstants.ACCESS_TOKEN_COOKIE_NAME;

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(cookieName);

        Components components = new Components()
                .addSecuritySchemes(cookieName, new SecurityScheme()
                        .name(cookieName)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                );

        Info info = new Info()
                .title("Room Reservation API 명세서")
                .version("v1.0.0")
                .description("성균관대학교 공간 예약 시스템을 위한 전용 API 문서입니다.");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}