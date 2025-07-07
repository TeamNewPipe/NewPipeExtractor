package org.schabi.newpipe.extractor.services.testcases;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultExtractorTest;

/**
 * Test case base class for {@link DefaultExtractorTest}
 */
public interface DefaultExtractorTestCase {
    StreamingService service();
    String name();
    String id();
    String url();
    default String originalUrl() { return url(); }
    default String urlContains() { return url();}
    default String originalUrlContains() { return originalUrl(); }
}