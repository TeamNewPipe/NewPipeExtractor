package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class DashMpdParser {

    public static class Result {
        private final List<VideoStream> videoStreams;
        private final List<VideoStream> videoOnlyStreams;
        private final List<AudioStream> audioStreams;


        public Result(List<VideoStream> videoStreams,
                      List<VideoStream> videoOnlyStreams,
                      List<AudioStream> audioStreams) {
            this.videoStreams = videoStreams;
            this.videoOnlyStreams = videoOnlyStreams;
            this.audioStreams = audioStreams;
        }

        public List<VideoStream> getVideoStreams() {
            return videoStreams;
        }

        public List<VideoStream> getVideoOnlyStreams() {
            return videoOnlyStreams;
        }

        public List<AudioStream> getAudioStreams() {
            return audioStreams;
        }
    }

    public static class DashMpdParsingException extends ParsingException {
        public DashMpdParsingException(String message, Exception e) {
            super(message, e);
        }
    }

    public abstract Result getStreams(@Nonnull final String manifestUrl) throws DashMpdParsingException, ReCaptchaException;

    @NonNull
    protected String manualDashFromRepresentation(Document document, Element representation)
            throws TransformerException {

        final Element mpdElement = (Element) document.getElementsByTagName("MPD").item(0);

        // Clone element so we can freely modify it
        final Element adaptationSet = (Element) representation.getParentNode();
        final Element adaptationSetClone = (Element) adaptationSet.cloneNode(true);

        // Remove other representations from the adaptation set
        final NodeList representations = adaptationSetClone.getElementsByTagName("Representation");
        for (int i = representations.getLength() - 1; i >= 0; i--) {
            final Node item = representations.item(i);
            if (!item.isEqualNode(representation)) {
                adaptationSetClone.removeChild(item);
            }
        }

        final Element newMpdRootElement = (Element) mpdElement.cloneNode(false);
        final Element periodElement = newMpdRootElement.getOwnerDocument().createElement("Period");
        periodElement.appendChild(adaptationSetClone);
        newMpdRootElement.appendChild(periodElement);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + nodeToString(newMpdRootElement);
    }

    private String nodeToString(Node node) throws TransformerException {
        final StringWriter result = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(result));
        return result.toString();
    }
}
