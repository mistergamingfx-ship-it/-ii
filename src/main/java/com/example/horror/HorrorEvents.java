package com.example.horror;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class HorrorEvents {
    private static final SoundEvent[] SOUNDS = {
            SoundEvents.BLOCK_WOODEN_DOOR_OPEN,
            SoundEvents.BLOCK_CHEST_OPEN,
            SoundEvents.ENTITY_ENDERMAN_STARE,
            SoundEvents.ENTITY_GHAST_AMBIENT,
            SoundEvents.ENTITY_ZOMBIE_AMBIENT,
            SoundEvents.BLOCK_GRAVEL_STEP,
            SoundEvents.ENTITY_CREEPER_PRIMED
    };

    /** Called every tick for each world; rolls events every 5 seconds. */
    public static void tick(ServerWorld world) {
        if (world.getTime() % 100 != 0) return;
        Random r = world.getRandom();

        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.isCreative() || p.isSpectator()) continue;
            int roll = r.nextInt(100);

            if (roll < 25) {
                ambientSound(world, p, r);
            } else if (roll < 29) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100));
            } else if (roll < 32) {
                p.sendMessage(Text.translatable("multiplayer.player.joined", "Herobrine")
                        .formatted(Formatting.YELLOW));
            } else if (roll < 42 && world.isNight() && world.getRegistryKey() == World.OVERWORLD) {
                trySpawnWatcher(world, p, r);
            }
        }
    }

    private static void ambientSound(ServerWorld world, ServerPlayerEntity p, Random r) {
        double angle = r.nextDouble() * Math.PI * 2;
        double d = 6 + r.nextDouble() * 6;
        double x = p.getX() + Math.cos(angle) * d;
        double z = p.getZ() + Math.sin(angle) * d;
        SoundEvent s = SOUNDS[r.nextInt(SOUNDS.length)];
        world.playSound(null, x, p.getY(), z, s, SoundCategory.AMBIENT, 1.0f, 0.5f + r.nextFloat() * 0.5f);
    }

    private static void trySpawnWatcher(ServerWorld world, ServerPlayerEntity p, Random r) {
        boolean exists = !world.getEntitiesByType(HorrorMod.WATCHER,
                p.getBoundingBox().expand(80), e -> true).isEmpty();
        if (exists) return;

        double angle = r.nextDouble() * Math.PI * 2;
        double d = 24 + r.nextDouble() * 8;
        int x = (int) (p.getX() + Math.cos(angle) * d);
        int z = (int) (p.getZ() + Math.sin(angle) * d);
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

        WatcherEntity w = HorrorMod.WATCHER.create(world);
        if (w == null) return;
        w.refreshPositionAndAngles(x + 0.5, y, z + 0.5, r.nextFloat() * 360f, 0f);
        world.spawnEntity(w);
    }
}
