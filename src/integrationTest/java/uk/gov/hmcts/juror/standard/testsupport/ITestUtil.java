package uk.gov.hmcts.juror.standard.testsupport;

import java.util.concurrent.atomic.AtomicInteger;

public final class ITestUtil {
    private static final String USER_EMAIL_PREFIX = "user";
    private static final String USER_EMAIL_POSTFIX = "@cgi.com";
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    private ITestUtil() {
    }

    public static int getNextUniqueIndex() {
        return ATOMIC_INTEGER.incrementAndGet();
    }

    public static String dynamicEmailGenerator(int uniqueId) {
        return USER_EMAIL_PREFIX + uniqueId + USER_EMAIL_POSTFIX;
    }
}
