package com.fileupload.fileproject.service;

import com.fileupload.fileproject.enums.RateLimitRule;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimitServiceTest {

    @SuppressWarnings("unchecked")
    private final ProxyManager<byte[]> proxyManager = mock(ProxyManager.class, RETURNS_DEEP_STUBS);
    private final RateLimitService service = new RateLimitService(proxyManager);

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "/api/private/files/initiate,        FILE_INITIATE",
            "/api/private/files/presigned-url,   FILE_PRESIGNED_URL",
            "/api/private/files/complete,        FILE_COMPLETE",
            "/api/private/download/42,           FILE_DOWNLOAD",
            "/api/private/files,                 FILE_LIST",
            "/api/private/files/count,           FILES_COUNT",
            "/api/private/shares/me,             SHARES_ME",
            "/api/private/shares,                SHARE_FILE",
            "/api/private/share,                 SHARE",
            "/api/private/recent-activity,       RECENT_ACTIVITY",
            "/api/private/users/count,           USERS_COUNT",
            "/api/private/users/files/count,     USERS_FILES_COUNT",
            "/api/private/invitations/send,      SEND_INVITATIONS",
            "/api/public/invitation/register/abc,COMPLETE_REGISTRATION",
            "/api/public/login,                  LOGIN",
            "/api/public/plans,                  PUBLIC_PLANS",
            "/api/private/tenant/upgrade,        TENANT_UPGRADE",
            "/api/public/register/tenant,        TENANT_REGISTER",
            "/api/private/members,               TEAM_MEMBERS",
            "/api/private/storageDetails,        STORAGE_DETAILS",
    })
    void resolveRule_matchesKnownEndpoints(String path, RateLimitRule expected) {
        assertEquals(expected, service.resolveRule(path));
    }

    @ParameterizedTest(name = "{0} falls back to DEFAULT")
    @CsvSource({
            "/unknown",
            "/api/private/does-not-exist",
            "/",
    })
    void resolveRule_unknownPath_fallsBackToDefault(String path) {
        assertEquals(RateLimitRule.DEFAULT, service.resolveRule(path));
    }

    @Test
    void resolveBucket_usesRuleScopedKeyAndConfiguredCapacity() {
        service.resolveBucket("tenant:1:user:2", RateLimitRule.FILE_INITIATE);

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Supplier<BucketConfiguration>> configCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(proxyManager.builder()).build(keyCaptor.capture(), configCaptor.capture());

        assertEquals("rl:tenant:1:user:2:FILE_INITIATE",
                new String(keyCaptor.getValue(), StandardCharsets.UTF_8));
        assertEquals(RateLimitRule.FILE_INITIATE.getBaseCapacity(),
                configCaptor.getValue().get().getBandwidths()[0].getCapacity());
    }

    @Test
    void resolveBucket_differentRulesForSameOwner_getDifferentKeys() {
        service.resolveBucket("ip:1.2.3.4", RateLimitRule.LOGIN);
        service.resolveBucket("ip:1.2.3.4", RateLimitRule.PUBLIC_PLANS);

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(proxyManager.builder(), times(2)).build(keyCaptor.capture(),
                ArgumentMatchers.<Supplier<BucketConfiguration>>any());

        String first = new String(keyCaptor.getAllValues().get(0), StandardCharsets.UTF_8);
        String second = new String(keyCaptor.getAllValues().get(1), StandardCharsets.UTF_8);
        assertEquals("rl:ip:1.2.3.4:LOGIN", first);
        assertEquals("rl:ip:1.2.3.4:PUBLIC_PLANS", second);
    }
}
