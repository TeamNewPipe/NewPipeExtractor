package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class YoutubeFeedInfoItemExtractor implements StreamInfoItemExtractor {
    private final Element entryElement;

    public YoutubeFeedInfoItemExtractor(Element entryElement) {
        this.entryElement = entryElement;
    }

    @Override
    public StreamType getStreamType() {
        // It is not possible to determine the stream type using the feed endpoint.
        // All entries are considered a video stream.
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() {
        // Not available when fetching through the feed endpoint.
        return -1;
    }

    @Override
    public long getViewCount() {
        return Long.parseLong(entryElement.getElementsByTag("media:statistics").first().attr("views"));
    }

    @Override
    public String getUploaderName() {
        return entryElement.select("author > name").first().text();
    }

    @Override
    public String getUploaderUrl() {
        return entryElement.select("author > uri").first().text();
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return entryElement.getElementsByTag("published").first().text();
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final Date date;
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = dateFormat.parse(getTextualUploadDate());
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date (\"" + getTextualUploadDate() + "\")", e);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new DateWrapper(calendar);
    }

    @Override
    public String getName() {
        return entryElement.getElementsByTag("title").first().text();
    }

    @Override
    public String getUrl() {
        return entryElement.getElementsByTag("link").first().attr("href");
    }

    @Override
    public String getThumbnailUrl() {
        return entryElement.getElementsByTag("media:thumbnail").first().attr("url");
    }
}
