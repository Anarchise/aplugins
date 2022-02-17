package net.runelite.client.plugins.avorkath;

import com.google.common.base.Preconditions;
import com.google.inject.Provides;
import com.openosrs.client.game.Sound;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.plugins.autils.Spells;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.Extension;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "AVorkath",
	description = "Anarchise' Auto Vorkath.",
	tags = {"vorkath","anarchise","aplugins"},
	enabledByDefault = false
)
public class AVorkathPlugin extends Plugin
{
	@Inject
	private Client client;
	@Provides
	AVorkathConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AVorkathConfig.class);
	}
	@Inject
	private AVorkathConfig configvk;
	@Inject
	private ClientThread clientThread;
	@Inject
	private AUtils utils;
	@Inject
	private KeyManager keyManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject private AVorkathOverlay overlayvk;
	private Rectangle bounds;
	private int timeout;
	private NPC vorkath;
	private List<WorldPoint> acidSpots = new ArrayList<>();
	boolean FirstWalk = true;
	private List<WorldPoint> acidFreePath = new ArrayList<>();
	private int lastAcidSpotsSize = 0;
	private final Set<Integer> DIAMOND_SET = Set.of(ItemID.DIAMOND_DRAGON_BOLTS_E, ItemID.DIAMOND_BOLTS_E);
	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));
	WorldArea RELEKKA_POH = new WorldArea(new WorldPoint(2664, 3625, 0), new WorldPoint(2678, 3638, 0));
	WorldArea RELEKKA_TOWN= new WorldArea(new WorldPoint(2635, 3668, 0), new WorldPoint(2652, 3684, 0));
	WorldArea VORKATH = new WorldArea(new WorldPoint(2262, 4032, 0), new WorldPoint(2286, 4053, 0));
	WorldArea VORKATH2 = new WorldArea(new WorldPoint(2259, 4053, 0), new WorldPoint(2290, 4083, 0));
	private final Set<Integer> RUBY_SET = Set.of(ItemID.RUBY_DRAGON_BOLTS_E, ItemID.RUBY_BOLTS_E);
	AVorkathState state;
	LocalPoint beforeLoc;
	Player player;
	MenuEntry targetMenu;
	LocalPoint dodgeRight;
	LocalPoint dodgeLeft;
	Instant botTimer;
	private boolean inFight;
	public AVorkathPlugin(){
		inFight = false;
	}
	List<String> lootableItems = new ArrayList<>();
	List<TileItem> loot = new ArrayList<>();
	boolean startTeaks = false;
	boolean killedvorkath = false;
	boolean noBomb = true;
	boolean noBomb2 = true;
	private int acidFreePathLength = 3;
	boolean banked = false;
	String[] values;

	private void reset() {
		loot.clear();
		lootableItems.clear();
		values = configvk.lootNames().toLowerCase().split("\\s*,\\s*");
		if (!configvk.lootNames().isBlank()) {
			lootableItems.addAll(Arrays.asList(values));
		}
		overlayManager.remove(overlayvk);
		startTeaks = false;
		state = null;
		killedvorkath = false;
		timeout = 0;
		inFight = false;
		noBomb = true;
		noBomb2 = true;
		dodgeRight = null;
		dodgeLeft = null;
		banked = false;
		botTimer = null;
	}
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("avork")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				//state = null;
				//player = null;
				overlayManager.add(overlayvk);
				loot.clear();
				lootableItems.clear();
				values = configvk.lootNames().toLowerCase().split("\\s*,\\s*");
				if (!configvk.lootNames().isBlank()) {
					lootableItems.addAll(Arrays.asList(values));
				}
				noBomb = true;
				noBomb2 = true;
				banked = false;
				botTimer = Instant.now();
			} else {
				reset();
			}
		}
	}
	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(configvk.sleepWeightedDistribution(), configvk.sleepMin(), configvk.sleepMax(), configvk.sleepDeviation(), configvk.sleepTarget());
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(configvk.tickDelayWeightedDistribution(), configvk.tickDelayMin(), configvk.tickDelayMax(), configvk.tickDelayDeviation(), configvk.tickDelayTarget());
		return tickLength;
	}
	@Override
	protected void startUp() throws Exception
	{
		reset();
	}

	@Inject ConfigManager configManager;


	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}

	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
		}
	}
	private void lootItem(List<TileItem> itemList) {
		TileItem lootItem = getNearestTileItem(itemList);
		if (lootItem != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", lootItem.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION.getId(), lootItem.getTile().getSceneLocation().getX(), lootItem.getTile().getSceneLocation().getY()));
		}
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
	int[] ItemIDs;
	public WidgetItem GetFoodItem() {
		WidgetItem item;

		item = utils.getInventoryWidgetItem(configvk.foodID());

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetRangedItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.RANGED.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetCombatItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.COMBAT.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntifireItem()
	{
		WidgetItem item;

		item = PrayerRestoreType.ANTIFIRE.getItemFromInventory(client);

		if (item != null)
		{
			return item;
		}

		return item;
	}
	public WidgetItem GetAntiVenomItem() {

		WidgetItem item;

		item = PrayerRestoreType.ANTIVENOM.getItemFromInventory(client);

		if (item != null) {
			return item;
		}

		return item;
	}
	public AVorkathState getState()
	{
		if (timeout > 0)
		{
			return AVorkathState.TIMEOUT;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		else {
			return getStates();
		}
	}
	private boolean isInVorkath()
	{
		return ArrayUtils.contains(client.getMapRegions(), 9023);
	}

	private AVorkathState getStates(){
		if (!noBomb2 && !isInVorkath()){
			noBomb2 = true;
		}
		if (!noBomb && !isInVorkath()){
			noBomb = true;
		}
		if (utils.findNearestNpc(8059) != null && isInVorkath()){
			acidFreePath.clear();
			acidSpots.clear();
			noBomb = true;
			noBomb2 = true;
		}
		if (banked && client.getLocalPlayer().getWorldArea().intersectsWith(EDGEVILLE_BANK)  && !utils.isBankOpen()){
			banked = false;
		}
		if (isInPOH(client) && utils.inventoryContains(RUBY_SET) && !utils.isItemEquipped(RUBY_SET)&& configvk.useRanged() && !configvk.useBlowpipe())
		{
			return AVorkathState.EQUIP_RUBIES;
		}
		if (!isInVorkath() && utils.inventoryContains(RUBY_SET) && !utils.isItemEquipped(RUBY_SET) && configvk.useRanged() && !configvk.useBlowpipe())
		{
			return AVorkathState.EQUIP_RUBIES;
		}
		if (isInVorkath() && calculateHealth(vorkath, 750) > 265 && calculateHealth(vorkath, 750) <= 750 && utils.inventoryContains(RUBY_SET) && !utils.isItemEquipped(RUBY_SET) && acidSpots.isEmpty()&& configvk.useRanged() && !configvk.useBlowpipe())
		{
			return AVorkathState.EQUIP_RUBIES;
		}
		if (isInVorkath() && calculateHealth(vorkath, 750) < 265 && utils.inventoryContains(DIAMOND_SET) && !utils.isItemEquipped(DIAMOND_SET) && acidSpots.isEmpty()&& configvk.useRanged() && !configvk.useBlowpipe())
		{
			return AVorkathState.EQUIP_DIAMONDS;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) == 1 && !isInVorkath())
		{
			return AVorkathState.DEACTIVATE_PRAY;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && banked){
			return AVorkathState.WALK_FIRST;
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && !banked){
			return AVorkathState.FIND_BANK;
		}
		if (player.getLocalLocation() == new LocalPoint(5824, 7872) && isInVorkath()) {
			utils.walk(new LocalPoint(6080, 7872));
		}
		if (isInPOH(client) && client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER) && configvk.usePOHpool()){
			return AVorkathState.DRINK_POOL;
		}
		if (utils.isItemEquipped(Collections.singleton(configvk.specWeapon())) && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) < configvk.specThreshold() * 10){
			WidgetItem weapon = utils.getInventoryWidgetItem(configvk.normalWeapon());
			WidgetItem offhand = utils.getInventoryWidgetItem(configvk.normalOffhand());
			if (weapon != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (offhand != null){
				clientThread.invoke(() -> client.invokeMenuAction("", "", offhand.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), offhand.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (utils.isItemEquipped(Collections.singleton(configvk.specWeapon())) && !isInVorkath()){
			WidgetItem weapon = utils.getInventoryWidgetItem(configvk.normalWeapon());
			WidgetItem offhand = utils.getInventoryWidgetItem(configvk.normalOffhand());
			if (weapon != null) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
			if (offhand != null){
				clientThread.invoke(() -> client.invokeMenuAction("", "", offhand.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), offhand.getIndex(), WidgetInfo.INVENTORY.getId()));
			}
		}
		if (!loot.isEmpty() && !utils.inventoryFull() && isInVorkath()){
			return AVorkathState.LOOT_ITEMS;
		}
		if (utils.inventoryContains(22124) && loot.isEmpty() && !isInPOH(client) && isInVorkath() && !configvk.onlytelenofood()){
			return AVorkathState.WALK_SECOND;
		}
		if (!utils.inventoryContains(configvk.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) <= configvk.healthTP() && loot.isEmpty() && !isInPOH(client) && isInVorkath()){
			return AVorkathState.WALK_SECOND;
		}
		if (utils.inventoryContains(configvk.foodID()) && utils.inventoryFull() && !loot.isEmpty() && !isInPOH(client) && isInVorkath()){
			return AVorkathState.EAT_FOOD;
		}
		if (getRestoreItem() == null && client.getBoostedSkillLevel(Skill.PRAYER) <= configvk.prayTP() && isInVorkath()){
			return AVorkathState.WALK_SECOND;
		}
		if (isInPOH(client)){
			return AVorkathState.TELE_EDGE;
		}
		if (player.getWorldArea().intersectsWith(RELEKKA_POH)){
			return AVorkathState.WALK_THIRD;
		}
		if (player.getWorldArea().intersectsWith(RELEKKA_TOWN)){
			return AVorkathState.USE_BOAT;
		}
		if (player.getWorldArea().intersectsWith(VORKATH)){
			return AVorkathState.JUMP_OBSTACLE;
		}
		if (!acidSpots.isEmpty() && isInVorkath()){
			return AVorkathState.ACID_WALK;
		}
		if (!noBomb && isInVorkath()){
			return AVorkathState.HANDLE_BOMB;
		}
		if (!noBomb2 && isInVorkath()){
			return AVorkathState.HANDLE_ICE;
		}
		if (configvk.antivenomplus() && client.getVar(VarPlayer.IS_POISONED) > 0 && isInVorkath()) {
			return AVorkathState.DRINK_ANTIVENOM;
		}
		if (client.getBoostedSkillLevel(Skill.RANGED) <= configvk.potThreshold() && isInVorkath() && configvk.useRanged()){
			return AVorkathState.DRINK_RANGE;
		}
		if (client.getBoostedSkillLevel(Skill.STRENGTH) <= configvk.potThreshold() && isInVorkath() && !configvk.useRanged()){
			return AVorkathState.DRINK_COMBAT;
		}
		if (configvk.superantifire() && client.getVarbitValue(6101) == 0 && isInVorkath()){
			return AVorkathState.DRINK_ANTIFIRE;
		}
		if (!configvk.superantifire() && client.getVarbitValue(3981) == 0 && isInVorkath()){
			return AVorkathState.DRINK_ANTIFIRE;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) == 0 && isInVorkath() && acidSpots.isEmpty() && noBomb2 && noBomb)
		{
			return AVorkathState.ACTIVATE_PRAY;
		}
		if (client.getVar(Varbits.QUICK_PRAYER) != 0 && isInVorkath() && !acidSpots.isEmpty() || !noBomb2 || !noBomb)
		{
			return AVorkathState.DEACTIVATE_PRAY;
		}
		if (utils.findNearestNpc(8059) != null && isInVorkath() && loot.isEmpty()  && utils.inventoryItemContainsAmount(configvk.foodID(), 3, false, false)){
			return AVorkathState.WAKE_VORKATH;
		}
		if (utils.findNearestNpc(8059) != null && isInVorkath() && loot.isEmpty()  && !utils.inventoryItemContainsAmount(configvk.foodID(), 3, false, false)){
			return AVorkathState.WALK_SECOND;
		}
		if (player.getWorldLocation().distanceTo(vorkath.getWorldArea()) <= 1 && configvk.useRanged()){
			return AVorkathState.MOVE_AWAY;
		}
		if (!utils.isItemEquipped(Collections.singleton(configvk.specWeapon())) && utils.inventoryFull() && utils.inventoryContains(configvk.foodID()) && configvk.normalOffhand() != 0 && calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && utils.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()){
			return AVorkathState.EAT_FOOD;
		}
		if (!utils.isItemEquipped(Collections.singleton(configvk.specWeapon())) &&  calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && utils.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()){
			return AVorkathState.EQUIP_SPEC;
		}
		if (utils.isItemEquipped(Collections.singleton(configvk.specWeapon())) && calculateHealth(vorkath, 750) >= configvk.specHP() && client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= configvk.specThreshold() * 10 && configvk.useSpec() && noBomb && noBomb2 && utils.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && isInVorkath()) {
			return AVorkathState.SPECIAL_ATTACK;
		}
		if (noBomb && noBomb2 &&utils.findNearestNpc(8061) != null && acidSpots.isEmpty() && vorkath != null && client.getLocalPlayer().getInteracting() != vorkath && isInVorkath()) {
			return AVorkathState.ATTACK_VORKATH;
		}
		else return AVorkathState.TIMEOUT;
	}
	private AVorkathState getBankState()
	{
		if (!banked){
			utils.depositAll();
			banked = true;
			return AVorkathState.DEPOSIT_ITEMS;
		}
		if (configvk.useSpec() && !utils.inventoryContains(configvk.specWeapon())) {
			utils.withdrawItem(configvk.specWeapon());
		}
		if (!utils.inventoryContains(2444) && configvk.useRanged() && !configvk.supers()){
			return AVorkathState.WITHDRAW_RANGED;
		}
		if (!utils.inventoryContains(22461) && configvk.useRanged() && configvk.supers()){
			return AVorkathState.WITHDRAW_RANGED;
		}
		if (!utils.inventoryContains(12695) && !configvk.useRanged()){
			return AVorkathState.WITHDRAW_COMBAT;
		}
		if (configvk.antivenomplus() && !utils.inventoryContains(12913)){
			return AVorkathState.WITHDRAW_VENOM;
		}
		if (!configvk.antivenomplus() && !utils.inventoryContains(5952)){
			return AVorkathState.WITHDRAW_VENOM;
		}
		if (configvk.superantifire() && !utils.inventoryContains(22209)){
			return AVorkathState.WITHDRAW_ANTIFIRE;
		}
		if (!configvk.superantifire() && !utils.inventoryContains(2452)){
			return AVorkathState.WITHDRAW_ANTIFIRE;
		}
		if (!utils.inventoryContains(12791)){
			return AVorkathState.WITHDRAW_POUCH;
		}
		if (!utils.inventoryContains(3024) && !utils.inventoryContains(2434)){
			return AVorkathState.WITHDRAW_RESTORES;
		}
		if (!utils.inventoryContains(8013)){
			return AVorkathState.WITHDRAW_TELES;
		}
		if (!utils.inventoryContains(DIAMOND_SET) && configvk.useRanged() && !configvk.useBlowpipe()){
			return AVorkathState.WITHDRAW_BOLTS;
		}

		if (!utils.inventoryContains(configvk.foodID())){
			return AVorkathState.WITHDRAW_FOOD1;	//main food
		}
		if (!utils.inventoryContains(configvk.foodID2())){
			return AVorkathState.WITHDRAW_FOOD2;	//alt food
		}
		if (player.getWorldArea().intersectsWith(EDGEVILLE_BANK) && utils.inventoryContains(configvk.foodID2()) && banked){
			return AVorkathState.WALK_FIRST;
		}
		else return AVorkathState.TIMEOUT;
	}


	@Subscribe
	private void onGameTick(final GameTick event) throws ClassNotFoundException, SQLException {
		player = client.getLocalPlayer();
		if (client != null && player != null)
		{
			if (!startTeaks) {
				return;
			}
			if (!client.isResized())
			{
				utils.sendGameMessage("client must be set to resizable mode");
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
				case SPECIAL_ATTACK:
					if (utils.isItemEquipped(Collections.singleton(configvk.specWeapon()))){
						clientThread.invoke(() -> client.invokeMenuAction("Use <col=00ff00>Special Attack</col>", "", 1, MenuAction.CC_OP.getId(), -1, 38862884));
					}
					break;
				case MOVE_AWAY:
					utils.walk(new WorldPoint(player.getWorldLocation().getX(), player.getWorldLocation().getY() - 3, player.getWorldLocation().getPlane()));
					break;
				case TELE_EDGE:
					utils.useDecorativeObject(13523, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), sleepDelay());
					timeout = tickDelay();
					break;
				case EQUIP_SPEC:
					WidgetItem weapon = utils.getInventoryWidgetItem(configvk.specWeapon());
					if (weapon != null) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", weapon.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), weapon.getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					break;
				case EQUIP_RUBIES:
					WidgetItem boltz = utils.getInventoryWidgetItem(RUBY_SET);
					if (boltz != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"",
										"",
										boltz.getId(),
										MenuAction.ITEM_SECOND_OPTION.getId(),
										boltz.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case DRINK_POOL:
					GameObject Pool = utils.findNearestGameObject(29240, 29241);
					utils.useGameObjectDirect(Pool, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case EQUIP_DIAMONDS:
					WidgetItem bolts = utils.getInventoryWidgetItem(DIAMOND_SET);
					if (bolts != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"",
										"",
										bolts.getId(),
										MenuAction.ITEM_SECOND_OPTION.getId(),
										bolts.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case ACID_WALK:
					calculateAcidFreePath();
					if (acidFreePathLength >= 3) {
						if (FirstWalk) {
							utils.walk(acidFreePath.get(1));
							FirstWalk = false;
						}
						if (!FirstWalk) {
							utils.walk(acidFreePath.get(acidFreePath.size() - 1));
							FirstWalk = true;
						}
					}
					break;
				case ATTACK_VORKATH:
					if (isInVorkath()) {
						utils.attackNPCDirect(vorkath);
					}
					break;
				case HANDLE_ICE:
					NPC npc = utils.findNearestNpc("Zombified Spawn");
					final WorldPoint locc = client.getLocalPlayer().getWorldLocation();
					clientThread.invoke(() -> client.invokeMenuAction("", "", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286876));
					timeout = 1;
					clientThread.invoke(() -> client.invokeMenuAction("", "", npc.getIndex(), MenuAction.SPELL_CAST_ON_NPC.getId(), npc.getLocalLocation().getX(), npc.getLocalLocation().getY()));
					break;
				case HANDLE_BOMB:
					final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
					final LocalPoint localLoc = LocalPoint.fromWorld(client, loc);
					dodgeRight = new LocalPoint(localLoc.getX() + 256, localLoc.getY());
					dodgeLeft = new LocalPoint(localLoc.getX() - 256, localLoc.getY());
					if (localLoc.distanceTo(dodgeLeft) <= 1){
						noBomb = true;
						noBomb2 = true;
					}
					if (localLoc.distanceTo(dodgeRight) <= 1){
						noBomb = true;
						noBomb2 = true;
					}
					if (localLoc.getX() < 6208) {
						utils.walk(dodgeRight);
						timeout = tickDelay();
						noBomb = true;
						noBomb2 = true;
						break;
					} else {
						utils.walk(dodgeLeft);
						timeout = tickDelay();
						noBomb = true;
						noBomb2 = true;
						break;
					}
				case DEACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1,  MenuAction.CC_OP.getId(), -1, 10485775));
					timeout = tickDelay();
					break;
				case ACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1,  MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case WITHDRAW_COMBAT:
					if (!configvk.supers()) {
						utils.withdrawItem(9739);
					}
					else {
						utils.withdrawItem(12695);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_RANGED:
					if (!configvk.supers()) {
						utils.withdrawItem(2444);
					}
					else {
						utils.withdrawItem(22461);
					}
					timeout = tickDelay();
					break;
				case WAKE_VORKATH:
					clientThread.invoke(() -> client.invokeMenuAction("", "", utils.findNearestNpc("Vorkath").getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0));
					timeout = tickDelay();
					break;
				case CLOSE_BANK:
					utils.closeBank();
					timeout = tickDelay();
					break;
				case WITHDRAW_VENOM:
					if (configvk.antivenomplus()) {
						utils.withdrawItemAmount(12913, configvk.antipoisonamount());
					}
					if (!configvk.antivenomplus()){
						utils.withdrawItemAmount(5952, configvk.antipoisonamount());
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_ANTIFIRE:
					if (configvk.superantifire()) {
						utils.withdrawItem(22209);
					}
					if (!configvk.superantifire()){
						utils.withdrawItem(2452);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_POUCH:
					utils.withdrawItem(12791);
					timeout = tickDelay();
					break;
				case WITHDRAW_RESTORES:
					if (configvk.useRestores()) {
						utils.withdrawItemAmount(3024, configvk.praypotAmount());
					}
					else
					{
						utils.withdrawItemAmount(2434, configvk.praypotAmount());
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_TELES:
					utils.withdrawItemAmount(8013, 10);
					timeout = tickDelay();
					break;
				case WITHDRAW_BOLTS:
					if (utils.bankContains(ItemID.DIAMOND_DRAGON_BOLTS_E, 1)) {
						utils.withdrawAllItem(ItemID.DIAMOND_DRAGON_BOLTS_E);
					}
					if (!utils.bankContains(ItemID.DIAMOND_DRAGON_BOLTS_E, 1) && utils.bankContains(ItemID.DIAMOND_BOLTS_E, 1)){
						utils.withdrawAllItem(ItemID.DIAMOND_BOLTS_E);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_FOOD1:
					utils.withdrawItemAmount(configvk.foodID(), configvk.foodAmount());
					timeout = tickDelay();
					break;
				case WITHDRAW_FOOD2:
					utils.withdrawItemAmount(configvk.foodID2(), configvk.foodAmount2());
					timeout = tickDelay();
					break;
				case MOVING:
					utils.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case DRINK_ANTIVENOM:
					WidgetItem ven = GetAntiVenomItem();
					if (ven != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										ven.getId(),
										MenuAction.ITEM_FIRST_OPTION.getId(),
										ven.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case DRINK_COMBAT:
					WidgetItem Cpot = GetCombatItem();
					if (Cpot != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Cpot.getId(),
										MenuAction.ITEM_FIRST_OPTION.getId(),
										Cpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case EAT_FOOD:
					WidgetItem food = GetFoodItem();
					if (food != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"",
										"",
										food.getId(),
										MenuAction.ITEM_FIRST_OPTION.getId(),
										food.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case DRINK_RANGE:
					WidgetItem Rpot = GetRangedItem();
					if (Rpot != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										Rpot.getId(),
										MenuAction.ITEM_FIRST_OPTION.getId(),
										Rpot.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case DRINK_ANTIFIRE:
					WidgetItem overload = GetAntifireItem();
					if (overload != null) {
						clientThread.invoke(() ->
								client.invokeMenuAction(
										"Drink",
										"<col=ff9040>Potion",
										overload.getId(),
										MenuAction.ITEM_FIRST_OPTION.getId(),
										overload.getIndex(),
										WidgetInfo.INVENTORY.getId()
								)
						);
					}
					break;
				case WALK_FIRST:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.ITEM_THIRD_OPTION.getId(), utils.getInventoryWidgetItem(8013).getIndex(), WidgetInfo.INVENTORY.getId()));
					banked = false;
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 8013, MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(8013).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case WALK_THIRD:
					utils.walk(new WorldPoint(2643, 3676, 0));
					timeout = tickDelay();
					break;
				case USE_BOAT:
					GameObject boat = utils.findNearestGameObject(29917);
					utils.useGameObjectDirect(boat, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
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
					break;
				case JUMP_OBSTACLE:
					utils.useGameObject(31990, 3, sleepDelay());
					timeout = tickDelay();
					break;
			}
		}
	}
	@Subscribe
	private void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName().toLowerCase();

		if (lootableItems.stream().anyMatch(itemName.toLowerCase()::contains) && item.getId() != 1751) {             // || client.getItemDefinition(event.getItem().getId()).getName() == "Dragon bones" || client.getItemDefinition(event.getItem().getId()).getName() == "Draconic visage") {
			loot.add(item);
		}
	}
	@Subscribe
	private void onItemDespawned(ItemDespawned event) {
		loot.remove(event.getItem());
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

	private int calculateHealth(NPC target, Integer maxHealth)
	{
		if (target == null || target.getName() == null)
		{
			return -1;
		}

		final int healthScale = target.getHealthScale();
		final int healthRatio = target.getHealthRatio();

		if (healthRatio < 0 || healthScale <= 0 || maxHealth == null)
		{
			return -1;
		}

		return (int)((maxHealth * healthRatio / healthScale) + 0.5f);
	}


	@Subscribe
	private void onClientTick(ClientTick event)
	{
		if (acidSpots.size() != lastAcidSpotsSize)
		{
			if (acidSpots.size() == 0)
			{
				acidFreePath.clear();
			}
			else
			{
				calculateAcidFreePath();
			}
			lastAcidSpotsSize = acidSpots.size();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
			if (vorkath != null)
			{
				final Actor actor = event.getActor();
				if (actor.getAnimation() == 7950 && actor.getName().contains("Vorkath"))
				{
					Widget widget = client.getWidget(10485775);
					if (widget != null)
					{
						bounds = widget.getBounds();
					}
				}
				if (actor.getAnimation() == 7949 && actor.getName().contains("Vorkath"))
				{
					if (client.getVar(Varbits.QUICK_PRAYER) == 1)
					{
						clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					}
				}
			}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event) {
		if (this.client.getGameState() == GameState.LOGGED_IN) {
			final Projectile projectile = event.getProjectile();

			final WorldPoint loc = client.getLocalPlayer().getWorldLocation();

			final LocalPoint localLoc = LocalPoint.fromWorld(client, loc);

			if (projectile.getId() == ProjectileID.VORKATH_BOMB_AOE) {
				noBomb = false;
			}
			if (projectile.getId() == ProjectileID.VORKATH_ICE) {
				noBomb2 = false;
				if (client.getLocalPlayer().getInteracting() != null) {
					utils.walk(localLoc);
				}
			}
		}
	}
	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		final Projectile proj = event.getProjectile();
		final LocalPoint loc = event.getPosition();
		final WorldPoint location = WorldPoint.fromLocal(client, loc);
		final LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		final WorldPoint loc1 = client.getLocalPlayer().getWorldLocation();
		final LocalPoint localLoc = LocalPoint.fromWorld(client, loc1);
		if (proj.getId() == ProjectileID.VORKATH_POISON_POOL_AOE)
		{
			addAcidSpot(WorldPoint.fromLocal(client, loc));
		}
		if (proj.getId() == ProjectileID.VORKATH_ICE) {
			noBomb2 = false;
			if (client.getLocalPlayer().getInteracting() != null) {
				utils.walk(localLoc);
			}
		}
		if (proj.getId() == ProjectileID.VORKATH_BOMB_AOE) {
			noBomb = false;
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
			final NPC npc = event.getNpc();

			if (npc.getName() == null)
			{
				return;
			}

			if (npc.getName().equals("Vorkath"))
			{
				vorkath = event.getNpc();
			}

			if (npc.getName().equals("Zombified Spawn"))
			{
				noBomb2 = false;
			}

	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{

			final NPC npc = event.getNpc();

			if (npc.getName() == null)
			{
				return;
			}

			Widget widget = client.getWidget(10485775);

			if (widget != null)
			{
				bounds = widget.getBounds();
			}

			if (npc.getName().equals("Vorkath"))
			{
				vorkath = null;
				if (client.getVar(Varbits.QUICK_PRAYER) == 1)
				{
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
				}
			}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) {
		loot.clear();
		GameState gamestate = event.getGameState();
		if (gamestate == GameState.LOADING && inFight) {
			reset();
		}
	}

	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);

	public static boolean isInPOH(Client client) {
		return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);
	}

	@Subscribe
	private void onChatMessage(ChatMessage event) {

			if (event.getType() != ChatMessageType.GAMEMESSAGE)
			{
				return;
			}

			Widget widget = client.getWidget(10485775);

			if (widget != null)
			{
				bounds = widget.getBounds();
			}

			String prayerMessage = ("Your prayers have been disabled!");
			String poisonMessage = ("You have been poisoned by venom!");
			String poisonMessageNV = ("You have been poisoned!");
			String frozenMessage = ("You have been frozen!");
			String spawnExplode = ("The spawn violently explodes, unfreezing you as it does so.");
			String unfrozenMessage = ("You become unfrozen as you kill the spawn.");

			if ((event.getMessage().equals(prayerMessage) || event.getMessage().contains(prayerMessage)))
			{
				clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
			}

			if (((event.getMessage().equals(frozenMessage))))
			{
				noBomb = false;
				noBomb2 = false;
			}
			if (((event.getMessage().equals(poisonMessage))))
			{
				WidgetItem pot = GetAntiVenomItem();
				if (pot != null) {
					clientThread.invoke(() ->
							client.invokeMenuAction(
									"Drink",
									"<col=ff9040>Potion",
									pot.getId(),
									MenuAction.ITEM_FIRST_OPTION.getId(),
									pot.getIndex(),
									WidgetInfo.INVENTORY.getId()
							)
					);
				}
			}
			if (((event.getMessage().equals(poisonMessageNV))))
			{
				WidgetItem pot = GetAntiVenomItem();
				if (pot != null && configvk.antivenomplus()) {
					clientThread.invoke(() ->
							client.invokeMenuAction(
									"Drink",
									"<col=ff9040>Potion",
									pot.getId(),
									MenuAction.ITEM_FIRST_OPTION.getId(),
									pot.getIndex(),
									WidgetInfo.INVENTORY.getId()
							)
					);
				}
			}
			if ((event.getMessage().equals(spawnExplode) || (event.getMessage().equals(unfrozenMessage))))
			{
				noBomb = true;
				noBomb2 = true;
				if (isInVorkath()) {
					utils.attackNPCDirect(vorkath);
				}
			}

	}

	private void addAcidSpot(WorldPoint acidSpotLocation)
	{
		if (!acidSpots.contains(acidSpotLocation))
		{
			acidSpots.add(acidSpotLocation);
		}
	}

	private void calculateAcidFreePath()
	{
		acidFreePath.clear();

		if (vorkath == null)
		{
			return;
		}

		final int[][][] directions = {
				{
						{0, 1}, {0, -1} // Positive and negative Y
				},
				{
						{1, 0}, {-1, 0} // Positive and negative X
				}
		};

		List<WorldPoint> bestPath = new ArrayList<>();
		double bestClicksRequired = 99;

		final WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();
		final WorldPoint vorkLoc = vorkath.getWorldLocation();
		final int maxX = vorkLoc.getX() + 14;
		final int minX = vorkLoc.getX() - 8;
		final int maxY = vorkLoc.getY() - 1;
		final int minY = vorkLoc.getY() - 8;

		// Attempt to search an acid free path, beginning at a location
		// adjacent to the player's location (including diagonals)
		for (int x = -1; x < 2; x++)
		{
			for (int y = -1; y < 2; y++)
			{
				final WorldPoint baseLocation = new WorldPoint(playerLoc.getX() + x,
						playerLoc.getY() + y, playerLoc.getPlane());

				if (acidSpots.contains(baseLocation) || baseLocation.getY() < minY || baseLocation.getY() > maxY)
				{
					continue;
				}

				// Search in X and Y direction
				for (int d = 0; d < directions.length; d++)
				{
					// Calculate the clicks required to start walking on the path
					double currentClicksRequired = Math.abs(x) + Math.abs(y);
					if (currentClicksRequired < 2)
					{
						currentClicksRequired += Math.abs(y * directions[d][0][0]) + Math.abs(x * directions[d][0][1]);
					}
					if (d == 0)
					{
						// Prioritize a path in the X direction (sideways)
						currentClicksRequired += 0.5;
					}

					List<WorldPoint> currentPath = new ArrayList<>();
					currentPath.add(baseLocation);

					// Positive X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][0][0],
								baseLocation.getY() + i * directions[d][0][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					// Negative X (first iteration) or positive Y (second iteration)
					for (int i = 1; i < 25; i++)
					{
						final WorldPoint testingLocation = new WorldPoint(baseLocation.getX() + i * directions[d][1][0],
								baseLocation.getY() + i * directions[d][1][1], baseLocation.getPlane());

						if (acidSpots.contains(testingLocation) || testingLocation.getY() < minY || testingLocation.getY() > maxY
								|| testingLocation.getX() < minX || testingLocation.getX() > maxX)
						{
							break;
						}

						currentPath.add(testingLocation);
					}

					if (currentPath.size() >= this.acidFreePathLength && currentClicksRequired < bestClicksRequired
							|| (currentClicksRequired == bestClicksRequired && currentPath.size() > bestPath.size()))
					{
						bestPath = currentPath;
						bestClicksRequired = currentClicksRequired;
					}
				}
			}
		}

		if (bestClicksRequired != 99)
		{
			acidFreePath = bestPath;
		}
	}


	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{

			final GameObject obj = event.getGameObject();

			if (obj.getId() == ObjectID.ACID_POOL || obj.getId() == ObjectID.ACID_POOL_32000)
			{
				addAcidSpot(obj.getWorldLocation());
			}

	}
	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{

			final GameObject obj = event.getGameObject();

			if (obj.getId() == ObjectID.ACID_POOL || obj.getId() == ObjectID.ACID_POOL_32000)
			{
				acidSpots.remove(obj.getWorldLocation());
			}

	}
	public void activatePrayer(WidgetInfo widgetInfo)
	{
		Widget prayer_widget = client.getWidget(widgetInfo);

		if (prayer_widget == null)
		{
			return;
		}

		if (client.getBoostedSkillLevel(Skill.PRAYER) <= 0)
		{
			return;
		}

		clientThread.invoke(() ->
				client.invokeMenuAction(
				"Activate",
						prayer_widget.getName(),
						1,
						MenuAction.CC_OP.getId(),
						prayer_widget.getItemId(),
						prayer_widget.getId()
				)
		);
	}
}