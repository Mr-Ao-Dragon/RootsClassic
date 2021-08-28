package elucent.rootsclassic.ritual;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import elucent.rootsclassic.Roots;
import elucent.rootsclassic.block.brazier.BrazierTile;
import elucent.rootsclassic.registry.RootsRegistry;
import elucent.rootsclassic.util.RootsUtil;

public abstract class RitualBase {

	private static final int RADIUS = 4;
	private ArrayList<Block> blocks = new ArrayList<>();
	private ArrayList<BlockPos> positionsRelative = new ArrayList<>();
	private List<ItemStack> incenses = new ArrayList<>();
	private List<ItemStack> ingredients = new ArrayList<>();
	private Vector3d color = new Vector3d(255, 255, 255);
	private Vector3d secondaryColor = new Vector3d(255, 255, 255);
	private final ResourceLocation name;
	private int level;

	public RitualBase(ResourceLocation parName, int level, double r, double g, double b) {
		if (RitualManager.getRitualFromName(parName) != null) {
			throw new IllegalArgumentException("No duplicate names for rituals");
		}
		this.name = parName;
		setPrimaryColor(r, g, b);
		setSecondaryColor(r, g, b);
		setLevel(level);
	}

	public void setLevel(int level) {
		if (level < 0 || level > 2) {
			throw new IllegalArgumentException("Level must be 0, 1 or 2");
		}
		this.blocks = new ArrayList<>();
		this.level = level;
		//level 0 has no stones
		if (level == 1 || level == 2) {
			//the first circle of tier 1 stones
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), -3, 0, -3);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), -3, 0, 3);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), 3, 0, -3);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), 3, 0, 3);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), 3, 0, 0);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), -3, 0, 0);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), 0, 0, 3);
			this.addRitualPillar(RootsRegistry.MUNDANE_STANDING_STONE.get(), 0, 0, -3);
		} else if (level == 2) {
			//the outer tier 2 stones
			this.addRitualPillar(RootsRegistry.ATTUNED_STANDING_STONE.get(), 5, 1, 0);
			this.addRitualPillar(RootsRegistry.ATTUNED_STANDING_STONE.get(), -5, 1, 0);
			this.addRitualPillar(RootsRegistry.ATTUNED_STANDING_STONE.get(), 0, 1, 5);
			this.addRitualPillar(RootsRegistry.ATTUNED_STANDING_STONE.get(), 0, 1, -5);
		}
	}

	public RitualBase addRitualPillar(Block b, int x, int y, int z) {
		getBlocks().add(b);
		getPositionsRelative().add(new BlockPos(x, y, z));
		return this;
	}

	public RitualBase addIngredient(ItemStack i) {
		getIngredients().add(i);
		return this;
	}

	public RitualBase addIncense(ItemStack i) {
		getIncenses().add(i);
		return this;
	}

	public boolean doIngredientsMatch(RitualBase ritual) {
		return RootsUtil.itemListsMatch(this.getIngredients(), ritual.getIngredients());
	}

	public abstract void doEffect(World world, BlockPos pos, IInventory inventory, List<ItemStack> incenses);

	public boolean verifyPositionBlocks(World world, BlockPos pos) {
		if (getPositionsRelative().size() > 0) {
			for (int i = 0; i < getPositionsRelative().size(); i++) {
				BlockPos loopPos = getPositionsRelative().get(i);
				Block loopBlock = getBlocks().get(i);
				BlockPos loopPosOffset = pos.add(loopPos.getX(), loopPos.getY(), loopPos.getZ());
				if (world.getBlockState(loopPosOffset).getBlock() != loopBlock) {
					Roots.LOGGER.info(this.level + " level recipe has Missing block " + loopBlock + " at position " + loopPosOffset);
					return false;
				}
			}
		}
		return true;
	}

	public List<BrazierTile> getRecipeBraziers(World world, BlockPos pos) {
		List<BrazierTile> links = new ArrayList<>();
		TileEntity tileHere;
		for (int i = -1 * RADIUS; i <= RADIUS; i++) {
			for (int j = -1 * RADIUS; j <= RADIUS; j++) {
				if (world.getBlockState(pos.add(i, 0, j)).getBlock() == RootsRegistry.BRAZIER.get()) {
					tileHere = world.getTileEntity(pos.add(i, 0, j));
					if (tileHere instanceof BrazierTile) {
						links.add((BrazierTile) tileHere);
					}
				}
			}
		}
		return links;
	}

	public boolean incenseMatches(World world, BlockPos pos) {
		ArrayList<ItemStack> incenseFromNearby = new ArrayList<>();
		List<BrazierTile> braziers = getRecipeBraziers(world, pos);
		for (BrazierTile brazier : braziers) {
			if (!brazier.getHeldItem().isEmpty()) {
				//              Roots.logger.info("found brazier item " + brazier.getHeldItem());
				incenseFromNearby.add(brazier.getHeldItem());
			}
		}
		return RootsUtil.itemListsMatch(getIncenses(), incenseFromNearby);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("[A] ");
		for (ItemStack mat : this.getIngredients()) {
			s.append(mat.getDisplayName().getString()).append("; ");
		}
		s.append("[I] ");
		for (ItemStack mat : this.getIncenses()) {
			s.append(mat.getDisplayName().getString()).append("; ");
		}
		return s.toString();
	}

	public List<ItemStack> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<ItemStack> ingredients)
			throws IllegalArgumentException {
		if (ingredients.size() == 0 || ingredients.size() > 3) {
			throw new IllegalArgumentException("Invalid ritual ingredients, must be in range [1,3]");
		}
		this.ingredients = ingredients;
	}

	public List<ItemStack> getIncenses() {
		return incenses;
	}

	public void setIncenses(List<ItemStack> incenses)
			throws IllegalArgumentException {
		if (incenses.size() == 0 || incenses.size() > 4) {
			throw new IllegalArgumentException("Invalid ritual incense, must be in range [1,4]");
		}
		this.incenses = incenses;
	}

	public ArrayList<BlockPos> getPositionsRelative() {
		return positionsRelative;
	}

	public void setPositionsRelative(ArrayList<BlockPos> positionsRelative) {
		this.positionsRelative = positionsRelative;
	}

	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(ArrayList<Block> blocks) {
		this.blocks = blocks;
	}

	public Vector3d getColor() {
		return color;
	}

	public void setPrimaryColor(double r, double g, double b) {
		this.color = buildColor(r, g, b);
	}

	public Vector3d getSecondaryColor() {
		return secondaryColor;
	}

	public RitualBase setSecondaryColor(double r, double g, double b) {
		this.secondaryColor = buildColor(r, g, b);
		return this;
	}

	private Vector3d buildColor(double r, double g, double b) throws IllegalArgumentException {
		if (r < 0 || r > 255 ||
				g < 0 || g > 255 ||
				b < 0 || b > 255) {
			throw new IllegalArgumentException("Invalid color value use [0, 255]");
		}
		return new Vector3d(r, g, b);
	}

	public ResourceLocation getName() {
		return name;
	}
}
