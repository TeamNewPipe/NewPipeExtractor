package org.schabi.newpipe.extractor;

/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * NewPipe Extractor.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Provides access to streaming services supported by NewPipe.
 */
public final class NewPipe {
    private static Downloader downloader;
    private static Locale preferredLocale;
    private static ContentCountry preferredContentCountry;

    private NewPipe() {
    }

    public static void init(final Downloader d) {
        init(d, Locale.UK);
    }

    public static void init(final Downloader d, final Locale l) {
        init(d, l, ContentCountry.fromLocale(l));
    }

    public static void init(final Downloader d, final Locale l, final ContentCountry c) {
        downloader = d;
        preferredLocale = l;
        preferredContentCountry = c;
    }

    public static Downloader getDownloader() {
        return downloader;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static List<StreamingService> getServices() {
        return ServiceList.all();
    }

    public static StreamingService getService(final int serviceId) throws ExtractionException {
        return ServiceList.all().stream()
                .filter(service -> service.getServiceId() == serviceId)
                .findFirst()
                .orElseThrow(() -> new ExtractionException(
                        "There's no service with the id = \"" + serviceId + "\""));
    }

    public static StreamingService getService(final String serviceName) throws ExtractionException {
        return ServiceList.all().stream()
                .filter(service -> service.getServiceInfo().getName().equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new ExtractionException(
                        "There's no service with the name = \"" + serviceName + "\""));
    }

    public static StreamingService getServiceByUrl(final String url) throws ExtractionException {
        for (final StreamingService service : ServiceList.all()) {
            if (service.getLinkTypeByUrl(url) != StreamingService.LinkType.NONE) {
                return service;
            }
        }
        throw new ExtractionException("No service can handle the url = \"" + url + "\"");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    public static void setupLocalization(final Locale locale) {
        setupLocalization(locale, null);
    }

    public static void setupLocalization(final Locale locale,
                                         @Nullable final ContentCountry country) {
        preferredLocale = locale;
        preferredContentCountry = country != null ? country : ContentCountry.fromLocale(locale);
    }

    @Nonnull
    public static Locale getPreferredLocale() {
        return preferredLocale == null ? Locale.UK : preferredLocale;
    }

    public static void setPreferredLocale(final Locale locale) {
        preferredLocale = locale;
    }

    @Nonnull
    public static ContentCountry getPreferredContentCountry() {
        return preferredContentCountry == null ? ContentCountry.DEFAULT : preferredContentCountry;
    }

    public static void setPreferredContentCountry(final ContentCountry contentCountry) {
        preferredContentCountry = contentCountry;
    }
}
