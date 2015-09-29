package com.mopub.mobileads;

import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.VastUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import java.util.List;

import static com.mopub.mobileads.test.support.VastUtils.createNode;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class VastLinearXmlManagerTest {

    private VastLinearXmlManager subject;
    private Node linearNode;

    @Before
    public void setup() throws Exception {
        String linearXml = "<Linear skipoffset=\"25%\">" +
                "<Duration>00:00:58</Duration>" +
                "<TrackingEvents>" +
                "    <Tracking event=\"creativeView\">" +
                "        <![CDATA[" +
                "        http://creativeView/one" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"creativeView\">" +
                "        <![CDATA[" +
                "        http://creativeView/two" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"start\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"progress\" offset=\"13%\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"progress\" offset=\"01:01:10.300\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                // Invalid tracking due to ambiguous offset.
                "    <Tracking event=\"progress\" offset=\"01:01\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                // Invalid tracking due to a too-high percentage offset.
                "    <Tracking event=\"progress\" offset=\"113%\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                // Invalid tracking due to a negative percentage offset.
                "    <Tracking event=\"progress\" offset=\"-113%\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                // Invalid tracking due to a non-number offset
                "    <Tracking event=\"progress\" offset=\"ten seconds\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"midpoint\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=18;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"midpoint\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.3;sz=1x1;ord=2922389?" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"firstQuartile\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=26;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"firstQuartile\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.2;sz=1x1;ord=2922389?" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"thirdQuartile\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=27;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"thirdQuartile\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.4;sz=1x1;ord=2922389?" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"complete\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=13;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"complete\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.5;sz=1x1;ord=2922389?" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"close\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/close?q=ignatius" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"close\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/close?q=j3" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"closeLinear\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/closeLinear" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"skip\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/skip?q=ignatius" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"skip\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/skip?q=j3" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"mute\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=16;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"pause\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/pause?num=1" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"pause\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/pause?num=2" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"resume\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/resume?num=1" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"resume\">" +
                "        <![CDATA[" +
                "        http://www.mopub.com/resume?num=2" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"fullscreen\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=19;ecn1=1;etm1=0;" +
                "        ]]>" +
                "    </Tracking>" +
                "    <Tracking event=\"fullscreen\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.6;sz=1x1;ord=2922389?" +
                "        ]]>" +
                "    </Tracking>" +
                "</TrackingEvents>" +
                "<AdParameters/>" +
                "<VideoClicks>" +
                "    <ClickThrough>" +
                "        <![CDATA[ http://www.google.com/support/richmedia ]]>" +
                "    </ClickThrough>" +
                "    <ClickTracking id=\"DART\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/click%3Bh%3Dv8/3e1b/3/0/%2a/z%3B223626102%3B0-0%3B0%3B47414672%3B255-0/0%3B30477563/30495440/1%3B%3B%7Eaopt%3D0/0/ff/0%3B%7Esscs%3D%3fhttp://s0.2mdn.net/dot.gif" +
                "        ]]>" +
                "    </ClickTracking>" +
                "    <ClickTracking id=\"ThirdParty\">" +
                "        <![CDATA[" +
                "        http://ad.doubleclick.net/clk;212442087;33815766;i?http://www.google.com/support/richmedia" +
                "        ]]>" +
                "    </ClickTracking>" +
                "</VideoClicks>" +
                "<MediaFiles>" +
                "    <MediaFile id=\"1\" delivery=\"progressive\" type=\"video/quicktime\" bitrate=\"457\"" +
                "               width=\"300\" height=\"225\">" +
                "        <![CDATA[" +
                "        http://s3.amazonaws.com/uploads.hipchat.com/10627/429509/t8hqeqf98nvtir7/big_buck_bunny.mp4" +
                "        ]]>" +
                "    </MediaFile>" +
                "    <MediaFile id=\"2\" delivery=\"progressive\" type=\"video/quicktime\" bitrate=\"457\"" +
                "               width=\"300\" height=\"225\">" +
                "        <![CDATA[" +
                "        http://s3.amazonaws.com/uploads.hipchat.com/10627/429509/t8hqeqf98nvtir7/big_buck_bunny_2.mp4" +
                "        ]]>" +
                "    </MediaFile>" +
                "</MediaFiles>" +
                "<Icons>" +
                "    <Icon program=\"program\" width=\"123\" height=\"234\" xPosition=\"789\" " +
                "    yPosition=\"101\" apiFramework=\"apiFramework\" offset=\"01:02:03\" " +
                "    duration=\"01:02:03.456\">" +
                "        <StaticResource creativeType=\"ImAge/JpEg\">" +
                "           <![CDATA[staticResource1]]>" +
                "        </StaticResource>" +
                "    </Icon>" +
                "    <Icon program=\"program\" width=\"123\" height=\"234\" xPosition=\"789\" " +
                "    yPosition=\"101\" apiFramework=\"apiFramework\" offset=\"01:02:03\" " +
                "    duration=\"01:02:03.456\">" +
                "        <StaticResource creativeType=\"ImAge/JpEg\">" +
                "           <![CDATA[staticResource2]]>" +
                "        </StaticResource>" +
                "    </Icon>" +
                "</Icons>" +
                "</Linear>";

        linearNode = createNode(linearXml);
    }

    @Test
    public void getFractionalTrackers_shouldReturnCorrectValues() {
        subject = new VastLinearXmlManager(linearNode);
        List<VastFractionalProgressTracker> trackers = subject.getFractionalProgressTrackers();

        assertThat(trackers.size()).isEqualTo(7);

        VastFractionalProgressTracker tracker0 = trackers.get(0);
        assertThat(tracker0.trackingFraction()).isEqualTo(0.13f);
        assertThat(tracker0.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;");

        VastFractionalProgressTracker tracker1 = trackers.get(1);
        assertThat(tracker1.trackingFraction()).isEqualTo(0.25f);
        assertThat(tracker1.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=26;ecn1=1;etm1=0;");

        VastFractionalProgressTracker tracker2 = trackers.get(2);
        assertThat(tracker2.trackingFraction()).isEqualTo(0.25f);
        assertThat(tracker2.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.2;sz=1x1;ord=2922389?");

        VastFractionalProgressTracker tracker3 = trackers.get(3);
        assertThat(tracker3.trackingFraction()).isEqualTo(0.5f);
        assertThat(tracker3.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=18;ecn1=1;etm1=0;");

        VastFractionalProgressTracker tracker4 = trackers.get(4);
        assertThat(tracker4.trackingFraction()).isEqualTo(0.5f);
        assertThat(tracker4.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.3;sz=1x1;ord=2922389?");

        VastFractionalProgressTracker tracker5 = trackers.get(5);
        assertThat(tracker5.trackingFraction()).isEqualTo(0.75f);
        assertThat(tracker5.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=27;ecn1=1;etm1=0;");

        VastFractionalProgressTracker tracker6 = trackers.get(6);
        assertThat(tracker6.trackingFraction()).isEqualTo(0.75f);
        assertThat(tracker6.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.4;sz=1x1;ord=2922389?");
    }

    @Test
    public void getAbsoluteProgressTrackers_shouldReturnCorrectValues() {
        subject = new VastLinearXmlManager(linearNode);
        List<VastAbsoluteProgressTracker> trackers = subject.getAbsoluteProgressTrackers();

        assertThat(trackers.size()).isEqualTo(4);

        VastAbsoluteProgressTracker tracker0 = trackers.get(0);
        assertThat(tracker0.getTrackingMilliseconds()).isEqualTo(0);
        assertThat(tracker0.getTrackingUrl()).isEqualTo("http://creativeView/one");

        VastAbsoluteProgressTracker tracker1 = trackers.get(1);
        assertThat(tracker1.getTrackingMilliseconds()).isEqualTo(0);
        assertThat(tracker1.getTrackingUrl()).isEqualTo("http://creativeView/two");

        VastAbsoluteProgressTracker tracker2 = trackers.get(2);
        assertThat(tracker2.getTrackingMilliseconds()).isEqualTo(2000);
        assertThat(tracker2.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;");

        VastAbsoluteProgressTracker tracker3 = trackers.get(3);
        assertThat(tracker3.getTrackingMilliseconds()).isEqualTo(3670300);
        assertThat(tracker3.getTrackingUrl()).isEqualTo("http://ad.doubleclick.net/activity;" +
                "src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;" +
                "rv=1;timestamp=2922389;eid1=11;ecn1=1;etm1=0;");
    }

    @Test
    public void getVideoCompleteTrackers_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        assertThat(VastUtils.vastTrackersToStrings(subject.getVideoCompleteTrackers()))
                .containsOnly("http://ad.doubleclick.net/activity;src=2215309;met=1;v=1;pid=47414672;aid=223626102;ko=0;cid=30477563;rid=30495440;rv=1;timestamp=2922389;eid1=13;ecn1=1;etm1=0;",
                        "http://ad.doubleclick.net/ad/N270.Process_Other/B3473145.5;sz=1x1;ord=2922389?");
    }

    @Test
    public void getVideoCloseTrackers_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        assertThat(VastUtils.vastTrackersToStrings(subject.getVideoCloseTrackers()))
                .containsOnly("http://www.mopub.com/close?q=ignatius",
                        "http://www.mopub.com/close?q=j3",
                        "http://www.mopub.com/closeLinear");
    }

    @Test
    public void getPauseTrackers_shouldReturnRepeatableVastTrackers() {
        subject = new VastLinearXmlManager(linearNode);
        for (VastTracker vastTracker : subject.getPauseTrackers()) {
            assertThat(vastTracker.isRepeatable());
        }
        assertThat(VastUtils.vastTrackersToStrings(subject.getPauseTrackers()))
                .containsOnly("http://www.mopub.com/pause?num=1",
                        "http://www.mopub.com/pause?num=2");
    }

    @Test
    public void getResumeTrackers_shouldReturnRepeatableVastTrackers() {
        subject = new VastLinearXmlManager(linearNode);
        for (VastTracker vastTracker : subject.getResumeTrackers()) {
            assertThat(vastTracker.isRepeatable());
        }
        assertThat(VastUtils.vastTrackersToStrings(subject.getResumeTrackers()))
                .containsOnly("http://www.mopub.com/resume?num=1",
                        "http://www.mopub.com/resume?num=2");
    }

    @Test
    public void getVideoSkipTrackers_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        assertThat(VastUtils.vastTrackersToStrings(subject.getVideoSkipTrackers()))
                .containsOnly("http://www.mopub.com/skip?q=ignatius",
                        "http://www.mopub.com/skip?q=j3");
    }

    @Test
    public void getClickThroughUrl_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        String url = subject.getClickThroughUrl();

        assertThat(url).isEqualTo("http://www.google.com/support/richmedia");
    }

    @Test
    public void getClickTrackers_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        assertThat(VastUtils.vastTrackersToStrings(subject.getClickTrackers()))
                .containsOnly("http://ad.doubleclick" +
                                ".net/click%3Bh%3Dv8/3e1b/3/0/%2a/z%3B223626102%3B0-0%3B0" +
                                "%3B47414672%3B255-0/0%3B30477563/30495440/1%3B%3B%7Eaopt%3D0/0" +
                                "/ff/0%3B%7Esscs%3D%3fhttp://s0.2mdn.net/dot.gif",
                        "http://ad.doubleclick.net/clk;212442087;33815766;i?http://www.google" +
                                ".com/support/richmedia");
    }

    @Test
    public void getSkipOffset_shouldReturnTheCorrectValue() {
        subject = new VastLinearXmlManager(linearNode);
        String skipOffset = subject.getSkipOffset();

        assertThat(skipOffset).isEqualTo("25%");
    }

    @Test
    public void getSkipOffset_withNoSkipOffsetAttribute_shouldReturnNull() throws Exception {
        String linearXml = "<Linear>" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        assertThat(subject.getSkipOffset()).isNull();
    }

    @Test
    public void getSkipOffset_withNoSkipOffsetAttributeValue_shouldReturnNull() throws Exception {
        String linearXml = "<Linear skipoffset=\"\">" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        assertThat(subject.getSkipOffset()).isNull();
    }

    @Test
    public void getMediaXmlManagers_shouldReturnMediaXmlManagers() {
        subject = new VastLinearXmlManager(linearNode);
        List<VastMediaXmlManager> mediaXmlManagers = subject.getMediaXmlManagers();

        assertThat(mediaXmlManagers.size()).isEqualTo(2);

        assertThat(mediaXmlManagers.get(0).getMediaUrl()).isEqualTo("http://s3.amazonaws" +
                ".com/uploads.hipchat.com/10627/429509/t8hqeqf98nvtir7/big_buck_bunny.mp4");
        assertThat(mediaXmlManagers.get(1).getMediaUrl()).isEqualTo("http://s3.amazonaws" +
                ".com/uploads.hipchat.com/10627/429509/t8hqeqf98nvtir7/big_buck_bunny_2.mp4");
    }

    @Test
    public void getMediaXmlManagers_withNoMediaFileNode_shouldReturnEmptyList() throws Exception {
        String linearXml = "<Linear skipoffset=\"25%\">" +
                "    <MediaFiles>" +
                "    </MediaFiles>" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        List<VastMediaXmlManager> mediaXmlManagers = subject.getMediaXmlManagers();
        assertThat(mediaXmlManagers).isEmpty();
    }

    @Test
    public void getMediaXmlManagers_withNoMediaFilesNode_shouldReturnEmptyList() throws Exception {
        String linearXml = "<Linear skipoffset=\"25%\">" +
                "    <MediaFile id=\"1\" delivery=\"progressive\" type=\"video/quicktime\" bitrate=\"457\"" +
                "               width=\"300\" height=\"225\">" +
                "        <![CDATA[" +
                "        http://s3.amazonaws.com/uploads.hipchat.com/10627/429509/t8hqeqf98nvtir7/big_buck_bunny.mp4" +
                "        ]]>" +
                "    </MediaFile>" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        List<VastMediaXmlManager> mediaXmlManagers = subject.getMediaXmlManagers();
        assertThat(mediaXmlManagers).isEmpty();
    }

    @Test
    public void getIconXmlManagers_shouldReturnIconXmlManagers() throws Exception {
        subject = new VastLinearXmlManager(linearNode);
        List<VastIconXmlManager> iconXmlManagers = subject.getIconXmlManagers();

        assertThat(iconXmlManagers).hasSize(2);
        assertThat(iconXmlManagers.get(0).getResourceXmlManager().getStaticResource())
                .isEqualTo("staticResource1");
        assertThat(iconXmlManagers.get(1).getResourceXmlManager().getStaticResource())
                .isEqualTo("staticResource2");
    }

    @Test
    public void getIconXmlManagers_withNoIconNode_shouldReturnEmptyList() throws Exception {
        String linearXml = "<Linear skipoffset=\"25%\">" +
                "    <Icons>" +
                "    </Icons>" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        assertThat(subject.getIconXmlManagers()).isEmpty();
    }

    @Test
    public void getIconXmlManagers_withNoIconsNode_shouldReturnEmptyList() throws Exception {
        String linearXml = "<Linear skipoffset=\"25%\">" +
                "    <Icon program=\"program\" width=\"123\" height=\"234\" xPosition=\"789\" " +
                "    yPosition=\"101\" apiFramework=\"apiFramework\" offset=\"01:02:03\" " +
                "    duration=\"01:02:03.456\">" +
                "        <StaticResource creativeType=\"ImAge/JpEg\">" +
                "           <![CDATA[staticResource1]]>" +
                "        </StaticResource>" +
                "    </Icon>" +
                "</Linear>";

        Node linearNode = createNode(linearXml);
        subject = new VastLinearXmlManager(linearNode);

        assertThat(subject.getIconXmlManagers()).isEmpty();
    }
}
