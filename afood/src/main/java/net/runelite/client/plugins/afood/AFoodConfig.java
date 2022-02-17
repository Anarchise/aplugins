package net.runelite.client.plugins.afood;

import net.runelite.client.config.*;
import net.runelite.client.config.Button;

import java.awt.*;
import java.awt.event.KeyEvent;


@ConfigGroup("afood")
public interface AFoodConfig extends Config
{

	@ConfigItem(
			position = 25,
			keyName = "prayDrinker",
			name = "Drink Prayer Potions",
			description = "Automatically drinks prayer pots/super restores"
	)
	default boolean prayDrinker()
	{
		return false;
	}

	@ConfigItem(
			keyName = "minPrayerLevel",
			name = "Minimum level to drink at",
			description = "",
			position = 26
	)
	default int minPrayerLevel() { return 1; }

	@ConfigItem(
			keyName = "maxPrayerLevel",
			name = "Maximum level to drink at",
			description = "",
			position = 27
	)
	default int maxPrayerLevel() { return 30; }

	@ConfigItem(
			position = 28,
			keyName = "foodEater",
			name = "Eat Food",
			description = "Automatically eat food"
	)
	default boolean foodEater()
	{
		return false;
	}

	@ConfigItem(
			keyName = "foodToEat",
			name = "Food ID 1",
			description = "ID of food to eat (first)",
			position = 29
	)
	default int foodToEat()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "foodtoEat2",
			name = "Food ID 2",
			description = "ID of food to eat (second)",
			position = 30
	)
	default int foodtoEat2() {return 0;}

	@ConfigItem(
			keyName = "foodtoEat3",
			name = "Food ID 3",
			description = "ID of food to eat (third)",
			position = 31
	)
	default int foodtoEat3() {return 0;}

	@ConfigItem(
			keyName = "minimumHealthSingle",
			name = "Health Single Eat",
			description = "Health to single eat food at",
			position = 32
	)
	default int minimumHealthSingle()
	{
		return 40 ;
	}

	@ConfigItem(
			keyName = "minimumHealthDouble",
			name = "Health Double Eat",
			description = "Health to combo eat 2 food at",
			position = 33
	)
	default int minimumHealthDouble()
	{
		return 20;
	}

	@ConfigItem(
			keyName = "thirdItemBrews",
			name = "Combo eat with Brews",
			description = "Use brews as the second item in combo eats",
			position = 34
	)
	default boolean thirdItemBrews() {return true;}

	@ConfigItem(
			keyName = "minimumHealthTriple",
			name = "Health Triple Eat",
			description = "Health to combo eat 3 food at",
			position = 35
	)
	default int minimumHealthTriple()
	{
		return 20;
	}

	default boolean logParams()
	{
		return false;
	}
}