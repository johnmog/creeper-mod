package com.johnmog.creepermod.events;

import com.johnmog.creepermod.CreeperMod;
import com.johnmog.creepermod.ai.CreeperSmartTargetGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handles all Creeper-related events:
 * <ol>
 *   <li>Replaces every hostile mob spawn with a Creeper (Endermen and Blazes are exempt).</li>
 *   <li>Charges Creepers that spawn during a thunderstorm.</li>
 *   <li>Injects the smart-targeting AI goal into every Creeper.</li>
 * </ol>
 */
public class CreeperEventHandler {

    /**
     * Feature 1 &amp; 2: Replace hostile mob spawns with Creepers and charge
     * them when a thunderstorm is active.
     *
     * <p>Fires during the natural spawn cycle just before the entity is
     * finalised. Cancelling the spawn prevents the original mob from appearing
     * and we immediately add a Creeper in its place.
     */
    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        ServerLevelAccessor levelAccessor = event.getLevel();
        ServerLevel serverLevel = levelAccessor.getLevel();

        if (mob instanceof Monster
                && !(mob instanceof Creeper)
                && !(mob instanceof EnderMan)
                && !(mob instanceof Blaze)) {
            // Cancel the original hostile mob spawn
            event.setSpawnCancelled(true);

            // Spawn a Creeper at the same position
            Creeper creeper = EntityType.CREEPER.create(serverLevel);
            if (creeper != null) {
                creeper.moveTo(event.getX(), event.getY(), event.getZ(),
                        mob.getYRot(), mob.getXRot());
                applyThunderstormCharge(creeper, serverLevel);
                serverLevel.addFreshEntityWithPassengers(creeper);
                CreeperMod.LOGGER.debug(
                        "Replaced {} spawn with Creeper at ({}, {}, {})",
                        mob.getType().getDescriptionId(),
                        (int) event.getX(), (int) event.getY(), (int) event.getZ());
            }
        } else if (mob instanceof Creeper creeper) {
            // Feature 2: charge naturally-spawned Creepers during a thunderstorm
            applyThunderstormCharge(creeper, serverLevel);
        }
    }

    /**
     * Feature 3: Injects the smart-targeting AI goal into Creepers as they
     * join the world so they hunt beds, chests, and players in priority order.
     *
     * <p>We also remove the vanilla {@code SwellGoal} and
     * {@code NearestAttackableTargetGoal} so the smart goal is the sole
     * driver of targeting and detonation logic.
     */
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Creeper creeper) {
            // Remove goals that conflict with our custom behaviour
            creeper.goalSelector.removeAllGoals(
                    g -> g instanceof SwellGoal || g instanceof MeleeAttackGoal);
            creeper.targetSelector.removeAllGoals(
                    g -> g instanceof NearestAttackableTargetGoal<?>);

            // Add the smart targeting + detonation goal at high priority (2)
            creeper.goalSelector.addGoal(2, new CreeperSmartTargetGoal(creeper));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static void applyThunderstormCharge(Creeper creeper, ServerLevel level) {
        if (level.isThundering()) {
            creeper.setPowered(true);
            CreeperMod.LOGGER.debug("Creeper charged due to thunderstorm at ({})",
                    creeper.blockPosition());
        }
    }
}
