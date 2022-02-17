package net.runelite.client.plugins.askiller;
import net.runelite.client.config.*;

@ConfigGroup("ASkiller")
public interface ASkillerConfiguration extends Config {

    @ConfigTitle(
            keyName = "delayConfig",
            name = "Delay Configuration",
            description = "Configure how the bot handles sleep delays in milliseconds",
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
    default int sleepMin() {
        return 60;
    }

    @Range(
            min = 0,
            max = 1550
    )
    @ConfigItem(
            keyName = "sleepMax",
            name = "Sleep Max",
            description = "",
            position = 4,
            section = "delayConfig"
    )
    default int sleepMax() {
        return 350;
    }

    @Range(
            min = 0,
            max = 1550
    )
    @ConfigItem(
            keyName = "sleepTarget",
            name = "Sleep Target",
            description = "",
            position = 5,
            section = "delayConfig"
    )
    default int sleepTarget() {
        return 100;
    }

    @Range(
            min = 0,
            max = 1550
    )
    @ConfigItem(
            keyName = "sleepDeviation",
            name = "Sleep Deviation",
            description = "",
            position = 6,
            section = "delayConfig"
    )
    default int sleepDeviation() {
        return 10;
    }

    @ConfigItem(
            keyName = "sleepWeightedDistribution",
            name = "Sleep Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 7,
            section = "delayConfig"
    )
    default boolean sleepWeightedDistribution() {
        return false;
    }

   /* @ConfigTitle(
            keyName = "delayTickConfig",
            name = "Game Tick Configuration",
            description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 8
    )*/
    String delayTickConfig = "delayTickConfig";

    @Range(
            min = 0,
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMin",
            name = "Game Tick Min",
            description = "",
            position = 9,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayMin() {
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
            position = 10,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayMax() {
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
            position = 11,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayTarget() {
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
            position = 12,
            section = "delayTickConfig",
            hidden = true
    )
    default int tickDelayDeviation() {
        return 1;
    }

    @ConfigItem(
            keyName = "tickDelayWeightedDistribution",
            name = "Game Tick Weighted Distribution",
            description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
            position = 13,
            section = "delayTickConfig",
            hidden = true
    )
    default boolean tickDelayWeightedDistribution() {
        return false;
    }

    @ConfigTitle(
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
            title = "instructionsTitle"
    )
    default String instructions() {
        return "Use developer tools to show Game Object and Npc ID's. " +
                "";
    }

    @ConfigTitle(
            keyName = "skillerTitle",
            name = "Skiller Configuration",
            description = "",
            position = 60
    )
    String skillerTitle = "delayConfig";

    @ConfigItem(
            keyName = "type",
            name = "Node type",
            description = "Type of node." +
                    "Rocks and Trees are Game Objects. Fishing spots are NPCs.",
            position = 70,
            title = "skillerTitle"
    )
    default ASkillerType type() {
        return ASkillerType.GAME_OBJECT;
    }

    @ConfigItem(
            keyName = "objectIds",
            name = "Object IDs",
            description = "Separate with comma",
            position = 80,
            title = "skillerTitle"
    )
    default String objectIds() {
        return "";
    }

    /*@ConfigTitle(
            keyName = "opcodeTitle",
            name = "Menu Opcodes",
            description = "",
            position = 85
    )
    String opcodeTitle = "opcodeTitle";
    */


    @ConfigItem(
            keyName = "combineItems",
            name = "Combine Inventory Items",
            description = "Enable to combine 2 items in inventory together" +
                    "Example cutting fish",
            position = 91,
            title = "opcodeTitle",
            hidden = true,
            unhide = "inventoryMenu"

    )
    default boolean combineItems() {
        return false;
    }

    @ConfigItem(
            keyName = "toolId",
            name = "Tool ID",
            description = "Inventory ID of the tool you want to use for combining, e.g. knife, tinderbox etc.",
            position = 92,
            hidden = true,
            unhide = "combineItems",
            title = "opcodeTitle"
    )
    default int toolId() {
        return 0;
    }

    @ConfigItem(
            keyName = "inventoryOpcodeValue",
            name = "Inventory Opcode Value",
            description = "Input custom Opcode value. If you are combining items this is the opcode when you click on the tool, if you are not combining items it is the opcode of the action you're performing, e.g. emptying jars",
            position = 93,
            hidden = true,
            unhide = "inventoryMenu",
            title = "opcodeTitle"
    )
    default int inventoryOpcodeValue() {
        return 0;
    }

    /*@ConfigTitle(
            keyName = "dropTitle",
            name = "Dropping & Banking",
            description = "",
            position = 89
    )*/
    String dropTitle = "dropTitle";

    @ConfigItem(
            keyName = "bankItems",
            name = "Bank items",
            description = "If a bank is near, will bank inventory instead of dropping.",
            position = 90,
            title = "dropTitle"
    )
    default boolean bankItems() {
        return false;
    }

    @ConfigItem(
            keyName = "items",
            name = "Items to keep",
            description = "Item ID's to ignore when dropping/banking.",
            position = 110
    )
    default String items() {
        return "";
    }

    @ConfigItem(
            keyName = "dropOne",
            name = "1 Tick",
            description = "Tick manipulation",
            position = 121,
            title = "dropTitle",
            hidden = true
    )
    default boolean dropOne() {
        return false;
    }

    @Range(
            min = 1,
            max = 60
    )


    @ConfigItem(
            keyName = "enableUI",
            name = "Enable UI",
            description = "Enable to turn on in game UI",
            position = 140,
            title = "skillerTitle"
    )
    default boolean enableUI() {
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
