# NewPipe Extractor

[![Build Status](https://travis-ci.org/TeamNewPipe/NewPipeExtractor.svg?branch=master)](https://travis-ci.org/TeamNewPipe/NewPipeExtractor) [![JIT Pack Badge](https://jitpack.io/v/TeamNewPipe/NewPipeExtractor.svg)](https://jitpack.io/#TeamNewPipe/NewPipeExtractor) [JDoc](https://teamnewpipe.github.io/NewPipeExtractor/javadoc/) • [Documentation](https://teamnewpipe.github.io/documentation/)

NewPipe Extractor is a library for extracting things from streaming sites. It is a core component of [NewPipe](https://github.com/TeamNewPipe/NewPipe), but could be used independently.

## Usage

NewPipe Extractor is available at JitPack's Maven repo.

If you're using Gradle, you could add NewPipe Extractor as a dependency with the following steps:

1. Add `maven { url 'https://jitpack.io' }` to the `repositories` in your `build.gradle`.
2. Add `implementation 'com.github.TeamNewPipe:NewPipeExtractor:v0.19.5'`the `dependencies` in your `build.gradle`. Replace `v0.19.5` with the latest release.

### Testing changes

To test changes quickly you can build the library locally. A good approach would be to add something like the following to your `settings.gradle`:

```groovy
includeBuild('../NewPipeExtractor') {
    dependencySubstitution {
        substitute module('com.github.TeamNewPipe:NewPipeExtractor') with project(':extractor')
    }
}
```

Another approach would be to use the local Maven repository, here's a gist of how to use it:

1. Add `mavenLocal()` in your project `repositories` list (usually as the first entry to give priority above the others).
2. It's _recommended_ that you change the `version` of this library (e.g. `LOCAL_SNAPSHOT`).
3. Run gradle's `ìnstall` task to deploy this library to your local repository (using the wrapper, present in the root of this project: `./gradlew install`)
4. Change the dependency version used in your project to match the one you chose in step 2 (`implementation 'com.github.TeamNewPipe:NewPipeExtractor:LOCAL_SNAPSHOT'`)

> Tip for Android Studio users: After you make changes and run the `install` task, use the menu option `File → "Sync with File System"` to refresh the library in your project.

## Supported sites

The following sites are currently supported:

- YouTube
- SoundCloud
- MediaCCC
- PeerTube (no P2P)

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)  

NewPipe is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.  
