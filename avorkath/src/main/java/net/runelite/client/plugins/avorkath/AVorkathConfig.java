package net.runelite.client.plugins.avorkath;

import net.runelite.client.config.*;
import net.runelite.client.config.Button;

import java.awt.*;
import java.awt.event.KeyEvent;


@ConfigGroup("avork")
public interface AVorkathConfig extends Config
{

	@ConfigSection(
			keyName = "delayConfig",
			name = "Sleep Delay Configuration",
			description = "Configure how the bot handles sleep delays",
			position = 0
	)
	String delayConfig = "delayConfig";

	@Range(
			min = 0,
			max = 550
	)
	@ConfigItem(
			keyName = "sleepMin",
			name = "Sleep Min",
			description = "",
			position = 2,
			section = "delayConfig"
	)
	default int sleepMin()
	{
		return 60;
	}

	@Range(
			min = 0,
			max = 550
	)
	@ConfigItem(
			keyName = "sleepMax",
			name = "Sleep Max",
			description = "",
			position = 3,
			section = "delayConfig"
	)
	default int sleepMax()
	{
		return 350;
	}

	@Range(
			min = 0,
			max = 550
	)
	@ConfigItem(
			keyName = "sleepTarget",
			name = "Sleep Target",
			description = "",
			position = 4,
			section = "delayConfig"
	)
	default int sleepTarget()
	{
		return 100;
	}

