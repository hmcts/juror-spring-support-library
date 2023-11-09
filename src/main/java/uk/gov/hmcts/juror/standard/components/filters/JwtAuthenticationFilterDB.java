package uk.gov.hmcts.juror.standard.components.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;

@Component
@ConditionalOnProperty(prefix = "uk.gov.hmcts.juror.security", name = "use-database", havingValue = "true")
public class JwtAuthenticationFilterDB extends AbstractJwtAuthenticationFilter {

    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilterDB(UserDetailsService userDetailsService, JwtService jwtService) {
        super(jwtService);
        this.userDetailsService = userDetailsService;
    }

    @Override
    UserDetails getUserDetails(String jwt) {
        String email = jwtService.extractEmail(jwt);
        if (email == null) {
            return null;
        }
        return userDetailsService.loadUserByUsername(email);
    }
}
