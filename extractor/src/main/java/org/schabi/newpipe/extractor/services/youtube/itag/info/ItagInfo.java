package org.schabi.newpipe.extractor.services.youtube.itag.info;

import org.schabi.newpipe.extractor.services.youtube.itag.format.BaseAudioItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.ItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoItagFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItagInfo<I extends ItagFormat<?>> {

    @Nonnull
    private final I itagFormat;

    // TODO: Maybe generate the streamUrl on-demand and not always instantly?
    @Nonnull
    private final String streamUrl;

    // region Audio

    /**
     * The average bitrate in KBit/s
     */
    @Nullable
    private Integer averageBitrate;

    @Nullable
    private Integer audioSampleRate;

    @Nullable
    private Integer audioChannels;

    // endregion

    // region Video

    @Nullable
    private Integer width;

    @Nullable
    private Integer height;

    @Nullable
    private Integer fps;

    // endregion

    @Nullable
    private Integer bitRate;

    @Nullable
    private String quality;

    @Nullable
    private String codec;

    @Nullable
    private ItagInfoRange initRange;

    @Nullable
    private ItagInfoRange indexRange;

    @Nullable
    private Long contentLength;

    @Nullable
    private Long approxDurationMs;

    @Nullable
    private String type;

    // region live or post-live

    @Nullable
    private Integer targetDurationSec;

    // endregion


    public ItagInfo(
            @Nonnull final I itagFormat,
            @Nonnull final String streamUrl) {
        this.itagFormat = itagFormat;
        this.streamUrl = streamUrl;
    }

    // region Getters + Setters

    @Nonnull
    public I getItagFormat() {
        return itagFormat;
    }

    @Nonnull
    public String getStreamUrl() {
        return streamUrl;
    }

    @Nullable
    public Integer getAverageBitrate() {
        return averageBitrate;
    }

    public void setAverageBitrate(@Nullable final Integer averageBitrate) {
        this.averageBitrate = averageBitrate;
    }

    @Nullable
    public Integer getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(@Nullable final Integer audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    @Nullable
    public Integer getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(@Nullable final Integer audioChannels) {
        this.audioChannels = audioChannels;
    }

    @Nullable
    public Integer getWidth() {
        return width;
    }

    public void setWidth(@Nullable final Integer width) {
        this.width = width;
    }

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(@Nullable final Integer height) {
        this.height = height;
    }

    @Nullable
    public Integer getFps() {
        return fps;
    }

    public void setFps(@Nullable final Integer fps) {
        this.fps = fps;
    }

    @Nullable
    public Integer getBitRate() {
        return bitRate;
    }

    public void setBitRate(@Nullable final Integer bitRate) {
        this.bitRate = bitRate;
    }

    @Nullable
    public String getQuality() {
        return quality;
    }

    public void setQuality(@Nullable final String quality) {
        this.quality = quality;
    }

    @Nullable
    public String getCodec() {
        return codec;
    }

    public void setCodec(@Nullable final String codec) {
        this.codec = codec;
    }

    @Nullable
    public ItagInfoRange getInitRange() {
        return initRange;
    }

    public void setInitRange(@Nullable final ItagInfoRange initRange) {
        this.initRange = initRange;
    }

    @Nullable
    public ItagInfoRange getIndexRange() {
        return indexRange;
    }

    public void setIndexRange(@Nullable final ItagInfoRange indexRange) {
        this.indexRange = indexRange;
    }

    @Nullable
    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(@Nullable final Long contentLength) {
        this.contentLength = contentLength;
    }

    @Nullable
    public Long getApproxDurationMs() {
        return approxDurationMs;
    }

    public void setApproxDurationMs(@Nullable final Long approxDurationMs) {
        this.approxDurationMs = approxDurationMs;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable final String type) {
        this.type = type;
    }

    @Nullable
    public Integer getTargetDurationSec() {
        return targetDurationSec;
    }

    public void setTargetDurationSec(@Nullable final Integer targetDurationSec) {
        this.targetDurationSec = targetDurationSec;
    }

    // endregion

    /**
     * Returns the combined averageBitrate from the current information and {@link #itagFormat}.
     * @return averageBitRate in KBit/s or <code>-1</code>
     */
    public int getCombinedAverageBitrate() {
        if (averageBitrate != null) {
            return averageBitrate;
        }

        if (itagFormat instanceof BaseAudioItagFormat) {
            return ((BaseAudioItagFormat) itagFormat).averageBitrate();
        }

        return -1;
    }

    /**
     * Returns the combined video-quality data from the current information and {@link #itagFormat}.
     * @return video-quality data
     */
    @Nonnull
    public VideoQualityData getCombinedVideoQualityData() {
        final Optional<VideoQualityData> optVideoItagFormatQualityData =
                itagFormat instanceof VideoItagFormat
                    ? Optional.of(((VideoItagFormat) itagFormat).videoQualityData())
                    : Optional.empty();

        return new VideoQualityData(
                Optional.ofNullable(height)
                        .orElse(optVideoItagFormatQualityData
                                .map(VideoQualityData::height)
                                .orElse(VideoQualityData.UNKNOWN)),
                Optional.ofNullable(width)
                        .orElse(optVideoItagFormatQualityData
                                .map(VideoQualityData::width)
                                .orElse(VideoQualityData.UNKNOWN)),
                Optional.ofNullable(fps)
                        .orElse(optVideoItagFormatQualityData
                                .map(VideoQualityData::fps)
                                .orElse(VideoQualityData.UNKNOWN))
        );
    }
}
