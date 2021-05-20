package service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.FailCaseLog;
import object.FunctionStatus;
import object.db.InboxRecord;
import object.PushMessage;
import object.ResponseMessage;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.amazonaws.services.lambda.runtime.LambdaRuntime.getLogger;

public class SNSNotificationService {
    static final LambdaLogger logger = getLogger();
    private final static SnsClient snsClient;
    static {
        snsClient = SnsClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .httpClient(ApacheHttpClient.builder().build())
                .build();
    }

    public static FunctionStatus register(String device_token, String platformApplicationArn) {
        try {
            Consumer<software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest.Builder> consumer = new Consumer<CreatePlatformEndpointRequest.Builder>() {
                @Override
                public void accept(CreatePlatformEndpointRequest.Builder builder) {
                    builder.platformApplicationArn(platformApplicationArn);
                    builder.token(device_token);
                    builder.customUserData("LAMBDA");
                }
            };
            CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(consumer);
            logger.log("\nregister - CreatePlatformEndpointResponse response: " + response.endpointArn() + "\n");
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("endpointArn", response.endpointArn());
            return new FunctionStatus(true, result);
        } catch (SnsException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
            logger.log("\nregister failCaseLog: " + failCaseLog.convertToJsonString() + "\n");
            return new FunctionStatus(false, e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
        }
    }

    public static FunctionStatus unRegister(String arn) {
        logger.log("\nunRegister: " + arn + "\n");
        try {
            Consumer<software.amazon.awssdk.services.sns.model.DeleteEndpointRequest.Builder> consumer = new Consumer<DeleteEndpointRequest.Builder>() {
                @Override
                public void accept(DeleteEndpointRequest.Builder builder) {
                    builder.endpointArn(arn);
                }
            };
            DeleteEndpointResponse response = snsClient.deleteEndpoint(consumer);
            logger.log("\nunRegister - DeleteEndpointResponse: " + response.toString() + "\n");
            return new FunctionStatus(true, null);
        } catch (SnsException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
            logger.log("\nunRegister failCaseLog: " + failCaseLog.convertToJsonString() + "\n");
            return new FunctionStatus(false, e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
        }
    }

    public static FunctionStatus subscribe(String endpointArn, String topic) {
        try {
            Consumer<SubscribeRequest.Builder> consumer = new Consumer<SubscribeRequest.Builder>() {
                @Override
                public void accept(SubscribeRequest.Builder builder) {
                    builder.endpoint(endpointArn);
                    builder.topicArn(topic);
                    builder.protocol("application");
                }
            };
            SubscribeResponse response = snsClient.subscribe(consumer);
            logger.log("\nsubscribe - SubscribeResponse: " + response.toString() + "\n");
            return new FunctionStatus(true, null);
        } catch (SnsException e) {
            FailCaseLog failCaseLog = new FailCaseLog(e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
            logger.log("\nsubscribe failCaseLog: " + failCaseLog.convertToJsonString() + "\n");
            logger.log(e.awsErrorDetails().errorMessage());
            return new FunctionStatus(false, e.statusCode(), e.awsErrorDetails().errorMessage(), e.getMessage());
        }
    }

    public ResponseMessage unSubscribe(String endpointArn) {
        logger.log("\nunSubscribe arn: " + endpointArn + "\n");
        try {
            Consumer<software.amazon.awssdk.services.sns.model.UnsubscribeRequest.Builder> consumer = new Consumer<UnsubscribeRequest.Builder>() {
                @Override
                public void accept(UnsubscribeRequest.Builder builder) {
                    builder.subscriptionArn(endpointArn);
                }
            };
            UnsubscribeResponse response = snsClient.unsubscribe(consumer);
            logger.log("\nUnsubscribeResponse response: " + response.toString() + "\n");
            return new ResponseMessage(200, null);
        } catch (SnsException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            return new ResponseMessage(e.statusCode(), new ResponseMessage.Message(e.awsErrorDetails().errorMessage(), e.getMessage()));
        }
    }

    public ResponseMessage publishNotification(InboxRecord recordTable) {
        PushMessage pushMessage = new PushMessage(recordTable);
        String message = new Gson().toJson(pushMessage);

        ResponseMessage sns_response = ("Group".equals(recordTable.getTarget_type())) ?
                pubTopic(message, recordTable.getTarget()):
                pubTarget(message, recordTable.getTarget());
        snsClient.close();
        return sns_response;
    }

    private static ResponseMessage pubTopic(String message, String topicArn) {
        logger.log("SNSNotificationService.pubTopic - Start");
        try {
            PublishRequest request = PublishRequest.builder().topicArn(topicArn).messageStructure("json").message(message).build();
            logger.log("SNSNotificationService.pubTopic - Sending");
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

    private static ResponseMessage pubTarget(String message, String targetArn) {
        logger.log("SNSNotificationService.pubTarget -  Start");
        try {
            new GsonBuilder().setPrettyPrinting().serializeNulls();
            PublishRequest request = PublishRequest.builder().targetArn(targetArn).messageStructure("json").message(message).build();
            logger.log("SNSNotificationService.pubTarget - Sending");
            PublishResponse result = snsClient.publish(request);
            logger.log(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());
            return new ResponseMessage(result.sdkHttpResponse().statusCode(), new ResponseMessage.Message(result.messageId(), 1));
        } catch (SnsException e) {
            logger.log(e.awsErrorDetails().errorMessage());
            return new ResponseMessage(e.statusCode(), new ResponseMessage.Message(e.awsErrorDetails().errorMessage(), e.getMessage()));
        }
    }
}
