package edu.skku.scg.reservation.global.util;

import com.google.common.net.HttpHeaders;
import edu.skku.scg.reservation.global.exception.BusinessException;
import edu.skku.scg.reservation.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static String extractOrigin(HttpServletRequest request) {
        String originUrl = request.getHeader(HttpHeaders.ORIGIN);

        if (StringUtils.hasText(originUrl)) {
            return originUrl;
        }

        String secFetchSite = request.getHeader("Sec-Fetch-Site");
        if (!"same-origin".equals(secFetchSite)) {
            throw new BusinessException(ErrorCode.UNREGISTERED_ORIGIN);
        }

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