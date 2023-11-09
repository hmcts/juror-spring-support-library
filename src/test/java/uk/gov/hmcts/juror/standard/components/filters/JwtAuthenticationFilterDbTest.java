package uk.gov.hmcts.juror.standard.components.filters;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.service.contracts.auth.JwtService;
import uk.gov.hmcts.juror.standard.testsupport.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtAuthenticationFilterDB.class})
@SpringBootTest(properties = {"uk.gov.hmcts.juror.security.use-database=true"})
class JwtAuthenticationFilterDbTest {

    @Autowired
    private JwtAuthenticationFilterDB jwtAuthenticationFilterDB;

    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtService jwtService;

    @Test
    void positiveUserFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(jwtService.extractEmail(TestConstants.JWT)).thenReturn(TestConstants.EMAIL);
        when(userDetailsService.loadUserByUsername(TestConstants.EMAIL)).thenReturn(userDetails);

        assertEquals(userDetails, jwtAuthenticationFilterDB.getUserDetails(TestConstants.JWT),
            "JWT user details must match");

        verify(jwtService, times(1)).extractEmail(TestConstants.JWT);
        verify(userDetailsService, times(1)).loadUserByUsername(TestConstants.EMAIL);
        verifyNoMoreInteractions(jwtService);
        verifyNoMoreInteractions(userDetailsService);
    }


    @Test
    void negativeEmailNotFound() {
        when(jwtService.extractEmail(TestConstants.JWT)).thenReturn(null);

        assertNull(jwtAuthenticationFilterDB.getUserDetails(TestConstants.JWT), "Jwt user details must be null");

        verify(jwtService, times(1)).extractEmail(TestConstants.JWT);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verifyNoMoreInteractions(jwtService);
        verifyNoMoreInteractions(userDetailsService);
    }
}
