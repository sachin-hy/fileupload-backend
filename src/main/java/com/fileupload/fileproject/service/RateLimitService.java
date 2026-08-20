package com.fileupload.fileproject.service;


import com.fileupload.fileproject.enums.PlanType;
import com.fileupload.fileproject.enums.RateLimitRule;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<byte[]> proxyManager;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RateLimitRule resolveRule(String requestPath) {
        for (RateLimitRule rule : RateLimitRule.values()) {
            if (pathMatcher.match(rule.getUrlPattern(), requestPath)) {
                return rule;
            }
        }
        return RateLimitRule.DEFAULT;
    }

    public Bucket resolveBucket(String ownerKey, RateLimitRule rule) {

        String keyString = "rl:%s:%s".formatted(ownerKey, rule.name());
        byte[] key = keyString.getBytes(StandardCharsets.UTF_8);

        int capacity = rule.getBaseCapacity();
        int refill = rule.getBaseRefillPerMinute();

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refill, Duration.ofMinutes(1))
                        .build())
                .build();

        return proxyManager.builder().build(key, configSupplier);

    }
}
