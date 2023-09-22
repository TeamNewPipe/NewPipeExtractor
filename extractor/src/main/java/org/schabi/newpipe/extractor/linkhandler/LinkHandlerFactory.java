package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.Objects;

/*
 * Created by Christian Schabesberger on 26.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * LinkHandlerFactory.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class LinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    public abstract String getId(String url) throws ParsingException, UnsupportedOperationException;

    public abstract String getUrl(String id) throws ParsingException, UnsupportedOperationException;

    public abstract boolean onAcceptUrl(String url) throws ParsingException;

    public String getUrl(final String id, final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id);
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    /**
     * Builds a {@link LinkHandler} from a url.<br>
     * Be sure to call {@link Utils#followGoogleRedirectIfNeeded(String)} on the url if overriding
     * this function.
     *
     * @param url the url to extract path and id from
     * @return a {@link LinkHandler} complete with information
     */
    public LinkHandler fromUrl(final String url) throws ParsingException {
        if (Utils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("The url is null or empty");
        }
        final String polishedUrl = Utils.followGoogleRedirectIfNeeded(url);
        final String baseUrl = Utils.getBaseUrl(polishedUrl);
        return fromUrl(polishedUrl, baseUrl);
    }

    /**
     * Builds a {@link LinkHandler} from an URL and a base URL. The URL is expected to be already
     * polished from Google search redirects (otherwise how could {@code baseUrl} have been
     * extracted?).<br>
     * So do not call {@link Utils#followGoogleRedirectIfNeeded(String)} on the URL if overriding
     * this function, since that should be done in {@link #fromUrl(String)}.
     *
     * @param url     the URL without Google search redirects to extract id from
     * @param baseUrl the base URL
     * @return a {@link LinkHandler} complete with information
     */
    public LinkHandler fromUrl(final String url, final String baseUrl) throws ParsingException {
        Objects.requireNonNull(url, "URL cannot be null");
        if (!acceptUrl(url)) {
            throw new ParsingException("URL not accepted: " + url);
        }

        final String id = getId(url);
        return new LinkHandler(url, getUrl(id, baseUrl), id);
    }

    public LinkHandler fromId(final String id) throws ParsingException {
        Objects.requireNonNull(id, "ID cannot be null");
        final String url = getUrl(id);
        return new LinkHandler(url, url, id);
    }

    public LinkHandler fromId(final String id, final String baseUrl) throws ParsingException {
        Objects.requireNonNull(id, "ID cannot be null");
        final String url = getUrl(id, baseUrl);
        return new LinkHandler(url, url, id);
    }

    /**
     * When a VIEW_ACTION is caught this function will test if the url delivered within the calling
     * Intent was meant to be watched with this Service.
     * Return false if this service shall not allow to be called through ACTIONs.
     */
    public boolean acceptUrl(final String url) throws ParsingException {
        return onAcceptUrl(url);
    }
}
