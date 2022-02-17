/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.askiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.client.plugins.askiller.ASkillerState.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
        name = "ASkiller",
        enabledByDefault = false,
        description = "Power skills for you",
        tags = {"fishing, mining, wood-cutting, anarchise, bot, power, skill"}
)
@Slf4j
public class ASkillerPlugin extends Plugin {
    @Inject
    private AUtils utils;
    @Inject
    private Client client;
    @Inject
    private ASkillerOverlay overlay;
    @Inject
    OverlayManager overlayManager;
    @Inject
    ClientThread clientThread;
    @Inject
    private ASkillerConfiguration config;
    @Inject
    private ConfigManager configManager;
    @Inject
    PluginManager pluginManager;

    ASkillerState state;
    GameObject targetObject;
    NPC targetNPC;
    WallObject targetWall;
    MenuEntry targetMenu;
    WorldPoint skillLocation;
    Instant botTimer;
    LocalPoint storedLocation;
    Player player;
    WorldArea DENSE_MINE_LOC = new WorldArea(new WorldPoint(1754, 3845, 0), new WorldPoint(1770, 3862, 0));
    int timeout = 0;
    int l = 0;
    int opcode;
    long sleepLength;
    boolean startplugin;
    boolean npcMoved;
    private final Set<Integer> itemIds = new HashSet<>();
    private final Set<Integer> objectIds = new HashSet<>();

    @Provides
    ASkillerConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ASkillerConfiguration.class);
    }

    @Override
    protected void startUp() {
        resetVals();
    }

    @Override
    protected void shutDown() {
        resetVals();

    }

    private void resetVals() {
        overlayManager.remove(overlay);
        state = null;
        timeout = 0;
        botTimer = null;
        skillLocation = null;
        startplugin = false;
        npcMoved = false;
        objectIds.clear();
        itemIds.clear();
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("ASkiller")) {
            return;
        }
        log.info("button {} pressed!", configButtonClicked.getKey());
        if (configButtonClicked.getKey().equals("startButton")) {
            if (!startplugin) {
                startplugin = true;
                state = null;
                targetMenu = null;
                botTimer = Instant.now();
                setLocation();
                getConfigValues();
                overlayManager.add(overlay);
            } else {
                resetVals();
            }
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("ASkiller")) {
            return;
        }
        switch (event.getKey()) {
            case "objectIds":
                objectIds.clear();

                objectIds.addAll(utils.stringToIntList(config.objectIds()));
                break;
            case "dropInventory":
            case "items":
                itemIds.clear();
                itemIds.addAll(utils.stringToIntList(config.items()));
                break;
        }
    }

    private void getConfigValues() {
        objectIds.clear();
        itemIds.clear();
        objectIds.addAll(utils.stringToIntList(config.objectIds()));
        itemIds.addAll(utils.stringToIntList(config.items()));
    }

    public void setLocation() {
        if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN)) {
            skillLocation = client.getLocalPlayer().getWorldLocation();
            storedLocation = client.getLocalPlayer().getLocalLocation();
        } else {
            log.debug("Not logged in.");
            skillLocation = null;
            resetVals();
        }
    }

    private long sleepDelay() {
        sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return sleepLength;
    }

    private int tickDelay() {
        int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        log.debug("tick delay for {} ticks", tickLength);
        return tickLength;
    }


    private GameObject getDenseEssence() {
        assert client.isClientThread();

        if (client.getVarbitValue(4927) == 0) {
            return utils.findNearestGameObject(NullObjectID.NULL_8981);
        }
        if (client.getVarbitValue(4928) == 0) {
            return utils.findNearestGameObject(NullObjectID.NULL_10796);
        }
        return null;
    }

    private void interactNPC() {
        targetNPC = utils.findNearestNpcWithin(skillLocation, 20, objectIds);
        opcode = (MenuAction.NPC_FIRST_OPTION.getId());
        if (targetNPC != null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "",targetNPC.getIndex(), opcode, 0, 0));

            //targetMenu = new MenuEntry("", "", targetNPC.getIndex(), opcode, 0, 0, false);
            //utils.doActionMsTime(targetMenu, targetNPC.getConvexHull().getBounds(), sleepDelay());
        } else {
            log.info("NPC is null");
        }
    }

    private void interactObject() {
        targetObject = (config.type() == ASkillerType.DENSE_ESSENCE) ? getDenseEssence() :
                utils.findNearestGameObjectWithin(skillLocation, 20, objectIds);
        opcode = (MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
        if (targetObject != null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "",targetObject.getId(), opcode, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

            //targetMenu = new MenuEntry("", "", targetObject.getId(), opcode, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
            //utils.doActionMsTime(targetMenu, targetObject.getConvexHull().getBounds(), sleepDelay());
        } else {
            log.info("Game Object is null, ids are: {}", objectIds.toString());
        }
    }

    private void interactWall() {
        targetWall = utils.findWallObjectWithin(skillLocation, 20, objectIds);
        opcode = (MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
        if (targetWall != null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "",targetWall.getId(), opcode, targetWall.getLocalLocation().getSceneX(), targetWall.getLocalLocation().getSceneY()));

            //targetMenu = new MenuEntry("", "", targetWall.getId(), opcode, targetWall.getLocalLocation().getSceneX(), targetWall.getLocalLocation().getSceneY(), false);
            //utils.doActionMsTime(targetMenu, targetWall.getConvexHull().getBounds(), sleepDelay());
        } else {
            log.info("Wall Object is null, ids are: {}", objectIds.toString());
        }
    }

    private ASkillerState getBankState() {
        if (!utils.isBankOpen() && !utils.isDepositBoxOpen()) {
            return FIND_BANK;
        }
        if (!utils.inventoryEmpty()) {
            return DEPOSIT_ALL;
        }
        return BANK_NOT_FOUND;
    }

    private void openBank() {
        GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
        if (bankTarget != null) {
            clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));

            //targetMenu = new MenuEntry("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY(), false);
            //utils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
        } else {
            utils.sendGameMessage("Bank not found");
            startplugin = false;
        }
    }

    private void handleDropAll() {
        utils.dropInventory(true, config.sleepMin(), config.sleepMax());
    }
