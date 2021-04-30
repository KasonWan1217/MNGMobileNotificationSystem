import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import object.AckRecordDBTable;
import object.ResponseMessage;
import object.RetrieveInboxRecordRequest;
import org.apache.log4j.BasicConfigurator;
import service.DynamoDBService;

import java.util.HashMap;
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
            RetrieveInboxRecordRequest request = new RetrieveInboxRecordRequest(input.getBody());
            output = new DynamoDBService().getInboxMessageRecord(request);
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        } else {
            output = new ResponseMessage(500, new ResponseMessage.Message("Request Error.", "Please check the Request Json."));
            logger.log("Request Error - Message: " + output.getMessage());
            return response.withStatusCode(200).withBody(output.convertToJsonString());
        }
    }
}
