package net.mirai.dynamicportals.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;

public class TesterWoodenSwordItem extends SwordItem {
    public TesterWoodenSwordItem(Properties properties) {
        super(net.minecraft.world.item.Tiers.WOOD, properties.attributes(SwordItem.createAttributes(net.minecraft.world.item.Tiers.WOOD, 3, -2.4F)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!target.level().isClientSide) {
            if (target.isAlive()) {
                target.invulnerableTime = 0;
                DamageSource source = attacker instanceof Player player
                    ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);
                target.hurt(source, Float.MAX_VALUE);
            }
        }
        return result;
    }
}
