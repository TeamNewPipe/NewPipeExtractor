package org.schabi.newpipe.extractor.streamdata.format.registry;

import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

public class AudioFormatRegistry extends MediaFormatRegistry<AudioMediaFormat> {

    public static final AudioMediaFormat M4A =
            new AudioMediaFormat(0x100, "m4a", "m4a", "audio/mp4");
    public static final AudioMediaFormat WEBMA =
            new AudioMediaFormat(0x200, "WebM", "webm", "audio/webm");
    public static final AudioMediaFormat MP3 =
            new AudioMediaFormat(0x300, "MP3", "mp3", "audio/mpeg");
    public static final AudioMediaFormat OPUS =
            new AudioMediaFormat(0x400, "opus", "opus", "audio/opus");
    public static final AudioMediaFormat OGG =
            new AudioMediaFormat(0x500, "ogg", "ogg", "audio/ogg");
    public static final AudioMediaFormat WEBMA_OPUS =
            new AudioMediaFormat(0x200, "WebM Opus", "webm", "audio/webm");

    public AudioFormatRegistry() {
        super(new AudioMediaFormat[]{M4A, WEBMA, MP3, OPUS, OGG, WEBMA_OPUS});
    }
}
