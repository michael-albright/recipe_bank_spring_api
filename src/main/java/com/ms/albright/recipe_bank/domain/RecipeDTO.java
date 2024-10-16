package com.ms.albright.recipe_bank.domain;

public class RecipeDTO {
    private String recipeName;
    private String recipeContent;

    // Getters and setters
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeContent() {
        return recipeContent;
    }

    public void setRecipeContent(String recipeContent) {
        this.recipeContent = recipeContent;
    }
}