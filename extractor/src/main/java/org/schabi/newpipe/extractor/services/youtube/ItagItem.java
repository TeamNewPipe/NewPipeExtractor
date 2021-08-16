package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.MediaFormat.*;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.*;

import java.io.Serializable;

public class ItagItem implements Serializable {
    /**
     * List can be found here:
     * https://github.com/ytdl-org/youtube-dl/blob/c2350cac/youtube_dl/extractor/youtube.py#L1109
     */
    private static final ItagItem[] ITAG_LIST = {
            /////////////////////////////////////////////////////
            // VIDEO     ID  Type   Format  Resolution  FPS  ////
            /////////////////////////////////////////////////////
            new ItagItem(17, VIDEO, v3GPP, "144p"),
            new ItagItem(36, VIDEO, v3GPP, "240p"),

            new ItagItem(18, VIDEO, MPEG_4, "360p"),
            new ItagItem(34, VIDEO, MPEG_4, "360p"),
            new ItagItem(35, VIDEO, MPEG_4, "480p"),
            new ItagItem(59, VIDEO, MPEG_4, "480p"),
            new ItagItem(78, VIDEO, MPEG_4, "480p"),
            new ItagItem(22, VIDEO, MPEG_4, "720p"),
            new ItagItem(37, VIDEO, MPEG_4, "1080p"),
            new ItagItem(38, VIDEO, MPEG_4, "1080p"),

            new ItagItem(43, VIDEO, WEBM, "360p"),
            new ItagItem(44, VIDEO, WEBM, "480p"),
            new ItagItem(45, VIDEO, WEBM, "720p"),
            new ItagItem(46, VIDEO, WEBM, "1080p"),

            //////////////////////////////////////////////////////////////////
            // AUDIO     ID      ItagType          Format        Bitrate    //
            //////////////////////////////////////////////////////////////////
            new ItagItem(171, AUDIO, WEBMA, 128),
            new ItagItem(172, AUDIO, WEBMA, 256),
            new ItagItem(139, AUDIO, M4A, 48),
            new ItagItem(140, AUDIO, M4A, 128),
            new ItagItem(141, AUDIO, M4A, 256),
            new ItagItem(249, AUDIO, WEBMA_OPUS, 50),
            new ItagItem(250, AUDIO, WEBMA_OPUS, 70),
            new ItagItem(251, AUDIO, WEBMA_OPUS, 160),

            /// VIDEO ONLY ////////////////////////////////////////////
            //           ID      Type     Format  Resolution  FPS  ////
            ///////////////////////////////////////////////////////////
            new ItagItem(160, VIDEO_ONLY, MPEG_4, "144p"),
            new ItagItem(133, VIDEO_ONLY, MPEG_4, "240p"),
            new ItagItem(134, VIDEO_ONLY, MPEG_4, "360p"),
            new ItagItem(135, VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(212, VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(136, VIDEO_ONLY, MPEG_4, "720p"),
            new ItagItem(298, VIDEO_ONLY, MPEG_4, "720p60", 60),
            new ItagItem(137, VIDEO_ONLY, MPEG_4, "1080p"),
            new ItagItem(299, VIDEO_ONLY, MPEG_4, "1080p60", 60),
            new ItagItem(266, VIDEO_ONLY, MPEG_4, "2160p"),

            new ItagItem(278, VIDEO_ONLY, WEBM, "144p"),
            new ItagItem(242, VIDEO_ONLY, WEBM, "240p"),
            new ItagItem(243, VIDEO_ONLY, WEBM, "360p"),
            new ItagItem(244, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(245, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(246, VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(247, VIDEO_ONLY, WEBM, "720p"),
            new ItagItem(248, VIDEO_ONLY, WEBM, "1080p"),
            new ItagItem(271, VIDEO_ONLY, WEBM, "1440p"),
            // #272 is either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            new ItagItem(272, VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(302, VIDEO_ONLY, WEBM, "720p60", 60),
            new ItagItem(303, VIDEO_ONLY, WEBM, "1080p60", 60),
            new ItagItem(308, VIDEO_ONLY, WEBM, "1440p60", 60),
            new ItagItem(313, VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(315, VIDEO_ONLY, WEBM, "2160p60", 60)
    };

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static boolean isSupported(final int itag) {
        for (ItagItem item : ITAG_LIST) {
            if (itag == item.id) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static ItagItem getItag(final int itagId) throws ParsingException {
        for (ItagItem item : ITAG_LIST) {
            if (itagId == item.id) {
                return item;
            }
        }
        throw new ParsingException("itag " + itagId + " is not supported");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Static constants
    //////////////////////////////////////////////////////////////////////////*/

    public static final int AVERAGE_BITRATE_UNKNOWN = -1;
    public static final int SAMPLE_RATE_UNKNOWN = -1;
    public static final int FPS_UNKNOWN = -1;
    public static final int TARGET_DURATION_SEC_UNKNOWN = -1;

    /*//////////////////////////////////////////////////////////////////////////
    // Constructors and misc
    //////////////////////////////////////////////////////////////////////////*/

    public enum ItagType {
        AUDIO,
        VIDEO,
        VIDEO_ONLY
    }

    /**
     * Call {@link #ItagItem(int, ItagType, MediaFormat, String, int)} with the fps set to 30.
     */
    public ItagItem(final int id,
                    final ItagType type,
                    final MediaFormat format,
                    final String resolution) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.resolutionString = resolution;
        this.fps = 30;
    }

    /**
     * Constructor for videos.
     */
    public ItagItem(final int id,
                    final ItagType type,
                    final MediaFormat format,
                    final String resolution,
                    final int fps) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.resolutionString = resolution;
        this.fps = fps;
    }

    public ItagItem(final int id,
                    final ItagType type,
                    final MediaFormat format,
                    final int avgBitrate) {
        this.id = id;
        this.itagType = type;
        this.mediaFormat = format;
        this.avgBitrate = avgBitrate;
    }

    private final MediaFormat mediaFormat;


    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    public final int id;
    public final ItagType itagType;

    // Audio fields
    public int avgBitrate = AVERAGE_BITRATE_UNKNOWN;
    private int sampleRate = SAMPLE_RATE_UNKNOWN;

    // Video fields
    public String resolutionString;
    public int fps = FPS_UNKNOWN;

    // Fields for Dash
    private int bitrate;
    private int width;
    private int height;
    private int initStart;
    private int initEnd;
    private int indexStart;
    private int indexEnd;
    private String quality;
    private String codec;
    private int targetDurationSec = TARGET_DURATION_SEC_UNKNOWN;

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(final int bitrate) {
        this.bitrate = bitrate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public int getInitStart() {
        return initStart;
    }

    public void setInitStart(final int initStart) {
        this.initStart = initStart;
    }

    public int getInitEnd() {
        return initEnd;
    }

    public void setInitEnd(final int initEnd) {
        this.initEnd = initEnd;
    }

    public int getIndexStart() {
        return indexStart;
    }

    public void setIndexStart(final int indexStart) {
        this.indexStart = indexStart;
    }

    public int getIndexEnd() {
        return indexEnd;
    }

    public void setIndexEnd(final int indexEnd) {
        this.indexEnd = indexEnd;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(final String quality) {
        this.quality = quality;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(final String codec) {
        this.codec = codec;
    }

    /**
     * Get the {@code targetDurationSec} value.
     * <p>
     * This value is an average time in seconds of sequences duration of livestreams and ended
     * livestreams. It is only returned for these stream types by YouTube and makes no sense for
     * videos, so {@link #TARGET_DURATION_SEC_UNKNOWN} is returned for video streams.
     * </p>
     *
     * @return the targetDurationSec value or {@link #TARGET_DURATION_SEC_UNKNOWN}
     */
    public int getTargetDurationSec() {
        return targetDurationSec;
    }

    /**
     * Set the {@code targetDurationSec} value.
     * <p>
     * This value is an average time in seconds of sequences duration of livestreams and ended
     * livestreams. It is only returned for these stream types by YouTube and makes no sense for
     * videos, so {@link #TARGET_DURATION_SEC_UNKNOWN} will be set for video streams or if this
     * value is less than or equal to 0.
     * </p>
     *
     */
    public void setTargetDurationSec(final int targetDurationSec) {
        if (targetDurationSec > 0) {
            this.targetDurationSec = targetDurationSec;
        }
    }

    /**
     * Get the sample rate.
     * <p>
     * It is only known for audio streams, so {@link #SAMPLE_RATE_UNKNOWN} is returned for video
     * streams or if the sample rate is unknown.
     * </p>
     *
     * @return the sample rate or {@link #SAMPLE_RATE_UNKNOWN}
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Set the sample rate.
     * <p>
     * It is only known for audio streams, so {@link #SAMPLE_RATE_UNKNOWN} is set for video
     * streams or if the sample rate value is less than or equal to 0.
     * </p>
     */
    public void setSampleRate(final int sampleRate) {
        if (sampleRate > 0) {
            this.sampleRate = sampleRate;
        }
    }
}
