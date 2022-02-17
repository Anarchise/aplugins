
package net.runelite.client.plugins.abankstander;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.discord.DiscordPresence;
import net.runelite.client.discord.DiscordService;
import net.runelite.client.discord.events.DiscordReady;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
//import static net.runelite.client.plugins.afletcher.AFletcherState.*;
import static net.runelite.client.plugins.abankstander.ABankStanderState.*;
@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "ABankStander",
	description = "Does various tasks for you."
)
@Slf4j
public class ABankStanderPlugin extends Plugin
{
	@Inject
	public ReflectBreakHandler chinBreakHandler;

	@Inject
	ClientThread clientThread;

	@Inject
	private Client client;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private NPCManager npcManager;



	@Inject
	private ItemManager itemManager;

	@Inject
	private ABankStanderConfig configbs;

	@Inject
	private ABankStanderOverlay overlaybs;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	boolean firstTime;
	ABankStanderState state;
	boolean startFireMaker;
	boolean deposited = false;
	int timeout = 0;
	boolean walkAction;
 	boolean second = false;
	 boolean third = false;
	int coordX;
	int coordY;
	GameObject targetObject;
	final Set<GameObject> fireObjects = new HashSet<>();
	final Set<Integer> requiredItems = new HashSet<>();
	boolean[] pathStates;
	WorldPoint FIRST_STEP = new WorldPoint(3007,3336,0);
	WorldPoint SECOND_STEP = new WorldPoint(3007,3346,0);
	int ore1 = 0;
	int ore2 = 0;
	int barOpcode;
	@Provides
	ABankStanderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ABankStanderConfig.class);
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{

		chinBreakHandler.unregisterPlugin(this);
	}

	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(false, configbs.sleepDelayMin(), configbs.sleepDelayMax(), configbs.sleepDelayDev(), configbs.sleepDelayTarg());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(false, configbs.tickDelayMin(), configbs.tickDelayMax(), configbs.tickDelayDev(), configbs.tickDelayTarg());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}
	private void CookFood()
	{
		targetObject = utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(),25,configbs.rangeId());
		if(targetObject!=null){
			clientThread.invoke(() -> client.invokeMenuAction("", "",targetObject.getId(), MenuAction.CC_OP.getId(), -1, 17694733));
		} else {
			utils.sendGameMessage("Cooker not found.");
		}
	}
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("AFletcher"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startFireMaker)
			{
				startUp();
				startFireMaker = true;
				targetMenu = null;
				firstTime = true;
				botTimer = Instant.now();
				overlayManager.add(overlaybs);
				chinBreakHandler.startPlugin(this);
			} else {
				chinBreakHandler.stopPlugin(this);
				overlayManager.remove(overlaybs);
				startFireMaker=false;
				fireObjects.clear();
				pathStates = null;
				requiredItems.clear();
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		if (chinBreakHandler.isBreakActive(this)){
			return;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			chinBreakHandler.startBreak(this);
		}
		int l = 0;
		if (!startFireMaker)
		{
			return;
		}
		if (!client.isResized())
		{
			utils.sendGameMessage("client must be set to resizable");
			startFireMaker = false;
			return;
		}
		player = client.getLocalPlayer();
		if(player==null){
			state = NULL_PLAYER;
			return;
		}
		if(configbs.Type() != ABankStanderType.HIGH_ALCHER && configbs.Type() != ABankStanderType.COOK_KARAMBWANS &&player.getAnimation()!=-1){
			state = ANIMATING;
			timeout=tickDelay();
			return;
		}
		if(timeout>0){
			utils.handleRun(30, 20);
			timeout--;
			return;
		}
		if (configbs.Type() == ABankStanderType.Fletcher) {
			if (!utils.isBankOpen() && !utils.inventoryContains(configbs.logId())) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}
			if (!utils.isBankOpen()) {
				if (firstTime) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",946, 38, utils.getInventoryWidgetItem(946).getIndex(), 9764864));
					firstTime = false;
					state = ACTION_FIRST;
					return;
				}
				if (configbs.fletchType() == ABankStanderTypeFletching.SHORTBOWS) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.logId(), 31, utils.getInventoryWidgetItem(configbs.logId()).getIndex(), 9764864));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to make?")) {
							firstTime = true;
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694735));
						}
					}
				}
				if (configbs.fletchType() == ABankStanderTypeFletching.LONBOWS) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.logId(), 31, utils.getInventoryWidgetItem(configbs.logId()).getIndex(), 9764864));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to make?")) {
							firstTime = true;
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694736));
						}
					}
				}
				if (configbs.fletchType() == ABankStanderTypeFletching.STOCKS) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.logId(), 31, utils.getInventoryWidgetItem(configbs.logId()).getIndex(), 9764864));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to make?")) {
							firstTime = true;
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694737));
						}
					}
				}
				if (configbs.fletchType() == ABankStanderTypeFletching.SHIELDS) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.logId(), 31, utils.getInventoryWidgetItem(configbs.logId()).getIndex(), 9764864));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to make?")) {
							firstTime = true;
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694738));
						}
					}
				}
				timeout = tickDelay();
				state = ACTION;
				return;
			}

			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(946) && utils.inventoryContains(configbs.logId())) {
				closeBank();
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(configbs.logId())) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(946)) {
				utils.withdrawItem(946);
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_TOOL;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(configbs.logId())) {
				utils.withdrawAllItem(configbs.logId());
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}

		if (configbs.Type() == ABankStanderType.SMITHER) {
			if (configbs.barType() == ABankStanderBarType.Bronze) {
				ore1 = 2349;
			} else if (configbs.barType() == ABankStanderBarType.Iron) {
				ore1 = 2351;
			} else if (configbs.barType() == ABankStanderBarType.Steel) {
				ore1 = 2353;
			} else if (configbs.barType() == ABankStanderBarType.Gold) {
				ore1 = 2357;
			} else if (configbs.barType() == ABankStanderBarType.Mithril) {
				ore1 = 2359;
			} else if (configbs.barType() == ABankStanderBarType.Adamant) {
				ore1 = 2361;
			} else if (configbs.barType() == ABankStanderBarType.Rune) {
				ore1 = 2363;
			} else {
				ore1 = 0;
			}

			if (configbs.toSmith() == ABankStanderTypeToSmith.DART_TIPS) {
				barOpcode = 20447261;
				ore2 = 1;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.LIMBS) {
				barOpcode = 20447267;
				ore2 = 1;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.PLATELEGS) {
				barOpcode = 20447252;
				ore2 = 3;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.PLATESKIRT) {
				barOpcode = 20447253;
				ore2 = 3;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.PLATEBODY) {
				barOpcode = 20447254;
				ore2 = 5;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.TWOHAND_SWORD) {
				barOpcode = 20447245;
				ore2 = 3;
			} else if (configbs.toSmith() == ABankStanderTypeToSmith.BOLTS) {
				barOpcode = 20447266;
				ore2 = 1;
			} else {
				barOpcode = 0;
			}


			if (ore1 != 0) {
				if (!utils.isBankOpen() && !utils.inventoryItemContainsAmount(ore1, ore2, false, false)) {
					openNearestBank();
					timeout = tickDelay();
					state = OPEN_BANK;
					return;
				}

				if (!utils.isBankOpen()  && utils.inventoryItemContainsAmount(ore1, ore2, false, false)) {
					useGameObject(2097, 3);
					if (client.getWidget(312, 1) != null) {
						clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, barOpcode));
						firstTime = true;
					}
					timeout = tickDelay();
					state = ACTION;
					return;
				}

				if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryItemContainsAmount(ore1, ore2, false, false)) {
					closeBank();
					deposited = false;
					firstTime = true;
					timeout = tickDelay();
					state = CLOSE_BANK;
					return;
				}
				if (utils.isBankOpen() && !deposited && !utils.inventoryFull() && !utils.inventoryItemContainsAmount(ore1, ore2, false, false)) {
					utils.depositAll();
					deposited = true;
					firstTime = true;
					timeout = tickDelay();
					state = DEPOSIT_ITEMS;
					return;
				}
				if (utils.isBankOpen() && !utils.inventoryContains(2347)) {
					utils.withdrawItem(2347);
					//timeout = tickDelay();
					state = WITHDRAW_TOOL;
					return;
				}
				if (utils.isBankOpen() && !utils.inventoryItemContainsAmount(ore1, ore2, false, false)) {
					if (configbs.barType() == ABankStanderBarType.Bronze) {
						utils.withdrawAllItem(2349);
					}
					else if (configbs.barType() == ABankStanderBarType.Iron) {
						utils.withdrawAllItem(2351);
					}
					else if (configbs.barType() == ABankStanderBarType.Steel) {
						utils.withdrawAllItem(2353);
					}
					else if (configbs.barType() == ABankStanderBarType.Gold) {
						utils.withdrawAllItem(2357);
					}
					else if (configbs.barType() == ABankStanderBarType.Mithril) {
						utils.withdrawAllItem(2359);
					}
					else if (configbs.barType() == ABankStanderBarType.Adamant) {
						utils.withdrawAllItem(2361);
					} else if (configbs.barType() == ABankStanderBarType.Rune) {
						utils.withdrawAllItem(2363);
					}
					firstTime = true;
					timeout = tickDelay();
					state = WITHDRAW_ITEMS;
					return;
				}
				state = NOT_SURE;
			}

		}

		if (configbs.Type() == ABankStanderType.SMELTER) {
			if (configbs.barType1() == ABankStanderBarType.Bronze) {
				ore1 = 438;
				ore2 = 436;
				barOpcode = 17694734;
			}if (configbs.barType1() == ABankStanderBarType.Molten_Glass) {
				ore1 = 1781;
				ore2 = 1783;
				barOpcode = 17694734;
			} else if (configbs.barType1() == ABankStanderBarType.Iron) {
				ore1 = 440;
				ore2 = 440;
				barOpcode = 17694736;
			} else if (configbs.barType1() == ABankStanderBarType.Steel) {
				ore1 = 453;
				ore2 = 440;
				barOpcode = 17694738;
			} else if (configbs.barType1() == ABankStanderBarType.Gold) {
				ore1 = 444; //
				ore2 = 444; //
				barOpcode = 17694739;
			} else if (configbs.barType1() == ABankStanderBarType.Mithril) {
				ore1 = 447;
				ore2 = 453;
				barOpcode = 17694740;
			} else if (configbs.barType1() == ABankStanderBarType.Adamant) {
				ore1 = 449;
				ore2 = 453;
				barOpcode = 17694741;
			} else if (configbs.barType1() == ABankStanderBarType.Rune) {
				ore1 = 451;
				ore2 = 453;
				barOpcode = 17694742;
			} else {
				ore1 = 0;
				ore2 = 0;
			}
			if (ore1 != 0) {
				if (!utils.isBankOpen() && !utils.inventoryContains(ore1)) {
					openNearestBank();
					timeout = tickDelay();
					state = OPEN_BANK;
					return;
				}

				if (!utils.isBankOpen() && utils.inventoryContains(ore1) && utils.inventoryContains(ore2)) {
					useGameObject(configbs.furnaceId(), 4);
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to smelt?")) {
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, barOpcode));
							firstTime = true;
						}
						else if (client.getWidget(270, 5).getText().equals("How many do you wish to make?")) {
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, barOpcode));
							firstTime = true;
						}
					}
					timeout = tickDelay();
					state = ACTION;
					return;
				}

				if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(ore1) && utils.inventoryContains(ore2)) {
					closeBank();
					deposited = false;
					firstTime = true;
					timeout = tickDelay();
					state = CLOSE_BANK;
					return;
				}
				if (utils.isBankOpen() && !deposited && !utils.inventoryContains(ore1)) {
					utils.depositAll();
					deposited = true;
					firstTime = true;
					timeout = tickDelay();
					state = DEPOSIT_ITEMS;
					return;
				}
				if (utils.isBankOpen() && deposited) {
					if (configbs.barType1() == ABankStanderBarType.Bronze) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 14);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						if (!utils.inventoryContains(ore2)) {
							utils.withdrawItemAmount(ore2, 14);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						return;
					}
					if (configbs.barType1() == ABankStanderBarType.Molten_Glass) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 14);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						if (!utils.inventoryContains(ore2)) {
							utils.withdrawItemAmount(ore2, 14);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						return;
					}
					else if (configbs.barType1() == ABankStanderBarType.Iron) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 28);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						return;
					}
					else if (configbs.barType1() == ABankStanderBarType.Steel) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 9);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						if (!utils.inventoryContains(453)) {
							utils.withdrawAllItem(453);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						return;
					}
					else if (configbs.barType1() == ABankStanderBarType.Gold) {
						utils.withdrawAllItem(444);
					}
					else if (configbs.barType1() == ABankStanderBarType.Mithril) {
						if (!utils.inventoryContains(447)) {
							utils.withdrawItemAmount(447, 5);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
							return;
						}
						if (!utils.inventoryContains(453)) {
							utils.withdrawAllItem(453);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
							return;
						}
					}
					else if (configbs.barType1() == ABankStanderBarType.Adamant) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 4);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						if (!utils.inventoryContains(453)) {
							utils.withdrawAllItem(453);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						return;
					} else if (configbs.barType1() == ABankStanderBarType.Rune) {
						if (!utils.inventoryContains(ore1)) {
							utils.withdrawItemAmount(ore1, 3);
							firstTime = true;
							timeout = tickDelay();
							state = WITHDRAW_ITEMS;
						}
						utils.withdrawAllItem(453);
						return;
					}
				}
				state = NOT_SURE;
			}

		}
		if (configbs.Type() == ABankStanderType.SAWMILL_PLANKS) {
			if (!utils.isBankOpen() && !utils.inventoryContains(configbs.planklogId1())) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}

			if (!utils.isBankOpen()) {
				if (firstTime) {
					NPC targetNPC = utils.findNearestNpc(3101);
					clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), 11, 0, 0));
					firstTime = false;
					state = ACTION_FIRST;
					return;
				} else {
					if (client.getWidget(270, 15) != null) {
						if (configbs.planklogId1() == 1511) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 17694734));
							timeout = tickDelay();
							state = ACTION;
							return;
						}
						else if (configbs.planklogId1() == 1521) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 17694735));
							timeout = tickDelay();
							state = ACTION;
							return;
						}
						else if (configbs.planklogId1() == 6333) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 17694736));
							timeout = tickDelay();
							state = ACTION;
							return;
						}else if (configbs.planklogId1() == 6332) {
							clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 17694737));
							timeout = tickDelay();
							state = ACTION;
							return;
						}

					}
				}
			}

			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(configbs.planklogId1())) {

				utils.walk(new WorldPoint(1616, 3505, 0));
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(configbs.planklogId1())) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(995)) {
				utils.withdrawAllItem(995);
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_TOOL;
				return;
			}

			if (utils.isBankOpen() && !utils.inventoryContains(configbs.planklogId1())) {
				utils.withdrawAllItem(configbs.planklogId1());
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}

	if (configbs.Type() == ABankStanderType.STRING_BOWS) {
		if (!utils.isBankOpen() && !utils.inventoryContains(configbs.logId1())) {
			openNearestBank();
			timeout = tickDelay();
			state = OPEN_BANK;
			return;
		}


		if (!utils.isBankOpen()) {
			if (firstTime) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", 1777, 38, utils.getInventoryWidgetItem(1777).getIndex(), 9764864));
				timeout = tickDelay();
				firstTime = false;
				state = ACTION_FIRST;
				return;
			}
			clientThread.invoke(() -> client.invokeMenuAction("", "", configbs.logId1(), 31, utils.getInventoryWidgetItem(configbs.logId1()).getIndex(), 9764864));
			timeout = tickDelay();
			if (client.getWidget(270, 5) != null) {
				if (client.getWidget(270, 5).getText().equals("How many would you like to string?")) {
					firstTime = true;
					clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694734));
				}
			}
			state = ACTION;
			return;
		}

		if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(configbs.logId1()) && utils.inventoryContains(1777)) {
			closeBank();
			firstTime = true;
			timeout = tickDelay();
			state = CLOSE_BANK;
			return;
		}
		if (utils.isBankOpen() && !utils.inventoryEmpty() && !utils.inventoryContains(configbs.logId1()) && !utils.inventoryContains(1777)) {
			utils.depositAll();
			firstTime = true;
			timeout = tickDelay();
			state = DEPOSIT_ITEMS;
			return;
		}
		if (utils.isBankOpen() && !utils.inventoryContains(1777)) {
			utils.withdrawItemAmount(1777, 14);
			timeout = tickDelay();
			state = WITHDRAW_TOOL;
			return;
		}
		if (utils.isBankOpen() && utils.inventoryContains(1777) && !utils.inventoryContains(configbs.logId1())) {
			//utils.withdrawItemAmount(configbs.logId(), 27);
			utils.withdrawAllItem(configbs.logId1());
			firstTime = true;
			timeout = tickDelay();
			state = WITHDRAW_ITEMS;
			return;
		}
		firstTime = true;
		state = NOT_SURE;
	}
