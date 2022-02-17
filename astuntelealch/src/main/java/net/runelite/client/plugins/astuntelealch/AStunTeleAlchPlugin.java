
package net.runelite.client.plugins.astuntelealch;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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
import java.util.HashSet;
import java.util.Set;
//import static net.runelite.client.plugins.afletcher.AFletcherState.*;
import static net.runelite.client.plugins.astuntelealch.AStunTeleAlchState.*;
@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "AStunTeleAlch",
	description = "Anarchise' StunTeleAlch Plugin."
)
@Slf4j
public class AStunTeleAlchPlugin extends Plugin
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
	private AStunTeleAlchConfig configbs;

	@Inject
	private AStunTeleAlchOverlay overlaybs;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	boolean firstTime = true;
	AStunTeleAlchState state;
	boolean startFireMaker;
	boolean deposited = false;
	int timeout = 0;
	boolean walkAction;
 boolean second = false;
 boolean third = false;

	int param1;
	int param2;

	int coordX;
	int coordY;
	GameObject targetObject;
	final Set<GameObject> fireObjects = new HashSet<>();
	final Set<Integer> requiredItems = new HashSet<>();
	boolean[] pathStates;
	@Provides
	AStunTeleAlchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AStunTeleAlchConfig.class);
	}
	void reset() {
		walkAction=false;
		coordX=0;
		coordY=0;
		firstTime=true;
		startFireMaker=false;
		requiredItems.clear();
		requiredItems.add(946);
		pathStates = null;
		chinBreakHandler.unregisterPlugin(this);
		botTimer = null;
		overlayManager.remove(overlaybs);
	}
	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
		reset();
	}

	@Override
	protected void shutDown()
	{
		reset();
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

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("astuntelealch"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startFireMaker)
			{
				//startUp();
				startFireMaker = true;
				targetMenu = null;
				firstTime = true;
				botTimer = Instant.now();
				overlayManager.add(overlaybs);
				chinBreakHandler.startPlugin(this);
			} else {
				startFireMaker = false;
				chinBreakHandler.stopPlugin(this);
			}
		}
	}
	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("astuntelealch"))
		{
			return;
		}
		startFireMaker = false;
	}

	@Subscribe
	private void onGameTick(GameTick gameTick) {
		if (chinBreakHandler.isBreakActive(this)) {
			return;
		}
		if (chinBreakHandler.shouldBreak(this)) {
			chinBreakHandler.startBreak(this);
		}
		int l = 0;
		if (!startFireMaker) {
			return;
		}
		if (!client.isResized()) {
			utils.sendGameMessage("client must be set to resizable");
			startFireMaker = false;
			return;
		}
		player = client.getLocalPlayer();

		if (player == null) {
			state = NULL_PLAYER;
			return;
		}

		int beforeLoc = 0;

		if (player.getAnimation() != -1) {
			state = ANIMATING;
			timeout = tickDelay();
			return;
		}

		if (timeout > 0) {
			utils.handleRun(30, 20);
			timeout--;
			return;
		}

		if (configbs.Type() == AStunTeleAlchType.STUN) {
			param1 = -1;
			if (configbs.stunType() == AStunTeleAlchStunType.CONFUSE) param2 = 14286856;
			if (configbs.stunType() == AStunTeleAlchStunType.WEAKEN) param2 = 14286861;
			if (configbs.stunType() == AStunTeleAlchStunType.CURSE) param2 = 14286865;
			if (configbs.stunType() == AStunTeleAlchStunType.CURSE) param2 = 14286865;
			if (configbs.stunType() == AStunTeleAlchStunType.VULNERABILITY) param2 = 14286904;
			if (configbs.stunType() == AStunTeleAlchStunType.ENFEEBLE) param2 = 14286908;
			if (configbs.stunType() == AStunTeleAlchStunType.STUN) param2 = 14286912;
		}
		if (configbs.Type() == AStunTeleAlchType.TELEPORTER) {
			param1 = -1;
			if (configbs.teleType() == AStunTeleAlchTeleType.VARROCK) param2 = 14286869;
			if (configbs.teleType() == AStunTeleAlchTeleType.LUMBRIDGE) param2 = 14286872;
			if (configbs.teleType() == AStunTeleAlchTeleType.FALADOR) param2 = 14286875;
			if (configbs.teleType() == AStunTeleAlchTeleType.HOUSE) param2 = 14286877;
			if (configbs.teleType() == AStunTeleAlchTeleType.CAMELOT) param2 = 14286880;
			if (configbs.teleType() == AStunTeleAlchTeleType.ARDOUGNE) param2 = 14286886;
			if (configbs.teleType() == AStunTeleAlchTeleType.WATCHTOWER) param2 = 14286891;
			if (configbs.teleType() == AStunTeleAlchTeleType.TROLLHEIM) param2 = 14286898;
			if (configbs.teleType() == AStunTeleAlchTeleType.KOUREND) param2 = 14286906;
		}

		if (configbs.Type() == AStunTeleAlchType.STUN) {
			if (utils.inventoryContains(561) || utils.runePouchContains(561) && utils.inventoryContains(configbs.alchID())) {
				NPC getNPC = utils.findNearestNpc(configbs.stunID());
				if (firstTime) {
					if (getNPC != null) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", 0, 25, param1, param2));
						clientThread.invoke(() -> client.invokeMenuAction("", "", getNPC.getIndex(), MenuAction.SPELL_CAST_ON_NPC.getId(), 0, 0));
						firstTime = false;
						return;
					}
				}
				clientThread.invoke(() -> client.invokeMenuAction("", "", 0, 25, -1, 14286888));
				clientThread.invoke(() -> client.invokeMenuAction("", "", configbs.alchID(), 32, utils.getInventoryWidgetItem(configbs.alchID()).getIndex(), 9764864));
				state = ALCHING;
				timeout = 5;
				firstTime = true;
				return;
			}
			if (configbs.Type() == AStunTeleAlchType.TELEPORTER) {
				if (utils.inventoryContains(561) || utils.runePouchContains(561)) {
					if (firstTime) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", 1, 57, param1, param2));
						firstTime = false;
						state = TELEPORTING;
						return;
					}
					if (!firstTime) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", 0, 25, -1, 14286888));
						clientThread.invoke(() -> client.invokeMenuAction("", "", configbs.alchID(), 32, utils.getInventoryWidgetItem(configbs.alchID()).getIndex(), 9764864));
						firstTime = true;
						state = ALCHING;
						return;
					}
				}
			}

		}
	}
}