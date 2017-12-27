package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.util.Collections;
import java.util.List;

public class ExtractorHelper {
    private ExtractorHelper() {}

    public static List<InfoItem> getStreamsOrLogError(Info info, ListExtractor extractor) {
        StreamInfoItemCollector collector;
        try {
            collector = extractor.getStreams();
        } catch (Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
        // Get from collector
        return getInfoItems(info, collector);
    }


    public static List<InfoItem> getRelatedVideosOrLogError(StreamInfo info, StreamExtractor extractor) {
        StreamInfoItemCollector collector;
        try {
            collector = extractor.getRelatedVideos();
        } catch (Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
        // Get from collector
        return getInfoItems(info, collector);
    }

    private static List<InfoItem> getInfoItems(Info info, InfoItemCollector collector) {
        List<InfoItem> result;
        try {
            result = collector.getItemList();
            info.addAllErrors(collector.getErrors());
        } catch (Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
        return result;
    }
}
