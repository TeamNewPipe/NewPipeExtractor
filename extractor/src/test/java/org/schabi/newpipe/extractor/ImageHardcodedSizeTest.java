/*
 * Copyright 2026 Mauricio Colli <mauriciocolli@outlook.com>
 * ImageHardcodedSizeTest.java is part of NewPipe Extractor
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package org.schabi.newpipe.extractor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures that hard-coded image sizes in the extractor source are known,
 * documented and stay correct.
 *
 * <p>
 * Some extractors hard-code image dimensions when a service guarantees them
 * (e.g. YouTube EUVM comment avatars are {@code 88x88}, the Bandcamp radio
 * button is {@code 512x512}). When a service changes such a size, the
 * extractor would silently report wrong values — that's what happened in
 * <a href="https://github.com/TeamNewPipe/NewPipeExtractor/pull/1525">#1525</a>.
 * </p>
 *
 * <p>
 * This test scans the source code for every {@code new Image(url, w, h, ...)}
 * call with hard-coded numeric dimensions and asserts that each occurrence is
 * explicitly registered in {@link #WHITELISTED_SIZES}. Adding a new hard-coded
 * size therefore requires documenting it here and (when possible) verifying it,
 * instead of it silently rotting.
 * </p>
 *
 * @see <a href="https://github.com/TeamNewPipe/NewPipeExtractor/issues/1526">
 *     issue #1526</a>
 */
public class ImageHardcodedSizeTest {

    private static final Pattern IMAGE_CONSTRUCTOR = Pattern.compile("new Image\\(");

    /**
     * The hard-coded sizes which are currently known and accepted.
     *
     * <p>
     * Keys are file name suffixes (relative to {@code extractor/src/main/java}),
     * values map the size ({@code <width>x<height>}) to an optional URL fragment
     * which the corresponding image URL must contain (used to cross-check that
     * the hard-coded value matches the URL a service uses for that size).
     * </p>
     */
    private static final Map<String, Map<String, String>> WHITELISTED_SIZES = Map.of(
            // Bandcamp's radio button PNG literally encodes its size in its file name
            "org/schabi/newpipe/extractor/services/bandcamp/extractors/BandcampRadioStreamExtractor.java",
            Map.of("512x512", "-512.png"),

            // All YouTube EUVM comment avatars are 88x88 (see #1525); verified
            // live by YoutubeAvatarHardcodedSizeTest below
            "org/schabi/newpipe/extractor/services/youtube/extractors/YoutubeCommentsEUVMInfoItemExtractor.java",
            Map.of("88x88", "")
    );

    /** One hard-coded {@code new Image(...)} size found in the source code. */
    private static final class HardcodedSize {
        final String filePath;
        final String size;
        final String urlArgument;

        HardcodedSize(final String filePath, final String size, final String urlArgument) {
            this.filePath = filePath;
            this.size = size;
            this.urlArgument = urlArgument;
        }
    }

    @Test
    public void everyHardcodedImageSizeMustBeWhitelisted() throws IOException {
        final List<HardcodedSize> hardcodedSizes = findHardcodedSizes();
        assertTrue(!hardcodedSizes.isEmpty(),
                "Scanner found no hardcoded image sizes; the scanner is probably broken");

        final StringBuilder unknown = new StringBuilder();
        for (final HardcodedSize hardcodedSize : hardcodedSizes) {
            final Map<String, String> allowed =
                    WHITELISTED_SIZES.get(hardcodedSize.filePath);
            final String allowedUrlFragment =
                    allowed == null ? null : allowed.get(hardcodedSize.size);

            if (allowedUrlFragment == null) {
                unknown.append(hardcodedSize.filePath)
                        .append(": ").append(hardcodedSize.size)
                        .append("\n");
            } else if (!allowedUrlFragment.isEmpty()
                    && !hardcodedSize.urlArgument.contains(allowedUrlFragment)) {
                // Cross-check: the size is supposed to be encoded in the image URL
                unknown.append(hardcodedSize.filePath)
                        .append(": hardcoded size ").append(hardcodedSize.size)
                        .append(" does not match URL fragment \"")
                        .append(allowedUrlFragment)
                        .append("\" (URL: ").append(hardcodedSize.urlArgument)
                        .append(")\n");
            }
        }

        assertEquals("", unknown.toString(),
                "Found hard-coded image sizes which are not whitelisted (and possibly not"
                        + " verified). If a service guarantees these sizes, add them to"
                        + " WHITELISTED_SIZES with a justification and, when possible, a"
                        + " URL fragment that must contain the size; otherwise use"
                        + " Image.WIDTH_UNKNOWN / Image.HEIGHT_UNKNOWN.");
    }

    private static List<HardcodedSize> findHardcodedSizes() throws IOException {
        final Path sourceRoot = resolveMainSourceDirectory();
        final List<HardcodedSize> result = new ArrayList<>();
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        final String relativePath = sourceRoot.relativize(path)
                                .toString()
                                .replace('\\', '/');
                        final String content;
                        try {
                            content = Files.readString(path, StandardCharsets.UTF_8);
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        findHardcodedSizesInContent(relativePath, content, result);
                    });
        }
        return result;
    }

    private static Path resolveMainSourceDirectory() {
        // Gradle runs tests from the module directory, but support running from
        // the repository root too (e.g. IDEs with a different working directory)
        for (final String candidate : new String[]{
                "src/main/java", "extractor/src/main/java"}) {
            final Path path = Paths.get(candidate).toAbsolutePath();
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        throw new IllegalStateException("Could not locate extractor source directory");
    }

    /**
     * Finds {@code new Image(url, width, height, resolutionLevel)} calls whose
     * width and height are hard-coded numeric literals.
     */
    private static void findHardcodedSizesInContent(
            final String filePath,
            final String content,
            final List<HardcodedSize> result) {
        final Matcher matcher = IMAGE_CONSTRUCTOR.matcher(content);
        while (matcher.find()) {
            final String args = extractArguments(content, matcher.end() - 1);
            if (args == null) {
                continue;
            }
            final List<String> arguments = splitTopLevelArguments(args);
            if (arguments.size() == 4
                    && arguments.get(1).matches("\\d+")
                    && arguments.get(2).matches("\\d+")) {
                final String size = String.format(Locale.ENGLISH, "%sx%s",
                        Integer.parseInt(arguments.get(1)),
                        Integer.parseInt(arguments.get(2)));
                result.add(new HardcodedSize(filePath, size, arguments.get(0)));
            }
        }
    }

    /** Returns the balanced-parentheses argument string starting at {@code openParenIndex}. */
    private static String extractArguments(final String content, final int openParenIndex) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = openParenIndex; i < content.length(); i++) {
            final char c = content.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (inString && c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
            } else if (!inString) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        return content.substring(openParenIndex + 1, i);
                    }
                }
            }
        }
        return null; // unbalanced (e.g. commented-out code) — ignore
    }

    /** Splits a constructor argument string on top-level commas only. */
    private static List<String> splitTopLevelArguments(final String args) {
        final List<String> arguments = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < args.length(); i++) {
            final char c = args.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (inString && c == '\\') {
                current.append(c);
                escaped = true;
            } else if (c == '"') {
                current.append(c);
                inString = !inString;
            } else if (inString) {
                current.append(c);
            } else if (c == '(' || c == '[') {
                depth++;
                current.append(c);
            } else if (c == ')' || c == ']') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                arguments.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            arguments.add(current.toString().trim());
        }
        return arguments;
    }
}
