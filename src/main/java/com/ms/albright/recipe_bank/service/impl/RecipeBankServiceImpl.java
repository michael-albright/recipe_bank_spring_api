package com.ms.albright.recipe_bank.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.albright.recipe_bank.AppConstants;
import com.ms.albright.recipe_bank.domain.Recipe;
import com.ms.albright.recipe_bank.service.RecipeBankService;
import com.ms.albright.recipe_bank.util.AwsAccessUtil;
import com.ms.albright.recipe_bank.util.DynamoDbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecipeBankServiceImpl implements RecipeBankService {

    //    private final ConcurrentHashMap<String, Map<String, String>> recipeContentCache = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, Map<String, Object>> recipeMetaDataCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Recipe> recipeCache = new ConcurrentHashMap<>();

    @Autowired
    AwsAccessUtil awsAccessUtil;

    @Value("${bucket.recipe.key}")
    private String bucketPrefix;

    @Override
    public String saveRecipe(String recipeName, String recipeCreator, String recipeContent) throws Exception {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String creationDate = dateFormat.format(new Date());

            // dynamo: save meta-data of recipe to dynamo
            String compositeKey = recipeName + "#" + creationDate;  // Composite key combining name and date

            // Create a map of attributes to save
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("recipeName#creationDate", AttributeValue.builder().s(compositeKey).build());
            item.put("recipeName", AttributeValue.builder().s(recipeName).build());
            item.put("creationDate", AttributeValue.builder().s(creationDate).build());
            item.put("recipeCreator", AttributeValue.builder().s(recipeCreator).build());

            awsAccessUtil.dynamoSaveItem(item);

            // s3: save recipeContent as .txt file, name will be recipeName#creationDate
            InputStream inputStream = new ByteArrayInputStream(recipeContent.getBytes(StandardCharsets.UTF_8));
            String fullBucketKey = recipeName + "#" + creationDate + AppConstants.TEXT_EXTENSION;
            awsAccessUtil.s3Upload(fullBucketKey, recipeContent, inputStream);

            Recipe recipe = new Recipe(recipeName, recipeCreator, creationDate, recipeContent);

            // update cache
            updateRecipeCache(compositeKey, recipe);

            // TODO: Implement logging
            System.out.println("File uploaded to S3: " + recipeName);
            return "File uploaded to S3: " + recipeName;
        } catch (Exception e) {
            // Handle any errors during upload
            return "Error uploading recipe to S3: " + e.getMessage();
        }
    }

    @Override
    public List<Recipe> getRecipeList() {
        checkAndUpdateCache();
        List<Recipe> recipeList = new ArrayList<>();
        for (Map.Entry<String, Recipe> entry : recipeCache.entrySet()) {
            recipeList.add(entry.getValue());
        }
        return recipeList;
    }

    @Override
    public Recipe getRecipe(String recipeName, String creationDate) {
        //TODO: Update to use a hash instead of a post, includes deleting all recipes in s3 and ensuring saveRecipe uses "#"
        String recipeAccessKey = recipeName + "|" + creationDate;

        String newAccessKey = recipeName + "#" + creationDate;
        if(recipeCache.isEmpty()) checkAndUpdateCache();
        if (recipeCache.get(newAccessKey) != null) {
            if (recipeCache.get(newAccessKey).getRecipeContent() == null) {
                String recipeContent = awsAccessUtil.s3getObjectContent(recipeAccessKey + AppConstants.TEXT_EXTENSION);
                recipeCache.get(newAccessKey).setRecipeContent(recipeContent);
            }
        } else {
            System.out.println("Recipe does not exist.");
            return null;
        }
        return recipeCache.get(newAccessKey);
    }

    @Override
    public List<String> searchRecipes(String userInput) {
        return recipeCache.keySet().stream()
                .filter(key -> key.toLowerCase().contains(userInput.toLowerCase())).toList();
    }

    /* Local Methods */
    public void updateRecipeCache(String compositeKey, Recipe recipe) {
        recipeCache.put(compositeKey, recipe);
    }

    public void checkAndUpdateCache() {
        int objectCount = getRecipeCount();
        if (recipeCache.size() != objectCount) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, AttributeValue>> allItems = awsAccessUtil.dynamoGetAllItems();
            for (Map<String, AttributeValue> item : allItems) {
                Map<String, Object> plainMap = DynamoDbUtil.convertItem(item);
                // Set recipeContent to null, recipeContent will be populated upon request
                plainMap.put("recipeContent", null);
                // remove partition key
                plainMap.remove("recipeName#creationDate");
                String recipeAccessKey = plainMap.get("recipeName") + "#" + plainMap.get("creationDate");
                if (recipeCache.get(recipeAccessKey) == null) {
                    Recipe recipe = objectMapper.convertValue(plainMap, Recipe.class);
                    recipeCache.put(recipeAccessKey, recipe);
                }
            }
        }
    }

    //TODO: Pull tableName from configuration data (env variable)
    private int getRecipeCount() {
        List<Map<String, AttributeValue>> recipes = awsAccessUtil.dynamoGetAllItems();
        return recipes.size();
    }
}