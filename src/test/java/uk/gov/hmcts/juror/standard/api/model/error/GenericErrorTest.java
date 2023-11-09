package uk.gov.hmcts.juror.standard.api.model.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ObjectMapper.class})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.AvoidThrowingRawExceptionTypes"
})
public abstract class GenericErrorTest<T extends GenericError> {

    @Autowired
    private ObjectMapper objectMapper;

    protected abstract String getErrorCode();

    protected abstract String getDefaultMessage();

    protected abstract T createErrorObject();

    @Test
    void positiveConstructorTest() {
        T errorObject = createErrorObject();
        assertNotNull(errorObject, "Messages must not be null");
        assertEquals(getErrorCode(), errorObject.getCode(), "Error code must match");

        if (getDefaultMessage() != null) {
            assertNotNull(errorObject.getMessages(), "Messages must not be null");
            assertEquals(1, errorObject.getMessages().size(), "Message size must match");
            assertThat("Error message must match",
                errorObject.getMessages(), hasItem(getDefaultMessage()));
        } else {
            assertNull(errorObject.getMessages(), "Messages must be null");
        }
    }

    @Test
    void positiveJsonSerializeTest() {
        T errorObject = createErrorObject();
        validateJson(errorObject, getDefaultMessage());
    }

    protected void validateJson(T errorObject, String defaultMessage) {
        validateJson(errorObject, defaultMessage == null ? null : Collections.singletonList(defaultMessage));
    }


    protected void validateJson(final T error, List<String> messages) {
        try {
            String generatedJson = objectMapper.writeValueAsString(error);
            List<String> expectedMessages;
            final String messageList;
            if (messages == null || messages.isEmpty()) {
                messageList = "null";
            } else {
                expectedMessages = messages.stream().map(message -> "\"" + message + "\"").collect(Collectors.toList());
                messageList = "[" + StringUtils.join(expectedMessages, ",") + "]";
            }

            String expectedJsonBuilder = "{\"code\":\"" + getErrorCode() + "\"" + ",\"messages\":" + messageList + "}";

            JSONAssert.assertEquals(expectedJsonBuilder, generatedJson, true);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
