package nc.block.tile.info;

import java.util.function.Supplier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;

public abstract class BlockTileInfo<TILE extends TileEntity> {

	public final String modId;
	public final String name;

	protected final Class<TILE> tileClass;
	protected final Supplier<TILE> tileSupplier;
	
	public final CreativeTabs creativeTab;
	
	public BlockTileInfo(String modId, String name, Class<TILE> tileClass, Supplier<TILE> tileSupplier, CreativeTabs creativeTab) {
		this.modId = modId;
		this.name = name;

		this.tileClass = tileClass;
		this.tileSupplier = tileSupplier;

		this.creativeTab = creativeTab;
	}
	
	public TileEntity getNewTile() {
		return tileSupplier.get();
	}
}
