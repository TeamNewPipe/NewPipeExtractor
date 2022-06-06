package org.schabi.newpipe.extractor.services.youtube.itag.format.registry;

import static org.schabi.newpipe.extractor.services.youtube.itag.delivery.simpleimpl.SimpleItagDeliveryDataBuilder.dash;
import static org.schabi.newpipe.extractor.services.youtube.itag.delivery.simpleimpl.SimpleItagDeliveryDataBuilder.hls;
import static org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry.MPEG_4;
import static org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry.V3GPP;
import static org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry.WEBM;
import static org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData.fromHeight;
import static org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData.fromHeightFps;
import static org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData.fromHeightWidth;

import org.schabi.newpipe.extractor.services.youtube.itag.format.AudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.ItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoAudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl.SimpleAudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl.SimpleVideoAudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl.SimpleVideoItagFormat;
import org.schabi.newpipe.extractor.streamdata.format.registry.AudioFormatRegistry;

import java.util.stream.Stream;

// https://github.com/ytdl-org/youtube-dl/blob/9aa8e5340f3d5ece372b983f8e399277ca1f1fe4/youtube_dl/extractor/youtube.py#L1195
public final class ItagFormatRegistry {

    public static final VideoAudioItagFormat[] VIDEO_AUDIO_FORMATS = new VideoAudioItagFormat[]{
            // v-- Video-codec: mp4v; Audio-codec: aac --v
            new SimpleVideoAudioItagFormat(17, V3GPP, fromHeightWidth(144, 176), 24),
            // v-- Video-codec: h264; Audio-codec: aac --v
            new SimpleVideoAudioItagFormat(18, MPEG_4, fromHeightWidth(360, 640), 96),
            new SimpleVideoAudioItagFormat(22, MPEG_4, fromHeightWidth(720, 1280), 192),

            // Note: According to yt-dl Itag 34 and 35 are flv-files
            new SimpleVideoAudioItagFormat(34, MPEG_4, fromHeightWidth(360, 640), 128),
            new SimpleVideoAudioItagFormat(35, MPEG_4, fromHeightWidth(480, 854), 128),

            // Itag 36 is no longer used because the height is unstable and it's not returned by YT
            // see also: https://github.com/ytdl-org/youtube-dl/blob/9aa8e5340f3d5ece372b983f8e399277ca1f1fe4/youtube_dl/extractor/youtube.py#L1204
            new SimpleVideoAudioItagFormat(37, MPEG_4, fromHeightWidth(1080, 1920), 192),
            new SimpleVideoAudioItagFormat(38, MPEG_4, fromHeightWidth(3072, 4092), 192),

            // v-- Video-codec: vp8; Audio-codec: vorbis --v
            new SimpleVideoAudioItagFormat(43, WEBM, fromHeightWidth(360, 640), 128),
            new SimpleVideoAudioItagFormat(44, WEBM, fromHeightWidth(480, 854), 128),
            new SimpleVideoAudioItagFormat(45, WEBM, fromHeightWidth(720, 1280), 192),
            new SimpleVideoAudioItagFormat(46, WEBM, fromHeightWidth(1080, 1920), 192),

            // HLS (used for live streaming)
            // v-- Video-codec: h264; Audio-codec: acc --v
            new SimpleVideoAudioItagFormat(91, MPEG_4, fromHeight(144), 48, hls()),
            new SimpleVideoAudioItagFormat(92, MPEG_4, fromHeight(240), 48, hls()),
            new SimpleVideoAudioItagFormat(93, MPEG_4, fromHeight(360), 128, hls()),
            new SimpleVideoAudioItagFormat(94, MPEG_4, fromHeight(480), 128, hls()),
            new SimpleVideoAudioItagFormat(95, MPEG_4, fromHeight(720), 256, hls()),
            new SimpleVideoAudioItagFormat(96, MPEG_4, fromHeight(1080), 256, hls()),
            new SimpleVideoAudioItagFormat(132, MPEG_4, fromHeight(240), 48, hls()),
            new SimpleVideoAudioItagFormat(151, MPEG_4, fromHeight(72), 24, hls())
    };

