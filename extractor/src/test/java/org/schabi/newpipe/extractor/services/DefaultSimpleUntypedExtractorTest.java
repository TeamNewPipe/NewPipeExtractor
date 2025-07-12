package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.InitNewPipeTest;

public abstract class DefaultSimpleUntypedExtractorTest<T> implements InitNewPipeTest {

    private T extractor;

    protected void initExtractor() throws Exception {
        extractor = createExtractor();
        fetchExtractor(extractor);
    }

    protected abstract T createExtractor() throws Exception;

    protected void fetchExtractor(final T extractor) throws Exception {
    }

    protected T extractor() {
        if (extractor == null) {
            try {
                initExtractor();
            } catch (final Exception ex) {
                if(ex instanceof RuntimeException) {
                    throw (RuntimeException)ex;
                }
                throw new RuntimeException("Failed to init extractor", ex);
            }
        }
        return extractor;
    }
}
