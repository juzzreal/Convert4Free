import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FileValidator {
    public static void validateInputFile(String inputPath) {
        Path inputFile = Path.of(inputPath);

        if (!Files.exists(inputFile)) {
            throw new IllegalArgumentException("Input file does not exist: " + inputPath);
        }

        if (!Files.isRegularFile(inputFile)) {
            throw new IllegalArgumentException("Input path is not a regular file: " + inputPath);
        }

        if (!hasExtension(inputPath, ".mkv")) {
            throw new IllegalArgumentException("Input file must end with .mkv");
        }
    }

    public static void validateOutputFile(String outputPath, boolean overwrite) {
        Path outputFile = Path.of(outputPath);
        Path parent = outputFile.toAbsolutePath().getParent();

        if (!hasExtension(outputPath, ".mp4")) {
            throw new IllegalArgumentException("Output file must end with .mp4");
        }

        if (parent != null && !Files.exists(parent)) {
            throw new IllegalArgumentException("Output folder does not exist: " + parent);
        }

        if (Files.exists(outputFile) && !overwrite) {
            throw new IllegalArgumentException(
                    "Output file already exists: " + outputPath + "\n"
                            + "Use --overwrite if you want to replace it.");
        }
    }

    private static boolean hasExtension(String path, String extension) {
        return path.toLowerCase(Locale.ROOT).endsWith(extension);
    }
}
