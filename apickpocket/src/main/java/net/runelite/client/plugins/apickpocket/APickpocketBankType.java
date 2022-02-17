/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */
package net.runelite.client.plugins.apickpocket;

import lombok.Getter;
import net.runelite.api.MenuAction;

import static net.runelite.api.MenuAction.*;

@Getter
public enum APickpocketBankType
{
   FIRST(GAME_OBJECT_FIRST_OPTION),
    SECOND(GAME_OBJECT_SECOND_OPTION),
    THIRD(GAME_OBJECT_THIRD_OPTION),
    FOURTH(GAME_OBJECT_FOURTH_OPTION),
    FIFTH(GAME_OBJECT_FIFTH_OPTION);

    MenuAction action;

    APickpocketBankType(MenuAction action)
    {
        this.action = action;
    }
}
