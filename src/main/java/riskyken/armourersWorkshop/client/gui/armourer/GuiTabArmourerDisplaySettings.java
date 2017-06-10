package riskyken.armourersWorkshop.client.gui.armourer;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import riskyken.armourersWorkshop.client.gui.GuiHelper;
import riskyken.armourersWorkshop.client.gui.controls.GuiCheckBox;
import riskyken.armourersWorkshop.client.gui.controls.GuiTabPanel;
import riskyken.armourersWorkshop.client.lib.LibGuiResources;
import riskyken.armourersWorkshop.common.network.PacketHandler;
import riskyken.armourersWorkshop.common.network.messages.client.MessageClientGuiSetSkin;
import riskyken.armourersWorkshop.common.tileentities.TileEntityArmourer;

@SideOnly(Side.CLIENT)
public class GuiTabArmourerDisplaySettings extends GuiTabPanel {

    private static final ResourceLocation TEXTURE = new ResourceLocation(LibGuiResources.ARMOURER);
    
    private final TileEntityArmourer tileEntity;
    
    private GuiTextField textUserSkin;
    private GuiCheckBox checkShowGuides;
    private GuiCheckBox checkShowOverlay;
    private GuiCheckBox checkShowHelper;
    
    public GuiTabArmourerDisplaySettings(int tabId, GuiScreen parent) {
        super(tabId, parent, false);
        tileEntity = ((GuiArmourer)parent).tileEntity;
    }
    
    @Override
    public void initGui(int xPos, int yPos, int width, int height) {
        super.initGui(xPos, yPos, width, height);
        String guiName = tileEntity.getInventoryName();
        
        buttonList.clear();
        
        checkShowGuides = new GuiCheckBox(7, 64, 118, GuiHelper.getLocalizedControlName(guiName, "showGuide"), tileEntity.isShowGuides());
        checkShowOverlay = new GuiCheckBox(9, 64, 134, GuiHelper.getLocalizedControlName(guiName, "showOverlay"), tileEntity.isShowOverlay());
        checkShowHelper = new GuiCheckBox(6, 64, 134, GuiHelper.getLocalizedControlName(guiName, "showHelper"), tileEntity.isShowHelper());
        
        buttonList.add(new GuiButtonExt(8, 138, 88, 30, 16, GuiHelper.getLocalizedControlName(guiName, "set")));
        textUserSkin = new GuiTextField(fontRenderer, x + 64, y + 88, 70, 16);
        textUserSkin.setMaxStringLength(30);
        if (tileEntity.getGameProfile() != null) {
            textUserSkin.setText(tileEntity.getGameProfile().getName());
        }
        
        buttonList.add(checkShowGuides);
        buttonList.add(checkShowOverlay);
        buttonList.add(checkShowHelper);
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        textUserSkin.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyTyped(char c, int keycode) {
        if (!textUserSkin.textboxKeyTyped(c, keycode)) {
            return super.keyTyped(c, keycode);
        }
        return false;
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 8) {
            String username = textUserSkin.getText().trim();
            // TODO change this to allow no username
            if (!username.equals("")) {
                PacketHandler.networkWrapper.sendToServer(new MessageClientGuiSetSkin(username));
            }
        }
    }

    @Override
    public void drawBackgroundLayer(float partialTickTime, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(this.x, this.y, 0, 0, this.width, this.height);
        
        textUserSkin.drawTextBox();
        
        checkShowGuides.setIsChecked(tileEntity.isShowGuides());
        checkShowOverlay.setIsChecked(tileEntity.isShowOverlay());
        
        int checkY = 134;
        if (tileEntity.getSkinType() != null) {
            checkShowOverlay.visible = tileEntity.getSkinType().showSkinOverlayCheckbox();
            checkShowOverlay.yPosition = checkY;
            if (checkShowOverlay.visible) {
                checkY += 16;
            }
        } else {
            checkShowOverlay.visible = false;
        }
        
        if (tileEntity.getSkinType() != null) {
            checkShowHelper.visible = tileEntity.getSkinType().showHelperCheckbox();
            checkShowHelper.yPosition = checkY;
            //checkY += 16;
        } else {
            checkShowHelper.visible = false;
        }
    }
}
