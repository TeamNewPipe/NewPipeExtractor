package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

/*
 * Created by Christian Schabesberger on 26.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * UrlIdHandler.java is part of NewPipe.
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

public abstract class UrlIdHandler {

    protected String id = "";
    protected String originalUrl = "";

    public abstract String onGetIdFromUrl(String url) throws ParsingException;
    public abstract String getUrl() throws ParsingException;
    public abstract boolean onAcceptUrl(final String url) throws ParsingException;


    public UrlIdHandler setUrl(String url) throws ParsingException {
        if(url == null) throw new IllegalArgumentException("url can not be null");
        originalUrl = url;
        id = onGetIdFromUrl(url);
        return this;
    }

    public UrlIdHandler setId(String id) throws ParsingException {
        if(id == null) throw new IllegalArgumentException("id can not be null");
        this.id = id;
        if(!acceptUrl(getUrl())) {
            throw new ParsingException("Malformed unacceptable url: " + getUrl());
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public String getOriginalUrl() throws ParsingException {
        return (originalUrl == null || originalUrl.isEmpty())
                ? getUrl()
                : originalUrl;
    }

    /**
     * When a VIEW_ACTION is caught this function will test if the url delivered within the calling
     * Intent was meant to be watched with this Service.
     * Return false if this service shall not allow to be called through ACTIONs.
     */
    public boolean acceptUrl(final String url) {
        try {
            return onAcceptUrl(url);
        } catch (Exception e) {
            return false;
        }
    }
}
