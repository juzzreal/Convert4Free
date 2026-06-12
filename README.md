# Convert4Free

A simple Java video converter by Juzzreal.

Convert4Free converts `.mkv` files to `.mp4` files using FFmpeg. It tries to
preserve the original video and audio quality by copying compatible streams
instead of re-encoding:

```text
ffmpeg -i input.mkv -c copy output.mp4
```

## Note

AI worked on this project.

## Requirements

- Java JDK
- FFmpeg in PATH

## Build

```powershell
javac -d out src\*.java
```

## Run

Desktop UI:

```powershell
java -jar Convert4Free.jar
```

Command line conversion:

```powershell
java -jar Convert4Free.jar input.mkv output.mp4
```

Update from GitHub:

```powershell
java -jar Convert4Free.jar --update
```

## Installer

```powershell
java -jar Convert4FreeInstaller.jar
```
