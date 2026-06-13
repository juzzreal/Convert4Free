public class ConversionProgress {
    private final double processedSeconds;

    public ConversionProgress(double processedSeconds) {
        this.processedSeconds = processedSeconds;
    }

    public double processedSeconds() {
        return processedSeconds;
    }
}
