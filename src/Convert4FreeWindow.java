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
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
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

    private final JComboBox<ConversionType> outputFormatBox = new JComboBox<>();
    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextArea logArea = new JTextArea();
    private final JCheckBox overwriteBox = new JCheckBox("Replace output file if it already exists");
    private final JCheckBox fastStartBox = new JCheckBox("Optimize MP4/MOV for faster playback", true);
    private final JCheckBox stereoAudioBox = new JCheckBox("Make audio stereo", true);
    private final JCheckBox copyVideoBox = new JCheckBox("Copy video when possible", true);
    private final JComboBox<String> qualityBox = new JComboBox<>(new String[] {"Balanced", "High quality", "Small file"});
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel statusTitleLabel = new JLabel("Choose a file to begin");
    private final JLabel statusDetailLabel = new JLabel("Convert4Free will detect the format and show what you can convert it into.");
    private final JLabel inputStepLabel = new JLabel();
    private final JLabel outputStepLabel = new JLabel();
    private final JLabel convertStepLabel = new JLabel();
    private final JLabel doneStepLabel = new JLabel();
    private final JLabel detailsLabel = new JLabel("Technical details");
    private final JLabel modeDescriptionLabel = new JLabel();
    private final JButton inputButton = new JButton("Choose");
    private final JButton outputButton = new JButton("Save as");
    private final JButton convertButton = new JButton("Convert");
    private final JButton updateButton = new JButton("Update");

    private ConversionType selectedConversionType = ConversionType.MKV_TO_MP4_EDIT;
    private File selectedInputFile;

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

        setAvailableModes(new ConversionType[0]);
        updateStepLabels("active", "waiting", "waiting", "waiting");
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
        JButton changelogButton = secondaryButton("Updates");
        JButton creditsButton = secondaryButton("Credits");
        styleSecondaryButton(updateButton);

        helpButton.addActionListener(event -> showMessage("Help", helpText()));
        changelogButton.addActionListener(event -> showUpdateLogPage());
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

        body.add(sectionTitle("1. Choose input file"));
        body.add(Box.createVerticalStrut(10));
        body.add(createFilePanel());
        body.add(Box.createVerticalStrut(18));
        body.add(sectionTitle("2. Choose output format"));
        body.add(Box.createVerticalStrut(10));
        body.add(createModePanel());
        body.add(Box.createVerticalStrut(14));
        body.add(sectionTitle("3. Settings"));
        body.add(Box.createVerticalStrut(10));
        body.add(createSettingsPanel());
        body.add(Box.createVerticalStrut(14));
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

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel qualityLabel = new JLabel("Quality");
        qualityLabel.setForeground(MUTED);
        qualityBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        configureCheckBox(fastStartBox);
        configureCheckBox(stereoAudioBox);
        configureCheckBox(copyVideoBox);

        panel.add(qualityLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(qualityBox);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fastStartBox);
        panel.add(stereoAudioBox);
        panel.add(copyVideoBox);
        return panel;
    }

    private void configureCheckBox(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setForeground(TEXT);
    }

    private JPanel createModePanel() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        outputFormatBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        outputFormatBox.setEnabled(false);
        outputFormatBox.addActionListener(event -> {
            ConversionType selected = (ConversionType) outputFormatBox.getSelectedItem();
            if (selected != null) {
                selectMode(selected);
            }
        });

        modeDescriptionLabel.setForeground(MUTED);
        modeDescriptionLabel.setFont(modeDescriptionLabel.getFont().deriveFont(12f));

        wrapper.add(outputFormatBox);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(modeDescriptionLabel);
        return wrapper;
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
        outputButton.setEnabled(false);
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

        JLabel title = new JLabel("What is happening?");
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
        JPanel friendlyStatus = new JPanel();
        friendlyStatus.setOpaque(false);
        friendlyStatus.setLayout(new BoxLayout(friendlyStatus, BoxLayout.Y_AXIS));
        statusTitleLabel.setForeground(TEXT);
        statusTitleLabel.setFont(statusTitleLabel.getFont().deriveFont(Font.BOLD, 20f));
        statusDetailLabel.setForeground(MUTED);
        statusDetailLabel.setFont(statusDetailLabel.getFont().deriveFont(13f));
        friendlyStatus.add(statusTitleLabel);
        friendlyStatus.add(Box.createVerticalStrut(4));
        friendlyStatus.add(statusDetailLabel);

        JPanel stepsPanel = new JPanel();
        stepsPanel.setOpaque(false);
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        stepsPanel.add(inputStepLabel);
        stepsPanel.add(Box.createVerticalStrut(6));
        stepsPanel.add(outputStepLabel);
        stepsPanel.add(Box.createVerticalStrut(6));
        stepsPanel.add(convertStepLabel);
        stepsPanel.add(Box.createVerticalStrut(6));
        stepsPanel.add(doneStepLabel);

        detailsLabel.setForeground(MUTED);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.BOLD, 12f));

        logArea.setText("No technical messages yet.\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(friendlyStatus, BorderLayout.CENTER);
        top.add(stepsPanel, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(detailsLabel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void selectMode(ConversionType type) {
        selectedConversionType = type;
        modeDescriptionLabel.setText(type.description());
        updateOutputSuggestion();
        outputButton.setEnabled(selectedInputFile != null);
        setFriendlyStatus("Ready to convert", "Output will be created as " + type.targetLabel() + ".");
        setStatus(type.targetLabel());
        updateStepLabels("done", "active", "waiting", "waiting");
    }

    private void setAvailableModes(ConversionType[] types) {
        boolean hasModes = types.length > 0;
        selectedConversionType = hasModes ? types[0] : ConversionType.MKV_TO_MP4_EDIT;

        outputFormatBox.removeAllItems();
        for (ConversionType type : types) {
            outputFormatBox.addItem(type);
        }
        outputFormatBox.setEnabled(hasModes);

        if (hasModes) {
            outputFormatBox.setSelectedItem(types[0]);
            selectMode(types[0]);
        } else {
            modeDescriptionLabel.setText("Choose a supported input file first.");
            outputButton.setEnabled(false);
            convertButton.setEnabled(false);
            setFriendlyStatus("Choose a file to begin", "Supported inputs: " + ConversionType.supportedInputExtensions());
            setStatus("Waiting");
            updateStepLabels("active", "waiting", "waiting", "waiting");
        }
    }

    private void chooseInputFile() {
        FileDialog dialog = new FileDialog((Frame) this, "Choose a video file", FileDialog.LOAD);
        dialog.setFilenameFilter((directory, name) -> ConversionType.isSupportedInput(name));
        openNearCurrentPath(dialog, inputField.getText());
        dialog.setVisible(true);

        if (dialog.getFile() != null) {
            File inputFile = new File(dialog.getDirectory(), dialog.getFile());
            if (!ConversionType.isSupportedInput(inputFile.getName())) {
                showMessage("Unsupported file", "Choose one of these inputs: " + ConversionType.supportedInputExtensions());
                return;
            }

            selectedInputFile = inputFile;
            inputField.setText(inputFile.getAbsolutePath());
            setAvailableModes(ConversionType.forInputPath(inputFile.getName()));
            outputField.setText(suggestOutputPath(inputFile, selectedConversionType));
            outputButton.setEnabled(true);
            convertButton.setEnabled(true);
            setFriendlyStatus("File detected", inputFile.getName() + " can be converted to " + availableTargetsText() + ".");
            updateStepLabels("done", "active", "waiting", "waiting");
            appendLog("Selected input: " + inputFile.getAbsolutePath());
        }
    }

    private void chooseOutputFile() {
        if (selectedInputFile == null) {
            showMessage("Choose input first", "Please choose an input file first. Then Convert4Free can suggest the right output type.");
            return;
        }

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

    private String availableTargetsText() {
        ConversionType[] types = ConversionType.forInputPath(selectedInputFile.getName());
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < types.length; index++) {
            if (index > 0) {
                builder.append(index == types.length - 1 ? " or " : ", ");
            }
            builder.append(types[index].targetLabel());
        }
        return builder.toString();
    }

    private void startConversion() {
        ConversionType conversionType = selectedConversionType;
        String inputPath = inputField.getText().trim();
        String outputPath = outputField.getText().trim();
        boolean overwrite = overwriteBox.isSelected();
        ConversionSettings settings = currentSettings();

        try {
            FileValidator.validateInputFile(inputPath, conversionType);
            FileValidator.validateOutputFile(outputPath, conversionType, overwrite);
        } catch (IllegalArgumentException exception) {
            showMessage("Invalid input", exception.getMessage());
            return;
        }

        setBusy(true, "Converting");
        logArea.setText("");
        setFriendlyStatus("Conversion running", "FFmpeg is processing your file. You can follow details below.");
        updateStepLabels("done", "done", "active", "waiting");
        appendLog("Starting " + conversionType + "...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                VideoConverter converter = new VideoConverter();
                converter.convert(Path.of(inputPath), Path.of(outputPath), conversionType, overwrite, settings, this::publish);
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
                    setFriendlyStatus("Conversion complete", "Your new file is ready.");
                    updateStepLabels("done", "done", "done", "done");
                    showMessage("Done", "Conversion completed successfully.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    setStatus("Failed");
                    setFriendlyStatus("Conversion failed", "Check the details below for the FFmpeg message.");
                    updateStepLabels("done", "done", "error", "waiting");
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
        setFriendlyStatus("Updating Convert4Free", "Downloading the newest GitHub version and preparing the app jar.");
        updateStepLabels("done", "done", "active", "waiting");
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
                    setFriendlyStatus("Update prepared", "Close Convert4Free so the replacement can finish.");
                    updateStepLabels("done", "done", "done", "active");
                    showMessage("Update prepared", "Close Convert4Free. The updater will finish replacing the app jar after exit.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    setStatus("Update failed");
                    setFriendlyStatus("Update failed", "The app could not finish the update. Details are below.");
                    updateStepLabels("done", "done", "error", "waiting");
                    appendLog("Update failed.");
                    appendLog(cause.getMessage());
                    showMessage("Update failed", cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void setBusy(boolean busy, String status) {
        boolean hasInput = selectedInputFile != null;
        convertButton.setEnabled(!busy && hasInput && !outputField.getText().trim().isBlank());
        updateButton.setEnabled(!busy);
        inputButton.setEnabled(!busy);
        outputButton.setEnabled(!busy && hasInput);
        outputFormatBox.setEnabled(!busy && hasInput);
        qualityBox.setEnabled(!busy);
        fastStartBox.setEnabled(!busy);
        stereoAudioBox.setEnabled(!busy);
        copyVideoBox.setEnabled(!busy);
        setStatus(status);
    }

    private ConversionSettings currentSettings() {
        return new ConversionSettings(
                (String) qualityBox.getSelectedItem(),
                fastStartBox.isSelected(),
                stereoAudioBox.isSelected(),
                copyVideoBox.isSelected());
    }

    private void setStatus(String status) {
        statusLabel.setText(status);
    }

    private void setFriendlyStatus(String title, String detail) {
        statusTitleLabel.setText(title);
        statusDetailLabel.setText(detail);
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void updateStepLabels(String input, String output, String convert, String done) {
        setStepLabel(inputStepLabel, input, "1", "Input file", stepText(input, "Choose a file", "File selected", "File problem"));
        setStepLabel(outputStepLabel, output, "2", "Output", stepText(output, "Pick an output format", "Output is ready", "Output problem"));
        setStepLabel(convertStepLabel, convert, "3", "Conversion", stepText(convert, "Waiting to convert", "Conversion finished", "Conversion failed"));
        setStepLabel(doneStepLabel, done, "4", "Finish", stepText(done, "Waiting for result", "Ready to use", "Could not finish"));
    }

    private String stepText(String state, String waiting, String complete, String failed) {
        return switch (state) {
            case "active" -> waiting;
            case "done" -> complete;
            case "error" -> failed;
            default -> waiting;
        };
    }

    private void setStepLabel(JLabel label, String state, String number, String title, String text) {
        String mark = switch (state) {
            case "done" -> "OK";
            case "active" -> ">";
            case "error" -> "!";
            default -> "-";
        };
        label.setText(mark + "  " + number + ". " + title + " - " + text);
        label.setForeground(switch (state) {
            case "done" -> ACCENT_DARK;
            case "active" -> TEXT;
            case "error" -> new Color(176, 42, 55);
            default -> MUTED;
        });
        label.setFont(label.getFont().deriveFont("active".equals(state) ? Font.BOLD : Font.PLAIN, 13f));
    }

    private void showMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showUpdateLogPage() {
        JEditorPane page = new JEditorPane("text/html", updateLogHtml());
        page.setEditable(false);
        page.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(page);
        scrollPane.setPreferredSize(new Dimension(560, 430));
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));
        JOptionPane.showMessageDialog(this, scrollPane, "Convert4Free Updates", JOptionPane.PLAIN_MESSAGE);
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

                1. Choose an input file.
                2. Pick one of the possible output formats.
                3. Adjust settings if needed.
                4. Click Convert.

                Convert4Free now includes more than 50 converter presets.

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

                Version 0.5.3
                - Rebuilt the status area
                - Added clear step-by-step progress text
                - Moved technical FFmpeg output into a secondary details area

                Version 0.5.2
                - Fixed output file selection
                - Replaced hidden output buttons with a stable output format dropdown
                - Output format choices are now visible after selecting an input file

                Version 0.5.1
                - Rebuilt the installer so it installs only Convert4Free.jar
                - Installer now asks where the final jar should be placed

                Version 0.5.0
                - Added 50+ converter presets
                - Added quality and audio/video settings
                - Improved the file-first conversion flow
                - Simplified README and help text

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

    private String updateLogHtml() {
        return """
                <html>
                <head>
                  <style>
                    body { font-family: Segoe UI, Arial, sans-serif; color: #212529; background: #ffffff; margin: 18px; }
                    h1 { font-size: 24px; margin: 0 0 4px 0; }
                    .sub { color: #68717a; margin-bottom: 18px; }
                    .version { border: 1px solid #dde2e8; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
                    h2 { font-size: 16px; margin: 0 0 8px 0; color: #145b4b; }
                    ul { margin: 0; padding-left: 18px; }
                    li { margin-bottom: 5px; }
                    .badge { color: #145b4b; background: #e2f5f0; padding: 4px 8px; border-radius: 999px; }
                  </style>
                </head>
                <body>
                  <h1>Update Log</h1>
                  <div class="sub">Convert4Free <span class="badge">v%s</span></div>
                  <div class="version">
                    <h2>0.5.3</h2>
                    <ul>
                      <li>Rebuilt the status area so each step is understandable.</li>
                      <li>Added simple progress labels for input, output, conversion, and finish.</li>
                      <li>Technical FFmpeg messages are still available as details.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.5.2</h2>
                    <ul>
                      <li>Fixed the missing output format selection.</li>
                      <li>Replaced the hidden format buttons with a reliable dropdown.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.5.1</h2>
                    <ul>
                      <li>Installer now asks where Convert4Free.jar should be created.</li>
                      <li>The installer leaves only the final jar in the chosen folder.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.5.0</h2>
                    <ul>
                      <li>Added more than 50 converter presets.</li>
                      <li>Added quality, audio, and playback settings.</li>
                      <li>Made the file-first workflow clearer.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.4.1</h2>
                    <ul>
                      <li>File-first workflow: choose input first, then pick a possible output.</li>
                      <li>Automatic input detection for MKV and MP4 files.</li>
                      <li>Friendlier status area with clear next steps.</li>
                      <li>This update page is built into the app with simple HTML and CSS.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.4.0</h2>
                    <ul>
                      <li>Full desktop UI rebuild.</li>
                      <li>Native Windows file picker dialogs.</li>
                      <li>Cleaner workflow and activity area.</li>
                    </ul>
                  </div>
                  <div class="version">
                    <h2>0.3.0</h2>
                    <ul>
                      <li>After Effects friendly MKV to MP4 mode.</li>
                      <li>Creates one AAC stereo audio track from MKV sources.</li>
                    </ul>
                  </div>
                </body>
                </html>
                """.formatted(Convert4Free.VERSION);
    }
}
