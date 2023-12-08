package net.coderbot.iris.block_rendering;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockRenderingSettings {
	public static final BlockRenderingSettings INSTANCE = new BlockRenderingSettings();

	private boolean reloadRequired;
	private Object2IntMap<BlockState> blockStateIds;
	private Map<Block, ChunkRenderTypeSet> blockTypeIds;
	private Object2IntFunction<NamespacedId> entityIds;
	private Object2IntFunction<NamespacedId> itemIds;
	private float ambientOcclusionLevel;
	private boolean disableDirectionalShading;
	private boolean hasVillagerConversionId;
	private boolean useSeparateAo;
	private boolean useExtendedVertexFormat;
	private boolean separateEntityDraws;
	private boolean voxelizeLightBlocks;

	public BlockRenderingSettings() {
		reloadRequired = false;
		blockStateIds = null;
		blockTypeIds = null;
		ambientOcclusionLevel = 1.0F;
		disableDirectionalShading = false;
		useSeparateAo = false;
		useExtendedVertexFormat = false;
		separateEntityDraws = false;
		voxelizeLightBlocks = false;
		hasVillagerConversionId = false;
	}

	public boolean isReloadRequired() {
		return reloadRequired;
	}

	public void clearReloadRequired() {
		reloadRequired = false;
	}

	@Nullable
	public Object2IntMap<BlockState> getBlockStateIds() {
		return blockStateIds;
	}

	@Nullable
	public Map<Block, ChunkRenderTypeSet> getBlockTypeIds() {
		return blockTypeIds;
	}

	// TODO (coderbot): This doesn't belong here. But I couldn't think of a nicer place to put it.
	@Nullable
	public Object2IntFunction<NamespacedId> getEntityIds() {
		return entityIds;
	}

	@Nullable
	public Object2IntFunction<NamespacedId> getItemIds() {
		return itemIds;
	}

	public void setBlockStateIds(Object2IntMap<BlockState> blockStateIds) {
		if (this.blockStateIds != null && this.blockStateIds.equals(blockStateIds)) {
			return;
		}

		this.reloadRequired = true;
		this.blockStateIds = blockStateIds;
	}

	public void setBlockTypeIds(Map<Block, ChunkRenderTypeSet> blockTypeIds) {
		if (this.blockTypeIds != null && this.blockTypeIds.equals(blockTypeIds)) {
			return;
		}

		this.reloadRequired = true;
		this.blockTypeIds = blockTypeIds;
	}

	public void setEntityIds(Object2IntFunction<NamespacedId> entityIds) {
		// note: no reload needed, entities are rebuilt every frame.
		this.entityIds = entityIds;
		this.hasVillagerConversionId = entityIds.containsKey(new NamespacedId("minecraft", "zombie_villager_converting"));
	}

	public void setItemIds(Object2IntFunction<NamespacedId> itemIds) {
		// note: no reload needed, entities are rebuilt every frame.
		this.itemIds = itemIds;
	}

	public float getAmbientOcclusionLevel() {
		return ambientOcclusionLevel;
	}

	public void setAmbientOcclusionLevel(float ambientOcclusionLevel) {
		if (ambientOcclusionLevel == this.ambientOcclusionLevel) {
			return;
		}

		this.reloadRequired = true;
		this.ambientOcclusionLevel = ambientOcclusionLevel;
	}

	public boolean shouldDisableDirectionalShading() {
		return disableDirectionalShading;
	}

	public void setDisableDirectionalShading(boolean disableDirectionalShading) {
		if (disableDirectionalShading == this.disableDirectionalShading) {
			return;
		}

		this.reloadRequired = true;
		this.disableDirectionalShading = disableDirectionalShading;
	}

	public boolean shouldUseSeparateAo() {
		return useSeparateAo;
	}

	public void setUseSeparateAo(boolean useSeparateAo) {
		if (useSeparateAo == this.useSeparateAo) {
			return;
		}

		this.reloadRequired = true;
		this.useSeparateAo = useSeparateAo;
	}

	public boolean shouldUseExtendedVertexFormat() {
		return useExtendedVertexFormat;
	}

	public void setUseExtendedVertexFormat(boolean useExtendedVertexFormat) {
		if (useExtendedVertexFormat == this.useExtendedVertexFormat) {
			return;
		}

		this.reloadRequired = true;
		this.useExtendedVertexFormat = useExtendedVertexFormat;
	}

	public boolean shouldVoxelizeLightBlocks() {
		return voxelizeLightBlocks;
	}

	public void setVoxelizeLightBlocks(boolean voxelizeLightBlocks) {
		if (voxelizeLightBlocks == this.voxelizeLightBlocks) {
			return;
		}

		this.reloadRequired = true;
		this.voxelizeLightBlocks = voxelizeLightBlocks;
	}

    public boolean shouldSeparateEntityDraws() {
		return separateEntityDraws;
    }

	public void setSeparateEntityDraws(boolean separateEntityDraws) {
		this.separateEntityDraws = separateEntityDraws;
	}

	public boolean hasVillagerConversionId() {
		return hasVillagerConversionId;
	}
}
