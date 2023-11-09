package uk.gov.hmcts.juror.standard.api.model.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class GenericErrorWithEditableMessages extends GenericError {

    protected GenericErrorWithEditableMessages(String code) {
        super(code);
    }

    @Override
    public GenericError addMessage(final String message) {
        super.addMessage(message);
        return this;
    }
}
