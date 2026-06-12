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
java -cp out Convert4Free
```

Command line conversion:

```powershell
java -cp out Convert4Free input.mkv output.mp4
```

Update from GitHub:

```powershell
java -cp out Convert4Free --update
```

## Installer

```powershell
javac Convert4FreeInstaller.java
java Convert4FreeInstaller
```
