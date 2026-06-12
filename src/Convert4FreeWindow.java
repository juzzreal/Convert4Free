import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Convert4FreeWindow extends JFrame {
    private static final Color BACKGROUND = new Color(246, 247, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(104, 113, 122);
    private static final Color BORDER = new Color(221, 226, 232);
    private static final Color ACCENT = new Color(28, 126, 102);
    private static final Color ACCENT_DARK = new Color(20, 91, 75);
    private static final Color ACCENT_SOFT = new Color(226, 245, 240);

    private final Map<ConversionType, JToggleButton> modeButtons = new EnumMap<>(ConversionType.class);
    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextArea logArea = new JTextArea();
    private final JCheckBox overwriteBox = new JCheckBox("Replace output file if it already exists");
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel modeDescriptionLabel = new JLabel();
    private final JButton inputButton = new JButton("Choose");
    private final JButton outputButton = new JButton("Choose");
    private final JButton convertButton = new JButton("Convert");
    private final JButton updateButton = new JButton("Update");

    private ConversionType selectedConversionType = ConversionType.MKV_TO_MP4;

    public static void open() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // If the native look and feel is unavailable, the custom layout still works.
            }

            Convert4FreeWindow window = new Convert4FreeWindow();
            window.setVisible(true);
        });
    }

    private Convert4FreeWindow() {
        super("Convert4Free");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createAppBar(), BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);
        setContentPane(root);

        selectMode(ConversionType.MKV_TO_MP4);
        pack();
    }

    private JPanel createAppBar() {
        JPanel appBar = new JPanel(new BorderLayout());
        appBar.setBackground(SURFACE);
        appBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(18, 24, 18, 24)));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Convert4Free");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));

        JLabel subtitle = new JLabel("Clean video conversion for editing and everyday use");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton helpButton = secondaryButton("Help");
        JButton changelogButton = secondaryButton("Changelog");
        JButton creditsButton = secondaryButton("Credits");
        styleSecondaryButton(updateButton);

        helpButton.addActionListener(event -> showMessage("Help", helpText()));
        changelogButton.addActionListener(event -> showMessage("Changelog", changelogText()));
        creditsButton.addActionListener(event -> showMessage("Credits", creditsText()));
        updateButton.addActionListener(event -> startUpdate());

        actions.add(versionBadge());
        actions.add(helpButton);
        actions.add(changelogButton);
        actions.add(creditsButton);
        actions.add(updateButton);

        appBar.add(titlePanel, BorderLayout.WEST);
        appBar.add(actions, BorderLayout.EAST);
        return appBar;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(22, 24, 24, 24));

        content.add(createWorkflowPanel(), BorderLayout.WEST);
        content.add(createStatusPanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createWorkflowPanel() {
        JPanel panel = cardPanel();
        panel.setPreferredSize(new Dimension(400, 560));
        panel.setLayout(new BorderLayout(0, 18));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(sectionTitle("1. Choose format"));
        body.add(Box.createVerticalStrut(10));
        body.add(createModePanel());
        body.add(Box.createVerticalStrut(18));
        body.add(sectionTitle("2. Pick files"));
        body.add(Box.createVerticalStrut(10));
        body.add(createFilePanel());
        body.add(Box.createVerticalStrut(12));
        body.add(overwriteBox);
        body.add(Box.createVerticalStrut(14));

        overwriteBox.setOpaque(false);
        overwriteBox.setForeground(TEXT);

        stylePrimaryButton(convertButton);
        convertButton.setPreferredSize(new Dimension(360, 46));
        convertButton.addActionListener(event -> startConversion());
        body.add(convertButton);

        panel.add(body, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createModePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        for (ConversionType type : ConversionType.values()) {
            JToggleButton button = modeButton(type);
            modeButtons.put(type, button);
            group.add(button);
            panel.add(button);
        }

        modeDescriptionLabel.setForeground(MUTED);
        modeDescriptionLabel.setFont(modeDescriptionLabel.getFont().deriveFont(12f));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(panel);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(modeDescriptionLabel);
        return wrapper;
    }

    private JToggleButton modeButton(ConversionType type) {
        JToggleButton button = new JToggleButton(type.toString());
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(JToggleButton.LEFT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        button.addActionListener(event -> selectMode(type));
        return button;
    }

    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        inputField.setText("");
        outputField.setText("");
        styleTextField(inputField);
        styleTextField(outputField);
        styleSecondaryButton(inputButton);
        styleSecondaryButton(outputButton);

        inputButton.addActionListener(event -> chooseInputFile());
        outputButton.addActionListener(event -> chooseOutputFile());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addFileRow(panel, constraints, 0, "Input", inputField, inputButton);
        addFileRow(panel, constraints, 1, "Output", outputField, outputButton);
        return panel;
    }

    private void addFileRow(
            JPanel panel,
            GridBagConstraints constraints,
            int row,
            String label,
            JTextField textField,
            JButton button) {
        JLabel fileLabel = new JLabel(label);
        fileLabel.setForeground(MUTED);

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.gridwidth = 1;
        panel.add(fileLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(textField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(button, constraints);
    }

    private JPanel createStatusPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Activity");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        statusLabel.setOpaque(true);
        statusLabel.setForeground(ACCENT_DARK);
        statusLabel.setBackground(ACCENT_SOFT);
        statusLabel.setBorder(new EmptyBorder(6, 12, 6, 12));

        header.add(title, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setForeground(new Color(39, 44, 50));
        logArea.setBackground(new Color(250, 251, 252));
        logArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        logArea.setText("Ready.\nPick a format, choose your files, then convert.\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void selectMode(ConversionType type) {
        selectedConversionType = type;
        for (Map.Entry<ConversionType, JToggleButton> entry : modeButtons.entrySet()) {
            boolean selected = entry.getKey() == type;
            JToggleButton button = entry.getValue();
            button.setSelected(selected);
            button.setBackground(selected ? ACCENT_SOFT : SURFACE);
            button.setForeground(selected ? ACCENT_DARK : TEXT);
            button.setBorder(new CompoundBorder(
                    new LineBorder(selected ? ACCENT : BORDER, selected ? 2 : 1, true),
                    new EmptyBorder(selected ? 11 : 12, selected ? 13 : 14, selected ? 11 : 12, selected ? 13 : 14)));
        }

        modeDescriptionLabel.setText(type.description());
        updateOutputSuggestion();
        setStatus("Ready");
    }

    private void chooseInputFile() {
        ConversionType conversionType = selectedConversionType;
        FileDialog dialog = new FileDialog((Frame) this, "Choose " + conversionType.inputExtension() + " file", FileDialog.LOAD);
        dialog.setFilenameFilter((directory, name) -> name.toLowerCase().endsWith(conversionType.inputExtension()));
        openNearCurrentPath(dialog, inputField.getText());
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            File inputFile = new File(dialog.getDirectory(), dialog.getFile());
            inputField.setText(inputFile.getAbsolutePath());
            outputField.setText(suggestOutputPath(inputFile, conversionType));
            appendLog("Selected input: " + inputFile.getAbsolutePath());
        }
    }

    private void chooseOutputFile() {
        ConversionType conversionType = selectedConversionType;
        FileDialog dialog = new FileDialog((Frame) this, "Save " + conversionType.outputExtension() + " file", FileDialog.SAVE);
        dialog.setFile(defaultOutputName(conversionType));
        openNearCurrentPath(dialog, outputField.getText().isBlank() ? inputField.getText() : outputField.getText());
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            String path = new File(dialog.getDirectory(), dialog.getFile()).getAbsolutePath();
            if (!path.toLowerCase().endsWith(conversionType.outputExtension())) {
                path = path + conversionType.outputExtension();
            }
            outputField.setText(path);
            appendLog("Selected output: " + path);
        }
    }

    private void openNearCurrentPath(FileDialog dialog, String pathText) {
        if (pathText == null || pathText.isBlank()) {
            return;
        }

        File file = new File(pathText);
        File folder = file.isDirectory() ? file : file.getParentFile();
        if (folder != null && folder.exists()) {
            dialog.setDirectory(folder.getAbsolutePath());
        }
    }

    private String defaultOutputName(ConversionType conversionType) {
        String inputPath = inputField.getText().trim();
        if (inputPath.isBlank()) {
            return "output" + conversionType.outputExtension();
        }
        return new File(suggestOutputPath(new File(inputPath), conversionType)).getName();
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
            outputField.setText(suggestOutputPath(new File(inputPath), selectedConversionType));
        }
    }

    private void startConversion() {
        ConversionType conversionType = selectedConversionType;
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

        setBusy(true, "Converting");
        logArea.setText("");
        appendLog("Starting " + conversionType + "...");

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
                setBusy(false, "Ready");
                try {
                    get();
                    setStatus("Complete");
                    showMessage("Done", "Conversion completed successfully.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    setStatus("Failed");
                    appendLog("Conversion failed.");
                    appendLog(cause.getMessage());
                    showMessage("Conversion failed", cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void startUpdate() {
        setBusy(true, "Updating");
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
                setBusy(false, "Ready");
                try {
                    get();
                    setStatus("Update ready");
                    showMessage("Update prepared", "Close Convert4Free. The updater will finish replacing the app jar after exit.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    setStatus("Update failed");
                    appendLog("Update failed.");
                    appendLog(cause.getMessage());
                    showMessage("Update failed", cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void setBusy(boolean busy, String status) {
        convertButton.setEnabled(!busy);
        updateButton.setEnabled(!busy);
        inputButton.setEnabled(!busy);
        outputButton.setEnabled(!busy);
        for (JToggleButton button : modeButtons.values()) {
            button.setEnabled(!busy);
        }
        setStatus(status);
    }

    private void setStatus(String status) {
        statusLabel.setText(status);
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        return label;
    }

    private JLabel versionBadge() {
        JLabel badge = new JLabel("v" + Convert4Free.VERSION);
        badge.setOpaque(true);
        badge.setForeground(ACCENT_DARK);
        badge.setBackground(ACCENT_SOFT);
        badge.setBorder(new EmptyBorder(7, 11, 7, 11));
        return badge;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        styleSecondaryButton(button);
        return button;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(SURFACE);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTextField(JTextField field) {
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(9, 10, 9, 10)));
    }

    private String helpText() {
        return """
                Convert4Free Help

                1. Choose a conversion mode.
                2. Pick an input file.
                3. Confirm the output file.
                4. Click Convert.

                Supported conversions:
                  MKV to MP4 for After Effects
                  MP4 to MOV
                  MP4 to MP3

                Command line:
                  java -jar Convert4Free.jar input.mkv output.mp4
                  java -jar Convert4Free.jar input.mp4 output.mov
                  java -jar Convert4Free.jar input.mp4 output.mp3
                """;
    }

    private String creditsText() {
        return """
                Convert4Free

                Created by Juzzreal
                AI worked on this project

                Powered by FFmpeg
                Built with Java

                Version: %s
                """.formatted(Convert4Free.VERSION);
    }

    private String changelogText() {
        return """
                Convert4Free Changelog

                Version 0.4.0
                - Fully rebuilt the desktop UI
                - Added native Windows file picker dialogs
                - Improved the conversion workflow and status area

                Version 0.3.0
                - Improved MKV to MP4 for After Effects
                - Converts MKV audio to AAC stereo
                - Mixes multiple MKV audio tracks into one editing-friendly track

                Version 0.2.0
                - Added MP4 to MOV conversion
                - Added MP4 to MP3 audio extraction
                - Redesigned the desktop UI for multiple conversion modes
                - Added automatic output extension suggestions
                """;
    }
}
