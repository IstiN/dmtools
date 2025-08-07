package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final String jwtSecret;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserService userService, 
                                 @Value("${jwt.secret}") String jwtSecret) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.jwtSecret = jwtSecret;
        logger.info("🔍 JWT FILTER - Initialized with jwtSecret length: {}", jwtSecret.length());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("🔍 JWT FILTER - Processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            logger.info("🔍 JWT FILTER - JWT token found: {}", jwt != null ? "YES" : "NO");
            
            if (jwt != null && jwtUtils.validateJwtTokenCustom(jwt, jwtSecret)) {
                String email = jwtUtils.getEmailFromJwtTokenCustom(jwt, jwtSecret);
                String userId = jwtUtils.getUserIdFromJwtTokenCustom(jwt, jwtSecret);
                
                logger.info("🔍 JWT AUTH - Valid JWT found for email: {}, userId: {}", email, userId);
                
                // Load user from database
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Get user roles and convert to authorities
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRoles() != null) {
                        for (String role : user.getRoles()) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                    if (authorities.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_REGULAR_USER"));
                    }
                    
                    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("") // Password is not needed as we use JWT
                        .authorities(authorities)
                        .build();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("✅ JWT AUTH - Authentication set for user: {}", userDetails.getUsername());
                } else {
                    logger.warn("❌ JWT AUTH - User not found in database for email: {}", email);
                }
            } else if (jwt != null) {
                logger.warn("❌ JWT AUTH - Invalid JWT token");
            } else {
                logger.debug("🔍 JWT AUTH - No JWT token found in request");
            }
        } catch (Exception e) {
            logger.error("❌ JWT AUTH - Cannot set user authentication: {}", e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // First try to get JWT from Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // Then try to get JWT from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
} 