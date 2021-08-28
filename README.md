# java-utility

Tools and utilities, written in Java.

## Quick Start

### Clone and go into this repository

```bash
git clone git@github.com:LearnDifferent/java-utility.git
cd java-utility
```

### Build a jar and run it

Take "Markdown Tool" for example. The directory is [Markdown](./Markdown) and the Main Class is `MarkdownTool`:

```bash
cd Markdown
javac MarkdownTool.java
jar -cfev markdownTool.jar MarkdownTool MarkdownTool.class
java -jar markdownTool.jar
```

### Build a jar with Maven and run it

Take  "Fanfou Album Download Tool" for example. The directory is [FanfouAlbumDownloadTool](./FanfouAlbumDownloadTool) :

```bash
cd FanfouAlbumDownloadTool
mvn package
mv target/FanfouAlbumDownloadTool.jar tool.jar
java -jar tool.jar
```

## License

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
