package nc.gui.processor;

import nc.Global;
import nc.container.processor.ContainerSorptions;
import nc.gui.NCGui;
import nc.gui.element.GuiBlockRenderer;
import nc.gui.element.NCEnumButton;
import nc.network.gui.ResetTankSorptionsPacket;
import nc.network.gui.ToggleTankOutputSettingPacket;
import nc.network.gui.ToggleTankSorptionPacket;
import nc.network.tile.TileUpdatePacket;
import nc.tile.ITileGui;
import nc.tile.info.TileContainerInfo;
import nc.tile.fluid.ITileFluid;
import nc.tile.internal.fluid.TankSorption;
import nc.util.BlockHelper;
import nc.util.Lang;
import nc.util.NCUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.IOException;

public abstract class GuiFluidSorptions<TILE extends TileEntity & ITileGui<TILE, PACKET, INFO> & ITileFluid, PACKET extends TileUpdatePacket, INFO extends TileContainerInfo<TILE>> extends NCGui {
	
	protected final NCGui parent;
	protected final TILE tile;
	protected final EnumFacing[] dirs;
	protected final int slot;
	protected final TankSorption.Type sorptionType;
	protected static ResourceLocation gui_textures;
	protected int[] a, b;
	
	public GuiFluidSorptions(NCGui parent, TILE tile, int slot, TankSorption.Type sorptionType) {
		super(new ContainerSorptions<>(tile));
		this.parent = parent;
		this.tile = tile;
		EnumFacing facing = tile.getFacingHorizontal();
		dirs = new EnumFacing[] {BlockHelper.bottom(facing), BlockHelper.top(facing), BlockHelper.left(facing), BlockHelper.right(facing), BlockHelper.front(facing), BlockHelper.back(facing)};
		this.slot = slot;
		this.sorptionType = sorptionType;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (isEscapeKeyDown(keyCode)) {
			FMLCommonHandler.instance().showGuiScreen(parent);
		}
		else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	public void renderTooltips(int mouseX, int mouseY) {
		drawTooltip(Lang.localize("gui.nc.container.bottom_config") + " " + tile.getTankSorption(dirs[0], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[0], slot).getName() + "_config"), mouseX, mouseY, a[0], b[0], 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.top_config") + " " + tile.getTankSorption(dirs[1], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[1], slot).getName() + "_config"), mouseX, mouseY, a[1], b[1], 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.left_config") + " " + tile.getTankSorption(dirs[2], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[2], slot).getName() + "_config"), mouseX, mouseY, a[2], b[2], 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.right_config") + " " + tile.getTankSorption(dirs[3], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[3], slot).getName() + "_config"), mouseX, mouseY, a[3], b[3], 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.front_config") + " " + tile.getTankSorption(dirs[4], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[4], slot).getName() + "_config"), mouseX, mouseY, a[4], b[4], 18, 18);
		drawTooltip(Lang.localize("gui.nc.container.back_config") + " " + tile.getTankSorption(dirs[5], slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankSorption(dirs[5], slot).getName() + "_config"), mouseX, mouseY, a[5], b[5], 18, 18);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 0.75F);
		IBlockState state = tile.getBlockState(tile.getTilePos());
		for (int i = 0; i < 6; ++i) {
			GuiBlockRenderer.renderGuiBlock(state, dirs[i], a[i] + 1, b[i] + 1, zLevel, 16, 16);
		}
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.getTextureManager().bindTexture(gui_textures);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 6; ++i) {
			buttonList.add(new NCEnumButton.TankSorption(i, guiLeft + a[i], guiTop + b[i], tile.getTankSorption(dirs[i], slot), sorptionType));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (tile.getTileWorld().isRemote) {
			for (int i = 0; i < 6; ++i) {
				if (guiButton.id == i) {
					if (i == 4 && NCUtil.isModifierKeyDown()) {
						for (int j = 0; j < 6; ++j) {
							tile.setTankSorption(dirs[j], slot, TankSorption.NON);
							((NCEnumButton.TankSorption) buttonList.get(j)).set(TankSorption.NON);
						}
						new ResetTankSorptionsPacket(tile, slot, false).sendToServer();
						return;
					}
					tile.toggleTankSorption(dirs[i], slot, sorptionType, false);
					new ToggleTankSorptionPacket(tile, dirs[i], slot, tile.getTankSorption(dirs[i], slot)).sendToServer();
					return;
				}
			}
		}
	}
	
	@Override
	protected void actionPerformedRight(GuiButton guiButton) {
		if (tile.getTileWorld().isRemote) {
			for (int i = 0; i < 6; ++i) {
				if (guiButton.id == i) {
					if (i == 4 && NCUtil.isModifierKeyDown()) {
						for (int j = 0; j < 6; ++j) {
							TankSorption sorption = tile.getFluidConnection(dirs[j]).getDefaultTankSorption(slot);
							tile.setTankSorption(dirs[j], slot, sorption);
							((NCEnumButton.TankSorption) buttonList.get(j)).set(sorption);
						}
						new ResetTankSorptionsPacket(tile, slot, true).sendToServer();
						return;
					}
					tile.toggleTankSorption(dirs[i], slot, sorptionType, true);
					new ToggleTankSorptionPacket(tile, dirs[i], slot, tile.getTankSorption(dirs[i], slot)).sendToServer();
				}
			}
		}
	}
	
	public static class Input<TILE extends TileEntity & ITileGui<TILE, PACKET, INFO> & ITileFluid, PACKET extends TileUpdatePacket, INFO extends TileContainerInfo<TILE>> extends GuiFluidSorptions<TILE, PACKET, INFO> {
		
		public Input(NCGui parent, TILE tile, int slot) {
			super(parent, tile, slot, TankSorption.Type.INPUT);
			gui_textures = new ResourceLocation(Global.MOD_ID + ":textures/gui/container/input_fluid_config.png");
			a = new int[] {25, 25, 7, 43, 25, 43};
			b = new int[] {43, 7, 25, 25, 25, 43};
			xSize = 68;
			ySize = 68;
		}
	}
	
	public static class Output<TILE extends TileEntity & ITileGui<TILE, PACKET, INFO> & ITileFluid, PACKET extends TileUpdatePacket, INFO extends TileContainerInfo<TILE>> extends GuiFluidSorptions<TILE, PACKET, INFO> {
		
		public Output(NCGui parent, TILE tile, int slot) {
			super(parent, tile, slot, TankSorption.Type.OUTPUT);
			gui_textures = new ResourceLocation(Global.MOD_ID + ":textures/gui/container/output_fluid_config.png");
			a = new int[] {47, 47, 29, 65, 47, 65};
			b = new int[] {43, 7, 25, 25, 25, 43};
			xSize = 90;
			ySize = 68;
		}
		
		@Override
		public void renderTooltips(int mouseX, int mouseY) {
			super.renderTooltips(mouseX, mouseY);
			drawTooltip(Lang.localize("gui.nc.container.tank_setting_config") + " " + tile.getTankOutputSetting(slot).getTextColor() + Lang.localize("gui.nc.container." + tile.getTankOutputSetting(slot).getName() + "_setting_config"), mouseX, mouseY, 7, 25, 18, 18);
		}
		
		@Override
		public void initGui() {
			super.initGui();
			buttonList.add(new NCEnumButton.TankOutputSetting(6, guiLeft + 7, guiTop + 25, tile.getTankOutputSetting(slot)));
		}
		
		@Override
		protected void actionPerformed(GuiButton guiButton) {
			super.actionPerformed(guiButton);
			if (tile.getTileWorld().isRemote) {
				if (guiButton.id == 6) {
					tile.setTankOutputSetting(slot, tile.getTankOutputSetting(slot).next(false));
					new ToggleTankOutputSettingPacket(tile, slot, tile.getTankOutputSetting(slot)).sendToServer();
				}
			}
		}
		
		@Override
		protected void actionPerformedRight(GuiButton guiButton) {
			super.actionPerformedRight(guiButton);
			if (tile.getTileWorld().isRemote) {
				if (guiButton.id == 6) {
					tile.setTankOutputSetting(slot, tile.getTankOutputSetting(slot).next(true));
					new ToggleTankOutputSettingPacket(tile, slot, tile.getTankOutputSetting(slot)).sendToServer();
				}
			}
		}
	}
}
