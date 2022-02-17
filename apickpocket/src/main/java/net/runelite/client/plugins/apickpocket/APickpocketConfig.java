package net.runelite.client.plugins.apickpocket;

import net.runelite.client.config.*;


@ConfigGroup("apickpocket")
public interface APickpocketConfig extends Config
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
		return 2;
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
		return 1;
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

	@ConfigItem(
			keyName = "menuAction",
			name = "Steal from",
			description = "Select whether to steal from an NPC or Game Object",
			position = 12
	)
	default APickpocketThiefType typethief()
	{
		return APickpocketThiefType.NPC;
	}

	@ConfigItem(
			keyName = "npcID",
			name = "NPC to Pickpocket",
			description = "Enter the ID of the npc to pickpocket.",
			position = 13
	)
	default int npcID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "menuAction",
			name = "Pickpocket Menu Action",
			description = "Select which menu action is used to pickpocket the selected NPC",
			position = 14
	)
	default APickpocketType type()
	{
		return APickpocketType.NPC_THIRD;
	}

	@ConfigItem(
			keyName = "returnLoc",
			name = "Location to Return",
			description = "Enter the Tile Location to walk back to after banking, in case the NPC is not visible from the bank.",
			position = 15,
			hidden = true,
			unhide = "bank"
	)
	default String returnLoc()
	{
		return "0,0,0";
	}

	@ConfigItem(
			keyName = "bank",
			name = "Use Bank",
			description = "Enable to bank to resupply if one is nearby.",
			position = 16,
			section = "pluginConfig"
	)
	default boolean bank() { return false; }

	@ConfigItem(
			keyName = "items",
			name = "Items to keep",
			description = "Item ID's to ignore when dropping.",
			position = 110,
			hidden = false,
			hide = "bank"
	)
	default String items() {
		return "";
	}

	@ConfigItem(
			keyName = "bankID",
			name = "Bank ID",
			description = "Enter the object ID of the bank to use.",
			position = 17,
			hidden = true,
			unhide = "bank"
	)
	default int bankID()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "bankmenuAction",
			name = "Bank Menu Action",
			description = "Select which menu action is used to open the bank",
			position = 18,
			hidden = true,
			unhide = "bank"
	)
	default APickpocketBankType type2()
	{
		return APickpocketBankType.SECOND;
	}

	@ConfigItem(
			keyName = "dodgynecks",
			name = "Use Dodgy Necklaces",
			description = "Enable to use dodgy necklaces.",
			position = 19,
			section = "pluginConfig"
	)
	default boolean dodgynecks() { return false; }

	@ConfigItem(
			keyName = "dodgyNecks",
			name = "Amount of Necklaces",
			description = "Amount of dodgy necklaces to withdraw.",
			position = 20,
			section = "pluginConfig",
			hidden = true,
			unhide = "bank"
	)
	default int dodgyNecks() { return 4; }

	@ConfigItem(
			keyName = "shadowVeil",
			name = "Use Shadow Veil",
			description = "Enable to use shadow veil spell.",
			position = 21,
			section = "pluginConfig"
	)
	default boolean shadowVeil() { return false; }

	@ConfigItem(
			keyName = "maxPouches",
			name = "Max Coin Pouches",
			description = "Amount of coin pouches before opening",
			position = 22,
			section = "pluginConfig"
	)
	default int maxPouches() { return 15; }


	@ConfigItem(
			keyName = "foodID",
			name = "ID of food",
			description = "ID of food",
			position = 23,
			section = "pluginConfig"
	)
	default int foodID() { return 1993; }

	@ConfigItem(
			keyName = "foodAmount",
			name = "Amount of food",
			description = "Amount of food to withdraw.",
			position = 24,
			section = "pluginConfig",
			hidden = true,
			unhide = "bank"
	)
	default int foodAmount() { return 14; }

	@ConfigItem(
			keyName = "minHealth",
			name = "Minimum Health",
			description = "Minimum health to allow before eating.",
			position = 25,
			section = "pluginConfig"
	)
	default int minHealth() { return 50; }

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 150
	)
	default Button startButton() {
		return new Button();
	}

	default boolean logParams()
	{
		return false;
	}
}