package edu.skku.scg.reservation.global.util;

import com.google.common.net.HttpHeaders;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.net.URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static String extractOrigin(HttpServletRequest request) {
        String originUrl = request.getHeader(HttpHeaders.ORIGIN);
        if (StringUtils.hasText(originUrl)) {
            return originUrl;
        }

        String referer = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.hasText(referer)) {
            try {
                URI uri = URI.create(referer);
                String scheme = uri.getScheme();
                String authority = uri.getAuthority();

                if (StringUtils.hasText(scheme) && StringUtils.hasText(authority) &&
                        (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    return scheme + "://" + authority;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        String secFetchSite = request.getHeader("Sec-Fetch-Site");

        if ("none".equals(secFetchSite) || "same-origin".equals(secFetchSite)) {
            return reconstructOrigin(request);
        }

        throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
    }

    private static String reconstructOrigin(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();

        if (!StringUtils.hasText(scheme) || !StringUtils.hasText(serverName)) {
            throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
        }

        StringBuilder originBuilder = new StringBuilder();
        originBuilder.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && port != 80 && port != -1) ||
                ("https".equals(scheme) && port != 443 && port != -1)) {
            originBuilder.append(":").append(port);
        }

        return originBuilder.toString();
    }
}