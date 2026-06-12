import java.util.List;
import java.util.Locale;

public enum ConversionType {
    MKV_TO_MP4("MKV to MP4", ".mkv", ".mp4", "Video remux", "Copies video and audio when MP4 supports the codecs."),
    MP4_TO_MOV("MP4 to MOV", ".mp4", ".mov", "Video remux", "Copies video and audio into a MOV container."),
    MP4_TO_MP3("MP4 to MP3", ".mp4", ".mp3", "Audio extract", "Extracts audio and encodes it as MP3.");

    private final String label;
    private final String inputExtension;
    private final String outputExtension;
    private final String mode;
    private final String description;

    ConversionType(String label, String inputExtension, String outputExtension, String mode, String description) {
        this.label = label;
        this.inputExtension = inputExtension;
        this.outputExtension = outputExtension;
        this.mode = mode;
        this.description = description;
    }

    public String inputExtension() {
        return inputExtension;
    }

    public String outputExtension() {
        return outputExtension;
    }

    public String mode() {
        return mode;
    }

    public String description() {
        return description;
    }

    public List<String> ffmpegOptions() {
        return switch (this) {
            case MKV_TO_MP4, MP4_TO_MOV -> List.of("-c", "copy");
            case MP4_TO_MP3 -> List.of("-vn", "-codec:a", "libmp3lame", "-q:a", "2");
        };
    }

    public String compatibilityHelp() {
        return switch (this) {
            case MKV_TO_MP4 ->
                    "The video or audio codec may not be compatible with the MP4 container. Re-encoding may be needed.";
            case MP4_TO_MOV ->
                    "The video or audio codec may not be compatible with the MOV container. Re-encoding may be needed.";
            case MP4_TO_MP3 ->
                    "The MP4 file may not contain an audio track, or this FFmpeg build may not support MP3 encoding.";
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
                "Unsupported conversion. Supported modes: MKV to MP4, MP4 to MOV, MP4 to MP3.");
    }

    @Override
    public String toString() {
        return label;
    }
}
