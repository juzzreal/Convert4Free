import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GitHubUpdater {
    private static final String REPOSITORY_ZIP_URL =
            "https://github.com/juzzreal/Convert4Free/archive/refs/heads/main.zip";

    public void updateFromGitHub(Consumer<String> logger) {
        Path projectFolder = Path.of("").toAbsolutePath();

        log(logger, "Checking project folder...");
        if (Files.exists(projectFolder.resolve(".git"))) {
            log(logger, "Git repository found. Updating with git pull...");
            runCommand(List.of("git", "fetch", "origin"), logger);
            runCommand(List.of("git", "pull", "--ff-only", "origin", "main"), logger);
        } else {
            log(logger, "No .git folder found. Updating from GitHub ZIP...");
            updateFromZip(projectFolder, logger);
        }

        log(logger, "");
        log(logger, "Recompiling Convert4Free...");
        compileProject(projectFolder, logger);

        log(logger, "");
        log(logger, "Update complete. Restart Convert4Free to use the newest version.");
    }

    private void updateFromZip(Path projectFolder, Consumer<String> logger) {
        try {
            Path zipFile = downloadZip(logger);
            extractZipOverProject(zipFile, projectFolder);
            Files.deleteIfExists(zipFile);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ConversionException("Could not update from GitHub ZIP: " + exception.getMessage());
        }
    }

    private Path downloadZip(Consumer<String> logger) throws IOException, InterruptedException {
        log(logger, "Downloading: " + REPOSITORY_ZIP_URL);
        Path zipFile = Files.createTempFile("convert4free-update", ".zip");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(REPOSITORY_ZIP_URL)).GET().build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipFile));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GitHub download failed with HTTP status " + response.statusCode());
        }

        return zipFile;
    }

    private void extractZipOverProject(Path zipFile, Path projectFolder) throws IOException {
        try (InputStream inputStream = Files.newInputStream(zipFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = stripRootFolder(entry.getName());
                if (name.isBlank()) {
                    continue;
                }

                Path target = projectFolder.resolve(name).normalize();
                if (!target.startsWith(projectFolder)) {
                    throw new IOException("Blocked unsafe zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private String stripRootFolder(String zipEntryName) {
        int slashIndex = zipEntryName.indexOf('/');
        if (slashIndex < 0) {
            return "";
        }
        return zipEntryName.substring(slashIndex + 1);
    }

    private void compileProject(Path projectFolder, Consumer<String> logger) {
        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-d");
        command.add("out");

        try (var stream = Files.list(projectFolder.resolve("src"))) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toString)
                    .forEach(command::add);
        } catch (IOException exception) {
            throw new ConversionException("Could not list Java source files: " + exception.getMessage());
        }

        runCommand(command, logger);
    }

    private void runCommand(List<String> command, Consumer<String> logger) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log(logger, line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ConversionException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
            }
        } catch (IOException exception) {
            throw new ConversionException(
                    "Could not run command: " + String.join(" ", command) + "\n"
                            + "Make sure Git and Java JDK are installed and available in PATH.\n"
                            + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConversionException("Update was interrupted.");
        }
    }

    private void log(Consumer<String> logger, String message) {
        if (logger != null) {
            logger.accept(message);
        }
    }
}
