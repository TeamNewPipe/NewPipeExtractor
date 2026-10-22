/*
 * SPDX-FileCopyrightText: 2026 NewPipe Extractor contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.Image.HEIGHT_UNKNOWN;

/**
 * Verifies that the hard-coded comment avatar size in
 * {@link org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsEUVMInfoItemExtractor}
 * ({@code 88x88}) still matches what YouTube actually serves.
 *
 * <p>
 * YouTube avatar URLs typically encode the requested size as a {@code =s<N>}
 * parameter (e.g. {@code https://yt3.ggpht.com/...=s88-c-k-c0x00ffffff-no-nd-rj}).
 * When the extractor reports {@code 88x88} but YouTube switched to serving a
 * different size, the metadata would silently be wrong — so this test checks
 * that the URL size matches the reported metadata.
 * </p>
 *
 * @see <a href="https://github.com/TeamNewPipe/NewPipeExtractor/issues/1526">
 *     issue #1526</a>
 */
public class YoutubeCommentsAvatarSizeTest implements InitYoutubeTest {

    private static final String VIDEO_URL = "https://www.youtube.com/watch?v=D00Au7k3i6o";
    /**
     * The size hard-coded in YoutubeCommentsEUVMInfoItemExtractor. Keep in sync!
     */
    private static final int HARDCODED_AVATAR_SIZE = 88;

    private static final Pattern SIZE_URL_PARAMETER = Pattern.compile("=s(\\d+)");

    @Test
    public void avatarUrlSizeMatchesHardcodedMetadata()
            throws IOException, ExtractionException {
        final YoutubeCommentsExtractor extractor =
                (YoutubeCommentsExtractor) YouTube.getCommentsExtractor(VIDEO_URL);
        extractor.fetchPage();
        final List<CommentsInfoItem> comments = extractor.getInitialPage().getItems();

        int checkedAvatars = 0;
        int checkedHardcodedAvatars = 0;

        for (final CommentsInfoItem comment : comments) {
            for (final Image avatar : comment.getUploaderAvatars()) {
                final Matcher matcher = SIZE_URL_PARAMETER.matcher(avatar.getUrl());
                if (!matcher.find()) {
                    // URL does not encode a size — nothing to verify
                    continue;
                }
                final int urlSize = Integer.parseInt(matcher.group(1));
                if (avatar.getHeight() != HEIGHT_UNKNOWN) {
                    assertEquals(urlSize, avatar.getHeight(),
                            "Avatar URL size (=s" + urlSize + ") does not match the"
                                    + " reported metadata height (" + avatar.getHeight()
                                    + "). If YouTube changed the served avatar size, update"
                                    + " the hard-coded value in"
                                    + " YoutubeCommentsEUVMInfoItemExtractor accordingly.");
                }
                checkedAvatars++;
                if (avatar.getHeight() == HARDCODED_AVATAR_SIZE) {
                    checkedHardcodedAvatars++;
                }
            }
        }

        assertTrue(checkedHardcodedAvatars > 0,
                "No comments with the hard-coded 88x88 avatar size were found");
    }
}
