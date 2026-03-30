package edu.skku.scg.reservation.global.resolver;

import edu.skku.scg.reservation.domain.organization.service.OriginService;
import edu.skku.scg.reservation.global.annotation.ManagementUnitId;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import edu.skku.scg.reservation.global.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

        if (!StringUtils.hasText(originUrl)) {
            String secFetchSite = request.getHeader("Sec-Fetch-Site");

            if ("same-origin".equals(secFetchSite)) {
                originUrl = HttpUtils.reconstructOrigin(request);
            } else {
                throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
            }
        }

        return originService.getManagementUnitId(originUrl);
    }
}