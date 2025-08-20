package uk.gov.hmcts.juror.standard.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.service.exceptions.UnauthorisedException;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtServiceImpl.class})
@DisplayName("JwtServiceImpl")
@SpringBootTest(properties = {
    "uk.gov.hmcts.juror.security.token-validity=" + TestConstants.TOKEN_VALIDITY,
    "uk.gov.hmcts.juror.security.secret=" + TestConstants.JWT_SECRET})
@SuppressWarnings({
    "unchecked",
    "PMD.ExcessiveImports",
    "PMD.AvoidDuplicateLiterals"
})
class JwtServiceImplTest {

    @Autowired
    private JwtServiceImpl jwtService;

    @MockitoBean
    private Clock clock;

    private Date currentDate;

    private MockedStatic<Jwts> jwtsMockedStatic;

    @BeforeEach
    void beforeEach() {
        jwtsMockedStatic = Mockito.mockStatic(Jwts.class);
        currentDate = new Date(System.currentTimeMillis());
        when(clock.millis()).thenReturn(currentDate.getTime());
    }

    @AfterEach
    void afterEach() {
        jwtsMockedStatic.close();
    }

    private Claims setupValidJwtMock() {
        JwtParserBuilder jwtParserBuilder = mock(JwtParserBuilder.class);
        JwtParser jwtParser = mock(JwtParser.class);
        Jws<Claims> jwtClaims = mock(Jws.class);
        Claims claims = mock(Claims.class);
        jwtsMockedStatic.when(Jwts::parser).thenReturn(jwtParserBuilder);
        when(jwtParserBuilder.verifyWith(any(SecretKey.class))).thenReturn(jwtParserBuilder);
        when(jwtParserBuilder.build()).thenReturn(jwtParser);
        when(jwtParser.parseSignedClaims(TestConstants.JWT_SECRET)).thenReturn(jwtClaims);
        when(jwtClaims.getPayload()).thenReturn(claims);
        return claims;
    }

