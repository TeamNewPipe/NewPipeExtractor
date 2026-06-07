package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;
import java.util.Objects;

public class StreamHeatmapEntry implements Serializable {
    private final long startTimeMillis;
    private final long durationMillis;
    private final double heatIntensity;

    public StreamHeatmapEntry(final long startTimeMillis,
                              final long durationMillis,
                              final double heatIntensity) {
        this.startTimeMillis = startTimeMillis;
        this.durationMillis = durationMillis;
        this.heatIntensity = heatIntensity;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public double getHeatIntensity() {
        return heatIntensity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StreamHeatmapEntry that = (StreamHeatmapEntry) o;
        return startTimeMillis == that.startTimeMillis
                && durationMillis == that.durationMillis
                && Double.compare(that.heatIntensity, heatIntensity) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimeMillis, durationMillis, heatIntensity);
    }
}
