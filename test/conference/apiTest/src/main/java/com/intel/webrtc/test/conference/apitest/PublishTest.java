package com.intel.webrtc.test.conference.apitest;

import static com.intel.webrtc.base.MediaCodecs.AudioCodec.G722;
import static com.intel.webrtc.base.MediaCodecs.AudioCodec.ILBC;
import static com.intel.webrtc.base.MediaCodecs.AudioCodec.ISAC;
import static com.intel.webrtc.base.MediaCodecs.AudioCodec.OPUS;
import static com.intel.webrtc.base.MediaCodecs.AudioCodec.PCMA;
import static com.intel.webrtc.base.MediaCodecs.AudioCodec.PCMU;
import static com.intel.webrtc.base.MediaCodecs.VideoCodec.H264;
import static com.intel.webrtc.base.MediaCodecs.VideoCodec.VP8;
import static com.intel.webrtc.base.MediaCodecs.VideoCodec.VP9;
import static com.intel.webrtc.base.MediaConstraints.VideoTrackConstraints;
import static com.intel.webrtc.base.MediaConstraints.VideoTrackConstraints.CameraFacing.FRONT;
import static com.intel.webrtc.test.conference.util.ConferenceAction.createClient;
import static com.intel.webrtc.test.conference.util.ConferenceAction.createPublishOptions;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getRemoteForwardStream;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getStats;
import static com.intel.webrtc.test.conference.util.ConferenceAction.getToken;
import static com.intel.webrtc.test.conference.util.ConferenceAction.join;
import static com.intel.webrtc.test.conference.util.ConferenceAction.publish;
import static com.intel.webrtc.test.conference.util.ConferenceAction.stop;
import static com.intel.webrtc.test.util.CommonAction.checkRTCStats;
import static com.intel.webrtc.test.util.CommonAction.createDefaultCapturer;
import static com.intel.webrtc.test.util.CommonAction.createLocalStream;
import static com.intel.webrtc.test.util.Config.AUDIO_ONLY_PRESENTER_ROLE;
import static com.intel.webrtc.test.util.Config.MIXED_STREAM_SIZE;
import static com.intel.webrtc.test.util.Config.PRESENTER_ROLE;
import static com.intel.webrtc.test.util.Config.RAW_STREAM_FILE;
import static com.intel.webrtc.test.util.Config.TIMEOUT;
import static com.intel.webrtc.test.util.Config.USER1_NAME;
import static com.intel.webrtc.test.util.Config.VIDEO_ONLY_VIEWER_ROLE;
import static com.intel.webrtc.test.util.Config.VIEWER_ROLE;

import com.intel.webrtc.base.IcsVideoCapturer;
import com.intel.webrtc.base.MediaCodecs.AudioCodec;
import com.intel.webrtc.base.MediaCodecs.VideoCodec;
import com.intel.webrtc.conference.Publication;
import com.intel.webrtc.conference.PublishOptions;
import com.intel.webrtc.conference.RemoteStream;
import com.intel.webrtc.test.conference.util.ConferenceClientObserver;
import com.intel.webrtc.test.util.RawCapturerForTest;
import com.intel.webrtc.test.util.TestCallback;

import org.webrtc.RTCStatsReport;

import java.io.IOException;
import java.util.HashMap;

public class PublishTest extends TestBase {

