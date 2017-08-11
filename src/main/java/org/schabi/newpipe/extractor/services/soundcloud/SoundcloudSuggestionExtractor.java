package org.schabi.newpipe.extractor.services.soundcloud;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SoundcloudSuggestionExtractor extends SuggestionExtractor {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public SoundcloudSuggestionExtractor(int serviceId) {
        super(serviceId);
    }

    @Override
    public List<String> suggestionList(String query, String contentCountry) throws RegexException, ReCaptchaException, IOException {
        List<String> suggestions = new ArrayList<>();

        Downloader dl = NewPipe.getDownloader();

        String url = "https://api-v2.soundcloud.com/search/queries"
                + "?q=" + URLEncoder.encode(query, CHARSET_UTF_8)
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10";

        String response = dl.download(url);
        JSONObject responseObject = new JSONObject(response);
        JSONArray responseCollection = responseObject.getJSONArray("collection");

        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject suggestion = responseCollection.getJSONObject(i);
            suggestions.add(suggestion.getString("query"));
        }

        return suggestions;
    }
}
