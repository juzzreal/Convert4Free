# Convert4Free

A simple Java command-line video converter by Fady.

Convert4Free 0.1.0 converts `.mkv` files to `.mp4` files by asking FFmpeg to
copy compatible video and audio streams directly:

```text
ffmpeg -i input.mkv -c copy output.mp4
```

This is a remuxing workflow, so Java does not load the video into RAM. FFmpeg
streams the file from disk, preserving the original quality whenever the codecs
are compatible with the MP4 container.

## Requirements

- Java 21 or newer
- FFmpeg installed and available in your `PATH`

## Build

```powershell
javac -d out src\*.java
```

## Usage

Start the desktop UI:

```powershell
java -cp out Convert4Free
```

or:

```powershell
java -cp out Convert4Free --ui
```

Use the command line directly:

```powershell
java -cp out Convert4Free input.mkv output.mp4
java -cp out Convert4Free input.mkv output.mp4 --overwrite
java -cp out Convert4Free --credits
java -cp out Convert4Free --changelog
java -cp out Convert4Free --help
```

## Project Structure

```text
src/
  Convert4Free.java      Main entry point
  Convert4FreeWindow.java Desktop user interface
  VideoConverter.java    Runs FFmpeg through ProcessBuilder
  FileValidator.java     Checks input and output file paths
  CreditsScreen.java     Prints creator credits
  ChangelogScreen.java   Prints the changelog
  HelpScreen.java        Prints usage help
  ConversionException.java
```
