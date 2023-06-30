# java-utility

Tools and utilities, written in Java.

## Table of Contents

* [Cloning the repository](#cloning-the-repository)
* [Building and Running](#building-and-running)
	* [Build a JAR file and run it](#build-a-jar-file-and-run-it) 
	* [Build a JAR file with Maven and run it](#build-a-jar-file-with-maven-and-run-it)
* [License](#license)

## Cloning the repository

Clone the repository into a local directory:

```bash
# Clone the repository
git clone git@github.com:LearnDifferent/java-utility.git

# Go into the repository
cd java-utility
```

Or you can [click here](https://github.com/LearnDifferent/java-utility/archive/refs/heads/master.zip) to download the zip file containing the code.

## Building and Running

### Compile a Java file and execute it

Take "CalibreTool" for example. The directory is [CalibreTool](./CalibreTool):

```
# Go into the directory 
cd CalibreTool

# Go into the source directory
cd src

# Compile the Java file
javac CalibreTool.java -encoding UTF-8

# Run the Java program
java CalibreTool
```

### Build a JAR file and run it

Take "Markdown Tool" for example. The directory is [Markdown](./Markdown) and the Main Class is `MarkdownTool` :

```bash
# Go into the directory 
cd Markdown

# Build a JAR file
javac MarkdownTool.java -encoding UTF-8
jar -cfev markdownTool.jar MarkdownTool MarkdownTool.class

# Run the JAR file
java -jar markdownTool.jar
```

### Build a JAR file with Maven and run it

Take "Fanfou Album Download Tool" for example. The directory is [FanfouAlbumDownloadTool](./FanfouAlbumDownloadTool) :

```bash
# Go into the directory 
cd FanfouAlbumDownloadTool

# Build a JAR file
mvn package

# Rename the JAR file and move it to current directory
mv target/FanfouAlbumDownloadTool.jar tool.jar

# Run the JAR file
java -jar tool.jar
```

Remember to use the JAR file **WITHOUT** the suffix **SNAPSHOT**.

## License

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
