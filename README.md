# java-utility

Tools and utilities, written in Java.

## Quick Start

### Clone and go into this repository

```bash
# Clone this repository
$ git clone git@github.com:LearnDifferent/java-utility.git

# Go into the repository
$ cd java-utility
```

### Build a JAR and run it

Take "Markdown Tool" for example. The directory is [Markdown](./Markdown) and the Main Class is `MarkdownTool`:

```bash
# Go into the directory 
$ cd Markdown

# Build a JAR file
$ javac MarkdownTool.java
$ jar -cfev markdownTool.jar MarkdownTool MarkdownTool.class

# Run the JAR file
$ java -jar markdownTool.jar
```

### Build a jar with Maven and run it

Take  "Fanfou Album Download Tool" for example. The directory is [FanfouAlbumDownloadTool](./FanfouAlbumDownloadTool) :

```bash
# Go into the directory 
$ cd FanfouAlbumDownloadTool

# Build a JAR file
$ mvn package

# Rename the JAR file and move it to current directory
$ mv target/FanfouAlbumDownloadTool.jar tool.jar

# Run the JAR file
$ java -jar tool.jar
```

## License

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
