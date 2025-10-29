package lepus.mc.datagen;

import java.util.concurrent.CompletableFuture;

import lepus.mc.codec.annotation.CodecAutogen;
import lepus.mc.core.Core;
import lepus.mc.datagen.annotation.ItemDatagen;
import lepus.mc.datagen.annotation.LangDatagen;
import lepus.mc.datagen.annotation.RegistryEntry;
import lepus.mc.datagen.internal.ExtTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class ExtDataGenerator {
	public static void datagen(GatherDataEvent event) {
		Core.logInfo("ExtDataGenerator starting to datagen.");
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		// 注册表内容生成
		RegistryEntry.RegistriesProvider registriesProvider = new RegistryEntry.RegistriesProvider(output, lookupProvider);
		generator.addProvider(event.includeServer(), registriesProvider);
		// Tag及其成员生成
		ExtTagsProvider.addProvider(generator, event.includeServer(), output, registriesProvider, helper);
		// 物品数据生成
		ItemDatagen.ModelProvider.addProvider(generator, event.includeClient(), output, helper);
		// 语言文件生成
		LangDatagen.LangProvider.addProvider(generator, event.includeClient(), output);
		CodecAutogen.CodecGenerator.generateCodecs();// 生成CODEC
		RegistryEntry.DeferredEntryHolderRegister.registerAll();
	}
}
