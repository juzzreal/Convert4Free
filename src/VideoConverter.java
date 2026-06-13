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
        command.add(outputFile.toString());

        log(logger, "Starting FFmpeg...");
        log(logger, "Convert4Free lets FFmpeg stream-process the file directly from disk.");
        log(logger, "");

        // FFmpeg reads and writes the video files directly. Java only starts the
        // process and prints status text, so the video is never loaded into RAM.
        ProcessResult result = runCommand(command, logger);

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
            case VIDEO_MP4, VIDEO_MOV, VIDEO_MKV, VIDEO_AVI -> h264VideoOptions(conversionType, settings);
            case VIDEO_WEBM -> webmVideoOptions(settings);
            case GIF -> gifOptions();
            case AUDIO_MP3 -> audioOptions("libmp3lame", settings.audioBitrate(), settings);
            case AUDIO_AAC -> audioOptions("aac", settings.audioBitrate(), settings);
            case AUDIO_WAV -> wavOptions(settings);
            case AUDIO_FLAC -> flacOptions(settings);
            case AUDIO_OGG -> audioOptions("libvorbis", qualityVorbisBitrate(settings), settings);
        };
    }

    private List<String> afterEffectsMp4Options(Path inputFile, ConversionSettings settings, Consumer<String> logger) {
        int audioStreamCount = countAudioStreams(inputFile, logger);
        List<String> options = new ArrayList<>();

        options.add("-map");
        options.add("0:v:0");

        if (audioStreamCount == 0) {
            log(logger, "No audio tracks found. Exporting video only.");
            options.add("-c:v");
            options.add("copy");
            options.add("-an");
            options.add("-movflags");
            options.add("+faststart");
            return options;
        }

        if (audioStreamCount == 1) {
            log(logger, "Found 1 audio track. Converting it to AAC stereo for After Effects.");
            options.add("-map");
            options.add("0:a:0");
        } else {
            log(logger, "Found " + audioStreamCount + " audio tracks. Mixing them into one AAC stereo track.");
            options.add("-filter_complex");
            options.add(audioMixFilter(audioStreamCount));
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

    private List<String> h264VideoOptions(ConversionType conversionType, ConversionSettings settings) {
        List<String> options = new ArrayList<>();
        options.add("-map");
        options.add("0:v:0");
        options.add("-map");
        options.add("0:a?");
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
        if (conversionType.profile() == ConversionProfile.VIDEO_MP4 || conversionType.profile() == ConversionProfile.VIDEO_MOV) {
            addFastStart(options, settings);
        }
        return options;
    }

    private List<String> webmVideoOptions(ConversionSettings settings) {
        List<String> options = new ArrayList<>();
        options.add("-map");
        options.add("0:v:0");
        options.add("-map");
        options.add("0:a?");
        options.add("-c:v");
        options.add("libvpx-vp9");
        options.add("-crf");
        options.add(settings.videoCrf());
        options.add("-b:v");
        options.add("0");
        options.add("-c:a");
        options.add("libopus");
        options.add("-b:a");
        options.add(settings.audioBitrate());
        return options;
    }

    private List<String> gifOptions() {
        return List.of("-vf", "fps=12,scale=720:-1:flags=lanczos", "-loop", "0", "-an");
    }

    private List<String> audioOptions(String codec, String bitrate, ConversionSettings settings) {
        List<String> options = new ArrayList<>();
        options.add("-vn");
        options.add("-c:a");
        options.add(codec);
        options.add("-b:a");
        options.add(bitrate);
        addAudioChannelOptions(options, settings);
        return options;
    }

    private List<String> wavOptions(ConversionSettings settings) {
        List<String> options = new ArrayList<>();
        options.add("-vn");
        options.add("-c:a");
        options.add("pcm_s16le");
        addAudioChannelOptions(options, settings);
        return options;
    }

    private List<String> flacOptions(ConversionSettings settings) {
        List<String> options = new ArrayList<>();
        options.add("-vn");
        options.add("-c:a");
        options.add("flac");
        addAudioChannelOptions(options, settings);
        return options;
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

    private String audioMixFilter(int audioStreamCount) {
        StringBuilder filter = new StringBuilder();
        for (int index = 0; index < audioStreamCount; index++) {
            filter.append("[0:a:").append(index).append("]");
        }
        filter.append("amix=inputs=")
                .append(audioStreamCount)
                .append(":duration=longest:normalize=0[aout]");
        return filter.toString();
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
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process process = processBuilder.start();
            // FFmpeg writes most status information to stderr, so both streams
            // are read to prevent the process from getting stuck.
            OutputCollector stdout = new OutputCollector(process.getInputStream(), logger);
            OutputCollector stderr = new OutputCollector(process.getErrorStream(), logger);

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
        private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

        OutputCollector(InputStream inputStream, Consumer<String> logger) {
            this.inputStream = inputStream;
            this.logger = logger;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    remember(line);
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
