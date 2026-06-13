public class ConversionSettings {
    private final String quality;
    private final boolean fastStart;
    private final boolean stereoAudio;
    private final boolean copyWhenPossible;

    public ConversionSettings(String quality, boolean fastStart, boolean stereoAudio, boolean copyWhenPossible) {
        this.quality = quality;
        this.fastStart = fastStart;
        this.stereoAudio = stereoAudio;
        this.copyWhenPossible = copyWhenPossible;
    }

    public static ConversionSettings defaults() {
        return new ConversionSettings("Balanced", true, true, true);
    }

    public String quality() {
        return quality;
    }

    public boolean fastStart() {
        return fastStart;
    }

    public boolean stereoAudio() {
        return stereoAudio;
    }

    public boolean copyWhenPossible() {
        return copyWhenPossible;
    }

    public String videoCrf() {
        return switch (quality) {
            case "High quality" -> "18";
            case "Small file" -> "28";
            default -> "23";
        };
    }

    public String audioBitrate() {
        return switch (quality) {
            case "High quality" -> "320k";
            case "Small file" -> "128k";
            default -> "192k";
        };
    }
}
