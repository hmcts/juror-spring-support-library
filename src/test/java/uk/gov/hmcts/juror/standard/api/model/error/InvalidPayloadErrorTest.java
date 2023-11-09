package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class InvalidPayloadErrorTest extends GenericErrorWithEditableMessagesTest<InvalidPayloadError> {
    @Override
    protected String getErrorCode() {
        return "INVALID_PAYLOAD";
    }

    @Override
    protected InvalidPayloadError createErrorObject() {
        return new InvalidPayloadError();
    }

    @Override
    protected String getDefaultMessage() {
        return null;
    }
}
