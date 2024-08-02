package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

		DynamoDB dynamoDB = new DynamoDB(client); //
		Table sourceTable = dynamoDB.getTable("cmtr-cc4eb9d3-Events");
		Table destinationTable = dynamoDB.getTable("cmtr-cc4eb9d3-Events-test");

		try {
			Item item;
			ScanSpec scanSpec = new ScanSpec();
			for (Item it : sourceTable.scan(scanSpec)) {
				item = it;
				destinationTable.putItem(item);
			}
		} catch (Exception e) {
			System.err.println("Unable to copy data from sourceTableName to destinationTableName");
			System.err.println(e.getMessage());
		}//




		Map<String, AttributeValue> itemValues = new HashMap<>();

		String uuid = UUID.randomUUID().toString();
		itemValues.put("id", new AttributeValue().withS(uuid));

		int principalId = (Integer) request.getOrDefault("principalId",0);
		itemValues.put("principalId", new AttributeValue().withN(String.valueOf(principalId)));

		Map<String,String> content = (Map<String,String>) request.getOrDefault("content",new HashMap<>());
		Map<String,AttributeValue> body = new HashMap<>();
		content.forEach((key,value)->body.put(key, new AttributeValue().withS(value)));
		itemValues.put("body", new AttributeValue().withM(body));

		String createdAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		itemValues.put("createdAt", new AttributeValue().withS(createdAt));

		client.putItem("cmtr-cc4eb9d3-Events", itemValues);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("statusCode", 201);

		Map<String,Object> event = new HashMap<>();
		event.put("id",String.valueOf(uuid));
		event.put("principalId", principalId);
		event.put("createdAt", createdAt);

		Map<String,String> simpleBody = new HashMap<>();
		body.forEach((key,value)->simpleBody.put(key,value.getS()));
		event.put("body", simpleBody);

		response.put("event", event);
		return response;
	}
}
