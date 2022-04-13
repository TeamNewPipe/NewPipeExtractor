/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * Parser.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.utils;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

/**
 * Avoid using regex !!!
 */
public final class Parser {

    private Parser() {
    }

    public static class RegexException extends ParsingException {
        public RegexException(final String message) {
            super(message);
        }
    }

    public static String matchGroup1(final String pattern, final String input)
            throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    public static String matchGroup1(final Pattern pattern,
                                     final String input) throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    public static String matchGroup(final String pattern,
                                    final String input,
                                    final int group) throws RegexException {
        return matchGroup(Pattern.compile(pattern), input, group);
    }

    public static String matchGroup(@Nonnull final Pattern pat,
                                    final String input,
                                    final int group) throws RegexException {
        final Matcher matcher = pat.matcher(input);
        final boolean foundMatch = matcher.find();
        if (foundMatch) {
            return matcher.group(group);
        } else {
            // only pass input to exception message when it is not too long
            if (input.length() > 1024) {
                throw new RegexException("Failed to find pattern \"" + pat.pattern() + "\"");
            } else {
                throw new RegexException("Failed to find pattern \"" + pat.pattern()
                        + "\" inside of \"" + input + "\"");
            }
        }
    }

    public static boolean isMatch(final String pattern, final String input) {
        final Pattern pat = Pattern.compile(pattern);
        final Matcher mat = pat.matcher(input);
        return mat.find();
    }

    public static boolean isMatch(@Nonnull final Pattern pattern, final String input) {
        final Matcher mat = pattern.matcher(input);
        return mat.find();
    }

    @Nonnull
    public static Map<String, String> compatParseMap(@Nonnull final String input)
            throws UnsupportedEncodingException {
        final Map<String, String> map = new HashMap<>();
        for (final String arg : input.split("&")) {
            final String[] splitArg = arg.split("=");
            if (splitArg.length > 1) {
                map.put(splitArg[0], URLDecoder.decode(splitArg[1], UTF_8));
            } else {
                map.put(splitArg[0], "");
            }
        }
        return map;
    }

    @Nonnull
    public static String[] getLinksFromString(final String txt) throws ParsingException {
        try {
            final List<String> links = new ArrayList<>();
            final LinkExtractor linkExtractor = LinkExtractor.builder()
                    .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW))
                    .build();
            final Iterable<LinkSpan> linkSpans = linkExtractor.extractLinks(txt);
            for (final LinkSpan ls : linkSpans) {
                links.add(txt.substring(ls.getBeginIndex(), ls.getEndIndex()));
            }

            String[] linksarray = new String[links.size()];
            linksarray = links.toArray(linksarray);
            return linksarray;
        } catch (final Exception e) {
            throw new ParsingException("Could not get links from string", e);
        }
    }
}
