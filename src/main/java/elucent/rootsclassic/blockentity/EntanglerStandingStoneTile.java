package elucent.rootsclassic.blockentity;

import elucent.rootsclassic.client.particles.MagicAuraParticleData;
import elucent.rootsclassic.registry.RootsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EntanglerStandingStoneTile extends BEBase {
  private static final int RADIUS = 10;
  private int ticker = 0;

  public EntanglerStandingStoneTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
  }

  public EntanglerStandingStoneTile(BlockPos pos, BlockState state) {
    this(RootsRegistry.ENTANGLER_STANDING_STONE_TILE.get(), pos, state);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    super.save(tag);
    return tag;
  }

  public static void serverTick(Level level, BlockPos pos, BlockState state, EntanglerStandingStoneTile tile) {
    tile.ticker++;
    if (tile.ticker % 20 == 0) {
      List<LivingEntity> nearbyCreatures = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos.getX() - RADIUS, pos.getY() - RADIUS, pos.getZ() - RADIUS,
              pos.getX() + RADIUS, pos.getY() + RADIUS, pos.getZ() + RADIUS));
      if (nearbyCreatures.size() > 0) {
        for (LivingEntity nearbyCreature : nearbyCreatures) {
          nearbyCreature.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1));
        }
      }
      tile.ticker = 0;
    }
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, EntanglerStandingStoneTile tile) {
    tile.ticker++;
    if (tile.ticker % 5 == 0 && level.isClientSide) {
      for (double i = 0; i < 720; i += 45.0) {
        double xShift = 0.5 * Math.sin(Math.PI * (i / 360.0));
        double zShift = 0.5 * Math.cos(Math.PI * (i / 360.0));
        level.addParticle(MagicAuraParticleData.createData(32, 32, 255),
                pos.getX() + 0.5 + xShift, pos.getY() + 0.5, pos.getZ() + 0.5 + zShift, 0, 0, 0);
      }
    }
  }
}
