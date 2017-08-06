package org.schabi.newpipe.extractor;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.Youtube;
import static org.schabi.newpipe.extractor.NewPipe.getServiceByUrl;

public class NewPipeTest {
    @Test
    public void getAllServicesTest() throws Exception {
        assertEquals(NewPipe.getServices().length, ServiceList.values().length);
    }

    @Test
    public void testAllServicesHaveDifferentId() throws Exception {
        HashSet<Integer> servicesId = new HashSet<>();
        for (StreamingService streamingService : NewPipe.getServices()) {
            String errorMsg = "There are services with the same id = " + streamingService.getServiceId() + " (current service > " + streamingService.getServiceInfo().name + ")";

            assertTrue(errorMsg, servicesId.add(streamingService.getServiceId()));
        }
    }

    @Test
    public void getServiceWithId() throws Exception {
        assertEquals(NewPipe.getService(Youtube.getId()), Youtube.getService());
        assertEquals(NewPipe.getService(SoundCloud.getId()), SoundCloud.getService());

        assertNotEquals(NewPipe.getService(SoundCloud.getId()), Youtube.getService());
    }

    @Test
    public void getServiceWithName() throws Exception {
        assertEquals(NewPipe.getService(Youtube.getServiceInfo().name), Youtube.getService());
        assertEquals(NewPipe.getService(SoundCloud.getServiceInfo().name), SoundCloud.getService());

        assertNotEquals(NewPipe.getService(Youtube.getServiceInfo().name), SoundCloud.getService());
    }

    @Test
    public void getServiceWithUrl() throws Exception {
        assertEquals(getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"), Youtube.getService());
        assertEquals(getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"), Youtube.getService());
        assertEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), Youtube.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/shupemoosic/pegboard-nerds-try-this"), SoundCloud.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/deluxe314/sets/pegboard-nerds"), SoundCloud.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), SoundCloud.getService());

        assertNotEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), Youtube.getService());
        assertNotEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), SoundCloud.getService());
    }

    @Test
    public void getIdWithServiceName() throws Exception {
        assertEquals(NewPipe.getIdOfService(Youtube.getServiceInfo().name), Youtube.getId());
        assertEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().name), SoundCloud.getId());

        assertNotEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().name), Youtube.getId());
    }

    @Test
    public void getServiceNameWithId() throws Exception {
        assertEquals(NewPipe.getNameOfService(Youtube.getId()), Youtube.getServiceInfo().name);
        assertEquals(NewPipe.getNameOfService(SoundCloud.getId()), SoundCloud.getServiceInfo().name);

        assertNotEquals(NewPipe.getNameOfService(Youtube.getId()), SoundCloud.getServiceInfo().name);
    }
}
