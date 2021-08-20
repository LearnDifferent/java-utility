import java.util.*;

public class MarkdownTool {

    private static String word;
    private static String id;
    // key: word, value: id
    private static final Map<String, String> TOC = new LinkedHashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        runTool(scanner);
        scanner.close();
    }

    private static void runTool(Scanner scanner) {

        System.out.println("Type in a Word:");

        do {
            word = scanner.nextLine();

            if ("".equals(word) || word == null) {
                System.out.println("Word is Empty (Hit Enter)");
                System.out.println("===========================");
                continue;
            }

            id = getId(word);

            System.out.println("Word: " + word);
            System.out.println("ID: " + id);

            System.out.println("Customize? (y/n, default: n)");

            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                changeIdIfNecessary(scanner);
                printResultIfEmphasis(scanner);
            } else {
                printResult();
            }

            // 将 word 和 id 加入进去
            TOC.put(word, id);

            System.out.println("Type in Another Word, or Hit Enter to Exit:");

        } while (!"".equals(word));

        generateTocIfNecessary(scanner);
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

    private static void printResultIfEmphasis(Scanner scanner) {

        System.out.println("Emphasize Word? (y/n), default: n");

        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("**<span id=\"" + id + "\">" + word + "</span>**");
            System.out.println("====================================");
            System.out.println("**[" + word + "](#" + id + ")**");
            System.out.println("====================================");
        } else {
            printResult();
        }
    }

    private static void printResult() {
        System.out.println("<span id=\"" + id + "\">" + word + "</span>");
        System.out.println("====================================");
        System.out.println("[" + word + "](#" + id + ")");
        System.out.println("====================================");
    }

    private static void generateTocIfNecessary(Scanner scanner) {
        System.out.println("Generate TOC? (y/n), default: y");

        // 只要不是 n，就打印 TOC
        if (!scanner.nextLine().equalsIgnoreCase("n")) {

            printCurrentToc();
            deleteElementInToc(scanner);
            printToc();
        }
    }

    private static void printCurrentToc() {
        System.out.println("Here are the current words and id:");
        System.out.println("=================================");

        // No.
        int[] count = {0};

        // 遍历打印
        TOC.forEach((k, v) -> System.out.println("No." + count[0]++ + " | Word: " + k + " | ID: " + v));

        System.out.println("=================================");
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

        System.out.println("=================");
        System.out.println("Words and IDs to Delete:");
        keysToDelete.forEach(k -> System.out.println("Word: " + k + " | ID: " + TOC.get(k)));
        System.out.println("=================");
        return keysToDelete;
    }

    private static void printToc() {

        System.out.println("Topics:");
        TOC.forEach((k, v) -> System.out.println("- [" + k.trim() + "](#" + v + ")"));
    }


}
