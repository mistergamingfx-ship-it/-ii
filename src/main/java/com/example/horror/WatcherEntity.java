package com.example.horror;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Stalker: moves toward you ONLY while you are not looking at it.
 * Look at it long enough or let it get close -> jumpscare and it vanishes.
 */
public class WatcherEntity extends HostileEntity {
    private int stareTicks = 0;
    private int lifeTicks = 0;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createWatcherAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.38)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        lifeTicks++;
        if (lifeTicks > 20 * 120) { // disappears after 2 minutes
            discard();
            return;
        }

        PlayerEntity player = getWorld().getClosestPlayer(getX(), getY(), getZ(), 64, true);
        if (player == null) return;

        double dist = distanceTo(player);

        if (isLookedAtBy(player)) {
            // frozen while watched
            getNavigation().stop();
            setVelocity(0, getVelocity().y, 0);
            velocityModified = true;
            stareTicks++;

            if (dist < 7 || stareTicks > 20 * 6) {
                jumpscare(player);
            }
        } else {
            stareTicks = Math.max(0, stareTicks - 1);
            getLookControl().lookAt(player, 30f, 30f);
            getNavigation().startMovingTo(player, 1.25);

            if (dist < 2.0 && age % 10 == 0) {
                player.damage(getDamageSources().mobAttack(this), 6.0f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200));
                getWorld().playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 1.0f, 0.6f);
            }
        }
    }

    private void jumpscare(PlayerEntity player) {
        getWorld().playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.HOSTILE, 1.5f, 0.5f);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 160));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
        discard();
    }

    private boolean isLookedAtBy(PlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d to = new Vec3d(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        if (to.lengthSquared() < 1.0E-4) return true;
        return look.dotProduct(to.normalize()) > 0.95 && player.canSee(this);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
