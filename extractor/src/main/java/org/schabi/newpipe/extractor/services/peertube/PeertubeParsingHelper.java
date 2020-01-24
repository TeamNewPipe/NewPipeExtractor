package org.schabi.newpipe.extractor.services.peertube;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import com.grack.nanojson.JsonObject;

public class PeertubeParsingHelper {
    
    private PeertubeParsingHelper() {
    }

    public static void validate(JsonObject json) throws ContentNotAvailableException {
        String error = json.getString("error");
        if(!StringUtil.isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }
    
    public static Calendar parseDateFrom(String textualUploadDate) throws ParsingException {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").parse(textualUploadDate);
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }

        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(date);
        return uploadDate;
    }

}
