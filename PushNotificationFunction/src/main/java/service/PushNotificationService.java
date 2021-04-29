package service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.InboxRecordTable;
import object.PushMessage;
import object.ResponseMessage;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.function.Consumer;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class PushNotificationService {
    static final LambdaLogger logger = getLogger();

    public ResponseMessage publishNotification(InboxRecordTable recordTable) {
        PushMessage pushMessage = new PushMessage(recordTable);
        String message = new Gson().toJson(pushMessage);
        //AmazonSNSClient snsClient = AmazonSNSClient.builder().setRegion(Region.getRegion(Regions.AP_NORTHEAST_1));
        SnsClient snsClient = SnsClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(ApacheHttpClient.builder().build())
                .build();
        ResponseMessage sns_response = ("Group".equals(recordTable.getTargetArnType())) ?
                pubTopic(snsClient, message, recordTable.getTargetArn()):
                pubTarget(snsClient, message, recordTable.getTargetArn());
        snsClient.close();
        return sns_response;
    }

    private static ResponseMessage pubTopic(SnsClient snsClient, String message, String topicArn) {
        logger.log("PushNotificationService.pubTopic - Start");
        try {
            PublishRequest request = PublishRequest.builder().topicArn(topicArn).messageStructure("json").message(message).build();
            logger.log("PushNotificationService.pubTopic - Sending");
            PublishResponse result = snsClient.publish(request);
            logger.log(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());
            Consumer<ListSubscriptionsByTopicRequest.Builder> listSubscriptionsByTopicRequest = new Consumer<ListSubscriptionsByTopicRequest.Builder>() {
                @Override
                public void accept(ListSubscriptionsByTopicRequest.Builder builder) {
                    builder.topicArn(topicArn);
                }
            };
            int qty = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).subscriptions().size();
            logger.log("Topic pushMsg_QTY: "+qty);
            return new ResponseMessage(result.sdkHttpResponse().statusCode(), new ResponseMessage.Message(result.messageId(), qty));
        } catch (SnsException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            return new ResponseMessage(e.statusCode(), new ResponseMessage.Message(e.awsErrorDetails().errorMessage(), e.getMessage()));

        }
    }

    private static ResponseMessage pubTarget(SnsClient snsClient, String message, String targetArn) {
        logger.log("PushNotificationService.pubTarget -  Start");
        try {
            new GsonBuilder().setPrettyPrinting().serializeNulls();
            PublishRequest request = PublishRequest.builder().targetArn(targetArn).messageStructure("json").message(message).build();
            logger.log("PushNotificationService.pubTarget - Sending");
            PublishResponse result = snsClient.publish(request);
            logger.log(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());
            return new ResponseMessage(result.sdkHttpResponse().statusCode(), new ResponseMessage.Message(result.messageId(), 1));
        } catch (SnsException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            return new ResponseMessage(e.statusCode(), new ResponseMessage.Message(e.awsErrorDetails().errorMessage(), e.getMessage()));
        }
    }
}
