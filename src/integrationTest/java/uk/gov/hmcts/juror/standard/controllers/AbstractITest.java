package uk.gov.hmcts.juror.standard.controllers;

import com.jayway.jsonpath.JsonPath;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration")
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod",
    "PMD.SignatureDeclareThrowsException"
})
public abstract class AbstractITest {
    public static final String ADMIN_EMAIL = "admin@scheduler.cgi.com";
    public static final String ADMIN_PASSWORD_ENCRYPTED =
        "kj3TXdvYqmFTXXTq!9nA7ZUmDgiQ&W7Z&v7mnFyp2bvM&BZ#nPosFfL8zNvw";

    public static final String USER_PASSWORD = "password123";

    private final MockMvc mockMvc;

    protected AbstractITest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions mockMvcPerform(@NotNull String url,
                                        @NotNull String jwt,
                                        @NotNull HttpMethod httpMethod,
                                        @NotNull String jsonContent) throws Exception {

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.request(
            httpMethod, new URI(url));

        if (!jwt.isEmpty()) {
            mockHttpServletRequestBuilder
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        }

        if (!jsonContent.isEmpty()) {
            mockHttpServletRequestBuilder.content(jsonContent);
        }

        return mockMvc.perform(mockHttpServletRequestBuilder
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));
    }

    public String generateJwt(String loginRequest) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                                  .post("/auth/login")
                                  .content(loginRequest)
                                  .contentType(MediaType.APPLICATION_JSON)
                                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jwt").isNotEmpty())
            .andReturn();

        return JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.jwt");
    }
}
