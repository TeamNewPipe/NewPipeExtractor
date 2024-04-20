package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.extractCachedUrlIfNeeded;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObjectOrThrow;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isGoogleURL;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;
import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.Description;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public final class YoutubeMetaInfoHelper {

    private YoutubeMetaInfoHelper() {
    }


    @Nonnull
    public static List<MetaInfo> getMetaInfo(@Nonnull final JsonArray contents)
            throws ParsingException {
        final List<MetaInfo> metaInfo = new ArrayList<>();
        for (final Object content : contents) {
            final JsonObject resultObject = (JsonObject) content;
            if (resultObject.has("itemSectionRenderer")) {
                for (final Object sectionContentObject
                        : resultObject.getObject("itemSectionRenderer").getArray("contents")) {

                    final JsonObject sectionContent = (JsonObject) sectionContentObject;
                    if (sectionContent.has("infoPanelContentRenderer")) {
                        metaInfo.add(getInfoPanelContent(sectionContent
                                .getObject("infoPanelContentRenderer")));
                    }
                    if (sectionContent.has("clarificationRenderer")) {
                        metaInfo.add(getClarificationRenderer(sectionContent
                                .getObject("clarificationRenderer")
                        ));
                    }
                    if (sectionContent.has("emergencyOneboxRenderer")) {
                        getEmergencyOneboxRenderer(
                                sectionContent.getObject("emergencyOneboxRenderer"),
                                metaInfo::add
                        );
                    }
                }
            }
        }
        return metaInfo;
    }

    @Nonnull
    private static MetaInfo getInfoPanelContent(@Nonnull final JsonObject infoPanelContentRenderer)
            throws ParsingException {
        final MetaInfo metaInfo = new MetaInfo();
        final StringBuilder sb = new StringBuilder();
        for (final Object paragraph : infoPanelContentRenderer.getArray("paragraphs")) {
            if (sb.length() != 0) {
                sb.append("<br>");
            }
            sb.append(getTextFromObject((JsonObject) paragraph));
        }
        metaInfo.setContent(new Description(sb.toString(), Description.HTML));
        if (infoPanelContentRenderer.has("sourceEndpoint")) {
            final String metaInfoLinkUrl = getUrlFromNavigationEndpoint(
                    infoPanelContentRenderer.getObject("sourceEndpoint"));
            try {
                metaInfo.addUrl(new URL(Objects.requireNonNull(extractCachedUrlIfNeeded(
                        metaInfoLinkUrl))));
            } catch (final NullPointerException | MalformedURLException e) {
                throw new ParsingException("Could not get metadata info URL", e);
            }

            final String metaInfoLinkText = getTextFromObject(
                    infoPanelContentRenderer.getObject("inlineSource"));
            if (isNullOrEmpty(metaInfoLinkText)) {
                throw new ParsingException("Could not get metadata info link text.");
            }
            metaInfo.addUrlText(metaInfoLinkText);
        }

        return metaInfo;
    }

    @Nonnull
    private static MetaInfo getClarificationRenderer(
            @Nonnull final JsonObject clarificationRenderer) throws ParsingException {
        final MetaInfo metaInfo = new MetaInfo();

        final String title = getTextFromObject(clarificationRenderer
                .getObject("contentTitle"));
        final String text = getTextFromObject(clarificationRenderer
                .getObject("text"));
        if (title == null || text == null) {
            throw new ParsingException("Could not extract clarification renderer content");
        }
        metaInfo.setTitle(title);
        metaInfo.setContent(new Description(text, Description.PLAIN_TEXT));

        if (clarificationRenderer.has("actionButton")) {
            final JsonObject actionButton = clarificationRenderer.getObject("actionButton")
                    .getObject("buttonRenderer");
            try {
                final String url = getUrlFromNavigationEndpoint(actionButton
                        .getObject("command"));
                metaInfo.addUrl(new URL(Objects.requireNonNull(extractCachedUrlIfNeeded(url))));
            } catch (final NullPointerException | MalformedURLException e) {
                throw new ParsingException("Could not get metadata info URL", e);
            }

            final String metaInfoLinkText = getTextFromObject(
                    actionButton.getObject("text"));
            if (isNullOrEmpty(metaInfoLinkText)) {
                throw new ParsingException("Could not get metadata info link text.");
            }
            metaInfo.addUrlText(metaInfoLinkText);
        }

        if (clarificationRenderer.has("secondaryEndpoint") && clarificationRenderer
                .has("secondarySource")) {
            final String url = getUrlFromNavigationEndpoint(clarificationRenderer
                    .getObject("secondaryEndpoint"));
            // Ignore Google URLs, because those point to a Google search about "Covid-19"
            if (url != null && !isGoogleURL(url)) {
                try {
                    metaInfo.addUrl(new URL(url));
                    final String description = getTextFromObject(clarificationRenderer
                            .getObject("secondarySource"));
                    metaInfo.addUrlText(description == null ? url : description);
                } catch (final MalformedURLException e) {
                    throw new ParsingException("Could not get metadata info secondary URL", e);
                }
            }
        }

        return metaInfo;
    }

    private static void getEmergencyOneboxRenderer(
            @Nonnull final JsonObject emergencyOneboxRenderer,
            final Consumer<MetaInfo> addMetaInfo
    ) throws ParsingException {
        final List<JsonObject> supportRenderers = emergencyOneboxRenderer.values()
                .stream()
                .filter(o -> o instanceof JsonObject
                        && ((JsonObject) o).has("singleActionEmergencySupportRenderer"))
                .map(o -> ((JsonObject) o).getObject("singleActionEmergencySupportRenderer"))
                .collect(Collectors.toList());

        if (supportRenderers.isEmpty()) {
            throw new ParsingException("Could not extract any meta info from emergency renderer");
        }

        for (final JsonObject r : supportRenderers) {
            final MetaInfo metaInfo = new MetaInfo();

            // usually an encouragement like "We are with you"
            final String title = getTextFromObjectOrThrow(r.getObject("title"), "title");

            // usually a phone number
            final String action; // this variable is expected to start with "\n"
            if (r.has("actionText")) {
                action = "\n" + getTextFromObjectOrThrow(r.getObject("actionText"), "action");
            } else if (r.has("contacts")) {
                final JsonArray contacts = r.getArray("contacts");
                final StringBuilder stringBuilder = new StringBuilder();
                // Loop over contacts item from the first contact to the last one
                for (int i = 0; i < contacts.size(); i++) {
                    stringBuilder.append("\n");
                    stringBuilder.append(getTextFromObjectOrThrow(contacts.getObject(i)
                            .getObject("actionText"), "contacts.actionText"));
                }
                action = stringBuilder.toString();
            } else {
                action = "";
            }

            // usually details about the phone number
            final String details = getTextFromObjectOrThrow(r.getObject("detailsText"), "details");

            // usually the name of an association
            final String urlText = getTextFromObjectOrThrow(r.getObject("navigationText"),
                    "urlText");

            metaInfo.setTitle(title);
            metaInfo.setContent(new Description(details + action, Description.PLAIN_TEXT));
            metaInfo.addUrlText(urlText);

            // usually the webpage of the association
            final String url = getUrlFromNavigationEndpoint(r.getObject("navigationEndpoint"));
            if (url == null) {
                throw new ParsingException("Could not extract emergency renderer url");
            }

            try {
                metaInfo.addUrl(new URL(replaceHttpWithHttps(url)));
            } catch (final MalformedURLException e) {
                throw new ParsingException("Could not parse emergency renderer url", e);
            }

            addMetaInfo.accept(metaInfo);
        }
    }
}
