package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonObject;
import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PeertubeParsingHelper {

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

}
