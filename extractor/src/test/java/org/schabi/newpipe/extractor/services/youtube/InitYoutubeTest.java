package org.schabi.newpipe.extractor.services.youtube;

import org.junit.jupiter.api.BeforeAll;
import org.schabi.newpipe.extractor.InitNewPipeTest;

public interface InitYoutubeTest extends InitNewPipeTest {
    @BeforeAll
    @Override
    default void setUp() throws Exception {
        InitNewPipeTest.super.setUp();
        YoutubeTestsUtils.ensureStateless();
    }
}
