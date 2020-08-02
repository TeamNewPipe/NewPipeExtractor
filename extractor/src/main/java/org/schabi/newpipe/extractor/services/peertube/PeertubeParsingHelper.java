package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PeertubeParsingHelper {
    public static final String START_KEY = "start";
    public static final String COUNT_KEY = "count";
    public static final int ITEMS_PER_PAGE = 12;
    public static final String START_PATTERN = "start=(\\d*)";

    private PeertubeParsingHelper() {
    }

    public static void validate(final JsonObject json) throws ContentNotAvailableException {
        final String error = json.getString("error");
        if (!Utils.isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }

    public static Calendar parseDateFrom(final String textualUploadDate) throws ParsingException {
        final Date date;
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = sdf.parse(textualUploadDate);
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }

        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(date);
        return uploadDate;
    }

    public static Page getNextPage(final String prevPageUrl, final long total) {
        final String prevStart;
        try {
            prevStart = Parser.matchGroup1(START_PATTERN, prevPageUrl);
        } catch (Parser.RegexException e) {
            return null;
        }
        if (Utils.isBlank(prevStart)) return null;
        final long nextStart;
        try {
            nextStart = Long.parseLong(prevStart) + ITEMS_PER_PAGE;
        } catch (NumberFormatException e) {
            return null;
        }

        if (nextStart >= total) {
            return null;
        } else {
            return new Page(prevPageUrl.replace(START_KEY + "=" + prevStart, START_KEY + "=" + nextStart));
        }
    }

    public static void collectStreamsFrom(final InfoItemsCollector collector, final JsonObject json, final String baseUrl) throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (Exception e) {
            throw new ParsingException("Unable to extract list info", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final PeertubeStreamInfoItemExtractor extractor = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                collector.commit(extractor);
            }
        }
    }
}
