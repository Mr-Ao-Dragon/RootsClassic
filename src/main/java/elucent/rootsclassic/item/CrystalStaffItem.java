package elucent.rootsclassic.item;

import elucent.rootsclassic.Const;
import elucent.rootsclassic.capability.IManaCapability;
import elucent.rootsclassic.capability.RootsCapabilityManager;
import elucent.rootsclassic.client.particles.MagicLineParticleData;
import elucent.rootsclassic.client.particles.MagicParticleData;
import elucent.rootsclassic.component.ComponentBase;
import elucent.rootsclassic.component.ComponentManager;
import elucent.rootsclassic.component.EnumCastType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CrystalStaffItem extends Item implements IManaRelatedItem {

	public CrystalStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity caster, int timeLeft) {
		if (timeLeft < (72000 - 12) && stack.hasTag()) {
			//BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
			PlayerEntity player = (PlayerEntity) caster;
			ResourceLocation compName = ResourceLocation.tryCreate(CrystalStaffItem.getEffect(stack));
			if(compName != null) {
				ComponentBase comp = ComponentManager.getComponentFromName(compName);
				if (comp == null || !caster.getCapability(RootsCapabilityManager.MANA_CAPABILITY).isPresent()) {
					return;
				}
				int potency = getPotency(stack) + 1;
				int efficiency = CrystalStaffItem.getEfficiency(stack);
				int size = CrystalStaffItem.getSize(stack);
				if (player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof SylvanArmorItem
						&& player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof SylvanArmorItem
						&& player.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof SylvanArmorItem
						&& player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof SylvanArmorItem) {
					potency += 1;
				}
				//        double xpCost = (comp.getManaCost() + potency) * (1.0 - 0.25 * efficiency);
				IManaCapability manaCap = player.getCapability(RootsCapabilityManager.MANA_CAPABILITY).orElse(null);
				if (manaCap.getMana() >= comp.getManaCost() / (efficiency + 1)) {
					//pay mana cost
					manaCap.setMana(manaCap.getMana() - ((comp.getManaCost()) / (efficiency + 1)));
					comp.doEffect(world, caster, EnumCastType.SPELL, caster.getPosX() + 3.0 * caster.getLookVec().x, caster.getPosY() + 3.0 * caster.getLookVec().y,
							caster.getPosZ() + 3.0 * caster.getLookVec().z, potency, efficiency, 3.0 + 2.0 * size);
					for (int i = 0; i < 90; i++) {
						double offX = random.nextFloat() * 0.5 - 0.25;
						double offY = random.nextFloat() * 0.5 - 0.25;
						double offZ = random.nextFloat() * 0.5 - 0.25;
						double coeff = (offX + offY + offZ) / 1.5 + 0.5;
						double dx = (caster.getLookVec().x + offX) * coeff;
						double dy = (caster.getLookVec().y + offY) * coeff;
						double dz = (caster.getLookVec().z + offZ) * coeff;
						if (world.rand.nextBoolean()) {
							world.addParticle(MagicParticleData.createData(comp.primaryColor.x, comp.primaryColor.y, comp.primaryColor.z),
									caster.getPosX() + dx, caster.getPosY() + 1.5 + dy, caster.getPosZ() + dz, dx, dy, dz);
						} else {
							world.addParticle(MagicParticleData.createData(comp.secondaryColor.x, comp.secondaryColor.y, comp.secondaryColor.z),
									caster.getPosX() + dx, caster.getPosY() + 1.5 + dy, caster.getPosZ() + dz, dx, dy, dz);
						}
					}
				}
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			if(!player.isSneaking()) {
				if (world.isRemote && Minecraft.getInstance().currentScreen != null) {
					return new ActionResult<>(ActionResultType.FAIL, stack);
				} else {
					player.setActiveHand(hand);
					return new ActionResult<>(ActionResultType.PASS, stack);
				}
			} else {
				tag.putInt(Const.NBT_SELECTED, tag.getInt(Const.NBT_SELECTED) + 1);
				if (tag.getInt(Const.NBT_SELECTED) > 4) {
					tag.putInt(Const.NBT_SELECTED, 1);
				}
				stack.setTag(tag);
				return new ActionResult<>(ActionResultType.FAIL, stack);
			}
		}
		return new ActionResult<>(ActionResultType.FAIL, stack);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldS, ItemStack newS, boolean slotChanged) {
		if (oldS.hasTag() && newS.hasTag()) {
			if (!CrystalStaffItem.getEffect(oldS).equals(CrystalStaffItem.getEffect(newS)) ||
					oldS.getTag().getInt(Const.NBT_SELECTED) != newS.getTag().getInt(Const.NBT_SELECTED) || slotChanged) {
				return true;
			}
		}
		return slotChanged;
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			String effect = CrystalStaffItem.getEffect(stack);
			if(effect != null) {
				ResourceLocation componentName = ResourceLocation.tryCreate(effect);
				if(componentName != null) {
					ComponentBase comp = ComponentManager.getComponentFromName(componentName);
					if (comp != null) {
						int potency = tag.getInt(Const.NBT_POTENCY);
						int efficiency = tag.getInt(Const.NBT_EFFICIENCY);
						int size = tag.getInt(Const.NBT_SIZE);
						comp.castingAction((PlayerEntity) player, count, potency, efficiency, size);
						if (random.nextBoolean()) {
							player.getEntityWorld().addParticle(MagicLineParticleData.createData(comp.primaryColor.x, comp.primaryColor.y, comp.primaryColor.z),
									player.getPosX() + 2.0 * (random.nextFloat() - 0.5), player.getPosY() + 2.0 * (random.nextFloat() - 0.5) + 1.0, player.getPosZ() + 2.0 * (random.nextFloat() - 0.5),
									player.getPosX(), player.getPosY() + 1.0, player.getPosZ());
						} else {
							player.getEntityWorld().addParticle(MagicLineParticleData.createData(comp.secondaryColor.x, comp.secondaryColor.y, comp.secondaryColor.z),
									player.getPosX() + 2.0 * (random.nextFloat() - 0.5), player.getPosY() + 2.0 * (random.nextFloat() - 0.5) + 1.0, player.getPosZ() + 2.0 * (random.nextFloat() - 0.5),
									player.getPosX(), player.getPosY() + 1.0, player.getPosZ());
						}
					}
				}
			}
		}
	}

	public static void createData(ItemStack stack) {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(Const.NBT_SELECTED, 1);
		tag.putInt(Const.NBT_POTENCY + "1", 0);
		tag.putInt(Const.NBT_POTENCY + "2", 0);
		tag.putInt(Const.NBT_POTENCY + "3", 0);
		tag.putInt(Const.NBT_POTENCY + "4", 0);
		tag.putInt(Const.NBT_EFFICIENCY + "1", 0);
		tag.putInt(Const.NBT_EFFICIENCY + "2", 0);
		tag.putInt(Const.NBT_EFFICIENCY + "3", 0);
		tag.putInt(Const.NBT_EFFICIENCY + "4", 0);
		tag.putInt(Const.NBT_SIZE + "1", 0);
		tag.putInt(Const.NBT_SIZE + "2", 0);
		tag.putInt(Const.NBT_SIZE + "3", 0);
		tag.putInt(Const.NBT_SIZE + "4", 0);
		tag.putString(Const.NBT_EFFECT + "1", "");
		tag.putString(Const.NBT_EFFECT + "2", "");
		tag.putString(Const.NBT_EFFECT + "3", "");
		tag.putString(Const.NBT_EFFECT + "4", "");
		stack.setTag(tag);
	}

	public static void addEffect(ItemStack stack, int slot, String effect, int potency, int efficiency, int size) {
		CompoundNBT tag = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		tag.putString(Const.NBT_EFFECT + slot, effect);
		tag.putInt(Const.NBT_POTENCY + slot, potency);
		tag.putInt(Const.NBT_EFFICIENCY + slot, efficiency);
		tag.putInt(Const.NBT_SIZE + slot, size);
		stack.setTag(tag);
	}

	public static Integer getPotency(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getInt(Const.NBT_POTENCY + tag.getInt(Const.NBT_SELECTED));
		}
		return 0;
	}

	public static Integer getEfficiency(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getInt(Const.NBT_EFFICIENCY + tag.getInt(Const.NBT_SELECTED));
		}
		return 0;
	}

	public static Integer getSize(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getInt(Const.NBT_SIZE + tag.getInt(Const.NBT_SELECTED));
		}
		return 0;
	}

	public static String getEffect(ItemStack stack) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getString(Const.NBT_EFFECT + tag.getInt(Const.NBT_SELECTED));
		}
		return null;
	}

	//Unused?
	public static String getEffect(ItemStack stack, int slot) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return tag.getString(Const.NBT_EFFECT + slot);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			String effect = CrystalStaffItem.getEffect(stack);
			if(effect != null) {
				ResourceLocation compName = ResourceLocation.tryCreate(effect);
				if (compName != null) {
					ComponentBase comp = ComponentManager.getComponentFromName(compName);
					if (comp != null) {
						tooltip.add(new TranslationTextComponent("rootsclassic.tooltip.spelltypeheading")
								.appendString(": ").mergeStyle(TextFormatting.GOLD).appendSibling(comp.getEffectName().mergeStyle(comp.getTextColor())));
					}
				}
			} else {
				//TODO: let people know it's an invalid effect
			}

			tooltip.add(new StringTextComponent("  +" + CrystalStaffItem.getPotency(stack) + " ")
					.appendSibling(new TranslationTextComponent("rootsclassic.tooltip.spellpotency")).appendString(".").mergeStyle(TextFormatting.RED));
			tooltip.add(new StringTextComponent("  +" + CrystalStaffItem.getEfficiency(stack) + " ")
					.appendSibling(new TranslationTextComponent("rootsclassic.tooltip.spellefficiency")).appendString(".").mergeStyle(TextFormatting.RED));
			tooltip.add(new StringTextComponent("  +" + CrystalStaffItem.getSize(stack) + " ")
					.appendSibling(new TranslationTextComponent("rootsclassic.tooltip.spellsize")).appendString(".").mergeStyle(TextFormatting.RED));
		} else {
			tooltip.add(new TranslationTextComponent("rootsclassic.error.unset").mergeStyle(TextFormatting.GRAY));
		}
	}
}