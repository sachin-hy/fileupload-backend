package com.fileupload.fileproject.service;

// JUnit 5
import com.fileupload.fileproject.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import com.fileupload.fileproject.repository.UserRepository;
import com.fileupload.fileproject.util.JwtUtil;


import java.util.*;

@ExtendWith(MockitoExtension.class)
public class GoogleServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityCustomService securityCustomService;

    @Mock private JwtUtil jwtUtil;

    @Mock WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock WebClient.RequestBodySpec requestBodySpec;
    @Mock WebClient.ResponseSpec responseSpec;
    @Mock Mono<Map> mono;
    @Mock WebClient.RequestHeadersSpec requestHeadersSpec;

   @Mock WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Spy
    @InjectMocks
    private GoogleService googleService;



    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleService, "tokenEndpoint", "http://fake-token.com");
        ReflectionTestUtils.setField(googleService, "userInfoEndpoint", "http://fake-user.com");
        ReflectionTestUtils.setField(googleService, "clientId", "fake-client-id");
        ReflectionTestUtils.setField(googleService, "clientSecret", "fake-secret");
        ReflectionTestUtils.setField(googleService, "redirectUri", "http://fake-redirect.com");
    }



    @Test
    public void testhandleGoogleCallback_Success()
    {
        String code = "democode";

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token","demo_access_token");
        tokenResponse.put("id_token","demo_id_token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);


        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email","abcd@gmail.com");
        userInfo.put("name","abcd");

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);



        when(responseSpec.bodyToMono(eq(Map.class))).thenReturn(mono);
        when(mono.block()).thenReturn(tokenResponse, userInfo);

        String fakeToken = "demotoken";
        doReturn(fakeToken).when(googleService).createToken(anyString(),anyString());

        String result = googleService.handleGoogleCallback(code);

        assertEquals(fakeToken, result);
    }





    @Test
    public void testCreateToken_Success_NewUser() {

        String email = "abcd@gmail.com";
        String name = "abcd";


        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());


        Users savedUser = new Users();
        savedUser.setEmail(email);
        savedUser.setFirstName(name);
        savedUser.setLastName(name);
        savedUser.setPassword("encodedPassword");

        when(userRepository.save(any(Users.class))).thenReturn(savedUser);


        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(email);
        when(mockUserDetails.getAuthorities()).thenReturn((Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        when(securityCustomService.loadUserByUsername(email)).thenReturn(mockUserDetails);


        String expectedToken = "demotoken";
        when(jwtUtil.generateToken(email)).thenReturn(expectedToken);


        when(passwordEncoder.encode(name)).thenReturn("encodedPassword");


        String resultToken = googleService.createToken(email, name);


        assertEquals(expectedToken, resultToken);


        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(Users.class));


        verify(securityCustomService, times(1)).loadUserByUsername(email);


        verify(jwtUtil, times(1)).generateToken(email);


        verify(passwordEncoder, times(1)).encode(name);

        // Verify authentication was set in SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
        assertEquals(mockUserDetails, authentication.getPrincipal());
        assertEquals(mockUserDetails.getAuthorities(), authentication.getAuthorities());
    }

}
