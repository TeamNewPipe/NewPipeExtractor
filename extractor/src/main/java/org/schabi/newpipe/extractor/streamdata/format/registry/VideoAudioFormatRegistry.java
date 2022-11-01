package org.schabi.newpipe.extractor.streamdata.format.registry;

import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;

public class VideoAudioFormatRegistry extends MediaFormatRegistry<VideoAudioMediaFormat> {

    public static final VideoAudioMediaFormat MPEG_4 =
            new VideoAudioMediaFormat(0x0, "MPEG-4", "mp4", "video/mp4");
    public static final VideoAudioMediaFormat V3GPP =
            new VideoAudioMediaFormat(0x10, "3GPP", "3gp", "video/3gpp");
    public static final VideoAudioMediaFormat WEBM =
            new VideoAudioMediaFormat(0x20, "WebM", "webm", "video/webm");

    public VideoAudioFormatRegistry() {
        super(new VideoAudioMediaFormat[]{MPEG_4, V3GPP, WEBM});
    }
}
