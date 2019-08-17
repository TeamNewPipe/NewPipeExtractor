package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

public class YoutubePlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final Element el;

    public YoutubePlaylistInfoItemExtractor(Element el) {
        this.el = el;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        String url;

        try {
            Element te = el.select("div[class=\"yt-thumb video-thumb\"]").first()
                    .select("img").first();
            url = te.attr("abs:src");

            if (url.contains(".gif")) {
                url = te.attr("abs:data-thumb");
            }
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist thumbnail url", e);
        }

        return url;
    }

    @Override
    public String getName() throws ParsingException {
        String name;
        try {
            final Element title = el.select("[class=\"yt-lockup-title\"]").first()
                    .select("a").first();

            name = title == null ? "" : title.text();
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist name", e);
        }

        return name;
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final Element a = el.select("div[class=\"yt-lockup-meta\"]")
                    .select("ul[class=\"yt-lockup-meta-info\"]")
                    .select("li").select("a").first();

            if(a != null) {
                return a.attr("abs:href");
            }

            // this is for yt premium playlists
            return el.select("h3[class=\"yt-lockup-title\"").first()
                    .select("a").first()
                    .attr("abs:href");

        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name;

        try {
            final Element div = el.select("div[class=\"yt-lockup-byline\"]").first()
                    .select("a").first();

            name = div.text();
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist uploader", e);
        }

        return name;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            final Element count = el.select("span[class=\"formatted-video-count-label\"]").first()
                    .select("b").first();

            return count == null ? 0 : Long.parseLong(Utils.removeNonDigitCharacters(count.text()));
        } catch (Exception e) {
            throw new ParsingException("Failed to extract playlist stream count", e);
        }
    }
}
