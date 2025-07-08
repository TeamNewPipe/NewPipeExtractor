/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
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

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

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

    @Nonnull
    public static Matcher matchOrThrow(@Nonnull final Pattern pattern,
                                              final String input) throws RegexException {
        final Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher;
        } else {
            String errorMessage = "Failed to find pattern \"" + pattern.pattern() + "\"";
            if (input.length() <= 1024) {
                errorMessage += " inside of \"" + input + "\"";
            }
            throw new RegexException(errorMessage);
        }
    }

    /**
     * Matches group 1 of the given pattern against the input
     * and returns the matched group
     *
     * @param pattern The regex pattern to match.
     * @param input   The input string to match against.
     * @return The matching group as a string.
     * @throws RegexException If the pattern does not match the input or if the group is not found.
     */
    @Nonnull
    public static String matchGroup1(final String pattern, final String input)
            throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    /**
     * Matches group 1 of the given pattern against the input
     * and returns the matched group
     *
     * @param pattern The regex pattern to match.
     * @param input   The input string to match against.
     * @return The matching group as a string.
     * @throws RegexException If the pattern does not match the input or if the group is not found.
     */
    @Nonnull
    public static String matchGroup1(final Pattern pattern, final String input)
            throws RegexException {
        return matchGroup(pattern, input, 1);
    }

    /**
     * Matches the specified group of the given pattern against the input,
     * and returns the matched group
     *
     * @param pattern The regex pattern to match.
     * @param input   The input string to match against.
     * @param group   The group number to retrieve (1-based index).
     * @return The matching group as a string.
     * @throws RegexException If the pattern does not match the input or if the group is not found.
     */
    @Nonnull
    public static String matchGroup(final String pattern, final String input, final int group)
            throws RegexException {
        return matchGroup(Pattern.compile(pattern), input, group);
    }

    /**
     * Matches the specified group of the given pattern against the input,
     * and returns the matched group
     *
     * @param pattern The regex pattern to match.
     * @param input   The input string to match against.
     * @param group   The group number to retrieve (1-based index).
     * @return The matching group as a string.
     * @throws RegexException If the pattern does not match the input or if the group is not found.
     */
    @Nonnull
    public static String matchGroup(@Nonnull final Pattern pattern,
                                    final String input,
                                    final int group)
            throws RegexException {
        return matchOrThrow(pattern, input).group(group);
    }

    /**
     * Matches multiple patterns against the input string and
     * returns the first successful matcher
     *
     * @param patterns The array of regex patterns to match.
     * @param input    The input string to match against.
     * @return A {@code Matcher} for the first successful match.
     * @throws RegexException If no patterns match the input or if {@code patterns} is empty.
     */
    public static String matchGroup1MultiplePatterns(final Pattern[] patterns, final String input)
            throws RegexException {
        return matchMultiplePatterns(patterns, input).group(1);
    }

    /**
     * Matches multiple patterns against the input string and
     * returns the first successful matcher
     *
     * @param patterns The array of regex patterns to match.
     * @param input    The input string to match against.
     * @return A {@code Matcher} for the first successful match.
     * @throws RegexException If no patterns match the input or if {@code patterns} is empty.
     */
    public static Matcher matchMultiplePatterns(final Pattern[] patterns, final String input)
            throws RegexException {
        RegexException exception = null;
        for (final var pattern : patterns) {
            final var matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher;
            } else if (exception == null) {
                exception = new RegexException("Failed to find pattern \"" + pattern.pattern()
                    + "\""
                    // only pass input to exception message when it is not too long
                    + (input.length() <= 1000
                    ? "inside of \"" + input + "\""
                    : "")
                );
            }
        }

        throw exception != null
            ? exception
            : new RegexException("Empty patterns array passed to matchMultiplePatterns");
    }

    public static boolean isMatch(final String pattern, final String input) {
        return isMatch(Pattern.compile(pattern), input);
    }

    public static boolean isMatch(@Nonnull final Pattern pattern, final String input) {
        return pattern.matcher(input).find();
    }

    @Nonnull
    public static Map<String, String> compatParseMap(@Nonnull final String input) {
        return Arrays.stream(input.split("&"))
                .map(arg -> arg.split("="))
                .filter(splitArg -> splitArg.length > 1)
                .collect(Collectors.toMap(splitArg -> splitArg[0],
                        splitArg -> Utils.decodeUrlUtf8(splitArg[1]),
                        (existing, replacement) -> replacement));
    }
}
