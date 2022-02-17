package net.runelite.client.plugins.atablets;

import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.atablets.Tasks.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Extension
@PluginDescriptor(
        name = "ATablets",
        description = "Makes Tablets in POH.",
        tags = {"construction", "cons", "anarchise"},
        enabledByDefault = false
)

public class ATabletsPlugin extends Plugin {
    static List<Class<?>> taskClassList = new ArrayList<>();

    static {
        taskClassList.add(Reset.class);
        taskClassList.add(Break.class);
       // taskClassList.add(Stop_Plugin.class);
        taskClassList.add(Run.class);
        taskClassList.add(Dialogue2.class);
        taskClassList.add(Craft.class);
        taskClassList.add(Build.class);

        taskClassList.add(Leave_House.class);
        taskClassList.add(Phials.class);
        taskClassList.add(Enter_House.class);
        taskClassList.add(Dialogue.class);
    }

    @Inject
    public Client client;

    @Inject
    public ATabletsConfig config;

    @Inject
    public ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ATabletsOverlay overlay;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    public ReflectBreakHandler chinBreakHandler;

    boolean pluginStarted = false;

    @Provides
    ATabletsConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(ATabletsConfig.class);
    }

    public String status = "initializing...";

    private final TaskSet tasks = new TaskSet();
    Instant botTimer;
    public int delay = 0;

    @Override
    protected void startUp() throws Exception {
        pluginStarted = false;
        chinBreakHandler.registerPlugin(this);
    }

    @Override
    protected void shutDown() throws Exception {
        pluginStarted = false;
        chinBreakHandler.unregisterPlugin(this);
    }


    @Subscribe
    public void onConfigButtonClicked(ConfigButtonClicked event) {
        if (!event.getGroup().equals(ATabletsConfig.class.getAnnotation(ConfigGroup.class).value())) {
            return;
        }
        if (event.getKey().equals("startButton")) {
            if (!pluginStarted) {
                pluginStarted = true;
                chinBreakHandler.startPlugin(this);
                status = "initializing...";
                overlayManager.add(overlay);
                tasks.clear();
                tasks.addAll(this, client, clientThread, config, taskClassList);
                botTimer = Instant.now();
            }
            else {
                pluginStarted = false;
                chinBreakHandler.stopPlugin(this);
                overlayManager.remove(overlay);
                tasks.clear();
                botTimer = null;
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!pluginStarted) {
            return;
        }

        if (chinBreakHandler.isBreakActive(this)) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (delay > 0) {
            delay--;
            return;
        }

        Task task = tasks.getValidTask();

        if (task != null) {
            status = task.getTaskDescription();
            task.onGameTick(event);
            delay = task.getDelay() + getRandomWait();
        }
    }

    public int getRandomWait() {
        return (int) ((Math.random() * (3 - 1)) + 1);
    }

    private void sendGameMessage(String message) {
        chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(
                                new ChatMessageBuilder()
                                        .append(ChatColorType.HIGHLIGHT)
                                        .append(message)
                                        .build())
                        .build());
    }

    public void stopPlugin() {
        stopPlugin("");
    }

    public void stopPlugin(String reason) {
        pluginStarted = false;
        chinBreakHandler.stopPlugin(this);

        if (reason != null && !reason.isEmpty())
            sendGameMessage("ATablets Stopped: " + reason);
    }
}