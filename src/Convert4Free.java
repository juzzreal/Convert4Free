import java.nio.file.Path;

public class Convert4Free {
    private static final String VERSION = "0.3.0";

    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && "--ui".equalsIgnoreCase(args[0]))) {
            Convert4FreeWindow.open();
            return;
        }

        if (hasFlag(args, "--help")) {
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

        if (args.length == 1 && "--update".equalsIgnoreCase(args[0])) {
            try {
                GitHubUpdater updater = new GitHubUpdater();
                updater.updateFromGitHub(System.out::println);
            } catch (ConversionException exception) {
                System.out.println("Update failed.");
                System.out.println(exception.getMessage());
                System.exit(1);
            }
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
            ConversionType conversionType = ConversionType.fromPaths(inputPath, outputPath);
            FileValidator.validateInputFile(inputPath, conversionType);
            FileValidator.validateOutputFile(outputPath, conversionType, overwrite);

            VideoConverter converter = new VideoConverter();
            converter.convert(Path.of(inputPath), Path.of(outputPath), conversionType, overwrite);
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
