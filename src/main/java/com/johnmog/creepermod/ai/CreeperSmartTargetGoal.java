package com.johnmog.creepermod.ai;

import com.johnmog.creepermod.CreeperMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 * Custom Creeper AI goal that replaces vanilla targeting and explosion logic.
 *
 * <p>Priority order for targets (all within {@link #SEARCH_RADIUS} blocks):
 * <ol>
 *   <li>Beds (nearest, located via the world's POI manager)</li>
 *   <li>Chests / Trapped-Chests (nearest, scanned from loaded chunk BE maps)</li>
 *   <li>Nearest player</li>
 * </ol>
 *
 * <p>The Creeper navigates toward its target. Every
 * {@link #STUCK_CHECK_INTERVAL} ticks we compare the distance to the target
 * against the previous check. If it hasn't closed the gap by
 * {@link #STUCK_DISTANCE_THRESHOLD} blocks, or if it is already within
 * {@link #EXPLODE_RANGE} of the target, the Creeper detonates.
 */
public class CreeperSmartTargetGoal extends Goal {

    /** Horizontal (and vertical) search radius in blocks. */
    private static final int SEARCH_RADIUS = 200;

    /** Navigation speed multiplier. */
    private static final double MOVE_SPEED = 0.8;

    /**
     * How often (in ticks) we re-scan for a target and check for stuck state.
     * 20 ticks = 1 second.
     */
    private static final int STUCK_CHECK_INTERVAL = 20;

    /**
     * Number of consecutive stuck checks before the Creeper gives up trying
     * to get closer and detonates.
     */
    private static final int STUCK_THRESHOLD = 5;

    /**
     * Minimum distance improvement (in blocks) required between stuck checks
     * to be considered "not stuck".
     */
    private static final double STUCK_DISTANCE_THRESHOLD = 0.5;

    /**
     * If the Creeper is within this many blocks of the target it considers
     * itself close enough and detonates immediately.
     */
    private static final double EXPLODE_RANGE = 2.5;

    // -----------------------------------------------------------------------

    private final Creeper creeper;

    @Nullable
    private Vec3 targetVec;

    private int stuckCheckTimer = 0;
    private int stuckCount = 0;
    private double lastDistToTarget = Double.MAX_VALUE;

    public CreeperSmartTargetGoal(Creeper creeper) {
        this.creeper = creeper;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // -----------------------------------------------------------------------
    // Goal lifecycle
    // -----------------------------------------------------------------------

    @Override
    public boolean canUse() {
        return creeper.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return creeper.isAlive();
    }

    @Override
    public void start() {
        stuckCheckTimer = 0;
        stuckCount = 0;
        lastDistToTarget = Double.MAX_VALUE;
        targetVec = findTarget();
    }

    @Override
    public void tick() {
        Level level = creeper.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }

        // Refresh target every 2 seconds
        if (creeper.tickCount % 40 == 0) {
            targetVec = findTarget();
        }

        if (targetVec == null) {
            targetVec = findTarget();
        }

        if (targetVec != null) {
            creeper.getLookControl().setLookAt(
                    targetVec.x, targetVec.y, targetVec.z, 30f, 30f);
            creeper.getNavigation().moveTo(
                    targetVec.x, targetVec.y, targetVec.z, MOVE_SPEED);
        }

        // Periodically check whether the Creeper is making progress
        stuckCheckTimer++;
        if (stuckCheckTimer >= STUCK_CHECK_INTERVAL) {
            stuckCheckTimer = 0;
            checkStuckAndMaybeExplode();
        }
    }

    @Override
    public void stop() {
        creeper.getNavigation().stop();
        targetVec = null;
    }

    // -----------------------------------------------------------------------
    // Target finding
    // -----------------------------------------------------------------------

    @Nullable
    private Vec3 findTarget() {
        Level level = creeper.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        BlockPos origin = creeper.blockPosition();

        // Priority 1 – beds (use the POI manager for efficiency)
        BlockPos bed = findNearestBed(serverLevel, origin);
        if (bed != null) {
            return Vec3.atCenterOf(bed);
        }

        // Priority 2 – chests (scan loaded chunk block-entity maps)
        BlockPos chest = findNearestChest(serverLevel, origin);
        if (chest != null) {
            return Vec3.atCenterOf(chest);
        }

        // Priority 3 – nearest player
        Player player = serverLevel.getNearestPlayer(creeper, SEARCH_RADIUS);
        if (player != null) {
            return player.position();
        }

        return null;
    }

    /**
     * Uses the world's POI (Point of Interest) manager to efficiently find
     * the nearest bed within {@link #SEARCH_RADIUS}.  Beds are registered as
     * {@code PoiTypes.HOME} POIs by Minecraft, so no block scanning is needed.
     */
    @Nullable
    private BlockPos findNearestBed(ServerLevel level, BlockPos origin) {
        Optional<BlockPos> result = level.getPoiManager().findClosest(
                holder -> holder.is(PoiTypes.HOME),
                pos -> pos.distSqr(origin) <= (double) SEARCH_RADIUS * SEARCH_RADIUS,
                origin,
                SEARCH_RADIUS,
                PoiManager.Occupancy.ANY);
        return result.orElse(null);
    }

    /**
     * Scans loaded chunks for the nearest chest (regular or trapped) within
     * {@link #SEARCH_RADIUS}, using each chunk's block-entity map for
     * efficiency — no block-by-block iteration required.
     */
    @Nullable
    private BlockPos findNearestChest(ServerLevel level, BlockPos origin) {
        double nearestDistSq = (double) SEARCH_RADIUS * SEARCH_RADIUS;
        BlockPos nearest = null;

        int chunkRadius = (SEARCH_RADIUS >> 4) + 1;
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                LevelChunk chunk = (LevelChunk) level.getChunk(
                        originChunkX + dx, originChunkZ + dz,
                        ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry
                        : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    if (be instanceof ChestBlockEntity
                            || be instanceof TrappedChestBlockEntity) {
                        BlockPos bePos = entry.getKey();
                        double distSq = bePos.distSqr(origin);
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearest = bePos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    // -----------------------------------------------------------------------
    // Stuck detection and explosion
    // -----------------------------------------------------------------------

    private void checkStuckAndMaybeExplode() {
        if (targetVec == null) {
            return;
        }

        double currentDist = creeper.position().distanceTo(targetVec);

        if (currentDist <= EXPLODE_RANGE) {
            triggerExplosion();
            return;
        }

        double improvement = lastDistToTarget - currentDist;
        if (improvement < STUCK_DISTANCE_THRESHOLD) {
            stuckCount++;
            CreeperMod.LOGGER.debug("Creeper stuck count {}/{} near ({})",
                    stuckCount, STUCK_THRESHOLD, creeper.blockPosition());
            if (stuckCount >= STUCK_THRESHOLD) {
                triggerExplosion();
                return;
            }
        } else {
            stuckCount = 0;
        }

        lastDistToTarget = currentDist;
    }

    /**
     * Creates a Creeper-style explosion at the Creeper's current position
     * (power 3 for a normal Creeper, power 6 for a charged one) and removes
     * the Creeper from the world.
     */
    private void triggerExplosion() {
        if (!creeper.isAlive()) {
            return;
        }
        Level level = creeper.level();
        float power = creeper.isPowered() ? 6.0F : 3.0F;
        CreeperMod.LOGGER.debug("Creeper detonating at ({}) power={}",
                creeper.blockPosition(), power);
        level.explode(creeper,
                creeper.getX(), creeper.getY(), creeper.getZ(),
                power, Level.ExplosionInteraction.MOB);
        creeper.discard();
    }
}
