package Tweet;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName="Tweets")
public class Tweet {
    private String user;
    private String content;
    private String id;
    private String time;

    @DynamoDBHashKey(attributeName="user")
    public String getUser() {return user;};
    public void setUser(String user) {
        this.user = user;
    }
    @DynamoDBAttribute(attributeName="content")
    public String getContent() {return content;};
    public void setContent(String content) {
        this.content = content;
    }
    @DynamoDBAttribute(attributeName="id")
    public String getId() {return id;}
    public void setId(String id) {
        this.id = id;
    }
    @DynamoDBRangeKey(attributeName = "time")
    public String getTime() {return time;}
    public void setTime(String time) {
        this.time = time;
    }

}
