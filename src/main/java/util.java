import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by liuzc on 2016/3/2.
 */

public class util {
    public static String httpGet(String urlToOpen) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToOpen);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static String getStrBetween(String str, String prev, String after){
        int startPos, endPos;
        if ((startPos = str.indexOf(prev)) >= 0){
            if ((endPos = str.indexOf(after, startPos)) > 0){
                return str.substring(startPos + prev.length(), endPos);
            }
        }
        return null;
    }

    public static int countStr(String origStr, String strToCount){
        int len = strToCount.length();
        int count = 0;
        int pos = 0;
        do {
            pos = origStr.indexOf(strToCount, pos);
            if (pos > 0) {
                pos += len;
                count++;
            }
        }while (pos > 0);
        return count;
    }

}
