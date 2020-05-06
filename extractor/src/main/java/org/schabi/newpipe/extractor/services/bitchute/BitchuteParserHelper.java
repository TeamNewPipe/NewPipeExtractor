package org.schabi.newpipe.extractor.services.bitchute;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.schabi.newpipe.extractor.NewPipe.getDownloader;
import static org.schabi.newpipe.extractor.services.bitchute.BitchuteService.BITCHUTE_LINK;

public class BitchuteParserHelper {


    private static String cookies;
    private static String csrfToken;

    public static boolean isInitDone() {
        if (cookies == null || csrfToken == null || cookies.isEmpty() || csrfToken.isEmpty()) {
            return false;
        }
        return true;
    }

    public static void init() throws ReCaptchaException, IOException {
        Response response = getDownloader().get(BITCHUTE_LINK);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : response.responseHeaders().entrySet()) {
            if (entry.getKey().equals("set-cookie")) {
                List<String> values = (List<String>) entry.getValue();
                for(String v: values) {
                    String val = v.split(";", 2)[0];
                    sb.append(val).append(";");
                    if (val.contains("csrf"))
                        csrfToken = val.split("=", 2)[1];
                }
                break;
            }
        }
        cookies = sb.toString();
    }

    public static Map<String, List<String>> getPostHeader(int contentLength) throws IOException, ReCaptchaException {
        Map<String, List<String>> headers = getBasicHeader();
        headers.put("Content-Type", Collections.singletonList("application/x-www-form-urlencoded"));
        headers.put("Content-Length", Collections.singletonList(String.valueOf(contentLength)));
        System.out.println("Headers: ");
        for(Map.Entry m: headers.entrySet())
            System.out.println(m.getKey()+": "+m.getValue());
        return headers;
    }

    public static Map<String, List<String>> getBasicHeader() throws IOException, ReCaptchaException {
        if (!isInitDone()) {
            init();
        }
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cookie", Collections.singletonList(cookies));
        headers.put("Referer", Collections.singletonList(BITCHUTE_LINK));
        return headers;
    }

    public static String getSubscriberCountForChannelID(String channelID)
            throws IOException, ExtractionException {
        if (!isInitDone()) {
            init();
        }
        System.out.println("Seeeeeeeeeeee");
        System.out.println(channelID);
        byte[] data = String.format("csrfmiddlewaretoken=%s", csrfToken).getBytes(StandardCharsets.UTF_8);
        Response response = getDownloader().post(
                String.format("https://www.bitchute.com/channel/%s/counts/", channelID),
                getPostHeader(data.length),
                data
        );


        System.out.println(response.responseBody());

        try {
            JsonObject jsonObject = JsonParser.object().from(response.responseBody());
            return String.valueOf(jsonObject.getLong("subscriber_count"));
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse bitchute sub count json");
        }
    }

    public static VideoCount getVideoCountObjectForStreamID(String streamID)
            throws IOException, ExtractionException {
        if(!isInitDone()){
            init();
        }

        byte[] data = String.format("csrfmiddlewaretoken=%s", csrfToken).getBytes(StandardCharsets.UTF_8);
        Response response = getDownloader().post(
                String.format("https://www.bitchute.com/video/%s/counts/", streamID),
                getPostHeader(data.length),
                data
        );

        try {
            JsonObject jsonObject = JsonParser.object().from(response.responseBody());
            return (new VideoCount(jsonObject.getInt("like_count"),
                    jsonObject.getInt("dislike_count"),jsonObject.getInt("view_count")));
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse bitchute sub count json");
        }

    }

    public static Document getExtendDocumentForUrl(String pageUrl, String offset)
            throws IOException, ExtractionException {
        if (!isInitDone()) {
            init();
        }

        byte[] data = String.format("csrfmiddlewaretoken=%s&offset=%s", csrfToken, offset)
                .getBytes(StandardCharsets.UTF_8);
        Response response = getDownloader().post(
                String.format("%s/extend/", pageUrl),
                getPostHeader(data.length),
                data
        );

        try {
            JsonObject jsonObject = JsonParser.object().from(response.responseBody());
            return Jsoup.parse(jsonObject.getString("html")
                    .replace("\n", ""), pageUrl);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse bitchute sub count json");
        }
    }

    public static class VideoCount {
        private long likeCount;
        private long dislikeCount;
        private long viewCount;

        public VideoCount(long likeCount, long dislikeCount, long viewCount) {
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.viewCount = viewCount;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public long getDislikeCount() {
            return dislikeCount;
        }

        public long getViewCount() {
            return viewCount;
        }
    }
}