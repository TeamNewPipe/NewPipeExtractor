package org.schabi.newpipe.extractor.services.soundcloud;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;

public class SoundcloudParsingHelper {
    private SoundcloudParsingHelper() {
    }

    public static final String clientId() throws ReCaptchaException, IOException, RegexException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download("https://soundcloud.com");
        Document doc = Jsoup.parse(response);

        Element jsElement = doc.select("script[src^=https://a-v2.sndcdn.com/assets/app]").first();
        String js = dl.download(jsElement.attr("src"));

        String clientId = Parser.matchGroup1(",client_id:\"(.*?)\"", js);
        return clientId;
    }

    public static String toTimeAgoString(String time) throws ParsingException {
        try {
            List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365), TimeUnit.DAYS.toMillis(30),
                    TimeUnit.DAYS.toMillis(7), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
                    TimeUnit.SECONDS.toMillis(1));
            List<String> timesString = Arrays.asList("year", "month", "week", "day", "hour", "minute", "second");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");

            long timeAgo = System.currentTimeMillis() - dateFormat.parse(time).getTime();

            StringBuilder timeAgoString = new StringBuilder();

            for (int i = 0; i < times.size(); i++) {
                Long current = times.get(i);
                long currentAmount = timeAgo / current;
                if (currentAmount > 0) {
                    timeAgoString.append(currentAmount).append(" ").append(timesString.get(i))
                            .append(currentAmount != 1 ? "s ago" : " ago");
                    break;
                }
            }
            if (timeAgoString.toString().equals("")) {
                timeAgoString.append("Just now");
            }
            return timeAgoString.toString();
        } catch (ParseException e) {
            throw new ParsingException(e.getMessage());
        }
    }

    public static String toDateString(String time) throws ParsingException {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
            Date date = dateFormat.parse(time);
            SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return newDateFormat.format(date);
        } catch (ParseException e) {
            throw new ParsingException(e.getMessage());
        }
    }

}
