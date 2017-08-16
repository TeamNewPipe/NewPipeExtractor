package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.search.InfoItemSearchCollector;
import org.schabi.newpipe.extractor.search.SearchEngine;

import java.io.IOException;
import java.net.URLEncoder;

public class SoundcloudSearchEngine extends SearchEngine {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public SoundcloudSearchEngine(int serviceId) {
        super(serviceId);
    }

    @Override
    public InfoItemSearchCollector search(String query, int page, String languageCode, Filter filter) throws IOException, ExtractionException {
        InfoItemSearchCollector collector = getInfoItemSearchCollector();

        Downloader dl = NewPipe.getDownloader();

        String url = "https://api-v2.soundcloud.com/search";

        switch (filter) {
            case STREAM:
                url += "/tracks";
                break;
            case CHANNEL:
                url += "/users";
                break;
            case PLAYLIST:
                url += "/playlists";
                break;
            case ANY:
                // Don't append any parameter to search for everything
            default:
                break;
        }

        url += "?q=" + URLEncoder.encode(query, CHARSET_UTF_8)
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10"
                + "&offset=" + Integer.toString(page * 10);

        JsonArray searchCollection;
        try {
            searchCollection = JsonParser.object().from(dl.download(url)).getArray("collection");
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        if (searchCollection.size() == 0) {
            throw new NothingFoundException("Nothing found");
        }

        for (Object result : searchCollection) {
            if (!(result instanceof JsonObject)) continue;
            //noinspection ConstantConditions
            JsonObject searchResult = (JsonObject) result;
            String kind = searchResult.getString("kind", "");
            switch (kind) {
                case "user":
                    collector.commit(new SoundcloudChannelInfoItemExtractor(searchResult));
                    break;
                case "track":
                    collector.commit(new SoundcloudStreamInfoItemExtractor(searchResult));
                    break;
                case "playlist":
                    collector.commit(new SoundcloudPlaylistInfoItemExtractor(searchResult));
                    break;
            }
        }

        return collector;
    }
}
