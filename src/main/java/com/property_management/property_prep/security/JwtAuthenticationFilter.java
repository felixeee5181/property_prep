package com.property_management.property_prep.security;

import com.property_management.property_prep.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Get the Authorization header from the incoming request
        final String authHeader = request.getHeader("Authorization");

        // 2. If the header is missing, or doesn't start with "Bearer ", let the request pass (they will get a 403 later)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cut off "Bearer " (7 characters) to get the actual token
        final String token = authHeader.substring(7);

        // 4. Extract the username from the token using our JwtUtil
        final String username = jwtUtil.extractUsername(token);

        // 5. If we found a username, and the user isn't already authenticated in this request...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load the user details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validate the token (check signature + expiration)
            if (jwtUtil.validateToken(token, userDetails.getUsername())) {

                // 8. Create an authentication object (this tells Spring "This user is logged in")
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 9. Attach request details (IP address, session info, etc.) – best practice
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 10. Set the authentication into the SecurityContext (this is what makes Spring Security know the user!)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}