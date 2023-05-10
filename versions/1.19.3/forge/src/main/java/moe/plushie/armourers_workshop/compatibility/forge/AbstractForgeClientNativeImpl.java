package moe.plushie.armourers_workshop.compatibility.forge;

import com.apple.library.coregraphics.CGRect;
import moe.plushie.armourers_workshop.compatibility.AbstractClientNativeImpl;
import moe.plushie.armourers_workshop.compatibility.ext.AbstractClientNativeProviderExt_V19;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.init.environment.EnvironmentType;
import moe.plushie.armourers_workshop.init.platform.forge.NotificationCenterImpl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;

import java.util.List;
import java.util.function.Consumer;

public class AbstractForgeClientNativeImpl extends AbstractClientNativeImpl implements AbstractForgeClientNativeProvider, AbstractClientNativeProviderExt_V19 {

    @Override
    public void willRegisterItemColor(Consumer<ItemColorRegistry> consumer) {
        NotificationCenterImpl.observer(RegisterColorHandlersEvent.Item.class, consumer, event -> (provider, values) -> event.getItemColors().register(provider::getTintColor, values));
    }

    @Override
    public void willRegisterBlockColor(Consumer<BlockColorRegistry> consumer) {
        NotificationCenterImpl.observer(RegisterColorHandlersEvent.Block.class, consumer, event -> (provider, values) -> event.getBlockColors().register(provider::getTintColor, values));
    }

    @Override
    public void willRegisterModel(Consumer<ModelRegistry> consumer) {
        NotificationCenterImpl.observer(ModelEvent.RegisterAdditional.class, consumer, event -> event::register);
    }

    @Override
    public void willRegisterKeyMapping(Consumer<KeyMappingRegistry> consumer) {
        NotificationCenterImpl.observer(RegisterKeyMappingsEvent.class, consumer, event -> event::register);
    }

    @Override
    public void willRegisterItemProperty(Consumer<ItemPropertyRegistry> consumer) {
        EnvironmentExecutor.didInit(EnvironmentType.COMMON, () -> () -> {
            consumer.accept((registryName, item, property) -> ItemProperties.register(item, registryName, property::getValue));
        });
    }

    @Override
    public void willRegisterTexture(Consumer<TextureRegistry> consumer) {
        // everything in the block, item, particle and a few other folders is now stitched automaticall.
    }

    @Override
    public void willPlayerEnter(Consumer<Player> consumer) {
        NotificationCenterImpl.observer(ClientPlayerNetworkEvent.LoggingIn.class, consumer, ClientPlayerNetworkEvent::getPlayer);
    }

    @Override
    public void willPlayerLeave(Consumer<Player> consumer) {
        NotificationCenterImpl.observer(ClientPlayerNetworkEvent.LoggingOut.class, consumer, ClientPlayerNetworkEvent::getPlayer);
    }

    @Override
    public void willRenderBlockHighlight(RenderBlockHighlight renderer) {
        NotificationCenterImpl.observer(RenderHighlightEvent.Block.class, event -> {
            renderer.render(event.getTarget(), event.getCamera(), event.getPoseStack(), event.getMultiBufferSource());
        });
    }

    @Override
    public void willRenderLivingEntity(RenderLivingEntity renderer) {
        NotificationCenterImpl.observer(RenderLivingEvent.Pre.class, event -> {
            renderer.render(event.getEntity(), event.getPartialTick(), event.getPackedLight(), event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer());
        });
    }

    @Override
    public void didRenderLivingEntity(RenderLivingEntity renderer) {
        NotificationCenterImpl.observer(RenderLivingEvent.Post.class, event -> {
            renderer.render(event.getEntity(), event.getPartialTick(), event.getPackedLight(), event.getPoseStack(), event.getMultiBufferSource(), event.getRenderer());
        });
    }

    @Override
    public void willRenderTooltip(RenderTooltip consumer) {
        NotificationCenterImpl.observer(RenderTooltipEvent.Pre.class, event -> {
            Font font = event.getFont();
            List<ClientTooltipComponent> tooltips = event.getComponents();
            int mouseX = event.getX();
            int mouseY = event.getY();
            int screenWidth = event.getScreenWidth();
            int screenHeight = event.getScreenHeight();
            int i = 0;
            int j = tooltips.size() == 1 ? -2 : 0;
            for (ClientTooltipComponent tooltip : tooltips) {
                int k = tooltip.getWidth(font);
                if (k > i) {
                    i = k;
                }
                j += tooltip.getHeight();
            }
            int j2 = mouseX + 12;
            int k2 = mouseY - 12;
            if (j2 + i > screenWidth) {
                j2 -= 28 + i;
            }
            if (k2 + j + 6 > screenHeight) {
                k2 = screenHeight - j - 6;
            }
            CGRect frame = new CGRect(j2, k2, i, j);
            consumer.render(event.getItemStack(), frame, mouseX, mouseY, screenWidth, screenHeight, event.getPoseStack());
        });
    }
}
