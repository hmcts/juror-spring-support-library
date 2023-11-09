package uk.gov.hmcts.juror.standard.components.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals"
})
class AbstractJwtAuthenticationFilterTest {

    private AbstractJwtAuthenticationFilter jwtAuthenticationFilter;


    private JwtService jwtService;

    private HttpServletRequest request;

    private HttpServletResponse response;
    private FilterChain filterChain;

    private SecurityContext securityContext;

    private MockedStatic<SecurityContextHolder> securityContextHolder;


    @BeforeEach
    public void beforeEach() throws Exception {
        this.jwtService = mock(JwtService.class);
        this.request = mock(HttpServletRequest.class);
        this.response = mock(HttpServletResponse.class);
        this.filterChain = mock(FilterChain.class);
        this.securityContext = mock(SecurityContext.class);

        this.securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        this.securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(this.securityContext);
        this.jwtAuthenticationFilter = spy(new AbstractJwtAuthenticationFilter(this.jwtService) {
            @Override
            UserDetails getUserDetails(String jwt) {
                return null;//This should be mocked in tests
            }
        });
    }

    @AfterEach
    public void afterEach() {
        securityContextHolder.close();
    }


    @Test
    void positiveAuthenticatedUser() throws ServletException, IOException {
        Set<GrantedAuthority> grantedAuthorities = Set.of(
            new SimpleGrantedAuthority("value1"),
            new SimpleGrantedAuthority("value2"),
            new SimpleGrantedAuthority("value3")
        );

        when(request.getHeader("Authorization")).thenReturn(TestConstants.AUTH_HEADER);
        when(jwtService.extractEmail(TestConstants.JWT)).thenReturn(TestConstants.EMAIL);
        UserDetails userDetails = mock(UserDetails.class);
        doReturn(grantedAuthorities).when(userDetails).getAuthorities();


        when(jwtAuthenticationFilter.getUserDetails(TestConstants.JWT)).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.isJwtValid(TestConstants.JWT, userDetails)).thenReturn(true);


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, timeout(1)).getUserDetails(TestConstants.JWT);
        verify(jwtService, timeout(1)).isJwtValid(TestConstants.JWT, userDetails);
        verify(securityContext, timeout(1)).getAuthentication();

        final ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(securityContext, timeout(1)).setAuthentication(authenticationCaptor.capture());

        Authentication authentication = authenticationCaptor.getValue();
        assertEquals(userDetails, authentication.getPrincipal(), "Principal must match");
        assertNull(authentication.getCredentials(), "Credentials must match");
        assertEquals(grantedAuthorities.size(), authentication.getAuthorities().size(), "Size must match");

        assertNotNull(authentication.getDetails(), "Auth details must not be null");
        assertInstanceOf(WebAuthenticationDetails.class, authentication.getDetails(), "Auth details must be instance "
            + "of WebAuthenticationDetails");

        verify(securityContext, timeout(1)).setAuthentication(authentication);
        verify(filterChain, timeout(1)).doFilter(request, response);
    }

    @Test
    void negativeNullAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, never()).getUserDetails(any());
        verify(jwtService, never()).isJwtValid(any(), any());
        verify(securityContext, never()).getAuthentication();
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, timeout(1)).doFilter(request, response);
    }

    @Test
    void negativeAuthHeaderNotBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic authentication");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, never()).getUserDetails(any());
        verify(jwtService, never()).isJwtValid(any(), any());
        verify(securityContext, never()).getAuthentication();

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, timeout(1)).doFilter(request, response);
    }

    @Test
    void negativeUserDetailsNotFound() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(TestConstants.AUTH_HEADER);
        when(jwtAuthenticationFilter.getUserDetails(TestConstants.JWT)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, times(1)).getUserDetails(any());
        verify(jwtService, never()).isJwtValid(any(), any());
        verify(securityContext, never()).getAuthentication();

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, timeout(1)).doFilter(request, response);
    }

    @Test
    void negativeAlreadyAuthenticated() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(TestConstants.AUTH_HEADER);
        when(jwtService.extractEmail(TestConstants.JWT)).thenReturn(TestConstants.EMAIL);
        when(jwtAuthenticationFilter.getUserDetails(TestConstants.JWT)).thenReturn(mock(UserDetails.class));
        when(securityContext.getAuthentication()).thenReturn(mock(Authentication.class));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, times(1)).getUserDetails(any());
        verify(jwtService, never()).isJwtValid(any(), any());
        verify(securityContext, timeout(1)).getAuthentication();

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, timeout(1)).doFilter(request, response);
    }

    @Test
    void negativeJwtNotValid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(TestConstants.AUTH_HEADER);
        when(jwtService.extractEmail(TestConstants.JWT)).thenReturn(TestConstants.EMAIL);
        UserDetails userDetails = mock(UserDetails.class);
        when(jwtAuthenticationFilter.getUserDetails(TestConstants.JWT)).thenReturn(userDetails);

        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtService.isJwtValid(TestConstants.JWT, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, timeout(1)).getHeader("Authorization");
        verify(jwtAuthenticationFilter, times(1)).getUserDetails(any());
        verify(jwtService, timeout(1)).isJwtValid(TestConstants.JWT, userDetails);
        verify(securityContext, timeout(1)).getAuthentication();

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain, timeout(1)).doFilter(request, response);
    }
}
