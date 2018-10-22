/*
 * Intel License Header Holder
 */
package com.intel.webrtc.conference;

import com.intel.webrtc.base.VideoCodecParameters;
import com.intel.webrtc.base.AudioCodecParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.intel.webrtc.base.CheckCondition.RCHECK;

/**
 * Options for subscribing a RemoteStream. Subscribing a RemoteStream with the SubscribeOptions that
 * does not comply with its SubscriptionCapabilities may cause failure.
 */
public final class SubscribeOptions {

    /**
     * Audio options for subscribing a RemoteStream.
     */
    public static class AudioSubscriptionConstraints {

        /**
         * Builder for building up a AudioSubscriptionConstraints.
         */
        public static class Builder {
            private List<AudioCodecParameters> codecs = new ArrayList<>();

            Builder() {
            }

            /**
             * Add an AudioCodecParameters to be supported for subscribing a RemoteStream.
             * AudioSubscriptionConstraints without any AudioCodecParameters specified, it will
             * support all audio codecs supported by the hardware devices.
             *
             * @param codec AudioCodecParameters to be added.
             * @return Builder
             */
            public Builder addCodec(AudioCodecParameters codec) {
                RCHECK(codec);
                codecs.add(codec);
                return this;
            }

            /**
             * Build up the AudioSubscriptionConstraints.
             *
             * @return AudioSubscriptionConstraints
             */
            public AudioSubscriptionConstraints build() {
                return new AudioSubscriptionConstraints(codecs);
            }
        }

        final List<AudioCodecParameters> codecs;

        /**
         * Get a Builder for creating a AudioSubscriptionConstraints.
         *
         * @return AudioSubscriptionConstraints
         */
        public static Builder builder() {
            return new Builder();
        }

        private AudioSubscriptionConstraints(List<AudioCodecParameters> codecs) {
            this.codecs = codecs;
        }

    }

    /**
     * Video options for subscribing a RemoteStream.
     */
    public static class VideoSubscriptionConstraints {

        /**
         * Builder for building up a VideoSubscriptionConstraints.
         */
        public static class Builder {
            private List<VideoCodecParameters> codecs = new ArrayList<>();
            private int resolutionWidth = 0, resolutionHeight = 0;
            private int frameRate = 0, keyFrameInterval = 0;
            private double bitrateMultiplier = 0;

            Builder() {
            }

            /**
             * Set up the video resolution for subscribing a RemoteStream. Mandatory for
             * VideoSubscriptionConstraints.
             *
             * @param width  resolution width.
             * @param height resolution height.
             * @return Builder
             */
            public Builder setResolution(int width, int height) {
                resolutionWidth = width;
                resolutionHeight = height;
                return this;
            }

            /**
             * Set up the video framerate for subscribing a RemoteStream. Optional for
             * VideoSubscriptionConstraints.
             *
             * @param frameRate framerate to be set.
             * @return Builder
             */
            public Builder setFrameRate(int frameRate) {
                this.frameRate = frameRate;
                return this;
            }

            /**
             * Set up the interval of keyframes for subscribing a RemoteStream. Optional for
             * VideoSubscriptionConstraints.
             *
             * @param keyFrameInterval interval of keyframes to be set.
             * @return Builder
             */
            public Builder setKeyFrameInterval(int keyFrameInterval) {
                this.keyFrameInterval = keyFrameInterval;
                return this;
            }

            /**
             * Set up the multiplier of bitrate for subscribing a RemoteStream. Optional for
             * VideoSubscriptionConstraints.
             *
             * @param multiplier multiplier of bitrate to be set.
             * @return Builder
             */
            public Builder setBitrateMultiplier(double multiplier) {
                bitrateMultiplier = multiplier;
                return this;
            }

            /**
             * Add a VideoCodecParameters to be supported for subscribing a RemoteStream.
             * AudioSubscriptionConstraints without any VideoCodecParameters specified, it will
             * support all video codecs supported by the hardware devices.
             *
             * @param codec VideoCodecParameters to be added.
             * @return Builder
             */
            public Builder addCodec(VideoCodecParameters codec) {
                RCHECK(codec);
                codecs.add(codec);
                return this;
            }

