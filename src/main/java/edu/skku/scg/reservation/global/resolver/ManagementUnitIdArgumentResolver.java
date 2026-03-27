package edu.skku.scg.reservation.global.resolver;

import edu.skku.scg.reservation.domain.organization.service.OriginService;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class ManagementUnitIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final OriginService originService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(ManagementUnitId.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String originUrl = request.getHeader(HttpHeaders.ORIGIN);

        if (originUrl == null || originUrl.isBlank()) {
            String secFetchSite = request.getHeader("Sec-Fetch-Site");

            if ("same-origin".equals(secFetchSite)) {
                String scheme = getScheme(request);
                String host = getHost(request);
                originUrl = scheme + "://" + host;
            } else {
                throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
            }
        }

        return originService.getManagementUnitId(originUrl);
    }

    private String getHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        return (forwardedHost != null && !forwardedHost.isBlank()) ? forwardedHost : request.getHeader(HttpHeaders.HOST);
    }

    private String getScheme(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : request.getScheme();
    }
}