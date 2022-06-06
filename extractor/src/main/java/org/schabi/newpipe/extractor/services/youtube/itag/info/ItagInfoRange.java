package org.schabi.newpipe.extractor.services.youtube.itag.info;

public class ItagInfoRange {
    private final int start;
    private final int end;

    public ItagInfoRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
