package org.schabi.newpipe.extractor.streamdata.format;

import javax.annotation.Nonnull;

public interface MediaFormat {

    int id();

    @Nonnull
    String name();

    @Nonnull
    String suffix();

    @Nonnull
    String mimeType();
}
