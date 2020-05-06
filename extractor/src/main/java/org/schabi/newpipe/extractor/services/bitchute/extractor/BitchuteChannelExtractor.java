package org.schabi.newpipe.extractor.services.bitchute.extractor;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bitchute.BitchuteParserHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;

public class BitchuteChannelExtractor extends ChannelExtractor {
    private Document doc;
    private String channelName;
    private String channelUrl;

    public BitchuteChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        Response response = getDownloader().get(getUrl(),
                BitchuteParserHelper.getBasicHeader());
        doc = Jsoup.parse(response.responseBody(), getUrl());
    }

    private String getChannelID() throws ParsingException, MalformedURLException {
        return Utils.stringToURL(getAvatarUrl()).getPath().split("/", 0)[3];
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            if (channelName == null) {
                channelName = doc.select("#channel-title").first().text();
            }
            return channelName;
        } catch (Exception e) {
            throw new ParsingException("Error parsing Channel Name");
        }
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        try {
            if (channelUrl == null) {
                channelUrl = doc.select("#page-bar > div > div > div.image-container > a > img")
                        .first().attr("data-src");
            }
            return channelUrl;
        } catch (Exception e) {
            throw new ParsingException("Error parsing Channel Avatar Url");
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return doc.select("#channel-description").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing Channel Description");
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        try {
            return Utils.mixedNumberWordToLong(BitchuteParserHelper
                    .getSubscriberCountForChannelID(getChannelID()));
        } catch (Exception e) {
            throw new ParsingException("Error parsing Channel Subscribers");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("offset", "0");
        jsonObject.put("name", getName());
        jsonObject.put("url", getUrl());
        return getInfoItemsPage(this.doc, jsonObject);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String json)
            throws IOException, ExtractionException {
        try {
            JsonObject jsonObject = JsonParser.object().from(json);
            System.out.println(jsonObject);
            return getInfoItemsPage(BitchuteParserHelper
                            .getExtendDocumentForUrl(getUrl(), jsonObject.getString("offset")),
                    jsonObject);
        } catch (JsonParserException e) {
            throw new ParsingException("Error parsing url json");
        }
    }

    @Override
    public String getNextPageUrl() {
        return null;
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return getAvatarUrl();
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    private InfoItemsPage<StreamInfoItem>
    getInfoItemsPage(Document doc, final JsonObject jsonObject) {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Elements videos = doc.select(".channel-videos-container");
        for (final Element e : videos) {
            collector.commit(new BitchuteChannelStreamInfoItemExtractor(e) {
                @Override
                public String getUploaderName() throws ParsingException {
                    return jsonObject.getString("name");
                }

                @Override
                public String getUploaderUrl() throws ParsingException {
                    return jsonObject.getString("url");
                }
            });
        }
        int offset = Integer.parseInt(jsonObject.getString("offset"));
        if (videos.size() < 25)
            return new InfoItemsPage<>(collector, null);
        offset += 25;
        jsonObject.put("offset", String.valueOf(offset));
        return new InfoItemsPage<>(collector, JsonWriter.string(jsonObject));
    }
}
