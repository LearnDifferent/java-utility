import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 读取 Calibre 目录中的 metadata.opf 里面的 <dc:title> 标签作为书的标题，
 * 将该标题写入该路径下所有相应后缀的文件的文件名中（例如 .mobi 和 .epub）
 *
 * @author zhou
 * @date 2023/6/27
 */
public class CalibreTool {

    /**
     * 相应的后缀
     */
    public static final Set<String> BOOK_SUFFIXES = Set.of(".mobi", ".epub");

    /**
     * 含有标题的文件名
     */
    public static final String FILE_NAME_WITH_TITLE = "metadata.opf";

    /**
     * 含有标题的文件名中，标题的标签前缀
     */
    public static final String TITLE_TAG_PREFIX = "<dc:title>";

    /**
     * 标题的标签后缀
     */
    public static final String TITLE_TAG_SUFFIX = "</dc:title>";

    /**
     * 成功次数
     */
    public static int successCount = 0;

    /**
     * 失败次数
     */
    public static int failCount = 0;

    /**
     * 已经存在次数
     */
    public static int existCount = 0;

    /**
     * 失败的文件：key 为文件应该的名称（title），value 为原文件所在路径（文件所在文件夹）
     */
    public static final Map<String, String> FAIL_MAP = new HashMap<>();

    /**
     * 更改名称后输出的文件存放的路径
     */
    public static final String OUTPUT_PATH = "."
            + File.separator + "books_output" + File.separator;

    public static void main(String[] args) {

        listCurrentDirectories()
                .map(CalibreTool::getDirectoryContainsTargetFile)
                .filter(Objects::nonNull)
                .forEach(CalibreTool::copyAndRenameFile);

        String messagePath = outputMessageFile();
        System.out.println("Message has been stored at: " + messagePath);
    }

    private static void copyAndRenameFile(File targetParentDir) {
        String title = getTitle(targetParentDir);
        copyAndRenameFile(targetParentDir, title);
    }

    /**
     * 根据父路径，查询子路径中包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径
     *
     * @param parentDirectory 父路径
     * @return 返回包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径，如果没有就返回 null
     * @see #getDirectoryContainsTargetFileNormal(File)
     * @see #getDirectoryContainsTargetFileStreamParallel(File)
     * @see #getDirectoryContainsTargetFileNewDirectoryStream(File)
     */
    private static File getDirectoryContainsTargetFile(File parentDirectory) {
        return walkDirectory(parentDirectory)
                .filter(Files::isRegularFile)
                .filter(file -> FILE_NAME_WITH_TITLE.equals(file.getFileName().toString()))
                .findFirst()
                .map(Path::getParent)
                .map(Path::toFile)
                .orElse(null);
    }

