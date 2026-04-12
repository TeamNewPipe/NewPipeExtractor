package org.schabi.newpipe.extractor.services.darkibox.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.regex.Pattern;

/**
 * Link handler factory for Darkibox streams.
 *
 * <p>Accepted URL patterns:</p>
 * <ul>
 *     <li>{@code https://darkibox.com/FILECODE}</li>
 *     <li>{@code https://darkibox.com/d/FILECODE}</li>
 *     <li>{@code https://darkibox.com/embed-FILECODE.html}</li>
 * </ul>
 */
public final class DarkiboxStreamLinkHandlerFactory extends LinkHandlerFactory {

    private static final DarkiboxStreamLinkHandlerFactory INSTANCE
            = new DarkiboxStreamLinkHandlerFactory();

    private static final String ID_PATTERN
            = "https?://(?:www\\.)?darkibox\\.com/(?:embed-([a-zA-Z0-9]+)\\.html"
            + "|d/([a-zA-Z0-9]+)"
            + "|([a-zA-Z0-9]+))";

    private DarkiboxStreamLinkHandlerFactory() {
    }

    public static DarkiboxStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        final var matcher = Parser.matchOrThrow(
                Pattern.compile(ID_PATTERN), url);
        // Return the first non-null group (embed, /d/, or direct)
        for (int i = 1; i <= 3; i++) {
            final String group = matcher.group(i);
            if (group != null) {
                return group;
            }
        }
        throw new ParsingException("Could not extract file code from URL: " + url);
    }

    @Override
    public String getUrl(final String id)
            throws ParsingException, UnsupportedOperationException {
        return "https://darkibox.com/" + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
            return true;
        } catch (final ParsingException e) {
            return false;
        }
    }
}
