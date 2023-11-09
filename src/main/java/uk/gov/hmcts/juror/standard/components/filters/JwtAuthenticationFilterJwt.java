package uk.gov.hmcts.juror.standard.components.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;


@Component
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "false")
public class JwtAuthenticationFilterJwt extends AbstractJwtAuthenticationFilter {

    @Autowired
    public JwtAuthenticationFilterJwt(JwtService jwtService) {
        super(jwtService);
    }

    @Override
    UserDetails getUserDetails(String jwt) {
        return jwtService.extractUserDetails(jwt);
    }
}
