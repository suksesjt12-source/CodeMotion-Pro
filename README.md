# CodeMotion Pro

Native Android starter for creating animated code-reveal videos.

## Implemented foundation
- Android code editor with language selector (Kotlin, Java, Python, JavaScript, HTML, C++, JSON).
- Preview panel for pasted source.
- 720p and 1080p export choices.
- Offline H.264 MP4 rendering through Android MediaCodec and MediaMuxer.
- GitHub Actions workflow builds a debug APK on pushes to main or manual dispatch.

## Build
Run `gradle assembleDebug` from the repository root. The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Current limitations
This is an initial functional foundation, not the full Maximum edition yet. The renderer currently creates an 8-second code-reveal video with a simple dark theme. Advanced syntax highlighting, audio tracks, editable timeline, project library, FFmpeg interoperability, 4K output, and Indonesian/English localization remain follow-up milestones. The initial export uses Android's built-in H.264 encoder and works offline without an external FFmpeg binary.
