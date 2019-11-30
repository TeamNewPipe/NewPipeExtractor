package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MediaCCCParsingHelper {
    private MediaCCCParsingHelper() {
    }

    public static Calendar parseDateFrom(String textualUploadDate) throws ParsingException {
        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(DateUtils.tryParseDateTimeString(textualUploadDate));
        return uploadDate;
    }

}
