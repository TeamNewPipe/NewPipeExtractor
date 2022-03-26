package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.stream.Description;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MetaInfo implements Serializable {

    private String title = "";
    private Description content;
    private List<URL> urls = new ArrayList<>();
    private List<String> urlTexts = new ArrayList<>();

    public MetaInfo(@Nonnull final String title,
                    @Nonnull final Description content,
                    @Nonnull final List<URL> urls,
                    @Nonnull final List<String> urlTexts) {
        this.title = title;
        this.content = content;
        this.urls = urls;
        this.urlTexts = urlTexts;
    }

    public MetaInfo() {
    }

    /**
     * @return Title of the info. Can be empty.
     */
    @Nonnull
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nonnull final String title) {
        this.title = title;
    }

    @Nonnull
    public Description getContent() {
        return content;
    }

    public void setContent(@Nonnull final Description content) {
        this.content = content;
    }

    @Nonnull
    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(@Nonnull final List<URL> urls) {
        this.urls = urls;
    }

    public void addUrl(@Nonnull final URL url) {
        urls.add(url);
    }

    @Nonnull
    public List<String> getUrlTexts() {
        return urlTexts;
    }

    public void setUrlTexts(@Nonnull final List<String> urlTexts) {
        this.urlTexts = urlTexts;
    }

    public void addUrlText(@Nonnull final String urlText) {
        urlTexts.add(urlText);
    }
}
