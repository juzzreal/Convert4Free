import java.nio.file.Path;

public class Convert4Free {
    private static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        if (args.length == 0 || hasFlag(args, "--help")) {
            HelpScreen.print();
            return;
        }

        if (args.length == 1 && "--credits".equalsIgnoreCase(args[0])) {
            CreditsScreen.print(VERSION);
            return;
        }

        if (args.length == 1 && "--changelog".equalsIgnoreCase(args[0])) {
            ChangelogScreen.print();
            return;
        }

        if (args.length < 2 || args.length > 3) {
            System.out.println("Invalid command.");
            System.out.println();
            HelpScreen.print();
            System.exit(1);
        }

        if (args.length == 3 && !"--overwrite".equalsIgnoreCase(args[2])) {
            System.out.println("Invalid option: " + args[2]);
            System.out.println();
            HelpScreen.print();
            System.exit(1);
        }

        boolean overwrite = hasFlag(args, "--overwrite");
        String inputPath = args[0];
        String outputPath = args[1];

        try {
            FileValidator.validateInputFile(inputPath);
            FileValidator.validateOutputFile(outputPath, overwrite);

            VideoConverter converter = new VideoConverter();
            converter.convertMkvToMp4(Path.of(inputPath), Path.of(outputPath), overwrite);
        } catch (ConversionException exception) {
            System.out.println("Conversion failed.");
            System.out.println(exception.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid input.");
            System.out.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
