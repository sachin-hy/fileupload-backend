package com.fileupload.fileproject.Filter;


import com.fileupload.fileproject.service.SecurityCustomService;

import com.fileupload.fileproject.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.protocol.RequestAddCookies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;




@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private SecurityCustomService securityCustomService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("Request URL: {} Method: {}", path, request.getMethod());

        // Skip filter for public endpoints
        if (path.equals("/login") ||
                path.equals("/signup") ||
                path.equals("/oauth2/authorization/google") ||
                path.equals("/oauth2/callback/google") ||
                path.equals("/error") ||
                path.equals("/download")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            String token = null;
            String username = null;

            if(authHeader != null && authHeader.startsWith("Bearer"))
            {
                token = authHeader.substring(7);
                username=jwtUtil.extractUsername(token);

            }else
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }


            if(jwtUtil.isTokenExpired(token))
            {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid Token");
                return;
            }

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null )
            {
                UserDetails userDetails = securityCustomService.loadUserByUsername(username);

                if(jwtUtil.validateToken(token,userDetails))
                {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }else
                {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid Token");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        }catch(ExpiredJwtException e)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("Invalid Token");
        }catch(SignatureException | MalformedJwtException e)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token\"}");
        }catch(Exception e)
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"An unexpected error occurred\"}");
        }
    }



}