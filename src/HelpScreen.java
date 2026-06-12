public class HelpScreen {
    public static void print() {
        System.out.println("Convert4Free Help");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar Convert4Free.jar");
        System.out.println("  java -jar Convert4Free.jar --ui");
        System.out.println("  java -jar Convert4Free.jar --update");
        System.out.println("  java -jar Convert4Free.jar input.mkv output.mp4");
        System.out.println("  java -jar Convert4Free.jar input.mkv output.mp4 --overwrite");
        System.out.println("  java -jar Convert4Free.jar --credits");
        System.out.println("  java -jar Convert4Free.jar --changelog");
        System.out.println("  java -jar Convert4Free.jar --help");
        System.out.println();
        System.out.println("Notes:");
        System.out.println("  Convert4Free uses FFmpeg with stream copying:");
        System.out.println("  ffmpeg -i input.mkv -c copy output.mp4");
        System.out.println();
        System.out.println("  This avoids loading the video into Java memory and preserves quality");
        System.out.println("  when the video/audio codecs are compatible with MP4.");
    }
}
