package edu.skku.scg.reservation.global.util;

import com.google.common.net.HttpHeaders;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static String extractOrigin(HttpServletRequest request) {
        String currentDetectedHost = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();

        String originHeader = request.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.hasText(originHeader) && !originHeader.equalsIgnoreCase(currentDetectedHost)) {
            return originHeader;
        }

        String referer = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.hasText(referer)) {
            try {
                URI uri = URI.create(referer);
                String scheme = uri.getScheme();
                String authority = uri.getAuthority();
                if (StringUtils.hasText(scheme) && StringUtils.hasText(authority)) {
                    return scheme + "://" + authority;
                }
            } catch (Exception ignored) {}
        }

        if (StringUtils.hasText(currentDetectedHost)) {
            return currentDetectedHost;
        }

        throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
    }
}