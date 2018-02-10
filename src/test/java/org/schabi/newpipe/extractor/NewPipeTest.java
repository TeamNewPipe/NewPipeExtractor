package org.schabi.newpipe.extractor;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.NewPipe.getServiceByUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class NewPipeTest {
    @Test
    public void getAllServicesTest() throws Exception {
        assertEquals(NewPipe.getServices().size(), ServiceList.all().size());
    }

    @Test
    public void testAllServicesHaveDifferentId() throws Exception {
        HashSet<Integer> servicesId = new HashSet<>();
        for (StreamingService streamingService : NewPipe.getServices()) {
            String errorMsg = "There are services with the same id = " + streamingService.getServiceId() + " (current service > " + streamingService.getServiceInfo().getName() + ")";

            assertTrue(errorMsg, servicesId.add(streamingService.getServiceId()));
        }
    }

    @Test
    public void getServiceWithId() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getServiceId()), YouTube);
        assertEquals(NewPipe.getService(SoundCloud.getServiceId()), SoundCloud);

        assertNotEquals(NewPipe.getService(SoundCloud.getServiceId()), YouTube);
    }

    @Test
    public void getServiceWithName() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getServiceInfo().getName()), YouTube);
        assertEquals(NewPipe.getService(SoundCloud.getServiceInfo().getName()), SoundCloud);

        assertNotEquals(NewPipe.getService(YouTube.getServiceInfo().getName()), SoundCloud);
    }

    @Test
    public void getServiceWithUrl() throws Exception {
        assertEquals(getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"), YouTube);
        assertEquals(getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"), YouTube);
        assertEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), YouTube);
        assertEquals(getServiceByUrl("https://soundcloud.com/shupemoosic/pegboard-nerds-try-this"), SoundCloud);
        assertEquals(getServiceByUrl("https://soundcloud.com/deluxe314/sets/pegboard-nerds"), SoundCloud);
        assertEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), SoundCloud);

        assertNotEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), YouTube);
        assertNotEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), SoundCloud);
    }

    @Test
    public void getIdWithServiceName() throws Exception {
        assertEquals(NewPipe.getIdOfService(YouTube.getServiceInfo().getName()), YouTube.getServiceId());
        assertEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().getName()), SoundCloud.getServiceId());

        assertNotEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().getName()), YouTube.getServiceId());
    }

    @Test
    public void getServiceNameWithId() throws Exception {
        assertEquals(NewPipe.getNameOfService(YouTube.getServiceId()), YouTube.getServiceInfo().getName());
        assertEquals(NewPipe.getNameOfService(SoundCloud.getServiceId()), SoundCloud.getServiceInfo().getName());

        assertNotEquals(NewPipe.getNameOfService(YouTube.getServiceId()), SoundCloud.getServiceInfo().getName());
    }
}
