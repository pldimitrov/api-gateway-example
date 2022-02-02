package lambda;

import Tweet.Tweet;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class CreateTweet implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1).build();
            DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride
                            .withTableNameReplacement(System.getenv("TWEETS_TABLE")))
                    .build();
            DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);

            Map<String, Object> authRequestContext =  event.getRequestContext().getAuthorizer();
            Map<String, String> authClaims = (Map<String, String>) authRequestContext.get("claims");
            String username = authClaims.get("cognito:username");

            Tweet tweet = new Tweet();
            tweet.setUser(username);
            tweet.setContent(event.getBody());
            tweet.setId(UUID.randomUUID().toString());
            tweet.setTime(Instant.now().toString());

            mapper.save(tweet);
        } catch (Exception e){
            logger.log(e.toString());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withIsBase64Encoded(false);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(false);

    }
}
