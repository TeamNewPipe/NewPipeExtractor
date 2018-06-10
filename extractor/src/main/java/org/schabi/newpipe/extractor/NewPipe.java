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

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;


/**
 * Provides access to streaming services supported by NewPipe.
 */
public class NewPipe {
    private static Downloader downloader = null;
    private static String countryLanguage = "";
    private static String languageCode = "";
    
    private NewPipe() {
    }

    public static void init(Downloader d) {
        downloader = d;
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
    
    /**
     * Sets the language for HTTP requests and which locale variant is preferred.
     *
     * @param locale            Android application context to obtain the language.
     *                       If this parameter is NULL, the device country/language will be used.
     * @param defaultCountry Default country code to be used if not possible
     *                       determine the device country code. This parameter can be NULL.
     */
    public static void setCountryLanguage(@Nullable Locale locale, @Nullable String defaultCountry) {
        if (locale == null) {
            // Use device defaults country/language.
            // NOTE: the use of "Locale.Category.DISPLAY" is only available on API 24 or higher.
            locale = Locale.getDefault();
        }

        languageCode = locale.getLanguage();
        String countryCode = locale.getCountry();

        if (isNullorEmpty(countryCode) && !isNullorEmpty(defaultCountry)) {
            countryCode = defaultCountry;// Just use a default value
        }

        StringBuilder language = new StringBuilder(25);
        language.append(languageCode);

        if (!isNullorEmpty(countryCode)) {
            language.append('-')
                    .append(countryCode)
                    .append(',')
                    .append(languageCode)
                    .append(";q=8.0");
        }

        countryLanguage = language.append(",*;q=0.3").toString();
    }

    /**
     * Sets the language for HTTP requests and which locale variant is preferred.
     *
     * @param defaultCountry Default country code to be used if not possible
     *                       determine the device country code. This parameter can be NULL.
     */
    public static void setCountryLanguage(@Nullable String defaultCountry) {
        setCountryLanguage(null, defaultCountry);
    }
    
    /**
     * Get the language for HTTP requests
     *
     * @return The "accept-language" value
     */
    public static String getCountryLanguage() {
        return countryLanguage;
    }

    public  static String getLanguage() {
        return languageCode;
    }

    private static boolean isNullorEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
