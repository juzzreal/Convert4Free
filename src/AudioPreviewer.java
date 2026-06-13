import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class AudioPreviewer {
    private static final List<Clip> ACTIVE_CLIPS = new ArrayList<>();

    public static void play(Path inputFile, int audioOrder) {
        try {
            Path previewFile = Files.createTempFile("convert4free-preview-", ".wav");
            createPreviewFile(inputFile, audioOrder, previewFile);
            playWav(previewFile);
        } catch (Exception exception) {
            throw new ConversionException("Could not preview audio track: " + exception.getMessage());
        }
    }

    private static void createPreviewFile(Path inputFile, int audioOrder, Path previewFile)
            throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-v",
                "error",
                "-i",
                inputFile.toString(),
                "-map",
                "0:a:" + audioOrder,
                "-t",
                "15",
                "-vn",
                "-ac",
                "2",
                "-ar",
                "44100",
                "-f",
                "wav",
                previewFile.toString());

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("FFmpeg could not create preview audio.");
        }
    }

    private static void playWav(Path previewFile) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(previewFile.toFile());
        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        ACTIVE_CLIPS.add(clip);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                clip.close();
                ACTIVE_CLIPS.remove(clip);
                try {
                    Files.deleteIfExists(previewFile);
                } catch (IOException ignored) {
                    // Preview cleanup can fail if the file is still held briefly by the audio system.
                }
            }
        });
        clip.start();
    }
}
