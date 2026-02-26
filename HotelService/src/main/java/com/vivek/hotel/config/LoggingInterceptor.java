package com.vivek.hotel.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("remoteAddr", request.getRemoteAddr());
        
        log.info("Incoming request: method={}, uri={}, remoteAddr={}", 
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr());
        
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Request completed: requestId={}, status={}, duration={}ms", 
            MDC.get("requestId"),
            response.getStatus(),
            duration);
        
        if (ex != null) {
            log.error("Request failed with exception", ex);
        }
        
        MDC.clear();
    }
}
