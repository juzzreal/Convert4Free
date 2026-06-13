import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class Convert4FreeInstaller extends JFrame {
    private static final String REPOSITORY_ZIP_URL =
            "https://github.com/juzzreal/Convert4Free/archive/refs/heads/main.zip";

    private final JTextField targetFolderField = new JTextField(
            Path.of(System.getProperty("user.home"), "Desktop").toString());
    private final JTextArea logArea = new JTextArea();
    private final JButton installButton = new JButton("Install Convert4Free.jar");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The installer still works with Java's fallback look and feel.
            }

            Convert4FreeInstaller installer = new Convert4FreeInstaller();
            installer.setVisible(true);
        });
    }

    private Convert4FreeInstaller() {
        super("Convert4Free Installer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 420));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel intro = new JLabel("Choose where Convert4Free.jar should be created.");
        root.add(intro, BorderLayout.NORTH);
        root.add(createTargetPanel(), BorderLayout.CENTER);
        root.add(createBottomPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel createTargetPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JButton chooseButton = new JButton("Choose folder");
        chooseButton.addActionListener(event -> chooseTargetFolder());

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(targetFolderField, BorderLayout.CENTER);
        row.add(chooseButton, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setText("Ready.\nThe installer downloads GitHub source into a temporary folder, builds the app, then keeps only Convert4Free.jar in your chosen folder.\n");

        panel.add(row, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        installButton.addActionListener(event -> startInstall());
        panel.add(installButton, BorderLayout.EAST);
        return panel;
    }

    private void chooseTargetFolder() {
        JFileChooser chooser = new JFileChooser(targetFolderField.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose install folder");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetFolderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startInstall() {
        Path targetFolder = Path.of(targetFolderField.getText()).toAbsolutePath();
        installButton.setEnabled(false);
        logArea.setText("");

        SwingWorker<Path, String> worker = new SwingWorker<>() {
            @Override
            protected Path doInBackground() throws Exception {
                return install(targetFolder, this::publish);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                installButton.setEnabled(true);
                try {
                    Path jarPath = get();
                    appendLog("Done: " + jarPath);
                    JOptionPane.showMessageDialog(
                            Convert4FreeInstaller.this,
                            "Installed:\n" + jarPath,
                            "Installation complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    appendLog("Installation failed: " + cause.getMessage());
                    JOptionPane.showMessageDialog(
                            Convert4FreeInstaller.this,
                            cause.getMessage(),
                            "Installation failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private static Path install(Path targetFolder, java.util.function.Consumer<String> logger)
            throws IOException, InterruptedException {
        Files.createDirectories(targetFolder);

        Path workFolder = Files.createTempDirectory("convert4free-install-");
        try {
            Path zipFile = downloadRepository(workFolder, logger);
            Path sourceFolder = workFolder.resolve("source");
            Path buildFolder = workFolder.resolve("out");
            extractRepository(zipFile, sourceFolder, logger);
            compileProject(sourceFolder, buildFolder, logger);
            Path jarPath = targetFolder.resolve("Convert4Free.jar");
            buildProgramJar(buildFolder, jarPath, logger);
            return jarPath;
        } finally {
            deleteFolder(workFolder);
        }
    }

    private static Path downloadRepository(Path workFolder, java.util.function.Consumer<String> logger)
            throws IOException, InterruptedException {
        logger.accept("Downloading Convert4Free from GitHub...");
        Path zipFile = workFolder.resolve("source.zip");

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(REPOSITORY_ZIP_URL)).GET().build();
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(zipFile));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GitHub download failed with HTTP status " + response.statusCode());
        }

        return zipFile;
    }

    private static void extractRepository(Path zipFile, Path targetFolder, java.util.function.Consumer<String> logger)
            throws IOException {
        logger.accept("Extracting into temporary build folder...");
        Files.createDirectories(targetFolder);

        try (InputStream inputStream = Files.newInputStream(zipFile);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = stripRootFolder(entry.getName());
                if (name.isBlank()) {
                    continue;
                }

                Path target = targetFolder.resolve(name).normalize();
                if (!target.startsWith(targetFolder)) {
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

    private static void compileProject(Path sourceFolder, Path buildFolder, java.util.function.Consumer<String> logger)
            throws IOException, InterruptedException {
        logger.accept("Compiling Java files...");

        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-d");
        command.add(buildFolder.toString());

        try (var stream = Files.list(sourceFolder.resolve("src"))) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toString)
                    .forEach(command::add);
        }

        runCommand(command, sourceFolder, logger);
    }

    private static void buildProgramJar(Path buildFolder, Path jarPath, java.util.function.Consumer<String> logger)
            throws IOException, InterruptedException {
        logger.accept("Building final jar...");
        List<String> command = List.of(
                "jar",
                "--create",
                "--file",
                jarPath.toString(),
                "--main-class",
                "Convert4Free",
                "-C",
                buildFolder.toString(),
                ".");
        runCommand(command, buildFolder, logger);
        logger.accept("Only the final jar was installed. Temporary source files were removed.");
    }

    private static void runCommand(List<String> command, Path workingFolder, java.util.function.Consumer<String> logger)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingFolder.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.accept(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed with exit code " + exitCode + ": " + String.join(" ", command));
        }
    }

    private void appendLog(String line) {
        logArea.append(line + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private static void deleteFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
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
