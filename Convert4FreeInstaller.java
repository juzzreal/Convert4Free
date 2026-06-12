import java.io.BufferedWriter;
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
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Convert4FreeInstaller {
    private static final String REPOSITORY_ZIP_URL =
            "https://github.com/juzzreal/Convert4Free/archive/refs/heads/main.zip";
    private static final Path INSTALL_FOLDER = Path.of(System.getProperty("user.home"), "Convert4Free");

    public static void main(String[] args) {
        try {
            System.out.println("Convert4Free Installer");
            System.out.println("Source: " + REPOSITORY_ZIP_URL);
            System.out.println("Install folder: " + INSTALL_FOLDER);
            System.out.println();

            Path zipFile = downloadRepository();
            replaceInstallFolder(zipFile);
            compileProject();
            createDesktopLauncher();

            Files.deleteIfExists(zipFile);

            System.out.println();
            System.out.println("Installation complete.");
            System.out.println("Use the desktop launcher: Convert4Free starten.bat");
        } catch (Exception exception) {
            System.out.println();
            System.out.println("Installation failed.");
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static Path downloadRepository() throws IOException, InterruptedException {
        System.out.println("Downloading Convert4Free from GitHub...");
        Path zipFile = Files.createTempFile("convert4free-main", ".zip");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(REPOSITORY_ZIP_URL)).GET().build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipFile));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GitHub download failed with HTTP status " + response.statusCode());
        }

        return zipFile;
    }

    private static void replaceInstallFolder(Path zipFile) throws IOException {
        System.out.println("Extracting project...");

        if (Files.exists(INSTALL_FOLDER)) {
            deleteFolder(INSTALL_FOLDER);
        }
        Files.createDirectories(INSTALL_FOLDER);

        try (InputStream inputStream = Files.newInputStream(zipFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = stripRootFolder(entry.getName());
                if (name.isBlank()) {
                    continue;
                }

                Path target = INSTALL_FOLDER.resolve(name).normalize();
                if (!target.startsWith(INSTALL_FOLDER)) {
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

    private static String stripRootFolder(String zipEntryName) {
        int slashIndex = zipEntryName.indexOf('/');
        if (slashIndex < 0) {
            return "";
        }
        return zipEntryName.substring(slashIndex + 1);
    }

    private static void compileProject() throws IOException, InterruptedException {
        System.out.println("Compiling Java files...");

        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-d");
        command.add("out");

        try (var stream = Files.list(INSTALL_FOLDER.resolve("src"))) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toString)
                    .forEach(command::add);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(INSTALL_FOLDER.toFile());
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Compilation failed. Make sure the Java JDK is installed.");
        }
    }

    private static void createDesktopLauncher() throws IOException {
        System.out.println("Creating desktop launcher...");

        Path desktop = Path.of(System.getProperty("user.home"), "Desktop");
        if (!Files.exists(desktop)) {
            desktop = Path.of(System.getProperty("user.home"), "OneDrive", "Desktop");
        }
        Files.createDirectories(desktop);

        Path launcher = desktop.resolve("Convert4Free starten.bat");
        try (BufferedWriter writer = Files.newBufferedWriter(launcher)) {
            writer.write("@echo off");
            writer.newLine();
            writer.write("cd /d \"" + INSTALL_FOLDER + "\"");
            writer.newLine();
            writer.write("java -cp out Convert4Free");
            writer.newLine();
            writer.write("pause");
            writer.newLine();
        }
    }

    private static void deleteFolder(Path folder) throws IOException {
        try (var stream = Files.walk(folder)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }
}
