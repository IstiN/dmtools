package com.github.istin.dmtools.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(true);
        filter.setMaxPayloadLength(1000);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

    @Bean
    public Filter customRequestLoggingFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                
                long startTime = System.currentTimeMillis();
                
                logger.info("🌐 INCOMING REQUEST: {} {} from {} [{}]", 
                    httpRequest.getMethod(), 
                    httpRequest.getRequestURI(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent"));
                
                logger.info("📋 Request Headers:");
                java.util.Enumeration<String> headerNames = httpRequest.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    logger.info("   {}: {}", headerName, httpRequest.getHeader(headerName));
                }
                
                System.out.println("🌐 HTTP Request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI() + " from " + httpRequest.getRemoteAddr());
                
                try {
                    chain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("✅ RESPONSE: {} {} -> {} ({} ms)", 
                        httpRequest.getMethod(), 
                        httpRequest.getRequestURI(),
                        httpResponse.getStatus(),
                        duration);
                    System.out.println("✅ HTTP Response: " + httpResponse.getStatus() + " (" + duration + " ms)");
                }
            }
        };
    }
} 