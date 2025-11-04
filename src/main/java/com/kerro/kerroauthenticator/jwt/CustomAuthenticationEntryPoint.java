package com.kerro.kerroauthenticator.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {


    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();
        String authHeader = request.getHeader("Authorization");

        log.warn("Unauthorized request: method={} path={} remoteAddr={} authHeaderPresent={} reason={}",
                method, path, remoteAddr, authHeader != null, safeMessage(authException));

        if (log.isDebugEnabled()) {
            log.debug("Authentication exception stacktrace: ", authException);
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String msg = String.format("{\"error\":\"unauthorized\",\"message\":\"Unauthorized access: %s\"}",
                sanitizeForClient(authException.getMessage()));
        response.getWriter().write(msg);
    }

    private String safeMessage(Exception e) {
        return e == null ? "N/A" : sanitizeForLog(e.getMessage());
    }

    private String sanitizeForLog(String s) {
        if (s == null) return "N/A";
        return s.replaceAll("[\\r\\n\\t]+", " ");
    }

    private String sanitizeForClient(String s) {
        if (s == null) return "Restricted access";
        return s.length() > 100 ? s.substring(0, 100) + "..." : s;
    }
}
