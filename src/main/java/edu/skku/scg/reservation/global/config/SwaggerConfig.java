package edu.skku.scg.reservation.global.config;

import edu.skku.scg.reservation.domain.auth.common.AuthConstants;
import edu.skku.scg.reservation.global.annotation.AdminApi;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.annotation.PublicApi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @PostConstruct
    public void initSwaggerConfig() {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(ManagementUnitId.class);
    }

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
                .description("""
                        성균관대학교 공간 예약 시스템을 위한 전용 API 문서입니다.
                        
                        각 API 제목 앞의 뱃지를 통해 필요한 권한을 확인할 수 있습니다.
                        * ⚪ **[Public]** : 로그인 불필요
                        * 🔵 **[Authenticated]** : 로그인 필요
                        * 🔴 **[Admin]** : 관리자 전용""");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public OperationCustomizer customizeAuthOperations() {
        return (operation, handlerMethod) -> {

            boolean isPublic = handlerMethod.getMethodAnnotation(PublicApi.class) != null;
            boolean isCollegeAdmin = handlerMethod.getMethodAnnotation(AdminApi.class) != null;

            String prefix;

            if (isPublic) {
                prefix = "⚪ ";
                operation.setSecurity(Collections.emptyList());
            } else if (isCollegeAdmin) {
                prefix = "🔴 ";
            } else {
                prefix = "🔵 ";
            }

            String originalSummary = operation.getSummary() != null ? operation.getSummary() : "";
            operation.setSummary(prefix + originalSummary);

            return operation;
        };
    }
}