package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public final class ExtractorHelper {
    private ExtractorHelper() {
    }

    @Nonnull
    public static DateWrapper parseDateWrapper(@Nonnull final String date) throws ParsingException {
        try {
            return new DateWrapper(OffsetDateTime.parse(date));
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + date + "\"", e);
        }
    }

    public static <T extends InfoItem> InfoItemsPage<T> getItemsPageOrLogError(
            final Info info, final ListExtractor<T> extractor) {
        try {
            final InfoItemsPage<T> page = extractor.getInitialPage();
            info.addAllErrors(page.getErrors());

            return page;
        } catch (final Exception e) {
            info.addError(e);
            return InfoItemsPage.emptyPage();
        }
    }


    public static List<InfoItem> getRelatedItemsOrLogError(final StreamInfo info,
                                                           final StreamExtractor extractor) {
        try {
            final InfoItemsCollector<? extends InfoItem, ?> collector = extractor.getRelatedItems();
            if (collector == null) {
                return Collections.emptyList();
            }
            info.addAllErrors(collector.getErrors());

            //noinspection unchecked
            return (List<InfoItem>) collector.getItems();
        } catch (final Exception e) {
            info.addError(e);
            return Collections.emptyList();
        }
    }

    /**
     * @deprecated Use {@link #getRelatedItemsOrLogError(StreamInfo, StreamExtractor)}
     */
    @Deprecated
    public static List<InfoItem> getRelatedVideosOrLogError(final StreamInfo info,
                                                            final StreamExtractor extractor) {
        return getRelatedItemsOrLogError(info, extractor);
    }

}
