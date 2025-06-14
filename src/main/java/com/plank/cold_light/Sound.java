package com.plank.cold_light;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class Sound {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "cold_light");
    private static RegistryObject<SoundEvent> create(String name)
    {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("cold_light", name)));
    }
    public static final RegistryObject<SoundEvent> JAVELIN_THUNDER = create("item.javelin.thunder");
    public static final RegistryObject<SoundEvent> JAVELIN_RETURN = create("item.javelin.return");
    public static final RegistryObject<SoundEvent> JAVELIN_RIPTIDE_1 = create("item.javelin.riptide_1");
    public static final RegistryObject<SoundEvent> JAVELIN_RIPTIDE_2 = create("item.javelin.riptide_2");
    public static final RegistryObject<SoundEvent> JAVELIN_RIPTIDE_3 = create("item.javelin.riptide_3");
}
