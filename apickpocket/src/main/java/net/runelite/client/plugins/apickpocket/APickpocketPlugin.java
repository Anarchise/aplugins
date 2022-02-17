package net.runelite.client.plugins.apickpocket;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import org.pf4j.Extension;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "APickpocket",
	description = "Anarchise' Pickpocketter",
	tags = {"anarchise","thieving","aplugins"},
	enabledByDefault = false
)
public class APickpocketPlugin extends Plugin
{

	private int nextRestoreVal = 0;

	@Inject
	private Client client;

	@Provides
	APickpocketConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(APickpocketConfig.class);
	}
	@Inject
	private APickpocketConfig configph;
	@Inject
	private ClientThread clientThread;
	private int timeout;
	@Inject
	private ItemManager itemManager;
	@Inject
	private AUtils utils;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	public ReflectBreakHandler chinBreakHandler;
	@Inject
	APickpocketOverlay zulrahOverlay;
	Instant botTimer;
	public APickpocketState state;
	WorldPoint ResetLocation = new WorldPoint(0, 0, 0);
	@Override
	protected void startUp() throws Exception
	{
		chinBreakHandler.registerPlugin(this);
		reset();
	}
	private final Set<Integer> itemIds = new HashSet<>();

	private void reset() {
		itemIds.clear();
		itemIds.addAll(utils.stringToIntList(configph.items()));
		startTeaks = false;
		banked = false;
		state = null;
		botTimer = null;
		overlayManager.remove(zulrahOverlay);
		chinBreakHandler.unregisterPlugin(this);
	}
	private void handleDropItems() {
		utils.dropAllExcept(utils.stringToIntList(configph.items()), true, configph.sleepMin(), configph.sleepMax());
	}
	@Override
	protected void shutDown() throws Exception
	{
		reset();
	}
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("apickpocket")) {
			return;
		}
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startTeaks) {
				startTeaks = true;
				state = null;
				overlayManager.add(zulrahOverlay);
				int[] customTemp = utils.stringToIntArray(configph.returnLoc());
				ResetLocation = new WorldPoint(customTemp[0], customTemp[1], customTemp[2]);
				chinBreakHandler.startPlugin(this);
				botTimer = Instant.now();
			} else {
				reset();
			}
		}
	}
	private static final List<Integer> regions = Arrays.asList(7513, 7514, 7769, 7770);
	private static boolean isInPOH(Client client) {return Arrays.stream(client.getMapRegions()).anyMatch(regions::contains);}

	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", bankTarget.getId(), utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(), bankTarget.getSceneMinLocation().getY()));
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
	public APickpocketState getState()
	{
		if (timeout > 0)
		{
			return APickpocketState.TIMEOUT;
		}
		if(utils.isBankOpen()){
			return getBankState();
		}
		else {
			return getStates();
		}
	}
	Player player;
	WorldPoint walkzone = new WorldPoint(0, 0, 0);
	Instant veilTimer;
	NPC beast;
	boolean leavingcave = false;
	boolean needsBank = false;
	long timeRan;
	int timeRun;
	int resetTime = 61;
	int timeRuns;
	boolean isVeiled = false;
	int l = 111;
	NPC bs;
	WorldPoint DOORPOINT = new WorldPoint(3243, 6073, 0);
	WorldArea BANK = new WorldArea(new WorldPoint(3250, 6100, 0), new WorldPoint(3263, 6111, 0));
	private APickpocketState getStates() {
		WallObject CLOSED_DOOR = utils.findWallObjectWithin(DOORPOINT,5, Collections.singleton(36253));

		if (veilTimer != null) {
			Duration duration = Duration.between(veilTimer, Instant.now());
			timeRan = duration.getSeconds();
			timeRun = (int) timeRan;
			timeRuns = (resetTime) - timeRun;
			if (timeRun > resetTime) {
				isVeiled = false;
				timeRan = 0;
				timeRun = 0;
				timeRuns = 0;
			}
		}

		if (configph.bank() && !utils.inventoryContains(configph.foodID())) {
			banked = false;
			return APickpocketState.FIND_BANK2;
		}
		if (configph.bank() && configph.dodgynecks() && !utils.inventoryContains(21143)) {
			banked = false;
			return APickpocketState.FIND_BANK2;
		}
		if (configph.bank() && utils.inventoryFull()) {
			banked = false;
			return APickpocketState.FIND_BANK2;
		}
		if (!configph.bank() && !utils.inventoryContains(configph.foodID()) && client.getBoostedSkillLevel(Skill.HITPOINTS) < 15) {
			utils.sendGameMessage("HP TOO LOW AND BANK DISABLED!");
			return APickpocketState.UNHANDLED_STATE;
		}

		if (!configph.bank() && utils.inventoryFull()) {
			return APickpocketState.DROP_INV;
		}
		if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= configph.minHealth() && utils.inventoryContains(configph.foodID())) {
			return APickpocketState.EAT_FOOD;
		}
		if (utils.inventoryContains(1935)) {
			return APickpocketState.DROP_JUG;
		}
		if (configph.shadowVeil() && !isVeiled && client.getVarbitValue(12414) == 0 && client.getVarbitValue(12291) == 0 && client.getGameState() == GameState.LOGGED_IN){
			return APickpocketState.CAST_SV;
		}
		if (utils.inventoryItemContainsAmount(22521, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22522, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22523, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22524, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22525, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22526, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22527, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22528, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22529, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22530, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22531, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22532, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22533, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22534, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22535, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22536, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22537, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(22538, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}		if (utils.inventoryItemContainsAmount(24703, configph.maxPouches(), true, false)){
			return APickpocketState.OPEN_POUCH;
		}
		if (configph.dodgynecks() && utils.inventoryContains(ItemID.DODGY_NECKLACE) && !utils.isItemEquipped(Collections.singleton(ItemID.DODGY_NECKLACE))) {
			return APickpocketState.EQUIP_NECKLACE;
		}
		if (!utils.inventoryFull() && bs != null) {
			return APickpocketState.PICKPOCKET;
		}
		if (!utils.inventoryFull() && bs == null) {
			return APickpocketState.WALK_SECOND;
		}
		return APickpocketState.TIMEOUT;
	}
	private boolean banked = false;

	private APickpocketState getBankState()
	{

		if (!banked){
			utils.depositAll();
			banked = true;
			leavingcave = false;
			return APickpocketState.DEPOSIT_ITEMS;
		}
		if (configph.shadowVeil() && !utils.inventoryContains(564)) {
			return APickpocketState.WITHDRAW_COSMIC;
		}
		if (configph.dodgynecks() && !utils.inventoryContains(21143)){
			return APickpocketState.WITHDRAW_NECKLACES;
		}
		if (!utils.inventoryContains(configph.foodID())) {
			return APickpocketState.WITHDRAW_FOOD1;
		}
		if (banked && utils.inventoryContains(configph.foodID())) {
			return APickpocketState.WALK_SECOND;
		}
		return APickpocketState.TIMEOUT;
	}
	public boolean startTeaks = false;


	@Inject ConfigManager configManager;

	public Prayer currentPrayer;

	public void useWallObject(WallObject targetObject, long sleepDelay, int opcode)
	{
		if(targetObject!=null) {
			clientThread.invoke(() -> client.invokeMenuAction("", "", targetObject.getId(), opcode, targetObject.getLocalLocation().getSceneX(), targetObject.getLocalLocation().getSceneY()));
		}
	}

	private GameObject bs2;
	private int kkk = 0;
	@Subscribe
	private void onGameTick(final GameTick event) throws IOException {
		if (!startTeaks){
			return;
		}
		if (chinBreakHandler.isBreakActive(this)){
			return;
		}
		if (chinBreakHandler.shouldBreak(this))
		{
			chinBreakHandler.startBreak(this);
		}

		bs = utils.findNearestNpc(configph.npcID());
		bs2 = utils.findNearestGameObject(configph.npcID());
		beast = utils.getFirstNPCWithLocalTarget();
		player = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		if (client != null && player != null) {
			if (!client.isResized()) {
				utils.sendGameMessage("client must be set to resizable mode");
				return;
			}
			state = getState();
			switch (state) {
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case DROP_INV:
					handleDropItems();
					break;
				case CAST_SV:
					veilTimer = Instant.now();
					clientThread.invoke(() -> client.invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, 14287025));
					isVeiled = true;
					timeout = tickDelay();
					break;
				case DROP_JUG:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 1935, MenuAction.ITEM_FIFTH_OPTION.getId(), utils.getInventoryWidgetItem(1935).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case WALK_FIRST:
					utils.walk(walkzone);
					timeout = tickDelay();
					break;
				case WALK_SECOND:
					utils.walk(ResetLocation);
					timeout = tickDelay();
					break;
				case OPEN_POUCH:
					if (utils.inventoryContains(22521)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22521, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22521).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22522)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22522, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22522).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22523)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22523, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22523).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22524)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22524, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22524).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22525)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22525, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22525).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22526)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22526, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22526).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22527)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22527, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22527).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22528)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22528, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22528).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22529)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22529, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22529).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22530)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22530, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22530).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22531)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22531, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22531).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22532)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22532, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22532).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22533)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22533, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22533).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22534)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22534, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22534).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22535)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22535, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22535).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22536)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22536, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22536).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22537)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22537, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22537).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(22538)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 22538, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(22538).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
					if (utils.inventoryContains(24703)) {
						clientThread.invoke(() -> client.invokeMenuAction(
								"", "", 24703, MenuAction.ITEM_FIRST_OPTION.getId(),
								utils.getInventoryWidgetItem(24703).getIndex(), WidgetInfo.INVENTORY.getId()));
					}
                    timeout = tickDelay();
					break;
				case OPEN_DOOR:
					banked = false;
					WallObject DOOR = utils.findNearestWallObject(36253);
					useWallObject(DOOR, sleepDelay(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
					timeout = tickDelay();
					break;
				case PICKPOCKET:
					banked = false;
					if (configph.typethief() == APickpocketThiefType.NPC) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", bs.getIndex(), configph.type().action.getId(), 0, 0));
					}
					if (configph.typethief() == APickpocketThiefType.OBJECT) {
						clientThread.invoke(() -> client.invokeMenuAction("", "", bs2.getId(), configph.type().action.getId(), bs2.getSceneMinLocation().getX(), bs2.getSceneMinLocation().getY()));
					}
					timeout = tickDelay();
					break;
				case EQUIP_NECKLACE:
					clientThread.invoke(() -> client.invokeMenuAction("", "", 21143, MenuAction.ITEM_SECOND_OPTION.getId(), utils.getInventoryWidgetItem(21143).getIndex(), WidgetInfo.INVENTORY.getId()));
					timeout = tickDelay();
					break;
				case DEACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Deactivate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					timeout = tickDelay();
					break;
				case ACTIVATE_PRAY:
					clientThread.invoke(() -> client.invokeMenuAction("Activate", "Quick-prayers", 1, MenuAction.CC_OP.getId(), -1, 10485775));
					break;
				case WITHDRAW_COSMIC:
					utils.withdrawAllItem(564);
					timeout = 4;
					break;
				case WITHDRAW_NECKLACES:
					utils.withdrawItemAmount(21143, configph.dodgyNecks());
					timeout = 4;
					break;
				case WITHDRAW_HOUSE:
					utils.withdrawItemAmount(8013, 5);
					timeout = 4;
					break;
				case WITHDRAW_FOOD1:
					utils.withdrawItemAmount(configph.foodID(), configph.foodAmount());
					timeout = 4;
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
				case FIND_BANK:
					//resetZul();
					openBank();
					timeout = tickDelay();
					break;
				case FIND_BANK2:
					GameObject bank = utils.findNearestGameObject(configph.bankID());
					utils.useGameObjectDirect(bank, sleepDelay(), configph.type2().action.getId());
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					//utils.depositAll();
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
					timeout = 4;
					break;

			}
		}
	}
	public WidgetItem GetFoodItem() {
		WidgetItem item;

		item = utils.getInventoryWidgetItem(configph.foodID());

		if (item != null)
		{
			return item;
		}

		return item;
	}
	private long sleepDelay()
	{
		long sleepLength = utils.randomDelay(configph.sleepWeightedDistribution(), configph.sleepMin(), configph.sleepMax(), configph.sleepDeviation(), configph.sleepTarget());
		return sleepLength;
	}
	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(configph.tickDelayWeightedDistribution(), configph.tickDelayMin(), configph.tickDelayMax(), configph.tickDelayDeviation(), configph.tickDelayTarget());
		return tickLength;
	}
}