# api-gateway-example



## To build the project

The business logic needs to be built and packaged into a fat jar first:
```
cd lambda
mvn clean package
```

The CDK code in `api-gateway-example-cdk` will provision all the necessary infrastructure and deploy the business logic as lambda functions running behind AWS API Gateway using AWS Cognito for AuthN/AuthZ and DynamoDB for storage.

```
cd api-gateway-example-cdk
cdk synth && cdk deploy
```



## Testing
A postman collection (importable as json) is available under `./postman` to make testing easier. The login and API endpoint URLs are already set up but it's necessary that you log in for each user ("folder" in the collection) individually to set the corresponding Oauth 2.0 JWT token.

Due to a bug in Postman (or rather an incompatibility with Cognito) the following needs to be done to set up the access token correctly.
- upon clicking `Get New Access Token` under the `Authorization` tab for each user (directory in the Postman collection), follow the instructions and enter username/password.
- click `Proceed`
- you will see a window that shows you 2 tokens - `Access Token` and `id_token`. You need to use `id_token` - copy it to use in the next stage. Postman is using `Access Token` by default and is unable to authenticate when using that.
- copy `id_token` as explained and click `Use Token` to go close the window
- paste `id_token` into the selected field in Postman, remove newline if needed

Use the requests (grouped by user/auth details) to test all endpoints.
