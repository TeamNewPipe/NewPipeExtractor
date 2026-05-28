package org.schabi.newpipe.extractor.stream.impl.base;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

public class BaseVideoStreamImpl extends BaseDemuxedStreamImpl implements BaseVideoStream {

    @Nonnull
    private final VideoMediaFormat mediaFormat;
    @Nonnull
    private final ProjectionType projectionType;
    @Nullable
    private final Integer averageBitrate;
    @Nullable
    private final Integer width;
    @Nullable
    private final Integer height;
    @Nullable
    private final Integer framerate;
    @Nullable
    private final String codec;
    @Nullable
    private final Boolean isHdr;

    public BaseVideoStreamImpl(@Nullable final String id,
                               @Nullable final String name,
                               @Nullable final Locale locale,
                               @Nullable final Boolean isAutomaticallyServiceGenerated,
                               @Nonnull final VideoMediaFormat mediaFormat,
                               @Nonnull final ProjectionType projectionType,
                               @Nullable final Integer averageBitrate,
                               @Nullable final Integer width,
                               @Nullable final Integer height,
                               @Nullable final Integer framerate,
                               @Nullable final String codec,
                               @Nullable final Boolean isHdr) {
        super(id, name, locale, isAutomaticallyServiceGenerated);
        this.mediaFormat = mediaFormat;
        this.projectionType = projectionType;
        this.averageBitrate = averageBitrate;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.codec = codec;
        this.isHdr = isHdr;
    }

    @Nonnull
    @Override
    public VideoMediaFormat getMediaFormat() {
        return mediaFormat;
    }

    @Nonnull
    @Override
    public ProjectionType getProjectionType() {
        return projectionType;
    }

    @Nullable
    @Override
    public Integer getAverageBitrate() {
        return averageBitrate;
    }

    @Nullable
    @Override
    public Integer getWidth() {
        return width;
    }

    @Nullable
    @Override
    public Integer getHeight() {
        return height;
    }

    @Nullable
    @Override
    public Integer getFramerate() {
        return framerate;
    }

    @Nullable
    @Override
    public String getCodec() {
        return codec;
    }

    @Nullable
    @Override
    public Boolean isHdr() {
        return isHdr;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseVideoStreamImpl that) || !super.equals(o)) {
            return false;
        }

        return mediaFormat.equals(that.mediaFormat) && projectionType == that.projectionType
                && Objects.equals(averageBitrate, that.averageBitrate)
                && Objects.equals(width, that.width)
                && Objects.equals(height, that.height)
                && Objects.equals(framerate, that.framerate)
                && Objects.equals(codec, that.codec)
                && Objects.equals(isHdr, that.isHdr);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mediaFormat.hashCode();
        result = 31 * result + projectionType.hashCode();
        result = 31 * result + Objects.hashCode(averageBitrate);
        result = 31 * result + Objects.hashCode(width);
        result = 31 * result + Objects.hashCode(height);
        result = 31 * result + Objects.hashCode(framerate);
        result = 31 * result + Objects.hashCode(codec);
        result = 31 * result + Objects.hashCode(isHdr);
        return result;
    }

    @Override
    public String toString() {
        return "BaseVideoStreamImpl{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + mediaFormat
                + ", projectionType=" + projectionType
                + ", averageBitrate=" + averageBitrate
                + ", width=" + width
                + ", height=" + height
                + ", framerate=" + framerate
                + ", codec=" + codec
                + ", isHdr=" + isHdr
                + "}";
    }
}
