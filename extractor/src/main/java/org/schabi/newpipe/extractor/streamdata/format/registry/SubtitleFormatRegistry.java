package org.schabi.newpipe.extractor.streamdata.format.registry;

import org.schabi.newpipe.extractor.streamdata.format.SubtitleMediaFormat;

public class SubtitleFormatRegistry extends MediaFormatRegistry<SubtitleMediaFormat> {

    public static final SubtitleMediaFormat VTT =
            new SubtitleMediaFormat(0x1000, "WebVTT", "vtt", "text/vtt");
    public static final SubtitleMediaFormat TTML =
            new SubtitleMediaFormat(0x2000, "Timed Text Markup Language", "ttml",
                    "application/ttml+xml");
    public static final SubtitleMediaFormat TRANSCRIPT1 =
            new SubtitleMediaFormat(0x3000, "TranScript v1", "srv1", "text/xml");
    public static final SubtitleMediaFormat TRANSCRIPT2 =
            new SubtitleMediaFormat(0x4000, "TranScript v2", "srv2", "text/xml");
    public static final SubtitleMediaFormat TRANSCRIPT3 =
            new SubtitleMediaFormat(0x5000, "TranScript v3", "srv3", "text/xml");
    public static final SubtitleMediaFormat SRT =
            new SubtitleMediaFormat(0x6000, "SubRip file format", "srt", "text/srt");


    public SubtitleFormatRegistry() {
        super(new SubtitleMediaFormat[]{VTT, TTML, TRANSCRIPT1, TRANSCRIPT2, TRANSCRIPT3, SRT});
    }
}
