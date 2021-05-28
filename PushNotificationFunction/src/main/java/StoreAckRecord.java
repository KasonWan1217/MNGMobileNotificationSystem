import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import object.FunctionStatus;
import object.db.AckRecord;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;
import util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreAckRecord implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
            AckRecord recordTable = new Gson().fromJson(input.getBody(), AckRecord.class);
            recordTable.setRead_timestamp(CommonUtil.getCurrentTime());
            fs_all.add(DynamoDBService.saveData(recordTable));
            if(! fs_all.get(fs_all.size()-1).isStatus()) {
                logger.log("\nError : " + new GsonBuilder().setPrettyPrinting().create().toJson(fs_all));
                return response.withStatusCode(200).withBody(new ResponseMessage(fs_all.get(fs_all.size() - 1)).convertToJsonString());
            }

            ResponseMessage.Message rs_msg = new ResponseMessage.Message();
            output = new ResponseMessage(200, rs_msg);
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}
