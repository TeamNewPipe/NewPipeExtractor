package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link ChannelTabExtractor} for YouTube system playlists using a
 * {@link YoutubePlaylistExtractor} instance.
 *
 * <p>
 * It is currently used to bypass age-restrictions on channels marked as age-restricted by their
 * owner(s).
 * </p>
 */
public class YoutubeChannelTabPlaylistExtractor extends ChannelTabExtractor {

    private final PlaylistExtractor playlistExtractorInstance;
    private boolean playlistExisting;

    /**
     * Construct a {@link YoutubeChannelTabPlaylistExtractor} instance.
     *
     * @param service     a {@link StreamingService} implementation, which must be the YouTube
     *                    one
     * @param linkHandler a {@link ListLinkHandler} which must have a valid channel ID (starting
     *                    with `UC`) and one of the given and supported content filters:
     *                    {@link ChannelTabs#VIDEOS}, {@link ChannelTabs#SHORTS},
     *                    {@link ChannelTabs#LIVESTREAMS}
     * @throws IllegalArgumentException if the given {@link ListLinkHandler} doesn't have the
     * required arguments
     * @throws SystemPlaylistUrlCreationException if the system playlist URL could not be created,
     * which should never happen
     */
    public YoutubeChannelTabPlaylistExtractor(@Nonnull final StreamingService service,
                                              @Nonnull final ListLinkHandler linkHandler)
            throws IllegalArgumentException, SystemPlaylistUrlCreationException {
        super(service, linkHandler);
        final ListLinkHandler playlistLinkHandler = getPlaylistLinkHandler(linkHandler);
        this.playlistExtractorInstance = new YoutubePlaylistExtractor(service, playlistLinkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        try {
            playlistExtractorInstance.onFetchPage(downloader);
            if (!playlistExisting) {
                playlistExisting = true;
            }
        } catch (final ContentNotAvailableException e) {
            // If a channel has no content of the type requested, the corresponding system playlist
            // won't exist, so a ContentNotAvailableException would be thrown
            // Ignore such issues in this case
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage getInitialPage() throws IOException, ExtractionException {
        if (!playlistExisting) {
            return InfoItemsPage.emptyPage();
        }
        return playlistExtractorInstance.getInitialPage();
    }

    @Override
    public InfoItemsPage getPage(final Page page) throws IOException, ExtractionException {
        if (!playlistExisting) {
            return InfoItemsPage.emptyPage();
        }
        return playlistExtractorInstance.getPage(page);
    }

    /**
     * Get a playlist {@link ListLinkHandler} from a channel tab one.
     *
     * <p>
     * This method converts a channel ID without its {@code UC} prefix into a YouTube system
     * playlist, depending on the first content filter provided in the given
     * {@link ListLinkHandler}.
     * </p>
     *
     * <p>
     * The first content filter must be a channel tabs one among the
     * {@link ChannelTabs#VIDEOS videos}, {@link ChannelTabs#SHORTS shorts} and
     * {@link ChannelTabs#LIVESTREAMS} ones, which would be converted respectively into playlists
     * with the ID {@code UULF}, {@code UUSH} and {@code UULV} on which the channel ID without the
     * {@code UC} part is appended.
     * </p>
     *
     * @param originalLinkHandler the original {@link ListLinkHandler} with which a
     * {@link YoutubeChannelTabPlaylistExtractor} instance is being constructed
     *
     * @return a {@link ListLinkHandler} to use for the {@link YoutubePlaylistExtractor} instance
     * needed to extract channel tabs data from a system playlist
     * @throws IllegalArgumentException if the original {@link ListLinkHandler} does not meet the
     * required criteria above
     * @throws SystemPlaylistUrlCreationException if the system playlist URL could not be created,
     * which should never happen
     */
    @Nonnull
    private ListLinkHandler getPlaylistLinkHandler(
            @Nonnull final ListLinkHandler originalLinkHandler)
            throws IllegalArgumentException, SystemPlaylistUrlCreationException {
        final List<String> contentFilters = originalLinkHandler.getContentFilters();
        if (contentFilters.isEmpty()) {
            throw new IllegalArgumentException("A content filter is required");
        }

        final String channelId = originalLinkHandler.getId();
        if (isNullOrEmpty(channelId) || !channelId.startsWith("UC")) {
            throw new IllegalArgumentException("Invalid channel ID");
        }

        final String channelIdWithoutUc = channelId.substring(2);

        final String playlistId;
        switch (contentFilters.get(0)) {
            case ChannelTabs.VIDEOS:
                playlistId = "UULF" + channelIdWithoutUc;
                break;
            case ChannelTabs.SHORTS:
                playlistId = "UUSH" + channelIdWithoutUc;
                break;
            case ChannelTabs.LIVESTREAMS:
                playlistId = "UULV" + channelIdWithoutUc;
                break;
            default:
                throw new IllegalArgumentException(
                        "Only Videos, Shorts and Livestreams tabs can extracted as playlists");
        }

        try {
            final String newUrl = YoutubePlaylistLinkHandlerFactory.getInstance()
                    .getUrl(playlistId);
            return new ListLinkHandler(newUrl, newUrl, playlistId, List.of(), "");
        } catch (final ParsingException e) {
            // This should be not reachable, as the given playlist ID should be valid and
            // YoutubePlaylistLinkHandlerFactory doesn't throw any exception
            throw new SystemPlaylistUrlCreationException(
                    "Could not create a YouTube playlist from a valid playlist ID", e);
        }
    }

    /**
     * Exception thrown when a YouTube system playlist URL could not be created.
     *
     * <p>
     * This exception should be never thrown, as given playlist IDs should be always valid.
     * </p>
     */
    public static final class SystemPlaylistUrlCreationException extends RuntimeException {
        SystemPlaylistUrlCreationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
