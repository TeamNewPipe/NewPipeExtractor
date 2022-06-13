package org.schabi.newpipe.extractor.streamdata.format;

import javax.annotation.Nonnull;

public class VideoAudioMediaFormat extends AbstractMediaFormat {
    public VideoAudioMediaFormat(
            final int id,
            @Nonnull final String name,
            @Nonnull final String suffix,
            @Nonnull final String mimeType
    ) {
        super(id, name, suffix, mimeType);
    }
}
