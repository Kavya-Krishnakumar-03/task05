package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(
		@EnvironmentVariable(key="region", value="${region}")
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
				.withRegion("eu-central-1")
				.build();

		Map<String, AttributeValue> itemValues = new HashMap<>();
		// Generate a numeric ID
		Random random = new Random();
		int numericId = random.nextInt(Integer.MAX_VALUE);  // Generate a random integer
		itemValues.put("Id", new AttributeValue().withN(String.valueOf(numericId)));  // Use 'withN' for numeric values

		// Safely get 'principalId' and 'content' as String
		String principalId = String.valueOf(request.getOrDefault("principalId", "defaultPrincipalId"));
		String content = String.valueOf(request.getOrDefault("content", "defaultContent"));

		itemValues.put("principalId", new AttributeValue().withS(principalId));
		itemValues.put("content", new AttributeValue().withS(content));

		client.putItem("cmtr-cc4eb9d3-Events", itemValues);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("statusCode", 201);
		response.put("body", itemValues);
		return response;
	}
}
