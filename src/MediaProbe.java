import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaProbe {
    public static MediaInfo probe(Path inputFile) {
        double duration = probeDuration(inputFile);
        List<AudioTrack> audioTracks = probeAudioTracks(inputFile);
        return new MediaInfo(duration, audioTracks);
    }

    private static double probeDuration(Path inputFile) {
        List<String> lines = run(List.of(
                "ffprobe",
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                inputFile.toString()));

        if (lines.isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(lines.get(0).trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static List<AudioTrack> probeAudioTracks(Path inputFile) {
        List<String> lines = run(List.of(
                "ffprobe",
                "-v",
                "error",
                "-select_streams",
                "a",
                "-show_entries",
                "stream=index,codec_name,channels:stream_tags=title,language",
                "-of",
                "flat",
                inputFile.toString()));

        Map<Integer, Map<String, String>> streams = new HashMap<>();
        for (String line : lines) {
            int streamMarker = line.indexOf("streams.stream.");
            if (streamMarker < 0) {
                continue;
            }

            int streamStart = streamMarker + "streams.stream.".length();
            int dot = line.indexOf('.', streamStart);
            int equals = line.indexOf('=');
            if (dot < 0 || equals < 0 || dot > equals) {
                continue;
            }

            try {
                int audioOrder = Integer.parseInt(line.substring(streamStart, dot));
                String key = line.substring(dot + 1, equals);
                String value = unquote(line.substring(equals + 1));
                streams.computeIfAbsent(audioOrder, ignored -> new HashMap<>()).put(key, value);
            } catch (NumberFormatException ignored) {
                // Ignore malformed ffprobe lines.
            }
        }

        List<AudioTrack> tracks = new ArrayList<>();
        streams.keySet().stream().sorted().forEach(audioOrder -> {
            Map<String, String> values = streams.get(audioOrder);
            int streamIndex = parseInt(values.get("index"), audioOrder);
            int channels = parseInt(values.get("channels"), 0);
            tracks.add(new AudioTrack(
                    audioOrder,
                    streamIndex,
                    values.get("codec_name"),
                    channels,
                    values.get("tags.language"),
                    values.get("tags.title")));
        });
        return tracks;
    }

    private static List<String> run(List<String> command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return List.of();
            }
            return lines;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return List.of();
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String unquote(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned.replace("\\\"", "\"");
    }
}
