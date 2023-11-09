package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class InternalServerErrorTest extends GenericErrorTest<InternalServerError> {
    @Override
    protected String getErrorCode() {
        return "INTERNAL_SERVER_ERROR";
    }

    @Override
    protected String getDefaultMessage() {
        return "An internal server error has occurred. Please contact system administrators if this problem persists.";
    }

    @Override
    protected InternalServerError createErrorObject() {
        return new InternalServerError();
    }
}
