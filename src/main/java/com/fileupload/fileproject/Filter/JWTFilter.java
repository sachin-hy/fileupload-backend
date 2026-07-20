package com.fileupload.fileproject.Filter;

import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;




@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher;

    public JWTFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // skip public apis
        if (pathMatcher.match("/api/public/**", path)) {
            filterChain.doFilter(request, response);
            return;
        }



        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7);

            // validate token (signature + expiration)
//            if (!jwtUtil.validateToken(token)) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid or expired token");
//                return;
//            }

            // extract the claims
            Claims claims = jwtUtil.getClaimsFromToken(token);
            //get username which is = to email
            String username = claims.getSubject();

            // extract tenant info
            Long tenantId = claims.get("tenantid", Long.class);
            String tenantKey = claims.get("tenantkey", String.class);

            ///  set tenant context
            if (tenantId != null && tenantKey != null) {
                TenantContext.setContext(tenantId, tenantKey);
                log.info("Tenant Context set: ID={}, Key={}", tenantId, tenantKey);
            }

            // extract role from JWT
            String role = claims.get("role", String.class);

            List<SimpleGrantedAuthority> authorities = role != null ? List.of(new SimpleGrantedAuthority(role)) : List.of();

            // Create authentication object
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                               username,
                                                           null,
                                                              authorities
                                                                  );
                                // used to store the remoteaddress ( ip address and seesion id if present)
            // inside auth token
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // now store this auth token inside the security context holder
            SecurityContextHolder.getContext().setAuthentication(authToken);

            //pass the filter
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");

        } catch (SignatureException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token signature");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Unexpected error occurred");

        } finally {
            // at end make sure to clear the tenantcontext
            TenantContext.clear();
            log.debug("TenantContext cleared");
        }
    }
}