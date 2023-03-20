package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Locale;

import static org.schabi.newpipe.extractor.MediaFormat.M4A;
import static org.schabi.newpipe.extractor.MediaFormat.MPEG_4;
import static org.schabi.newpipe.extractor.MediaFormat.WEBM;
import static org.schabi.newpipe.extractor.MediaFormat.WEBMA;
import static org.schabi.newpipe.extractor.MediaFormat.WEBMA_OPUS;
import static org.schabi.newpipe.extractor.MediaFormat.v3GPP;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.AUDIO;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.VIDEO;
import static org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType.VIDEO_ONLY;

public class ItagItem implements Serializable {

    /**
     * List can be found here:
     * https://github.com/ytdl-org/youtube-dl/blob/e988fa4/youtube_dl/extractor/youtube.py#L1195
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
        for (final ItagItem item : ITAG_LIST) {
            if (itag == item.id) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static ItagItem getItag(final int itagId) throws ParsingException {
        for (final ItagItem item : ITAG_LIST) {
            if (itagId == item.id) {
                return new ItagItem(item);
            }
        }
        throw new ParsingException("itag " + itagId + " is not supported");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Static constants
    //////////////////////////////////////////////////////////////////////////*/

    public static final int AVERAGE_BITRATE_UNKNOWN = -1;
    public static final int SAMPLE_RATE_UNKNOWN = -1;
    public static final int FPS_NOT_APPLICABLE_OR_UNKNOWN = -1;
    public static final int TARGET_DURATION_SEC_UNKNOWN = -1;
    public static final int AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN = -1;
    public static final long CONTENT_LENGTH_UNKNOWN = -1;
    public static final long APPROX_DURATION_MS_UNKNOWN = -1;

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

