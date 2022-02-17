package net.runelite.client.plugins.atablets.Tasks;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.atablets.ATabletsConfig;
import net.runelite.client.plugins.atablets.ATabletsPlugin;
import net.runelite.client.plugins.atablets.Task;

import java.awt.event.KeyEvent;

public class Reset extends Task {
    public Reset(ATabletsPlugin plugin, Client client, ClientThread clientThread, ATabletsConfig config) {
        super(plugin, client, clientThread, config);
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public boolean validate() {
       return getIdleTicks();
    }

    @Override
    public String getTaskDescription() {
        return "Resetting idle timer";
    }

    @Override
    public void onGameTick(GameTick gameTick) {
        if (getIdleTicks()) {
            pressKey();
            client.setKeyboardIdleTicks(0);
            client.setMouseIdleTicks(0);
        }
    }

    private void pressKey() {
        int key = client.getTickCount() % 2 == 1 ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT;

        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyRelease);
    }

    private boolean getIdleTicks() {
        int idleClientTicks = client.getKeyboardIdleTicks();

        if (client.getMouseIdleTicks() > idleClientTicks) {
            idleClientTicks = client.getMouseIdleTicks();
        }

        return idleClientTicks > 12500;
    }
}
