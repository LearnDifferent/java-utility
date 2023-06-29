import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final Set<String> BOOK_SUFFIX = Set.of(".mobi", ".epub");

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
     * 失败的文件：key 为文件应该的名称（title），value 为原文件所在路径（文件所在文件夹）
     */
    public static final Map<String, String> FAIL_MAP = new HashMap<>();

    /**
     * 更改名称后输出的文件存放的路径
     */
    public static final String OUTPUT_PATH = "."
            + File.separator + "books_output" + File.separator;

    public static void main(String[] args) {
        List<File> allCurrentDirectories = getAllCurrentDirectories();

        for (File currentDirectory : allCurrentDirectories) {
            File targetParentDir = getDirectoryContainsTargetFile(currentDirectory);
            if (Objects.nonNull(targetParentDir)) {
                String title = getTitle(targetParentDir);
                copyAndRenameFile(targetParentDir, title);
            }
        }

        String messagePath = outputMessageFile();
        System.out.println("Message has been stored at: " + messagePath);
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

        return file.getAbsolutePath();
    }

    private static void copyAndRenameFile(File directory, String title) {
        File[] files = directory.listFiles();
        assert files != null;

        List<File> targetFiles = Arrays.stream(files)
                .filter(file -> {
                    String name = file.getName();
                    for (String bookSuffix : BOOK_SUFFIX) {
                        if (name.endsWith(bookSuffix)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());

        targetFiles.forEach(file -> {
            File outputFile = getOutputFile(title, file);
            copyFile(file, outputFile);
        });
    }

    private static File getOutputFile(String title, File file) {
        int index = file.getName().lastIndexOf(".");
        String suffix = file.getName().substring(index);

        String name = title + suffix;
        String pathname = OUTPUT_PATH + name;

        File outputFile = new File(pathname);
        File parentFile = outputFile.getParentFile();
        parentFile.mkdirs();
        return outputFile;
    }

    private static void copyFile(File source, File target) {
        try {
            Files.copy(source.toPath(), target.toPath());
            successCount++;
        } catch (InvalidPathException e) {
            failCount++;
            saveFailMessage(source, target);
            System.err.println("File name contains invalid characters: [Source: "
                    + source.getAbsolutePath() + ", [Target: " + target.getName() + "]");
        } catch (IOException e) {
            failCount++;
            saveFailMessage(source, target);
            System.err.println("Fail: [Source: "
                    + source.getName() + ", [Target: " + target.getName() + "]"
                    + ", [Reason: " + e.getMessage() + "]");
        }
    }

    private static void saveFailMessage(File source, File target) {
        String name = target.getName();
        int idx = name.lastIndexOf(".");
        String title = name.substring(0, idx);

        String directory = source.getParentFile().getAbsolutePath();

        FAIL_MAP.put(title, directory);
    }

    /**
     * 示范 {@link java.nio.channels.FileChannel} 的用法
     *
     * @param source 原文件
     * @param target 目标文件
     * @deprecated
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

    private static List<File> getAllCurrentDirectories() {
        File file = new File(".");
        File[] files = file.listFiles();
        assert files != null;

        return Arrays.stream(files)
                .filter(File::isDirectory)
                .collect(Collectors.toList());
    }


    /**
     * 根据父路径，查询子路径中包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径
     *
     * @param parentDirectory 父路径
     * @return 返回包含 {@link #FILE_NAME_WITH_TITLE} 文件的路径，如果没有就返回 null
     */
    private static File getDirectoryContainsTargetFile(File parentDirectory) {
        File[] filesInParentDir = parentDirectory.listFiles();
        assert filesInParentDir != null;

        for (File file : filesInParentDir) {
            if (file.isDirectory()) {
                return getDirectoryContainsTargetFile(file);
            }

            if (file.getName().equals(FILE_NAME_WITH_TITLE)) {
                return file.getParentFile();
            }
        }

        return null;
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