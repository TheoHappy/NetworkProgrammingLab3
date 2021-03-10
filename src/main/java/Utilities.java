import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.swing.text.Element;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;

public class Utilities {

    private static String LOGIN_INPUT = "login_username";
    private static String PASSWORD_INPUT = "login_password";
    private static String SUBMIT_BUTTON = "login";
    private static String LINK = "https://rutracker.org/forum/login.php";

    final static  String authUser = "uC8ADg1n";
    final static  String authPassword = "wWwVs3jr";

    public static Map<String,String> getCookies() {
//        System.setProperty("https.proxyUser", "uC8ADg1n");
//        System.setProperty("https.proxyPassword", "wWwVs3jr");

        Authenticator.setDefault(
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyHost", "91.239.85.148");
        System.setProperty("http.proxyPort", "62771");
        Connection.Response response = null;
        try {
            response = Jsoup.connect(LINK)
                    .referrer(LINK)
                    .data(LOGIN_INPUT, Credentials.LOGIN)
                    .data(PASSWORD_INPUT, Credentials.PASSWORD)
                    .data(SUBMIT_BUTTON, "")
                    .method(Connection.Method.POST)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.cookies();
    }
    public static int spliter(String text){
        String[] tokens = text.split(" ");
        return Integer.parseInt(tokens[2]);
    }

}
