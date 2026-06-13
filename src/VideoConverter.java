import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class VideoConverter {
    public void convert(Path inputFile, Path outputFile, ConversionType conversionType, boolean overwrite) {
        convert(inputFile, outputFile, conversionType, overwrite, ConversionSettings.defaults(), System.out::println);
    }

    public void convert(Path inputFile, Path outputFile, ConversionType conversionType, boolean overwrite, Consumer<String> logger) {
        convert(inputFile, outputFile, conversionType, overwrite, ConversionSettings.defaults(), logger);
    }

    public void convert(
            Path inputFile,
            Path outputFile,
            ConversionType conversionType,
            boolean overwrite,
            ConversionSettings settings,
            Consumer<String> logger) {
        convert(inputFile, outputFile, conversionType, overwrite, settings, logger, null);
    }

    public void convert(
            Path inputFile,
            Path outputFile,
            ConversionType conversionType,
            boolean overwrite,
            ConversionSettings settings,
            Consumer<String> logger,
            Consumer<ConversionProgress> progressListener) {
        checkFfmpegInstalled(logger);

        log(logger, "Convert4Free");
        log(logger, "Preparing " + conversionType + " conversion...");
        log(logger, "Input:  " + inputFile);
        log(logger, "Output: " + outputFile);
        log(logger, "Mode:   " + conversionType.mode());
        log(logger, "Info:   " + conversionType.description());
        log(logger, "Quality: " + settings.quality());
        log(logger, "");

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add(overwrite ? "-y" : "-n");
        command.add("-i");
        command.add(inputFile.toString());
        command.addAll(ffmpegOptions(inputFile, conversionType, settings, logger));
        if (progressListener != null) {
            command.add("-progress");
            command.add("pipe:1");
            command.add("-nostats");
        }
        command.add(outputFile.toString());

        log(logger, "Starting FFmpeg...");
        log(logger, "Convert4Free lets FFmpeg stream-process the file directly from disk.");
        log(logger, "");

        // FFmpeg reads and writes the video files directly. Java only starts the
        // process and prints status text, so the video is never loaded into RAM.
        ProcessResult result = runCommand(command, logger, progressListener);

        if (result.exitCode() == 0) {
            log(logger, "");
            log(logger, "Conversion completed successfully.");
            log(logger, successMessage(conversionType));
            return;
        }

        throw new ConversionException(
                "FFmpeg could not complete the " + conversionType + " conversion.\n"
                        + conversionType.compatibilityHelp() + "\n\n"
                        + "FFmpeg exit code: " + result.exitCode() + "\n"
                        + result.lastOutputLines());
    }

    private String successMessage(ConversionType conversionType) {
        if (conversionType.profile() == ConversionProfile.EDITING_MP4) {
            return "Created an editing-friendly MP4 with AAC stereo audio.";
        }
        if (conversionType.isAudioOnlyOutput()) {
            return "Audio conversion completed successfully.";
        }
        if (conversionType.profile() == ConversionProfile.GIF) {
            return "Animated GIF created successfully.";
        }
        return "Video conversion completed successfully.";
    }

    private List<String> ffmpegOptions(
            Path inputFile,
            ConversionType conversionType,
            ConversionSettings settings,
            Consumer<String> logger) {
        return switch (conversionType.profile()) {
            case EDITING_MP4 -> afterEffectsMp4Options(inputFile, settings, logger);
            case VIDEO_MP4, VIDEO_MOV, VIDEO_MKV, VIDEO_AVI -> h264VideoOptions(inputFile, conversionType, settings, logger);
            case VIDEO_WEBM -> webmVideoOptions(inputFile, settings, logger);
            case GIF -> gifOptions();
            case AUDIO_MP3 -> audioOptions(inputFile, "libmp3lame", settings.audioBitrate(), settings, logger);
            case AUDIO_AAC -> audioOptions(inputFile, "aac", settings.audioBitrate(), settings, logger);
            case AUDIO_WAV -> wavOptions(inputFile, settings, logger);
            case AUDIO_FLAC -> flacOptions(inputFile, settings, logger);
            case AUDIO_OGG -> audioOptions(inputFile, "libvorbis", qualityVorbisBitrate(settings), settings, logger);
        };
    }

    private List<String> afterEffectsMp4Options(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        List<String> options = new ArrayList<>();

        options.add("-map");
        options.add("0:v:0");

        if (audioTracks.isEmpty()) {
            log(logger, "No audio tracks selected. Exporting video only.");
            options.add("-c:v");
            options.add("copy");
            options.add("-an");
            options.add("-movflags");
            options.add("+faststart");
            return options;
        }

        if (audioTracks.size() == 1) {
            log(logger, "Using 1 selected audio track. Converting it to AAC stereo for editing apps.");
            options.add("-map");
            options.add("0:a:" + audioTracks.get(0));
        } else {
            log(logger, "Mixing " + audioTracks.size() + " selected audio tracks into one AAC stereo track.");
            options.add("-filter_complex");
            options.add(audioMixFilter(audioTracks));
            options.add("-map");
            options.add("[aout]");
        }

        options.add("-c:v");
        options.add(settings.copyWhenPossible() ? "copy" : "libx264");
        if (!settings.copyWhenPossible()) {
            options.add("-crf");
            options.add(settings.videoCrf());
            options.add("-preset");
            options.add("medium");
        }
        options.add("-c:a");
        options.add("aac");
        options.add("-b:a");
        options.add(settings.audioBitrate());
        addAudioChannelOptions(options, settings);
        addFastStart(options, settings);
        return options;
    }

    private List<String> h264VideoOptions(
            Path inputFile,
            ConversionType conversionType,
            ConversionSettings settings,
            Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        List<String> options = new ArrayList<>();
        options.add("-map");
        options.add("0:v:0");
        addSelectedAudioMaps(options, audioTracks);
        options.add("-c:v");
        options.add(settings.copyWhenPossible() ? "copy" : "libx264");
        if (!settings.copyWhenPossible()) {
            options.add("-crf");
            options.add(settings.videoCrf());
            options.add("-preset");
            options.add("medium");
        }
        addVideoAudioOptions(options, "aac", settings.audioBitrate(), settings, audioTracks);
        if (conversionType.profile() == ConversionProfile.VIDEO_MP4 || conversionType.profile() == ConversionProfile.VIDEO_MOV) {
            addFastStart(options, settings);
        }
        return options;
    }

    private List<String> webmVideoOptions(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        List<String> options = new ArrayList<>();
        options.add("-map");
        options.add("0:v:0");
        addSelectedAudioMaps(options, audioTracks);
        options.add("-c:v");
        options.add("libvpx-vp9");
        options.add("-crf");
        options.add(settings.videoCrf());
        options.add("-b:v");
        options.add("0");
        addVideoAudioOptions(options, "libopus", settings.audioBitrate(), settings, audioTracks);
        return options;
    }

    private List<String> gifOptions() {
        return List.of("-vf", "fps=12,scale=720:-1:flags=lanczos", "-loop", "0", "-an");
    }

    private List<String> audioOptions(
            Path inputFile,
            String codec,
            String bitrate,
            ConversionSettings settings,
            Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        if (audioTracks.isEmpty()) {
            throw new ConversionException("No audio tracks selected.");
        }
        List<String> options = new ArrayList<>();
        options.add("-vn");
        addAudioOutputMapping(options, audioTracks);
        options.add("-c:a");
        options.add(codec);
        options.add("-b:a");
        options.add(bitrate);
        addAudioChannelOptions(options, settings);
        return options;
    }

    private List<String> wavOptions(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        if (audioTracks.isEmpty()) {
            throw new ConversionException("No audio tracks selected.");
        }
        List<String> options = new ArrayList<>();
        options.add("-vn");
        addAudioOutputMapping(options, audioTracks);
        options.add("-c:a");
        options.add("pcm_s16le");
        addAudioChannelOptions(options, settings);
        return options;
    }

    private List<String> flacOptions(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        List<Integer> audioTracks = selectedAudioTracks(inputFile, settings, logger);
        if (audioTracks.isEmpty()) {
            throw new ConversionException("No audio tracks selected.");
        }
        List<String> options = new ArrayList<>();
        options.add("-vn");
        addAudioOutputMapping(options, audioTracks);
        options.add("-c:a");
        options.add("flac");
        addAudioChannelOptions(options, settings);
        return options;
    }

    private void addSelectedAudioMaps(List<String> options, List<Integer> audioTracks) {
        if (audioTracks.isEmpty()) {
            options.add("-an");
            return;
        }

        for (Integer audioTrack : audioTracks) {
            options.add("-map");
            options.add("0:a:" + audioTrack);
        }
    }

    private void addVideoAudioOptions(
            List<String> options,
            String codec,
            String bitrate,
            ConversionSettings settings,
            List<Integer> audioTracks) {
        if (audioTracks.isEmpty()) {
            return;
        }
        options.add("-c:a");
        options.add(codec);
        options.add("-b:a");
        options.add(bitrate);
        addAudioChannelOptions(options, settings);
    }

    private void addAudioOutputMapping(List<String> options, List<Integer> audioTracks) {
        if (audioTracks.size() == 1) {
            options.add("-map");
            options.add("0:a:" + audioTracks.get(0));
            return;
        }

        options.add("-filter_complex");
        options.add(audioMixFilter(audioTracks));
        options.add("-map");
        options.add("[aout]");
    }

    private void addAudioChannelOptions(List<String> options, ConversionSettings settings) {
        if (settings.stereoAudio()) {
            options.add("-ac");
            options.add("2");
        }
    }

    private void addFastStart(List<String> options, ConversionSettings settings) {
        if (settings.fastStart()) {
            options.add("-movflags");
            options.add("+faststart");
        }
    }

    private String qualityVorbisBitrate(ConversionSettings settings) {
        return switch (settings.quality()) {
            case "High quality" -> "256k";
            case "Small file" -> "96k";
            default -> "160k";
        };
    }

    private String audioMixFilter(List<Integer> audioTracks) {
        StringBuilder filter = new StringBuilder();
        for (Integer audioTrack : audioTracks) {
            filter.append("[0:a:").append(audioTrack).append("]");
        }
        filter.append("amix=inputs=")
                .append(audioTracks.size())
                .append(":duration=longest:normalize=0[aout]");
        return filter.toString();
    }

    private List<Integer> selectedAudioTracks(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        if (settings.customAudioSelection()) {
            return settings.selectedAudioTracks();
        }

        int audioStreamCount = countAudioStreams(inputFile, logger);
        List<Integer> audioTracks = new ArrayList<>();
        for (int index = 0; index < audioStreamCount; index++) {
            audioTracks.add(index);
        }
        return audioTracks;
    }

    private int countAudioStreams(Path inputFile, Consumer<String> logger) {
        log(logger, "Checking audio tracks...");
        ProcessResult result = runCommand(
                List.of(
                        "ffprobe",
                        "-v",
                        "error",
                        "-select_streams",
                        "a",
                        "-show_entries",
                        "stream=index",
                        "-of",
                        "csv=p=0",
                        inputFile.toString()),
                null);

        if (result.exitCode() != 0) {
            log(logger, "ffprobe could not count audio tracks. Falling back to first audio track.");
            return 1;
        }

        int count = 0;
        for (String line : result.outputLines()) {
            if (!line.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private void checkFfmpegInstalled(Consumer<String> logger) {
        log(logger, "Checking FFmpeg installation...");
        ProcessResult result = runCommand(List.of("ffmpeg", "-version"), null);

        if (result.exitCode() != 0) {
            throw new ConversionException(
                    "FFmpeg was not found or could not be started.\n"
                            + "Please install FFmpeg and make sure the 'ffmpeg' command is available in your PATH.");
        }

        log(logger, "FFmpeg is available.");
    }

    private ProcessResult runCommand(List<String> command, Consumer<String> logger) {
        return runCommand(command, logger, null);
    }

    private ProcessResult runCommand(
            List<String> command,
            Consumer<String> logger,
            Consumer<ConversionProgress> progressListener) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process process = processBuilder.start();
            // FFmpeg writes most status information to stderr, so both streams
            // are read to prevent the process from getting stuck.
            OutputCollector stdout = new OutputCollector(process.getInputStream(), logger, progressListener);
            OutputCollector stderr = new OutputCollector(process.getErrorStream(), logger, null);

            Thread stdoutThread = new Thread(stdout);
            Thread stderrThread = new Thread(stderr);
            stdoutThread.start();
            stderrThread.start();

            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            List<String> combinedOutput = new ArrayList<>();
            combinedOutput.addAll(stdout.lines());
            combinedOutput.addAll(stderr.lines());

            return new ProcessResult(exitCode, combinedOutput);
        } catch (IOException exception) {
            return new ProcessResult(1, List.of(exception.getMessage()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConversionException("The conversion was interrupted.");
        }
    }

    private void log(Consumer<String> logger, String message) {
        if (logger != null) {
            logger.accept(message);
        }
    }

    private static class OutputCollector implements Runnable {
        private static final int MAX_STORED_LINES = 40;

        private final InputStream inputStream;
        private final Consumer<String> logger;
        private final Consumer<ConversionProgress> progressListener;
        private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

        OutputCollector(
                InputStream inputStream,
                Consumer<String> logger,
                Consumer<ConversionProgress> progressListener) {
            this.inputStream = inputStream;
            this.logger = logger;
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    remember(line);
                    if (handleProgress(line)) {
                        continue;
                    }
                    if (logger != null) {
                        logger.accept(line);
                    }
                }
            } catch (IOException exception) {
                remember(exception.getMessage());
            }
        }

        List<String> lines() {
            return List.copyOf(lines);
        }

        private void remember(String line) {
            lines.add(line);
            if (lines.size() > MAX_STORED_LINES) {
                lines.remove(0);
            }
        }

        private boolean handleProgress(String line) {
            if (progressListener == null) {
                return false;
            }

            if (line.startsWith("out_time_us=") || line.startsWith("out_time_ms=")) {
                int equals = line.indexOf('=');
                if (equals > 0) {
                    try {
                        double seconds = Long.parseLong(line.substring(equals + 1).trim()) / 1_000_000.0;
                        progressListener.accept(new ConversionProgress(seconds));
                    } catch (NumberFormatException ignored) {
                        // Ignore malformed progress values.
                    }
                }
                return true;
            }

            return line.startsWith("progress=")
                    || line.startsWith("bitrate=")
                    || line.startsWith("speed=")
                    || line.startsWith("total_size=")
                    || line.startsWith("out_time=");
        }
    }

    private record ProcessResult(int exitCode, List<String> outputLines) {
        String lastOutputLines() {
            if (outputLines.isEmpty()) {
                return "No FFmpeg output was captured.";
            }

            return String.join(System.lineSeparator(), outputLines);
        }
    }
}
