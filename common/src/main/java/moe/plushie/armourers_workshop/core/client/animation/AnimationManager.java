package moe.plushie.armourers_workshop.core.client.animation;

import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkinAnimation;
import moe.plushie.armourers_workshop.core.client.other.BlockEntityRenderData;
import moe.plushie.armourers_workshop.core.client.other.EntityRenderData;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.utils.TickUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AnimationManager {

    private final HashMap<SkinDescriptor, Entry> entries = new HashMap<>();
    private final HashMap<BakedSkinAnimation, AnimationState> states = new HashMap<>();

    private final ArrayList<BakedSkinAnimation> animations = new ArrayList<>();
    private final ArrayList<BakedSkinAnimation> removeOnCompletion = new ArrayList<>();

    public static AnimationManager of(Entity entity) {
        var renderData = EntityRenderData.of(entity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public static AnimationManager of(BlockEntity blockEntity) {
        var renderData = BlockEntityRenderData.of(blockEntity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public void load(Map<SkinDescriptor, BakedSkin> skins) {
        var oldEntries = new HashMap<>(entries);
        skins.forEach((key, skin) -> {
            oldEntries.remove(key);
            var entry = entries.computeIfAbsent(key, Entry::new);
            if (entry.isLoaded || skin == null) {
                return;
            }
            entry.isLoaded = true;
            entry.animations = skin.getAnimations();
            if (entry.animations != null) {
                entry.animations.forEach(animation -> {
                    // auto play
                    if (isParallelAnimation(animation)) {
                        play(animation, TickUtils.animationTicks(), 0);
                    }
                });
                animations.addAll(entry.animations);
            }
        });
        oldEntries.forEach((key, entry) -> {
            entries.remove(key);
            if (entry.animations != null) {
                entry.animations.forEach(this::stop);
                animations.removeAll(entry.animations);
            }
        });
    }

    public void tick() {
        // ignore when no task.
        if (removeOnCompletion.isEmpty()) {
            return;
        }
        var animationTicks = TickUtils.animationTicks();
        var animations = new ArrayList<>(removeOnCompletion);
        for (var animation : animations) {
            var state = states.get(animation);
            if (state != null && state.isCompleted(animationTicks)) {
                stop(animation);
            }
        }
    }

    public void play(String name, float atTime, int playCount) {
        animations.forEach(animation -> {
            if (name.equals(animation.getName())) {
                play(animation, atTime, playCount);
            }
        });
    }

    public void stop(String name) {
        animations.forEach(animation -> {
            if (name.isEmpty() || name.equals(animation.getName())) {
                stop(animation);
            }
        });
    }

    public void play(BakedSkinAnimation animation, float atTime, int playCount) {
        var state = new AnimationState(animation);
        if (playCount == 0) {
            playCount = switch (animation.getLoop()) {
                case NONE -> 1;
                case LAST_FRAME -> 0;
                case LOOP -> -1;
            };
        }
        state.setStartTime(atTime);
        state.setPlayCount(playCount);
        states.put(animation, state);
        ModLog.debug("start play {}", animation);
        // automatically remove on animation completion.
        if (playCount > 0) {
            removeOnCompletion.add(animation);
        } else {
            removeOnCompletion.remove(animation);
        }
    }

    public void stop(BakedSkinAnimation animation) {
        var state = states.remove(animation);
        if (state == null) {
            return;
        }
        ModLog.debug("stop play {}", animation);
        removeOnCompletion.remove(animation);
    }

    public AnimationState getAnimationState(BakedSkinAnimation animation) {
        return states.get(animation);
    }


    private boolean isParallelAnimation(BakedSkinAnimation animation) {
        var name = animation.getName();
        return name != null && name.matches("^(.+\\.)?parallel\\d+$");
    }

    protected static class Entry {

        private final SkinDescriptor descriptor;

        private List<BakedSkinAnimation> animations;
        private boolean isLoaded = false;

        public Entry(SkinDescriptor descriptor) {
            this.descriptor = descriptor;
        }
    }
}
