import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import object.FunctionStatus;
import object.ResponseMessage;
import object.db.SnsAccount;
import object.db.SnsAccount.Subscription;
import object.request.SubscribeTopicRequest;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;
import service.SNSNotificationService;
import util.CommonUtil;
import util.DBEnumValue;
import util.ErrorMessageUtil;

import java.util.*;
import java.util.stream.Collectors;

import static util.ErrorMessageUtil.ErrorMessage.*;
import static util.ErrorMessageUtil.ErrorMessage.Request_Format_Error;

public class SubscribeTopic implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        String request_time = CommonUtil.getCurrentTime();
        BasicConfigurator.configure();
        final LambdaLogger logger = context.getLogger();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        ResponseMessage output;

        if (input != null) {
            Gson gson = new Gson();
            ArrayList<FunctionStatus> fs_all = new ArrayList<>();
            SubscribeTopicRequest request = gson.fromJson(input.getBody(), SubscribeTopicRequest.class);
            //Get Sns Account Info from DB
            fs_all.add(DynamoDBService.getSnsAccount_AppRegId(request.getApp_reg_id()));
            SnsAccount snsAccount = (SnsAccount) fs_all.get(fs_all.size() - 1).getResponse().get("snsAccount");
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + gson.toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(DynamoDB_Query_Error.getCode(), fs_all.get(fs_all.size() - 1).convertToMessage()).convertToJsonString());
            } else if (snsAccount == null){
                logger.log("\nError : " + gson.toJson(fs_all));
                return response.withStatusCode(200).withBody(ErrorMessageUtil.getErrorResponseMessage(AppRegId_Null_Error).convertToJsonString());
            }

            //Topic Subscription
            List<Subscription> list_newSubscription = new ArrayList<>();
            logger.log("\nsnsAccount Subscriptions : " + gson.toJson(snsAccount.getSubscriptions()));
            boolean subscriptionExists = false;
            //Try to get old subscribed record form Sns Account filtered by request's channel name
            List<Subscription> subscribed_topic = snsAccount.getSubscriptions().stream()
                    .filter(item -> item.getChannel_name().equals(request.getChannel_name()))
                    .collect(Collectors.toList());

            for (Subscription subscription : snsAccount.getSubscriptions()) {
                if (DBEnumValue.ArnType.Platform.toString().equals(subscription.getChannel_type())) {
                    logger.log("\nPlatform Name : " + CommonUtil.getSnsTopicArn(request.getChannel_name()));
                    //Try to get old subscribed record filtered by this Platform
                    List<Subscription> subscribed_channel = subscribed_topic.stream()
                            .filter(item -> item.getRef_platform_name().equals(subscription.getChannel_name()))
                            .collect(Collectors.toList());
                    if (subscribed_channel.size() < 1) {
                        //Subscribe the topic
                        fs_all.add(SNSNotificationService.subscribe(subscription.getArn(), CommonUtil.getSnsTopicArn(request.getChannel_name())));
                        if (! fs_all.get(fs_all.size() - 1).isStatus()) {
                            //Unsubscribe the new Subscriptions
                            for (Subscription newSubscription : list_newSubscription)
                                fs_all.add(SNSNotificationService.unsubscribe(newSubscription.getArn()));
                            List<FunctionStatus> filteredList = fs_all.stream().filter(entry -> !entry.isStatus()).collect(Collectors.toList());
                            List<ResponseMessage.Message> list_errorMessage = Arrays.asList(gson.fromJson(gson.toJson(filteredList), ResponseMessage.Message[].class));
                            logger.log("\nError : " + gson.toJson(list_errorMessage));
                            return response.withStatusCode(200).withBody(new ResponseMessage(Sns_Subscription_Error.getCode(), list_errorMessage).convertToJsonString());
                        }
                        //Add new Subscription to list
                        String subscriptionArn = fs_all.get(fs_all.size() - 1).getResponse().get("subscriptionArn").toString();
                        list_newSubscription.add(new Subscription(request.getChannel_name(), subscriptionArn, DBEnumValue.ArnType.Topic.toString(), subscription.getChannel_name(), CommonUtil.getCurrentTime()));
                    }
                }
            }

            if (list_newSubscription.size() > 0) {
                snsAccount.getSubscriptions().addAll(list_newSubscription);
                fs_all.add(DynamoDBService.updateData(snsAccount));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    for (Subscription subscription : list_newSubscription)
                        fs_all.add(SNSNotificationService.unsubscribe(subscription.getArn()));
                    List<FunctionStatus> filteredList = fs_all.stream().filter(entry -> !entry.isStatus()).collect(Collectors.toList());
                    List<ResponseMessage.Message> list_errorMessage = Arrays.asList(gson.fromJson(gson.toJson(filteredList), ResponseMessage.Message[].class));
                    logger.log("\nError : " + gson.toJson(list_errorMessage));
                    return response.withStatusCode(200).withBody(new ResponseMessage(DynamoDB_Update_Error.getCode(), list_errorMessage).convertToJsonString());
                }
            }
            output = new ResponseMessage(200, new ResponseMessage.Message());
        } else {
            output = new ResponseMessage(Request_Format_Error.getCode(), Request_Format_Error.getError_msg());
            logger.log("Request Error - Message: " + output.getMessage());
        }
        return response.withStatusCode(200).withBody(output.convertToJsonString());
    }
}


