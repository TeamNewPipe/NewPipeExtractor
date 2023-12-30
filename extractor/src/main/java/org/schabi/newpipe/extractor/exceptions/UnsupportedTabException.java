package org.schabi.newpipe.extractor.exceptions;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

public final class UnsupportedTabException extends UnsupportedOperationException {
    public UnsupportedTabException(final FilterItem unsupportedTab) {
        super("Unsupported tab " + unsupportedTab);
    }
}
