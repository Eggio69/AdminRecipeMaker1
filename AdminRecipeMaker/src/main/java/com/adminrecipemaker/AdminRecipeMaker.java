package com.adminrecipemaker;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AdminRecipeMaker implements ModInitializer {
    public static final String MOD_ID = "adminrecipemaker";
    private static final Map<UUID, EditorSession> EDITORS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(CustomRecipeManager::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(CustomRecipeManager::save);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("recipe")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("create")
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "id");
                            if (!id.matches("[a-z0-9_.-]{1,64}")) {
                                context.getSource().sendError(Text.literal("ID: только a-z, 0-9, _, -, . (до 64 символов)."));
                                return 0;
                            }
                            SimpleInventory inventory = new SimpleInventory(54);
                            EDITORS.put(player.getUuid(), new EditorSession(id, inventory));
                            player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                                (syncId, playerInventory, p) -> GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory),
                                Text.literal("Recipe Editor: " + id)));
                            player.sendMessage(Text.literal("Редактор открыт. Слоты 1–9 — ингредиенты, слот 14 — результат. Затем /recipe save " + id), false);
                            return 1;
                        })))
                .then(CommandManager.literal("save")
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String id = StringArgumentType.getString(context, "id");
                            EditorSession session = EDITORS.get(player.getUuid());
                            if (session == null || !session.id.equals(id)) {
                                context.getSource().sendError(Text.literal("Сначала открой редактор: /recipe create " + id));
                                return 0;
                            }
                            if (CustomRecipeManager.addFromEditor(context.getSource().getServer(), id, session.inventory)) {
                                context.getSource().sendFeedback(() -> Text.literal("Рецепт сохранён: " + id), false);
                                return 1;
                            }
                            context.getSource().sendError(Text.literal("Не получилось сохранить: положи результат в слот 14 и хотя бы один ингредиент в слоты 1–9."));
                            return 0;
                        })))
                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("id", StringArgumentType.word())
                        .executes(context -> {
                            String id = StringArgumentType.getString(context, "id");
                            boolean removed = CustomRecipeManager.remove(context.getSource().getServer(), id);
                            context.getSource().sendFeedback(() -> Text.literal(removed ? "Удалён рецепт: " + id : "Рецепт не найден: " + id), false);
                            return removed ? 1 : 0;
                        })))
                .then(CommandManager.literal("list").executes(context -> {
                    var ids = CustomRecipeManager.listIds();
                    context.getSource().sendFeedback(() -> Text.literal(ids.isEmpty() ? "Кастомных рецептов нет." : "Рецепты: " + String.join(", ", ids)), false);
                    return ids.size();
                }))
                .then(CommandManager.literal("reload").executes(context -> {
                    CustomRecipeManager.load(context.getSource().getServer());
                    context.getSource().sendFeedback(() -> Text.literal("Рецепты перезагружены."), false);
                    return 1;
                }))
        ));
    }

    public static EditorSession getEditor(UUID playerId) { return EDITORS.get(playerId); }

    public record EditorSession(String id, SimpleInventory inventory) {}
}
