package com.black_dog20.servertabinfo.common.datagen;

import com.black_dog20.servertabinfo.ServerTabInfo;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ServerTabInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(true, new GeneratorLanguageEnglish(generator));
        generator.addProvider(true, new GeneratorLanguageDanish(generator));
        generator.addProvider(true, new GeneratorLanguageGerman(generator));
        generator.addProvider(true, new GeneratorLanguageRussian(generator));
        generator.addProvider(true, new GeneratorLanguageChinese(generator));
    }


}
