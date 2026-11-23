package org.schabi.newpipe.extractor.stream.impl.base;

import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BaseMuxedStreamImpl implements BaseMuxedStream {

    @Nullable
    private final String id;
    @Nullable
    private final String name;
    @Nullable
    private final List<BaseAudioStream> demuxedAudioStreams;
    @Nullable
    private final List<BaseVideoStream> demuxedVideoStreams;
    @Nullable
    private final List<BaseSubtitlesStream> demuxedSubtitlesStreams;
    @Nullable
    private final List<BaseMuxedStream> muxedStreams;
    @Nullable
    private final Integer averageBitrate;

    public BaseMuxedStreamImpl(@Nullable final String id,
                               @Nullable final String name,
                               @Nullable final List<BaseAudioStream> demuxedAudioStreams,
                               @Nullable final List<BaseVideoStream> demuxedVideoStreams,
                               @Nullable final List<BaseSubtitlesStream> demuxedSubtitlesStreams,
                               @Nullable final List<BaseMuxedStream> muxedStreams,
                               @Nullable final Integer averageBitrate) {
        this.id = id;
        this.name = name;
        this.demuxedAudioStreams = demuxedAudioStreams;
        this.demuxedVideoStreams = demuxedVideoStreams;
        this.demuxedSubtitlesStreams = demuxedSubtitlesStreams;
        this.muxedStreams = muxedStreams;
        this.averageBitrate = averageBitrate;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public List<BaseAudioStream> getDemuxedAudioStreams() {
        return demuxedAudioStreams;
    }

    @Nullable
    @Override
    public List<BaseVideoStream> getDemuxedVideoStreams() {
        return demuxedVideoStreams;
    }

    @Nullable
    @Override
    public List<BaseSubtitlesStream> getDemuxedSubtitlesStreams() {
        return demuxedSubtitlesStreams;
    }

    @Nullable
    @Override
    public List<BaseMuxedStream> getMuxedStreams() {
        return muxedStreams;
    }

    @Nullable
    @Override
    public Integer getAverageBitrate() {
        return averageBitrate;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof BaseMuxedStreamImpl that)) {
            return false;
        }

        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(demuxedAudioStreams, that.demuxedAudioStreams)
                && Objects.equals(demuxedVideoStreams, that.demuxedVideoStreams)
                && Objects.equals(demuxedSubtitlesStreams, that.demuxedSubtitlesStreams)
                && Objects.equals(muxedStreams, that.muxedStreams)
                && Objects.equals(averageBitrate, that.averageBitrate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(demuxedAudioStreams);
        result = 31 * result + Objects.hashCode(demuxedVideoStreams);
        result = 31 * result + Objects.hashCode(demuxedSubtitlesStreams);
        result = 31 * result + Objects.hashCode(muxedStreams);
        result = 31 * result + Objects.hashCode(averageBitrate);
        return result;
    }

    @Override
    public String toString() {
        return "BaseMuxedStreamImpl{"
                + "id=" + id
                + ", name=" + name
                + ", demuxedAudioStreams=" + demuxedAudioStreams
                + ", demuxedVideoStreams=" + demuxedVideoStreams
                + ", demuxedSubtitlesStreams=" + demuxedSubtitlesStreams
                + ", muxedStreams=" + muxedStreams
                + ", averageBitrate=" + averageBitrate
                + "}";
    }
}
