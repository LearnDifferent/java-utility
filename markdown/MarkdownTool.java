import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarkdownTool {

    private static String word;
    private static String id;
    // key: word, value: id
    private static final Map<String, String> TOC = new LinkedHashMap<>();
    // 上下分隔符
    private static final String SEP = "=================================";

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in);
             FileOutputStream fos = new FileOutputStream(getLogFile());
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {

            runTool(scanner, pw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getLogFile() throws IOException {
        Calendar calendar = Calendar.getInstance();
        Date time = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        String filename = "log_" + sdf.format(time) + ".txt";
        // 当前目录创建一个名为 markdown_temp 的目录，并在其中存储记录
        File file = new File("markdown_temp", filename);

        if (file.getParentFile().mkdirs() || file.createNewFile()) {
            return file;
        } else {
            throw new IOException("Fail to Create Log File");
        }
    }

    private static void runTool(Scanner scanner, PrintWriter pw) {

        System.out.println("Type in a Word:");

        do {
            word = scanner.nextLine();

            if ("".equals(word) || word == null) {
                System.out.println("Word is Empty (Hit Enter)");
                System.out.println(SEP);
                continue;
            }

            id = getId(word);

            System.out.println("Word: " + word);
            System.out.println("ID: " + id);

            System.out.println("Customize? (y/n, default: n)");

            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                changeIdIfNecessary(scanner);
                printResultIfEmphasis(scanner, pw);
            } else {
                printResult(pw);
            }

            // 将 word 和 id 加入进去
            TOC.put(word, id);

            System.out.println("Type in Another Word, or Hit Enter to Exit:");

        } while (!"".equals(word));

        generateTocIfNecessary(scanner, pw);
    }

    private static String getId(String word) {
        return word.trim().toLowerCase().replaceAll(" ", "-");
    }

    private static void changeIdIfNecessary(Scanner scanner) {

        System.out.println("Change ID? (y/n), default: n");

        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {

            System.out.println("Type in ID:");

            while (true) {
                String typeIn = scanner.nextLine();
                if (!"".equals(typeIn) && typeIn != null) {
                    id = typeIn;
                    break;
                }
                System.out.println("Please Type in ID!");
            }
        }
    }

    private static void printResultIfEmphasis(Scanner scanner, PrintWriter pw) {

        System.out.println("Emphasize Word? (y/n), default: n");

        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            String line1 = "**<span id=\"" + id + "\">" + word + "</span>**";
            String line2 = "**[" + word + "](#" + id + ")**";

            printTwoLine(pw, line1, line2);

        } else {
            printResult(pw);
        }
    }

    private static void printTwoLine(PrintWriter pw, String line1, String line2) {
        printToConsoleAndFile(line1, pw);
        // 这一行只在命令行中输出
        System.out.println(SEP);
        printToConsoleAndFile(line2, pw);
        printToConsoleAndFile(SEP, pw);
    }

    private static void printResult(PrintWriter pw) {
        String line1 = "<span id=\"" + id + "\">" + word + "</span>";
        String line2 = "[" + word + "](#" + id + ")";

        printTwoLine(pw, line1, line2);
    }

    private static void generateTocIfNecessary(Scanner scanner, PrintWriter pw) {
        System.out.println("Generate TOC? (y/n), default: y");

        // 只要不是 n，就打印 TOC
        if (!scanner.nextLine().equalsIgnoreCase("n")) {

            printCurrentToc();
            deleteElementInToc(scanner);
            printFinalToc(pw);
        }
    }

    private static void printCurrentToc() {
        System.out.println("Here are the current words and id:");
        System.out.println(SEP);
        // No.
        int[] count = {0};
        // 遍历打印
        TOC.forEach((k, v) -> System.out.println("No." + count[0]++ + " | Word: " + k + " | ID: " + v));
        System.out.println(SEP);
    }

    private static void deleteElementInToc(Scanner scanner) {
        while (true) {
            System.out.println("Type in the word's number you want to delete");
            System.out.println("For Example: To delete No.1, No.2 and No.4, type: 1 2 4");
            System.out.println("If no words to delete, hit Enter");

            // 输入的 No.
            String nums = scanner.nextLine();

            if ("".equals(nums) || nums == null) {
                break;
            }

            // 获取需要删除的 keys
            Set<String> keysToDelete = getDeleteKeys(nums);

            System.out.println("Type in 'd' to delete; " +
                    "Type in 'c' to clear and reselect; " +
                    "Hit Enter to cancel delete motion and print TOC");

            String flag = scanner.nextLine().trim();

            if (flag.equalsIgnoreCase("d")) {
                // 每个 keysToDelete 中的词，都是 toc 中需要删除的 key
                keysToDelete.forEach(key -> {
                    TOC.remove(key);
                    System.out.println("Deleted: " + key);
                });
                printCurrentToc();
                System.out.println("Type in 'd' to delete more; Hit Enter to print TOC");
                if (!scanner.nextLine().trim().equalsIgnoreCase("d")) {
                    // 只要输入的不是 d，就断开循环
                    break;
                }
            } else if (flag.equalsIgnoreCase("c")) {
                // 打印一下当前 wordList，然后继续 while 循环
                printCurrentToc();
            } else {
                break;
            }
        }
    }

    private static Set<String> getDeleteKeys(String nums) {
        // 输入的每个数字
        String[] split = nums.split(" ");

        // 将 toc 中的 key 转化为列表
        List<String> keys = new ArrayList<>(TOC.keySet());

        // 需要删除的 keys 列表，用于存储需要删除的 keys
        Set<String> keysToDelete = new HashSet<>();

        Arrays.stream(split).forEach(n -> {
            try {
                // 获取输入的数字作为 index，如果不是数字就抛出 NumberFormatException
                int index = Integer.parseInt(n.trim());
                // 获取 keys 列表中，该 index 位置的 key
                String key = keys.get(index);
                // 存储该 key 到需要删除的 keys 列表中
                keysToDelete.add(key);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println(n + " is not an appropriate number.");
            }
        });

        System.out.println(SEP);
        System.out.println("Words and IDs to Delete:");
        keysToDelete.forEach(k -> System.out.println("Word: " + k + " | ID: " + TOC.get(k)));
        System.out.println(SEP);
        return keysToDelete;
    }

    private static void printFinalToc(PrintWriter pw) {
        printToConsoleAndFile("Topics:", pw);
        TOC.forEach((k, v) -> printToConsoleAndFile("- [" + k.trim() + "](#" + v + ")", pw));
    }

    private static void printToConsoleAndFile(String line, PrintWriter pw) {
        // 在控制行打印
        System.out.println(line);
        // 放到到文件中
        pw.println(line);
        pw.flush();
    }
}
