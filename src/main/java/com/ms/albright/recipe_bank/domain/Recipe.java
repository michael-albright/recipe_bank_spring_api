package com.ms.albright.recipe_bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recipe {
    private String recipeName;
    private String recipeCreator;
    private String creationDate;
    private String recipeContent;
}