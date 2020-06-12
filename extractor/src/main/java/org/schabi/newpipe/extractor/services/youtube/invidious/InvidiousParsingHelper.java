package org.schabi.newpipe.extractor.services.youtube.invidious;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousParsingHelper.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.util.Calendar;
import java.util.Date;

public class InvidiousParsingHelper {

    /**
     * Used to check HTTP code and handle Json parsing.
     *
     * @param response the response got from the service
     * @param apiUrl   the url used to call the service
     * @return a valid JsonObject
     * @throws ExtractionException if the HTTP code indicate an error or the json parsing went wrong.
     */
    public static JsonObject getValidJsonObjectFromResponse(final Response response, final String apiUrl) throws ExtractionException {
        if (response.responseCode() >= 400) {
            throw new ExtractionException("Could not get page " + apiUrl + " (" + response.responseCode() + " : " + response.responseMessage());
        }

        try {
            return JsonParser.object().from(response.responseBody());
        } catch (JsonParserException e) {
            throw new ExtractionException("Could not parse json", e);
        }
    }

    public static DateWrapper getUploadDateFromEpochTime(final long epochTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(epochTime * 1000)); // * 1000 because it's second-based, not millisecond based
        return new DateWrapper(calendar);
    }
}
