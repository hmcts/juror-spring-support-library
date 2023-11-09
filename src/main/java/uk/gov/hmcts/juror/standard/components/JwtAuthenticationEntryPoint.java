package uk.gov.hmcts.juror.standard.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.standard.api.model.error.UnauthorisedError;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
        throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try (OutputStream responseStream = response.getOutputStream()) {
            objectMapper.writeValue(responseStream, new UnauthorisedError());
            responseStream.flush();
        }
    }
}
