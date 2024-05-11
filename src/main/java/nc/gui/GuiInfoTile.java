package nc.gui;

import nc.network.NCPacket;
import nc.tile.ITileGui;
import nc.tile.TileContainerInfo;
import nc.util.Lazy;
import nc.util.Lazy.LazyInt;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiInfoTile<TILE extends TileEntity & ITileGui<TILE, PACKET, INFO>, PACKET extends NCPacket, INFO extends TileContainerInfo<TILE>> extends NCGui {
	
	protected final EntityPlayer player;
	
	protected final TILE tile;
	protected final INFO info;
	
	protected final ResourceLocation guiTextures;
	
	protected final Lazy<String> guiName;
	protected final LazyInt nameWidth;
	
	public GuiInfoTile(Container inventory, EntityPlayer player, TILE tile, String textureLocation) {
		super(inventory);
		
		this.player = player;
		
		this.tile = tile;
		info = tile.getContainerInfo();
		
		guiTextures = new ResourceLocation(textureLocation);
		
		guiName = new Lazy<>(() -> tile.getDisplayName().getUnformattedText());
		nameWidth = new LazyInt(() -> fontRenderer.getStringWidth(guiName.get()));
	}
	
	protected void defaultStateAndBind() {
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(guiTextures);
	}
}
