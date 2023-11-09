package uk.gov.hmcts.juror.standard.api.model.error;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases" //False positive done via inheritance
})
class RemoteGatewayErrorTest extends GenericErrorTest<RemoteGatewayError> {
    @Override
    protected String getErrorCode() {
        return "REMOTE_GATEWAY";
    }

    @Override
    protected String getDefaultMessage() {
        return "An unexpected message was received from a remote gateway.";
    }

    @Override
    protected RemoteGatewayError createErrorObject() {
        return new RemoteGatewayError();
    }
}