    private JwtBuilder setupJwtTokenGenerator() {
        JwtBuilder jwtBuilder = mock(JwtBuilder.class);
        jwtsMockedStatic.when(Jwts::builder).thenReturn(jwtBuilder);


        when(jwtBuilder.id(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.issuer(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.claims(anyMap())).thenReturn(jwtBuilder);
        when(jwtBuilder.subject(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.issuedAt(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.expiration(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.signWith(any())).thenReturn(jwtBuilder);
        when(jwtBuilder.compact()).thenReturn(TestConstants.JWT_SECRET);
        return jwtBuilder;
    }

    @DisplayName("public String extractEmail(String jwt)")
    @Nested
    class ExtractEmail {

        @Test
        void positiveEmailFound() {
            Claims claims = setupValidJwtMock();
            String subject = "schedular@cgi.com";
            when(claims.getSubject()).thenReturn(subject);
            assertEquals(subject, jwtService.extractEmail(TestConstants.JWT_SECRET),
                "JWT Subject must match");
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeEmailNotFound() {
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(null);
            assertNull(jwtService.extractEmail(TestConstants.JWT_SECRET), "Jwt subject must be null");
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeInvalidJwt() {
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::parser).thenThrow(exception);

            UnauthorisedException unauthorisedException =
                assertThrows(UnauthorisedException.class, () -> jwtService.extractEmail(TestConstants.JWT_SECRET));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }

    @DisplayName("public String extractUsername(String jwt)")
    @Nested
    class ExtractUsername {

        @Test
        void positiveUsernameFound() {
            String subject = "schedular@cgi.com";
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(subject);
            assertEquals(subject, jwtService.extractUsername(TestConstants.JWT_SECRET), "Subject must match");
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeUsernameNotFound() {
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(null);
            assertNull(jwtService.extractUsername(TestConstants.JWT_SECRET), "Subject must be null");
            verify(claims, times(1)).getSubject();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeInvalidJwt() {
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::parser).thenThrow(exception);

            UnauthorisedException unauthorisedException =
                assertThrows(UnauthorisedException.class, () -> jwtService.extractUsername(TestConstants.JWT_SECRET));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }

    @DisplayName("public Date extractExpiration(String jwt)")
    @Nested
    class ExtractExpiration {

        @Test
        void positiveExpirationDateFound() {
            Date expirationDate = new Date();
            Claims claims = setupValidJwtMock();
            when(claims.getExpiration()).thenReturn(expirationDate);
            assertEquals(expirationDate, jwtService.extractExpiration(TestConstants.JWT_SECRET),
                "Expiration date must match");
            verify(claims, times(1)).getExpiration();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeExpirationDateNotFound() {
            Claims claims = setupValidJwtMock();
            when(claims.getSubject()).thenReturn(null);
            assertNull(jwtService.extractExpiration(TestConstants.JWT_SECRET), "Expiration must be null");
            verify(claims, times(1)).getExpiration();
            verifyNoMoreInteractions(claims);
        }

        @Test
        void negativeInvalidJwt() {
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::parser).thenThrow(exception);

            UnauthorisedException unauthorisedException =
                assertThrows(UnauthorisedException.class, () -> jwtService.extractExpiration(TestConstants.JWT_SECRET));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }

    @DisplayName("public boolean isJwtValid(String jwt, UserDetails userDetails)")
    @Nested
    class IsJwtValid {

        @Test
        void positiveIsValid() {
            Claims claims = setupValidJwtMock();
            String email = "schedular@cgi.com";

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(email);
            when(claims.getSubject()).thenReturn(email);
            when(claims.getExpiration()).thenReturn(new Date(currentDate.getTime() + TestConstants.TOKEN_VALIDITY));

            assertTrue(jwtService.isJwtValid(TestConstants.JWT_SECRET, userDetails), "Jwt must be valid");
        }

        @Test
        void negativeIsInvalidEmailNotMatch() {
            Claims claims = setupValidJwtMock();
            String email = "schedular@cgi.com";

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn("new_" + email);
            when(claims.getSubject()).thenReturn(email);
            when(claims.getExpiration()).thenReturn(new Date(currentDate.getTime() + TestConstants.TOKEN_VALIDITY));

            assertFalse(jwtService.isJwtValid(TestConstants.JWT_SECRET, userDetails), "Jwt must not be valid");

        }

        @Test
        void negativeIsInvalidExpired() {
            Claims claims = setupValidJwtMock();
            String email = "schedular@cgi.com";

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(email);
            when(claims.getSubject()).thenReturn(email);
            when(claims.getExpiration()).thenReturn(new Date(currentDate.getTime() + TestConstants.TOKEN_VALIDITY - 1));

            assertTrue(jwtService.isJwtValid(TestConstants.JWT_SECRET, userDetails), "Jwt must be valid");

        }
    }

    @DisplayName("public String generateJwtToken(UserDetails userDetails)")
    @Nested
    class GenerateJwtTokenUserDetails {
        @Test
        void positiveTokenGenerated() {
            String email = "schedular@cgi.com";
            Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("Role 1"),
                new SimpleGrantedAuthority("Role 2"),
                new SimpleGrantedAuthority("Role 3")
            );


            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(email);
            doReturn(authorities).when(userDetails).getAuthorities();

            JwtBuilder jwtBuilder = setupJwtTokenGenerator();
            assertEquals(TestConstants.JWT_SECRET, jwtService.generateJwtToken(userDetails), "Jwt must match");
            verify(jwtBuilder, times(1)).id(null);
            verify(jwtBuilder, times(1)).issuer(null);
            verify(jwtBuilder, times(1)).subject(email);
            verify(jwtBuilder, times(1)).issuedAt(currentDate);
            verify(jwtBuilder, times(1)).expiration(new Date(currentDate.getTime() + TestConstants.TOKEN_VALIDITY));
            verify(jwtBuilder, times(1)).compact();


            ArgumentCaptor<Key> keyArgumentCaptor = ArgumentCaptor.forClass(Key.class);
            ArgumentCaptor<Map<String, Object>> claimsArgumentCaptor = ArgumentCaptor.forClass(Map.class);

            verify(jwtBuilder, times(1)).signWith(keyArgumentCaptor.capture());
            verify(jwtBuilder, times(1)).claims(claimsArgumentCaptor.capture());

            Key key = keyArgumentCaptor.getValue();
            assertEquals("HmacSHA256", key.getAlgorithm(), "Algorithm must match");
            assertEquals(TestConstants.JWT, new String(key.getEncoded()),
                "Secret must match");

            Map<String, Object> claims = claimsArgumentCaptor.getValue();
            assertEquals(1, claims.size(),"Claim size must match");
            assertNotNull(claims.get("permissions"), "Claim permissions must exist");
            assertInstanceOf(Set.class, claims.get("permissions"), "Permissions must be a instance of Set");
            Set<String> permissions = (Set<String>) claims.get("permissions");
            assertEquals(authorities.size(), permissions.size(), "Permissions size must match");
            assertThat("Permissions must match", permissions, hasItems("Role 1", "Role 2", "Role 3"));
        }

        @Test
        void negativeExceptionRaised() {
            UserDetails userDetails = mock(UserDetails.class);
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::builder).thenThrow(exception);

            UnauthorisedException unauthorisedException =
                assertThrows(UnauthorisedException.class, () -> jwtService.generateJwtToken(userDetails));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }

    @DisplayName("public String generateJwtToken(Map<String, Object> claims, UserDetails userDetails)")
    @Nested
    class GenerateJwtTokenClaimAndUserDetails {
        private Map<String, Object> getClaims() {
            return Map.of(
                "Key", "Value",
                "Key 2", "Value 2",
                "Key 3", "Value 3"
            );
        }

        @Test
        void positiveTokenGenerated() {
            String email = "schedular@cgi.com";
            Map<String, Object> claims = getClaims();
            JwtBuilder jwtBuilder = setupJwtTokenGenerator();
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(email);
            assertEquals(TestConstants.JWT_SECRET, jwtService.generateJwtToken(claims, userDetails),"Jwt must match");


            verify(jwtBuilder, times(1)).id(null);
            verify(jwtBuilder, times(1)).issuer(null);
            verify(jwtBuilder, times(1)).claims(claims);

            verify(jwtBuilder, times(1)).subject(email);
            verify(jwtBuilder, times(1)).issuedAt(currentDate);
            verify(jwtBuilder, times(1)).expiration(new Date(currentDate.getTime() + TestConstants.TOKEN_VALIDITY));
            verify(jwtBuilder, times(1)).compact();


            ArgumentCaptor<Key> keyArgumentCaptor = ArgumentCaptor.forClass(Key.class);

            verify(jwtBuilder, times(1)).signWith(keyArgumentCaptor.capture());

            Key key = keyArgumentCaptor.getValue();
            assertEquals("HmacSHA256", key.getAlgorithm(), "Algorithm must match");
            assertEquals(TestConstants.JWT, new String(key.getEncoded()),
                "Secret must match");

        }

        @Test
        void negativeExceptionRaised() {
            Map<String, Object> claims = getClaims();
            UserDetails userDetails = mock(UserDetails.class);
            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::builder).thenThrow(exception);

            UnauthorisedException unauthorisedException =
                assertThrows(UnauthorisedException.class, () -> jwtService.generateJwtToken(claims, userDetails));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }

    @DisplayName("public String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key "
        + "secretKey, Map<String, Object> claims)")
    @Nested
    class GenerateJwtTokenFull {
        private Map<String, Object> getClaims() {
            return Map.of(
                "Key", "Value",
                "Key 2", "Value 2",
                "Key 3", "Value 3");
        }

        @Test
        void positiveTokenGenerated() {
            String id = "MyId";
            String issuer = "MyIssuer";
            String subject = "MySubject";
            long tokenValidity = 500L;
            Key secretKey = mock(Key.class);
            Map<String, Object> claims = getClaims();

            JwtBuilder jwtBuilder = setupJwtTokenGenerator();

            assertEquals(TestConstants.JWT_SECRET,
                jwtService.generateJwtToken(id, issuer, subject, tokenValidity, secretKey, claims),
                "Jwt must match");

            verify(jwtBuilder, times(1)).id(id);
            verify(jwtBuilder, times(1)).issuer(issuer);
            verify(jwtBuilder, times(1)).claims(claims);
            verify(jwtBuilder, times(1)).subject(subject);
            verify(jwtBuilder, times(1)).issuedAt(currentDate);
            verify(jwtBuilder, times(1)).expiration(new Date(currentDate.getTime() + tokenValidity));
            verify(jwtBuilder, times(1)).signWith(secretKey);
            verify(jwtBuilder, times(1)).compact();
        }

        @Test
        void negativeExceptionRaised() {
            String id = "MyId";
            String issuer = "MyIssuer";
            String subject = "MySubject";
            long tokenValidity = 500L;
            Key secretKey = mock(Key.class);
            Map<String, Object> claims = getClaims();

            MalformedJwtException exception = new MalformedJwtException("Example exception");
            jwtsMockedStatic.when(Jwts::builder).thenThrow(exception);

            UnauthorisedException unauthorisedException = assertThrows(UnauthorisedException.class,
                () -> jwtService.generateJwtToken(id, issuer, subject, tokenValidity, secretKey, claims));
            assertEquals("Failed to parse JWT", unauthorisedException.getMessage(), "Message must match");
            assertEquals(exception, unauthorisedException.getCause(), "Cause must match");
        }
    }
}
