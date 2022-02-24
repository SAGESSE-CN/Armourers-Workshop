package moe.plushie.armourers_workshop.core.gui.wardrobe;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import moe.plushie.armourers_workshop.core.entity.MannequinEntity;
import moe.plushie.armourers_workshop.core.gui.widget.AWComboButton;
import moe.plushie.armourers_workshop.core.network.NetworkHandler;
import moe.plushie.armourers_workshop.core.network.packet.UpdateWardrobePacket;
import moe.plushie.armourers_workshop.core.texture.TextureDescriptor;
import moe.plushie.armourers_workshop.core.wardrobe.SkinWardrobe;
import moe.plushie.armourers_workshop.core.wardrobe.SkinWardrobeContainer;
import moe.plushie.armourers_workshop.core.wardrobe.SkinWardrobeOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.util.Strings;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class TextureSettingPanel extends BaseSettingPanel {

    private AWComboButton comboList;
    private TextFieldWidget textBox;
    private SkinWardrobe wardrobe;

    private TextureDescriptor oldDescriptor = TextureDescriptor.EMPTY;
    private TextureDescriptor.Source source = TextureDescriptor.Source.NONE;
    private HashMap<TextureDescriptor.Source, String> defaultValues = new HashMap<>();

    public TextureSettingPanel(SkinWardrobeContainer container) {
        super("inventory.armourers_workshop.wardrobe.man_texture");
        this.wardrobe = container.getWardrobe();
        this.prepareDefaultValue();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        this.addTextField(leftPos + 83 + 1, topPos + 70, defaultValues.get(source));
        this.addComboList(leftPos + 83, topPos + 27, source);
        this.addButton(new Button(leftPos + 83, topPos + 90, 100, 20, getDisplayText("set"), button -> submit()));
    }

    @Override
    public void removed() {
        super.removed();
        this.comboList = null;
        this.textBox = null;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (this.textBox != null) {
            this.textBox.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
        boolean typed = super.keyPressed(key, p_231046_2_, p_231046_3_);
        if (!typed && textBox != null && textBox.isFocused() && key == GLFW.GLFW_KEY_ENTER) {
            submit();
        }
        return typed;
    }

    private void prepareDefaultValue() {
        Entity entity = wardrobe.getEntity();
        if (!(entity instanceof MannequinEntity)) {
            return;
        }
        defaultValues.clear();
        oldDescriptor = entity.getEntityData().get(MannequinEntity.DATA_TEXTURE);
        source = oldDescriptor.getSource();
        if (source == TextureDescriptor.Source.USER) {
            defaultValues.put(source, oldDescriptor.getName());
        }
        if (source == TextureDescriptor.Source.URL) {
            defaultValues.put(source, oldDescriptor.getURL());
        }
    }

    private void submit() {
        textBox.setFocus(false);
        int index = comboList.getSelectedIndex();
        TextureDescriptor.Source source = TextureDescriptor.Source.values()[index + 1];
        applyText(source, textBox.getValue());
    }

    private void changeSource(TextureDescriptor.Source newSource) {
        if (this.source == newSource) {
            return;
        }
        defaultValues.put(source, textBox.getValue());
        textBox.setValue(defaultValues.getOrDefault(newSource, ""));
        textBox.setFocus(false);
        comboList.setSelectedIndex(newSource.ordinal() - 1);
        source = newSource;
    }

    private void applyText(TextureDescriptor.Source source, String value) {
        TextureDescriptor descriptor = TextureDescriptor.EMPTY;
        if (Strings.isNotEmpty(value)) {
            if (source == TextureDescriptor.Source.URL) {
                descriptor = new TextureDescriptor(value);
            }
            if (source == TextureDescriptor.Source.USER) {
                descriptor = new TextureDescriptor(new GameProfile(null, value));
            }
        }
        if (oldDescriptor.equals(descriptor)) {
            return; // no changes
        }
        UpdateWardrobePacket packet = UpdateWardrobePacket.option(wardrobe, SkinWardrobeOption.MANNEQUIN_TEXTURE, descriptor);
        NetworkHandler.getInstance().sendToServer(packet);
    }

    private void addComboList(int x, int y, TextureDescriptor.Source source) {
        int selectedIndex = 0;
        if (source != TextureDescriptor.Source.NONE) {
            selectedIndex = source.ordinal() - 1;
        }
        ArrayList<AWComboButton.ComboItem> items = new ArrayList<>();
        items.add(new AWComboButton.ComboItem(getDisplayText("dropdown.user")));
        items.add(new AWComboButton.ComboItem(getDisplayText("dropdown.url")));
        comboList = new AWComboButton(x, y, 80, 14, items, selectedIndex, button -> {
            if (button instanceof AWComboButton) {
                int index = ((AWComboButton) button).getSelectedIndex();
                changeSource(TextureDescriptor.Source.values()[index + 1]);
            }
        });
        addButton(comboList);
    }

    private void addTextField(int x, int y, String defaultValue) {
        textBox = new TextFieldWidget(font, x, y, 165, 16, StringTextComponent.EMPTY);
        if (!defaultValue.isEmpty()) {
            textBox.setValue(defaultValue);
        }
        addWidget(textBox);
    }
}