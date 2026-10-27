package com.adminrecipemaker.mixin;

import com.adminrecipemaker.CustomRecipeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {
    @Shadow @Final private RecipeInputInventory input;

    @Inject(method = "onTakeItem", at = @At("HEAD"), cancellable = true)
    private void adminrecipemaker$consumeCustomIngredients(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        CustomRecipeManager.CustomRecipe recipe = CustomRecipeManager.find(input);
        if (recipe != null) {
            recipe.consume(input);
            ci.cancel();
        }
    }
}
