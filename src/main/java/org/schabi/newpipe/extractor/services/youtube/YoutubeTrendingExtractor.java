package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingExtractor.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class YoutubeTrendingExtractor extends KioskExtractor {

    private Document doc;

    public YoutubeTrendingExtractor(StreamingService service, String url, String kioskId)
            throws ExtractionException {
        super(service, url, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String contentCountry = getContentCountry();
        String url = getCleanUrl();
        if(contentCountry != null && !contentCountry.isEmpty()) {
            url += "?gl=" + contentCountry;
        }

        String pageContent = downloader.download(url);
        doc = Jsoup.parse(pageContent, url);
    }

    @Nonnull
    @Override
    public UrlIdHandler getUrlIdHandler() {
        return new YoutubeTrendingUrlIdHandler();
    }

    @Override
    public String getNextPageUrl() {
        return "";
    }

    @Override
    public ListExtractor.InfoItemPage getPage(String pageUrl) {
        return null;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            Element a = doc.select("a[href*=\"/feed/trending\"]").first();
            Element span = a.select("span[class*=\"display-name\"]").first();
            Element nameSpan = span.select("span").first();
            return nameSpan.text();
        } catch (Exception e) {
            throw new ParsingException("Could not get Trending name", e);
        }
    }

    @Nonnull
    @Override
    public StreamInfoItemsCollector getInfoItems() throws ParsingException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Elements uls = doc.select("ul[class*=\"expanded-shelf-content-list\"]");
        for(Element ul : uls) {
            for(final Element li : ul.children()) {
                final Element el = li.select("div[class*=\"yt-lockup-dismissable\"]").first();
                collector.commit(new YoutubeStreamInfoItemExtractor(li) {
                    @Override
                    public String getUrl() throws ParsingException {
                        try {
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.attr("abs:href");
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    @Override
                    public String getName() throws ParsingException {
                        try {
                            Element dl = el.select("h3").first().select("a").first();
                            return dl.text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    @Override
                    public String getUploaderUrl() throws ParsingException {
                        try {
                            String link = getUploaderLink().attr("abs:href");
                            if (link.isEmpty()) {
                                throw new IllegalArgumentException("is empty");
                            }
                            return link;
                        } catch (Exception e) {
                            throw new ParsingException("Could not get Uploader name");
                        }
                    }

                    private Element getUploaderLink() {
                        Element uploaderEl = el.select("div[class*=\"yt-lockup-byline \"]").first();
                        return uploaderEl.select("a").first();
                    }

                    @Override
                    public String getUploaderName() throws ParsingException {
                        try {
                            return getUploaderLink().text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get Uploader name");
                        }
                    }

                    @Override
                    public String getThumbnailUrl() throws ParsingException {
                        try {
                            String url;
                            Element te = li.select("span[class=\"yt-thumb-simple\"]").first()
                                    .select("img").first();
                            url = te.attr("abs:src");
                            // Sometimes youtube sends links to gif files which somehow seem to not exist
                            // anymore. Items with such gif also offer a secondary image source. So we are going
                            // to use that if we've caught such an item.
                            if (url.contains(".gif")) {
                                url = te.attr("abs:data-thumb");
                            }
                            return url;
                        } catch (Exception e) {
                            throw new ParsingException("Could not get thumbnail url", e);
                        }
                    }
                });
            }
        }

        return collector;
    }
}