    public void testPublish_beforeJoin_shouldFail() {
        client1 = createClient(null);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, null, false);
        client1 = null;
    }

    public void testPublish_withoutOption_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        Publication publication = publish(client1, localStream1, null, observer1, true);
        RTCStatsReport statsReport = getStats(publication, true);
        HashMap<String, String> expectation = new HashMap<>();
        expectation.put("videoCodec", "vp8");
        checkRTCStats(statsReport, expectation, true, true, true);
    }

    public void testPublish_withDefaultOption_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        PublishOptions publishOptions = createPublishOptions(null, null);
        publish(client1, localStream1, publishOptions, observer1, true);
    }

    public void testPublish_withAudioCodec_shouldSucceed() {
        AudioCodec[] audioCodecs = new AudioCodec[]{OPUS, PCMU, PCMA, G722, ISAC, ILBC};
        String[] checkCodecs = new String[]{"opus", "pcmu", "pcma", "g722", "isac", "ilbc"};
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        for (int i = 0; i < audioCodecs.length; i++) {
            PublishOptions publishOptions = createPublishOptions(new AudioCodec[]{audioCodecs[i]},
                    0,
                    new VideoCodec[]{}, 0);
            Publication publication = publish(client1, localStream1, publishOptions, observer1,
                    true);
            assertTrue(observer1.remoteStreams.get(
                    i).publicationSettings.audioPublicationSettings.codec.name.name()
                    .equalsIgnoreCase(
                            checkCodecs[i]));
            RTCStatsReport sendStats = getStats(publication, true);
            HashMap<String, String> expectation = new HashMap<>();
            expectation.put("audioCodec", checkCodecs[i]);
            checkRTCStats(sendStats, expectation, true, true, true);
            stop(publication, observer1, true);
        }
    }

    public void testPublish_withVideoCodec_shouldSucceed() {
        VideoCodec[] videoCodecs = new VideoCodec[]{VP8, VP9, H264};
        String[] checkCodecs = new String[]{"vp8", "vp9", "h264"};
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        for (int i = 0; i < videoCodecs.length; i++) {
            PublishOptions publishOptions = createPublishOptions(new AudioCodec[]{},
                    new VideoCodec[]{videoCodecs[i]});
            Publication publication = publish(client1, localStream1, publishOptions, observer1,
                    true);
            assertTrue(observer1.remoteStreams.get(
                    i).publicationSettings.videoPublicationSettings.codec.name.name()
                    .equalsIgnoreCase(
                            checkCodecs[i]));
            RTCStatsReport sendStats = getStats(publication, true);
            HashMap<String, String> expectation = new HashMap<>();
            expectation.put("videoCodec", checkCodecs[i]);
            checkRTCStats(sendStats, expectation, true, true, true);
            stop(publication, observer1, true);
        }
    }

    public void testPublish_withAudioBitrate_shouldSucceed() {
        int[] bitrate = new int[]{-1, 30, 60};
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        for (int i = 0; i < bitrate.length; i++) {
            PublishOptions publishOptions = createPublishOptions(new AudioCodec[]{}, bitrate[i],
                    new VideoCodec[]{}, 0);
            Publication publication = publish(client1, localStream1, publishOptions, observer1,
                    true);
            stop(publication, observer1, true);
        }
    }

    public void testPublish_withVideoBitrate_shouldSucceed() {
        int[] bitrate = new int[]{-1, 300, 2000};
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        for (int i = 0; i < bitrate.length; i++) {
            PublishOptions publishOptions = createPublishOptions(new AudioCodec[]{}, 60,
                    new VideoCodec[]{}, bitrate[i]);
            Publication publication = publish(client1, localStream1, publishOptions, observer1,
                    true);
            stop(publication, observer1, true);
        }
    }

    public void testPublish_withResolution_shouldSucceed() {
        String[] resolutions = new String[]{"1920x1280", "1280x720", "640x480"};
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        for (int i = 0; i < resolutions.length; i++) {
            int width = Integer.valueOf(resolutions[i].split("x")[0]);
            int height = Integer.valueOf(resolutions[i].split("x")[1]);
            VideoTrackConstraints vmc = VideoTrackConstraints.create(true);
            vmc.setResolution(width, height);
            vmc.setFramerate(30);
            vmc.setCameraFacing(FRONT);
            capturer1 = new IcsVideoCapturer(vmc);
            localStream1 = createLocalStream(true, capturer1);
            PublishOptions publishOptions = createPublishOptions(new AudioCodec[]{},
                    new VideoCodec[]{});
            Publication publication = publish(client1, localStream1, publishOptions, observer1,
                    true);
            assertTrue(observer1.remoteStreams.get(
                    i).publicationSettings.videoPublicationSettings.resolutionWidth == width);
            assertTrue(observer1.remoteStreams.get(
                    i).publicationSettings.videoPublicationSettings.resolutionHeight == height);
            stop(publication, observer1, true);
            capturer1.dispose();
            localStream1.dispose();
        }
        capturer1 = null;
        localStream1 = null;
    }

    public void testPublish_twiceWithSameStream_shouldSuccessAt2nd() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, observer1, true);
        publish(client1, localStream1, null, observer1, true);
    }

    public void testPublish_twiceWithDifferentStream_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, observer1, true);
        capturer2 = null;
        try {
            capturer2 = new RawCapturerForTest(RAW_STREAM_FILE);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        localStream2 = createLocalStream(true, capturer2);
        publish(client1, localStream2, null, observer1, true);
    }

    public void testPublish_withAudioOnly_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        localStream1 = createLocalStream(true, null);
        publish(client1, localStream1, null, observer1, true);
        assertNull(observer1.remoteStreams.get(
                0).subscriptionCapability.videoSubscriptionCapabilities);
        assertNotNull(observer1.remoteStreams.get(
                0).subscriptionCapability.audioSubscriptionCapabilities);
    }

    public void testPublish_withVideoOnly_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(false, capturer1);
        publish(client1, localStream1, null, observer1, true);
        assertNotNull(observer1.remoteStreams.get(
                0).subscriptionCapability.videoSubscriptionCapabilities);
        assertNull(observer1.remoteStreams.get(
                0).subscriptionCapability.audioSubscriptionCapabilities);
    }

    public void testPublish_withNullStream_shouldThrowException() {
        client1 = createClient(null);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        try {
            publish(client1, null, null, null, false);
            fail("RuntimeException expected.");
        } catch (RuntimeException ignored) {
        }
    }

    public void testPublish_withViewerRole_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(VIEWER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, null, false);
    }

    public void testPublish_videoStreamWithVideoOnlyViewer_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(VIDEO_ONLY_VIEWER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(false, capturer1);
        publish(client1, localStream1, null, null, false);
    }

    public void testPublish_videoStreamWithAudioOnlyPresenterRole_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(AUDIO_ONLY_PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, null, false);
    }

    public void testPublish_audioOnlyStreamWithAudioOnlyPresenterRole_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(AUDIO_ONLY_PRESENTER_ROLE, USER1_NAME), null, null, true);
        localStream1 = createLocalStream(true, null);
        publish(client1, localStream1, null, observer1, true);
    }

    public void testPublish_videoOnlyStreamWithVideoOnlyViewer_shouldFail() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(AUDIO_ONLY_PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(false, capturer1);
        publish(client1, localStream1, null, null, false);
    }

    public void testPublish_afterPublicationStop_shouldSucceed() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        Publication publication1 = publish(client1, localStream1, null, observer1, true);
        stop(publication1, observer1, true);
        publish(client1, localStream1, null, observer1, true);
    }

    public void testPublish_sameStreamTwiceWithoutCallBack_shouldSucceedTwice() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 2);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        TestCallback<Publication> callback1 = new TestCallback<>();
        TestCallback<Publication> callback2 = new TestCallback<>();
        client1.publish(localStream1, callback1);
        client1.publish(localStream1, callback2);
        assertTrue(callback1.getResult(true, TIMEOUT));
        assertTrue(callback2.getResult(true, TIMEOUT));
        assertTrue(observer1.getResultForPublish(TIMEOUT));
    }

    public void testPublish_differentStreamWithoutWaitCallBack_shouldSucceedOnce() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 2);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        capturer2 = null;
        try {
            capturer2 = new RawCapturerForTest(RAW_STREAM_FILE);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        localStream2 = createLocalStream(true, capturer2);
        TestCallback<Publication> callback1 = new TestCallback<>();
        TestCallback<Publication> callback2 = new TestCallback<>();
        client1.publish(localStream1, callback1);
        client1.publish(localStream2, callback2);
        assertTrue(callback1.getResult(true, TIMEOUT));
        assertTrue(callback2.getResult(true, TIMEOUT));
        assertTrue(observer1.getResultForPublish(TIMEOUT));
    }

    public void testPublish_checkAttributes() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        HashMap<String, String> attr = new HashMap<>();
        attr.put("attribute_key", "attribute_value");
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        localStream1.setAttributes(attr);
        publish(client1, localStream1, null, observer1, true);
        int localStream1sN = client1.info().getRemoteStreams().size() - MIXED_STREAM_SIZE;
        RemoteStream forwardStream1 = getRemoteForwardStream(client1, localStream1sN - 1);
        assertTrue(forwardStream1.getAttributes() != null);
        assertTrue(forwardStream1.getAttributes().size() == attr.size());
        for (String key : attr.keySet()) {
            assertTrue(attr.get(key).equals(forwardStream1.getAttributes().get(key)));
        }
    }

    public void testPublish_checkNullAttributes() {
        observer1 = new ConferenceClientObserver(USER1_NAME, 1);
        client1 = createClient(observer1);
        join(client1, getToken(PRESENTER_ROLE, USER1_NAME), null, null, true);
        capturer1 = createDefaultCapturer();
        localStream1 = createLocalStream(true, capturer1);
        publish(client1, localStream1, null, observer1, true);
        int localStream1sN = client1.info().getRemoteStreams().size() - MIXED_STREAM_SIZE;
        RemoteStream forwardStream1 = getRemoteForwardStream(client1, localStream1sN - 1);
        assertTrue(forwardStream1.getAttributes() == null);
    }
}
