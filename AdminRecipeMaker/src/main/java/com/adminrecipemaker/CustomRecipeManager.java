package com.adminrecipemaker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.CraftingResultInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CustomRecipeManager {
    private static final Map<String, CustomRecipe> RECIPES = new LinkedHashMap<>();
    private static MinecraftServer server;

    private CustomRecipeManager() {}

    public static synchronized void load(MinecraftServer minecraftServer) {
        server = minecraftServer;
        RECIPES.clear();
        Path path = filePath(minecraftServer);
        if (!Files.exists(path)) return;
        try {
            var ops = RegistryOps.of(JsonOps.INSTANCE, minecraftServer.getRegistryManager());
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonObject recipeJson = entry.getValue().getAsJsonObject();
                JsonArray ingredientsJson = recipeJson.getAsJsonArray("ingredients");
                ItemStack[] ingredients = new ItemStack[9];
                for (int i = 0; i < 9; i++) {
                    ingredients[i] = i < ingredientsJson.size()
                        ? ItemStack.CODEC.parse(ops, ingredientsJson.get(i)).getOrThrow()
                        : ItemStack.EMPTY;
                }
                ItemStack output = ItemStack.CODEC.parse(ops, recipeJson.get("output")).getOrThrow();
                if (!output.isEmpty()) RECIPES.put(entry.getKey(), new CustomRecipe(ingredients, output));
            }
        } catch (Exception e) {
            System.err.println("[AdminRecipeMaker] Не удалось загрузить рецепты: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void save(MinecraftServer minecraftServer) {
        if (minecraftServer != null) server = minecraftServer;
        if (server == null) return;
        try {
            Path path = filePath(server);
            Files.createDirectories(path.getParent());
            var ops = RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager());
            JsonObject root = new JsonObject();
            for (var entry : RECIPES.entrySet()) {
                JsonObject recipeJson = new JsonObject();
                JsonArray ingredients = new JsonArray();
                for (ItemStack ingredient : entry.getValue().ingredients) {
                    ingredients.add(ItemStack.CODEC.encodeStart(ops, ingredient).getOrThrow());
                }
                recipeJson.add("ingredients", ingredients);
                recipeJson.add("output", ItemStack.CODEC.encodeStart(ops, entry.getValue().output).getOrThrow());
                root.add(entry.getKey(), recipeJson);
            }
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception e) {
            System.err.println("[AdminRecipeMaker] Не удалось сохранить рецепты: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized boolean addFromEditor(MinecraftServer server, String id, Inventory editor) {
        ItemStack output = editor.getStack(13).copy(); // GUI slot 14
        if (output.isEmpty()) return false;
        ItemStack[] ingredients = new ItemStack[9];
        boolean hasIngredient = false;
        for (int i = 0; i < 9; i++) {
            ingredients[i] = editor.getStack(i).copy();
            if (!ingredients[i].isEmpty()) hasIngredient = true;
        }
        if (!hasIngredient) return false;
        RECIPES.put(id, new CustomRecipe(ingredients, output));
        save(server);
        return true;
    }

    public static synchronized boolean remove(MinecraftServer server, String id) {
        boolean removed = RECIPES.remove(id) != null;
        if (removed) save(server);
        return removed;
    }

    public static synchronized List<String> listIds() {
        return Collections.unmodifiableList(new ArrayList<>(RECIPES.keySet()));
    }

    public static synchronized CustomRecipe find(Inventory input) {
        for (CustomRecipe recipe : RECIPES.values()) {
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    public static synchronized void updateResult(RecipeInputInventory input, CraftingResultInventory result) {
        CustomRecipe recipe = find(input);
        if (recipe != null) result.setStack(0, recipe.output.copy());
    }

    private static Path filePath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("config").resolve("adminrecipemaker").resolve("recipes.json");
    }

    public static final class CustomRecipe {
        private final ItemStack[] ingredients;
        private final ItemStack output;

        private CustomRecipe(ItemStack[] ingredients, ItemStack output) {
            this.ingredients = ingredients;
            this.output = output;
        }

        public boolean matches(Inventory input) {
            if (input.size() != 9) return false;
            for (int i = 0; i < 9; i++) {
                ItemStack expected = ingredients[i];
                ItemStack actual = input.getStack(i);
                if (expected.isEmpty() != actual.isEmpty()) return false;
                if (expected.isEmpty()) continue;
                if (!ItemStack.areItemsAndComponentsEqual(expected, actual)) return false;
                if (actual.getCount() < expected.getCount()) return false;
            }
            return true;
        }

        public void consume(RecipeInputInventory input) {
            for (int i = 0; i < 9; i++) {
                ItemStack expected = ingredients[i];
                if (expected.isEmpty()) continue;
                ItemStack actual = input.getStack(i);
                actual.decrement(expected.getCount());
                if (actual.isEmpty()) input.setStack(i, ItemStack.EMPTY);
            }
            input.markDirty();
        }

        public ItemStack output() { return output.copy(); }
    }
}
