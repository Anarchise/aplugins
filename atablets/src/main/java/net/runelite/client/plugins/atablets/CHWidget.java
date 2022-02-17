package net.runelite.client.plugins.atablets;

public enum CHWidget {

    CRAFT_VARROCK_TABS(79, 23),
    CRAFT_CAMELOT_TABS(79, 24),
    CRAFT_FALADOR_TABS(79, 18),
    CRAFT_ARDOUGNE_TABS(79, 19),
    CRAFT_WATCHTOWER_TABS(79, 14),
    CRAFT_HOUSE_TABS(79, 15),
    CRAFT_LUMBRIDGE_TABS(79, 12),

    CRAFT_BTB_TABS(79, 20 ),
    CRAFT_BTP_TABS(79, 25),

    CRAFT_DRAGONSTONE_TABS(79, 21),
    CRAFT_RUBY_TABS(79, 22),
    CRAFT_EMERALD_TABS(79, 16),
    CRAFT_SAPPHIRE_TABS(79, 17),
    CRAFT_ONYX_TABS(79, 11),
    CRAFT_DIAMOND_TABS(79, 13),

   ;
    private final int groupId;
    private final int childId;

    CHWidget(int groupId, int childId) {
        this.groupId = groupId;
        this.childId = childId;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public int getChildId() {
        return this.childId;
    }
}
