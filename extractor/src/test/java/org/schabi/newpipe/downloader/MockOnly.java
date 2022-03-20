package org.schabi.newpipe.downloader;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this to annotate tests methods/classes that should only be run when the downloader is of type
 * {@link DownloaderType#MOCK} or {@link DownloaderType#RECORDING}. This should be used when e.g. an
 * extractor returns different results each time because the underlying service web page does so. In
 * that case it makes sense to only run the tests with the mock downloader, since the real web page
 * is not reliable, but we still want to make sure that the code correctly interprets the stored and
 * mocked web page data.
 * @see MockOnlyCondition
 */
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MockOnlyCondition.class)
public @interface MockOnly {

    /**
     * The reason why the test is mockonly.
     */
    String value();
}
