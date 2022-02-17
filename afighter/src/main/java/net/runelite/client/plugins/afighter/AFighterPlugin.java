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
package net.runelite.client.plugins.afighter;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.time.Instant;
import java.util.*;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import static net.runelite.client.plugins.afighter.AFighterState.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "AFighter",
		enabledByDefault = false,
		description = "Kills stuff.",
		tags = {"anarchise","dragons","pvm"}
)
@Slf4j
public class AFighterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	public ReflectBreakHandler chinBreakHandler;

	@Inject
	private AFighterConfig config;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	ClientThread clientThread;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private AFighterOverlay overlay;

	@Inject
	private ItemManager itemManager;
	List<String> lootableItems = new ArrayList<>();

	List<String> killableEnemies = new ArrayList<>();
	int count = 0;
    int l = 1;
	AFighterState state;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	boolean Firstime;
	boolean EmptiedFirst = false;
	boolean EmptiedSecond = false;
	boolean EmptiedThird = false;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	Rectangle clickBounds;

	boolean DidObstacle = false;
	int timeout = 0;
	long sleepLength;
	boolean startTeaks;
	List<TileItem> loot = new ArrayList<>();
	String[] values;
	String[] names;

	int essenceValue;
	//3055 4846
	@Provides
	AFighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AFighterConfig.class);
	}

	@Override
	protected void startUp()
	{
		resetVals();
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}

	private void resetVals()
	{
		loot.clear();
		lootableItems.clear();
		killableEnemies.clear();
		values = config.lootNames().toLowerCase().split("\\s*,\\s*");
		names = config.enemyNames().toLowerCase().split("\\s*,\\s*");
		if (!config.lootNames().isBlank()) {
			lootableItems.clear();
			lootableItems.addAll(Arrays.asList(values));
			log.debug("Lootable items are: {}", lootableItems.toString());
		}
		if (!config.enemyNames().isBlank()){
			killableEnemies.clear();
			killableEnemies.addAll(Arrays.asList(names));
			log.debug("Enemies are: {}", killableEnemies.toString());
		}
		overlayManager.remove(overlay);
		Firstime = true;
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
	}

	private void onChatMessage(ChatMessage event) {
		ChatMessageType type = event.getType();
		String msg = event.getMessage();

		if (!type.equals(ChatMessageType.PUBLICCHAT)) {
			if (msg.contains("...and you manage to crawl through.")) {
				DidObstacle = true;
			}
		}
	}
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("afighter"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startTeaks)
			{
				EmptiedFirst = false;
				EmptiedSecond = false;
				EmptiedThird = false;
				startTeaks = true;
				Firstime = true;
				state = null;
				targetMenu = null;
				loot.clear();
				botTimer = Instant.now();
				setLocation();
				overlayManager.add(overlay);
				chinBreakHandler.startPlugin(this);
			}
			else
			{
				startTeaks=false;
				chinBreakHandler.stopPlugin(this);
				resetVals();
			}
		}
	}
	public boolean checkHasBindingNeck(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Binding necklace"))
				return true;
		}

		return false;
	}
	public boolean checkHasDuelingRing(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Ring of dueling("))
				return true;
		}

		return false;
	}
	public boolean checkHasGlory(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Amulet of glory("))
				return true;
		}

		return false;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ADragons"))
		{
			return;
		}
		startTeaks = false;
		resetVals();
	}

	public void setLocation()
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
		{
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		}
		else
		{
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
	}
	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private AFighterState getBankState()
	{
		if (utils.inventoryContains(536)){
			utils.depositAll();
			return DEPOSIT_ITEMS;
		}

		if (config.usePots() && config.potion1() != 0 && !utils.inventoryContains(config.potion1())){
			utils.withdrawItem(config.potion1());
			return WITHDRAW_ITEMS;
		}
		if (config.usePots() && config.potion2() != 0 && !utils.inventoryContains(config.potion2())){
			utils.withdrawItem(config.potion2());
			return WITHDRAW_ITEMS;
		}
		if (!utils.inventoryContains(config.foodID())){
			utils.withdrawItemAmount(config.foodID(), config.foodAmount());
			return WITHDRAW_ITEMS;
		}
		if (!utils.inventoryContains(536) && !utils.inventoryContains(1751) && utils.inventoryContains(config.foodID())){
			return JUMP_OBSTACLE;
		}
		return TIMEOUT;
	}

	public AFighterState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		else if (utils.isMoving(beforeLoc))
		{
			timeout = 2 + tickDelay();
			return MOVING;
		}

		else if(utils.isBankOpen()){
			return getBankState();
		}
		else if(client.getLocalPlayer().getAnimation()!=-1){
			return ANIMATING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1 && client.getLocalPlayer().getAnimation() != 7202){
			return ANIMATING;
		}
		else {
			return getAirsState();
		}
	}
	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "",bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		}
	}
	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startTeaks)
		{
			return;
		}
		if (chinBreakHandler.isBreakActive(this)){
			return;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			chinBreakHandler.startBreak(this);
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && skillLocation != null)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("client must be set to resizable");
				startTeaks = false;
				return;
			}
			state = getState();
			beforeLoc = player.getLocalLocation();
			utils.setMenuEntry(null);
			switch (state)
			{
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case MOVING:
					utils.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case WALK_FIRST:
					utils.walk(new WorldPoint(2910, 3372, 0));
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					utils.walk(new WorldPoint(2927, 3360, 0));
					timeout = tickDelay();
					break;
				case ATTACK:
					findEnemiesLoop();
					timeout = tickDelay();
					break;
				case FIND_BANK:
					openBank();
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					//utils.depositAll();
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
					timeout = tickDelay();
					break;
				case LOOT_ITEMS:
					lootItem(loot);
					timeout = tickDelay();
					break;
				case JUMP_OBSTACLE:
					utils.useGameObject(24222, 3, sleepDelay());
					timeout = tickDelay();
					break;
				case CLIMB_LADDER:
					utils.useGameObject(16680, 3, sleepDelay());
					timeout = tickDelay();
					break;
				case CLIMB_LADDER2:
					utils.useGameObject(17385, 3, sleepDelay());
					timeout = tickDelay();
					break;
				case JUMP_OBSTACLE2:
					utils.useGameObject(16509, 3, sleepDelay());
					timeout = tickDelay();
					break;
				case BURY_BONES:
					buryBones();
					timeout = tickDelay();
					break;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		loot.clear();
		if (event.getGameState() == GameState.LOGGED_IN && startTeaks)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}
	public void findEnemiesLoop(){
		NPC target = utils.findNearestAttackableNpcWithin(client.getLocalPlayer().getWorldLocation(), 20, config.enemyNames(), false);

		if (target == null)
		{
			return;
		}

		if (utils.findNearestNpcTargetingLocal(config.enemyNames(), false) != null){
			return;
		}

		utils.attackNPCDirect(target);
	}

	private TileItem getNearestTileItem(List<TileItem> tileItems) {
		int currentDistance;
		TileItem closestTileItem = tileItems.get(0);
		int closestDistance = closestTileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
		for (TileItem tileItem : tileItems) {
			currentDistance = tileItem.getTile().getWorldLocation().distanceTo(player.getWorldLocation());
			if (currentDistance < closestDistance) {
				closestTileItem = tileItem;
				closestDistance = currentDistance;
			}
		}
		return closestTileItem;
	}

	private void buryBones() {
		List<WidgetItem> bones = utils.getInventoryItems("bones");
		for (WidgetItem bone : bones) {
			if (bone != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "",bone.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), bone.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
	}

	private void lootItem(List<TileItem> itemList) {
		TileItem lootItem = getNearestTileItem(itemList);
		if (lootItem != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "",lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
	}
	private AFighterState getAirsState()
	{
		if (config.Type() == AFighterType.NORMAL) {
			if (utils.inventoryContains("bones") && config.buryBones()){
				return BURY_BONES;
			}
			if (loot.isEmpty() && !utils.inventoryFull()) {
				return ATTACK;
			}
			if (!loot.isEmpty() && !utils.inventoryFull()) {
				return LOOT_ITEMS;
			}
			return UNHANDLED_STATE;
		}
		if (config.Type() == AFighterType.BLUE_DRAGONS) {
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2936, 3354, 0), new WorldPoint(2954, 3378, 0)))) {
				return FIND_BANK;
			}
			if (player.getWorldLocation() == new WorldPoint(2936, 3355, 0)) {
				return FIND_BANK;
			}
			if (player.getWorldLocation().equals(new WorldPoint(2934, 3355, 0))) {
				return WALK_FIRST;
			}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2907, 3364, 0), new WorldPoint(2917, 3373, 0))) && !utils.inventoryFull()) {
				return CLIMB_LADDER;
			}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2907, 3364, 0), new WorldPoint(2917, 3373, 0))) && utils.inventoryFull()) {
				return WALK_SECOND;
			}
			if (player.getWorldLocation().equals(new WorldPoint(2884, 9796, 0))) {
				return JUMP_OBSTACLE2;
			}
			if (player.getWorldLocation() == new WorldPoint(2927, 3360, 0) && utils.inventoryFull()) {
				return JUMP_OBSTACLE;
			}
			if (utils.inventoryContains("bones") && config.buryBones()){
				return BURY_BONES;
			}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2889, 9775, 0), new WorldPoint(2925, 9815, 0))) && loot.isEmpty() && !utils.inventoryFull()) {
				return ATTACK;
			}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2889, 9775, 0), new WorldPoint(2925, 9815, 0))) && !loot.isEmpty() && !utils.inventoryFull()) {
				return LOOT_ITEMS;
			}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2889, 9775, 0), new WorldPoint(2925, 9815, 0))) && utils.inventoryFull()) {
				return JUMP_OBSTACLE2;
			}
			if (player.getWorldLocation().equals(new WorldPoint(2886, 9799, 0)) && utils.inventoryFull()) {
				return CLIMB_LADDER2;
			}
			if (player.getWorldLocation().equals(new WorldPoint(2884, 3398, 0))) {
				return WALK_SECOND;
			}
			return UNHANDLED_STATE;
		}
		return UNHANDLED_STATE;
	}


	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		if (!startTeaks) {
			return;
		}
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();
		if (client.getItemDefinition(event.getItem().getId()).getName() == "Dragon bones" || client.getItemDefinition(event.getItem().getId()).getName() == "Babydragon bones" || lootableItems.stream().anyMatch(itemName.toLowerCase()::contains)) {             // || client.getItemDefinition(event.getItem().getId()).getName() == "Dragon bones" || client.getItemDefinition(event.getItem().getId()).getName() == "Draconic visage") {
			loot.add(item);
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		if (!startTeaks) {
			return;
		}
		loot.remove(event.getItem());
	}
}