    public static final AudioItagFormat[] AUDIO_FORMATS = new AudioItagFormat[] {
            // DASH MP4 audio
            // v-- Audio-codec: aac --v
            new SimpleAudioItagFormat(139, AudioFormatRegistry.M4A, 48, dash()),
            new SimpleAudioItagFormat(140, AudioFormatRegistry.M4A, 128, dash()),
            new SimpleAudioItagFormat(141, AudioFormatRegistry.M4A, 256, dash()),

            // DASH WEBM audio
            // v-- Audio-codec: vorbis --v
            new SimpleAudioItagFormat(171, AudioFormatRegistry.WEBMA, 128, dash()),
            new SimpleAudioItagFormat(172, AudioFormatRegistry.WEBMA, 256, dash()),

            // DASH WEBM audio with opus inside
            // v-- Audio-codec: opus --v
            new SimpleAudioItagFormat(249, AudioFormatRegistry.WEBMA_OPUS, 50, dash()),
            new SimpleAudioItagFormat(250, AudioFormatRegistry.WEBMA_OPUS, 70, dash()),
            new SimpleAudioItagFormat(251, AudioFormatRegistry.WEBMA_OPUS, 160, dash())
    };

    public static final VideoItagFormat[] VIDEO_FORMATS = new VideoItagFormat[] {
            // DASH MP4 video
            // v-- Video-codec: h264 --v
            new SimpleVideoItagFormat(133, MPEG_4, fromHeight(240), dash()),
            new SimpleVideoItagFormat(134, MPEG_4, fromHeight(360), dash()),
            new SimpleVideoItagFormat(135, MPEG_4, fromHeight(480), dash()),
            new SimpleVideoItagFormat(136, MPEG_4, fromHeight(720), dash()),
            new SimpleVideoItagFormat(137, MPEG_4, fromHeight(1080), dash()),
            // Itag 138 has an unknown height and is ignored
            new SimpleVideoItagFormat(160, MPEG_4, fromHeight(144), dash()),
            new SimpleVideoItagFormat(212, MPEG_4, fromHeight(480), dash()),
            new SimpleVideoItagFormat(298, MPEG_4, fromHeightFps(720, 60), dash()),
            new SimpleVideoItagFormat(299, MPEG_4, fromHeightFps(1080, 60), dash()),
            new SimpleVideoItagFormat(266, MPEG_4, fromHeight(2160), dash()),

            // DASH WEBM video
            // v-- Video-codec: vp9 --v
            new SimpleVideoItagFormat(278, WEBM, fromHeight(144), dash()),
            new SimpleVideoItagFormat(242, WEBM, fromHeight(240), dash()),
            new SimpleVideoItagFormat(243, WEBM, fromHeight(360), dash()),
            // Itag 244, 245 and 246 are identical?
            new SimpleVideoItagFormat(244, WEBM, fromHeight(480), dash()),
            new SimpleVideoItagFormat(245, WEBM, fromHeight(480), dash()),
            new SimpleVideoItagFormat(246, WEBM, fromHeight(480), dash()),
            new SimpleVideoItagFormat(247, WEBM, fromHeight(720), dash()),
            new SimpleVideoItagFormat(248, WEBM, fromHeight(1080), dash()),
            new SimpleVideoItagFormat(271, WEBM, fromHeight(1440), dash()),
            // Itag 272 is either 3840x2160 (RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            new SimpleVideoItagFormat(272, WEBM, fromHeight(2160), dash()),

            new SimpleVideoItagFormat(302, WEBM, fromHeightFps(720, 60), dash()),
            new SimpleVideoItagFormat(303, WEBM, fromHeightFps(1080, 60), dash()),
            new SimpleVideoItagFormat(308, WEBM, fromHeightFps(1440, 60), dash()),
            new SimpleVideoItagFormat(312, WEBM, fromHeight(2160), dash()),
            new SimpleVideoItagFormat(315, WEBM, fromHeightFps(2160, 60), dash()),
    };

    private ItagFormatRegistry() {
        // No impl
    }

    public static boolean isSupported(final int id) {
        return Stream.of(VIDEO_AUDIO_FORMATS, AUDIO_FORMATS, VIDEO_FORMATS)
                .flatMap(Stream::of)
                .anyMatch(itagFormat -> itagFormat.id() == id);
    }

    public static ItagFormat getById(final int id) {
        return Stream.of(VIDEO_AUDIO_FORMATS, AUDIO_FORMATS, VIDEO_FORMATS)
                .flatMap(Stream::of)
                .filter(itagFormat -> itagFormat.id() == id)
                .findFirst()
                .orElse(null);
    }
}
