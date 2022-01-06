package org.schabi.newpipe.extractor.services.niconico.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.services.niconico.NiconicoServiceParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class NiconicoTrendRSSExtractor implements StreamInfoItemExtractor {
    private final Element item;
    private final Document cdata;
    private final String uploaderName;
    private final String uploaderUrl;
    private final String uploaderAvaterUrl;

    public NiconicoTrendRSSExtractor(final Element e, final @Nullable String upldName,
                                     final @Nullable String upldUrl,
                                     final @Nullable String upldAvaterUrl) {
        item = e;
        cdata = Jsoup.parse(e.select("description").text());

        uploaderName = upldName;
        uploaderUrl = upldUrl;
        uploaderAvaterUrl = upldAvaterUrl;
    }

    @Override
    public String getName() throws ParsingException {
        final String title = item.select("title").text();
        if (Parser.isMatch(NiconicoService.TRENDING_RSS_STR, title))
        {
            return Parser.matchGroup1(NiconicoService.TRENDING_RSS_STR, title);
        }
        return title;
    }

    @Override
    public String getUrl() throws ParsingException {
        // idk why cannot use select("link").text()
        final List<TextNode> textNodes = item.textNodes();
        final Optional<TextNode> url = textNodes.stream().filter(
                str -> Parser.isMatch(NiconicoService.SMILEVIDEO, str.text()))
                .findFirst();
        if (url.isPresent()) {
            return url.get().text();
        }
        throw new ParsingException("could not get video's url.");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return cdata.getElementsByClass("nico-thumbnail")
                .select("img")
                .attr("src");
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return -1;
    }

    @Override
    public long getViewCount() throws ParsingException {
        final String count = cdata.getElementsByClass("nico-info-total-view").text();
        if (count.isEmpty()) {
            return -1;
        }
        return Long.parseLong(count.replaceAll(",", ""));
    }

    @Override
    public String getUploaderName() throws ParsingException {
        if (uploaderName == null || uploaderName.isEmpty())
        {
            return "";
        }
        return uploaderName;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        if (uploaderUrl == null || uploaderUrl.isEmpty())
        {
            return "";
        }
        return uploaderUrl;
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return uploaderAvaterUrl;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        // pubDate returns wrong date, and it should parse from nico-info-date.
        final String strDate = cdata.getElementsByClass("nico-info-date").text();
        return strDate;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        // pubDate returns wrong date, and it should parse from nico-info-date.
        final String strDate = cdata.getElementsByClass("nico-info-date").text();
        return NiconicoServiceParsingHelper.parseRSSDateTime(strDate);
    }
}
