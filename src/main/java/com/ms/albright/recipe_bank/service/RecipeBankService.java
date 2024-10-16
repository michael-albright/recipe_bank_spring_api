package com.ms.albright.recipe_bank.service;

import com.ms.albright.recipe_bank.domain.RecipeDTO;

import java.util.List;

public interface RecipeBankService {
    List<String> getRecipeList();

    String saveRecipe(String recipeName, String recipeContent) throws Exception;

    RecipeDTO getRecipe(String recipeName);

    List<String> searchRecipes(String recipeName);
}
