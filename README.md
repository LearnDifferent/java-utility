# Description

Tools and utilities, written in Java.

# Quick Start

## Clone this repository, then cd `java-utility` directory

```bash
git clone git@github.com:LearnDifferent/java-utility.git
cd java-utility
```

## Build a jar and run it

Take "Markdown Tool" for example. The directory is [Markdown](./Markdown) and the Main Class is `MarkdownTool`:

```bash
cd Markdown
javac MarkdownTool.java
jar -cfev markdownTool.jar MarkdownTool MarkdownTool.class
java -jar markdownTool.jar
```

## Build a jar with Maven and run it

Take  "Fanfou Album Download Tool" for example. The directory is [FanfouAlbumDownloadTool](./FanfouAlbumDownloadTool) :

```bash
cd FanfouAlbumDownloadTool
mvn cleanmvn
mvn cleanmvn package
mv target/FanfouAlbumTool-1.0-SNAPSHOT-jar-with-dependencies.jar .
java -jar FanfouAlbumTool-1.0-SNAPSHOT-jar-with-dependencies.jar
```

# License

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
