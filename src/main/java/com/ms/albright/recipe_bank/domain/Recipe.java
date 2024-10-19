package com.ms.albright.recipe_bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    private String recipeName;
    private String recipeCreator;
    private String creationDate;
    private String recipeContent;
}