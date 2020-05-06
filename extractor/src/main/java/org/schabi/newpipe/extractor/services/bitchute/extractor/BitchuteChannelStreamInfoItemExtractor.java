package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;

public abstract class BitchuteChannelStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private Element element;

    public BitchuteChannelStreamInfoItemExtractor(Element element) {
        this.element = element;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return YoutubeParsingHelper.parseDurationString(element.
                select("span.video-duration").first().text());
    }

    @Override
    public long getViewCount() throws ParsingException {
        return Utils.mixedNumberWordToLong(element.select("span.video-views").first().text());
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return element.select("div.channel-videos-details").first().text();
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {

        Date date;
        try {
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            date = df.parse(getTextualUploadDate());
        } catch (ParseException e) {
            throw new ParsingException("Couldn't parse date");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new DateWrapper(calendar);
    }

    @Override
    public String getName() throws ParsingException {
        return element.select("div.channel-videos-title").first().text();
    }

    @Override
    public String getUrl() throws ParsingException {
        return element.select("div.channel-videos-title > a").first()
                .absUrl("href");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return element.select("div.channel-videos-image > img").first()
                .absUrl("data-src");
    }
}
