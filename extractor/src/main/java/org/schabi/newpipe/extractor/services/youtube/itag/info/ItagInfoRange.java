package org.schabi.newpipe.extractor.services.youtube.itag.info;

public class ItagInfoRange {
    private final int start;
    private final int end;

    public ItagInfoRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    @Override
    public String toString() {
        return "ItagInfoRange{"
                + "start=" + start
                + ", end=" + end
                + '}';
    }
}
