import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.FunctionStatus;
import object.InboxMessageRecord;
import object.ResponseMessage;
import object.db.SnsAccount.Subscriptions;
import object.request.RetrieveInboxRecordRequest;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;
import util.DBEnumValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrieveInboxRecord implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
            RetrieveInboxRecordRequest request = new Gson().fromJson(input.getBody(), RetrieveInboxRecordRequest.class);
            fs_all.add(new DynamoDBService().getSubscriptionsList(request.getApp_reg_id()));
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }

            List<Subscriptions> arrayList_topicName = (List<Subscriptions>) fs_all.get(fs_all.size() - 1).getResponse().get("arrayList_channelName");
            fs_all.add(new DynamoDBService().getInboxMessageRecord(request, arrayList_topicName));
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }

            ResponseMessage.Message rs_msg = new ResponseMessage.Message();
            rs_msg.setInbox_msg((InboxMessageRecord[]) fs_all.get(fs_all.size()-1).getResponse().get("inbox_msg"));
            output = new ResponseMessage(200, rs_msg);
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}
