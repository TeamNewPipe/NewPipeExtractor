package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Info implements Serializable {

    private final int serviceId;
    /**
     * Id of this Info object <br>
     * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     &gt;    RER5qCTzZ7
     */
    private final String id;
    /**
     * Different than the {@link #originalUrl} in the sense that it <i>may</i> be set as a cleaned
     * url.
     *
     * @see LinkHandler#getUrl()
     * @see Extractor#getOriginalUrl()
     */
    private final String url;
    /**
     * The url used to start the extraction of this {@link Info} object.
     *
     * @see Extractor#getOriginalUrl()
     */
    private String originalUrl;
    private final String name;

    private final List<Throwable> errors = new ArrayList<>();

    public void addError(final Throwable throwable) {
        this.errors.add(throwable);
    }

    public void addAllErrors(final Collection<Throwable> throwables) {
        this.errors.addAll(throwables);
    }

    public Info(final int serviceId,
                final String id,
                final String url,
                final String originalUrl,
                final String name) {
        this.serviceId = serviceId;
        this.id = id;
        this.url = url;
        this.originalUrl = originalUrl;
        this.name = name;
    }

    public Info(final int serviceId, final LinkHandler linkHandler, final String name) {
        this(serviceId,
                linkHandler.getId(),
                linkHandler.getUrl(),
                linkHandler.getOriginalUrl(),
                name);
    }

    @Override
    public String toString() {
        final String ifDifferentString
                = url.equals(originalUrl) ? "" : " (originalUrl=\"" + originalUrl + "\")";
        return getClass().getSimpleName() + "[url=\"" + url + "\"" + ifDifferentString
                + ", name=\"" + name + "\"]";
    }

    // if you use an api and want to handle the website url
    // overriding original url is essential
    public void setOriginalUrl(final String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public int getServiceId() {
        return serviceId;
    }

    public StreamingService getService() {
        try {
            return NewPipe.getService(serviceId);
        } catch (final ExtractionException e) {
            // this should be unreachable, as serviceId certainly refers to a valid service
            throw new RuntimeException("Info object has invalid service id", e);
        }
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getName() {
        return name;
    }

    public List<Throwable> getErrors() {
        return errors;
    }
}