//    private void handleDropItems() {utils.dropItems(utils.stringToIntList(config.items()), true, 2, 4);}
    private void handleDropItems() { utils.dropAllExcept(utils.stringToIntList(config.items()), true, config.sleepMin(), config.sleepMax()); }

    public ASkillerState getState() {
        if (timeout > 0) {
            return TIMEOUT;
        }
        if (utils.isMoving(storedLocation)) {
            timeout = 2 + tickDelay();
            return RUNNING;
        }
        if (utils.inventoryFull()) {
            if (config.type() == ASkillerType.DENSE_ESSENCE) {
                return WAITING;
            }
            if (config.bankItems()) {
                return getBankState();
            }

            //return DROP_ALL;
            return DROP_ITEMS;
            //return (config.bankItems() ? DROP_ALL : DROP_ITEMS);
        }

        if (client.getLocalPlayer().getAnimation() == -1 || npcMoved) {
            switch (config.type()) {
                case DENSE_ESSENCE:
                    return (DENSE_MINE_LOC.distanceTo(client.getLocalPlayer().getWorldLocation()) == 0) ?
                            FIND_GAME_OBJECT : WAITING;
                case WALL_OBJECT:
                    return FIND_WALL;
                case NPC:
                    return FIND_NPC;
                case GAME_OBJECT:
                    return FIND_GAME_OBJECT;
            }
        }
        return ANIMATING;
    }

    @Subscribe
    private void onGameTick(GameTick tick) {
        if (!startplugin) {
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && skillLocation != null) {
            if (!client.isResized()) {
                utils.sendGameMessage("set client to resizable");
                startplugin = false;
                return;
            }
            if (client.getWidget(WidgetInfo.BANK_PIN_CONTAINER) != null) {
                log.info("bank pin needed");
                utils.sendGameMessage("bank pin needed");
                return;
            }
            state = getState();
            storedLocation = player.getLocalLocation();
            switch (state) {
                case TIMEOUT:
                    utils.handleRun(30, 20);
                    timeout--;
                    break;
                case DROP_ITEMS:
                    handleDropItems();
                    timeout = tickDelay();
                    break;
                case DROP_ALL:
                    handleDropAll();
                    timeout = tickDelay();
                    break;
                case FIND_GAME_OBJECT:
                    interactObject();
                    timeout = tickDelay();
                    break;
                case FIND_WALL:
                    interactWall();
                    timeout = tickDelay();
                    break;
                case FIND_NPC:
                    interactNPC();
                    npcMoved = false;
                    timeout = tickDelay();
                    break;
                case FIND_BANK:
                    openBank();
                    timeout = tickDelay();
                    break;
                case DEPOSIT_ALL:
                    //utils.depositAllOfItems(utils.stringToIntList(config.items()));
                    utils.depositAllExcept(utils.stringToIntList(config.items()));
                    //utils.depositAll();
                    timeout = tickDelay();
                    break;
                case ANIMATING:
                case RUNNING:
                    utils.handleRun(30, 20);
                    timeout = tickDelay();
                    break;
            }
        }
    }




    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (targetObject == null || event.getGameObject() != targetObject || !startplugin) {
            return;
        } else {
            if (client.getLocalDestinationLocation() != null) {
                interactObject(); //This is a failsafe, Player can get stuck with a destination on object despawn and be "forever moving".
            }
        }
    }

    @Subscribe
    public void onNPCDefinitionChanged(NpcChanged event) {
        if (targetNPC == null || event.getNpc() != targetNPC || !startplugin) {
            return;
        }
        if (timeout == 0) {
            interactNPC();
        } else {
            npcMoved = true;
        }
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != 93 || !startplugin || !config.dropOne()) {
            return;
        }

        handleDropAll();
        timeout = tickDelay();
        return;
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && startplugin) {
            state = TIMEOUT;
            timeout = 2;
        }
    }
}
