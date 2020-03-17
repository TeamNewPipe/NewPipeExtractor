package org.schabi.newpipe.extractor.utils;

import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * Parser.java is part of NewPipe.
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

/**
 * avoid using regex !!!
 */
public class Parser {

    private Parser() {
    }

    public static class RegexException extends ParsingException {
        public RegexException(String message) {
            super(message);
        }
    }

    public static String matchGroup1(String pattern, String input) throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    public static String matchGroup1(Pattern pattern, String input) throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    public static String matchGroup(String pattern, String input, int group) throws RegexException {
        Pattern pat = Pattern.compile(pattern);
        return matchGroup(pat, input, group);
    }

    public static String matchGroup(Pattern pat, String input, int group) throws RegexException {
        Matcher mat = pat.matcher(input);
        boolean foundMatch = mat.find();
        if (foundMatch) {
            return mat.group(group);
        } else {
            // only pass input to exception message when it is not too long
            if (input.length() > 1024) {
                throw new RegexException("failed to find pattern \"" + pat.pattern());
            } else {
                throw new RegexException("failed to find pattern \"" + pat.pattern() + " inside of " + input + "\"");
            }
        }
    }

    public static boolean isMatch(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);
        return mat.find();
    }

    public static Map<String, String> compatParseMap(final String input) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        for (String arg : input.split("&")) {
            String[] splitArg = arg.split("=");
            if (splitArg.length > 1) {
                map.put(splitArg[0], URLDecoder.decode(splitArg[1], "UTF-8"));
            } else {
                map.put(splitArg[0], "");
            }
        }
        return map;
    }

    public static String[] getLinksFromString(final String txt) throws ParsingException {
        try {
            ArrayList<String> links = new ArrayList<>();
            LinkExtractor linkExtractor = LinkExtractor.builder()
                    .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW))
                    .build();
            Iterable<LinkSpan> linkss = linkExtractor.extractLinks(txt);
            for (LinkSpan ls : linkss) {
                links.add(txt.substring(ls.getBeginIndex(), ls.getEndIndex()));
            }

            String[] linksarray = new String[links.size()];
            linksarray = links.toArray(linksarray);
            return linksarray;
        } catch (Exception e) {
            throw new ParsingException("Could not get links from string", e);
        }
    }
}
