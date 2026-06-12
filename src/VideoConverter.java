import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoConverter {
    public void convertMkvToMp4(Path inputFile, Path outputFile, boolean overwrite) {
        checkFfmpegInstalled();

        System.out.println("Convert4Free");
        System.out.println("Preparing MKV to MP4 conversion...");
        System.out.println("Input:  " + inputFile);
        System.out.println("Output: " + outputFile);
        System.out.println("Mode:   stream copy/remux when possible");
        System.out.println();

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add(overwrite ? "-y" : "-n");
        command.add("-i");
        command.add(inputFile.toString());
        command.add("-c");
        command.add("copy");
        command.add(outputFile.toString());

        System.out.println("Starting FFmpeg...");
        System.out.println("This keeps CPU, power, and RAM usage low by copying compatible streams.");
        System.out.println();

        // FFmpeg reads and writes the video files directly. Java only starts the
        // process and prints status text, so the video is never loaded into RAM.
        ProcessResult result = runCommand(command, true);

        if (result.exitCode() == 0) {
            System.out.println();
            System.out.println("Conversion completed successfully.");
            System.out.println("Original audio/video quality was preserved by stream copying.");
            return;
        }

        throw new ConversionException(
                "FFmpeg could not remux this file into MP4 using stream copy.\n"
                        + "The video or audio codec may not be compatible with the MP4 container.\n"
                        + "A future version of Convert4Free can add a re-encoding mode for this case.\n"
                        + "For now, you may need to re-encode with FFmpeg manually.\n\n"
                        + "FFmpeg exit code: " + result.exitCode() + "\n"
                        + result.lastOutputLines());
    }

    private void checkFfmpegInstalled() {
        System.out.println("Checking FFmpeg installation...");
        ProcessResult result = runCommand(List.of("ffmpeg", "-version"), false);

        if (result.exitCode() != 0) {
            throw new ConversionException(
                    "FFmpeg was not found or could not be started.\n"
                            + "Please install FFmpeg and make sure the 'ffmpeg' command is available in your PATH.");
        }

        System.out.println("FFmpeg is available.");
    }

    private ProcessResult runCommand(List<String> command, boolean showOutput) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process process = processBuilder.start();
            // FFmpeg writes most status information to stderr, so both streams
            // are read to prevent the process from getting stuck.
            OutputCollector stdout = new OutputCollector(process.getInputStream(), showOutput);
            OutputCollector stderr = new OutputCollector(process.getErrorStream(), showOutput);

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

    private static class OutputCollector implements Runnable {
        private static final int MAX_STORED_LINES = 40;

        private final InputStream inputStream;
        private final boolean showOutput;
        private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

        OutputCollector(InputStream inputStream, boolean showOutput) {
            this.inputStream = inputStream;
            this.showOutput = showOutput;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    remember(line);
                    if (showOutput) {
                        System.out.println(line);
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
