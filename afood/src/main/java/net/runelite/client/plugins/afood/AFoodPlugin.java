package net.runelite.client.plugins.afood;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
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
import net.runelite.client.plugins.afood.PrayerRestoreType;
import net.runelite.client.plugins.autils.AUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
	name = "AFood",
	description = "Eats food & drinks prayer pots/restores",
	tags = {"food","pots","prayer"},
	enabledByDefault = false
)
public class AFoodPlugin extends Plugin
{

	@Provides
	AFoodConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AFoodConfig.class);
	}

	private int nextRestoreVal = 0;

	@Inject
	private AFoodConfig configfe;

	@Inject
	private ClientThread clientThread;

	@Inject private ItemManager itemManager;

	private Rectangle bounds;

	private int timeout;
	private Random r;
int jgjj = 1929;
	@Inject private Client client;

	@Inject
	private AUtils utils;

	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("afood"))
		{
			return;
		}
		nextRestoreVal = r.nextInt(configfe.maxPrayerLevel() - configfe.minPrayerLevel()) + configfe.minPrayerLevel();
	}

	@Inject ConfigManager configManager;

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

	@Subscribe
	private void onGameTick(final GameTick event) {
		if (configfe.prayDrinker()) {
			WidgetItem restoreItem = getRestoreItem();
			int currentPrayerPoints = client.getBoostedSkillLevel(Skill.PRAYER);
			if (currentPrayerPoints <= utils.getRandomIntBetweenRange(configfe.minPrayerLevel(), configfe.maxPrayerLevel())) {
				clientThread.invoke(() ->
						client.invokeMenuAction(
								"Drink",
								"<col=ff9040>Potion",
								restoreItem.getId(),
								MenuAction.ITEM_FIRST_OPTION.getId(),
								restoreItem.getIndex(),
								WidgetInfo.INVENTORY.getId()
						)
				);
			}
		}
		if (configfe.foodEater()){
			int health = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
			if (health <= this.configfe.minimumHealthTriple()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodToEat(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodToEat()).getIndex(), WidgetInfo.INVENTORY.getId()));
				if (configfe.thirdItemBrews()){
					WidgetItem restoreItem = getBrewItem();
					clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodtoEat3(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodtoEat3()).getIndex(), WidgetInfo.INVENTORY.getId()));
				}
				if (!configfe.thirdItemBrews()) {
					clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodtoEat2(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodtoEat2()).getIndex(), WidgetInfo.INVENTORY.getId()));
					clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodtoEat3(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodtoEat3()).getIndex(), WidgetInfo.INVENTORY.getId()));
				}

			}

			if (health <= this.configfe.minimumHealthDouble() && health > this.configfe.minimumHealthTriple()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodToEat(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodToEat()).getIndex(), WidgetInfo.INVENTORY.getId()));
				if (configfe.thirdItemBrews()){
					WidgetItem restoreItem = getBrewItem();
					clientThread.invoke(() -> client.invokeMenuAction("Drink", "<col=ff9040>Potion", restoreItem.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), restoreItem.getIndex(), WidgetInfo.INVENTORY.getId()));
				}
				if (!configfe.thirdItemBrews()){
					clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodtoEat2(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodtoEat2()).getIndex(), WidgetInfo.INVENTORY.getId()));
				}
			}
			if (health < this.configfe.minimumHealthSingle() && health > this.configfe.minimumHealthDouble()) {
				clientThread.invoke(() -> client.invokeMenuAction("", "", configfe.foodToEat(), MenuAction.ITEM_FIRST_OPTION.getId(), utils.getInventoryWidgetItem(configfe.foodToEat()).getIndex(), WidgetInfo.INVENTORY.getId()));
			}
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
}