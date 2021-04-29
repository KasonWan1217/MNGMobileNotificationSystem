import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import service.DynamoDBService;
import service.PushNotificationService;
import object.InboxRecordTable;
import object.ResponseMessage;
import org.apache.log4j.BasicConfigurator;

/**
 * Handler for requests to Lambda function.
 */
public class SendNotification implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        BasicConfigurator.configure();
        final LambdaLogger logger = context.getLogger();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
        ResponseMessage output = null;
        if (input != null) {
//          String dynamoDBName = System.getenv("DynamoDBName");
            InboxRecordTable recordTable = new InboxRecordTable(input.getBody());
            output = new PushNotificationService().publishNotification(recordTable);

            if (output.getCode() == null || !output.getCode().equals(200)) {
                logger.log("Send Notification Fail - Message: " + output.getMessage().getErrorMsg());
            } else {
                recordTable.setMessage_id(output.getMessage().getMessage_id());
                if(!recordTable.isDirectMsg()) {
                    ResponseMessage db_response = DynamoDBService.insertData(recordTable);
                    if (!db_response.getCode().equals(200))
                        output = db_response;
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

