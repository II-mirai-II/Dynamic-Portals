package net.mirai.dynamicportals.item;

import net.mirai.dynamicportals.DynamicPortals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TesterWoodenSwordItem extends SwordItem {
    private static final int TEST_ATTACK_DAMAGE_BONUS = 2048;
    private static final float VANILLA_WOODEN_SWORD_ATTACK_SPEED = -2.4F;
    private static final ResourceLocation TEST_SWEEPING_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(
        DynamicPortals.MOD_ID,
        "tester_wooden_sword.sweeping_damage"
    );

    public TesterWoodenSwordItem(Properties properties) {
        super(Tiers.WOOD, properties.attributes(createTesterAttributes()));
    }

    private static ItemAttributeModifiers createTesterAttributes() {
        return SwordItem.createAttributes(Tiers.WOOD, TEST_ATTACK_DAMAGE_BONUS, VANILLA_WOODEN_SWORD_ATTACK_SPEED)
            .withModifierAdded(
                Attributes.SWEEPING_DAMAGE_RATIO,
                new AttributeModifier(TEST_SWEEPING_DAMAGE_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            );
    }
}
