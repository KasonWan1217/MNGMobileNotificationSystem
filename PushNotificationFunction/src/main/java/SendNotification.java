import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.google.gson.GsonBuilder;
import object.FunctionStatus;
import object.db.SnsAccount.Subscriptions;
import service.DynamoDBService;
import service.SNSNotificationService;
import object.db.InboxRecord;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;
import util.CommonUtil;
import util.DBEnumValue;

/**
 * Handler for requests to Lambda function.
 */
public class SendNotification implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        String request_time = CommonUtil.getCurrentTime();
        BasicConfigurator.configure();
        final LambdaLogger logger = context.getLogger();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        ResponseMessage output = null;

        if (input != null) {
            ArrayList<FunctionStatus> fs_all = new ArrayList<FunctionStatus>();
            InboxRecord recordTable = new InboxRecord(input.getBody());
            recordTable.setCreate_datetime(request_time);
            ArrayList<String> msg_id_arrayList = new ArrayList<String>();
            int msg_qty = 0;

            //Get all app platform name form db for device token registration
            if (DBEnumValue.TargetType.Personal.toString().equals(recordTable.getTarget_type())) {
                fs_all.add(DynamoDBService.getSubscriptionsList(recordTable.getTarget()));
                if(! fs_all.get(fs_all.size()-1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }
                for (Subscriptions subscriptions : (List<Subscriptions>) fs_all.get(fs_all.size() - 1).getResponse().get("arrayList_channelName")) {
                    if (DBEnumValue.ArnType.Platform.toString().equals(subscriptions.getChannel_type())) {
                        fs_all.add(new SNSNotificationService().publishNotification(recordTable, subscriptions.getArn()));
                        if (!fs_all.get(fs_all.size() - 1).isStatus()) {
                            logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                            return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                        }
                        msg_id_arrayList.add(fs_all.get(fs_all.size() - 1).getResponse().get("msg_id").toString());
                        msg_qty++;
                    }
                }
            } else {
                fs_all.add(new SNSNotificationService().publishNotification(recordTable, CommonUtil.getSnsTopicArn(recordTable.getTarget())));
                if(! fs_all.get(fs_all.size()-1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }
                msg_id_arrayList.add(fs_all.get(fs_all.size()-1).getResponse().get("msg_id").toString());
                msg_qty = (int) fs_all.get(fs_all.size()-1).getResponse().get("msg_qty");
            }

            recordTable.setSns_msg_id(msg_id_arrayList);
            recordTable.setMsg_qty(String.valueOf(msg_qty));

            if(!recordTable.isDirect_msg()) {
                fs_all.add(DynamoDBService.saveData(recordTable));
                if(! fs_all.get(fs_all.size()-1).isStatus()) {
                    logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                    return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
                }
            }
            logger.log("Send Notification Success.");

            ResponseMessage.Message rs_msg = new ResponseMessage.Message();
            rs_msg.setMsg_qty(recordTable.getMsg_qty());
            output = new ResponseMessage(200, rs_msg);
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}

