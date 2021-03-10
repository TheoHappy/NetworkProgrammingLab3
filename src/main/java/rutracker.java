import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class rutracker {
    private static Map<String, String> cookies = Utilities.getCookies();
    private static Integer numberOfSearchResults;

    public static List<String> search(String searchWord) throws IOException {
        List<String> links = new ArrayList<>();
        Document page = Jsoup.connect("https://rutracker.net/forum/tracker.php?start=0&nm=" + searchWord).cookies(cookies).get();

        Element tableResult = page.selectFirst("table[id=\"tor-tbl\"]");
        Elements names = tableResult.select("tr[class=tCenter hl-tr]");
        Elements topics = tableResult.select("a[class=gen f ts-text]");
        Elements themes = tableResult.select("a[class=med tLink ts-text hl-tags bold]");
        Elements sizes = tableResult.select("a[class=small tr-dl dl-stub]");
        Element numberOfResults = page.selectFirst("p[class=med bold]");

        numberOfSearchResults = Utilities.spliter(numberOfResults.text());
        System.out.println("Number of results: " + numberOfSearchResults);

        System.out.println("Topic | Name | Size");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(i + 1 + " | " + topics.get(i).text() + " | " + themes.get(i).text() + " | " + sizes.get(i).text());
            links.add("https://rutracker.org/forum/" + themes.get(i).attr("href"));
        }

        return links;
    }

    public static void getImg(String link) throws IOException {
        Document page = Jsoup.connect(link).cookies(cookies).get();
        Element imgLink = page.select("var[class=postImg postImgAligned img-right]").first();
        System.out.println(imgLink.attr("title"));
        ImageWorker.downloadImage(imgLink.attr("title"));
    }

    public static String getRegistrationDate(String link) throws Exception {
        Document page = Jsoup.connect(link).cookies(cookies).get();
        Element table = page.selectFirst("td[class=row1 w70 vTop]");
        String text = table.text();
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can't extract date from string");
    }

    public static void getAllImagesFromPage(List<String> links) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(5);
        for (String link : links) {
            exec.submit(() -> {
                try {
                    getImg(link);
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                System.out.println(Thread.currentThread().getName());

            });
        }
        latch.await();
        exec.shutdown();
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public static String headRequest() throws IOException {
        Connection.Response resp = Jsoup.connect("https://rutracker.net/forum/index.php").method(Connection.Method.HEAD).cookies(cookies).execute();
        return resp.contentType();
    }

    public static Map<String, List<String>> optionsRequest() throws IOException {
        Connection.Response resp = Jsoup.connect("https://rutracker.net/forum/index.php").method(Connection.Method.OPTIONS).cookies(cookies).execute();
        return resp.multiHeaders();
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Scanner strScanner = new Scanner(System.in);
        System.out.println("Search: ");
        String searchWord = scanner.nextLine();
        List<String> links = search(searchWord);
        System.out.println("Pages: 1-" + (int) Math.ceil(numberOfSearchResults / (double) 50));
        System.out.println("Download 1 image or all? type 1 for 1 or 2 foar all");
        System.out.println("Choose option:");
        int option = scanner.nextInt();
        if (option == 1) {
            System.out.println("Chose number of torrent");
            int chose = scanner.nextInt();
            getImg(links.get(chose));
        }
        else if (option == 2){
            getAllImagesFromPage(links);

        }
//        getImg(links.get(option));

        System.out.println("Registration date is : " + getRegistrationDate("https://rutracker.net/forum/profile.php?mode=viewprofile&u=31291485"));
        System.out.println("Content-Type:" + headRequest());
        System.out.println("Options response:" + optionsRequest());
    }
}
