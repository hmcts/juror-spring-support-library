package uk.gov.hmcts.juror.standard.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.standard.api.ExceptionHandling;
import uk.gov.hmcts.juror.standard.testsupport.ControllerTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = RootController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(
    classes = {
        RootController.class,
        ExceptionHandling.class
    }
)
@DisplayName("Controller: /")
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert"
})
class RootControllerTest extends ControllerTestSupport {
    private static final String CONTROLLER_BASEURL = "/";

    @Test
    void positiveGetWelcomeMessage() throws Exception {
        this.mockMvc
            .perform(get(CONTROLLER_BASEURL).contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("Welcome to Spring"));
    }
}
