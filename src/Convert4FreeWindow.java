import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.filechooser.FileNameExtensionFilter;

public class Convert4FreeWindow extends JFrame {
    private final JComboBox<ConversionType> conversionTypeBox = new JComboBox<>(ConversionType.values());
    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextArea logArea = new JTextArea();
    private final JCheckBox overwriteBox = new JCheckBox("Overwrite existing output file");
    private final JButton convertButton = new JButton("Convert");

    public static void open() {
        SwingUtilities.invokeLater(() -> {
            Convert4FreeWindow window = new Convert4FreeWindow();
            window.setVisible(true);
        });
    }

    private Convert4FreeWindow() {
        super("Convert4Free by Juzzreal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createButtonBar(), BorderLayout.SOUTH);
        pack();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));

        JLabel title = new JLabel("Convert4Free");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("Video and audio converter by Juzzreal");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 0, 6, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(new JLabel("Convert:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        formPanel.add(conversionTypeBox, constraints);

        conversionTypeBox.addActionListener(event -> {
            updateOutputSuggestion();
            appendLog("Mode changed to " + selectedConversionType() + ".");
        });

        addFileRow(formPanel, constraints, 1, "Input file:", inputField, "Browse...", this::chooseInputFile);
        addFileRow(formPanel, constraints, 2, "Output file:", outputField, "Save as...", this::chooseOutputFile);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        formPanel.add(overwriteBox, constraints);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setText("Ready.\nChoose a conversion mode, select an input file, then click Convert.\n");

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createButtonBar() {
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonBar.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));

        JButton helpButton = new JButton("Help");
        JButton changelogButton = new JButton("Changelog");
        JButton creditsButton = new JButton("Credits");
        JButton updateButton = new JButton("Update");

        helpButton.addActionListener(event -> showMessage("Help", helpText()));
        changelogButton.addActionListener(event -> showMessage("Changelog", changelogText()));
        creditsButton.addActionListener(event -> showMessage("Credits", creditsText()));
        updateButton.addActionListener(event -> startUpdate());
        convertButton.addActionListener(event -> startConversion());

        buttonBar.add(helpButton);
        buttonBar.add(changelogButton);
        buttonBar.add(creditsButton);
        buttonBar.add(updateButton);
        buttonBar.add(convertButton);
        return buttonBar;
    }

    private void addFileRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            JTextField textField,
            String buttonText,
            Runnable action) {
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel(label), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(textField, constraints);

        JButton button = new JButton(buttonText);
        button.addActionListener(event -> action.run());

        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(button, constraints);
    }

    private void chooseInputFile() {
        ConversionType conversionType = selectedConversionType();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                conversionType.inputExtension().toUpperCase().substring(1) + " files",
                conversionType.inputExtension().substring(1)));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File inputFile = chooser.getSelectedFile();
            inputField.setText(inputFile.getAbsolutePath());
            outputField.setText(suggestOutputPath(inputFile, conversionType));
        }
    }

    private void chooseOutputFile() {
        ConversionType conversionType = selectedConversionType();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                conversionType.outputExtension().toUpperCase().substring(1) + " files",
                conversionType.outputExtension().substring(1)));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            String path = outputFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(conversionType.outputExtension())) {
                path = path + conversionType.outputExtension();
            }
            outputField.setText(path);
        }
    }

    private String suggestOutputPath(File inputFile, ConversionType conversionType) {
        String path = inputFile.getAbsolutePath();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex >= 0) {
            return path.substring(0, dotIndex) + conversionType.outputExtension();
        }
        return path + conversionType.outputExtension();
    }

    private void updateOutputSuggestion() {
        String inputPath = inputField.getText().trim();
        if (!inputPath.isBlank()) {
            outputField.setText(suggestOutputPath(new File(inputPath), selectedConversionType()));
        }
    }

    private ConversionType selectedConversionType() {
        return (ConversionType) conversionTypeBox.getSelectedItem();
    }

    private void startConversion() {
        ConversionType conversionType = selectedConversionType();
        String inputPath = inputField.getText().trim();
        String outputPath = outputField.getText().trim();
        boolean overwrite = overwriteBox.isSelected();

        try {
            FileValidator.validateInputFile(inputPath, conversionType);
            FileValidator.validateOutputFile(outputPath, conversionType, overwrite);
        } catch (IllegalArgumentException exception) {
            showMessage("Invalid input", exception.getMessage());
            return;
        }

        convertButton.setEnabled(false);
        logArea.setText("");
        appendLog("Starting conversion...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                VideoConverter converter = new VideoConverter();
                converter.convert(Path.of(inputPath), Path.of(outputPath), conversionType, overwrite, this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                convertButton.setEnabled(true);
                try {
                    get();
                    showMessage("Done", "Conversion completed successfully.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    appendLog("Conversion failed.");
                    appendLog(cause.getMessage());
                    showMessage("Conversion failed", cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void startUpdate() {
        convertButton.setEnabled(false);
        logArea.setText("");
        appendLog("Starting update...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                GitHubUpdater updater = new GitHubUpdater();
                updater.updateFromGitHub(this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                convertButton.setEnabled(true);
                try {
                    get();
                    showMessage("Update complete", "Update complete. Restart Convert4Free to use the newest version.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    appendLog("Update failed.");
                    appendLog(cause.getMessage());
                    showMessage("Update failed", cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private String helpText() {
        return """
                Convert4Free Help

                Choose a conversion mode, select an input file and output file, then click Convert.

                Supported conversions:
                  MKV to MP4
                  MP4 to MOV
                  MP4 to MP3

                Command line:
                  java -jar Convert4Free.jar
                  java -jar Convert4Free.jar --ui
                  java -jar Convert4Free.jar --update
                  java -jar Convert4Free.jar input.mkv output.mp4
                  java -jar Convert4Free.jar input.mp4 output.mov
                  java -jar Convert4Free.jar input.mp4 output.mp3
                  java -jar Convert4Free.jar input.mkv output.mp4 --overwrite
                  java -jar Convert4Free.jar --credits
                  java -jar Convert4Free.jar --changelog
                  java -jar Convert4Free.jar --help
                """;
    }

    private String creditsText() {
        return """
                ==============================
                        Convert4Free
                ==============================

                A simple, free video converter.

                Created by Juzzreal
                AI worked on this project

                Powered by FFmpeg
                Built with Java

                Version: 0.3.0
                ==============================
                """;
    }

    private String changelogText() {
        return """
                Convert4Free Changelog

                Version 0.3.0
                - Improved MKV to MP4 for After Effects
                - Converts MKV audio to AAC stereo
                - Mixes multiple MKV audio tracks into one editing-friendly track

                Version 0.2.0
                - Added MP4 to MOV conversion
                - Added MP4 to MP3 audio extraction
                - Redesigned the desktop UI for multiple conversion modes
                - Added automatic output extension suggestions

                Version 0.1.0
                - Added MKV to MP4 conversion
                - Added audio and video stream preservation using FFmpeg
                - Added command-line input/output support
                - Added credits screen
                - Added changelog screen
                - Added basic file validation
                - Added overwrite protection
                - Added desktop UI
                - Added GitHub updater
                """;
    }
}
