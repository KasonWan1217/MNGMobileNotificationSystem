package util;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {

    public static String getSnsTopicArn(String topic) {
        String prefix = System.getenv("SnsTopicDomain");
        return prefix + topic;
    }

    public static String getSnsPlatformArn(String topic) {
        String prefix = System.getenv("SnsPlatformDomain");
        return prefix + topic;
    }

    private static String convertToBase29(String num, String mod) {
        BigInteger decimal = new BigInteger(num, 29);
        BigInteger modNum = new BigInteger(mod);
        return num + decimal.mod(modNum);
    }

    public static boolean checkAppRegId(String app_reg_id) {
        if (genCheckDigit(app_reg_id.substring(0, 10)).equals(app_reg_id.substring(10, 11))) {
            return true;
        }
        return false;
    }

    public static String getNewAppRegId(String app_reg_id, String app_id, String datetime) {
        if (app_reg_id == null || app_reg_id.isEmpty()) {
            app_reg_id = String.format(app_id + "%08d", 0);
        }
        String nextID = nextNum(app_reg_id.substring(0, 10), datetime);

        return nextID + genCheckDigit(nextID);
    }

    private static String nextNum(String str, String datetime) {
        int addNum = Integer.parseInt(datetime.substring(13, 14));

        if (addNum < 1 || addNum>7) {
            addNum = 4;
        }
        return split(str, addNum);
    }

    private static String split(String str, int num) {
        String prefix = str.substring(0, 2);
        int splitNum = Integer.parseInt(str.substring(2, 10)) + num;
        return String.format(prefix + "%08d", splitNum);
    }

    private static String genCheckDigit(String str) {
        int initNum = Integer.parseInt("BEA", 16)*12;
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            sum += Integer.parseInt(str.substring(i, i+1), 16) * (str.length()+1 - i);
        }
        sum += initNum;
        String checkDigit = Integer.toHexString(sum % 16);
        return checkDigit.toUpperCase();
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

}