            /**
             * Build up the VideoSubscriptionConstraints.
             *
             * @return VideoSubscriptionConstraints
             */
            public VideoSubscriptionConstraints build() {
                RCHECK(resolutionWidth != 0 && resolutionHeight != 0);
                VideoSubscriptionConstraints constraints = new VideoSubscriptionConstraints(codecs);
                constraints.resolutionWidth = resolutionWidth;
                constraints.resolutionHeight = resolutionHeight;
                constraints.frameRate = frameRate;
                constraints.keyFrameInterval = keyFrameInterval;
                constraints.bitrateMultiplier = bitrateMultiplier;
                return constraints;
            }

        }

        final List<VideoCodecParameters> codecs;
        private int resolutionWidth = 0, resolutionHeight = 0;
        private int frameRate = 0, keyFrameInterval = 0;
        private double bitrateMultiplier = 0;

        /**
         * Get a Builder for creating a VideoSubscriptionConstraints.
         *
         * @return VideoSubscriptionConstraints
         */
        public static Builder builder() {
            return new Builder();
        }

        private VideoSubscriptionConstraints(List<VideoCodecParameters> codecs) {
            this.codecs = codecs;
        }

        JSONObject generateOptionsMsg() throws JSONException {
            JSONObject videoParams = new JSONObject();

            if (resolutionWidth != 0 && resolutionHeight != 0) {
                JSONObject reso = new JSONObject();
                reso.put("width", resolutionWidth);
                reso.put("height", resolutionHeight);
                videoParams.put("resolution", reso);
            }

            if (frameRate != 0) {
                videoParams.put("framerate", frameRate);
            }

            if (bitrateMultiplier != 0) {
                videoParams.put("bitrate", "x" + bitrateMultiplier);
            }

            if (keyFrameInterval != 0) {
                videoParams.put("keyFrameInterval", keyFrameInterval);
            }

            return videoParams;
        }
    }

    /**
     * Builder for building up a SubscribeOptions.
     */
    public static class Builder {
        private boolean subAudio, subVideo;
        private AudioSubscriptionConstraints audioOption;
        private VideoSubscriptionConstraints videoOption;

        Builder(boolean subAudio, boolean subVideo) {
            this.subAudio = subAudio;
            this.subVideo = subVideo;
        }

        /**
         * Set up the AudioSubscriptionConstraints. Mandatory if subAudio is true.
         *
         * @param audioOption AudioSubscriptionConstraints to be set.
         * @return Builder
         */
        public Builder setAudioOption(AudioSubscriptionConstraints audioOption) {
            this.audioOption = audioOption;
            return this;
        }

        /**
         * Set up the VideoSubscriptionConstraints. Mandatory if subVideo is true.
         *
         * @param videoOption VideoSubscriptionConstraints to be set.
         * @return Builder
         */
        public Builder setVideoOption(VideoSubscriptionConstraints videoOption) {
            this.videoOption = videoOption;
            return this;
        }

        /**
         * Build up the SubscribeOptions.
         *
         * @return SubscribeOptions
         */
        public SubscribeOptions build() {
            RCHECK(!subAudio || audioOption != null);
            RCHECK(!subVideo || videoOption != null);
            return new SubscribeOptions(subAudio ? audioOption : null,
                                        subVideo ? videoOption : null);
        }
    }

    public final AudioSubscriptionConstraints audioOption;
    public final VideoSubscriptionConstraints videoOption;

    /**
     * Get a Builder for creating a SubscribeOptions.
     *
     * @param subAudio whether to subscribe the audio track.
     * @param subVideo whether to subscribe the video track.
     * @return Builder
     */
    public static Builder builder(boolean subAudio, boolean subVideo) {
        return new Builder(subAudio, subVideo);
    }

    private SubscribeOptions(AudioSubscriptionConstraints audioOption,
                             VideoSubscriptionConstraints videoOption) {
        this.audioOption = audioOption;
        this.videoOption = videoOption;
    }

}
