package org.schabi.newpipe.extractor.services;

import org.schabi.newpipe.extractor.Extractor;

public abstract class DefaultSimpleExtractorTest<T extends Extractor> extends DefaultSimpleUntypedExtractorTest<T> {

    @Override
    protected void fetchExtractor(final T extractor) throws Exception {
        extractor.fetchPage();
    }
}
