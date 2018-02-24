package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.util.Collections;
import java.util.List;

public class ExtractorHelper {
    private ExtractorHelper() {}

    public static List<InfoItem> getInfoItemsOrLogError(Info info, ListExtractor extractor) {
        InfoItemsCollector collector;
        try {
            collector = extractor.getInfoItems();
        } catch (Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
        // Get from collector
        return getInfoItems(info, collector);
    }


    public static List<InfoItem> getRelatedVideosOrLogError(StreamInfo info, StreamExtractor extractor) {
        StreamInfoItemsCollector collector;
        try {
            collector = extractor.getRelatedVideos();
        } catch (Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
        // Get from collector
        return getInfoItems(info, collector);
    }

    private static List<InfoItem> getInfoItems(Info info, InfoItemsCollector collector) {
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
