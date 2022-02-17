package net.runelite.client.plugins.azulrah;

import net.runelite.client.config.*;
import net.runelite.client.config.Button;

import java.awt.*;
import java.awt.event.KeyEvent;


@ConfigGroup("azulrah")
public interface AZulrahConfig extends Config
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


	public static enum SnakelingSettings
	{
		OFF("Off"),
		MES("Remove Att. Op."),
		ENTITY("Entity Hider");

		private final String name;

		public String toString()
		{
			return this.name;
		}

		private SnakelingSettings(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return this.name;
		}
	}

	@ConfigItem(
			keyName = "fairyRings",
			name = "Use Fairy Rings",
			description = "Enable to use Fairy Ring (last location) instead of zul-andra scrolls",
			position = 48
	)
	default boolean fairyRings() { return true; }

	@ConfigItem(
			keyName = "usePOHPool",
			name = "POH Pool",
			description = "Enable to use POH Pool",
			position = 50
	)
	default boolean usePOHPool() { return true; }

	@ConfigItem(
			keyName = "mageID1",
			name = "ID 1 (range gear)",
			description = "To withdraw from bank",
			position = 54,
			hidden = false,
			hide = "RangedOnly"

	)
	default int mageID1() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID2",
			name = "ID 2 (range gear)",
			description = "To withdraw from bank",
			position = 55,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID2() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID3",
			name = "ID 3 (range gear)",
			description = "To withdraw from bank",
			position = 56,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID3() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID4",
			name = "ID 4 (range gear)",
			description = "To withdraw from bank",
			position = 57,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID4() {
		return 0;
	}

	@ConfigItem(
			keyName = "mageID5",
			name = "ID 5 (range gear)",
			description = "To withdraw from bank",
			position = 58,
			hidden = false,
			hide = "RangedOnly"

	)
	default int mageID5() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID6",
			name = "ID 6 (range gear)",
			description = "To withdraw from bank",
			position = 59,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID6() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID7",
			name = "ID 7 (range gear)",
			description = "To withdraw from bank",
			position = 60,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID7() {
		return 0;
	}
	@ConfigItem(
			keyName = "mageID8",
			name = "ID 8 (range gear)",
			description = "To withdraw from bank",
			position = 61,
			hidden = false,
			hide = "RangedOnly"
	)
	default int mageID8() {
		return 0;
	}



	@ConfigItem(
			keyName = "praypotAmount",
			name = "Amount of Super Restores",
			description = "Amount of super restores to withdraw from the bank",
			position = 63
	)
	default int praypotAmount() { return 2; }

	@ConfigItem(
			keyName = "useRestores",
			name = "Use Super Restores",
			description = "Disable to use Prayer Potions",
			position = 64
	)
	default boolean useRestores() { return true; }


	@ConfigItem(
			keyName = "supers",
			name = "Super Potions",
			description = "Enable for Divine Bastion & Divine Magic Potions, Disable for Range & Magic Potions",
			position = 66
	)
	default boolean supers() { return true; }

	@ConfigItem(
			keyName = "antivenomplus",
			name = "Use Antivenom+",
			description = "Disable to use antidote++",
			position = 68
	)
	default boolean antivenomplus() { return true; }

	@ConfigItem(
			keyName = "superantipoison",
			name = "Use Superantipoison",
			description = "Enable this to override and use superantipoison",
			position = 70
	)
	default boolean superantipoison() { return true; }

	@ConfigItem(
			keyName = "serphelm",
			name = "Use Serpentine Helm",
			description = "Enable to skip all antipoison potions",
			position = 72
	)
	default boolean serphelm() { return true; }


	@ConfigItem(
			keyName = "foodAmount",
			name = "Amount of food 1",
			description = "Amount of food to withdraw",
			position = 74
	)
	default int foodAmount() { return 17; }

	@ConfigItem(
			keyName = "foodID",
			name = "ID of food 1",
			description = "ID of food to withdraw.",
			position = 76
	)
	default int foodID() { return 385; }

	@ConfigItem(
			keyName = "foodAmount2",
			name = "Amount of food 2",
			description = "Amount of food to withdraw",
			position = 78
	)
	default int foodAmount2() { return 4; }

	@ConfigItem(
			keyName = "foodID2",
			name = "ID of food 2",
			description = "ID of food to withdraw.",
			position = 80
	)
	default int foodID2() { return 3144; }

	@ConfigItem(
			keyName = "RangedOnly",
			name = "Ranged Only Mode",
			description = "Enable if you are using Ranged only",
			position = 82
	)
	default boolean RangedOnly() { return false; }

	@ConfigItem(
			keyName = "MageOnly",
			name = "Magic Only Mode",
			description = "Enable if you are using Magic only",
			position = 82
	)
	default boolean MageOnly() { return false;}


	@ConfigItem(
			keyName = "nomagepots",
			name = "Ignore Magic Potions",
			description = "Enable this to override and ignore all magic potions",
			position = 84
	)
	default boolean nomagepots() { return false; }

	@ConfigItem(
			keyName = "imbuedheart",
			name = "Use Imbued Heart",
			description = "Enable to use imbued heart instead of magic/divine magic pots",
			position = 86,
			hidden = false,
			hide = "RangedOnly"
	)
	default boolean imbuedheart() { return false; }

	@ConfigItem(
			keyName = "Rigour",
			name = "Use Rigour",
			description = "Enable to use Rigour instead of Eagle Eye",
			position = 88
	)
	default boolean Rigour() { return false; }

	@ConfigItem(
			keyName = "Augury",
			name = "Use Augury",
			description = "Enable to use Augury instead of Mystic Might",
			position = 90
	)
	default boolean Augury() { return false; }


	@ConfigItem(
			keyName = "lootNames",
			name = "Items to loot (separate with comma)",
			description = "Provide partial or full names of items you'd like to loot.",
			position = 99
	)
	default String lootNames() {
		return "mutagen,fang,visage,onyx,jar,scale,uncut,dragon,zul,manta,battlestaff,grimy,snapdragon,dwarf,torstol,toadflax,magic,spirit,key,shield,runite,antidote,adamant,yew,rune,coconut";
	}

	public static enum DisplayMode
	{
		CURRENT("Current"),
		NEXT("Next"),
		BOTH("Both");

		private final String name;

		public String toString()
		{
			return this.name;
		}

		private DisplayMode(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return this.name;
		}
	}

	public static enum DisplayType
	{
		OFF("Off"),
		OVERLAY("Overlay"),
		TILE("Tile"),
		BOTH("Both");

		private final String name;

		public String toString()
		{
			return this.name;
		}

		private DisplayType(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return this.name;
		}
	}

	default boolean logParams()
	{
		return false;
	}

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