    /**
     * Copy constructor of the {@link ItagItem} class.
     *
     * @param itagItem the {@link ItagItem} to copy its properties into a new {@link ItagItem}
     */
    public ItagItem(@Nonnull final ItagItem itagItem) {
        this.mediaFormat = itagItem.mediaFormat;
        this.id = itagItem.id;
        this.itagType = itagItem.itagType;
        this.avgBitrate = itagItem.avgBitrate;
        this.sampleRate = itagItem.sampleRate;
        this.audioChannels = itagItem.audioChannels;
        this.resolutionString = itagItem.resolutionString;
        this.fps = itagItem.fps;
        this.bitrate = itagItem.bitrate;
        this.width = itagItem.width;
        this.height = itagItem.height;
        this.initStart = itagItem.initStart;
        this.initEnd = itagItem.initEnd;
        this.indexStart = itagItem.indexStart;
        this.indexEnd = itagItem.indexEnd;
        this.quality = itagItem.quality;
        this.codec = itagItem.codec;
        this.targetDurationSec = itagItem.targetDurationSec;
        this.approxDurationMs = itagItem.approxDurationMs;
        this.contentLength = itagItem.contentLength;
        this.audioTrackId = itagItem.audioTrackId;
        this.audioTrackName = itagItem.audioTrackName;
        this.isDescriptiveAudio = itagItem.isDescriptiveAudio;
        this.audioLocale = itagItem.audioLocale;
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    private final MediaFormat mediaFormat;

    public final int id;
    public final ItagType itagType;

    // Audio fields
    /**
     * @deprecated Use {@link #getAverageBitrate()} instead.
     */
    @Deprecated
    public int avgBitrate = AVERAGE_BITRATE_UNKNOWN;
    private int sampleRate = SAMPLE_RATE_UNKNOWN;
    private int audioChannels = AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN;

    // Video fields
    /**
     * @deprecated Use {@link #getResolutionString()} instead.
     */
    @Deprecated
    public String resolutionString;

    /**
     * @deprecated Use {@link #getFps()} and {@link #setFps(int)} instead.
     */
    @Deprecated
    public int fps = FPS_NOT_APPLICABLE_OR_UNKNOWN;

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
    private long approxDurationMs = APPROX_DURATION_MS_UNKNOWN;
    private long contentLength = CONTENT_LENGTH_UNKNOWN;
    private String audioTrackId;
    private String audioTrackName;
    private boolean isDescriptiveAudio;
    @Nullable
    private Locale audioLocale;

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

    /**
     * Get the frame rate.
     *
     * <p>
     * It is set to the {@code fps} value returned in the corresponding itag in the YouTube player
     * response.
     * </p>
     *
     * <p>
     * It defaults to the standard value associated with this itag.
     * </p>
     *
     * <p>
     * Note that this value is only known for video itags, so {@link
     * #FPS_NOT_APPLICABLE_OR_UNKNOWN} is returned for non video itags.
     * </p>
     *
     * @return the frame rate or {@link #FPS_NOT_APPLICABLE_OR_UNKNOWN}
     */
    public int getFps() {
        return fps;
    }

    /**
     * Set the frame rate.
     *
     * <p>
     * It is only known for video itags, so {@link #FPS_NOT_APPLICABLE_OR_UNKNOWN} is set/used for
     * non video itags or if the sample rate value is less than or equal to 0.
     * </p>
     *
     * @param fps the frame rate
     */
    public void setFps(final int fps) {
        this.fps = fps > 0 ? fps : FPS_NOT_APPLICABLE_OR_UNKNOWN;
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

    /**
     * Get the resolution string associated with this {@code ItagItem}.
     *
     * <p>
     * It is only known for video itags.
     * </p>
     *
     * @return the resolution string associated with this {@code ItagItem} or
     * {@code null}.
     */
    @Nullable
    public String getResolutionString() {
        return resolutionString;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(final String codec) {
        this.codec = codec;
    }

    /**
     * Get the average bitrate.
     *
     * <p>
     * It is only known for audio itags, so {@link #AVERAGE_BITRATE_UNKNOWN} is always returned for
     * other itag types.
     * </p>
     *
     * <p>
     * Bitrate of video itags and precise bitrate of audio itags can be known using
     * {@link #getBitrate()}.
     * </p>
     *
     * @return the average bitrate or {@link #AVERAGE_BITRATE_UNKNOWN}
     * @see #getBitrate()
     */
    public int getAverageBitrate() {
        return avgBitrate;
    }

    /**
     * Get the sample rate.
     *
     * <p>
     * It is only known for audio itags, so {@link #SAMPLE_RATE_UNKNOWN} is returned for non audio
     * itags, or if the sample rate is unknown.
     * </p>
     *
     * @return the sample rate or {@link #SAMPLE_RATE_UNKNOWN}
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Set the sample rate.
     *
     * <p>
     * It is only known for audio itags, so {@link #SAMPLE_RATE_UNKNOWN} is set/used for non audio
     * itags, or if the sample rate value is less than or equal to 0.
     * </p>
     *
     * @param sampleRate the sample rate of an audio itag
     */
    public void setSampleRate(final int sampleRate) {
        this.sampleRate = sampleRate > 0 ? sampleRate : SAMPLE_RATE_UNKNOWN;
    }

    /**
     * Get the number of audio channels.
     *
     * <p>
     * It is only known for audio itags, so {@link #AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN} is
     * returned for non audio itags, or if it is unknown.
     * </p>
     *
     * @return the number of audio channels or {@link #AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN}
     */
    public int getAudioChannels() {
        return audioChannels;
    }

    /**
     * Set the number of audio channels.
     *
     * <p>
     * It is only known for audio itags, so {@link #AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN} is
     * set/used for non audio itags, or if the {@code audioChannels} value is less than or equal to
     * 0.
     * </p>
     *
     * @param audioChannels the number of audio channels of an audio itag
     */
    public void setAudioChannels(final int audioChannels) {
        this.audioChannels = audioChannels > 0
                ? audioChannels
                : AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN;
    }

    /**
     * Get the {@code targetDurationSec} value.
     *
     * <p>
     * This value is the average time in seconds of the duration of sequences of livestreams and
     * ended livestreams. It is only returned by YouTube for these stream types, and makes no sense
     * for videos, so {@link #TARGET_DURATION_SEC_UNKNOWN} is returned for those.
     * </p>
     *
     * @return the {@code targetDurationSec} value or {@link #TARGET_DURATION_SEC_UNKNOWN}
     */
    public int getTargetDurationSec() {
        return targetDurationSec;
    }

    /**
     * Set the {@code targetDurationSec} value.
     *
     * <p>
     * This value is the average time in seconds of the duration of sequences of livestreams and
     * ended livestreams.
     * </p>
     *
     * <p>
     * It is only returned for these stream types by YouTube and makes no sense for videos, so
     * {@link #TARGET_DURATION_SEC_UNKNOWN} will be set/used for video streams or if this value is
     * less than or equal to 0.
     * </p>
     *
     * @param targetDurationSec the target duration of a segment of streams which are using the
     *                          live delivery method type
     */
    public void setTargetDurationSec(final int targetDurationSec) {
        this.targetDurationSec = targetDurationSec > 0
                ? targetDurationSec
                : TARGET_DURATION_SEC_UNKNOWN;
    }

    /**
     * Get the {@code approxDurationMs} value.
     *
     * <p>
     * It is only known for DASH progressive streams, so {@link #APPROX_DURATION_MS_UNKNOWN} is
     * returned for other stream types or if this value is less than or equal to 0.
     * </p>
     *
     * @return the {@code approxDurationMs} value or {@link #APPROX_DURATION_MS_UNKNOWN}
     */
    public long getApproxDurationMs() {
        return approxDurationMs;
    }

    /**
     * Set the {@code approxDurationMs} value.
     *
     * <p>
     * It is only known for DASH progressive streams, so {@link #APPROX_DURATION_MS_UNKNOWN} is
     * set/used for other stream types or if this value is less than or equal to 0.
     * </p>
     *
     * @param approxDurationMs the approximate duration of a DASH progressive stream, in
     *                         milliseconds
     */
    public void setApproxDurationMs(final long approxDurationMs) {
        this.approxDurationMs = approxDurationMs > 0
                ? approxDurationMs
                : APPROX_DURATION_MS_UNKNOWN;
    }

    /**
     * Get the {@code contentLength} value.
     *
     * <p>
     * It is only known for DASH progressive streams, so {@link #CONTENT_LENGTH_UNKNOWN} is
     * returned for other stream types or if this value is less than or equal to 0.
     * </p>
     *
     * @return the {@code contentLength} value or {@link #CONTENT_LENGTH_UNKNOWN}
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Set the content length of stream.
     *
     * <p>
     * It is only known for DASH progressive streams, so {@link #CONTENT_LENGTH_UNKNOWN} is
     * set/used for other stream types or if this value is less than or equal to 0.
     * </p>
     *
     * @param contentLength the content length of a DASH progressive stream
     */
    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength > 0 ? contentLength : CONTENT_LENGTH_UNKNOWN;
    }

    /**
     * Get the {@code audioTrackId} of the stream, if present.
     *
     * @return the {@code audioTrackId} of the stream or null
     */
    @Nullable
    public String getAudioTrackId() {
        return audioTrackId;
    }

    /**
     * Set the {@code audioTrackId} of the stream.
     *
     * @param audioTrackId the {@code audioTrackId} of the stream
     */
    public void setAudioTrackId(@Nullable final String audioTrackId) {
        this.audioTrackId = audioTrackId;
    }

    /**
     * Get the {@code audioTrackName} of the stream, if present.
     *
     * @return the {@code audioTrackName} of the stream or {@code null}
     */
    @Nullable
    public String getAudioTrackName() {
        return audioTrackName;
    }

    /**
     * Set the {@code audioTrackName} of the stream, if present.
     *
     * @param audioTrackName the {@code audioTrackName} of the stream or {@code null}
     */
    public void setAudioTrackName(@Nullable final String audioTrackName) {
        this.audioTrackName = audioTrackName;
    }

    /**
     * Return whether the stream is a descriptive audio.
     *
     * @return whether the stream is a descriptive audio
     */
    public boolean isDescriptiveAudio() {
        return isDescriptiveAudio;
    }

    /**
     * Set whether the stream is a descriptive audio.
     *
     * @param isDescriptiveAudio whether the stream is a descriptive audio
     */
    public void setIsDescriptiveAudio(final boolean isDescriptiveAudio) {
        this.isDescriptiveAudio = isDescriptiveAudio;
    }

    /**
     * Return the audio {@link Locale} of the stream, if known.
     *
     * @return the audio {@link Locale} of the stream, if known, or {@code null} if that's not the
     * case
     */
    @Nullable
    public Locale getAudioLocale() {
        return audioLocale;
    }

    /**
     * Set the audio {@link Locale} of the stream.
     *
     * <p>
     * If it is unknown, {@code null} could be passed, which is the default value.
     * </p>
     *
     * @param audioLocale the audio {@link Locale} of the stream, which could be {@code null}
     */
    public void setAudioLocale(@Nullable final Locale audioLocale) {
        this.audioLocale = audioLocale;
    }
}
