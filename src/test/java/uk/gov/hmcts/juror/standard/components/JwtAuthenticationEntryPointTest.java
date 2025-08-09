package uk.gov.hmcts.juror.standard.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        JwtAuthenticationEntryPoint.class
    }
)
class JwtAuthenticationEntryPointTest {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockitoBean
    private HttpServletRequest request;
    @MockitoBean
    private HttpServletResponse response;
    @MockitoBean
    private ObjectMapper objectMapper;

    @Test
    void positiveTypical() throws IOException {
        try (ServletOutputStream outputStreamMock = mock(ServletOutputStream.class)) {
            when(response.getOutputStream()).thenReturn(outputStreamMock);

            jwtAuthenticationEntryPoint.commence(request, response, null);

            verify(response, times(1)).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response, times(1)).getOutputStream();

            verify(objectMapper, times(1)).writeValue(eq(outputStreamMock), any(UnauthorisedError.class));
            verify(outputStreamMock, times(1)).flush();
        }
    }
}
