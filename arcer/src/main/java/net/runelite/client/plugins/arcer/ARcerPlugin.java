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
package net.runelite.client.plugins.arcer;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

import static net.runelite.api.MenuAction.ITEM_SECOND_OPTION;
import static net.runelite.client.plugins.arcer.ARcerState.*;
import static net.runelite.client.plugins.arcer.ARcerType.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "ARunecraft",
		enabledByDefault = false,
		description = "Crafts runes",
		tags = {"rune, craft, runecraft, anarchise"}
)
@Slf4j
public class ARcerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	ClientThread clientThread;

	@Inject
	public ReflectBreakHandler chinBreakHandler;

	@Inject
	private ARcerConfiguration config;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private ARcerOverlay overlay;

	@Inject
	private ItemManager itemManager;


	int count = 0;
	ARcerState state;
	GameObject targetObject;
	DecorativeObject decorativeObject;
	NPC targetNPC;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	boolean Firstime;
	boolean FirstInventory = true;
	boolean FirstInvent = true;
	boolean EmptiedFirst = false;
	boolean EmptiedSecond = false;
	boolean EmptiedThird = false;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	Rectangle altRect = new Rectangle(-100,-100, 10, 10);
	Rectangle clickBounds;
    int l = 0;
	WorldArea FALADOR_EAST_BANK = new WorldArea(new WorldPoint(3009,3353,0),new WorldPoint(3019,3359,0));

	WorldPoint FIRST_CLICK_AIR = new WorldPoint(3006,3315,0);
	WorldArea FIRST_POINT_AIR = new WorldArea(new WorldPoint(3004,3314,0),new WorldPoint(3009,3319,0));

	WorldPoint SECOND_CLICK_AIR = new WorldPoint(3006,3330,0);
	WorldArea SECOND_POINT_AIR = new WorldArea(new WorldPoint(3003,3325,0),new WorldPoint(3010,3333,0));

	WorldArea AIR_ALTAR = new WorldArea(new WorldPoint(2839,4826,0),new WorldPoint(2849,4840,0));
	WorldPoint OUTSIDE_ALTAR_AIR = new WorldPoint(2983,3288,0);



	WorldArea ZANARIS_BANK = new WorldArea(new WorldPoint(2374,4451,0),new WorldPoint(2390,4463,0));

	WorldPoint FIRST_CLICK_COSMIC = new WorldPoint(2384,4410,0);
	WorldArea FIRST_POINT_COSMIC = new WorldArea(new WorldPoint(2372,4398,0),new WorldPoint(2395,4418,0));

	WorldPoint NEARER_ALTAR_COSMIC = new WorldPoint(2406, 4387, 0);
	WorldArea NEARER_COSMIC_POINT = new WorldArea(new WorldPoint(2402, 4383, 0), new WorldPoint(2412, 4391, 0));

	WorldPoint SECOND_CLICK_COSMIC = new WorldPoint(2415,4425,0);
	WorldArea SECOND_POINT_COSMIC = new WorldArea(new WorldPoint(2410,4420,0),new WorldPoint(2421,4428,0));

	WorldPoint NEARER_BANK_COSMIC = new WorldPoint(2383, 4458, 0);

	WorldArea COSMIC_ALTAR = new WorldArea(new WorldPoint(2113,4802,0),new WorldPoint(2173,4862,0));
	WorldPoint OUTSIDE_ALTAR_COSMIC = new WorldPoint(2405,4381,0);




	WorldArea DRAYNOR_BANK = new WorldArea(new WorldPoint(3088,3241,0),new WorldPoint(3097,3247,0));
	WorldPoint FIRST_CLICK_WATER = new WorldPoint(3139,3207,0);
	WorldArea FIRST_POINT_WATER = new WorldArea(new WorldPoint(3131,3202,0),new WorldPoint(3147, 3215,0));

	WorldPoint THIRD_CLICK_WATER = new WorldPoint(3099, 3230, 0);
	WorldArea THIRD_POINT_WATER = new WorldArea(new WorldPoint(3095, 3227, 0), new WorldPoint(3104, 3233, 0));

	WorldPoint SECOND_CLICK_WATER = new WorldPoint(3135,3215,0);
	WorldArea SECOND_POINT_WATER = new WorldArea(new WorldPoint(3130,3211,0),new WorldPoint(3143,3220,0));

	WorldPoint CLICK_NEARER_ALTER = new WorldPoint(3159,3179,0);
	WorldArea POINT_NEARER_ALTER = new WorldArea(new WorldPoint(3155,3176,0),new WorldPoint(3164,3183,0));

	WorldPoint OUTSIDE_ALTAR_WATER = new WorldPoint(3182,3162,0);
	WorldArea WATER_ALTAR = new WorldArea(new WorldPoint(2709,4829,0),new WorldPoint(2731,4842,0));



	WorldArea VARROCK_EAST_BANK = new WorldArea(new WorldPoint(3249, 3416, 0), new WorldPoint(3257, 3424, 0));
	WorldPoint FIRST_CLICK_EARTH = new WorldPoint(3298,3467,0);
	WorldArea FIRST_POINT_EARTH = new WorldArea(new WorldPoint(3295,3464,0),new WorldPoint(3302, 3470,0));
	WorldPoint OUTSIDE_ALTAR_EARTH = new WorldPoint(3302,3477,0);
	WorldArea EARTH_ALTAR = new WorldArea(new WorldPoint(2648,4828,0),new WorldPoint(2666,4847,0));

	WorldPoint SECOND_CLICK_EARTH = new WorldPoint(3281,3428,0);
	WorldArea SECOND_POINT_EARTH = new WorldArea(new WorldPoint(3278,3425,0),new WorldPoint(3285, 3431,0));


	WorldArea FIRE_ALTAR = new WorldArea(new WorldPoint(2568,4830,0),new WorldPoint(2600,4853,0));
	WorldArea CASTLE_WARS_BANK = new WorldArea(new WorldPoint(2435, 3081, 0), new WorldPoint(2445, 3097, 0));

	WorldArea FIRST_POINT_FIRE = new WorldArea(new WorldPoint(3309,3228,0),new WorldPoint(3325, 3249,0));
	WorldArea SECOND_POINT_FIRE =  new WorldArea(new WorldPoint(2435, 3081, 0), new WorldPoint(2445, 3097, 0));



	WorldArea INSIDE_ABYSS_OBSTACLES = new WorldArea(new WorldPoint(3023, 4816, 0), new WorldPoint(3055, 4846, 0));
	WorldArea NATURE_ALTAR = new WorldArea(new WorldPoint(2390,4832,0),new WorldPoint(2413,4851,0));
	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));

	WorldArea WILDERNESS_DITCH = new WorldArea(new WorldPoint(3062,3511,0),new WorldPoint(3120, 3529,0));
	WorldArea ZAMORAK_MAGE =  new WorldArea(new WorldPoint(3100, 3551, 0), new WorldPoint(3111, 3561, 0));
	WorldArea INSIDE_ABYSS = new WorldArea(new WorldPoint(3007, 4803, 0), new WorldPoint(3075, 4867, 0));
	 WorldPoint WILD_HALFWAY = new WorldPoint(3108, 3541, 0);
	 WorldArea IN_WILD_HALFWAY = new WorldArea(new WorldPoint(3104, 3537, 0), new WorldPoint(3113, 3544, 0));
	boolean DidObstacle = false;
	boolean CraftedFirst = false;
	int timeout = 0;
	long sleepLength;
	boolean startTeaks;
	boolean FirstPouch = false;
	boolean SecondPouch = true;
	boolean ThirdPouch = false;
	boolean FirstPouch1 = false;
	boolean SecondPouch1 = true;
	boolean ThirdPouch1 = false;



	int essenceValue;
	//3055 4846
	@Provides
	ARcerConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ARcerConfiguration.class);
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
		FirstInvent = true;
		FirstPouch = false;
		SecondPouch = true;
		ThirdPouch = false;
		FirstPouch1 = false;
		SecondPouch1 = true;
		ThirdPouch1 = false;
		EmptiedFirst = false;
		EmptiedSecond = false;
		EmptiedThird = false;
		overlayManager.remove(overlay);
		Firstime = true;
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
		if(config.useRuneEssence()){
			essenceValue = 1436;
		} else {
			essenceValue = 7936;
		}
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
		if (!configButtonClicked.getGroup().equalsIgnoreCase("ARcer"))
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
				targetMenu = null;
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
		if (!event.getGroup().equals("ARcer"))
		{
			return;
		}
		startTeaks = false;
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

	private ARcerState getBankState()
	{
		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			if (utils.inventoryContains(555)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}

		if (config.runeType() == ARcerRuneType.LAW_RUNE) {
			if (utils.inventoryContains(563)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}


		if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
			if (utils.inventoryContains(564)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}

		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			if (utils.inventoryContains(557)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}

		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			if (utils.inventoryFull()) {
				if (config.mode().equals(COMBINATION_RUNES)) {
					if (utils.inventoryContains(1438) && utils.inventoryContains(556) && checkHasBindingNeck(client.getLocalPlayer()) && checkHasDuelingRing(client.getLocalPlayer())) {
						return WALK_FIRST_POINT;
					} else {
						utils.depositAll();
						return FIND_BANK;
					}
				}
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(554) || utils.inventoryContains(4697)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}

		/*
		if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
			if (utils.inventoryFull()) {
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(554) || utils.inventoryContains(4697)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}*/


		if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
			if (utils.inventoryFull()) {
				if (utils.inventoryContains(7936) || utils.inventoryContains(1436)) {
					return DEPOSIT_ITEMS;
				} else {
					return WALK_TO_MAGE;
				}
			}
			else return WALK_TO_MAGE;
		}

		if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
			if (utils.inventoryContains(561)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && !ThirdPouch) {
					return FILL_POUCH;
				}
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else if(!utils.inventoryFull() && ThirdPouch){
					return WITHDRAW_ITEMS;
				} else if (!utils.inventoryContains(essenceValue)) {
					return WITHDRAW_ITEMS;
				}
			}
		}
		if (config.runeType() == ARcerRuneType.BODY_RUNE) {
			if (utils.inventoryContains(559)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}

		else if(config.runeType() == ARcerRuneType.AIR_RUNE) {
			if (utils.inventoryContains(556)) {
				return DEPOSIT_ITEMS;
			}
			if (!config.runePouches()) {
				if (utils.inventoryFull()) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
			if (config.runePouches()) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && !ThirdPouch) {
					return FILL_POUCH;
				} if (utils.inventoryContains(essenceValue) && utils.inventoryFull() && ThirdPouch) {
					return WALK_FIRST_POINT;
				} else {
					return WITHDRAW_ITEMS;
				}
			}
		}
		return TIMEOUT;
	}

	public ARcerState getState()
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
		else if(client.getLocalPlayer().getAnimation()!=-1 && config.runeType() != ARcerRuneType.ZEAH_BLOOD_RUNE &&  config.runeType() != ARcerRuneType.NATURE_RUNE_ABYSS){
			return ANIMATING;
		}
		else if(client.getLocalPlayer().getAnimation()!=-1 && client.getLocalPlayer().getAnimation() != 7202 && config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE){
			return ANIMATING;
		}
		else {
			return getAirsState();
		}
	}
	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));

			/*targetMenu = new MenuEntry("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY(), false);
			//utils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(bankTarget.getConvexHull().getBounds(), sleepDelay());*/
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
				case FIND_BANK:
					if (config.runeType() == ARcerRuneType.COSMIC_RUNE){
						utils.useGameObject(26711,3, sleepDelay());
					}
					else {

						openBank();
					}
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(556);
						}
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(555);
						}
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(563);
						}
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(564);
						}
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(557);
						}
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(554);
						} else if (config.mode().equals(COMBINATION_RUNES)) {
							depositItem(4697);
						}
					}
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
						if (config.mode().equals(RUNES)) {
							utils.depositAll();
						}
					}
					else if (config.runeType() == ARcerRuneType.BODY_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(559);
						}
					}
					else if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
						if (utils.inventoryContains(1436))
							utils.depositAllOfItem(1436);
						else if (utils.inventoryContains(7936))
							utils.depositAllOfItem(7936);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
					if(config.useStams()) {
						if (client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && checkRunEnergy() < config.minEnergy()) {
							if (utils.inventoryContains(12631)) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043));

								//targetMenu = new MenuEntry("Drink", "<col=ff9040>Stamina potion(1)</col>", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043, false);
								//utils.delayMouseClick(utils.getInventoryWidgetItem(12631).getCanvasBounds(), sleepDelay());
								return;
							} else {
								if (utils.inventoryFull()) {
									utils.depositAll();
									return;
								} else {
									clientThread.invoke(() -> client.invokeMenuAction("", "", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445));

									//targetMenu = new MenuEntry("Withdraw-1", "<col=ff9040>Stamina potion(1)</col>", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445, false);
									//utils.delayMouseClick(utils.getBankItemWidget(12631).getBounds(), sleepDelay());
									return;
								}
							}
						} else if (checkRunEnergy() < config.minEnergyStam()) {
							if (utils.inventoryContains(12631)) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043));

								//targetMenu = new MenuEntry("Drink", "<col=ff9040>Stamina potion(1)</col>", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043, false);
								//utils.delayMouseClick(utils.getInventoryWidgetItem(12631).getCanvasBounds(), sleepDelay());
								return;
							} else {
								if (utils.inventoryFull()) {
									utils.depositAll();
									return;
								} else {
									clientThread.invoke(() -> client.invokeMenuAction("", "", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445));

									//targetMenu = new MenuEntry("Withdraw-1", "<col=ff9040>Stamina potion(1)</col>", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445, false);
									//utils.delayMouseClick(utils.getBankItemWidget(12631).getBounds(), sleepDelay());
									return;
								}
							}
						}
					}
					if (config.runePouches() == true) {
						if (!utils.inventoryContains(5509)){
							utils.withdrawAllItem(5509);
							return;
						}
						if (!utils.inventoryContains(5510)){
							utils.withdrawAllItem(5510);
							return;
						}
						if (!utils.inventoryContains(5512)){
							utils.withdrawAllItem(5512);
							return;
						}
					}
					if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (config.mode() == COMBINATION_RUNES)
						{
							if (!utils.inventoryContains(2552) && !checkHasDuelingRing(player)) {// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
								utils.withdrawItem(2552);
								return;
							}
							if (utils.inventoryContains(2552) && !checkHasDuelingRing(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
							{
								//utils.closeBank();
								clientThread.invoke(() -> client.invokeMenuAction("", "", 2552, 34, utils.getInventoryWidgetItem(2552).getIndex(), 9764864));

								//targetMenu = new MenuEntry("Wear", "Wear", 2552, 34, utils.getInventoryWidgetItem(2552).getIndex(), 9764864, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								return;
							}
							if (!utils.inventoryContains(5521) && !checkHasBindingNeck(player)) {// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
								utils.withdrawItem(5521);
								return;
							}
							if (utils.inventoryContains(5521) && !checkHasBindingNeck(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
							{
								//utils.closeBank();
								clientThread.invoke(() -> client.invokeMenuAction("", "", 5521, 34, utils.getInventoryWidgetItem(5521).getIndex(), 9764864));
								//targetMenu = new MenuEntry("Wear", "Wear", 5521, 34, utils.getInventoryWidgetItem(5521).getIndex(), 9764864, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								return;
							}
							if (!utils.inventoryContains(556)) {
								utils.withdrawAllItem(556);
								return;
							}
							if (!utils.inventoryContains(1438)){
								utils.withdrawItem(1438);
								return;
							}
						}
						else {
							if (!utils.inventoryContains(2552) && !checkHasDuelingRing(player)) {// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
								utils.withdrawItem(2552);
								sleepDelay();
								return;
							}
							if (utils.inventoryContains(2552) && !checkHasDuelingRing(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
							{
								//utils.closeBank();
								clientThread.invoke(() -> client.invokeMenuAction("", "", 2552, 34, 0, 9764864));
								//targetMenu = new MenuEntry("Wear", "Wear", 2552, 34, 0, 9764864, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								return;
							}
						}
					}











					/*
					if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
						if (!utils.inventoryContains(2552) && !checkHasDuelingRing(player)) {// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
							utils.withdrawItem(2552);
							sleepDelay();
							return;
						}
						if (utils.inventoryContains(2552) && !checkHasDuelingRing(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
						{
							//utils.closeBank();
							targetMenu = new MenuEntry("Wear", "Wear", 2552, 34, 0, 9764864, false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							return;
						}
					}*/










					if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						if (!utils.inventoryContains(11978) && !checkHasGlory(player)){// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
							utils.withdrawItem(11978);
							sleepDelay();
							return;
						}
						if (utils.inventoryContains(11978) &&!checkHasGlory(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
						{
							//utils.closeBank();
							clientThread.invoke(() -> client.invokeMenuAction("", "", 11978, 34,  utils.getInventoryWidgetItem(11978).getIndex(), 9764864));
							//targetMenu = new MenuEntry("Wear", "Wear", 11978, 34,  utils.getInventoryWidgetItem(11978).getIndex(), 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							return;
							//utils.findNearestBankNoDepositBoxes();
							//utils.depositAll();
						}
					}
					if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
						if (!utils.inventoryContains(11978) && !checkHasGlory(player)){// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
							utils.withdrawItem(11978);
							sleepDelay();
							return;
						}
						if (utils.inventoryContains(11978) &&!checkHasGlory(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
						{
							//utils.closeBank();
							clientThread.invoke(() -> client.invokeMenuAction("", "", 11978, 34, utils.getInventoryWidgetItem(11978).getIndex(), 9764864));
							//targetMenu = new MenuEntry("Wear", "Wear", 11978, 34, utils.getInventoryWidgetItem(11978).getIndex(), 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							return;
							//utils.findNearestBankNoDepositBoxes();
							//utils.depositAll();
						}
					}

					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
						sleepDelay(); // Nothing to withdraw
						return;
					}
					if(utils.inventoryContains(229)){
						utils.depositAll();
						return;
					}
					if(config.mode().equals(RUNES) || config.mode().equals(COMBINATION_RUNES)  && config.runeType() != ARcerRuneType.MINE_ESS_VARROCK){
						utils.withdrawAllItem(essenceValue);
					}
					timeout = tickDelay();
					break;
				case LEAVE_BOAT:
					targetObject = utils.findNearestGameObject(2415);
					clientThread.invoke(() -> client.invokeMenuAction("", "", 2415, 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

					//targetMenu = new MenuEntry("Cross", "<col=ffff>Gangplank", 2415, 3, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
					//utils.setMenuEntry(targetMenu);
					//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
					timeout = tickDelay();
					break;
				case ENTER_ALTAR:
					if (config.runeType() == ARcerRuneType.BODY_RUNE) {
						if (utils.inventoryContains(1446)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34818, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						if (utils.inventoryContains(1438)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34813, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.WATER_RUNE)
					{
						if (utils.inventoryContains(1444)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34815, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.LAW_RUNE)
					{
						if (utils.inventoryContains(1458)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34820, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.COSMIC_RUNE)
					{
						if (utils.inventoryContains(1454)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34819, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.EARTH_RUNE)
					{
						if (utils.inventoryContains(1440)) {
							useTalismanOnAltar();
						} else {
							utils.useGameObject(34816, 3, sleepDelay());
						}
					}
					if (config.runeType() == ARcerRuneType.FIRE_RUNE)
					{
						//if (utils.inventoryContains(1440)) {
						//	useTalismanOnAltar();
						//} else {
						GameObject obj = utils.findNearestGameObject(34817);
						utils.useGameObjectDirect(obj,sleepDelay(),3);
					}
					/*if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER)
					{
						//if (utils.inventoryContains(1440)) {
						//	useTalismanOnAltar();
						//} else {
						useGameObject(34817, 3);
						//}
					}*/
					if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS)
					{
						count = 0;
						if (config.runePouches() && utils.inventoryContains(5513)){
							NPC Mage = utils.findNearestNpc(4363);
							clientThread.invoke(() -> client.invokeMenuAction("", "", 4363, 11, Mage.getLocalLocation().getSceneX(), Mage.getLocalLocation().getSceneY()));

							//targetMenu = new MenuEntry("", "", 4363, 11, Mage.getLocalLocation().getSceneX(), Mage.getLocalLocation().getSceneY(), false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
						else {
							DidObstacle = false;
							DecorativeObject Rift = utils.findNearestDecorativeObject(24975);
							clientThread.invoke(() -> client.invokeMenuAction("", "", 24975, 3, Rift.getLocalLocation().getSceneX(), Rift.getLocalLocation().getSceneY()));

							//targetMenu = new MenuEntry("", "", 24975, 3, Rift.getLocalLocation().getSceneX(), Rift.getLocalLocation().getSceneY(), false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
						//useDecorativeObject(24975, 3);
						//useGameObjectDirect(Rift, 3); //Enter obstacle, only agility thieving or mining
					}
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						WallObject Obj = utils.findWallObjectWithin(new WorldPoint(3253, 3398, 0),1, 11780);
						if (Obj != null)
						{
							clientThread.invoke(() -> client.invokeMenuAction("", "", 11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY()));

							//targetMenu = new MenuEntry("", "", 11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY(), false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
						 //TODO Check door open/closed
						clientThread.invoke(() -> client.invokeMenuAction("", "", 8110, 12,  0, 0));

						//targetMenu = new MenuEntry("Teleport", "<col=ffff00>Aubury", 8110, 12,  0, 0, false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

					}
					timeout = tickDelay();
					break;
				case CRAFT_BLOCKS:
					if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE) {
						if (utils.inventoryContains(13445)) {
							utils.useGameObject(27979, 3, sleepDelay());
							timeout = tickDelay();
						}
					}
					timeout = tickDelay();
					break;
				case CRAFT_FRAGMENTS:
					if (utils.inventoryContains(13446)) {
						if (Firstime) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 1755, 38, utils.getInventoryWidgetItem(1755).getIndex(), WidgetInfo.INVENTORY.getId()));

							//targetMenu = new MenuEntry("Use", "<col=ff9040>" + itemManager.getItemComposition(1755).getName(), 1755, 38, utils.getInventoryWidgetItem(1755).getIndex(), WidgetInfo.INVENTORY.getId(), false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							Firstime = false;
							break;
						}
						clientThread.invoke(() -> client.invokeMenuAction("", "", 13446,31,utils.getInventoryWidgetItem(13446).getIndex(),WidgetInfo.INVENTORY.getId()));
						//targetMenu = new MenuEntry("Use","<col=ff9040>"+itemManager.getItemComposition(1755).getName()+"<col=ffffff> -> <col=ff9040>"+itemManager.getItemComposition(13446).getName(),13446,31,utils.getInventoryWidgetItem(13446).getIndex(),WidgetInfo.INVENTORY.getId(),false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
						Firstime = true;
						break;
					}
					else {
						break;
					}
				case RUN_TO_ALTAR2:
					utils.walk(new WorldPoint(1716, 3857, 0));
					timeout = tickDelay();
					break;
				case RUN_TO_ALTAR3:
					utils.walk(new WorldPoint(1718, 3833, 0));
					timeout = tickDelay();
					break;
				case RUN_TO_ALTAR1:
					utils.walk(new WorldPoint(1720, 3881, 0));
					timeout = tickDelay();
					break;
				case BACK_TO_MINE:
					if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE) {
						utils.useGameObject(27984, 3, sleepDelay());
					}
					timeout = tickDelay();
					break;
				case CRAFT_RUNES:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.useGameObject(34760, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.useGameObject(34762, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						utils.useGameObject(34767, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
						utils.useGameObject(34766, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE) {
						utils.useGameObject(27978, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.useGameObject(34763, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (config.mode() == COMBINATION_RUNES){
							//targetMenu = new MenuEntry("Use", "Use", 1440, 38, 0, 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

							//targetMenu = new MenuEntry("Use", "<col=ff9040>Earth talisman<col=ffffff> -> <col=ffff>Altar", 34764, 1, 56, 37, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							useTalismanOnAltar();
						}
						else {
							utils.useGameObject(34764, 3, sleepDelay());
						}
					}
					/*else if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
					 	tradePlayer();
					}*/
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
						if (!utils.isAnimating() && !utils.isMoving()) {
							utils.useGameObject(34768, 3, sleepDelay());
						}
					}
					else if (config.runeType() == ARcerRuneType.BODY_RUNE) {
						utils.useGameObject(34765, 3, sleepDelay());
					}
					timeout = tickDelay();
					break;
				case EMPTY_POUCH:
				//	if (!EmptiedFirst && !EmptiedSecond && !EmptiedThird) {

				//		break;
				//	}
					if (!utils.isAnimating() && !utils.isMoving()) {
						if (!EmptiedFirst && !EmptiedSecond) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 5509, 34, utils.getInventoryWidgetItem(5509).getIndex(), 9764864));

							//targetMenu = new MenuEntry("Empty", "<col=ff9040>Small pouch</col>", 5509, 34, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
							timeout = tickDelay();
							EmptiedFirst = true;
							break;
						}
						if (EmptiedFirst && !EmptiedSecond) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 5510, 34, utils.getInventoryWidgetItem(5510).getIndex(), 9764864));

							//targetMenu = new MenuEntry("Empty", "<col=ff9040>Small pouch</col>", 5509, 34, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
							timeout = tickDelay();
							EmptiedSecond = true;
							break;
						}
						if (EmptiedFirst && EmptiedSecond) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 5512, 34, utils.getInventoryWidgetItem(5512).getIndex(), 9764864));

							//targetMenu = new MenuEntry("Empty", "<col=ff9040>Small pouch</col>", 5509, 34, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
							timeout = tickDelay();
							EmptiedThird = true;
							break;
						}

					}
					break;
				case USE_PORTAL:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.useGameObject(34748, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.useGameObject(34750, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.BODY_RUNE) {
						utils.useGameObject(34753, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						if (utils.isItemEquipped(Collections.singleton(11978)) || utils.isItemEquipped(Collections.singleton(11976))|| utils.isItemEquipped(Collections.singleton(1712))|| utils.isItemEquipped(Collections.singleton(1710))|| utils.isItemEquipped(Collections.singleton(1708))|| utils.isItemEquipped(Collections.singleton(1706)))
						{
							clientThread.invoke(() -> client.invokeMenuAction("", "", 4, 57, -1, 25362449));

							//targetMenu = new MenuEntry("Draynor Village", "<col=ff9040>Amulet of glory", 4, 57, -1, 25362449,false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
						//useGameObject(34755, 3);	//TODO: CHANGE!
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
						utils.useGameObject(34754, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.useGameObject(34751, 3, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						/*if (config.useRunner()){

						}*/
						//else {
							if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554)) || utils.isItemEquipped(Collections.singleton(2556)) || utils.isItemEquipped(Collections.singleton(2558)) || utils.isItemEquipped(Collections.singleton(2560)) || utils.isItemEquipped(Collections.singleton(2562)) || utils.isItemEquipped(Collections.singleton(2564)) || utils.isItemEquipped(Collections.singleton(2566))) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", 3, 57, -1, 25362456));

								//targetMenu = new MenuEntry("Castle Wars", "<col=ff9040>Ring of dueling", 3, 57, -1, 25362456, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							}
						//}
					}
					/*else if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
						if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554))|| utils.isItemEquipped(Collections.singleton(2556))|| utils.isItemEquipped(Collections.singleton(2558))|| utils.isItemEquipped(Collections.singleton(2560))|| utils.isItemEquipped(Collections.singleton(2562))|| utils.isItemEquipped(Collections.singleton(2564))|| utils.isItemEquipped(Collections.singleton(2566)))
						{
							targetMenu = new MenuEntry("Castle Wars", "<col=ff9040>Ring of dueling", 3, 57, -1, 25362456,false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
					}*/
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
						if (!utils.isAnimating() && !utils.isMoving()) {
							if (utils.isItemEquipped(Collections.singleton(11978)) || utils.isItemEquipped(Collections.singleton(11976)) || utils.isItemEquipped(Collections.singleton(1712)) || utils.isItemEquipped(Collections.singleton(1710)) || utils.isItemEquipped(Collections.singleton(1708)) || utils.isItemEquipped(Collections.singleton(1706))) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", 2, 57, -1, 25362449));

								//targetMenu = new MenuEntry("Edgeville", "<col=ff9040>Amulet of glory", 2, 57, -1, 25362449, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
							}
							FirstPouch = false;
							SecondPouch = true;
							ThirdPouch = false;
							FirstPouch1 = false;
							SecondPouch1 = true;
							ThirdPouch1 = false;
							FirstInvent = true;
							EmptiedFirst = false;
							EmptiedSecond = false;
							EmptiedThird = false;
							break;
						}
					}
					else if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK){
						Set<Integer> PORTAL = Set.of(NpcID.PORTAL_3088, NpcID.PORTAL_3086, NullNpcID.NULL_9412);
						targetNPC = utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 15, PORTAL);

						TileObject NearestPortal = utils.findNearestObject(34779, 34825);
						if (targetNPC != null) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));

							//targetMenu = new MenuEntry("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(targetNPC.getConvexHull().getBounds(), sleepDelay());
							timeout = tickDelay();
							break;
						}
						else if (NearestPortal != null){
							utils.useGameObject(NearestPortal.getId(), 3, sleepDelay());
							timeout = tickDelay();
							break;
						}
						//useGameObject(34825, 3);
						//else if (utils.findNearestObject(34779) != null)//
						//useGameObject(34779, 3);
					}
					timeout = tickDelay();
					break;
				case FILL_POUCH:
					if (!utils.inventoryFull() && !utils.inventoryContains(essenceValue)){
						utils.withdrawAllItem(essenceValue);
						break;
					}
					if (!utils.inventoryContains(5509)){
						utils.withdrawAllItem(5509);
						break;
					}
					if (!utils.inventoryContains(5510)){
						utils.withdrawAllItem(5510);
						break;
					}
					if (!utils.inventoryContains(5512)){
						utils.withdrawAllItem(5512);
						break;
					}
					if (utils.inventoryContains(5509) && !FirstPouch){
						clientThread.invoke(() -> client.invokeMenuAction("", "",  5509, 33, utils.getInventoryWidgetItem(5509).getIndex(), 9764864));

						//targetMenu = new MenuEntry("Fill", "<col=ff9040>Small pouch</col>", 5509, 33, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
						FirstPouch = true;
						timeout = tickDelay();
						break;
					}
					if (utils.inventoryContains(5510) && SecondPouch && FirstPouch){
						clientThread.invoke(() -> client.invokeMenuAction("", "",  5510, 33, utils.getInventoryWidgetItem(5510).getIndex(), 9764864));

						//targetMenu = new MenuEntry("Fill", "<col=ff9040>Small pouch</col>", 5509, 33, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
						SecondPouch = false;
						timeout = tickDelay();
						break;
					}
					if (utils.inventoryContains(5512) && !SecondPouch && FirstPouch){
						clientThread.invoke(() -> client.invokeMenuAction("", "",  5512, 33, utils.getInventoryWidgetItem(5512).getIndex(), 9764864));

						//targetMenu = new MenuEntry("Fill", "<col=ff9040>Small pouch</col>", 5509, 33, utils.getInventoryWidgetItem(5509).getIndex(), 9764864, false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(utils.getRandomNullPoint(), sleepDelay());
						ThirdPouch = true;
						timeout = tickDelay();
						break;
					}
					break;
				case WALK_THIRD_POINT:
					if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(THIRD_CLICK_WATER);
					}
					/*else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						utils.walk(THIRD_CLICK_LAW, 2, sleepDelay());
					}*/
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE)
					{
						utils.walk(NEARER_BANK_COSMIC);
					}
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS)
					{
						NPC Mage = utils.findNearestNpc(2581);

						clientThread.invoke(() -> client.invokeMenuAction("", "",  Mage.getIndex(), MenuAction.NPC_FOURTH_OPTION.getId(), Mage.getLocalLocation().getX(), Mage.getLocalLocation().getY()));

						//targetMenu = new MenuEntry("", "", Mage.getIndex(), MenuAction.NPC_FOURTH_OPTION.getId(), Mage.getLocalLocation().getX(), Mage.getLocalLocation().getY(), false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

						//utils.interactNPC(MenuAction.NPC_FOURTH_OPTION.getId(), sleepDelay(), 2581);
					}
					timeout = tickDelay();
					break;
				case WALK_NEARER_ALTER:
					if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(CLICK_NEARER_ALTER);
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE)
					{
						clientThread.invoke(() -> client.invokeMenuAction("", "",  3594, 11, 0, 0));

						//targetMenu = new MenuEntry("Take-boat", "<col=ffff00>Monk of Entrana", 3594, 11, 0, 0,false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						//utils.walk(NEARER_ALTAR_LAW, 2, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE)
					{
						utils.walk(NEARER_ALTAR_COSMIC);
					}
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS)
					{
						FirstPouch = false;
						SecondPouch = true;
						ThirdPouch = false;
						FirstPouch1 = false;
						SecondPouch1 = true;
						ThirdPouch1 = false;
						FirstInvent = true;
						EmptiedFirst = false;
						EmptiedSecond = false;
						EmptiedThird = false;
						utils.walk(WILD_HALFWAY);
					}
					else if (config.runeType() == ARcerRuneType.BODY_RUNE)
					{
						utils.walk(new WorldPoint(3071, 3454, 0));
					}
					timeout = tickDelay();
					break;
				case WALK_TO_MAGE:
					if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS){
					utils.walk(new WorldPoint(3106, 3557, 0)); // Zamorak Mage Wild
					}
					WallObject Obj = utils.findWallObjectWithin(new WorldPoint(3253, 3398, 0),1, 11780);
					if (Obj != null)
					{
						clientThread.invoke(() -> client.invokeMenuAction("", "",  11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY()));

						//targetMenu = new MenuEntry("", "", 11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY(), false);
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						timeout = tickDelay();
						break;
					}
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						utils.walk(new WorldPoint(3253, 3401, 0)); // Aubury Varrock
					}
					timeout = tickDelay();
					break;
				case WALK_TO_BANK:
					if (config.runeType() == ARcerRuneType.BODY_RUNE){
						utils.walk(new WorldPoint(3086, 3488, 0));
					}
					timeout = tickDelay();
					break;
				case MINE_ESSENCE:
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						utils.useGameObject(34773, 3, sleepDelay());
					}
					if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE){
						GameObject DenseEssence = getDenseEssence();
						//GameObject DenseEssence = utils.findNearestGameObject(NullObjectID.NULL_8981, NullObjectID.NULL_10796);
						utils.useGameObjectDirect(DenseEssence, sleepDelay(), 3);
					}
					timeout = tickDelay();
					break;
				case JUMP_OBSTACLE:
					if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE){
						//utils.findNearestGroundObject(34741);
						utils.useGroundObject(34741, 3, sleepDelay());
					}
					timeout = tickDelay();
					break;
				case JUMP_OBSTACLE2:
					if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE){
						utils.useGroundObject(27984, 3, sleepDelay());
					}
					timeout = tickDelay();
					break;
				case WALK_SECOND_POINT:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.walk(SECOND_CLICK_AIR);
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(SECOND_CLICK_WATER);
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						utils.walk(new WorldPoint(3046, 3235, 0));
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
						utils.walk(SECOND_CLICK_COSMIC);
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.walk(SECOND_CLICK_EARTH);
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
						List<GameObject> objects = utils.getGameObjects( 26252, 26188, 26189, 26187, 26250, 27208, 26192, 26251, 26574, 26190, 26191);
						if (!objects.isEmpty() && count < objects.size()){
							count++;
							GameObject selectedObject = utils.findNearestGameObject(objects.get(count).getId());
							ObjectComposition ImposterDef = utils.getImpostorDefinition(selectedObject.getId());
							if (ImposterDef.getName().contains("Eyes")){
								utils.useGameObjectDirect((GameObject) ImposterDef, sleepDelay(), 3);
								count = 0;
								break;
							}
							else if (ImposterDef.getName().contains("Gap")){
								utils.useGameObjectDirect(selectedObject, sleepDelay(), 3);
								count = 0;
								break;
							}
							else if (ImposterDef.getName().contains("Rock")){
								utils.useGameObjectDirect(selectedObject, sleepDelay(), 3);
								count = 0;
								break;
							}

						}
						timeout = tickDelay();
						break;
						/*List<GameObject> objects = utils.getGameObjects( 26252, 26188, 26189, 26187, 26250, 27208, 26192, 26251, 26574, 26190, 26191);
						if (!objects.isEmpty() && count < objects.size()){
							count++;
							GameObject selectedObject = objects.get(count);
							ObjectComposition ImposterDef = utils.getImpostorDefinition(selectedObject.getId());
							if (ImposterDef.getName().contains("Eyes")){
								utils.useGameObjectDirect((GameObject) ImposterDef, sleepDelay(), 3);
								break;
							}
							else if (ImposterDef.getName().contains("Gap")){
								utils.useGameObjectDirect(selectedObject, sleepDelay(), 3);
								break;
							}
							else if (ImposterDef.getName().contains("Rock")){
								utils.useGameObjectDirect(selectedObject, sleepDelay(), 3);
								break;
							}

						}
						break;*/
					}

				case WALK_FIRST_POINT:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.walk(FIRST_CLICK_AIR);
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(FIRST_CLICK_WATER);
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						utils.walk(new WorldPoint(3058, 3253, 0));
					}
					else if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
						utils.walk(FIRST_CLICK_COSMIC);
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.walk(FIRST_CLICK_EARTH);
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554))|| utils.isItemEquipped(Collections.singleton(2556))|| utils.isItemEquipped(Collections.singleton(2558))|| utils.isItemEquipped(Collections.singleton(2560))|| utils.isItemEquipped(Collections.singleton(2562))|| utils.isItemEquipped(Collections.singleton(2564))|| utils.isItemEquipped(Collections.singleton(2566)))
						{
							utils.closeBank();
							clientThread.invoke(() -> client.invokeMenuAction("", "",  2, 57, -1, 25362456));

							//targetMenu = new MenuEntry("Duel Arena", "<col=ff9040>Ring of dueling", 2, 57, -1, 25362456,false);
							//utils.setMenuEntry(targetMenu);
							//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

						}
					}
					/*else if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
						if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554))|| utils.isItemEquipped(Collections.singleton(2556))|| utils.isItemEquipped(Collections.singleton(2558))|| utils.isItemEquipped(Collections.singleton(2560))|| utils.isItemEquipped(Collections.singleton(2562))|| utils.isItemEquipped(Collections.singleton(2564))|| utils.isItemEquipped(Collections.singleton(2566)))
						{
							utils.closeBank();
							targetMenu = new MenuEntry("Duel Arena", "<col=ff9040>Ring of dueling", 2, 57, -1, 25362456,false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

						}
					}*/
					else if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {

						utils.useGameObject(23271,3,sleepDelay());
					}
				case CRAFT_TIARAS:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
						client.setSelectedItemSlot(utils.getInventoryWidgetItem(1438).getIndex());
						client.setSelectedItemID(1438);
						utils.useGameObject(34760, 1, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
						client.setSelectedItemSlot(utils.getInventoryWidgetItem(1444).getIndex());
						client.setSelectedItemID(1444);
						utils.useGameObject(34762, 1, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.LAW_RUNE) {
						client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
						client.setSelectedItemSlot(utils.getInventoryWidgetItem(1458).getIndex());
						client.setSelectedItemID(1458);
						utils.useGameObject(34767, 1, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
						client.setSelectedItemSlot(utils.getInventoryWidgetItem(1440).getIndex());
						client.setSelectedItemID(1440);
						utils.useGameObject(34763, 1, sleepDelay());
					}
					break;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && startTeaks)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}

	private ARcerState getAirsState()
	{
		if (config.runeType() == ARcerRuneType.AIR_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_AIR)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(AIR_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(AIR_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(AIR_ALTAR)  && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}  else if (player.getWorldArea().intersectsWith(AIR_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_AIR)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_AIR)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(FALADOR_EAST_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					} else if (player.getWorldArea().intersectsWith(FALADOR_EAST_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}

		if (config.runeType() == ARcerRuneType.COSMIC_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_COSMIC)) {
						return WALK_NEARER_ALTER;
					} else if (player.getWorldArea().intersectsWith(NEARER_COSMIC_POINT)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(COSMIC_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(COSMIC_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(COSMIC_ALTAR)  && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					} else if (player.getWorldArea().intersectsWith(COSMIC_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_COSMIC)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_COSMIC)) {
						return WALK_THIRD_POINT;
					} /*else if (player.getWorldArea().intersectsWith(ZANARIS_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(ZANARIS_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					} else if (player.getWorldArea().intersectsWith(ZANARIS_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}

		if (config.runeType() == ARcerRuneType.ZEAH_BLOOD_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(1755) && !utils.inventoryContains(7938)) {	//Doesnt have fragments
					if (utils.inventoryContains(13446)) {
						return CRAFT_FRAGMENTS;	// ^ has dark blocks
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1749, 3841, 0), new WorldPoint(1782, 3873, 0))) && !utils.inventoryFull()) {
						FirstInventory = true;
						CraftedFirst = false;
						return MINE_ESSENCE;
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1749, 3841, 0), new WorldPoint(1782, 3873, 0))) && utils.inventoryFull() && utils.inventoryContains(13445)) {
						FirstInventory = true;
						CraftedFirst = false;
						return JUMP_OBSTACLE;
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1755, 3873, 0), new WorldPoint(1767, 3878, 0))) && utils.inventoryContains(13445)) {
						FirstInventory = true;
						CraftedFirst = false;
						return RUN_TO_ALTAR1; // ^ jumped obstacle & dark altar not visible
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && utils.inventoryContains(13445)) {
						FirstInventory = true;
						CraftedFirst = false;
						return CRAFT_BLOCKS;	// ^ near dark altar has blocks
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && !utils.inventoryContains(13445) && !utils.inventoryContains(13446)) {
						FirstInventory = true;
						CraftedFirst = false;
						return JUMP_OBSTACLE;	// ^ near dark altar has no essence
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3820, 0), new WorldPoint(1735, 3841, 0))) && utils.inventoryContains(13446)) {
						FirstInventory = true;
						CraftedFirst = false;
						return CRAFT_FRAGMENTS;	// ^ Blood altar
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3820, 0), new WorldPoint(1735, 3841, 0))) && !utils.inventoryContains(13446)) {
						FirstInventory = true;
						CraftedFirst = false;
						return JUMP_OBSTACLE2;	// ^ Blood altar
					}
				}
				if (utils.inventoryContains(1755) && utils.inventoryContains(7938)) { // Has Fragments
					/*if (utils.inventoryContains(13446)) {
						return CRAFT_FRAGMENTS;	// ^ has dark blocks
					} else*/
					if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && !utils.inventoryContains(13445) && !utils.inventoryContains(13446) && FirstInventory) {
						FirstInventory = false; // ^ near dark altar has no essence 1st invent
						return JUMP_OBSTACLE;	// ^ near dark altar has no essence 1st invent
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && utils.inventoryContains(13446) && FirstInventory) {
						return CRAFT_FRAGMENTS;	// ^ near dark altar has blocks
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && !utils.inventoryContains(13445) && utils.inventoryFull() && utils.inventoryContains(13446) && !FirstInventory) {
						return RUN_TO_ALTAR2;	//near dark altar done crafting
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1749, 3841, 0), new WorldPoint(1782, 3873, 0))) && !utils.inventoryFull()) {
						FirstInventory = false;
						return MINE_ESSENCE;
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1749, 3841, 0), new WorldPoint(1782, 3873, 0))) && utils.inventoryFull() && utils.inventoryContains(13445)) {
						FirstInventory = false;
						return JUMP_OBSTACLE;
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1755, 3873, 0), new WorldPoint(1767, 3878, 0))) && utils.inventoryContains(13445)) {
						FirstInventory = false;
						return RUN_TO_ALTAR1; // ^ jumped obstacle & dark altar not visible
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3868, 0), new WorldPoint(1727, 3890, 0))) && utils.inventoryContains(13445)) {
						FirstInventory = false;
						return CRAFT_BLOCKS;	// ^ near dark altar has blocks
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1711, 3849, 0), new WorldPoint(1724, 3864, 0))) && !utils.inventoryContains(13445)) {
						FirstInventory = false;
						return RUN_TO_ALTAR3;	// ^ near blood altar
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3820, 0), new WorldPoint(1724, 3864, 0))) && utils.inventoryContains(13446) && utils.inventoryFull()) {
						FirstInventory = false;
						CraftedFirst = true;
						return CRAFT_RUNES;	// ^ near blood altar
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3820, 0), new WorldPoint(1724, 3864, 0))) && !utils.inventoryContains(13446) && CraftedFirst) {
						FirstInventory = false;
						CraftedFirst = true;
						return CRAFT_RUNES;	// ^ near blood altar
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(1708, 3820, 0), new WorldPoint(1735, 3841, 0))) && utils.inventoryContains(13446) && CraftedFirst) {
						FirstInventory = false;
						return CRAFT_FRAGMENTS;	// ^ Blood altar
					}
				}

			}
			return UNHANDLED_STATE;
		}

		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_WATER)) {
						return WALK_NEARER_ALTER;
					} else if (player.getWorldArea().intersectsWith(POINT_NEARER_ALTER)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(WATER_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(WATER_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(WATER_ALTAR)  && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					} else if (player.getWorldArea().intersectsWith(WATER_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_WATER)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_WATER)) {
						return WALK_THIRD_POINT;
					} else if (player.getWorldArea().intersectsWith(THIRD_POINT_WATER)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(DRAYNOR_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					} else if (player.getWorldArea().intersectsWith(DRAYNOR_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}


		if (config.runeType() == ARcerRuneType.LAW_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3042, 3246, 0), new WorldPoint(3064, 3261, 0)))) {
						return WALK_SECOND_POINT;	//Port Sarim Docks ^
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3034, 3231, 0), new WorldPoint(3052, 3241, 0)))) {
						return WALK_NEARER_ALTER;	//Port Sarim Docks near monks ^
					}
					else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2824, 3327, 1), new WorldPoint(2841, 3333, 1)))) {
						return LEAVE_BOAT;	//On boat in Entrana ^
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2811, 3323, 0), new WorldPoint(2874, 3392, 0)))) {
						return ENTER_ALTAR;	//Off boat in Entrana ^
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2440, 4808, 0), new WorldPoint(2479, 4843, 0)))) {
						return CRAFT_RUNES; 	// Law Altar ^
					}
				} else {
					if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2440, 4808, 0), new WorldPoint(2479, 4843, 0))) && !EmptiedThird) {
						return EMPTY_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2440, 4808, 0), new WorldPoint(2479, 4843, 0))) && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}
					else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2440, 4808, 0), new WorldPoint(2479, 4843, 0))) && EmptiedThird) {
						return USE_PORTAL;
					}
					/*else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(VARROCK_EAST_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3085, 3237, 0), new WorldPoint(3114, 3258, 0)))) {
						return FIND_BANK;
					}

				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}


		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_EARTH)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(EARTH_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(EARTH_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(EARTH_ALTAR) && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}
					else if (player.getWorldArea().intersectsWith(EARTH_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					}
					/*else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(VARROCK_EAST_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(VARROCK_EAST_BANK)) {
						return FIND_BANK;
					}
					else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_EARTH)) {
						return WALK_SECOND_POINT;
					}
					else if (player.getWorldArea().intersectsWith(SECOND_POINT_EARTH)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}
		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			//utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(7936) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK) && utils.inventoryContains(1438) && utils.inventoryContains(556) && checkHasBindingNeck(client.getLocalPlayer()) && checkHasDuelingRing(client.getLocalPlayer())) {
						return WALK_FIRST_POINT;
					} //if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK) && utils.inventoryContains(1438) && utils.inventoryContains(556) && checkHasBindingNeck(client.getLocalPlayer()) && checkHasDuelingRing(client.getLocalPlayer()) && !ThirdPouch) {
						//return FIND_BANK;
					//}
				if (player.getWorldArea().intersectsWith(FIRST_POINT_FIRE)) {
						return ENTER_ALTAR;
					} if (player.getWorldArea().intersectsWith(FIRE_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(FIRE_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(FIRE_ALTAR) && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}  else if (player.getWorldArea().intersectsWith(FIRE_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					}
					/*else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
		}
		/*if (config.runeType() == ARcerRuneType.FIRE_ALTAR_RUNNER) {
			utils.setMenuEntry(null);
			if (!config.mode().equals(TIARAS)) {
				if (utils.inventoryContains(essenceValue)) {
					if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK) && utils.inventoryContains(1438) && utils.inventoryContains(556) && checkHasBindingNeck(client.getLocalPlayer()) && checkHasDuelingRing(client.getLocalPlayer())) {
						return WALK_FIRST_POINT;
					} else if (player.getWorldArea().intersectsWith(FIRST_POINT_FIRE)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(FIRE_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(FIRE_ALTAR)) {
						return USE_PORTAL; // teleport away instead
					}  else if (player.getWorldArea().intersectsWith(SECOND_POINT_FIRE)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
		}*/
		if (config.runeType() == ARcerRuneType.BODY_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return WALK_NEARER_ALTER;
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3042, 3435, 0), new WorldPoint(3079, 3464, 0)))) {
						return ENTER_ALTAR;
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2502, 4822, 0), new WorldPoint(2543, 4860, 0)))) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2502, 4822, 0), new WorldPoint(2543, 4860, 0))) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2502, 4822, 0), new WorldPoint(2543, 4860, 0))) && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}  else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(2502, 4822, 0), new WorldPoint(2543, 4860, 0))) && EmptiedThird) {
						return USE_PORTAL;
					}
					/*else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3042, 3435, 0), new WorldPoint(3079, 3464, 0)))) {
						return WALK_TO_BANK;
					}
					else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}







		if (config.runeType() == ARcerRuneType.NATURE_RUNE_ABYSS) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue) && utils.inventoryFull()) {
					if (player.getWorldArea().intersectsWith(WILDERNESS_DITCH)) {
						return WALK_NEARER_ALTER;
					}  else if (player.getWorldArea().intersectsWith(IN_WILD_HALFWAY)) {
						return WALK_TO_MAGE;
					} else if (player.getWorldArea().intersectsWith(ZAMORAK_MAGE)) {
						return WALK_THIRD_POINT;
					} else if (player.getWorldArea().intersectsWith(INSIDE_ABYSS)  && !player.getWorldArea().intersectsWith(INSIDE_ABYSS_OBSTACLES)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(INSIDE_ABYSS_OBSTACLES)){
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(NATURE_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(NATURE_ALTAR) && !EmptiedThird) {
						return EMPTY_POUCH;
					} else if (player.getWorldArea().intersectsWith(NATURE_ALTAR) && utils.inventoryContains(essenceValue) && EmptiedThird && EmptiedFirst && EmptiedSecond){
						return CRAFT_RUNES;
					}  else if (player.getWorldArea().intersectsWith(NATURE_ALTAR) && EmptiedThird) {
						return USE_PORTAL;
					}
					/*else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK)) {
						return FIND_BANK;
					}*/
					else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(essenceValue) && !ThirdPouch) {
						return FILL_POUCH;
					}
					else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(essenceValue) && !utils.inventoryFull()) {
						return FIND_BANK;
					}
					else if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && !utils.inventoryContains(essenceValue)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}

		if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
			utils.setMenuEntry(null);
			if (!utils.inventoryContains(essenceValue)) {
				if (!utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return ENTER_ALTAR; // Near Aubury
				}  else {//if (!utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(14535, 1610, 0), new WorldPoint(14606, 1684, 0)))) {
					return MINE_ESSENCE;
				}
			} else {
				if (utils.inventoryFull() && !player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return USE_PORTAL;
				}else if (utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return FIND_BANK;
				}
			}
			return UNHANDLED_STATE;
		}
		return UNHANDLED_STATE;
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event){
		if(targetMenu!=null){
			menuAction(event,targetMenu.getOption(), targetMenu.getTarget(), targetMenu.getIdentifier(), targetMenu.getMenuAction(),
					targetMenu.getParam0(), targetMenu.getParam1());
			targetMenu = null;
		}
	}

	public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1)
	{
		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setActionParam(param0);
		menuOptionClicked.setWidgetId(param1);
	}

	private void useTalismanOnAltar()
	{
		if (config.runeType() == ARcerRuneType.AIR_RUNE) {
			targetObject = utils.findNearestGameObject(34813);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1438).getIndex());
				client.setSelectedItemID(1438);
				clientThread.invoke(() -> client.invokeMenuAction("", "",  targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

				/*targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}*/

			}
		}
		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			targetObject = utils.findNearestGameObject(34764);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1438).getIndex());
				client.setSelectedItemID(1438);
				clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

				/*targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}
*/
			}
		}
		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			targetObject = utils.findNearestGameObject(34815);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1444).getIndex());
				client.setSelectedItemID(1444);
				clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

				/*targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}*/

			}
		}
		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			targetObject = utils.findNearestGameObject(34816);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1440).getIndex());
				client.setSelectedItemID(1440);
				clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY()));

				/*targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}*/

			}
		}
	}

	/*
	private void tradePlayer () {
		final List<Player> players = client.getPlayers();
		for (final Player player : players)
		{
			if (player.getName().contains(config.runnerName()))
			{
				targetMenu = new MenuEntry("Trade with", "", player.getPlayerId(), 2047, 0, 0, false);
				utils.setMenuEntry(targetMenu);
				utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
			}
		}
	}*/


	private Point getRandomNullPoint()
	{
		if(client.getWidget(161,34)!=null){
			Rectangle nullArea = client.getWidget(161,34).getBounds();
			return new Point ((int)nullArea.getX()+utils.getRandomIntBetweenRange(0,nullArea.width), (int)nullArea.getY()+utils.getRandomIntBetweenRange(0,nullArea.height));
		}

		return new Point(client.getCanvasWidth()-utils.getRandomIntBetweenRange(0,2),client.getCanvasHeight()-utils.getRandomIntBetweenRange(0,2));
	}

	private void depositItem(int id)
	{
		clientThread.invoke(() -> client.invokeMenuAction("", "", 8, 57, utils.getInventoryWidgetItem(id).getIndex(),983043));

		//targetMenu = new MenuEntry("", "", 8, 57, utils.getInventoryWidgetItem(id).getIndex(),983043,false);
		//utils.setMenuEntry(targetMenu);
		//utils.delayMouseClick(utils.getInventoryWidgetItem(id).getCanvasBounds(),sleepDelay());
	}

	private int checkRunEnergy()
	{
		try{
			return client.getEnergy();
		} catch (Exception ignored) {

		}
		return 0;
	}
}
