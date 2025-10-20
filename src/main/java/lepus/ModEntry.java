package lepus;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import lepus.mc.core.Core;
import lepus.mc.core.ModInit;
import lyra.filesystem.KlassPath;
import lyra.internal.oops.markWord;
import lyra.vm.Vm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ModEntry.ModId)
public class ModEntry {
	public static final String ModName = "Lepus";

	public static final String ModId = "lepus";

	public static final Logger Logger = LogUtils.getLogger();

	static {
		ModInit.Initializer.forInit();
	}

	@ModInit
	public static final void init(Dist env) {
		// 打印调试信息
		Logger.info("JVM bit-version=" + Vm.NATIVE_JVM_BIT_VERSION + ", flag UseCompressedOops=" + Vm.UseCompressedOops);
		Logger.info("KlassWord offset=" + markWord.KLASS_WORD_OFFSET + ", length=" + markWord.KLASS_WORD_LENGTH);
		Logger.info("Running on " + Core.Env + " environment with PID=" + Vm.getProcessId());
		Logger.info("Mod classpath=\"" + KlassPath.getKlassPath() + '\"');
	}
}
