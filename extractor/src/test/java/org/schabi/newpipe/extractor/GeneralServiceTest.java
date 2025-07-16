package org.schabi.newpipe.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.NewPipe.getServiceByUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

public class GeneralServiceTest {
    @Test
    public void getAllServicesTest() {
        assertEquals(NewPipe.getServices().size(), ServiceList.all().size());
    }

    @Test
    public void testAllServicesHaveDifferentId() {
        final HashSet<Integer> servicesId = new HashSet<>();
        for (final StreamingService streamingService : NewPipe.getServices()) {
            final String errorMsg =
                    "There are services with the same id = " + streamingService.getServiceId()
                            + " (current service > " + streamingService.getServiceInfo().getName() + ")";

            assertTrue(servicesId.add(streamingService.getServiceId()), errorMsg);
        }
    }

    @Test
    public void getServiceWithId() throws Exception {
        assertEquals(YouTube, NewPipe.getService(YouTube.getServiceId()));
    }

    @Test
    public void getServiceWithUrl() throws Exception {
        assertEquals(YouTube, getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"));
        assertEquals(YouTube, getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"));
        assertEquals(YouTube, getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"));
        assertEquals(YouTube, getServiceByUrl("https://www.google.it/url?sa=t&rct=j&q=&esrc=s&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DHu80uDzh8RY&source=video"));

        assertEquals(SoundCloud, getServiceByUrl("https://soundcloud.com/pegboardnerds"));
        assertEquals(SoundCloud, getServiceByUrl("https://www.google.com/url?sa=t&url=https%3A%2F%2Fsoundcloud.com%2Fciaoproduction&rct=j&q=&esrc=s&source=web&cd="));
    }
}
