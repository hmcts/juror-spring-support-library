package uk.gov.hmcts.juror.standard.components.filters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        JwtAuthenticationFilterJwt.class
    }
)
@SpringBootTest(properties = {
    "uk.gov.hmcts.juror.security.use-database=false"
})
class JwtAuthenticationFilterJwtTest {

    @Autowired
    private JwtAuthenticationFilterJwt jwtAuthenticationFilterJwt;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void positiveUserDetailsFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(jwtService.extractUserDetails(TestConstants.JWT)).thenReturn(userDetails);
        assertEquals(userDetails, jwtAuthenticationFilterJwt.getUserDetails(TestConstants.JWT),
            "User details must match");
    }

    @Test
    void negativeUserDetailsNotFound() {
        when(jwtService.extractUserDetails(TestConstants.JWT)).thenReturn(null);
        assertNull(jwtAuthenticationFilterJwt.getUserDetails(TestConstants.JWT),
            "User details should be null");

    }
}
