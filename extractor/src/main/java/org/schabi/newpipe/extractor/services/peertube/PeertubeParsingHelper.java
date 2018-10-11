package org.schabi.newpipe.extractor.services.peertube;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class PeertubeParsingHelper {
    
    private PeertubeParsingHelper() {
    }

    public static String toDateString(String time) throws ParsingException {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").parse(time);
            SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return newDateFormat.format(date);
        } catch (ParseException e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }
}
