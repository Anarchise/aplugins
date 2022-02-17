package net.runelite.client.plugins.apker;

import com.google.inject.Provides;
import io.reactivex.rxjava3.core.Single;
import net.runelite.api.widgets.WidgetItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.apker.ScriptCommand.ScriptCommand;
import net.runelite.client.ui.overlay.OverlayManager;
import com.openosrs.client.util.WeaponMap;
import com.openosrs.client.util.WeaponStyle;
import org.pf4j.Extension;
import java.util.Random;
import java.util.stream.Collectors;

import net.runelite.client.game.ItemManager;import net.runelite.client.plugins.apker.ScriptCommand.ScriptCommandFactory;
import net.runelite.client.plugins.autils.AUtils;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "APKer",
		description = "Anarchise' PKing Tools",
		tags = {"combat", "player", "enemy", "pvp", "overlay"},
		enabledByDefault = false
)
public class PkToolsPlugin extends Plugin
{
	private static final Duration WAIT = Duration.ofSeconds(5);

	public Queue<ScriptCommand> commandList = new ConcurrentLinkedQueue<>();

	@Inject AUtils utils;

	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	private PkToolsConfig configpk;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;


	@Inject
	private PkToolsHotkeyListener pkToolsHotkeyListener;

	@Inject
	private KeyManager keyManager;

