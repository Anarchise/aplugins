package net.runelite.client.plugins.atablets;

import net.runelite.client.config.*;

@ConfigGroup("atablets")
public interface ATabletsConfig extends Config {
    @ConfigItem(keyName = "mode",
            name = "Object: ",
            description = "",
            position = 0
    )
    default CHMode mode() { return CHMode.CRAFT_VARROCK_TABS; }

   @ConfigItem(
            keyName = "lecturnId",
           name = "Lecturn Object ID",
           description = "",
            position = 1
    )
    default int lecturnId() { return 1; }

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
