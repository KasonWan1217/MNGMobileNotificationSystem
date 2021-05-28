import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.FunctionStatus;
import object.db.SnsAccount;
import object.db.SnsAccount.Subscriptions;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;
import service.SNSNotificationService;
import util.CommonMessage;
import util.CommonUtil;
import util.DBEnumValue.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterSnsService implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        BasicConfigurator.configure();
        final LambdaLogger logger = context.getLogger();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        ResponseMessage output = null;

        if (input != null) {
            ArrayList<FunctionStatus> fs_all = new ArrayList<FunctionStatus>();
            SnsAccount snsAccount = new Gson().fromJson(input.getBody(), SnsAccount.class);
            if (snsAccount.getApp_reg_id() == null || "".equals(snsAccount.getApp_reg_id())) {
                //Try to get Old device token value
                fs_all.add(DynamoDBService.getSnsAccount_checkStatus(snsAccount.getDevice_token(), Status.Success.toString()));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

                //Reset old Account and update status
                SnsAccount oldAccount = (SnsAccount) fs_all.get(fs_all.size() - 1).getResponse().get("snsAccount");
                //check SnsAccount is null
                logger.log("\noldAccount : " + new GsonBuilder().setPrettyPrinting().create().toJson(oldAccount));
                if (Status.Success.toString().equals(oldAccount.getActive_status())) {
                    fs_all.add(SNSNotificationService.resetAccount(oldAccount.getSubscriptions()));
                    oldAccount.setActive_status(Status.Reset.toString());
                    logger.log("\nReset Account : " + oldAccount.convertToJsonString());
                    DynamoDBService.saveData(oldAccount);
                }

                //Create new Account
                snsAccount.setActive_status(Status.Fail.toString());
                snsAccount.setCreate_datetime(CommonUtil.getCurrentTime());
                fs_all.add(DynamoDBService.getSnsAccount_LatestAppRegID(snsAccount.getApp_name()));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

                snsAccount.setApp_reg_id(CommonUtil.getNewAppRegId(fs_all.get(fs_all.size() - 1).getResponse().get("app_reg_id").toString(), snsAccount.getApp_id(), snsAccount.getCreate_datetime()));
                fs_all.add(DynamoDBService.saveData(snsAccount));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

                //Get all platform name form db for device token registration
                fs_all.add(DynamoDBService.getPlatformName(snsAccount.getApp_name(), snsAccount.getMobile_type()));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

                //Check the registerSusccess
                boolean registerSuccess = false;
                //Platform registration
                logger.log("\narrayList_platformName : " + (ArrayList<String>) fs_all.get(fs_all.size() - 1).getResponse().get("arrayList_platformName"));
                for (String platformName : (ArrayList<String>) fs_all.get(fs_all.size() - 1).getResponse().get("arrayList_platformName")) {
                    Subscriptions table_Subscriptions = new Subscriptions(platformName, "", ArnType.Platform.toString(), snsAccount.getCreate_datetime());
                    //device token registration

                    fs_all.add(SNSNotificationService.register(snsAccount.getDevice_token(), CommonUtil.getSnsPlatformArn(table_Subscriptions.getChannel_name())));
                    if (fs_all.get(fs_all.size() - 1).isStatus()) {
                        String endpoint_platform = (String) fs_all.get(fs_all.size() - 1).getResponse().get("endpointArn");
                        table_Subscriptions.setArn(endpoint_platform);

                        //endpointArn subscription
                        fs_all.add(SNSNotificationService.subscribe(endpoint_platform, CommonUtil.getSnsTopicArn(AppName.BEA_APP_Grp.toString())));
                        if (fs_all.get(fs_all.size() - 1).isStatus()) {
                            String subscriptionArn = (String) fs_all.get(fs_all.size() - 1).getResponse().get("subscriptionArn");
                            Subscriptions table_sub_topic = new Subscriptions(AppName.BEA_APP_Grp.toString(),
                                    subscriptionArn,
                                    ArnType.Topic.toString(),
                                    snsAccount.getCreate_datetime());
                            snsAccount.addSubscriptions(table_Subscriptions);
                            snsAccount.addSubscriptions(table_sub_topic);
                            registerSuccess = true;
                        } else {
                            fs_all.add(SNSNotificationService.unRegister(endpoint_platform));
                            logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                        }
                    }
                }

                if (registerSuccess) {
                    logger.log("registerSuccess");
                    snsAccount.setActive_status(Status.Success.toString());
                    //Update SubscriptionsStatus and Status.
                    fs_all.add(DynamoDBService.saveData(snsAccount));
                    if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                        logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                        return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                    }
                } else {
                    //All Platform Registration Fails OR Topic Subscription Fails
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

            }
            ResponseMessage.Message rs_msg = new ResponseMessage.Message();
            if (! CommonUtil.checkAppRegId(snsAccount.getApp_reg_id())) {
                rs_msg.setError_msg(CommonMessage.Fails_Message.AppRegId_Format_Error.toString());
                output = new ResponseMessage(410, rs_msg);
            } else {
                if (snsAccount.getApp_reg_id() != null && !snsAccount.getApp_reg_id().isEmpty()) {
                    rs_msg.setApp_reg_id(snsAccount.getApp_reg_id());
                    output = new ResponseMessage(200, rs_msg);
                } else {
                    rs_msg.setError_msg(CommonMessage.Fails_Message.AppRegId_Null_Error.toString());
                    output = new ResponseMessage(411, rs_msg);
                }
            }
{            }
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}
