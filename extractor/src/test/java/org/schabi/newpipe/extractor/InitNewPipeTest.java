package org.schabi.newpipe.extractor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.schabi.newpipe.downloader.DownloaderFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface InitNewPipeTest {

    @BeforeAll
    default void setUp() throws Exception {
        initNewPipe(this.getClass());
    }

    static void initNewPipe(final Class<?> clazz) {
        NewPipe.init(DownloaderFactory.getDownloader(clazz));
    }

    static void initNewPipe(final Class<?> clazz, final String specificUseCase) {
        NewPipe.init(DownloaderFactory.getDownloader(clazz, specificUseCase));
    }

    /**
     * Ensures that NewPipe is not initialized.
     * This can be used to ensure that a class works standalone and does e.g. no network requests.
     */
    static void initEmpty() {
        NewPipe.init(null, null, null);
    }
}
