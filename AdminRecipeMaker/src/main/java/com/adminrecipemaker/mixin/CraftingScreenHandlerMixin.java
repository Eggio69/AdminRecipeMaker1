package com.adminrecipemaker.mixin;

import com.adminrecipemaker.CustomRecipeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.screen.CraftingScreenHandler")
public abstract class CraftingScreenHandlerMixin {
    @Inject(method = "updateResult", at = @At("TAIL"))
    private static void adminrecipemaker$applyCustomRecipe(
        ScreenHandler handler,
        ServerWorld world,
        PlayerEntity player,
        RecipeInputInventory craftingInventory,
        CraftingResultInventory resultInventory,
        @Nullable RecipeEntry<CraftingRecipe> recipe,
        CallbackInfo ci
    ) {
        CustomRecipeManager.updateResult(craftingInventory, resultInventory);
    }
}
