package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

public class YoutubeMixPlaylistExtractor extends PlaylistExtractor {

  private Document doc;

  public YoutubeMixPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
    super(service, linkHandler);
  }

  @Override
  public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
    final String url = getUrl();
    final Response response = downloader.get(url, getExtractorLocalization());
    doc = YoutubeParsingHelper.parseAndCheckPage(url, response);
  }

  @Nonnull
  @Override
  public String getName() throws ParsingException {
    try {
      return doc.select("div[class=\"playlist-info\"] h3[class=\"playlist-title\"]").first().text();
    } catch (Exception e) {
      throw new ParsingException("Could not get playlist name", e);
    }
  }

  @Override
  public String getThumbnailUrl() throws ParsingException {
    try {
      return doc.select("ol[class*=\"playlist-videos-list\"] li").first().attr("data-thumbnail-url");
    } catch (Exception e) {
      throw new ParsingException("Could not get playlist thumbnail", e);
    }
  }

  @Override
  public String getBannerUrl() {
    return "";
  }

  @Override
  public String getUploaderUrl() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public String getUploaderName() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public String getUploaderAvatarUrl() {
    //Youtube mix are auto-generated
    return "";
  }

  @Override
  public long getStreamCount() {
    // Auto-generated playlist always start with 25 videos and are endless
    // But the html doesn't have a continuation url
    return doc.select("ol[class*=\"playlist-videos-list\"] li").size();
  }

  @Nonnull
  @Override
  public InfoItemsPage<StreamInfoItem> getInitialPage() {
    StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
    Element ol = doc.select("ol[class*=\"playlist-videos-list\"]").first();
    collectStreamsFrom(collector, ol);
    return new InfoItemsPage<>(collector, getNextPageUrl());
  }

  @Override
  public String getNextPageUrl() {
    return "";
  }

  @Override
  public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) {
    //Continuations are not implemented
    return null;
  }

  private void collectStreamsFrom(
      @Nonnull StreamInfoItemsCollector collector,
      @Nullable Element element) {
    collector.reset();

    if (element == null) {
      return;
    }

    final LinkHandlerFactory streamLinkHandlerFactory = getService().getStreamLHFactory();
    final TimeAgoParser timeAgoParser = getTimeAgoParser();

    for (final Element li : element.children()) {

      collector.commit(new YoutubeStreamInfoItemExtractor(li, timeAgoParser) {

        @Override
        public boolean isAd() {
          return false;
        }

        @Override
        public String getUrl() throws ParsingException {
          try {
            return streamLinkHandlerFactory.fromId(li.attr("data-video-id")).getUrl();
          } catch (Exception e) {
            throw new ParsingException("Could not get web page url for the video", e);
          }
        }

        @Override
        public String getName() throws ParsingException {
          try {
            return li.attr("data-video-title");
          } catch (Exception e) {
            throw new ParsingException("Could not get name", e);
          }
        }

        @Override
        public long getDuration() throws ParsingException {
          //Not present in doc
          return 0;
        }

        @Override
        public String getUploaderName() throws ParsingException {
          try {
            return li.select(
                "div[class=\"playlist-video-description\"]"
                    + "span[class=\"video-uploader-byline\"]")
                .first()
                .text();
          } catch (Exception e) {
            throw new ParsingException("Could not get uploader", e);
          }
        }

        @Override
        public String getUploaderUrl() {
          //Not present in doc
          return "";
        }

        @Override
        public String getTextualUploadDate() {
          //Not present in doc
          return "";
        }

        @Override
        public long getViewCount() {
          return -1;
        }

        @Override
        public String getThumbnailUrl() throws ParsingException {
          try {
            return "https://i.ytimg.com/vi/" + streamLinkHandlerFactory.fromUrl(getUrl()).getId()
                + "/hqdefault.jpg";
          } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
          }
        }
      });
    }
  }
}
