# Convert4Free

Simple Java media converter by Juzzreal.

Convert4Free uses FFmpeg and includes 50+ presets for video, audio, web,
legacy, animation, and editing-friendly exports.

AI worked on this project.

## Run

```powershell
java -jar Convert4Free.jar
```

## Build

```powershell
javac -d out src\*.java
jar --create --file Convert4Free.jar --main-class Convert4Free -C out .
```

## Needs

- Java JDK
- FFmpeg in PATH
