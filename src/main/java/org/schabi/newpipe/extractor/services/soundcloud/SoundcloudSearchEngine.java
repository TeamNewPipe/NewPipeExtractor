package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.search.InfoItemSearchCollector;
import org.schabi.newpipe.extractor.search.SearchEngine;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.EnumSet;

public class SoundcloudSearchEngine extends SearchEngine {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public SoundcloudSearchEngine(int serviceId) {
        super(serviceId);
    }

    @Override
    public InfoItemSearchCollector search(String query, int page, String languageCode, EnumSet<Filter> filter) throws IOException, ExtractionException {
        InfoItemSearchCollector collector = getInfoItemSearchCollector();

        Downloader downloader = NewPipe.getDownloader();

        String url = "https://api-v2.soundcloud.com/search";

        if (filter.contains(Filter.STREAM) && !filter.contains(Filter.CHANNEL)) {
            url += "/tracks";
        } else if (!filter.contains(Filter.STREAM) && filter.contains(Filter.CHANNEL)) {
            url += "/users";
        }

        url += "?q=" + URLEncoder.encode(query, CHARSET_UTF_8)
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10"
                + "&offset=" + Integer.toString(page * 10);

        String searchJson = downloader.download(url);
        JSONObject search = new JSONObject(searchJson);
        JSONArray searchCollection = search.getJSONArray("collection");

        if (searchCollection.length() == 0) {
            throw new NothingFoundException("Nothing found");
        }

        for (int i = 0; i < searchCollection.length(); i++) {
            JSONObject searchResult = searchCollection.getJSONObject(i);
            String kind = searchResult.getString("kind");
            if (kind.equals("user")) {
                collector.commit(new SoundcloudChannelInfoItemExtractor(searchResult));
            } else if (kind.equals("track")) {
                collector.commit(new SoundcloudStreamInfoItemExtractor(searchResult));
            }
        }

        return collector;
    }
}
