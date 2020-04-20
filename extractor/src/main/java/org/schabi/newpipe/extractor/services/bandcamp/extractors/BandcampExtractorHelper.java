// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BandcampExtractorHelper {

    /**
     * <p>Get JSON behind <code>var $variable = </code> out of web page</p>
     *
     * <p>Originally a part of bandcampDirect.</p>
     *
     * @param html     The HTML where the JSON we're looking for is stored inside a
     *                 variable inside some JavaScript block
     * @param variable Name of the variable
     * @return The JsonObject stored in the variable with this name
     */
    public static JsonObject getJSONFromJavaScriptVariables(String html, String variable) throws JsonParserException, ArrayIndexOutOfBoundsException, ParsingException {

        String[] part = html.split("var " + variable + " = ");

        String firstHalfGone = part[1];

        firstHalfGone = firstHalfGone.replaceAll("\" \\+ \"", "");

        int position = -1;
        int level = 0;
        for (char character : firstHalfGone.toCharArray()) {
            position++;

            switch (character) {
                case '{':
                    level++;
                    continue;
                case '}':
                    level--;
                    if (level == 0) {
                        return JsonParser.object().from(firstHalfGone.substring(0, position + 1)
                                .replaceAll(" {4}//.+", "") // Remove "for the curious" in JSON
                                .replaceAll("// xxx: note - don't internationalize this variable", "") // Remove this comment
                        );
                    }
            }
        }

        throw new ParsingException("Unexpected HTML: JSON never ends");
    }

    /**
     * Translate all these parameters together to the URL of the corresponding album or track
     * using the mobile api
     */
    public static String getStreamUrlFromIds(long bandId, long itemId, String itemType) throws ParsingException {

        try {
            String jsonString = NewPipe.getDownloader().get(
                    "https://bandcamp.com/api/mobile/22/tralbum_details?band_id=" + bandId
                            + "&tralbum_id=" + itemId + "&tralbum_type=" + itemType.substring(0, 1))
                    .responseBody();

            return JsonParser.object().from(jsonString).getString("bandcamp_url").replace("http://", "https://");

        } catch (JsonParserException | ReCaptchaException | IOException e) {
            throw new ParsingException("Ids could not be translated to URL", e);
        }

    }

    /**
     * Concatenate all non-null and non-empty strings together while separating them using
     * the comma parameter
     */
    public static String smartConcatenate(String[] strings, String comma) {
        StringBuilder result = new StringBuilder();

        // Remove empty strings
        ArrayList<String> list = new ArrayList<>(Arrays.asList(strings));
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null || list.get(i).isEmpty() || list.get(i).equals("null")) {
                list.remove(i);
            }
        }

        // Append remaining strings to result
        for (int i = 0; i < list.size(); i++) {
            String string = list.get(i);
            result.append(string);

            if (i != list.size() - 1) {
                // This is not the last iteration yet
                result.append(comma);
            }

        }

        return String.valueOf(result);
    }
}