	@Getter(AccessLevel.PACKAGE)



	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	public Player lastEnemy;
	private NPCManager npcManager;
	private Instant lastTime;
	private int nextRestoreVal = 0;
	Integer lastMaxHealth;
	Player target;
	public boolean MaultoAgs = false;
	public boolean AgsToMaul = false;
	public boolean SingleMaulAgs = false;
	int timeout = 0;
	private Random r = new Random();
	@Provides
	PkToolsConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(PkToolsConfig.class);
	}

	@Override
	protected void startUp() throws IOException {
		keyManager.registerKeyListener(pkToolsHotkeyListener);

	}

	@Override
	protected void shutDown()
	{
		lastTime = null;
		keyManager.unregisterKeyListener(pkToolsHotkeyListener);
	}

	@Subscribe
	public void onInteractingChanged(final InteractingChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		final Actor opponent = event.getTarget();

		if (opponent == null)
		{
			lastTime = Instant.now();
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		final List<Player> players = client.getPlayers();

		for (final Player player : players)
		{
			if (localPlayer != null && player == localPlayer.getInteracting())
			{
				lastEnemy = player;
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		lastEnemyTimer();
		processCommands();
	}
	boolean First = true;
	@Subscribe
	public void onGameTick(GameTick event) throws IOException {
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (SingleMaulAgs && First){
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item2 : inventory.getWidgetItems()) {
				if (item2.getId() == 24225) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item2.getId(), item2.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item2.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Maul
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					timeout = 4;
					First = false;
				}
			}
		}
		if (SingleMaulAgs && !First && utils.isItemEquipped(Collections.singleton(24225))){
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : inventory.getWidgetItems()) {
				if (item.getId() == 11802) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Ags
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy
					First = true;
					SingleMaulAgs = false;
				}
			}
			SingleMaulAgs = false;
			First = true;
		}


		if (AgsToMaul && First){
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : inventory.getWidgetItems()) {
				if (item.getId() == 11802) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Ags
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy
					First = false;
				}
			}
		}
		if (AgsToMaul && !First && utils.isItemEquipped(Collections.singleton(11802)) && client.getLocalPlayer().getAnimation() == 7045){
			timeout = 4;
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item2 : inventory.getWidgetItems()) {
				if (item2.getId() == 24225) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy Again
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item2.getId(), item2.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item2.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Maul
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy Again
					First = true;
					AgsToMaul = false;
				}
			}
			AgsToMaul = false;
			First = true;
		}


		if (MaultoAgs && !First) {
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item : inventory.getWidgetItems()) {
				if (item.getId() == 11802) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item.getId(), item.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Ags
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy
					First = true;
					MaultoAgs = false;
				}
			}
			MaultoAgs = false;
			First = true;
		}
		if (MaultoAgs && First) {
			Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
			for (WidgetItem item2 : inventory.getWidgetItems()) {
				if (item2.getId() == 24225) {
					clientThread.invoke(() -> client.invokeMenuAction("Wield", "<col=ff9040>" + item2.getId(), item2.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), item2.getIndex(), WidgetInfo.INVENTORY.getId()));
					//Wear Maul
					clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy Again
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					//Spec
					//clientThread.invoke(() -> client.invokeMenuAction("", "", lastEnemy.getPlayerId(), client.getSpellSelected() ? MenuAction.SPELL_CAST_ON_PLAYER.getId() : MenuAction.PLAYER_SECOND_OPTION.getId(), 0, 0));
					//Click Enemy Again
					timeout = 4;
					First = false;
				}
			}
		}
		doAutoSwapPrayers();
		doSwapGear();
	}

	private void processCommands()
	{
		while (commandList.peek() != null)
		{
			commandList.poll().execute(client, configpk, this, configManager);
		}
	}
	int lll = 999990;
	public void lastEnemyTimer()
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer == null)
		{
			return;
		}

		if (lastEnemy == null)
		{
			return;
		}

		if (localPlayer.getInteracting() == null)
		{
			if (Duration.between(lastTime, Instant.now()).compareTo(PkToolsPlugin.WAIT) > 0)
			{
				lastEnemy = null;
			}
		}
	}

	public void activatePrayer(Prayer prayer)
	{
		if (prayer == null)
		{
			return;
		}

		//check if prayer is already active this tick
		if (client.isPrayerActive(prayer))
		{
			return;
		}

		WidgetInfo widgetInfo = prayer.getWidgetInfo();

		if (widgetInfo == null)
		{
			return;
		}

		Widget prayer_widget = client.getWidget(widgetInfo);

		if (prayer_widget == null)
		{
			return;
		}

		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return;
		}

		//entryList.add(new MenuEntry("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId(), false));
		clientThread.invoke(() -> client.invokeMenuAction("Activate", prayer_widget.getName(), 1, MenuAction.CC_OP.getId(), prayer_widget.getItemId(), prayer_widget.getId()));
		//click();
	}
	/*public void doSpecOnHealth()
	{
		if (!configpk.specOnHealth()){
			return;
		}
		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}
			//
			if (lastEnemy instanceof NPC)
			{
				lastMaxHealth = npcManager.getHealth(((NPC) lastEnemy).getId());
			}
			else if (lastEnemy instanceof Player)
			{
				if (lastEnemy.getName() != null && lastEnemy.getHealthScale() > 0) {
					String opponentName = Text.removeTags(lastEnemy.getName());
					final HiscoreResult hiscoreResult = hiscoreManager.lookupAsync(opponentName, getHiscoreEndpoint());
					if (hiscoreResult != null) {
						final int hp = hiscoreResult.getHitpoints().getLevel();
						if (hp > 0) {
							lastMaxHealth = hp;
						}
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/


	public WidgetItem getRestoreItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.PRAYER_POTION.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		item = PrayerRestoreType.SANFEW_SERUM.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		item = PrayerRestoreType.SUPER_RESTORE.getItemFromInventory(client);

		return item;
	}

	public WidgetItem getBrewItem()
	{
		WidgetItem item;
		item = PrayerRestoreType.SARA_BREWS.getItemFromInventory(client);
		if (item != null)
		{
			return item;
		}
		return item;
	}
	private void addCommands(String command)
	{
		for (String c : command.split("\\s*\n\\s*"))
		{
			commandList.add(ScriptCommandFactory.builder(c));
		}
	}
	public int getBoostAmount(WidgetItem restoreItem, int prayerLevel)
	{
		if (PrayerRestoreType.PRAYER_POTION.containsId(restoreItem.getId()))
		{
			return 7 + (int) Math.floor(prayerLevel * .25);
		}
		else if (PrayerRestoreType.SANFEW_SERUM.containsId(restoreItem.getId()))
		{
			return 4 + (int) Math.floor(prayerLevel * (double)(3 / 10));
		}
		else if (PrayerRestoreType.SUPER_RESTORE.containsId(restoreItem.getId()))
		{
			return 8 + (int) Math.floor(prayerLevel * .25);
		}

		return 0;
	}
	public void doSwapGear()
	{
		if (!configpk.autoGearSwap())
		{
			return;
		}

		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			/*if (client.getLocalPlayer().getInteracting() != lastEnemy){
				return;
			}*/

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}
			if (!configpk.swapFromPray()){
				WeaponStyle weaponStyle = WeaponMap.StyleMap.getOrDefault(lastEnemyAppearance.getEquipmentId(KitType.WEAPON), null);
				if (weaponStyle == null)
				{
					return;
				}

				switch (weaponStyle)
				{
					case MELEE:
						WeaponStyle localWeaponStyle = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle == WeaponStyle.MELEE){
							break;
						}
						addCommands("group1");	//group1 = melee
						addCommands("clickenemy");
						break;
					case RANGE:
						WeaponStyle localWeaponStyle2 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle2 == WeaponStyle.MAGIC){
							break;
						}
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case MAGIC:
						WeaponStyle localWeaponStyle3 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle3 == WeaponStyle.RANGE){
							break;
						}
						addCommands("group2");	//group2 = ranged
						addCommands("clickenemy");
						break;
					default:
						break;
				}
			}

			if (configpk.swapFromPray()) {
				HeadIcon currentIcon = lastEnemy.getOverheadIcon();
				if (currentIcon == null) {
					return;
				}
				switch (currentIcon) {
					case MELEE:
						WeaponStyle localWeaponStyle2 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle2 == WeaponStyle.MAGIC){
							break;
						}
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case RANGED:
						WeaponStyle localWeaponStyle1 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle1 == WeaponStyle.MAGIC){
							break;
						}
						/*addCommands("group1");	//group1 = melee
						addCommands("clickenemy");*/
						addCommands("group3");	//group3 = magic
						addCommands("freeze");
						addCommands("clickenemy");
						break;
					case MAGIC:
						WeaponStyle localWeaponStyle3 = WeaponMap.StyleMap.getOrDefault(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), null);
						if (localWeaponStyle3 == WeaponStyle.RANGE){
							break;
						}
						addCommands("group2");	//group2 = ranged
						addCommands("clickenemy");
						break;
					default:
						break;
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void doAutoSwapPrayers()
	{
		if (!configpk.autoPrayerSwitcher())
		{
			return;
		}

		if (!configpk.autoPrayerSwitcher())
		{
			return;
		}

		try
		{
			if (lastEnemy == null)
			{
				return;
			}

			PlayerComposition lastEnemyAppearance = lastEnemy.getPlayerComposition();

			if (lastEnemyAppearance == null)
			{
				return;
			}

			WeaponStyle weaponStyle = WeaponMap.StyleMap.getOrDefault(lastEnemyAppearance.getEquipmentId(KitType.WEAPON), null);

			if (weaponStyle == null)
			{
				return;
			}

			switch (weaponStyle)
			{
				case MELEE:
					if (configpk.enableMeleePrayer()) {
						activatePrayer(Prayer.PROTECT_FROM_MELEE);
					}
					break;
				case RANGE:
					if (configpk.enableRangedPrayer()) {
					activatePrayer(Prayer.PROTECT_FROM_MISSILES);
					}
					break;
				case MAGIC:
					if (configpk.enableMagicPrayer()) {
						activatePrayer(Prayer.PROTECT_FROM_MAGIC);
					}
					break;
				default:
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("apker"))
		{
			return;
		}
	}
}
