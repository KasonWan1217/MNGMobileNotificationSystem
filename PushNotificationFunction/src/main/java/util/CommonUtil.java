package util;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class CommonUtil {

    public static String getSnsTopicArn(String topic) {
        String prefix = System.getenv("SNS_ARN_PREFIX");
        return prefix + topic;
    }


}
