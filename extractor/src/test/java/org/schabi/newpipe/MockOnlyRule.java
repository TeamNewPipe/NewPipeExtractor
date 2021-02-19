package org.schabi.newpipe;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.schabi.newpipe.downloader.DownloaderType;

import javax.annotation.Nonnull;

/**
 * <p>
 * Checks if the system variable "downloader" is set to "REAL" and skips the tests if it is.
 * Otherwise execute the test.
 * </p>
 *
 * <p>
 * Use it by creating a public variable of this inside the test class and annotate it with
 * {@link org.junit.Rule}. Then annotate the tests to be skipped with {@link MockOnly}
 * </p>
 *
 * <p>
 * Allows skipping unreliable or time sensitive tests in CI pipeline.
 * </p>
 */
public class MockOnlyRule implements TestRule {

    final String downloader = System.getProperty("downloader");

    @Override
    @Nonnull
    public Statement apply(@Nonnull Statement base, @Nonnull Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final MockOnly annotation = description.getAnnotation(MockOnly.class);
                if (annotation != null) {
                    final boolean isMockDownloader = downloader == null ||
                            !downloader.equalsIgnoreCase(DownloaderType.REAL.toString());

                    Assume.assumeTrue("The test is not reliable against real website. Reason: "
                            + annotation.reason(), isMockDownloader);
                }


                base.evaluate();
            }
        };
    }
}
