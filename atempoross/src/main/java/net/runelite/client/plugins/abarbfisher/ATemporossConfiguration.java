
package net.runelite.client.plugins.abarbfisher;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("atempoross")
public interface ATemporossConfiguration extends Config
{

	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 2
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
		position = 3,
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
		position = 4,
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
		position = 5,
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
		position = 6,
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
		position = 7,
		section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}

	default int tickDelayMin()
	{
		return 2;
	}
	default int tickDelayMax()
	{
		return 4;
	}
	default int tickDelayTarget()
	{
		return 3;
	}
	default int tickDelayDeviation() {return 1;}
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigSection(
		keyName = "instructionsTitle",
		name = "Instructions",
		description = "",
		position = 16
	)
	String instructionsTitle = "instructionsTitle";

	@ConfigItem(
		keyName = "instructions",
		name = "",
		description = "Instructions. Don't enter anything into this field",
		position = 20,
		section = "instructionsTitle"
	)
	default String instructions()
	{
		return "Supports all methods.";
	}

	@ConfigSection(
		keyName = "barbarianfisherTitle",
		name = "Tempoross Config",
		description = "",
		position = 60
	)
	String barbarianfisherTitle = "barbarianfisherTitle";

	@ConfigItem(
		keyName = "enableUI",
		name = "Enable UI",
		description = "Enable to turn on in game UI",
		position = 140,
		section = "barbarianfisherTitle"
	)
	default boolean enableUI()
	{
		return true;
	}



	@ConfigItem(
			keyName = "catch7cook7",
			name = "Catch 7 fish, cook 7 fish",
			description = "If disabled, will catch & cook 16 instead",
			position = 138,
			section = "barbarianfisherTitle"
	)
	default boolean catch7cook7()
	{
		return true;
	}


	@ConfigItem(
			keyName = "OnlyDepositCooked",
			name = "Cook fish?",
			description = "If disabled, will deposit raw fish (maximises xp) ",
			position = 137,
			section = "barbarianfisherTitle"
	)
	default boolean OnlyDepositCooked()
	{
		return true;
	}

	@ConfigItem(
			keyName = "useBuckets",
			name = "Douse fires?",
			description = "Will grab and fill buckets as needed ",
			position = 139,
			section = "barbarianfisherTitle"
	)
	default boolean useBuckets()
	{
		return true;
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
