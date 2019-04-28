package org.schabi.newpipe.extractor;

/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * NewPipe.java is part of NewPipe.
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

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Provides access to streaming services supported by NewPipe.
 */
public class NewPipe {
    private static Downloader downloader;
    private static Localization preferredLocalization;
    private static ContentCountry preferredContentCountry;

    private NewPipe() {
    }

    public static void init(Downloader d) {
        downloader = d;
        preferredLocalization = Localization.DEFAULT;
        preferredContentCountry = ContentCountry.DEFAULT;
    }

    public static void init(Downloader d, Localization l) {
        downloader = d;
        preferredLocalization = l;
        preferredContentCountry = l.getCountryCode().isEmpty() ? ContentCountry.DEFAULT : new ContentCountry(l.getCountryCode());
    }

    public static void init(Downloader d, Localization l, ContentCountry c) {
        downloader = d;
        preferredLocalization = l;
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

    public static StreamingService getService(int serviceId) throws ExtractionException {
        for (StreamingService service : ServiceList.all()) {
            if (service.getServiceId() == serviceId) {
                return service;
            }
        }
        throw new ExtractionException("There's no service with the id = \"" + serviceId + "\"");
    }

    public static StreamingService getService(String serviceName) throws ExtractionException {
        for (StreamingService service : ServiceList.all()) {
            if (service.getServiceInfo().getName().equals(serviceName)) {
                return service;
            }
        }
        throw new ExtractionException("There's no service with the name = \"" + serviceName + "\"");
    }

    public static StreamingService getServiceByUrl(String url) throws ExtractionException {
        for (StreamingService service : ServiceList.all()) {
            if (service.getLinkTypeByUrl(url) != StreamingService.LinkType.NONE) {
                return service;
            }
        }
        throw new ExtractionException("No service can handle the url = \"" + url + "\"");
    }

    public static int getIdOfService(String serviceName) {
        try {
            //noinspection ConstantConditions
            return getService(serviceName).getServiceId();
        } catch (ExtractionException ignored) {
            return -1;
        }
    }

    public static String getNameOfService(int id) {
        try {
            //noinspection ConstantConditions
            return getService(id).getServiceInfo().getName();
        } catch (Exception e) {
            System.err.println("Service id not known");
            e.printStackTrace();
            return "<unknown>";
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    public static void setupLocalization(Localization preferredLocalization) {
        setupLocalization(preferredLocalization, null);
    }

    public static void setupLocalization(Localization preferredLocalization, @Nullable ContentCountry preferredContentCountry) {
        NewPipe.preferredLocalization = preferredLocalization;

        if (preferredContentCountry != null) {
            NewPipe.preferredContentCountry = preferredContentCountry;
        } else {
            NewPipe.preferredContentCountry = preferredLocalization.getCountryCode().isEmpty()
                    ? ContentCountry.DEFAULT
                    : new ContentCountry(preferredLocalization.getCountryCode());
        }
    }

    @Nonnull
    public static Localization getPreferredLocalization() {
        return preferredLocalization == null ? Localization.DEFAULT : preferredLocalization;
    }

    public static void setPreferredLocalization(Localization preferredLocalization) {
        NewPipe.preferredLocalization = preferredLocalization;
    }

    @Nonnull
    public static ContentCountry getPreferredContentCountry() {
        return preferredContentCountry == null ? ContentCountry.DEFAULT : preferredContentCountry;
    }

    public static void setPreferredContentCountry(ContentCountry preferredContentCountry) {
        NewPipe.preferredContentCountry = preferredContentCountry;
    }
}
