package org.schabi.newpipe.extractor.streamdata.delivery.dashmanifestcreator;

public final class DashManifestCreatorConstants {
    private DashManifestCreatorConstants() {
        // No impl!
    }

    // XML elements of DASH MPD manifests
    // see https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html
    public static final String MPD = "MPD";
    public static final String PERIOD = "Period";
    public static final String ADAPTATION_SET = "AdaptationSet";
    public static final String ROLE = "Role";
    public static final String REPRESENTATION = "Representation";
    public static final String AUDIO_CHANNEL_CONFIGURATION = "AudioChannelConfiguration";
    public static final String SEGMENT_TEMPLATE = "SegmentTemplate";
    public static final String SEGMENT_TIMELINE = "SegmentTimeline";
    public static final String BASE_URL = "BaseURL";
    public static final String SEGMENT_BASE = "SegmentBase";
    public static final String INITIALIZATION = "Initialization";
}
