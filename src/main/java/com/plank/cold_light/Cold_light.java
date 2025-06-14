package com.plank.cold_light;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Cold_light.MODID)
public class Cold_light {
    public static final String MODID = "cold_light";
    public Cold_light() {
        @SuppressWarnings("removal")
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Sound.SOUNDS.register(bus);
    }
}
