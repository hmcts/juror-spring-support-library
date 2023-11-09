package uk.gov.hmcts.juror.standard.api.model.error;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class GenericErrorWithEditableMessagesTest<T extends GenericErrorWithEditableMessages>
    extends GenericErrorTest<T> {

    @Test
    void positiveAddMessageSingle() {
        T error = createErrorObject();
        assertNull(error.getMessages(), "Messages must be null");
        String message = "This is my error message";
        assertEquals(error, error.addMessage(message), "Add message should return self");
        assertNotNull(error.getMessages(), "Messages must not be null");
        assertEquals(1, error.getMessages().size(), "Message size must match");
        assertThat("Messages must match", error.getMessages(), hasItem(message));
        validateJson(error, message);
    }

    @Test
    void positiveAddMessageMultiple() {
        T error = createErrorObject();
        assertNull(error.getMessages(), "Messages must be null");

        List<String> messages = new ArrayList<>();
        messages.add("This is my first message");
        messages.add("This is my second message");
        messages.add("This is my third message");
        messages.forEach(error::addMessage);

        assertNotNull(error.getMessages(), "Message must not be null");
        assertEquals(messages.size(), error.getMessages().size(), "Message size must match");
        assertThat("Messages must match", error.getMessages(), hasItems(messages.toArray(new String[0])));
        validateJson(error, messages);
    }
}
