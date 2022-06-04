package org.schabi.newpipe.extractor.streamdata.stream.util;

import org.schabi.newpipe.extractor.streamdata.stream.Stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NewPipeStreamUtil {
    private NewPipeStreamUtil() {
        // No impl
    }

    /**
     * Checks if the list already contains a stream with the same statistics.
     *
     * @param stream the stream to be compared against the streams in the stream list
     * @param streams the list of {@link Stream}s which will be compared
     * @return whether the list already contains one stream with equals stats
     */
    public static boolean containSimilarStream(final Stream stream,
                                               final Collection<? extends Stream> streams) {
        if (streams == null || streams.isEmpty()) {
            return false;
        }
        return streams.stream().anyMatch(stream::equalsStream);
    }

    public <T extends Stream> List<T> removeEqualStreams(final Collection<T> streams) {
        final List<T> returnList = new ArrayList<>();
        for(final T stream : streams) {
            if (!containSimilarStream(stream, returnList)) {
                returnList.add(stream);
            }
        }
        return returnList;
    }
}
