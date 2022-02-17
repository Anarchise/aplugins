package net.runelite.client.plugins.atablets.Tasks;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.atablets.ATabletsConfig;
import net.runelite.client.plugins.atablets.ATabletsPlugin;
import net.runelite.client.plugins.atablets.MiscUtils;
import net.runelite.client.plugins.atablets.Task;

public class Build extends Task {
    public Build(ATabletsPlugin plugin, Client client, ClientThread clientThread, ATabletsConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 1;
    }

    @Override
    public boolean validate() {
        //if inside house
        if (!MiscUtils.isInPOH(client)) {
            return false;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return false;
        }

        if (inventoryWidget.getWidgetItems()
                .stream()
                .filter(item -> item.getId() == config.mode().getPlankId())
                .count() < config.mode().getPlankCost()) {
            return false;
        }

        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.lecturnId())
                .result(client);

        if (gameObjects == null || gameObjects.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void onGameTick(GameTick event) {
        QueryResults<GameObject> gameObjects = new GameObjectQuery()
                .idEquals(config.lecturnId())
                .result(client);

        if (gameObjects == null || gameObjects.isEmpty()) {
            return;
        }

        GameObject larderSpaceObject = gameObjects.first();

        if (client.getLocalPlayer().getAnimation() == 4068 || client.getLocalPlayer().getAnimation() == 8491){
            return;
        }
        if (larderSpaceObject == null) {
            return;
        }

        clientThread.invoke(() ->
                client.invokeMenuAction(
                        "Study",
                        "",
                        config.lecturnId(),
                        MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        larderSpaceObject.getSceneMinLocation().getX(),
                        larderSpaceObject.getSceneMinLocation().getY()
                )
        );
    }
}
