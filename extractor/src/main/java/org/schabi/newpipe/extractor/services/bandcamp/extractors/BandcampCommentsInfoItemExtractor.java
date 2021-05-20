package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

public class BandcampCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final Element writing;
    private final String url;

    public BandcampCommentsInfoItemExtractor(Element writing, String url) {
        this.writing = writing;
        this.url = url;
    }

    @Override
    public String getName() throws ParsingException {
        return writing.getElementsByClass("text").first().ownText();
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return writing.getElementsByClass("thumb").attr("src");
    }

    @Override
    public String getCommentText() {
        return writing.getElementsByClass("text").first().ownText();
    }

    @Override
    public String getTextualUploadDate() {
        return "";
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Override
    public String getCommentId() {
        return "";
    }

    @Override
    public String getUploaderUrl() {
        //return writing.getElementsByClass("name").attr("href");
        // Fan links cannot be opened
        return "";
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return writing.getElementsByClass("name").first().text();
    }

    @Override
    public String getUploaderAvatarUrl() {
        return writing.getElementsByClass("thumb").attr("src");
    }

    @Override
    public boolean isHeartedByUploader() {
        return false;
    }

    @Override
    public boolean isPinned() {
        return false;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }
}
