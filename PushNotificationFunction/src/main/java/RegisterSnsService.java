import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.FunctionStatus;
import object.db.InboxRecord;
import object.db.SNSAccount;
import object.db.SnsSubscriptions;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;
import service.SNSNotificationService;
import util.CommonUtil;
import util.DynamoDBTable;

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
        String sns_platform_domain = System.getenv("SNS_Platform_Domain");
        String sns_topic_domain = System.getenv("SNS_Topic_Domain");

        if (input != null) {
            ArrayList<FunctionStatus> fs_all = new ArrayList<FunctionStatus>();
            SNSAccount snsAccount = new Gson().fromJson(input.getBody(), SNSAccount.class);
            snsAccount.setStatus("F");
            snsAccount.setCreate_datetime(CommonUtil.getCurrentTime());
            snsAccount.setApp_reg_id(CommonUtil.getNewAppRegId(DynamoDBService.getLatestAppRegID(snsAccount.getApp_reg_id())));
            //Store the sns account record to DB
            fs_all.add(DynamoDBService.saveData(snsAccount));
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }
            //Get all app platform name form db for device token registration

            fs_all.add(DynamoDBService.getApplicationPlatform(snsAccount.getApp_name(), snsAccount.getMobile_type()));
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }

            SNSNotificationService snsNotificationService = new SNSNotificationService();
            ArrayList<Object> dynamoDB_save_arraylist = new ArrayList<Object>();
            //Check the registerSusccess
            boolean registerSuccess = false;
            //Platform registration
            for (String platformName : (ArrayList<String>) fs_all.get(fs_all.size() - 1).getResponse().get("arrayList_platformArn")) {
                SnsSubscriptions table_sub_platform = new Gson().fromJson(snsAccount.convertToJsonString(), SnsSubscriptions.class);
                table_sub_platform.setArn_type(DynamoDBTable.ARN_TYPE_PLATFORM);
                table_sub_platform.setArn(sns_platform_domain + platformName);
                //device token registration
                fs_all.add(snsNotificationService.register(snsAccount.getDevice_token(), table_sub_platform.getArn()));
                if(fs_all.get(fs_all.size()-1).isStatus()) {
                    //endpointArn subscription
                    String endpointArn = (String) fs_all.get(fs_all.size() - 1).getResponse().get("endpointArn");
                    fs_all.add(snsNotificationService.subscribe(endpointArn, sns_topic_domain + DynamoDBTable.BEA_APP_GRP + "1"));
                    if (fs_all.get(fs_all.size() - 1).isStatus()) {
                        SnsSubscriptions table_sub_topic = new Gson().fromJson(snsAccount.convertToJsonString(), SnsSubscriptions.class);
                        table_sub_topic.setArn_type(DynamoDBTable.ARN_TYPE_TOPIC);
                        table_sub_topic.setArn(sns_platform_domain + platformName);
                        dynamoDB_save_arraylist.add(table_sub_platform);
                        dynamoDB_save_arraylist.add(table_sub_topic);
                        registerSuccess = true;
                    } else {
                        fs_all.add(snsNotificationService.unRegister(endpointArn));
                        logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    }
                }
            }

            if (registerSuccess) {
                //Insert Subscription Status
                fs_all.add(DynamoDBService.saveData(dynamoDB_save_arraylist));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }

                //Update Status to T(True).
                fs_all.add(DynamoDBService.saveData(snsAccount));
                if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }
            } else {
                //All Platform Registration Fails OR Topic Subscription Fails
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }

            ResponseMessage.Message rs_msg = new ResponseMessage.Message();
            rs_msg.setApp_reg_id(snsAccount.getApp_reg_id());
            output = new ResponseMessage(200, rs_msg);
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}
