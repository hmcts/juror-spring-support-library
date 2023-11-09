package uk.gov.hmcts.juror.standard.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class JwtSecurityConfig {

    private String id;

    @NotNull
    private long tokenValidity;

    @NotBlank
    private String secret;

    @NotBlank
    private String subject;

    private String issuer;

    private Map<String, Object> claims;

    private String authenticationPrefix;

    public void setClaims(Map<String, Object> claims) {
        if (claims.containsKey("permissions") && claims.get("permissions") instanceof Map claimsMap) {
            claims.put("permissions", claimsMap.values());
        }
        this.claims = claims;
    }
}
