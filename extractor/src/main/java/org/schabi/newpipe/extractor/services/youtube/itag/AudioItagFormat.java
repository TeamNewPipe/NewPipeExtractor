package org.schabi.newpipe.extractor.services.youtube.itag;

import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

import javax.annotation.Nonnull;

public interface AudioItagFormat extends ItagFormat {
    @Nonnull
    AudioMediaFormat audioMediaFormat();

    int averageBitrate();
}
