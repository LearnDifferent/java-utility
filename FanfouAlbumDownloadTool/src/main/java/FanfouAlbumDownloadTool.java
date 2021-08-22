import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FanfouAlbumDownloadTool {

    private static final Map<String, String> HEADERS;
    private static final String ALBUM_URL_PREFIX = "https://fanfou.com/album";

    static {
        // 初始化 header
        HEADERS = new HashMap<>();
        HEADERS.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        HEADERS.put("Accept-Encoding", "gzip, deflate, br");
        HEADERS.put("Accept-Language", "ja-JP,ja;q=0.9,zh-CN;q=0.8,zh;q=0.7,en-US;q=0.6,en;q=0.5");
        HEADERS.put("Cache-Control", "max-age=0");
        HEADERS.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
    }

    public static void main(String[] args) {
        runTool();
    }

    private static void runTool() {

        try (Scanner sc = new Scanner(System.in)) {
            downloadAlbums(sc);
        } catch (HttpStatusException e) {
            System.err.println("Incorrect Album Url Or Cookie: Please check again");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void downloadAlbums(Scanner sc) throws IOException, InterruptedException {
        // 获取用户输入的 URL
        String typeInAlbumUrl = getTypeInAlbumUrl(sc);
        // 获取 cookie
        Map<String, String> cookies = getCookies(sc);

        // 获取网页中，含有 Album 信息的元素
        Element albumInfo = getAlbumInfoElement(typeInAlbumUrl, cookies);

        // 获取用户名称作为 Album 名称
        String albumName = albumInfo.getElementsByTag("img").attr("alt");
        // 获取当前 Album 的 URL：
        String albumUrl = ALBUM_URL_PREFIX +
                albumInfo.getElementsByTag("a").attr("href");

        System.out.println("About to download " + albumName + "'s album: " + albumUrl);

        // 获取需要下载的页数
        int from;
        int to;
        while (true) {
            try {
                System.out.println("Download from which page ?");
                from = Integer.parseInt(sc.nextLine().trim());
                System.out.println("Download to which page ?");
                to = Integer.parseInt(sc.nextLine().trim());
                if (from <= to) {
                    break;
                }
                System.out.println("You can't download from page " + from
                        + " to page " + to + ". Please try again.");
            } catch (NumberFormatException e) {
                System.err.println("Please type in an integer");
            }
        }

        // 如果 from 小于 1，就设定为 1，
        for (int i = Math.max(from, 1); i <= to; i++) {
            // 获取当前页数的 Album 的所有照片的 URL
            List<String> photoUrls = getPhotoUrls(albumUrl + "/p." + i, cookies);
            // 下载照片
            downloadPhoto(photoUrls,albumName);
            System.out.println("Downloaded page " + i);
            // todo 输出到 log
            // 限制下载速度，防止被判定为机器人
            TimeUnit.SECONDS.sleep(3);
        }
    }

    private static String getTypeInAlbumUrl(Scanner sc) {
        String url;

        while (true) {
            System.out.println("Type in the album's URL:");
            url = sc.nextLine();
            if (albumUrlCheck(url)) {
                break;
            }
            System.err.println("URL is not valid. Please try again.");
        }

        return url;
    }

    private static boolean albumUrlCheck(String url) {
        if ("".equals(url) || url == null) {
            // 判断是否为空
            return false;
        }
        // 判断网页格式是否正确
        return url.startsWith(ALBUM_URL_PREFIX);
    }

    private static Map<String, String> getCookies(Scanner sc) {
        System.out.println("Type in Cookie:");
        String cookie = sc.nextLine();
        return getCookieMapFromCookie(cookie);
    }

    /**
     * 将 Cookie 转化为 Map 集合
     *
     * @param cookie cookie
     * @return cookie 的 Map 集合
     */
    private static Map<String, String> getCookieMapFromCookie(String cookie) {
        String[] split = cookie.split(";");
        Map<String, String> map = new HashMap<>();
        for (String s : split) {
            String[] entry = s.split("=");
            try {
                map.put(entry[0], entry[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Incorrect Cookie: Please check your cookie and try again later");
            }
        }
        return map;
    }

    private static Element getAlbumInfoElement(String typeInAlbumUrl, Map<String, String> cookies) throws IOException {
        Connection.Response response = Jsoup.connect(typeInAlbumUrl)
                .headers(HEADERS)
                .cookies(cookies)
                .timeout(10_000)
                .ignoreContentType(true)
                .execute();

        String body = response.body();
        Document document = Jsoup.parse(body);
        Elements avatar = document.getElementsByClass("avatar");
        return avatar.get(0);
    }

    private static List<String> getPhotoUrls(String currentAlbumUrl,
                                             Map<String, String> cookies) throws IOException {

        Connection.Response response = Jsoup.connect(currentAlbumUrl)
                .headers(HEADERS)
                .cookies(cookies)
                .timeout(10_000)
                .ignoreContentType(true)
                .execute();

        String body = response.body();
        Document document = Jsoup.parse(body);
        Elements photos = document.getElementsByClass("photo");

        List<String> list = new ArrayList<>();
        photos.forEach(p -> list.add(getOriginPhotoUrl(p)));
        return list;
    }

    private static String getOriginPhotoUrl(Element p) {
        String url = p.getElementsByTag("img").attr("src");

        // 饭否的照片会在 URL 地址后面加上类似 @120w_120h_1l.jpg 的字符串来展示缩略图
        // 只要去除了 @ 及后面的字符串，就是原图的 URL
        String[] split = url.split("@");
        return split[0];
    }

    private static void downloadPhoto(List<String> photoUrls, String albumName) {
        photoUrls.forEach(url -> downloadPhoto(url, albumName));
    }

    private static void downloadPhoto(String photoUrl, String albumName) {
        // 获取 URL 实体类
        URL url = null;
        try {
            url = new URL(photoUrl);
        } catch (MalformedURLException e) {
            System.err.println("URL is not valid: " + photoUrl);
        }

        try (DataInputStream in = new DataInputStream(Objects.requireNonNull(url).openStream());
             FileOutputStream fos = new FileOutputStream(getPhotoFile(photoUrl, albumName));
             DataOutputStream out = new DataOutputStream(fos)
        ) {
            byte[] buffer = new byte[2048];

            for (int len = in.read(buffer); len > 0; len = in.read(buffer)) {
                out.write(buffer, 0, len);
            }

            System.out.println("Done.");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            // todo log?
            try {
                // 限制下载速度，防止被判定为机器人
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static File getPhotoFile(String url, String parentPath) throws IOException {
        int index = url.lastIndexOf("/");
        String filename = url.substring(index + 1);
        File file = new File(parentPath, filename);

        if (file.getParentFile().mkdirs()) {
            System.out.println("Create Album Directory: " + parentPath);
        }

        if (!file.createNewFile()) {
            throw new IOException("Photo exists: " + filename);
        }

        System.out.println("Downloading: " + filename);
        return file;
    }
}
