package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.InitNewPipeTest;


/**
 * Represents a common abstraction of a test class where an extractor is set up and various
 * tests for it are executed.
 * <p/>
 * Please see {@link DefaultSimpleExtractorTest} for an implementation that is exactly typed to
 * {@link org.schabi.newpipe.extractor.Extractor}
 */
public abstract class DefaultSimpleUntypedExtractorTest<T> implements InitNewPipeTest {

    private T extractor;

    /**
     * This should create an extractor, save it to variable
     * {@link DefaultSimpleUntypedExtractorTest#extractor},
     * and then set up the extractor by making it fetch network resources if applicable
     */
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
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException)ex;
                }
                throw new RuntimeException("Failed to init extractor", ex);
            }
        }
        return extractor;
    }
}
