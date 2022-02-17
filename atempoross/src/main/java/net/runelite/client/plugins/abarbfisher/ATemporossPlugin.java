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
package net.runelite.client.plugins.abarbfisher;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.time.Instant;
import java.util.*;
import javax.inject.Inject;

import com.openosrs.http.api.discord.DiscordClient;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.discord.events.DiscordReady;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.plugins.discord.DiscordConfig;
import net.runelite.client.plugins.discord.DiscordPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.Extension;
import net.runelite.client.discord.*;
import static net.runelite.client.plugins.abarbfisher.ATemporossState.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "ATempoross",
	enabledByDefault = false,
	description = "Does Tempoross for you.",
	tags = {"anarchise"}
)
@Slf4j
public class ATemporossPlugin extends Plugin
{

	@Inject
	public ReflectBreakHandler chinBreakHandler;

	@Inject
	private Client client;

	@Inject
	private ATemporossConfiguration configts;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	PluginManager pluginManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	ClientThread clientThread;

	@Inject
	private ATemporossOverlay overlayts;

	NPC targetSpot;
	ATemporossState state;
	//GameObject targetObject;
	//NPC targetNPC;
	MenuEntry targetMenu;
	boolean checked = false;
	Instant botTimer;
	Player player;
	ATemporossState previousState;
	int amount = 7;
	Rectangle altRect = new Rectangle(-100,-100, 10, 10);
	boolean waveIsIncoming = false;
	boolean firstInventory = true;
	GameObject Hammers;
	GameObject Ropes;
	GameObject Harpoon;
	GameObject Buckets;
	GameObject Hose;
	GameObject Box1;
	WorldPoint HammerLoc;
	NPC Box;
	boolean GotLocations = false;
	boolean hasTethered = false;
	boolean startedFishing = false;
	boolean roundStarted = false;
	boolean StartedDepositing = false;
	boolean StartedCooking = false;
	boolean StormBlowing = false;
	WorldPoint StartLocation;
	private static final String WAVE_END_SAFE = "as the wave washes over you";
	private static final String WAVE_END_DANGEROUS = "the wave slams into you";
	private static final String GAME_END = "retreats to the depths";
	private static final String GAME_BEGIN = "weigh anchor and sail you out";
	private final Set<Integer> HARPOONS = Set.of(ItemID.HARPOON, ItemID.INFERNAL_HARPOON, ItemID.BARBTAIL_HARPOON, ItemID.INFERNAL_HARPOON_OR, ItemID.CORRUPTED_HARPOON, ItemID.CRYSTAL_HARPOON, ItemID.DRAGON_HARPOON, ItemID.TRAILBLAZER_HARPOON);
	int timeout = 0;
	long sleepLength;
	boolean startBarbarianFisher;
	boolean firstTimeUsingChisel;
	private final Set<Integer> rawFishIds = new HashSet<>();
	//private final Set<Integer> cookedFishIds = new HashSet<>();
	private final Set<Integer> requiredIds = new HashSet<>();
	private static final String WAVE_INCOMING_MESSAGE = "a colossal wave closes in...";
	@Provides
	ATemporossConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ATemporossConfiguration.class);
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
		overlayManager.remove(overlayts);
		state = null;
		timeout = 0;
		botTimer = null;
		firstInventory = true;
		startBarbarianFisher = false;
		requiredIds.clear();
		rawFishIds.clear();
	}
	int lll = 99999990;
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("atempoross"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBarbarianFisher)
			{
				startBarbarianFisher = true;
				firstInventory = true;
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				overlayManager.add(overlayts);
				chinBreakHandler.startPlugin(this);
			}
			else
			{
				resetVals();
				chinBreakHandler.stopPlugin(this);
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ATempoross"))
		{
			return;
		}
		startBarbarianFisher = false;
	}



	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(configts.sleepWeightedDistribution(), configts.sleepMin(), configts.sleepMax(), configts.sleepDeviation(), configts.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(false, 2, 3, 1, 2);
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
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
		GameObject targetObject = utils.findNearestGameObject(id);
		if(targetObject!=null){
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY()));

			//targetMenu = new MenuEntry("","",targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY(),false);
			//utils.setMenuEntry(targetMenu);
			//utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
		}
	}
	private void enterWaitingLobby(){
		useGameObject(41305, 3);
	}

	private void interactNPC(int objectIds) {
		NPC targetNPC = utils.findNearestNpc(objectIds);
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));

			//targetMenu = new MenuEntry("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
			//utils.setMenuEntry(targetMenu);
			//utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
			//utils.doActionMsTime(targetMenu, targetNPC.getConvexHull().getBounds(), sleepDelay());
		} else {
			log.info("NPC is null");
		}
	}
	private void interactNPCDirect(NPC targetNPC) {
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));

			//targetMenu = new MenuEntry("", "", targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
			//utils.setMenuEntry(targetMenu);
			//utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
			//utils.doActionMsTime(targetMenu, targetNPC.getConvexHull().getBounds(), sleepDelay());
		} else {
			log.info("NPC is null");
		}
	}
	private void interactNPCOption2(int objectIds) {
		NPC targetNPC = utils.findNearestNpc(objectIds);
		if (targetNPC != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetNPC.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0));

			//targetMenu = new MenuEntry("", "", targetNPC.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
			//utils.setMenuEntry(targetMenu);
			//utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
			//utils.doActionMsTime(targetMenu, targetNPC.getConvexHull().getBounds(), sleepDelay());
		} else {
			log.info("NPC is null");
		}
	}
	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chinBreakHandler.isBreakActive(this)){
			return;
		}
		/*if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}*/

		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains(WAVE_INCOMING_MESSAGE))
		{
			waveIsIncoming = true;
		}
		else if (message.contains(WAVE_END_SAFE) || message.contains(WAVE_END_DANGEROUS))
		{
			waveIsIncoming = false;
			hasTethered = false;
		}
		else if (message.contains(GAME_END)){
			roundStarted = false;
			interactNPCOption2(utils.findNearestNpc(10593, 10595, 10597, 10596).getId());
		}
	}

	public ATemporossState getState()
	{
		if (!checked) {
			//log.info("{}", utils.getPath());
			checked = true;
		}
		if (chinBreakHandler.isBreakActive(this)){
			return HANDLE_BREAK;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null) {
			if (timeout > 0) {
				return TIMEOUT;
			}

			if (utils.iterating) {
				previousState = ITERATING;
				return ITERATING;
			}

			NPC nearestStorm = utils.findNearestNpcWithin(player.getWorldLocation(), 3, Collections.singleton(10580));

			if (StormBlowing) {
				return RUN_FROM_FIRE;
			}

			if (!configts.useBuckets() && utils.findNearestObject(NullObjectID.NULL_41006) != null && player.getWorldArea().intersectsWith(utils.findNearestObject(NullObjectID.NULL_41006).getWorldLocation().toWorldArea())) {
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}

			if (!configts.useBuckets() && utils.findNearestNpc(8643) != null && player.getWorldArea().intersectsWith(utils.findNearestNpc(8643).getWorldArea())) {
				//NullObjectID.NULL_41006
				//utils.findNearestNpc(8643)
				previousState = RUN_FROM_FIRE;
				return RUN_FROM_FIRE;
			}

			//if (utils.isMoving(beforeLoc)) {
			//timeout = 2 + tickDelay();
			//	return MOVING;
			//}
			if (player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3136, 2818, 0), new WorldPoint(3168, 2853, 0)))) {
				previousState = ENTER_LOBBY;
				return ENTER_LOBBY;
			}
			if (waveIsIncoming && !hasTethered){
				previousState = TETHER;
				return TETHER;
			}
			if (client.getWidget(437, 45) == null) {
				previousState = TIMEOUT;
				return TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 1%")) {
				previousState = TIMEOUT;
				return TIMEOUT;
			}
			if (client.getWidget(437, 45).getText().equals("Essence: 0%")) {
				previousState = TIMEOUT;
				return TIMEOUT;
			}
			if (client.getWidget(437, 35) != null)  {
				roundStarted = true;
			}
			else if (client.getWidget(437, 35) == null)  {
				roundStarted = false;
				GotLocations = false;
			}
			/*if (roundStarted && client.getWidget(437, 35) == null)  {

			}*/
			if (client.getWidget(437, 35) != null && !waveIsIncoming) {
				if (!GotLocations)
				{
					StartLocation = client.getLocalPlayer().getWorldLocation();
					Hammers = utils.findNearestGameObject(40964);//useGameObject(40964, 3);
					Harpoon = utils.findNearestGameObject(40967);//useGameObject(40967, 3);
					Ropes = utils.findNearestGameObject(40965);
					HammerLoc = Hammers.getWorldLocation();

					Buckets = utils.findNearestGameObject(40966);

					Box = utils.findNearestNpc(10576, 10578, 10579, 10577);
					Box1 = utils.findNearestGameObject(40970, 40979, 40976, 40969);
					GotLocations = true;
				}

				if (configts.catch7cook7()) {
					amount = 7;
				}
				else if (!configts.catch7cook7()){
					amount = 16;
				}



				if (utils.findNearestNpc(10571) != null && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}

				if (client.getWidget(437, 35).getText().equals("Energy: 1%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}
				else if (client.getWidget(437, 35).getText().equals("Energy: 0%")) {
					StartedCooking = false;
					StartedDepositing = false;
					return SPIRIT_POOL;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 8%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 7%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 6%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 5%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 4%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 3%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 2%")) {
					amount = 7;
				}
				if (client.getWidget(437, 35).getText().equals("Energy: 3%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 89%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 88%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 87%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 86%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 85%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 84%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 83%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 82%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 81%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 80%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 99%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 98%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 97%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 96%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 95%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 94%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 93%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 92%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 91%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 90%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 79%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 78%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 77%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 76%")) {
					amount = 7;
				}
				if (client.getWidget(437, 55).getText().equals("Storm Intensity: 75%")) {
					amount = 7;
				}




			/*if (client.getLocalPlayer().getAnimation() != -1) {
				return ANIMATING;
			}*/
				/*if (configts.OnlyDepositCooked() && utils.inventoryContains(25565)){
					return DEPOSIT_FISH;
				}*/
				/*f (!configts.OnlyDepositCooked() && previousState == DEPOSIT_FISH && utils.inventoryContains(25564)){
					return DEPOSIT_FISH;
				}*/


				/*if (!configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false)){
					previousState = COOKING;
					return COOKING;
				}
				if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25565, amount, false, false)){
					previousState = COOKING;
					return COOKING;
				}*/
				if (!StartedDepositing) {
					if (configts.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25565) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (!configts.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25564)&& client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (!configts.OnlyDepositCooked() && configts.catch7cook7() && utils.inventoryItemContainsAmount(25564, 7, false, false)&& client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (configts.OnlyDepositCooked() && configts.catch7cook7() && utils.inventoryItemContainsAmount(25565, 7, false, false)&& client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;    // ????
					}
					if (!configts.OnlyDepositCooked() && !configts.catch7cook7() && utils.inventoryItemContainsAmount(25564, 16, false, false)&& client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;
					}
					if (configts.OnlyDepositCooked() && !configts.catch7cook7() && utils.inventoryItemContainsAmount(25565, 16, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
						StartedDepositing = true;
						previousState = DEPOSIT_FISH;
						return DEPOSIT_FISH;    // ????
					}
				}

				if (StartedDepositing && utils.findNearestNpc(10571) == null){
					if (utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE){
						return DEPOSIT_FISH;
					}
					if (utils.inventoryContains(25565) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE){
						return DEPOSIT_FISH;
					}
					if (!utils.inventoryContains(25564) && !utils.inventoryContains(25565)){
						StartedDepositing = false;
					}
				}
				if (configts.useBuckets() && utils.inventoryContains(1929) && utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643)) != null)  {
					previousState = DOUSE_FIRE;
					return DOUSE_FIRE;
				}
				if (configts.useBuckets() && !utils.inventoryContains(1925) && !utils.inventoryContains(1929) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
					previousState = GET_BUCKETS;
					return GET_BUCKETS;
				}
				if (!utils.inventoryContains(2347) && client.getLocalPlayer().getAnimation() != 896){
					previousState = GET_HAMMER;
					return GET_HAMMER;
				}
				if (utils.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011) != null ) {
					previousState = REPAIR_MAST;
					return REPAIR_MAST;
				}
				if (!utils.inventoryContains(954) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = GET_ROPES;
					return GET_ROPES;
				}
				if (!utils.inventoryContains(311) && !utils.isItemEquipped(HARPOONS) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = GET_HARPOON;
					return GET_HARPOON;
				}
				if (configts.useBuckets() && utils.inventoryContains(1925) && !utils.inventoryContains(1929) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
					previousState = FILL_BUCKETS;
					return FILL_BUCKETS;
				}


				if (!StartedCooking) {
					if (configts.OnlyDepositCooked() && utils.inventoryFull() && utils.inventoryContains(25564)&& client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}

					if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
					if (configts.OnlyDepositCooked() && utils.inventoryItemContainsAmount(25564, amount, false, false) && client.getLocalPlayer().getAnimation() == AnimationID.IDLE && client.getLocalPlayer().getAnimation() != 896) {
						StartedCooking = true;
						previousState = COOKING;
						return COOKING;
					}
				}
				if (StartedCooking){
					if (utils.inventoryContains(25564) && client.getLocalPlayer().getAnimation() != 896){
						return COOKING;
					}
					if (!utils.inventoryContains(25564)){
						StartedCooking = false;
					}
				}

				if (configts.catch7cook7() && !utils.inventoryItemContainsAmount(25564, 7, false, false)) {
					return FISHING;
				}
				if (!configts.catch7cook7() && !utils.inventoryItemContainsAmount(25564, 16, false, false) ) {
					return FISHING;
				}

				return TIMEOUT;
			}
			//else return LEAVE_LOBBY;
		}
		return NULL_STATE;
	}
	int iii = 0;
	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (chinBreakHandler.shouldBreak(this))
		{
			chinBreakHandler.startBreak(this);
		}
		if (!chinBreakHandler.isBreakActive(this)) {
			if (!startBarbarianFisher) {
				return;
			}
			player = client.getLocalPlayer();
			if (client != null && player != null) {
				if (!client.isResized()) {
					utils.sendGameMessage("client must be set to resizable");
					startBarbarianFisher = false;
					return;
				}
				state = getState();
				switch (state) {
					case TIMEOUT:
						utils.handleRun(30, 20);
						timeout--;
						break;
				/*case ANIMATING:
				case MOVING:
					utils.handleRun(30, 20);
					timeout = tickDelay();
					break;*/
					case ITERATING:
						timeout = tickDelay();
						break;
					case LEAVE_LOBBY:
						interactNPCOption2(10595);
						break;
					case FILL_BUCKETS:

							GameObject WaterPipe = utils.findNearestGameObject(41004);
							if (WaterPipe != null) {
								utils.useGameObjectDirect(WaterPipe, sleepDelay(), 3);
							}

						break;
					case GET_BUCKETS:
						//utils.walk(Buckets.getWorldLocation());
					//	timeout = 4;
					//	if (!utils.isMoving()) {
							GameObject Empty2 = utils.findNearestGameObject(40966);    //40970, 40979, 40976, 40969
							if (Empty2 != null) {
								utils.useGameObjectDirect(Empty2, sleepDelay(), 4);
								timeout = tickDelay();
							}
							break;
					//	}
					//	break;
					case GET_ROPES:
						//utils.walk(Buckets.getWorldLocation());
						//timeout = 4;
					//	if (!utils.isMoving()) {
							GameObject Empty = utils.findNearestGameObject(40965);    //40970, 40979, 40976, 40969
							if (Empty != null) {
								utils.useGameObjectDirect(Empty, sleepDelay(), 3);
								//timeout = tickDelay();
							}
							break;
						//}
						//break;
					case REPAIR_MAST:
						GameObject Objecter = utils.findNearestGameObjectWithin(player.getWorldLocation(), 8, 40997, 40996, 41010, 41011);
						if (Objecter != null) {
							useGameObject(Objecter.getId(), 3);
						}
						timeout = tickDelay();
						break;
					case GET_HAMMER:
						//utils.walk(Buckets.getWorldLocation());
						//timeout = 6;
						//if (!utils.isMoving()) {
							GameObject Empty3 = utils.findNearestGameObject(40964);    //40970, 40979, 40976, 40969
							if (Empty3 != null) {
								utils.useGameObjectDirect(Empty3, sleepDelay(), 3);
								//timeout = tickDelay();
							}
						break;
						//}
						//break;
					case GET_HARPOON:
						//utils.walk(Buckets.getWorldLocation());
						//timeout = 6;
						//if (!utils.isMoving()) {
							GameObject Emptyy = utils.findNearestGameObject(40967);    //40970, 40979, 40976, 40969
							if (Emptyy != null) {
								utils.useGameObjectDirect(Emptyy, sleepDelay(), 3);
								//timeout = tickDelay();
							}
							break;
						//}
						//break;
					case TETHER:
						if (waveIsIncoming && !hasTethered) {
							GameObject Object = utils.findNearestGameObject(41354, 41355, 41353, 41352);
							if (Object != null) {
								useGameObject(Object.getId(), 3);
								hasTethered = true;
								break;
							}
						}
						timeout = tickDelay();
						break;
					case DEPOSIT_FISH:
						//utils.walk(Buckets.getWorldLocation());
						//timeout = 4;
						//if (!utils.isMoving()) {
							NPC Empty1 = utils.findNearestNpc(10576, 10578, 10579, 10577);    //40970, 40979, 40976, 40969
							if (Empty1 != null) {
								interactNPCDirect(Empty1);
								//timeout = tickDelay();
							}
							else{
								utils.walk(Buckets.getWorldLocation());
							}
							break;
					//	}
					//	break;
					case FIND_NPC:
						interactNPC(10565);
						timeout = tickDelay();
						break;
					case COOKING:
						utils.handleRun(30, 20);
						if (client.getLocalPlayer().getAnimation() != 896){// || client.getLocalPlayer().getAnimation() == AnimationID.FISHING_HARPOON || client.getLocalPlayer().getAnimation() == AnimationID.FISHING_BARBTAIL_HARPOON || client.getLocalPlayer().getAnimation() == AnimationID.FISHING_DRAGON_HARPOON || client.getLocalPlayer().getAnimation() == AnimationID.FISHING_CRYSTAL_HARPOON) {
							useGameObject(41236, 3);    //cook on shrine
							timeout = 4;
						}
						break;
					case DOUSE_FIRE:	//8878
						utils.handleRun(30, 20);
						if (configts.useBuckets()) {
							NPC nearestFire = utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643));
							if (nearestFire != null) {
								clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));

								//targetMenu = new MenuEntry("", "", nearestFire.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
								//utils.setMenuEntry(targetMenu);
								//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								timeout = tickDelay();
							}
						}
						break;
					case RUN_FROM_FIRE:
						utils.handleRun(30, 20);
						//if (!configts.useBuckets()) {
							//utils.walk(player.getWorldLocation(), 4, sleepDelay());
							//utils.sendGameMessage("On fire! Moving...");
							//timeout = tickDelay();
							//NPC nearestFire = utils.findNearestNpcWithin(player.getWorldLocation(), 3, Collections.singleton(8643));
							//if (nearestFire != null && nearestFire.getAnimation() == 8878) {
								utils.walk(new WorldPoint(player.getWorldLocation().getX() - 5, player.getWorldLocation().getY(), player.getWorldLocation().getPlane()));
								StormBlowing = false;
								//}
							timeout = tickDelay();
					//	}
						break;
					case SPIRIT_POOL:
						NPC spiritPool2 = utils.findNearestNpc(10571);
						if (spiritPool2 != null) {
							//utils.sendGameMessage("Spirit pool found");
						/*if (client.getLocalPlayer().getAnimation() == AnimationID.IDLE) {
							interactNPC(10571);
						}*/
							if (client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON || client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON || client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON || client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON) {
								interactNPC(10571);
							}
							break;
						} else {
							utils.walk(utils.findNearestObject(41004).getWorldLocation());
						}
						timeout = tickDelay();
						break;
					case FISHING:
						utils.handleRun(30, 20);
						if (client.getLocalPlayer().getAnimation() != 896 && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BARBTAIL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_DRAGON_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_CRYSTAL_HARPOON && client.getLocalPlayer().getAnimation() != AnimationID.FISHING_BAREHAND) {
					/*Set<Integer> SPOTS = Set.of(10572, 10569, 10565, 10568);
					NPC targetNPC = utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 100, SPOTS);

					if (targetNPC != null) { 	//TODO not working :(
						targetMenu = new MenuEntry("", "",
								targetNPC.getIndex(), 9, 0, 0, false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
					}*/
							//if (!startedFishing) {


							NPC nearestFire1 = utils.findNearestNpcWithin(player.getWorldLocation(), 6, Collections.singleton(8643));
							NPC spiritPool = utils.findNearestNpc(10571); // 10571
							//NPC spiritPool2 = utils.findNearestNpc(10571);

							NPC targetSpot = utils.findNearestNpc(10569);
							NPC targetSpot2 = utils.findNearestNpc(10568);

							if (nearestFire1 != null) {
								if (configts.useBuckets()) {
									clientThread.invoke(() -> client.invokeMenuAction("", "", nearestFire1.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));

									//targetMenu = new MenuEntry("", "", nearestFire1.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
									//utils.setMenuEntry(targetMenu);
									//utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								} else {
									if (player.getWorldArea().intersectsWith(nearestFire1.getWorldArea())) {
										utils.walk(player.getWorldLocation());
										timeout = tickDelay();
										break;
									}
								}
							}


							if (spiritPool != null) {
								//utils.sendGameMessage("Spirit pool found");
								interactNPC(10571);
								//timeout = tickDelay();
								startedFishing = true;
								timeout = tickDelay();
								break;
							}
						/*if (spiritPool2 != null) {
							utils.sendGameMessage("Spirit pool found");
							interactNPC(10571);
							//timeout = tickDelay();
							startedFishing = true;
							break;
						}*/
							else if (targetSpot != null) {
								//utils.sendGameMessage("Double fish spot found");
								interactNPC(10569); // double fish spot
								//timeout = tickDelay();
								startedFishing = true;
								timeout = tickDelay();
								break;
							} else if (targetSpot2 != null) {
								//utils.sendGameMessage("Double fish spot found");
								interactNPC(10568); // double fish spot
								//timeout = tickDelay();
								startedFishing = true;
								timeout = tickDelay();
								break;
							} else if (utils.findNearestNpc(10565) != null) {
								//utils.sendGameMessage("Fishing spot found");
								interactNPC(10565); //regular spot
								//timeout = tickDelay();
								startedFishing = true;
								timeout = tickDelay();
								break;
							} else if (utils.findNearestNpc(10568) != null) {
								//utils.sendGameMessage("Fishing spot found");
								interactNPC(10568); //regular spot
								//timeout = tickDelay();
								startedFishing = true;
								timeout = tickDelay();
								break;
							} else  if (utils.findNearestNpc(10568) == null && utils.findNearestNpc(10565) == null && targetSpot == null && targetSpot2 == null){
								//utils.sendGameMessage("NPC not found, moving to nearest shrine");
								if (!utils.isMoving()) {
									useGameObject(41236, 3);    //cook on shrine
								}
								//GameObject object = utils.findNearestGameObject(41354, 41355);//41236
								//utils.walk(object.getWorldLocation(), 1, tickDelay());
								//timeout = tickDelay();
								startedFishing = false;
								timeout = tickDelay();
								break;
							}
						}
						break;
					case ENTER_LOBBY:
						enterWaitingLobby();
						timeout = tickDelay();
						break;
					case MISSING_ITEMS:
						startBarbarianFisher = false;
						utils.sendGameMessage("Missing required items IDs: " + String.valueOf(requiredIds) + " from inventory. Stopping.");
						resetVals();
						break;
				}
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
			final Actor actor = event.getActor();
			NPC npc = (NPC) actor;
			if (npc.getId() == 10580) {
				switch (npc.getAnimation())
				{
					case 8877:
					StormBlowing = true;
					break;
				}
			}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && startBarbarianFisher)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}

	private ATemporossState getBarbarianFisherState()
	{
		if(utils.inventoryFull() && utils.inventoryContains(rawFishIds)){
			return DROPPING_ITEMS;
		}
		return TIMEOUT;
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event){
		log.debug(event.toString());
	}
}
