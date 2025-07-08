package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.testcases.DefaultStreamExtractorTestCase;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Test for {@link StreamExtractor}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ParameterisedDefaultStreamExtractorTest<TTestCase extends DefaultStreamExtractorTestCase> extends DefaultStreamExtractorTest {
    protected TTestCase testCase;
    protected StreamExtractor extractor;

    protected ParameterisedDefaultStreamExtractorTest(TTestCase testCase)
    {
        this.testCase = testCase;
    }

    @BeforeAll
    public void setUp() throws Exception {
        if (extractor != null) {
            throw new IllegalStateException("extractor already initialized before BeforeAll");
        }
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = testCase.service().getStreamExtractor(testCase.url());
        extractor.fetchPage();
    }

    ///
    /// DefaultExtractorTest overrides
    /// 
    
    @Override public StreamExtractor extractor() throws Exception { return extractor; }

    @Override public StreamingService expectedService() throws Exception { return testCase.service(); }
    @Override public String expectedName() throws Exception { return testCase.name(); }
    @Override public String expectedId() throws Exception { return testCase.id(); }
    @Override public String expectedUrlContains() throws Exception { return testCase.urlContains(); }
    @Override public String expectedOriginalUrlContains() throws Exception { return testCase.originalUrlContains(); }
    
    ///
    /// DefaultStreamExtractorTest overrides
    /// 
    @Override public StreamType expectedStreamType() { return testCase.streamType(); }
    @Override public String expectedUploaderName() { return testCase.uploaderName(); }
    @Override public String expectedUploaderUrl() { return testCase.uploaderUrl(); }
    @Override public boolean expectedUploaderVerified() { return testCase.uploaderVerified(); }
    @Override public long expectedUploaderSubscriberCountAtLeast() { return testCase.uploaderSubscriberCountAtLeast(); }
    @Override public String expectedSubChannelName() { return testCase.subChannelName(); }
    @Override public String expectedSubChannelUrl() { return testCase.subChannelUrl(); }
    @Override public boolean expectedDescriptionIsEmpty() { return testCase.descriptionIsEmpty(); }
    @Override public List<String> expectedDescriptionContains() { return testCase.descriptionContains(); }
    @Override public long expectedLength() { return testCase.length(); }
    @Override public long expectedTimestamp() { return testCase.timestamp(); }
    @Override public long expectedViewCountAtLeast() { return testCase.viewCountAtLeast(); }
    @Override @Nullable public String expectedUploadDate() { return testCase.uploadDate(); }
    @Override @Nullable public String expectedTextualUploadDate() { return testCase.textualUploadDate(); }
    @Override public long expectedLikeCountAtLeast() { return testCase.likeCountAtLeast(); }
    @Override public long expectedDislikeCountAtLeast() { return testCase.dislikeCountAtLeast(); }
    @Override public boolean expectedHasRelatedItems() { return testCase.hasRelatedItems(); }
    @Override public int expectedAgeLimit() { return testCase.ageLimit(); }
    @Override @Nullable public String expectedErrorMessage() { return testCase.errorMessage(); }
    @Override public boolean expectedHasVideoStreams() { return testCase.hasVideoStreams(); }
    @Override public boolean expectedHasAudioStreams() { return testCase.hasAudioStreams(); }
    @Override public boolean expectedHasSubtitles() { return testCase.hasSubtitles(); }
    @Override @Nullable public String expectedDashMpdUrlContains() { return testCase.dashMpdUrlContains(); }
    @Override public boolean expectedHasFrames() { return testCase.hasFrames(); }
    @Override public String expectedHost() { return testCase.host(); }
    @Override public StreamExtractor.Privacy expectedPrivacy() { return testCase.privacy(); }
    @Override public String expectedCategory() { return testCase.category(); }
    @Override public String expectedLicence() { return testCase.licence(); }
    @Override public Locale expectedLanguageInfo() { return testCase.languageInfo(); }
    @Override public List<String> expectedTags() { return testCase.tags(); }
    @Override public String expectedSupportInfo() { return testCase.supportInfo(); }
    @Override public int expectedStreamSegmentsCount() { return testCase.streamSegmentsCount(); }
    @Override public List<MetaInfo> expectedMetaInfo() { return testCase.metaInfo(); }
}
