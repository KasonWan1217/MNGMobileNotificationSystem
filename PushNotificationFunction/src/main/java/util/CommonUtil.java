package util;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String ARN_TYPE_TOPIC = "Topic";
    public static String ARN_TYPE_PLATFORM = "Platform";

    public static String getSnsTopicArn(String topic) {
        String prefix = System.getenv("SNS_ARN_PREFIX");
        return prefix + topic;
    }

    public static String getNewAppRegId(String latest_appRegId) {
        String prefix = latest_appRegId.substring(0, 2);
        int number = Integer.parseInt(latest_appRegId.substring(2, 10)) + 1;
        String ref_num = prefix + String.format("%08d",number);
        return convertToBase36(ref_num, "9");
    }

    private static String split(String str) {
        String prefix = str.substring(0, 2);
        int splitNum = Integer.parseInt(str.substring(2, 10)) + 1;
        return prefix + String.format("%08d",splitNum);
    }

    private static String convertToBase36(String num, String mod) {
        BigInteger decimal = new BigInteger(num, 36);
        BigInteger modNum = new BigInteger(mod);
        return num + decimal.mod(modNum);
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

}
