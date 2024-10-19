package com.ms.albright.recipe_bank.service;

import com.ms.albright.recipe_bank.domain.Recipe;

import java.util.List;
import java.util.Map;

public interface RecipeBankService {
    List<Recipe> getRecipeList();

    String saveRecipe(String recipeName, String recipeCreator, String recipeContent) throws Exception;

    Recipe getRecipe(String recipeName, String creationDate);

    List<String> searchRecipes(String recipeName);
}
