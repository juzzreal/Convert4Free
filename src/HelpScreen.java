public class HelpScreen {
    public static void print() {
        System.out.println("Convert4Free Help");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar Convert4Free.jar");
        System.out.println("  java -jar Convert4Free.jar --ui");
        System.out.println("  java -jar Convert4Free.jar --update");
        System.out.println("  java -jar Convert4Free.jar input.mkv output.mp4");
        System.out.println("  java -jar Convert4Free.jar input.mp4 output.mov");
        System.out.println("  java -jar Convert4Free.jar input.mp4 output.mp3");
        System.out.println("  java -jar Convert4Free.jar input.mkv output.mp4 --overwrite");
        System.out.println("  java -jar Convert4Free.jar --credits");
        System.out.println("  java -jar Convert4Free.jar --changelog");
        System.out.println("  java -jar Convert4Free.jar --help");
        System.out.println();
        System.out.println("Supported conversions:");
        System.out.println("  MKV to MP4 (After Effects friendly)");
        System.out.println("  MP4 to MOV");
        System.out.println("  MP4 to MP3");
        System.out.println();
        System.out.println("Notes:");
        System.out.println("  MKV to MP4 creates one AAC stereo audio track for editing apps.");
        System.out.println("  Video container conversions use FFmpeg stream copying when possible.");
        System.out.println("  MP4 to MP3 extracts the audio track and encodes it as MP3.");
        System.out.println();
        System.out.println("  Java does not load the whole video into RAM.");
    }
}
