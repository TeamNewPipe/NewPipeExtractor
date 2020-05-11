package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonObject;
import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;

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

    public static void validate(JsonObject json) throws ContentNotAvailableException {
        String error = json.getString("error");
        if (!StringUtil.isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }

    public static Calendar parseDateFrom(String textualUploadDate) throws ParsingException {
        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = sdf.parse(textualUploadDate);
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }

        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(date);
        return uploadDate;
    }

    public static String getNextPageUrl(String prevPageUrl, long total) {
        String prevStart;
        try {
            prevStart = Parser.matchGroup1(START_PATTERN, prevPageUrl);
        } catch (Parser.RegexException e) {
            return "";
        }
        if (StringUtil.isBlank(prevStart)) return "";
        long nextStart = 0;
        try {
            nextStart = Long.parseLong(prevStart) + ITEMS_PER_PAGE;
        } catch (NumberFormatException e) {
            return "";
        }

        if (nextStart >= total) {
            return "";
        } else {
            return prevPageUrl.replace(START_KEY + "=" + prevStart, START_KEY + "=" + nextStart);
        }
    }

}
