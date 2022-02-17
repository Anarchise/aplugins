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

public class Leave_House extends Task {
    public Leave_House(ATabletsPlugin plugin, Client client, ClientThread clientThread, ATabletsConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 2;
    }

    @Override
    public boolean validate() {

        if (!MiscUtils.isInPOH(client)) {
            return false;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return false;
        }

        if (inventoryWidget.getWidgetItems().stream().filter(item -> item.getId() == config.mode().getPlankId()).count() >= config.mode().getPlankCost()) {
            return false;
        }

        return true;
    }

    @Override
    public void onGameTick(GameTick event) {
        QueryResults<GameObject> results = new GameObjectQuery()
                .nameEquals("Portal")
                .result(client);

        if (results == null || results.isEmpty()) {
            return;
        }

        GameObject portalObject = results.first();

        if (portalObject == null) {
            return;
        }

        clientThread.invoke(() ->
                client.invokeMenuAction(
                        "Enter",
                        "<col=ffff>Portal",
                        portalObject.getId(),
                        MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        portalObject.getSceneMinLocation().getX(),
                        portalObject.getSceneMinLocation().getY()
                )
        );
    }
}
