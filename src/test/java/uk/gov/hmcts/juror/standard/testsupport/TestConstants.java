package uk.gov.hmcts.juror.standard.testsupport;

@SuppressWarnings({
    "PMD.TestClassWithoutTestCases"
})
public final class TestConstants {

    public static final String JWT = "this-is-a-test-key-for-us-when-creating-secrets";
    public static final String JWT_SECRET = "dGhpcy1pcy1hLXRlc3Qta2V5LWZvci11cy13aGVuLWNyZWF0aW5nLXNlY3JldHM=";
    public static final String AUTH_HEADER = "Bearer " + JWT;
    public static final String EMAIL = "admin@scheduler.cgi.com";
    public static final long TOKEN_VALIDITY = 160_000;

    private TestConstants() {

    }
}
