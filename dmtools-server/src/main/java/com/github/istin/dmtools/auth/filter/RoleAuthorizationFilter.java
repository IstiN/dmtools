package com.github.istin.dmtools.auth.filter;

import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter component for role-based access control validation
 */
@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RoleAuthorizationFilter.class);
    
    public RoleAuthorizationFilter(UserService userService) {
        // UserService not used in current implementation but kept for future enhancements
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Role authorization check for: {} {}", method, requestUri);
        
        // Check if this is an admin endpoint
        if (requestUri.startsWith("/api/admin")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access attempt to admin endpoint: {} {}", method, requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication required\",\"code\":\"UNAUTHORIZED\"}");
                return;
            }
            
            // Check if user has admin role
            boolean hasAdminRole = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    
            if (!hasAdminRole) {
                // Get user email for logging
                String userEmail = "unknown";
                if (authentication.getPrincipal() instanceof UserDetails) {
                    userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
                }
                
                logger.warn("Access denied to admin endpoint for user {}: {} {}", userEmail, method, requestUri);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Admin access required\",\"code\":\"FORBIDDEN\"}");
                return;
            }
            
            logger.debug("Admin access granted for: {} {}", method, requestUri);
        }
        
        filterChain.doFilter(request, response);
    }
}
