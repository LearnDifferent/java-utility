# Description

My Java tools and utilities

# Quick Start

## Clone this repository, then cd `java-utility` directory

```bash
git clone git@github.com:LearnDifferent/java-utility.git
cd java-utility
```

## Build a jar and run it

Take "Markdown Tool" for example. The directory is [markdown](./markdown) and the Main Class is `MarkdownTool`:

```bash
cd markdown
javac MarkdownTool.java
jar -cfev markdownTool.jar MarkdownTool MarkdownTool.class
java -jar markdownTool.jar
```

# License 

Released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
