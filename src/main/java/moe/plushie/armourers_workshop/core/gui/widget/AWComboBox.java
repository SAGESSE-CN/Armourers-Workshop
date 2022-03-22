package moe.plushie.armourers_workshop.core.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import moe.plushie.armourers_workshop.core.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AWComboBox extends Button {

    private final FontRenderer font;
    private final int fontHeight;
    protected int selectedIndex;

    protected int handX;
    protected int handY;
    protected int handWidth;
    protected int handHeight;

    protected int listX;
    protected int listY;
    protected int listRowHeight;
    protected ComboItemList list;

    protected boolean isInited = false;
    protected boolean popping;

    public AWComboBox(int x, int y, int width, int height, List<ComboItem> items, int selectedIndex, Button.IPressable changeHandler) {
        super(x, y, width, height, StringTextComponent.EMPTY, changeHandler, NO_TOOLTIP);
        this.font = Minecraft.getInstance().font;
        this.fontHeight = font.lineHeight;
        this.list = new ComboItemList(items, width, height, y + height + 1, x, font.lineHeight + 2);
        this.selectedIndex = selectedIndex;
        this.handWidth = 14;
        this.handHeight = height;
        this.handX = x + width - handWidth;
        this.handY = y;
        this.listY = y + height + 1;
        this.listX = x;
        this.listRowHeight = fontHeight + 2;
        this.list.setSelected(getSelectedItem());
        this.isInited = true;
    }

    public int getMaxRowCount() {
        return this.list.getMaxRowCount();
    }

    public void setMaxRowCount(int maxRowCount) {
        this.list.setMaxRowCount(maxRowCount);
    }

    public int getHandState() {
        if (isHovered) {
            return 1;
        }
        return 0;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    @Nullable
    public ComboItem getSelectedItem() {
        if (selectedIndex >= 0 && selectedIndex < list.children().size()) {
            return list.children().get(selectedIndex);
        }
        return null;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        int k = getHandState();
        RenderUtils.bind(WIDGETS_LOCATION);
        GuiUtils.drawContinuousTexturedBox(matrixStack, x, y, 0, 46, width, height, 200, 20, 2, 3, 2, 2, 0);
        GuiUtils.drawContinuousTexturedBox(matrixStack, handX, handY, 0, 66 + 20 * k, handWidth, handHeight, 200, 20, 2, 3, 2, 2, 0);

        String text = "v";
        int textY = handY + (handHeight - fontHeight) / 2;
        if (popping) {
            text = "^";
            textY += 2;
        }
        int textWidth = font.width(text);
        font.draw(matrixStack, text, handX + (handWidth - textWidth) / 2.0f, textY, 0xffffff);

        ComboItem item = getSelectedItem();
        if (item != null) {
            item.render(matrixStack, x, y, width, height, mouseX, mouseY, partialTicks, true);
        }

        if (popping) {
            list.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onPress() {
    }

    protected void onChange() {
        if (!isInited) {
            return;
        }
        if (onPress != null) {
            onPress.onPress(this);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (popping && list.isMouseOver(mouseX, mouseY)) {
            if (list.setSelectedAtPosition(mouseX, mouseY)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                popping = false;
            }
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            popping = !popping;
            return true;
        }
        popping = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        if (popping && list.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_)) {
            return true;
        }
        return super.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_);
    }

    @Override
    public boolean isMouseOver(double p_231047_1_, double p_231047_3_) {
        if (popping && list.isMouseOver(p_231047_1_, p_231047_3_)) {
            return true;
        }
        return super.isMouseOver(p_231047_1_, p_231047_3_);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        if (popping && list.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_)) {
            return true;
        }
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        if (popping && list.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_)) {
            return true;
        }
        return super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    public static class ComboItem extends AbstractOptionList.Entry<ComboItem> {

        final FontRenderer font;
        final ITextComponent title;

        boolean isEnabled = true;

        public ComboItem(ITextComponent title) {
            this.title = title;
            this.font = Minecraft.getInstance().font;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void render(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks, boolean isTopRender) {
            int textColor = 0xffffffff;
            if (!isEnabled) {
                textColor = 0xffcc0000;
            } else if (!isTopRender && isHovered(x, y, width, height, mouseX, mouseY)) {
                textColor = 0xffffffa0;
                fill(matrixStack, x + 1, y - 1, x + width - 1, y + height - 1, 0x44cccccc);
            }
            if (isTopRender) {
                font.draw(matrixStack, title, x + 3, y + 3, textColor);
            } else {
                font.draw(matrixStack, title, x + 3, y + 1, textColor);
            }
//            fill(matrixStack, x, y, x + width, y + height, 0x44ffcccc);
        }

        public boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= (x + width) && mouseY >= (y + 1) && mouseY <= (y + height);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return new ArrayList<>();
        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float p_230432_10_) {
            render(matrixStack, left - 1, top, width, height + 4, mouseX, mouseY, p_230432_10_, false);
        }
    }

    public class ComboItemList extends AbstractOptionList<ComboItem> {

        private int maxRowCount;

        public ComboItemList(List<ComboItem> items, int width, int height, int y0, int x0, int itemHeight) {
            super(Minecraft.getInstance(), width, height, y0, y0 + height, itemHeight);
            items.forEach(this::addEntry);
            this.x0 = x0;
            this.x1 = x0 + width;
            this.y0 = y0;
            this.y1 = y0 + height;
            this.setMaxRowCount(items.size());
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);
        }

        @Override
        public void render(MatrixStack matrixStack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
            RenderUtils.tile(matrixStack, x0, y0, 0, 46, width, height, 200, 20, 2, 3, 2, 2, WIDGETS_LOCATION);
            RenderUtils.enableScissor(x0, y0, width, height);
            super.render(matrixStack, p_230430_2_, p_230430_3_, p_230430_4_);
            RenderUtils.disableScissor();
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        protected int getScrollbarPosition() {
            return x1 - 6;
        }

        @Override
        public void setSelected(@Nullable ComboItem item) {
            super.setSelected(item);
            List<ComboItem> children = children();
            for (int i = 0; i < children.size(); ++i) {
                if (children.get(i) == item) {
                    selectedIndex = i;
                    onChange();
                    break;
                }
            }
        }

        public boolean setSelectedAtPosition(double mouseX, double mouseY) {
            ComboItem item = getEntryAtPosition(mouseX, mouseY);
            if (item != null && item.isEnabled()) {
                setSelected(item);
                return true;
            }
            return false;
        }

        public int getMaxRowCount() {
            return maxRowCount;
        }

        public void setMaxRowCount(int maxRowCount) {
            this.maxRowCount = maxRowCount;
            this.height = 4 + itemHeight * maxRowCount;
            this.y1 = y0 + height;
        }
    }
}
