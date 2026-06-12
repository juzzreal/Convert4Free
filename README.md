# Convert4Free

A simple Java video converter by Juzzreal.

Convert4Free uses FFmpeg and supports:

- MKV to MP4 for editing apps like After Effects
- MP4 to MOV
- MP4 to MP3

MKV to MP4 keeps the video stream when possible and creates one AAC stereo audio
track, so clips import more reliably into editing software.

Version 0.4.0 includes a rebuilt desktop UI and native Windows file picker
dialogs.

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
java -jar Convert4Free.jar input.mp4 output.mov
java -jar Convert4Free.jar input.mp4 output.mp3
```

Update from GitHub:

```powershell
java -jar Convert4Free.jar --update
```

## Installer

```powershell
java -jar Convert4FreeInstaller.jar
```
