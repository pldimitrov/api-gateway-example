package lambda;

import Tweet.Tweet;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;

import java.util.*;

public class ListTweets  implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        List<Tweet> results = null;
        String results_json;
        try {
            HashMap<String, String> queryAttributes = new HashMap<>();
            DynamoDBQueryExpression<Tweet> queryExpression = new DynamoDBQueryExpression<>();
            HashMap<String, AttributeValue> searchValue = new HashMap<>();

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1).build();
            DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.
                            withTableNameReplacement(
                            System.getenv("TWEETS_TABLE")))
                    .build();
            DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);

            String resource = event.getResource();

            switch (resource) {
                case "/my_tweets":
                    queryExpression.withKeyConditionExpression("#user = :user");
                    queryAttributes.put("#user", "user");
                    queryExpression.setExpressionAttributeNames(queryAttributes);

                    Map<String, Object> authRequestContext =  event.getRequestContext().getAuthorizer();
                    Map<String, String> authClaims = (Map<String, String>) authRequestContext.get("claims");
                    String username = authClaims.get("cognito:username");

                    searchValue.put(":user", new AttributeValue().withS(username));
                    queryExpression.setExpressionAttributeValues(searchValue);
                    queryExpression.setScanIndexForward(true);
                    results = mapper.query(Tweet.class, queryExpression);
                    break;
                case "/tweets":
                    results = mapper.scan(Tweet.class, new DynamoDBScanExpression());
                    logger.log(Arrays.toString(results.stream().toArray()));
                    break;
            }

            Gson gson = new Gson();
            results_json = gson.toJson(results);

        } catch (Exception e){
            logger.log(e.toString());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(e.toString())
                    .withIsBase64Encoded(false);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(results_json)
                .withIsBase64Encoded(false);
    }
}
