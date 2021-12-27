package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.ExtractorAsserts;
import org.schabi.newpipe.extractor.StreamingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;

public abstract class DefaultExtractorTest<T extends Extractor> implements BaseExtractorTest {
    public abstract T extractor() throws Exception;

    public abstract StreamingService expectedService() throws Exception;
    public abstract String expectedName() throws Exception;
    public abstract String expectedId() throws Exception;
    public abstract String expectedUrlContains() throws Exception;
    public abstract String expectedOriginalUrlContains() throws Exception;

    @Test
    @Override
    public void testServiceId() throws Exception {
        assertEquals(expectedService().getServiceId(), extractor().getServiceId());
    }

    @Test
    @Override
    public void testName() throws Exception {
        assertEquals(expectedName(), extractor().getName());
    }

    @Test
    @Override
    public void testId() throws Exception {
        assertEquals(expectedId(), extractor().getId());
    }

    @Test
    @Override
    public void testUrl() throws Exception {
        final String url = extractor().getUrl();
        assertIsSecureUrl(url);
        ExtractorAsserts.assertContains(expectedUrlContains(), url);
    }

    @Test
    @Override
    public void testOriginalUrl() throws Exception {
        final String originalUrl = extractor().getOriginalUrl();
        assertIsSecureUrl(originalUrl);
        ExtractorAsserts.assertContains(expectedOriginalUrlContains(), originalUrl);
    }
}
