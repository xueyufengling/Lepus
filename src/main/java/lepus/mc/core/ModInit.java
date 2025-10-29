package lepus.mc.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lyra.klass.KlassWalker;
import lyra.lang.DynamicConcurrentArrayList;
import lyra.lang.JavaLang;
import lyra.lang.internal.ReflectionBase;
import lyra.object.Placeholders.TypeWrapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;

/**
 * 初始化函数注解、<br>
 * 在加载完依赖库、获取事件总线ModBus后调用。<br>
 * 所有mod初始化操作必须在具有该注解的方法中进行，不得在mod入口类构造函数中进行初始化操作。<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ModInit {
	/**
	 * Mod的运行环境，客户端或服务端
	 * 
	 * @return
	 */
	Dist[] env() default { Dist.CLIENT, Dist.DEDICATED_SERVER
	};

	public static enum Stage {
		PRE_INIT, POST_INIT, LOAD_COMPLETE, CLIENT_CONNECT
	}

	/**
	 * 执行时机：<br>
	 * PRE_INIT在加载完类库以及初始化fw.core.registry.registries包注册表后执行；<br>
	 * POST_INIT在生成CODEC、注册注册表条目后执行。如果需要访问这些内容，需要选择POST_INIT作为运行时机。<br>
	 * 
	 * @return
	 */
	Stage exec_stage() default Stage.PRE_INIT;

	@EventBusSubscriber(bus = Bus.MOD)
	public static class Initializer {
		public static final Stage stageOf(Method m) {
			if (m == null)
				return null;
			ModInit anno = m.getAnnotation(ModInit.class);
			if (anno == null)
				return null;
			else
				return anno.exec_stage();
		}

		@SubscribeEvent(priority = EventPriority.HIGHEST)
		private static final void preLoadComplete(FMLLoadCompleteEvent event) {
			ModInit.Initializer.executeAllInitFuncs(event, ModInit.Stage.LOAD_COMPLETE);
		}

		/**
		 * 防止在PRE_INIT注解函数中添加新的@ModInit注解类时报错并发修改List
		 */
		private static final DynamicConcurrentArrayList<Class<?>> modInitClasses = new DynamicConcurrentArrayList<>();

		static final void executeAllInitFuncs(Event event, Stage run_stage) {
			ModContainer mod = Core.Mod;
			IEventBus bus = Core.ModBus;
			Dist dist = Core.Env;
			TypeWrapper<Integer> executed_counter = TypeWrapper.wrap(0);
			try {
				Core.logInfo("Initializer entered stage " + run_stage);
				modInitClasses.forEach(
						(Class<?> modInitClass) -> {
							Core.logInfo("Initializer is scanning " + " static @ModInit(exec_stage=" + run_stage + ") methods in " + modInitClass.toString());
							KlassWalker.walkAnnotatedMethods(modInitClass, ModInit.class, (Method m, boolean isStatic, Object obj, ModInit annotation) -> {
								if (isStatic && annotation.exec_stage() == run_stage) {
									Dist[] env = annotation.env();
									for (Dist d : env) {
										if (d == dist) {
											Class<?>[] paramTypes = m.getParameterTypes();
											Object[] args = new Object[paramTypes.length];
											for (int i = 0; i < paramTypes.length; ++i) {
												if (paramTypes[i] == ModContainer.class || paramTypes[i] == FMLModContainer.class)
													args[i] = mod;
												else if (paramTypes[i] == IEventBus.class)
													args[i] = bus;
												else if (paramTypes[i] == Dist.class)
													args[i] = dist;
												else if (paramTypes[i] == Event.class)
													args[i] = event;
											}
											try {
												ReflectionBase.setAccessible(m, true);
												m.invoke(obj, args);
												Core.logInfo("Executed " + m.toString());
												++executed_counter.value;
											} catch (IllegalAccessException | InvocationTargetException e) {
												Core.logError("Method " + m + " execute failed.", e);
											}
											break;// 只要匹配任意一个env指定的运行环境，则执行该静态方法并退出循环转而判定下一个静态方法
										}
									}
								}
								return true;
							});
						});
				Core.logInfo("Initializer executed " + executed_counter.value + " @ModInit methods in stage " + run_stage);
			} catch (Throwable ex) {
				Core.logError("Initializer execute @ModInit methods faield in stage " + run_stage + ", " + executed_counter.value + " executed.", ex);
			}
		}

		/**
		 * 设置初始化函数所在类
		 * 
		 * @param modInitCls
		 */
		public static final void forInit(Class<?> modInitCls) {
			modInitClasses.add(modInitCls);
		}

		public static final void forInit() {
			Class<?> caller = JavaLang.getOuterCallerClass();
			forInit(caller);
		}
	}
}
