package com.fileupload.fileproject.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.fileproject.context.TenantContext;
import com.fileupload.fileproject.context.UserIDContext;
import com.fileupload.fileproject.enums.RateLimitRule;
import com.fileupload.fileproject.service.RateLimitService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        Long tenantId = TenantContext.getTenantId();

        RateLimitRule rule = rateLimitService.resolveRule(path);

        String bucketOwnerKey;
        if(tenantId == null){
            bucketOwnerKey = "ip:" + request.getRemoteAddr();
        }else{
            Long userId = UserIDContext.getUserId();
            bucketOwnerKey = "tenant:" + tenantId + ":user:" + userId;
        }

        Bucket bucket = rateLimitService.resolveBucket(bucketOwnerKey, rule);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            System.out.println("Bucket key = " + bucketOwnerKey);
            System.out.println("X-RateLimit-Remaining"+ String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "RATE_LIMITED");
            body.put("message", "Too many requests.");
            body.put("code", 429);

            response.getWriter().write(objectMapper.writeValueAsString(body));
        }

    }
}
