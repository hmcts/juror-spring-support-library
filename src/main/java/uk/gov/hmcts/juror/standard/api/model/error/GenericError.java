package uk.gov.hmcts.juror.standard.api.model.error;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod"
})
public abstract class GenericError {

    @NotNull
    protected final String code;

    @NotNull
    private List<String> messages;

    protected GenericError(String code) {
        this.code = code;
    }

    protected GenericError addMessage(final String message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        return this;
    }
}
