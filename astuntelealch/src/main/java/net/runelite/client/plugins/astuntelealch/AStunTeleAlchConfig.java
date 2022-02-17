
package net.runelite.client.plugins.astuntelealch;

import net.runelite.client.config.*;

//@ConfigGroup("ElFiremaker")
@ConfigGroup("astuntelealch")
public interface AStunTeleAlchConfig extends Config
{

	@ConfigItem(
			keyName = "Type",
			name = "Type",
			description = "Stun alcher, or tele alcher",
			position = 0,
			title = "agilityTitle"
	)
	default AStunTeleAlchType Type() {
		return AStunTeleAlchType.STUN;
	}

	@ConfigItem(
			keyName = "stunType",
			name = "Type of stun",
			description = "Which stun to use",
			position = 2

	)
	default AStunTeleAlchStunType stunType() {
		return AStunTeleAlchStunType.CONFUSE;
	}

	@ConfigItem(
			keyName = "teleType",
			name = "Type of teleport",
			description = "",
			position = 3
	)
	default AStunTeleAlchTeleType teleType(){
		return AStunTeleAlchTeleType.VARROCK;
	}

	@ConfigItem(
			keyName = "alchID",
			name = "Item ID (High Alcher)",
			description = "Enter the Id of the item you want to Alch.",
			position = 10
	)
	default int alchID() { return 0; }

	@ConfigItem(
			keyName = "stunID",
			name = "NPC ID",
			description = "Enter the Id of the NPC you want to stun.",
			position = 14
	)
	default int stunID() { return 0; }

	@ConfigSection(
			keyName = "delayConfig",
			name = "Delay Configuration",
			description = "Configure how the bot handles sleep delays in milliseconds",
			position = 19
	)
	String delayConfig = "delayConfig";

	@ConfigItem(
			keyName = "sleepDelayMin",
			name = "Sleep Delay Minimum",
			description = "Sleep delay minimum.",
			position = 20,
			section = "delayConfig"

	)
	default int sleepDelayMin() { return 10; }

	@ConfigItem(
			keyName = "sleepDelayMax",
			name = "Sleep Delay Maximum",
			description = "Sleep delay maximum.",
			position = 21,
			section = "delayConfig"
	)
	default int sleepDelayMax() { return 550; }

	@ConfigItem(
			keyName = "sleepDelayDev",
			name = "Sleep Delay Deviation",
			description = "Sleep delay deviation.",
			position = 22,
			section = "delayConfig"
	)
	default int sleepDelayDev() { return 70; }

	@ConfigItem(
			keyName = "sleepDelayTarg",
			name = "Sleep Delay Target",
			description = "Sleep Tick delay target.",
			position =23,
			section = "delayConfig"
	)
	default int sleepDelayTarg() { return 100; }

	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Tick Delay Minimum",
			description = "Tick delay minimum.",
			position = 24,
			section = "delayConfig"

	)
	default int tickDelayMin() { return 1; }

	@ConfigItem(
			keyName = "tickDelayMax",
			name = "Tick Delay Maximum",
			description = "Tick delay maximum.",
			position = 25,
			section = "delayConfig"
	)
	default int tickDelayMax() { return 3; }

	@ConfigItem(
			keyName = "tickDelayDev",
			name = "Tick Delay Deviation",
			description = "Tick delay deviation.",
			position = 26,
			section = "delayConfig"
	)
	default int tickDelayDev() { return 1; }

	@ConfigItem(
			keyName = "tickDelayTarg",
			name = "Tick Delay Target",
			description = "Tick delay target.",
			position =27,
			section = "delayConfig"
	)
	default int tickDelayTarg() { return 1; }

	@ConfigItem(
			keyName = "enableUI",
			name = "Enable UI",
			description = "Enable to turn on in game UI",
			position = 140
	)
	default boolean enableUI()
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