public class HelpScreen {
    public static void print() {
        System.out.println("Convert4Free Help");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java Convert4Free");
        System.out.println("  java Convert4Free --ui");
        System.out.println("  java Convert4Free --update");
        System.out.println("  java Convert4Free input.mkv output.mp4");
        System.out.println("  java Convert4Free input.mkv output.mp4 --overwrite");
        System.out.println("  java Convert4Free --credits");
        System.out.println("  java Convert4Free --changelog");
        System.out.println("  java Convert4Free --help");
        System.out.println();
        System.out.println("Notes:");
        System.out.println("  Convert4Free uses FFmpeg with stream copying:");
        System.out.println("  ffmpeg -i input.mkv -c copy output.mp4");
        System.out.println();
        System.out.println("  This avoids loading the video into Java memory and preserves quality");
        System.out.println("  when the video/audio codecs are compatible with MP4.");
    }
}
