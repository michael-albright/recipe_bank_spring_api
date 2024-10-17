package com.ms.albright.recipe_bank.service.impl;

import com.ms.albright.recipe_bank.AppConstants;
import com.ms.albright.recipe_bank.domain.RecipeDTO;
import com.ms.albright.recipe_bank.service.RecipeBankService;
import com.ms.albright.recipe_bank.util.AwsAccessUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RecipeBankServiceImpl implements RecipeBankService {

    private final ConcurrentHashMap<String, RecipeDTO> recipeCache = new ConcurrentHashMap<>();
/*
    private final ConcurrentHashMap<String, List<String>> recipeNamesCache = new ConcurrentHashMap<>();
*/
    @Autowired
    AwsAccessUtil awsAccessUtil;

    @Value("${bucket.recipe.key}")
    private String bucketPrefix;

    @Override
    public RecipeDTO getRecipe(String recipeName) {
        return recipeCache.get(recipeName);
    }

    @Override
    public List<String> searchRecipes(String recipeName) {
        return recipeCache.keySet().stream()
                .filter(key -> key.toLowerCase().contains(recipeName.toLowerCase())).toList();
    }

    @Override
    public List<String> getRecipeList() {
        checkAndUpdateCache();
        return new ArrayList<>(recipeCache.keySet());
    }

    @Override
    public String saveRecipe(String recipeName, String recipeContent) throws Exception {
        try {
            InputStream inputStream = new ByteArrayInputStream(recipeContent.getBytes(StandardCharsets.UTF_8));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String formattedDate = dateFormat.format(new Date());

            String fullBucketKey = recipeName + "|" + formattedDate + AppConstants.TEXT_EXTENSION;
            awsAccessUtil.s3Upload(fullBucketKey, recipeContent, inputStream);

            // After successful upload, create the RecipeDTO and cache it
            RecipeDTO recipeDTO = new RecipeDTO();
            recipeDTO.setRecipeName(recipeName);
            recipeDTO.setRecipeContent(recipeContent);
            recipeCache.put(recipeName.replace(AppConstants.TEXT_EXTENSION, ""), recipeDTO);

            // TODO: Implement logging
            System.out.println("File uploaded to S3: " + recipeName);
            return "File uploaded to S3: " + recipeName;
        } catch (Exception e) {
            // Handle any errors during upload
            return "Error uploading recipe to S3: " + e.getMessage();
        }
    }

    /* Local Methods */
    public void checkAndUpdateCache() {
        int objectCount = getObjectCountInS3Bucket();
        if(recipeCache.size() != objectCount) {
            getAllRecipes();
        }
    }

    private int getObjectCountInS3Bucket() {
        ListObjectsV2Response response = awsAccessUtil.s3ListObjects();
        List<S3Object> filteredObjList = getFilteredS3Objects(response);
        return filteredObjList.size();
    }

    // Filter out folder-like keys, such as "recipe-bank/upload/recipes/"
    public List<S3Object> getFilteredS3Objects(ListObjectsV2Response response) {
        return response.contents().stream()
                .filter(s3Object -> !s3Object.key().equals("recipe-bank/upload/recipes/"))
                .collect(Collectors.toList());
    }

    public void getAllRecipes() {
        ListObjectsV2Response response = awsAccessUtil.s3ListObjects();
        List<S3Object> filteredObjList = getFilteredS3Objects(response);

        recipeCache.clear();
        filteredObjList.forEach(s3Object -> {
            String recipeName = s3Object.key().replace(bucketPrefix, "").replace(AppConstants.TEXT_EXTENSION, "");
            String recipeContent = awsAccessUtil.s3ObjectContent(s3Object.key());

            // Cache the recipe
            RecipeDTO recipeDTO = new RecipeDTO();
            recipeDTO.setRecipeName(recipeName);
            recipeDTO.setRecipeContent(recipeContent);
            recipeCache.put(recipeName, recipeDTO);
        });
    }


}