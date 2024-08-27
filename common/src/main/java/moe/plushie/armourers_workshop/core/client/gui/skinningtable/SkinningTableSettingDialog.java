package moe.plushie.armourers_workshop.core.client.gui.skinningtable;

import com.apple.library.coregraphics.CGRect;
import com.apple.library.foundation.NSString;
import com.apple.library.uikit.UICheckBox;
import com.apple.library.uikit.UIControl;
import moe.plushie.armourers_workshop.core.client.gui.widget.ConfirmDialog;
import moe.plushie.armourers_workshop.core.skin.SkinOptions;

public class SkinningTableSettingDialog extends ConfirmDialog {

    private final SkinOptions options;

    private int contentHeight = 24; // 24 + n + 8

    public SkinningTableSettingDialog(SkinOptions options) {
        super();
        this.options = options.copy();
        this.setup();
        this.contentHeight += 6;
    }

    private void setup() {
        messageLabel.removeFromSuperview();

        // [x] ...
        addOptionView(Option.ITEM_RENDERER_SHOW_SKIN);
        addOptionView(Option.TOOLTIP_SHOW_NAME);
        addOptionView(Option.TOOLTIP_SHOW_AUTHOR);
        addOptionView(Option.TOOLTIP_SHOW_SKIN_TYPE);
        addOptionView(Option.TOOLTIP_SHOW_FLAVOUR);
        addOptionView(Option.TOOLTIP_SHOW_HAS_SKIN);
        addOptionView(Option.TOOLTIP_SHOW_OPEN_WARDROBE);
        addOptionView(Option.TOOLTIP_SHOW_SKIN_PREVIEW);
    }

    private UICheckBox addOptionView(Option<Boolean> option) {
        var checkBox = new UICheckBox(new CGRect(8, contentHeight, bounds().getWidth() - 16, 9));
        checkBox.setTitle(NSString.localizedString("skinning-table.setting." + option.key));
        checkBox.setSelected(option.get(options));
        checkBox.addTarget(this, UIControl.Event.VALUE_CHANGED, (self, sender) -> {
            option.set(options, checkBox.isSelected());
        });
        checkBox.setAutoresizingMask(AutoresizingMask.flexibleWidth | AutoresizingMask.flexibleBottomMargin);
        addSubview(checkBox);
        contentHeight += 9 + 4;
        return checkBox;
    }

    @Override
    public void sizeToFit() {
        setBounds(new CGRect(0, 0, bounds().getWidth(), contentHeight + 30));
    }

    public SkinOptions getOptions() {
        return options.copy();
    }

    public static abstract class Option<T> {

        public static final Option<Boolean> TOOLTIP_SHOW_NAME = flags("tooltip.showName", SkinOptions.TooltipFlags.NAME);
        public static final Option<Boolean> TOOLTIP_SHOW_AUTHOR = flags("tooltip.showAuthor", SkinOptions.TooltipFlags.AUTHOR);
        public static final Option<Boolean> TOOLTIP_SHOW_FLAVOUR = flags("tooltip.showFlavour", SkinOptions.TooltipFlags.AUTHOR);
        public static final Option<Boolean> TOOLTIP_SHOW_HAS_SKIN = flags("tooltip.showHasSkin", SkinOptions.TooltipFlags.HAS_SKIN);
        public static final Option<Boolean> TOOLTIP_SHOW_OPEN_WARDROBE = flags("tooltip.showOpenWardrobe", SkinOptions.TooltipFlags.OPEN_WARDROBE);
        public static final Option<Boolean> TOOLTIP_SHOW_SKIN_TYPE = flags("tooltip.showSkinType", SkinOptions.TooltipFlags.TYPE);
        public static final Option<Boolean> TOOLTIP_SHOW_SKIN_PREVIEW = flags("tooltip.showSkinPreview", SkinOptions.TooltipFlags.PREVIEW);

        public static final Option<Boolean> ITEM_RENDERER_SHOW_SKIN = itemRenderer("itemRenderer.showSkin");

        private final String key;

        public Option(String key) {
            this.key = key;
        }

        public static Option<Boolean> flags(String key, SkinOptions.TooltipFlags flags) {
            return new Option<>(key) {

                @Override
                public Boolean get(SkinOptions options) {
                    return options.get(flags);
                }

                @Override
                public void set(SkinOptions options, Boolean value) {
                    options.set(flags, value);
                }
            };
        }

        public static Option<Boolean> itemRenderer(String key) {
            return new Option<>(key) {

                @Override
                public Boolean get(SkinOptions options) {
                    return options.getEmbeddedItemRenderer() == 2;
                }

                @Override
                public void set(SkinOptions options, Boolean value) {
                    if (value) {
                        options.setEnableEmbeddedItemRenderer(2);
                    } else {
                        options.setEnableEmbeddedItemRenderer(0);
                    }
                }
            };
        }

        public abstract T get(SkinOptions options);

        public abstract void set(SkinOptions options, T value);
    }
}
