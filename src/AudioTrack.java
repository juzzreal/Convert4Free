public class AudioTrack {
    private final int audioOrder;
    private final int streamIndex;
    private final String codec;
    private final int channels;
    private final String language;
    private final String title;

    public AudioTrack(int audioOrder, int streamIndex, String codec, int channels, String language, String title) {
        this.audioOrder = audioOrder;
        this.streamIndex = streamIndex;
        this.codec = clean(codec, "unknown");
        this.channels = channels;
        this.language = clean(language, "unknown language");
        this.title = clean(title, "");
    }

    public int audioOrder() {
        return audioOrder;
    }

    public int streamIndex() {
        return streamIndex;
    }

    public String displayName() {
        String name = "Track " + (audioOrder + 1);
        if (!title.isBlank()) {
            name += " - " + title;
        }
        name += " (" + language + ", " + codec;
        if (channels > 0) {
            name += ", " + channels + " ch";
        }
        name += ")";
        return name;
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
