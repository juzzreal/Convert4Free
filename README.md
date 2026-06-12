# Convert4Free

A simple Java video converter by Juzzreal.

Convert4Free uses FFmpeg and supports:

- MKV to MP4
- MP4 to MOV
- MP4 to MP3

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