////////////KARAMBWANS
		if (configbs.Type() == ABankStanderType.COOK_KARAMBWANS) {
			if (!utils.isBankOpen() && !utils.inventoryContains(3142)) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}

			if (!utils.isBankOpen()) {
				if (firstTime) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",3142,38,utils.getInventoryWidgetItem(3142).getIndex(),WidgetInfo.INVENTORY.getId()));
					firstTime = false;
					state = ACTION_FIRST;
					return;
				}
				else {
					targetObject = utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 25, configbs.rangeIdK());
					clientThread.invoke(() -> client.invokeMenuAction("", "",targetObject.getId(),1,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("What would you like to cook?")) {
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694735));
							firstTime = true;
							return;
						}
					}
				}
				state = ACTION;
				return;
			}

			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(3142)) {
				closeBank();
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(3142)) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(3142)) {
				//utils.withdrawItemAmount(configbs.logId(), 27);
				utils.withdrawAllItem(3142);
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}

		if (configbs.Type() == ABankStanderType.COOKER) {
			if (!utils.isBankOpen() && !utils.inventoryContains(configbs.foodId())) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}

			if (!utils.isBankOpen()) {
				if (firstTime) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.foodId(),38,utils.getInventoryWidgetItem(configbs.foodId()).getIndex(),WidgetInfo.INVENTORY.getId()));
					timeout = 1;
					targetObject = utils.findNearestGameObjectWithin(client.getLocalPlayer().getWorldLocation(), 25, configbs.rangeId());
					clientThread.invoke(() -> client.invokeMenuAction("", "",targetObject.getId(),1,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));
					firstTime = false;
					state = ACTION_FIRST;
					return;
				}
				else {
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("How many would you like to cook?")) {
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694734));
							firstTime = true;
						}
					}
				}
				timeout = tickDelay();
				state = ACTION;
				return;
			}

			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(configbs.foodId())) {
				closeBank();
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(configbs.foodId())) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(configbs.foodId())) {
				utils.withdrawAllItem(configbs.foodId());
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}

		if (configbs.Type() == ABankStanderType.HERB_CLEANER) {
			if (!utils.isBankOpen() && !utils.inventoryContains(configbs.herbId())) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}
			if (!utils.isBankOpen() && utils.inventoryContains(configbs.herbId())) {
				clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.herbId(), 33, utils.getInventoryWidgetItem(configbs.herbId()).getIndex(), WidgetInfo.INVENTORY.getId()));
				firstTime = false;
				state = ACTION;
				timeout = tickDelay();
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(configbs.herbId())) {
				closeBank();
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(configbs.herbId())) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(configbs.herbId())) {
				//utils.withdrawItemAmount(configbs.logId(), 27);
				utils.withdrawAllItem(configbs.herbId());
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}
		if (configbs.Type() == ABankStanderType.Crafter) {
			if (!utils.isBankOpen() && !utils.inventoryContains(configbs.gemId())) {
				openNearestBank();
				timeout = tickDelay();
				state = OPEN_BANK;
				return;
			}

			if (!utils.isBankOpen()) {
				if (firstTime) {
					clientThread.invoke(() -> client.invokeMenuAction("", "",1755, 38, utils.getInventoryWidgetItem(1755).getIndex(), 9764864));
					firstTime = false;
					timeout = tickDelay();
					state = ACTION_FIRST;
					return;
				}
				else {
					clientThread.invoke(() -> client.invokeMenuAction("", "",configbs.gemId(), 31, utils.getInventoryWidgetItem(configbs.gemId()).getIndex(), 9764864));
					if (client.getWidget(270, 5) != null) {
						if (client.getWidget(270, 5).getText().equals("How many gems would you like to cut?")) {
							firstTime = true;
							clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, -1, 17694734));
							timeout = tickDelay();
						}
					}
				}
				timeout = tickDelay();
				state = ACTION;
				return;
			}

			if (utils.isBankOpen() && utils.inventoryFull() && utils.inventoryContains(1755) && utils.inventoryContains(configbs.gemId())) {
				closeBank();
				firstTime = true;
				timeout = tickDelay();
				state = CLOSE_BANK;
				return;
			}
			if (utils.isBankOpen() && utils.inventoryFull() && !utils.inventoryContains(configbs.gemId())) {
				utils.depositAll();
				firstTime = true;
				timeout = tickDelay();
				state = DEPOSIT_ITEMS;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(1755)) {
				utils.withdrawItem(1755);
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_TOOL;
				return;
			}
			if (utils.isBankOpen() && !utils.inventoryContains(configbs.gemId())) {
				utils.withdrawAllItem(configbs.gemId());
				firstTime = true;
				timeout = tickDelay();
				state = WITHDRAW_ITEMS;
				return;
			}
			firstTime = true;
			state = NOT_SURE;
		}
	}

	private void openNearestBank()
	{
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "",bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		} else {
			utils.sendGameMessage("Bank not found");
		}
	}

	private Point getRandomNullPoint()
	{
		if(client.getWidget(161,34)!=null){
			Rectangle nullArea = client.getWidget(161,34).getBounds();
			return new Point ((int)nullArea.getX()+utils.getRandomIntBetweenRange(0,nullArea.width), (int)nullArea.getY()+utils.getRandomIntBetweenRange(0,nullArea.height));
		}

		return new Point(client.getCanvasWidth()-utils.getRandomIntBetweenRange(0,2),client.getCanvasHeight()-utils.getRandomIntBetweenRange(0,2));
	}
	private void useGameObject(int id, int opcode)
	{
		targetObject = utils.findNearestGameObject(id);
		if(targetObject!=null){
			clientThread.invoke(() -> client.invokeMenuAction("", "",targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));
		}
	}
	private void closeBank()
	{
		clientThread.invoke(() -> client.invokeMenuAction("", "",1, 57, 11, 786434));
	}
}