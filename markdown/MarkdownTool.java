import java.util.*;

public class MarkdownTool {

    private static String word;
    private static String id;
    private static List<String> wordList = new ArrayList<>();

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

            id = putWordToListAndGetId(word);

            System.out.println("Word: " + word);
            System.out.println("ID: " + id);

            System.out.println("Customize? (y/n, default: n)");

            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                changeIdIfNecessary(scanner);
                printResultIfEmphasis(scanner);
            } else {
                printResult();
            }

            System.out.println("Type in Another Word, or Hit Enter to Exit:");

        } while (!"".equals(word));

        generateTocIfNecessary(scanner);
    }

    private static void generateTocIfNecessary(Scanner scanner) {
        System.out.println("Generate TOC? (y/n), default: n");

        // 输入 y 的时候就打印 TOC
        if (scanner.nextLine().equalsIgnoreCase("y")) {

            printWordList();
            deleteWordsInWordList(scanner);
            printToc();
        }
    }


    private static void deleteWordsInWordList(Scanner scanner) {
        while (true) {
            System.out.println("Type in the word's number you want to delete");
            System.out.println("For Example: To delete No.1, No.2 and No.4, type: 1 2 4");
            System.out.println("If no words to delete, hit Enter");

            // 输入的 No.
            String nums = scanner.nextLine();

            if ("".equals(nums) || nums == null) {
                break;
            }

            Set<String> deleteWords = getDeleteWords(nums);

            System.out.println("Type in 'd' to delete; " +
                    "Type in 'c' to clear and reselect; " +
                    "Hit Enter to cancel delete motion and print TOC");

            String flag = scanner.nextLine().trim();

            if (flag.equalsIgnoreCase("d")) {
                wordList.removeAll(deleteWords);
                System.out.println("Deleted!");
                printWordList();
                System.out.println("Need to delete more? (y/n), default: n");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    // 只要输入的不是 y，就断开循环
                    break;
                }
            } else if (flag.equalsIgnoreCase("c")) {
                // 打印一下当前 wordList，然后继续 while 循环
                printWordList();
            } else {
                break;
            }
        }
    }

    private static Set<String> getDeleteWords(String nums) {
        // 输入的每个数字
        String[] split = nums.split(" ");
        // 需要删除的词
        Set<String> deleteWords = new HashSet<>();

        Arrays.stream(split).forEach(n -> {
            try {
                // 获取 index，如果不是就抛出 NumberFormatException
                int i = Integer.parseInt(n.trim());
                // 获取该 index 的 wordList 的值，可能会抛出 IndexOutOfBoundsException
                // 然后将该值放入 deleteWords 中
                deleteWords.add(wordList.get(i));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println(n + " is not an appropriate number.");
            }
        });

        System.out.println("=================");
        System.out.println("Words to Delete:");
        deleteWords.forEach(System.out::println);
        System.out.println("=================");
        return deleteWords;
    }

    private static void printWordList() {
        System.out.println("Here are the current words:");
        System.out.println("=================================");
        for (int i = 0; i < wordList.size(); i++) {
            System.out.println("No." + i + ": " + wordList.get(i));
        }
        System.out.println("=================================");
    }

    private static void printToc() {
        StringBuffer sb = new StringBuffer("Topics:" + System.lineSeparator());
        wordList.forEach(w -> sb.append("- ")
                .append(w.trim())
                .append(System.lineSeparator()));
        System.out.println(sb.toString());
    }

    private static String putWordToListAndGetId(String word) {

        // trim 一下
        String w = word.trim();

        // 将 word 放入列表中
        wordList.add(w);

        // 生成 id 并返回
        return w.toLowerCase().replaceAll(" ", "-");
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
}
