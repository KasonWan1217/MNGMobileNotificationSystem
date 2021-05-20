import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import service.DynamoDBService;
import service.SNSNotificationService;
import object.db.InboxRecord;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;
import util.CommonUtil;

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
//          String dynamoDBName = System.getenv("DynamoDBName");
            InboxRecord recordTable = new InboxRecord(input.getBody());
            recordTable.setCreate_datetime(request_time);
            output = new SNSNotificationService().publishNotification(recordTable);

            if (output.getCode() == null || !output.getCode().equals(200)) {
                logger.log("Send Notification Fail - Message: " + output.getMessage().getError_msg());
            } else {
                recordTable.setSns_msg_id(output.getMessage().getMsg_id());
                recordTable.setMessage_qty(output.getMessage().getMessage_qty());
                if(recordTable.getDirect_msg() != 0) {
                    output.combine(DynamoDBService.insertData(recordTable));
                    if (!output.getCode().equals(200))
                        return response.withStatusCode(200).withBody(output.convertToJsonString());
                }
                logger.log("Send Notification Success - Code: " + output.getCode());
            }
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}

