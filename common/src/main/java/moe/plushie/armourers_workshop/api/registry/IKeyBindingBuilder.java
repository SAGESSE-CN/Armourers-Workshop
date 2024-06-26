package moe.plushie.armourers_workshop.api.registry;

import moe.plushie.armourers_workshop.api.client.key.IKeyBinding;
import moe.plushie.armourers_workshop.api.client.key.IKeyModifier;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface IKeyBindingBuilder<T extends IKeyBinding> {

    IKeyBindingBuilder<T> modifier(IKeyModifier modifier);

    IKeyBindingBuilder<T> category(String category);

    IKeyBindingBuilder<T> bind(Supplier<Runnable> handler);

    T build(String name);
}
