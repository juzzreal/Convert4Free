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
        convert(inputFile, outputFile, conversionType, overwrite, System.out::println);
    }

    public void convert(Path inputFile, Path outputFile, ConversionType conversionType, boolean overwrite, Consumer<String> logger) {
        checkFfmpegInstalled(logger);

        log(logger, "Convert4Free");
        log(logger, "Preparing " + conversionType + " conversion...");
        log(logger, "Input:  " + inputFile);
        log(logger, "Output: " + outputFile);
        log(logger, "Mode:   " + conversionType.mode());
        log(logger, "Info:   " + conversionType.description());
        log(logger, "");

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add(overwrite ? "-y" : "-n");
        command.add("-i");
        command.add(inputFile.toString());
        command.addAll(conversionType.ffmpegOptions());
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
        return switch (conversionType) {
            case MKV_TO_MP4, MP4_TO_MOV -> "Original video/audio quality was preserved by stream copying.";
            case MP4_TO_MP3 -> "Audio was extracted and encoded as MP3.";
        };
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
