package uk.gov.hmcts.juror.standard.service.impl.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Permission;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.Role;
import uk.gov.hmcts.juror.standard.datastore.entity.auth.User;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.service.exceptions.UnauthorisedException;

import java.security.Key;
import java.time.Clock;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Service
@Slf4j
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.LawOfDemeter"
})
public class JwtServiceImpl implements JwtService {


    @Value("${uk.gov.hmcts.juror.security.token-validity}")
    private long tokenValidity;

    @Value("${uk.gov.hmcts.juror.security.secret}")
    private String jwtSecret;

    private final Clock clock;

    private static final String PERMISSIONS_KEY = "permissions";
    private static final String ROLES_KEY = "roles";

    @Autowired
    public JwtServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String extractEmail(String jwt) {
        return extractUsername(jwt);
    }

    @Override
    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }


    private <T> T extractClaim(String jwt, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaims(jwt));
    }

    private Claims extractAllClaims(String jwt) {
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(jwt).getPayload();
        } catch (Exception exception) {
            throw new UnauthorisedException("Failed to parse JWT", exception);
        }
    }

    @Override
    public boolean isJwtValid(String jwt, UserDetails userDetails) {
        final boolean doesEmailMatch = extractEmail(jwt).equals(userDetails.getUsername());
        final boolean isExpired = isJwtExpired(jwt);
        if (log.isTraceEnabled()) {
            log.trace("isJwtValid: email match: " + doesEmailMatch + " isExpired: " + isExpired);
        }
        return doesEmailMatch && !isExpired;
    }

    @Override
    public UserDetails extractUserDetails(String jwt) {
        return User.builder().email(extractEmail(jwt)).permissions(extractPermissions(jwt)).build();
    }


    private Set<Permission> extractPermissions(String jwt) {
        Set<String> permissions = extractClaim(jwt, claims -> {
            Object obj = claims.getOrDefault(PERMISSIONS_KEY, Set.of());
            if (obj instanceof Collection<?> set) {
                return set.stream().map(Object::toString).collect(Collectors.toUnmodifiableSet());
            }
            return Set.of();
        });
        if (log.isTraceEnabled()) {
            log.trace("Extracting permissions from JWT found: " + String.join(",", permissions));
        }
        return permissions.stream().map(Permission::new).collect(Collectors.toUnmodifiableSet());
    }

    private boolean isJwtExpired(String jwt) {
        return extractExpiration(jwt).before(new Date(clock.millis()));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    public String generateJwtToken(@NotNull UserDetails userDetails) {
        Map<String, Object> claims = new ConcurrentHashMap<>();
        claims.put(PERMISSIONS_KEY,
            userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        if (userDetails instanceof User user) {
            claims.put(ROLES_KEY, user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        }
        return generateJwtToken(claims, userDetails);
    }

    @Override
    public String generateJwtToken(Map<String, Object> claims, UserDetails userDetails) {
        Key signingKey = getSigningKey();
        return generateJwtToken(null, null, userDetails.getUsername(), tokenValidity, signingKey, claims);
    }

    @Override
    public String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey,
                                   Map<String, Object> claims) {
        try {
            Date issuedAtDate = new Date(clock.millis());
            return Jwts.builder().id(id).issuer(issuer).claims(claims).subject(subject).issuedAt(issuedAtDate)
                .expiration(new Date(issuedAtDate.getTime() + tokenValidity)).signWith(secretKey).compact();
        } catch (Exception exception) {
            throw new UnauthorisedException("Failed to parse JWT", exception);
        }
    }
}
