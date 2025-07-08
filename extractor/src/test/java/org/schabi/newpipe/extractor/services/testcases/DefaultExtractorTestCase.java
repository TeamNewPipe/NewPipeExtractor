package org.schabi.newpipe.extractor.services.testcases;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultExtractorTest;

/**
 * Test case base class for {@link DefaultExtractorTest}
 */
public interface DefaultExtractorTestCase {
    public abstract StreamingService service();
    public abstract String name();
    public abstract String id();
    public abstract String url();
    public default String originalUrl() { return url(); }
    public default String urlContains() { return url();}
    public default String originalUrlContains() { return originalUrl(); }
}