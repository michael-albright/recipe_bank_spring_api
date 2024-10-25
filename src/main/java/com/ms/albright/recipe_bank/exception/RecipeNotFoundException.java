package com.ms.albright.recipe_bank.exception;

public class RecipeNotFoundException extends RuntimeException {

    public RecipeNotFoundException(String message) {
        super(message);
    }

    public RecipeNotFoundException(String message, Object... messageTextTokens) {
        super(String.format(message, messageTextTokens));
    }
}
