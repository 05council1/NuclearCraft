package nc.container.multiblock.controller;

import nc.multiblock.fission.FissionReactor;
import nc.network.multiblock.FissionUpdatePacket;
import nc.tile.info.TileContainerInfo;
import nc.tile.fission.*;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerSolidFissionController extends ContainerMultiblockController<FissionReactor, IFissionPart, FissionUpdatePacket, TileSolidFissionController, TileContainerInfo<TileSolidFissionController>> {
	
	public ContainerSolidFissionController(EntityPlayer player, TileSolidFissionController controller) {
		super(player, controller);
	}
}
