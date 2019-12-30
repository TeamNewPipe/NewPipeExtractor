package org.schabi.newpipe.extractor.services.youtube.linkHandler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by Christian Schabesberger on 02.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeParsingHelper.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class YoutubeParsingHelper {

    private YoutubeParsingHelper() {
    }

    private static final String[] RECAPTCHA_DETECTION_SELECTORS = {
            "form[action*=\"/das_captcha\"]",
            "input[name*=\"action_recaptcha_verify\"]"
    };

    public static Document parseAndCheckPage(final String url, final Response response) throws ReCaptchaException {
        final Document document = Jsoup.parse(response.responseBody(), url);

        for (String detectionSelector : RECAPTCHA_DETECTION_SELECTORS) {
            if (!document.select(detectionSelector).isEmpty()) {
                throw new ReCaptchaException("reCAPTCHA challenge requested (detected with selector: \"" + detectionSelector + "\")", url);
            }
        }

        return document;
    }

    public static boolean isYoutubeURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("youtube.com") || host.equalsIgnoreCase("www.youtube.com")
                || host.equalsIgnoreCase("m.youtube.com") || host.equalsIgnoreCase("music.youtube.com");
    }

    public static boolean isYoutubeServiceURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("www.youtube-nocookie.com") || host.equalsIgnoreCase("youtu.be");
    }

    public static boolean isHooktubeURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("hooktube.com");
    }

    public static boolean isInvidioURL(URL url) {
        String host = url.getHost();
        return host.equalsIgnoreCase("invidio.us") || host.equalsIgnoreCase("dev.invidio.us") || host.equalsIgnoreCase("www.invidio.us") || host.equalsIgnoreCase("invidious.snopyta.org") || host.equalsIgnoreCase("de.invidious.snopyta.org") || host.equalsIgnoreCase("fi.invidious.snopyta.org") || host.equalsIgnoreCase("vid.wxzm.sx") || host.equalsIgnoreCase("invidious.kabi.tk") || host.equalsIgnoreCase("invidiou.sh") || host.equalsIgnoreCase("www.invidiou.sh") || host.equalsIgnoreCase("no.invidiou.sh") || host.equalsIgnoreCase("invidious.enkirton.net") || host.equalsIgnoreCase("tube.poal.co") || host.equalsIgnoreCase("invidious.13ad.de") || host.equalsIgnoreCase("yt.elukerio.org");
    }

    public static long parseDurationString(String input)
            throws ParsingException, NumberFormatException {

        // If time separator : is not detected, try . instead

        final String[] splitInput = input.contains(":")
                ? input.split(":")
                : input.split("\\.");

        String days = "0";
        String hours = "0";
        String minutes = "0";
        final String seconds;

        switch (splitInput.length) {
            case 4:
                days = splitInput[0];
                hours = splitInput[1];
                minutes = splitInput[2];
                seconds = splitInput[3];
                break;
            case 3:
                hours = splitInput[0];
                minutes = splitInput[1];
                seconds = splitInput[2];
                break;
            case 2:
                minutes = splitInput[0];
                seconds = splitInput[1];
                break;
            case 1:
                seconds = splitInput[0];
                break;
            default:
                throw new ParsingException("Error duration string with unknown format: " + input);
        }
        return ((((Long.parseLong(days) * 24)
                + Long.parseLong(hours) * 60)
                + Long.parseLong(minutes)) * 60)
                + Long.parseLong(seconds);
    }

    public static Calendar parseDateFrom(String textualUploadDate) throws ParsingException {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(textualUploadDate);
        } catch (ParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }

        final Calendar uploadDate = Calendar.getInstance();
        uploadDate.setTime(date);
        return uploadDate;
    }
}