    private static Stream<Path> walkDirectory(File parentDirectory) {
        try {
            return Files.walk(parentDirectory.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 根据父路径，查询子路径中包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径的初始方法
     *
     * @param parentDirectory 父路径
     * @return 返回包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径，如果没有就返回 null
     */
    private static File getDirectoryContainsTargetFileNormal(File parentDirectory) {
        File[] filesInParentDir = parentDirectory.listFiles();
        assert filesInParentDir != null;

        for (File file : filesInParentDir) {
            if (file.isDirectory()) {
                return getDirectoryContainsTargetFileNormal(file);
            }

            if (file.getName().equals(FILE_NAME_WITH_TITLE)) {
                return file.getParentFile();
            }
        }

        return null;
    }

    /**
     * {@link #getDirectoryContainsTargetFileNormal(File)} 的 Stream API 实现方式。
     * 缺点就是同一个 {@code parentDirectory} 为了区分 File 和 Directory 做出不同的动作而遍历了 2 次
     *
     * @param parentDirectory 父路径
     * @return 返回包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径，如果没有就返回 null
     */
    private static File getDirectoryContainsTargetFileStreamParallel(File parentDirectory) {
        return listFileToStream(parentDirectory)
                .parallel()
                .filter(File::isDirectory)
                .map(CalibreTool::getDirectoryContainsTargetFileStreamParallel)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() ->
                        listFileToStream(parentDirectory)
                                .filter(File::isFile)
                                .filter(file -> FILE_NAME_WITH_TITLE.equals(file.getName()))
                                .findFirst()
                                .map(File::getParentFile)
                                .orElse(null)
                );
    }

    /**
     * {@link #getDirectoryContainsTargetFileNormal(File)} 的 {@link Files#newDirectoryStream(Path)} 实现方式。
     * <p>
     * 需要在处理大型目录结构时提高性能，可以尝试使用java.nio.file.DirectoryStream接口。
     * 与Files.walk()和Files.find()方法不同，DirectoryStream接口可以在遍历过程中进行并行处理，从而加快处理速度
     * <p>
     * 在这个方法中用到了 StreamSupport.stream()：
     * StreamSupport.stream()是一个用于将Iterable、Spliterator或Iterator对象转换为流的工具方法。由于Java 8之前的集合框架中没有流的概念，因此可以使用StreamSupport.stream()方法将现有的集合或迭代器转换为流，以便在流中进行操作。
     * 例如，在使用DirectoryStream接口时，我们需要将其转换为流以便于使用流操作。可以使用StreamSupport.stream()方法将DirectoryStream对象转换为流，并使用流的方法链进行过滤、映射和查找等操作。
     * StreamSupport.stream()方法接受两个参数：一个用于提供元素的Spliterator对象和一个用于控制是否并行处理的布尔值。在默认情况下，流是顺序处理的，但是可以将第二个参数设置为true来启用并行处理，以加快处理速度。
     *
     * @param parentDirectory 父路径
     * @return 返回包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径，如果没有就返回 null
     */
    private static File getDirectoryContainsTargetFileNewDirectoryStream(File parentDirectory) {

        try (DirectoryStream<Path> dir = Files.newDirectoryStream(parentDirectory.toPath())) {
            return StreamSupport.stream(dir.spliterator(), true)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().equals(FILE_NAME_WITH_TITLE))
                    .findFirst()
                    .map(Path::getParent)
                    .map(Path::toFile)
                    .orElseGet(() -> {
                        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(parentDirectory.toPath())) {
                            return StreamSupport.stream(dirStream.spliterator(), true)
                                    .filter(Files::isDirectory)
                                    .map(directory -> getDirectoryContainsTargetFileNewDirectoryStream(
                                            directory.toFile()))
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException("读取文件失败");
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 输出执行信息
     *
     * @return 保存了执行信息的文件的路径
     */
    private static String outputMessageFile() {
        String fileName = "." + File.separator + "calibre_msg_"
                + System.currentTimeMillis() + ".txt";
        File file = new File(fileName);

        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {

            pw.println("Success：" + successCount);
            pw.println("Failure: " + failCount);
            pw.println("Already exists: " + existCount);
            pw.println("===============================================");

            FAIL_MAP.forEach((k, v) -> {
                pw.println(k);
                pw.println(v);
                pw.println("----------------------------------------------");
            });
            pw.println("===============================================");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getCanonicalPath(file);
    }

    private static void copyAndRenameFile(File directory, String title) {
        listFileToStream(directory)
                .filter(CalibreTool::checkIfFilenameEndsWithBookSuffix)
                .forEach(file -> getOutputFileAndCopyFile(title, file));
    }

    private static boolean checkIfFilenameEndsWithBookSuffix(File file) {
        String name = file.getName();
        return BOOK_SUFFIXES.stream().anyMatch(name::endsWith);
    }

    private static void getOutputFileAndCopyFile(String title, File file) {
        File outputFile = getOutputFile(title, file);
        copyFile(file, outputFile);
    }

    private static File getOutputFile(String title, File file) {
        int index = file.getName().lastIndexOf(".");
        String suffix = file.getName().substring(index);

        String name = title + suffix;
        String pathname = OUTPUT_PATH + name;

        File outputFile = new File(pathname);
        File parentFile = outputFile.getParentFile();
        try {
            Files.createDirectories(parentFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("创建目录失败");
        }
        return outputFile;
    }

    private static void copyFile(File source, File target) {
        try {
            Files.copy(source.toPath(), target.toPath());
            successCount++;
        } catch (InvalidPathException e) {
            failCount++;
            saveFailMessage(source, target);
            String reason = e.getMessage();
            System.err.println("File name contains invalid characters: [Source: "
                    + getCanonicalPath(source) + "], [Target: "
                    + target.getName() + "], [Reason: " + reason + "]");
        } catch (IOException e) {
            saveFailMessage(source, target);
            if (e instanceof FileAlreadyExistsException) {
                existCount++;
                System.out.println(target.getName() + " already exists");
                return;
            }

            failCount++;
            System.err.println("Fail: [Source: "
                    + source.getName() + ", [Target: " + target.getName() + "]"
                    + ", [Reason: " + e.getMessage() + "]");
        }
    }

    private static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    private static void saveFailMessage(File source, File target) {
        String name = target.getName();
        int idx = name.lastIndexOf(".");
        String title = name.substring(0, idx);

        File parentFile = source.getParentFile();
        String directory = getCanonicalPath(parentFile);

        FAIL_MAP.put(title, directory);
    }

    /**
     * 示范 {@link java.nio.channels.FileChannel} 的用法
     *
     * @param source 原文件
     * @param target 目标文件
     */
    private static void copyFileWithChannel(File source, File target) {
        try (FileInputStream fis = new FileInputStream(source);
             FileChannel inputChannel = fis.getChannel();
             FileOutputStream fos = new FileOutputStream(target);
             FileChannel outputChannel = fos.getChannel()) {

            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            successCount++;
        } catch (IOException e) {
            failCount++;
            saveFailMessage(source, target);
            System.err.println("Fail: [Source: "
                    + source.getName() + ", [Target: " + target.getName() + "]"
                    + ", [Reason: " + e.getMessage() + "]");
        }
    }

    private static Stream<File> listFileToStream(File file) {
        try {
            return Files.list(file.toPath())
                    .map(Path::toFile);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    private static Stream<File> listCurrentDirectories() {
        return listFileToStream(new File("."))
                .filter(File::isDirectory);
    }


    /**
     * 在 {@link #FILE_NAME_WITH_TITLE} 文件中获取需要的标题
     *
     * @param targetDir 目标文件的路径
     * @return 需要的标题
     */
    private static String getTitle(File targetDir) {
        String targetLine = getTitleLine(targetDir);
        if (Objects.isNull(targetLine) || "".equals(targetLine)) {
            return "";
        }

        // 删除标题前后的标签，只保留标题并返回
        return targetLine
                .replaceAll(TITLE_TAG_PREFIX, "")
                .replaceAll(TITLE_TAG_SUFFIX, "")
                .trim();
    }

    /**
     * 获取含有需要需要的标题的那一行
     *
     * @param targetDir 目标文件的路径
     * @return 含有需要需要的标题的那一行
     */
    private static String getTitleLine(File targetDir) {
        String path = targetDir.getPath();
        // 获取需要的文件
        File file = new File(path + File.separator + FILE_NAME_WITH_TITLE);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines
                    .filter(line -> line.contains(TITLE_TAG_PREFIX)
                            && line.contains(TITLE_TAG_SUFFIX))
                    .findFirst()
                    .orElse("");
        } catch (IOException e) {
            return "";
        }
    }
}