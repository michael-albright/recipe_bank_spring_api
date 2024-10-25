package com.ms.albright.recipe_bank.controller;

import com.ms.albright.recipe_bank.domain.Recipe;
import com.ms.albright.recipe_bank.service.RecipeBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipeBankController")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Recipe Bank API", description = "API for managing recipes")
public class RecipeBankController {

    @Autowired
    RecipeBankService recipeBankService;

    @GetMapping("/getRecipeList")
    public ResponseEntity<List<Recipe>> getRecipeList() throws Exception {
        return new ResponseEntity<>(recipeBankService.getRecipeList(), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/searchRecipes")
    public ResponseEntity<List<String>> searchRecipes(@RequestParam String recipeName) throws Exception {
        return new ResponseEntity<>(recipeBankService.searchRecipes(recipeName), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/getRecipe")
    public ResponseEntity<Recipe> getRecipe(@RequestParam String recipeName, @RequestParam String creationDate) throws Exception {
        return new ResponseEntity<>(recipeBankService.getRecipe(recipeName, creationDate), HttpStatusCode.valueOf(200));
    }

    @PostMapping("/saveRecipe")
    @Operation(summary = "Upload a new recipe to s3", description = "Uploads a new recipe to the Recipe Bank")
    public ResponseEntity<String> saveRecipe(@RequestBody Recipe recipe) throws Exception {
        recipeBankService.saveRecipe(recipe.getRecipeName(), recipe.getRecipeCreator(), recipe.getRecipeContent());
        return ResponseEntity.ok("Recipe saved successfully!");
    }

}
