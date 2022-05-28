package org.schabi.newpipe.extractor.streamdata.format;

public class VideoAudioMediaFormat extends AbstractMediaFormat {
    public VideoAudioMediaFormat(
            final int id,
            final String name,
            final String suffix,
            final String mimeType
    ) {
        super(id, name, suffix, mimeType);
    }
}
