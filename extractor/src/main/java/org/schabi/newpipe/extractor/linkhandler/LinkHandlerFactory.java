package org.schabi.newpipe.extractor.linkhandler;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

/*
 * Created by Christian Schabesberger on 26.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * LinkHandlerFactory.java is part of NewPipe.
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

public abstract class LinkHandlerFactory {

    ///////////////////////////////////
    // To Override
    ///////////////////////////////////

    public abstract String getId(String url) throws ParsingException;
    public abstract String getUrl(String id) throws ParsingException;
    public abstract boolean onAcceptUrl(final String url) throws ParsingException;

    public String getUrl(String id, String baseUrl) throws ParsingException {
        return getUrl(id);
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////

    /**
     * Builds a {@link LinkHandler} from a url.<br>
     * Be sure to call {@link Utils#followGoogleRedirectIfNeeded(String)} on the url if overriding
     * this function.
     * @param url the url to extract path and id from
     * @return a {@link LinkHandler} complete with information
     */
    public LinkHandler fromUrl(final String url) throws ParsingException {
        final String polishedUrl = Utils.followGoogleRedirectIfNeeded(url);
        final String baseUrl = Utils.getBaseUrl(polishedUrl);
        return fromUrl(polishedUrl, baseUrl);
    }

    /**
     * Builds a {@link LinkHandler} from a url and a base url. The url is expected to be already
     * polished from google search redirects (otherwise how could {@code baseUrl} have been
     * extracted?).<br>
     * So do not call {@link Utils#followGoogleRedirectIfNeeded(String)} on the url if overriding
     * this function, since that should be done in {@link #fromUrl(String)}.
     * @param url the url without google search redirects to extract id from
     * @param baseUrl the base url
     * @return a {@link LinkHandler} complete with information
     */
    public LinkHandler fromUrl(String url, String baseUrl) throws ParsingException {
        if (url == null) throw new IllegalArgumentException("url can not be null");
        if (!acceptUrl(url)) {
            throw new ParsingException("Malformed unacceptable url: " + url);
        }

        final String id = getId(url);
        return new LinkHandler(url, getUrl(id, baseUrl), id);
    }

    public LinkHandler fromId(String id) throws ParsingException {
        if (id == null) throw new IllegalArgumentException("id can not be null");
        final String url = getUrl(id);
        return new LinkHandler(url, url, id);
    }

    public LinkHandler fromId(String id, String baseUrl) throws ParsingException {
        if (id == null) throw new IllegalArgumentException("id can not be null");
        final String url = getUrl(id, baseUrl);
        return new LinkHandler(url, url, id);
    }

    /**
     * When a VIEW_ACTION is caught this function will test if the url delivered within the calling
     * Intent was meant to be watched with this Service.
     * Return false if this service shall not allow to be called through ACTIONs.
     */
    public boolean acceptUrl(final String url) throws ParsingException {
        try {
            return onAcceptUrl(url);
        } catch (FoundAdException fe) {
            throw fe;
        }
    }

}
