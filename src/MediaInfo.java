import java.util.List;

public class MediaInfo {
    private final double durationSeconds;
    private final List<AudioTrack> audioTracks;

    public MediaInfo(double durationSeconds, List<AudioTrack> audioTracks) {
        this.durationSeconds = durationSeconds;
        this.audioTracks = List.copyOf(audioTracks);
    }

    public static MediaInfo empty() {
        return new MediaInfo(0, List.of());
    }

    public double durationSeconds() {
        return durationSeconds;
    }

    public List<AudioTrack> audioTracks() {
        return audioTracks;
    }
}
