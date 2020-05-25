package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final Element searchResult, resultInfo;

    public BandcampPlaylistInfoItemExtractor(Element searchResult) {
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getUploaderName() {
        return resultInfo.getElementsByClass("subhead").text()
                .split(" by")[0];
    }

    @Override
    public long getStreamCount() {
        String length = resultInfo.getElementsByClass("length").text();
        return Integer.parseInt(length.split(" track")[0]);
    }

    @Override
    public String getName() {
        return resultInfo.getElementsByClass("heading").text();
    }

    @Override
    public String getUrl() {
        return resultInfo.getElementsByClass("itemurl").text();
    }

    @Override
    public String getThumbnailUrl() {
        Element img = searchResult.getElementsByClass("art").first()
                .getElementsByTag("img").first();
        if (img != null) {
            return img.attr("src");
        } else return null;
    }
}
