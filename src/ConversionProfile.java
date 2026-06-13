public enum ConversionProfile {
    EDITING_MP4(false),
    VIDEO_MP4(false),
    VIDEO_MOV(false),
    VIDEO_MKV(false),
    VIDEO_WEBM(false),
    VIDEO_AVI(false),
    GIF(false),
    AUDIO_MP3(true),
    AUDIO_AAC(true),
    AUDIO_WAV(true),
    AUDIO_FLAC(true),
    AUDIO_OGG(true);

    private final boolean audioOnlyOutput;

    ConversionProfile(boolean audioOnlyOutput) {
        this.audioOnlyOutput = audioOnlyOutput;
    }

    public boolean isAudioOnlyOutput() {
        return audioOnlyOutput;
    }
}
