package net.runelite.client.plugins.atablets;

import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.client.plugins.atablets.ATabletsConfig;

import javax.inject.Inject;
    public enum CHMode {

        // CRUDE_WOODEN_CHAIR("Crude Wooden Chair", ItemID.PLANK, ObjectID.CHAIR_6752, ObjectID.CHAIR_SPACE_4516, 2, CHWidget.CRUDE_WOODEN_CHAIR, ItemID.STEEL_NAILS),
        // WOODEN_CHAIR("Wooden Chair", ItemID.PLANK, ObjectID.CHAIR_6753, ObjectID.CHAIR_SPACE, 3, CHWidget.WOODEN_CHAIR, ItemID.STEEL_NAILS),
        CRAFT_VARROCK_TABS("Varrock", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_VARROCK_TABS),
        CRAFT_LUMBRIDGE_TABS("Lumbridge", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_LUMBRIDGE_TABS),
        CRAFT_HOUSE_TABS("House", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_HOUSE_TABS),
        CRAFT_FALADOR_TABS("Falador", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_FALADOR_TABS),
        CRAFT_CAMELOT_TABS("Camelot", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_CAMELOT_TABS),
        CRAFT_ARDOUGNE_TABS("Ardougne", ItemID.SOFT_CLAY, 13642, 13642,1, CHWidget.CRAFT_ARDOUGNE_TABS),
        CRAFT_WATCHTOWER_TABS("Watchtower", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_WATCHTOWER_TABS),

        CRAFT_BTP_TABS("Bones 2 Peaches", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_BTP_TABS),
        CRAFT_BTB_TABS("Bones 2 Bananas", ItemID.SOFT_CLAY, 13642, 13642,1, CHWidget.CRAFT_BTB_TABS),


        CRAFT_SAPPHIRE_TABS("Sapphire", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_SAPPHIRE_TABS),
        CRAFT_EMERALD_TABS("Emerald", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_EMERALD_TABS),
        CRAFT_RUBY_TABS("Ruby", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_RUBY_TABS),
        CRAFT_DIAMOND_TABS("Diamond", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_DIAMOND_TABS),
        CRAFT_DRAGONSTONE_TABS("Dragonstone", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_DRAGONSTONE_TABS),
        CRAFT_ONYX_TABS("Onyx", ItemID.SOFT_CLAY, 13642, 13642, 1, CHWidget.CRAFT_ONYX_TABS),


        ;


        private final String name;
        private final int plankId;
        private final int objectId;
        private final int objectSpaceId;
        private final int plankCost;
        private final CHWidget widget;
        private final int[] otherReqs;

        CHMode(String name, int plankId, int objectId, int objectSpaceId, int plankCost, CHWidget widget, int... otherReqs) {
            this.name = name;
            this.plankId = plankId;
            this.objectId = objectId;
            this.objectSpaceId = objectSpaceId;
            this.plankCost = plankCost;
            this.widget = widget;
            this.otherReqs = otherReqs;
        }

        public String getName() {
            return name;
        }

        public int getPlankId() {
            return plankId;
        }

        public int getObjectId() {
            return objectId;
        }

        public int getObjectSpaceId() {
            return objectSpaceId;
        }

        public int getPlankCost() {
            return plankCost;
        }

        public CHWidget getWidget() {
            return widget;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public int[] getOtherReqs() {
            return otherReqs;
        }

}