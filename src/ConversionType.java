import java.util.Arrays;
import java.util.Locale;

public enum ConversionType {
    MKV_TO_MP4_EDIT("MKV to MP4 (After Effects)", ".mkv", ".mp4", "Editing", "After Effects friendly MP4 with one AAC stereo track.", ConversionProfile.EDITING_MP4),
    MKV_TO_MOV("MKV to MOV", ".mkv", ".mov", "Video", "MOV video for editing workflows.", ConversionProfile.VIDEO_MOV),
    MKV_TO_WEBM("MKV to WEBM", ".mkv", ".webm", "Web", "WEBM video for web use.", ConversionProfile.VIDEO_WEBM),
    MKV_TO_AVI("MKV to AVI", ".mkv", ".avi", "Legacy", "AVI video for older apps.", ConversionProfile.VIDEO_AVI),
    MKV_TO_MP3("MKV to MP3", ".mkv", ".mp3", "Audio", "Extracts audio as MP3.", ConversionProfile.AUDIO_MP3),
    MKV_TO_WAV("MKV to WAV", ".mkv", ".wav", "Audio", "Extracts uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    MKV_TO_FLAC("MKV to FLAC", ".mkv", ".flac", "Audio", "Extracts lossless FLAC audio.", ConversionProfile.AUDIO_FLAC),
    MKV_TO_AAC("MKV to AAC", ".mkv", ".aac", "Audio", "Extracts AAC audio.", ConversionProfile.AUDIO_AAC),

    MP4_TO_MOV("MP4 to MOV", ".mp4", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    MP4_TO_MP3("MP4 to MP3", ".mp4", ".mp3", "Audio", "Extracts audio as MP3.", ConversionProfile.AUDIO_MP3),
    MP4_TO_WAV("MP4 to WAV", ".mp4", ".wav", "Audio", "Extracts uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    MP4_TO_FLAC("MP4 to FLAC", ".mp4", ".flac", "Audio", "Extracts lossless FLAC audio.", ConversionProfile.AUDIO_FLAC),
    MP4_TO_AAC("MP4 to AAC", ".mp4", ".aac", "Audio", "Extracts AAC audio.", ConversionProfile.AUDIO_AAC),
    MP4_TO_OGG("MP4 to OGG", ".mp4", ".ogg", "Audio", "Extracts OGG Vorbis audio.", ConversionProfile.AUDIO_OGG),
    MP4_TO_WEBM("MP4 to WEBM", ".mp4", ".webm", "Web", "WEBM video for browsers.", ConversionProfile.VIDEO_WEBM),
    MP4_TO_MKV("MP4 to MKV", ".mp4", ".mkv", "Video", "MKV video container.", ConversionProfile.VIDEO_MKV),
    MP4_TO_AVI("MP4 to AVI", ".mp4", ".avi", "Legacy", "AVI video for older apps.", ConversionProfile.VIDEO_AVI),
    MP4_TO_M4V("MP4 to M4V", ".mp4", ".m4v", "Video", "M4V video container.", ConversionProfile.VIDEO_MP4),
    MP4_TO_GIF("MP4 to GIF", ".mp4", ".gif", "Animation", "Short animated GIF.", ConversionProfile.GIF),

    MOV_TO_MP4("MOV to MP4", ".mov", ".mp4", "Video", "MP4 video for sharing.", ConversionProfile.VIDEO_MP4),
    MOV_TO_MKV("MOV to MKV", ".mov", ".mkv", "Video", "MKV video container.", ConversionProfile.VIDEO_MKV),
    MOV_TO_WEBM("MOV to WEBM", ".mov", ".webm", "Web", "WEBM video for browsers.", ConversionProfile.VIDEO_WEBM),
    MOV_TO_AVI("MOV to AVI", ".mov", ".avi", "Legacy", "AVI video for older apps.", ConversionProfile.VIDEO_AVI),
    MOV_TO_MP3("MOV to MP3", ".mov", ".mp3", "Audio", "Extracts audio as MP3.", ConversionProfile.AUDIO_MP3),
    MOV_TO_WAV("MOV to WAV", ".mov", ".wav", "Audio", "Extracts WAV audio.", ConversionProfile.AUDIO_WAV),
    MOV_TO_FLAC("MOV to FLAC", ".mov", ".flac", "Audio", "Extracts FLAC audio.", ConversionProfile.AUDIO_FLAC),
    MOV_TO_GIF("MOV to GIF", ".mov", ".gif", "Animation", "Short animated GIF.", ConversionProfile.GIF),

    AVI_TO_MP4("AVI to MP4", ".avi", ".mp4", "Video", "MP4 video for modern players.", ConversionProfile.VIDEO_MP4),
    AVI_TO_MOV("AVI to MOV", ".avi", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    AVI_TO_MKV("AVI to MKV", ".avi", ".mkv", "Video", "MKV video container.", ConversionProfile.VIDEO_MKV),
    AVI_TO_WEBM("AVI to WEBM", ".avi", ".webm", "Web", "WEBM video for browsers.", ConversionProfile.VIDEO_WEBM),
    AVI_TO_MP3("AVI to MP3", ".avi", ".mp3", "Audio", "Extracts MP3 audio.", ConversionProfile.AUDIO_MP3),
    AVI_TO_WAV("AVI to WAV", ".avi", ".wav", "Audio", "Extracts WAV audio.", ConversionProfile.AUDIO_WAV),

    WEBM_TO_MP4("WEBM to MP4", ".webm", ".mp4", "Video", "MP4 video for modern players.", ConversionProfile.VIDEO_MP4),
    WEBM_TO_MOV("WEBM to MOV", ".webm", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    WEBM_TO_MKV("WEBM to MKV", ".webm", ".mkv", "Video", "MKV video container.", ConversionProfile.VIDEO_MKV),
    WEBM_TO_MP3("WEBM to MP3", ".webm", ".mp3", "Audio", "Extracts MP3 audio.", ConversionProfile.AUDIO_MP3),
    WEBM_TO_WAV("WEBM to WAV", ".webm", ".wav", "Audio", "Extracts WAV audio.", ConversionProfile.AUDIO_WAV),
    WEBM_TO_GIF("WEBM to GIF", ".webm", ".gif", "Animation", "Short animated GIF.", ConversionProfile.GIF),

    FLV_TO_MP4("FLV to MP4", ".flv", ".mp4", "Video", "MP4 video for modern players.", ConversionProfile.VIDEO_MP4),
    FLV_TO_MOV("FLV to MOV", ".flv", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    FLV_TO_MP3("FLV to MP3", ".flv", ".mp3", "Audio", "Extracts MP3 audio.", ConversionProfile.AUDIO_MP3),
    FLV_TO_WAV("FLV to WAV", ".flv", ".wav", "Audio", "Extracts WAV audio.", ConversionProfile.AUDIO_WAV),

    WMV_TO_MP4("WMV to MP4", ".wmv", ".mp4", "Video", "MP4 video for modern players.", ConversionProfile.VIDEO_MP4),
    WMV_TO_MOV("WMV to MOV", ".wmv", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    WMV_TO_MP3("WMV to MP3", ".wmv", ".mp3", "Audio", "Extracts MP3 audio.", ConversionProfile.AUDIO_MP3),
    WMV_TO_WAV("WMV to WAV", ".wmv", ".wav", "Audio", "Extracts WAV audio.", ConversionProfile.AUDIO_WAV),

    M4V_TO_MP4("M4V to MP4", ".m4v", ".mp4", "Video", "MP4 video container.", ConversionProfile.VIDEO_MP4),
    M4V_TO_MOV("M4V to MOV", ".m4v", ".mov", "Video", "MOV video for editing apps.", ConversionProfile.VIDEO_MOV),
    M4V_TO_MP3("M4V to MP3", ".m4v", ".mp3", "Audio", "Extracts MP3 audio.", ConversionProfile.AUDIO_MP3),

    MP3_TO_WAV("MP3 to WAV", ".mp3", ".wav", "Audio", "Uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    MP3_TO_FLAC("MP3 to FLAC", ".mp3", ".flac", "Audio", "FLAC audio file.", ConversionProfile.AUDIO_FLAC),
    MP3_TO_AAC("MP3 to AAC", ".mp3", ".aac", "Audio", "AAC audio file.", ConversionProfile.AUDIO_AAC),
    MP3_TO_OGG("MP3 to OGG", ".mp3", ".ogg", "Audio", "OGG Vorbis audio.", ConversionProfile.AUDIO_OGG),

    WAV_TO_MP3("WAV to MP3", ".wav", ".mp3", "Audio", "Compressed MP3 audio.", ConversionProfile.AUDIO_MP3),
    WAV_TO_FLAC("WAV to FLAC", ".wav", ".flac", "Audio", "Lossless FLAC audio.", ConversionProfile.AUDIO_FLAC),
    WAV_TO_AAC("WAV to AAC", ".wav", ".aac", "Audio", "AAC audio file.", ConversionProfile.AUDIO_AAC),
    WAV_TO_OGG("WAV to OGG", ".wav", ".ogg", "Audio", "OGG Vorbis audio.", ConversionProfile.AUDIO_OGG),

    FLAC_TO_MP3("FLAC to MP3", ".flac", ".mp3", "Audio", "Compressed MP3 audio.", ConversionProfile.AUDIO_MP3),
    FLAC_TO_WAV("FLAC to WAV", ".flac", ".wav", "Audio", "Uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    FLAC_TO_AAC("FLAC to AAC", ".flac", ".aac", "Audio", "AAC audio file.", ConversionProfile.AUDIO_AAC),
    FLAC_TO_OGG("FLAC to OGG", ".flac", ".ogg", "Audio", "OGG Vorbis audio.", ConversionProfile.AUDIO_OGG),

    AAC_TO_MP3("AAC to MP3", ".aac", ".mp3", "Audio", "Compressed MP3 audio.", ConversionProfile.AUDIO_MP3),
    AAC_TO_WAV("AAC to WAV", ".aac", ".wav", "Audio", "Uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    AAC_TO_FLAC("AAC to FLAC", ".aac", ".flac", "Audio", "Lossless FLAC audio.", ConversionProfile.AUDIO_FLAC),
    AAC_TO_OGG("AAC to OGG", ".aac", ".ogg", "Audio", "OGG Vorbis audio.", ConversionProfile.AUDIO_OGG),

    OGG_TO_MP3("OGG to MP3", ".ogg", ".mp3", "Audio", "Compressed MP3 audio.", ConversionProfile.AUDIO_MP3),
    OGG_TO_WAV("OGG to WAV", ".ogg", ".wav", "Audio", "Uncompressed WAV audio.", ConversionProfile.AUDIO_WAV),
    OGG_TO_FLAC("OGG to FLAC", ".ogg", ".flac", "Audio", "Lossless FLAC audio.", ConversionProfile.AUDIO_FLAC),
    OGG_TO_AAC("OGG to AAC", ".ogg", ".aac", "Audio", "AAC audio file.", ConversionProfile.AUDIO_AAC);

    private final String label;
    private final String inputExtension;
    private final String outputExtension;
    private final String category;
    private final String description;
    private final ConversionProfile profile;

    ConversionType(
            String label,
            String inputExtension,
            String outputExtension,
            String category,
            String description,
            ConversionProfile profile) {
        this.label = label;
        this.inputExtension = inputExtension;
        this.outputExtension = outputExtension;
        this.category = category;
        this.description = description;
        this.profile = profile;
    }

    public String inputExtension() {
        return inputExtension;
    }

    public String outputExtension() {
        return outputExtension;
    }

    public String targetLabel() {
        return outputExtension.toUpperCase(Locale.ROOT).substring(1);
    }

    public String mode() {
        return category;
    }

    public String description() {
        return description;
    }

    public ConversionProfile profile() {
        return profile;
    }

    public boolean isAudioOnlyOutput() {
        return profile.isAudioOnlyOutput();
    }

    public String compatibilityHelp() {
        return switch (profile) {
            case EDITING_MP4 -> "The video codec may not be compatible with MP4, or the audio layout could not be converted.";
            case GIF -> "The selected video may be too long or too large for a practical GIF.";
            case AUDIO_MP3, AUDIO_AAC, AUDIO_WAV, AUDIO_FLAC, AUDIO_OGG -> "The input may not contain a readable audio stream.";
            default -> "The selected input may need different codec settings for this output format.";
        };
    }

    public static ConversionType fromPaths(String inputPath, String outputPath) {
        String input = inputPath.toLowerCase(Locale.ROOT);
        String output = outputPath.toLowerCase(Locale.ROOT);

        for (ConversionType type : values()) {
            if (input.endsWith(type.inputExtension) && output.endsWith(type.outputExtension)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported conversion. Choose a supported input/output pair in the desktop app.");
    }

    public static ConversionType[] forInputPath(String inputPath) {
        String input = inputPath.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> input.endsWith(type.inputExtension))
                .toArray(ConversionType[]::new);
    }

    public static boolean isSupportedInput(String inputPath) {
        return forInputPath(inputPath).length > 0;
    }

    public static String supportedInputExtensions() {
        return Arrays.stream(values())
                .map(ConversionType::inputExtension)
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    @Override
    public String toString() {
        return label;
    }
}
