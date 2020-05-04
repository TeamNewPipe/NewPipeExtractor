package org.schabi.newpipe.extractor.services.bitchute.parser;

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

public class BitchuteParserHelper {

    private static final String BITCHUTE_LINK = "https://www.bitchute.com/";

    private static String cookies;
    private static String csrfToken;

    public static boolean isInitDone() {
        if (cookies == null || csrfToken == null) {
            return false;
        }
        return true;
    }

    public static void init() throws ReCaptchaException, IOException {
        Response response = getDownloader().get(BITCHUTE_LINK);
        StringJoiner sJ = new StringJoiner(";");
        for (Map.Entry entry : response.responseHeaders().entrySet()) {
            if (entry.getKey().equals("Set-Cookie")) {
                String val = entry.getValue().toString().split(";", 2)[0];
                sJ.add(val);
                if (val.contains("csrf"))
                    csrfToken = val.split("=", 2)[1];
            }
        }
        cookies = sJ.toString();
    }

    public static Map<String, List<String>> getPostHeader(int contentLength) throws IOException, ReCaptchaException {
        Map<String, List<String>> headers = getBasicHeader();
        headers.put("Content-Type", Collections.singletonList("application/x-www-form-urlencoded"));
        headers.put("Content-Length", Collections.singletonList(String.valueOf(contentLength)));
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
        byte[] data = String.format("csrfmiddlewaretoken=%s", csrfToken).getBytes(StandardCharsets.UTF_8);
        Response response = getDownloader().post(
                String.format("https://www.bitchute.com/channel/%s/counts/", channelID),
                getPostHeader(data.length),
                data
        );

        try {
            JsonObject jsonObject = JsonParser.object().from(response.responseBody());
            return String.valueOf(jsonObject.getLong("subscriber_count"));
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse bitchute sub count json");
        }
    }

    public static Document getExtendDocumentForUrl(String pageUrl, String offset)
            throws IOException, ExtractionException {
        if (!isInitDone()) {
            init();
        }

        byte[] data = String.format("csrfmiddlewaretoken=%s&offset=%s", csrfToken,offset)
                .getBytes(StandardCharsets.UTF_8);
        Response response = getDownloader().post(
                String.format("%s/extend/", pageUrl),
                getPostHeader(data.length),
                data
        );

        try {
            JsonObject jsonObject = JsonParser.object().from(response.responseBody());
            return Jsoup.parse(jsonObject.getString("html")
                    .replace("\n",""),pageUrl);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse bitchute sub count json");
        }
    }
}