	@Range(
			min = 0,
			max = 550
	)
	@ConfigItem(
			keyName = "sleepDeviation",
			name = "Sleep Deviation",
			description = "",
			position = 5,
			section = "delayConfig"
	)
	default int sleepDeviation()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "sleepWeightedDistribution",
			name = "Sleep Weighted Distribution",
			description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
			position = 6,
			section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}


	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Game Tick Min",
			description = "",
			position = 8,
			section = "delayConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayMax",
			name = "Game Tick Max",
			description = "",
			position = 9,
			section = "delayConfig"
	)
	default int tickDelayMax()
	{
		return 3;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayTarget",
			name = "Game Tick Target",
			description = "",
			position = 10,
			section = "delayConfig"
	)
	default int tickDelayTarget()
	{
		return 2;
	}

	@Range(
			min = 0,
			max = 10
	)
	@ConfigItem(
			keyName = "tickDelayDeviation",
			name = "Game Tick Deviation",
			description = "",
			position = 11,
			section = "delayConfig"
	)
	default int tickDelayDeviation()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "tickDelayWeightedDistribution",
			name = "Game Tick Weighted Distribution",
			description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
			position = 12,
			section = "delayConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}


	@ConfigSection(
			keyName = "pluginConfig",
			name = "Plugin Configuration",
			description = "",
			position = 14
	)
	String pluginConfig = "pluginConfig";

	@ConfigItem(
			keyName = "supers",
			name = "Super Pots",
			description = "Enable to use Bastion potions for ranged, Super Combat for melee. Disable to use ranging potions for ranged, Combat potions for melee.",
			position = 55
	)
	default boolean supers() { return true; }

	@ConfigItem(
			keyName = "potThreshold",
			name = "Level to Drink",
			description = "Enter level to drink combat related potions, e.g set at 99, it will drink at or below 99",
			position = 56
	)
	default int potThreshold() { return 99; }

	@ConfigItem(
			keyName = "useRanged",
			name = "Ranged Mode",
			description = "If disabled, uses melee",
			position = 57
	)
	default boolean useRanged() { return true; }

	@ConfigItem(
			keyName = "useBlowpipe",
			name = "Blowpipe",
			description = "If disabled, will attempt to use Bolts",
			position = 58
	)
	default boolean useBlowpipe() { return false; }


	@ConfigItem(
			keyName = "lootNames",
			name = "Items to loot (separate with comma)",
			description = "Provide partial or full names of items you'd like to loot.",
			position = 65
	)
	default String lootNames() {
		return "visage,lump,limb,scroll,key,med,legs,shield,shield,ore,stone,rune,bar,wrath,bolts,grimy,coins";
	}

	@ConfigItem(
			keyName = "superantifire",
			name = "Ext Super Antifire",
			description =  "Enable to use Extended Super Antifire. Disable to use regular antifire.",
			position = 66
	)
	default boolean superantifire()
	{
		return true;
	}

	@ConfigItem(
			keyName = "antivenomplus",
			name = "Anti Venom+",
			description =  "Enable to use Anti-venom+. Disable to use Antidote++",
			position = 67
	)
	default boolean antivenomplus()
	{
		return true;
	}

	@ConfigItem(
			keyName = "antipoisonamount",
			name = "Antivenom Amount",
			description =  "Amount of (4) dose Antivenom+, or Antidote++ to take",
			position = 67
	)
	default int antipoisonamount() { return 1; }

	@ConfigItem(
			keyName = "usePOHpool",
			name = "Drink POH Pool",
			description =  "Enable to drink from POH pool to restore HP / Prayer.",
			position = 68
	)
	default boolean usePOHpool()
	{
		return true;
	}


	@ConfigItem(
			keyName = "praypotAmount",
			name = "Amount of Super Restores",
			description = "Amount of super restores to withdraw from the bank",
			position = 69
	)
	default int praypotAmount() { return 2; }

	@ConfigItem(
			keyName = "useRestores",
			name = "Use Super Restores",
			description = "Disable to use Prayer Potions",
			position = 70
	)
	default boolean useRestores() { return true; }

	@ConfigItem(
			keyName = "onlytelenofood",
			name = "Only Tele With No Food",
			description =  "Enable to only teleport out when you have 0 food and / or 0 restore pots. Disable to teleport out after every kill.",
			position = 71
	)
	default boolean onlytelenofood()
	{
		return false;
	}
	/*@ConfigItem(
			keyName = "foodtotele",
			name = "Food Teleport Threshold",
			description = "Minimum amount of food required. Running below this amount will force teleport.",
			position = 69
	)
	default int foodtotele() { return 2; }*/
	@ConfigItem(
			keyName = "foodAmount",
			name = "Amount of food 1",
			description = "Amount of food to withdraw",
			position = 79
	)
	default int foodAmount() { return 17; }

	@ConfigItem(
			keyName = "foodID",
			name = "ID of food 1",
			description = "ID of food to withdraw.",
			position = 80
	)
	default int foodID() { return 385; }

	@ConfigItem(
			keyName = "foodAmount2",
			name = "Amount of food 2",
			description = "Amount of food to withdraw",
			position = 81
	)
	default int foodAmount2() { return 4; }

	@ConfigItem(
			keyName = "foodID2",
			name = "ID of food 2",
			description = "ID of food to withdraw.",
			position = 82
	)
	default int foodID2() { return 3144; }

	@ConfigItem(
			keyName = "healthTP",
			name = "Min Health",
			description = "Minimum health to allow before teleporting (after running out of food)",
			position = 82
	)
	default int healthTP() { return 40; }

	@ConfigItem(
			keyName = "prayTP",
			name = "Min Pray",
			description = "Minimum prayer to allow before teleporting (after running out of potions)",
			position = 82
	)
	default int prayTP() { return 1; }

	@ConfigItem(
			keyName = "useSpec",
			name = "Use Spec Weapon",
			description = "Enable to use a special attack.",
			position = 83
	)
	default boolean useSpec() { return false; }

	@ConfigItem(
			keyName = "specWeapon",
			name = "Spec Weapon ID",
			description = "ID of special attack weapon",
			position = 84,
			hidden = true,
			unhide = "useSpec"
	)
	default int specWeapon() { return 0; }

	@ConfigItem(
			keyName = "normalWeapon",
			name = "Regular Weapon ID",
			description = "ID of regular weapon",
			position = 85,
			hidden = true,
			unhide = "useSpec"
	)
	default int normalWeapon() { return 0; }

	@ConfigItem(
			keyName = "normalOffhand",
			name = "Regular Offhand ID",
			description = "ID of regular offhand (0 for none)",
			position = 85,
			hidden = true,
			unhide = "useSpec"
	)
	default int normalOffhand() { return 0; }

	@ConfigItem(
			keyName = "specHP",
			name = "Spec HP",
			description = "Minimum health Vorkath must have before spec",
			position = 86,
			hidden = true,
			unhide = "useSpec"
	)
	default int specHP() { return 200; }

	@ConfigItem(
			keyName = "specThreshold",
			name = "Spec Energy",
			description = "Amount of special attack energy required to spec",
			position = 86,
			hidden = true,
			unhide = "useSpec"
	)
	default int specThreshold() { return 50; }


//	@ConfigItem(
//			keyName = "useDivines",
//			name = "Divine Pots",
//			description = "If enabled, uses Divine Ranging & Defence instead of Super",
//			position = 71
//	)
	default boolean useDivines() { return false; }

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 150
	)
	default Button startButton() {
		return new Button();
	}

}