package uk.gov.hmcts.juror.standard.service.contracts.auth;

import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public interface JwtService {

    String extractEmail(String jwt);

    String extractUsername(String jwt);

    Date extractExpiration(String jwt);

    String generateJwtToken(UserDetails userDetails);

    String generateJwtToken(Map<String, Object> claims, UserDetails userDetails);

    String generateJwtToken(String id, String issuer, String subject, long tokenValidity, Key secretKey,
                            Map<String, Object> claims);

    boolean isJwtValid(String jwt, UserDetails userDetails);

    UserDetails extractUserDetails(String jwt);
}
