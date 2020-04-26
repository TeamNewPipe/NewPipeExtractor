package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    private String title, artist, url, cover;
    private int trackCount;

    public BandcampPlaylistInfoItemExtractor(Element searchResult) {

        Element resultInfo = searchResult.getElementsByClass("result-info").first();

        Element img = searchResult.getElementsByClass("art").first()
                .getElementsByTag("img").first();
        if (img != null) {
            cover = img.attr("src");
        }

        title = resultInfo.getElementsByClass("heading").text();
        url = resultInfo.getElementsByClass("itemurl").text();

        artist = resultInfo.getElementsByClass("subhead").text()
                .split(" by")[0];

        String length = resultInfo.getElementsByClass("length").text();
        trackCount = Integer.parseInt(length.split(" track")[0]);

    }

    public BandcampPlaylistInfoItemExtractor(JsonObject featuredStory) {
        title = featuredStory.getString("album_title");
        artist = featuredStory.getString("band_name");
        url = featuredStory.getString("item_url");
        cover = featuredStory.has("art_id") ? getImageUrl(featuredStory.getLong("art_id"), true) : "";
        trackCount = featuredStory.getInt("num_streamable_tracks");
    }

    @Override
    public String getUploaderName() {
        return artist;
    }

    @Override
    public long getStreamCount() {
        return trackCount;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return cover;
    }
}
