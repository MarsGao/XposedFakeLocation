package com.noobexon.xposedfakelocation.xposed.hooks

import android.app.Activity
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.noobexon.xposedfakelocation.xposed.utils.LocationUtil
import com.noobexon.xposedfakelocation.xposed.utils.PreferencesUtil
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedInterface.Hooker
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

// TODO: in all hooks, we should check every 3 seconds if we are still in scope. isPlaying is not enough.

class LocationApiHooks(private val module: XposedInterface, private val classLoader: ClassLoader) {
    private val tag = "[LocationApiHooks]"
    private val runtimeTag = "XFL-SdkHook"
    private val hookedExecutables = ConcurrentHashMap.newKeySet<Executable>()
    private val dynamicallyHookedClasses = ConcurrentHashMap.newKeySet<Class<*>>()
    private val runtimeMarkers = ConcurrentHashMap.newKeySet<String>()
    private val amapLocationClasses = setOf(
        "com.amap.location.sdkh.base.type.location.AmapLocation",
        "com.amap.location.type.location.Location",
        "com.amap.location.support.bean.location.AmapLocation",
    )
    private val amapCallbackClasses = mapOf(
        "com.amap.location.sdkh.base.LocationGlobal" to setOf("setLatestLocation"),
        "com.amap.location.sdkh.AmapLocationService\$NativeLocationCallback" to setOf(
            "onBaseLocationChanged",
            "onNetworkLocationChanged"
        ),
        "com.amap.location.sdkh.base.locator.AbstractLocator" to setOf("report"),
        "com.amap.location.sdkh.environment.gnss.location.GnssLocationManager\$1" to
            setOf("onLocationChanged"),
        "com.amap.location.sdkh.environment.sysloc.AmapSyslocManager\$1" to
            setOf("onLocationChanged"),
        "com.amap.location.sdkh.environment.sysloc.SyslocProvider" to setOf("report"),
        "com.amap.location.sdkh.module.NativeDiffDataProxy\$2" to setOf("onLocationChanged"),
        "com.amap.location.sdkh.module.NativeGnssProxy\$1" to setOf("onLocationChanged"),
        "com.amap.location.sdkh.module.NativeGnssProxy\$2" to setOf("onLocationChanged"),
        "com.amap.location.sdkh.module.NativeLocatorProxy\$1" to setOf("onLocationChanged"),
        "com.amap.location.sdkh.module.NativeLocatorProxy\$LocatorCallback" to
            setOf("onLocationChanged"),
    )
    private val dynamicTargetClasses = amapLocationClasses +
        amapCallbackClasses.keys +
        setOf(
            "com.tencent.mm.pluginsdk.location.Location",
            "com.tencent.mm.pluginsdk.location.LocationView",
            "com.tencent.pigeon.biz.BizUserLocationInfo",
            "com.tencent.mm.plugin.location.ui.impl.j1",
            "com.tencent.mm.plugin.location.ui.impl.t2",
            "com.tencent.mm.plugin.location.ui.impl.p2",
            "com.tencent.mm.plugin.location.model.LocationInfo",
            "cx0.h",
        )

    fun initHooks() {
        hookLocation()
        hookLocationManager()
        hookWeChatActivityLifecycle()
        hookDynamicClassLoading()
        initThirdPartyHooks()
        module.log(Log.INFO, tag, "Instantiated hooks successfully")
    }

    fun initThirdPartyHooks() {
        Log.i(runtimeTag, "Installing third-party SDK hooks")
        hookThirdPartySdks()
    }

    private fun hookLocation() {
        try {
            val locationClass = Class.forName("android.location.Location", false, classLoader)

            module.hook(locationClass.getDeclaredMethod("getLatitude")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getLatitude()")
                module.log(Log.INFO, tag, "\t Original latitude: $original")
                if (PreferencesUtil.getIsPlaying() == true) {
                    module.log(Log.INFO, tag, "\t Modified to: ${LocationUtil.latitude}")
                    LocationUtil.latitude
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getLongitude")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getLongitude()")
                module.log(Log.INFO, tag, "\t Original longitude: $original")
                if (PreferencesUtil.getIsPlaying() == true) {
                    module.log(Log.INFO, tag, "\t Modified to: ${LocationUtil.longitude}")
                    LocationUtil.longitude
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getAccuracy")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getAccuracy()")
                module.log(Log.INFO, tag, "\t Original accuracy: $original")
                if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseAccuracy() == true) {
                    module.log(Log.INFO, tag, "\t Modified to: ${LocationUtil.accuracy}")
                    LocationUtil.accuracy
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getAltitude")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getAltitude()")
                module.log(Log.INFO, tag, "\t Original altitude: $original")
                if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseAltitude() == true) {
                    module.log(Log.INFO, tag, "\t Modified to: ${LocationUtil.altitude}")
                    LocationUtil.altitude
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getVerticalAccuracyMeters")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getVerticalAccuracyMeters()")
                module.log(Log.INFO, tag, "\tOriginal vertical accuracy: $original")
                if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseVerticalAccuracy() == true) {
                    module.log(Log.INFO, tag, "\tModified to: ${LocationUtil.verticalAccuracy}")
                    LocationUtil.verticalAccuracy
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getSpeed")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getSpeed()")
                module.log(Log.INFO, tag, "\tOriginal speed: $original")
                if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseSpeed() == true) {
                    module.log(Log.INFO, tag, "\tModified to: ${LocationUtil.speed}")
                    LocationUtil.speed
                } else {
                    original
                }
            }

            module.hook(locationClass.getDeclaredMethod("getSpeedAccuracyMetersPerSecond")).intercept { chain ->
                val original = chain.proceed()
                LocationUtil.updateLocation()
                module.log(Log.INFO, tag, "Leaving method getSpeedAccuracyMetersPerSecond()")
                module.log(Log.INFO, tag, "\tOriginal speed accuracy: $original")
                if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseSpeedAccuracy() == true) {
                    module.log(Log.INFO, tag, "\tModified to: ${LocationUtil.speedAccuracy}")
                    LocationUtil.speedAccuracy
                } else {
                    original
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                module.hook(locationClass.getDeclaredMethod("getMslAltitudeMeters")).intercept { chain ->
                    val original = chain.proceed()
                    LocationUtil.updateLocation()
                    module.log(Log.INFO, tag, "Leaving method getMslAltitudeMeters()")
                    module.log(Log.INFO, tag, "\tOriginal MSL altitude: $original")
                    if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseMeanSeaLevel() == true) {
                        module.log(Log.INFO, tag, "\tModified to: ${LocationUtil.meanSeaLevel}")
                        LocationUtil.meanSeaLevel
                    } else {
                        original
                    }
                }

                module.hook(locationClass.getDeclaredMethod("getMslAltitudeAccuracyMeters")).intercept { chain ->
                    val original = chain.proceed()
                    LocationUtil.updateLocation()
                    module.log(Log.INFO, tag, "Leaving method getMslAltitudeAccuracyMeters()")
                    module.log(Log.INFO, tag, "\tOriginal MSL altitude accuracy: $original")
                    if (PreferencesUtil.getIsPlaying() == true && PreferencesUtil.getUseMeanSeaLevelAccuracy() == true) {
                        module.log(Log.INFO, tag, "\tModified to: ${LocationUtil.meanSeaLevelAccuracy}")
                        LocationUtil.meanSeaLevelAccuracy
                    } else {
                        original
                    }
                }
            } else {
                module.log(Log.INFO, tag, "getMslAltitudeMeters() and getMslAltitudeAccuracyMeters() not available on this API level")
            }

        } catch (e: Exception) {
            module.log(Log.ERROR, tag, "Error hooking Location class - ${e.message}")
        }
    }

    private fun hookLocationManager() {
        try {
            val locationManagerClass = Class.forName("android.location.LocationManager", false, classLoader)
            val method = locationManagerClass.getDeclaredMethod("getLastKnownLocation", String::class.java)

            module.hook(method).intercept { chain ->
                val original = chain.proceed() as? Location
                module.log(Log.INFO, tag, "Leaving method getLastKnownLocation(provider)")
                module.log(Log.INFO, tag, "\t Original location: $original")
                val provider = chain.getArg(0) as String
                module.log(Log.INFO, tag, "\t Requested data from: $provider")
                if (PreferencesUtil.getIsPlaying() == true) {
                    val fakeLocation = LocationUtil.createFakeLocation(provider = provider)
                    module.log(Log.INFO, tag, "\t Modified location: $fakeLocation")
                    fakeLocation
                } else {
                    original
                }
            }

        } catch (e: Exception) {
            module.log(Log.ERROR, tag, "Error hooking LocationManager - ${e.message}")
        }
    }

    private fun hookThirdPartySdks() {
        hookAmapSdk()
        hookWeChatSdk()
        tryHookLocationClass("com.baidu.location.BDLocation")
    }

    private fun hookAmapSdk() {
        amapLocationClasses.forEach { className ->
            tryHookLocationClass(className)
            hookCoordinateSetters(className)
            hookAmapConstructors(className)
        }

        amapCallbackClasses.forEach { (className, methodNames) ->
            hookAmapLocationArguments(className, methodNames)
        }
    }

    private fun hookDynamicClassLoading() {
        val loaderMethods = buildList {
            addAll(
                ClassLoader::class.java.declaredMethods.filter {
                    it.name == "loadClass" &&
                        it.parameterTypes.firstOrNull() == String::class.java &&
                        it.returnType == Class::class.java &&
                        !Modifier.isAbstract(it.modifiers)
                }
            )

            try {
                val baseDexClassLoader = Class.forName("dalvik.system.BaseDexClassLoader")
                addAll(
                    baseDexClassLoader.declaredMethods.filter {
                        it.name == "findClass" &&
                            it.parameterTypes.firstOrNull() == String::class.java &&
                            it.returnType == Class::class.java &&
                            !Modifier.isAbstract(it.modifiers)
                    }
                )
            } catch (e: Throwable) {
                module.log(Log.WARN, tag, "Cannot inspect BaseDexClassLoader: ${e.message}")
            }
        }.distinct()

        loaderMethods.forEach { method ->
            installHook(method, "${method.declaringClass.name}.${method.name}") { chain ->
                val result = chain.proceed()
                val requestedName = chain.getArg(0) as? String
                val loadedClass = result as? Class<*>
                if (requestedName in dynamicTargetClasses && loadedClass != null) {
                    installHooksForDynamicallyLoadedClass(requestedName!!, loadedClass)
                }
                result
            }
        }
    }

    private fun installHooksForDynamicallyLoadedClass(className: String, cls: Class<*>) {
        if (!dynamicallyHookedClasses.add(cls)) return

        try {
            when {
                className in amapLocationClasses -> {
                    tryHookLocationClass(className, cls)
                    hookCoordinateSetters(className, cls)
                    hookAmapConstructors(className, cls)
                }

                className in amapCallbackClasses -> {
                    hookAmapLocationArguments(
                        className,
                        amapCallbackClasses.getValue(className),
                        cls
                    )
                }

                className == "com.tencent.mm.pluginsdk.location.Location" ->
                    hookWeChatLocationWrapperClass(cls)

                className == "com.tencent.mm.pluginsdk.location.LocationView" ->
                    hookWeChatLocationViewClass(cls)

                className == "com.tencent.pigeon.biz.BizUserLocationInfo" ->
                    hookWeChatBizLocationClass(cls)

                className == "com.tencent.mm.plugin.location.ui.impl.j1" ||
                    className == "com.tencent.mm.plugin.location.ui.impl.t2" ->
                    hookWeChatLocationCallbackClass(className, cls)

                className == "com.tencent.mm.plugin.location.ui.impl.p2" ->
                    hookWeChatLocationInfoArgumentClass(cls)

                className == "cx0.h" ->
                    hookWeChatLocationDispatcherClass(cls)
            }
            reportRuntimeOnce(
                "dynamic-class:$className",
                "Installed dynamic hooks for $className from ${cls.classLoader}"
            )
        } catch (e: Throwable) {
            dynamicallyHookedClasses.remove(cls)
            module.log(Log.WARN, tag, "Cannot install dynamic hooks for $className: ${e.message}")
        }
    }

    private fun hookWeChatSdk() {
        hookWeChatLocationDispatcher()
        hookWeChatLocationWrapper()
        hookWeChatBizLocation()
        hookWeChatLocationCallbacks()
        hookWeChatLocationInfoArguments()
    }

    private fun hookWeChatActivityLifecycle() {
        val method = Activity::class.java.getDeclaredMethod("onCreate", Bundle::class.java)
        installHook(method, "android.app.Activity.onCreate") { chain ->
            val result = chain.proceed()
            val activity = chain.thisObject as? Activity
            if (activity?.javaClass?.name == "com.tencent.mm.plugin.location_soso.SoSoProxyUI") {
                activity.javaClass.classLoader?.let(::installWeChatHooksFromRuntimeLoader)
            }
            result
        }
    }

    private fun installWeChatHooksFromRuntimeLoader(runtimeClassLoader: ClassLoader) {
        listOf<Pair<String, (Class<*>) -> Unit>>(
            "cx0.h" to ::hookWeChatLocationDispatcherClass,
            "com.tencent.mm.plugin.location.ui.impl.j1" to {
                hookWeChatLocationCallbackClass(it.name, it)
            },
            "com.tencent.mm.plugin.location.ui.impl.t2" to {
                hookWeChatLocationCallbackClass(it.name, it)
            },
            "com.tencent.mm.plugin.location.ui.impl.p2" to ::hookWeChatLocationInfoArgumentClass,
        ).forEach { (className, installer) ->
            try {
                val cls = Class.forName(className, false, runtimeClassLoader)
                installer(cls)
                reportRuntimeOnce(
                    "wechat-runtime-loader:$className",
                    "Installed WeChat hooks for $className from SoSoProxyUI ClassLoader"
                )
            } catch (e: Throwable) {
                Log.w(runtimeTag, "Cannot load $className from SoSoProxyUI ClassLoader", e)
            }
        }
    }

    private fun hookWeChatLocationDispatcher() {
        val cls = findClass("cx0.h") ?: return
        hookWeChatLocationDispatcherClass(cls)
    }

    private fun hookWeChatLocationDispatcherClass(cls: Class<*>) {
        cls.declaredMethods
            .filter {
                it.name == "c" &&
                    Modifier.isStatic(it.modifiers) &&
                    it.parameterTypes.size == 9 &&
                    it.parameterTypes[1] == java.lang.Boolean.TYPE &&
                    it.parameterTypes[2] == java.lang.Double.TYPE &&
                    it.parameterTypes[3] == java.lang.Double.TYPE
            }
            .forEach { method ->
                installHook(method, "cx0.h.c") { chain ->
                    if (PreferencesUtil.getIsPlaying() != true) {
                        return@installHook chain.proceed()
                    }

                    LocationUtil.updateLocation()
                    val newArgs = chain.args.toTypedArray()
                    // cx0.h.c receives latitude then longitude, and dispatches them as longitude,
                    // latitude to cx0.c.onGetLocation.
                    newArgs[1] = true
                    newArgs[2] = LocationUtil.latitude
                    newArgs[3] = LocationUtil.longitude
                    val result = chain.proceed(newArgs)
                    reportRuntimeOnce(
                        "wechat-location-dispatch",
                        "Spoofed WeChat cx0.h.c location dispatch"
                    )
                    result
                }
                reportRuntimeOnce(
                    "wechat-location-dispatch-installed",
                    "Installed WeChat cx0.h.c location dispatch hook"
                )
            }

        cls.declaredMethods
            .filter {
                it.name in setOf("f", "g") &&
                    !Modifier.isStatic(it.modifiers) &&
                    it.parameterTypes.size == 1 &&
                    it.returnType == java.lang.Void.TYPE
            }
            .forEach { method ->
                installHook(method, "cx0.h.${method.name}") { chain ->
                    if (PreferencesUtil.getIsPlaying() != true) {
                        return@installHook chain.proceed()
                    }

                    val callback = chain.getArg(0) ?: return@installHook chain.proceed()
                    val callbackMethod = callback.javaClass.methods.firstOrNull {
                        it.name == "onGetLocation" &&
                            it.parameterTypes.size == 7 &&
                            it.parameterTypes[0] == java.lang.Boolean.TYPE &&
                            it.parameterTypes[1] == java.lang.Float.TYPE &&
                            it.parameterTypes[2] == java.lang.Float.TYPE
                    } ?: callback.javaClass.declaredMethods.firstOrNull {
                        it.name == "onGetLocation" && it.parameterTypes.size == 7
                    } ?: return@installHook chain.proceed()

                    LocationUtil.updateLocation()
                    callbackMethod.isAccessible = true
                    callbackMethod.invoke(
                        callback,
                        true,
                        LocationUtil.longitude.toFloat(),
                        LocationUtil.latitude.toFloat(),
                        0,
                        0.0,
                        0.0,
                        0.0
                    )
                    reportRuntimeOnce(
                        "wechat-location-cache:${method.name}",
                        "Spoofed WeChat cx0.h.${method.name} cached location"
                    )
                    null
                }
            }
    }

    private fun hookWeChatLocationCallbacks() {
        listOf(
            "com.tencent.mm.plugin.location.ui.impl.j1",
            "com.tencent.mm.plugin.location.ui.impl.t2",
        ).forEach { className ->
            val cls = findClass(className) ?: return@forEach
            hookWeChatLocationCallbackClass(className, cls)
        }
    }

    private fun hookWeChatLocationCallbackClass(className: String, cls: Class<*>) {
        cls.declaredMethods
            .filter {
                it.name == "onGetLocation" &&
                    it.parameterTypes.size == 7 &&
                    it.parameterTypes[0] == java.lang.Boolean.TYPE &&
                    it.parameterTypes[1] == java.lang.Float.TYPE &&
                    it.parameterTypes[2] == java.lang.Float.TYPE
            }
            .forEach { method ->
                val label = "$className.onGetLocation"
                val installed = installHook(method, label) { chain ->
                    if (PreferencesUtil.getIsPlaying() != true) {
                        return@installHook chain.proceed()
                    }

                    LocationUtil.updateLocation()
                    val newArgs = chain.args.toTypedArray()
                    // WeChat's callback order is longitude, latitude.
                    newArgs[1] = LocationUtil.longitude.toFloat()
                    newArgs[2] = LocationUtil.latitude.toFloat()
                    val result = chain.proceed(newArgs)
                    reportRuntimeOnce(
                        "wechat-location-callback:$className",
                        "Spoofed WeChat location callback $label"
                    )
                    result
                }
                if (installed) {
                    reportRuntimeOnce(
                        "wechat-location-callback-installed:$className",
                        "Installed WeChat location callback hook $label"
                    )
                }
            }
    }

    private fun hookWeChatLocationInfoArguments() {
        val cls = findClass("com.tencent.mm.plugin.location.ui.impl.p2") ?: return
        hookWeChatLocationInfoArgumentClass(cls)
    }

    private fun hookWeChatLocationInfoArgumentClass(cls: Class<*>) {
        val className = cls.name
        cls.declaredMethods
            .filter {
                it.name == "u" &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0].name ==
                    "com.tencent.mm.plugin.location.model.LocationInfo"
            }
            .forEach { method ->
                installHook(method, "$className.u") { chain ->
                    val changed = spoofWeChatLocationInfo(chain.getArg(0))
                    if (changed) {
                        reportRuntimeOnce(
                            "wechat-location-info-argument",
                            "Spoofed WeChat LocationInfo before map update"
                        )
                    }
                    chain.proceed()
                }
            }
    }

    private fun tryHookLocationClass(className: String) {
        try {
            val cls = Class.forName(className, false, classLoader)
            tryHookLocationClass(className, cls)
        } catch (e: Throwable) {
            module.log(Log.DEBUG, tag, "$className not found in this process, skipping")
        }
    }

    private fun tryHookLocationClass(className: String, cls: Class<*>) {
        var hooked = 0
        listOf("getLatitude", "getLongitude", "latitude", "longitude").forEach { methodName ->
            try {
                val method = generateSequence(cls as Class<*>?) { it.superclass }
                    .mapNotNull { current ->
                        current.declaredMethods.firstOrNull {
                            it.name == methodName && it.parameterTypes.isEmpty()
                        }
                    }
                    .firstOrNull()
                    ?: cls.methods.firstOrNull {
                        it.name == methodName && it.parameterTypes.isEmpty()
                    }
                    ?: throw NoSuchMethodException("$className.$methodName()")

                val installed = installHook(method, "$className.$methodName") { chain ->
                    val original = chain.proceed()
                    if (PreferencesUtil.getIsPlaying() != true) return@installHook original

                    LocationUtil.updateLocation()
                    val fakeValue = when (methodName) {
                        "getLatitude", "latitude" -> LocationUtil.latitude
                        "getLongitude", "longitude" -> LocationUtil.longitude
                        else -> return@installHook original
                    }
                    val replacement = when (method.returnType) {
                        java.lang.Double.TYPE, java.lang.Double::class.java -> fakeValue
                        java.lang.Float.TYPE, java.lang.Float::class.java -> fakeValue.toFloat()
                        else -> {
                            module.log(
                                Log.WARN,
                                tag,
                                "Cannot fake $className.$methodName: unsupported return type ${method.returnType.name}"
                            )
                            return@installHook original
                        }
                    }

                    reportRuntimeOnce(
                        "getter:$className.$methodName",
                        "Spoofed $className.$methodName"
                    )
                    replacement
                }
                if (installed) hooked++
            } catch (e: Throwable) {
                module.log(Log.WARN, tag, "Cannot hook $className.$methodName: ${e.message}")
            }
        }
        if (hooked > 0) {
            module.log(Log.INFO, tag, "Hooked $className ($hooked methods)")
            Log.i(runtimeTag, "Installed $className getter hooks ($hooked)")
        }
    }

    private fun hookCoordinateSetters(className: String) {
        val cls = findClass(className) ?: return
        hookCoordinateSetters(className, cls)
    }

    private fun hookCoordinateSetters(className: String, cls: Class<*>) {
        val methods = generateSequence(cls as Class<*>?) { it.superclass }
            .flatMap { it.declaredMethods.asSequence() }
            .filter {
                it.name in setOf("setLatitude", "setLongitude") &&
                    it.parameterTypes.size == 1 &&
                    isCoordinateType(it.parameterTypes[0])
            }
            .distinct()
            .toList()

        methods.forEach { method ->
            val label = "$className.${method.name}"
            installHook(method, label) { chain ->
                if (PreferencesUtil.getIsPlaying() != true) return@installHook chain.proceed()

                LocationUtil.updateLocation()
                val value = if (method.name == "setLatitude") {
                    LocationUtil.latitude
                } else {
                    LocationUtil.longitude
                }
                val newArgs = chain.args.toTypedArray()
                newArgs[0] = convertCoordinate(value, method.parameterTypes[0])
                val result = chain.proceed(newArgs)
                spoofAmapLocation(chain.thisObject)
                reportRuntimeOnce("setter:$label", "Spoofed $label")
                result
            }
        }
    }

    private fun hookAmapConstructors(className: String) {
        val cls = findClass(className) ?: return
        hookAmapConstructors(className, cls)
    }

    private fun hookAmapConstructors(className: String, cls: Class<*>) {
        cls.declaredConstructors.forEach { constructor ->
            installHook(constructor, "$className.<init>") { chain ->
                val result = chain.proceed()
                spoofAmapLocation(result ?: chain.thisObject)
                result
            }
        }
    }

    private fun hookAmapLocationArguments(className: String, methodNames: Set<String>) {
        val cls = findClass(className) ?: return
        hookAmapLocationArguments(className, methodNames, cls)
    }

    private fun hookAmapLocationArguments(
        className: String,
        methodNames: Set<String>,
        cls: Class<*>
    ) {
        cls.declaredMethods
            .filter { it.name in methodNames && !Modifier.isAbstract(it.modifiers) }
            .forEach { method ->
                val label = "$className.${method.name}"
                installHook(method, label) { chain ->
                    val changed = chain.args.count(::spoofAmapLocation)
                    if (changed > 0) {
                        reportRuntimeOnce("callback:$label", "Spoofed AMap callback $label")
                    }
                    chain.proceed()
                }
            }
    }

    private fun hookWeChatLocationWrapper() {
        val className = "com.tencent.mm.pluginsdk.location.Location"
        val cls = findClass(className) ?: return
        hookWeChatLocationWrapperClass(cls)

        val locationView = findClass("com.tencent.mm.pluginsdk.location.LocationView") ?: return
        hookWeChatLocationViewClass(locationView)
    }

    private fun hookWeChatLocationWrapperClass(cls: Class<*>) {
        val className = cls.name
        cls.declaredConstructors.forEach { constructor ->
            installHook(constructor, "$className.<init>") { chain ->
                val newArgs = chain.args.toTypedArray()
                if (PreferencesUtil.getIsPlaying() == true &&
                    constructor.parameterTypes.size >= 2 &&
                    constructor.parameterTypes[0] == java.lang.Float.TYPE &&
                    constructor.parameterTypes[1] == java.lang.Float.TYPE
                ) {
                    LocationUtil.updateLocation()
                    newArgs[0] = LocationUtil.latitude.toFloat()
                    newArgs[1] = LocationUtil.longitude.toFloat()
                }
                val result = chain.proceed(newArgs)
                if (spoofWeChatLocation(result ?: chain.thisObject)) {
                    reportRuntimeOnce(
                        "wechat-wrapper-constructor",
                        "Spoofed WeChat SDK Location constructor"
                    )
                }
                result
            }
        }
    }

    private fun hookWeChatLocationViewClass(locationView: Class<*>) {
        locationView.declaredMethods
            .filter { it.name == "getLocation" && it.parameterTypes.isEmpty() }
            .forEach { method ->
                installHook(method, "LocationView.getLocation") { chain ->
                    val result = chain.proceed()
                    if (spoofWeChatLocation(result)) {
                        reportRuntimeOnce(
                            "wechat-location-view",
                            "Spoofed WeChat LocationView.getLocation result"
                        )
                    }
                    result
                }
            }
    }

    private fun hookWeChatBizLocation() {
        val cls = findClass("com.tencent.pigeon.biz.BizUserLocationInfo") ?: return
        hookWeChatBizLocationClass(cls)
    }

    private fun hookWeChatBizLocationClass(cls: Class<*>) {
        val className = cls.name
        cls.declaredConstructors.forEach { constructor ->
            installHook(constructor, "$className.<init>") { chain ->
                val newArgs = chain.args.toTypedArray()
                if (PreferencesUtil.getIsPlaying() == true &&
                    constructor.parameterTypes.size >= 2 &&
                    isDoubleType(constructor.parameterTypes[0]) &&
                    isDoubleType(constructor.parameterTypes[1])
                ) {
                    LocationUtil.updateLocation()
                    newArgs[0] = LocationUtil.longitude
                    newArgs[1] = LocationUtil.latitude
                }
                val result = chain.proceed(newArgs)
                if (spoofWeChatBizLocation(result ?: chain.thisObject)) {
                    reportRuntimeOnce(
                        "wechat-biz-constructor",
                        "Spoofed BizUserLocationInfo constructor"
                    )
                }
                result
            }
        }

        mapOf(
            "getLongitude" to false,
            "component1" to false,
            "getLatitude" to true,
            "component2" to true,
        ).forEach { (methodName, isLatitude) ->
            cls.declaredMethods
                .filter {
                    it.name == methodName &&
                        it.parameterTypes.isEmpty() &&
                        isCoordinateType(it.returnType)
                }
                .forEach { method ->
                    installHook(method, "$className.$methodName") { chain ->
                        val original = chain.proceed()
                        if (PreferencesUtil.getIsPlaying() != true) {
                            return@installHook original
                        }
                        LocationUtil.updateLocation()
                        val value = if (isLatitude) {
                            LocationUtil.latitude
                        } else {
                            LocationUtil.longitude
                        }
                        reportRuntimeOnce(
                            "wechat-biz-getter:$methodName",
                            "Spoofed BizUserLocationInfo.$methodName"
                        )
                        convertCoordinate(value, method.returnType)
                    }
                }
        }
    }

    private fun spoofAmapLocation(value: Any?): Boolean {
        if (value == null ||
            PreferencesUtil.getIsPlaying() != true ||
            !value.javaClass.name.startsWith("com.amap.location.")
        ) {
            return false
        }

        LocationUtil.updateLocation()
        var changed = 0
        changed += setCoordinateFields(
            value,
            setOf("latitude", "mLatitude", "gcjLatitude"),
            LocationUtil.latitude
        )
        changed += setCoordinateFields(
            value,
            setOf("longitude", "mLongitude", "gcjLongitude"),
            LocationUtil.longitude
        )
        if (changed > 0) {
            reportRuntimeOnce(
                "amap-fields:${value.javaClass.name}",
                "Spoofed AMap fields on ${value.javaClass.name}"
            )
        }
        return changed > 0
    }

    private fun spoofWeChatLocation(value: Any?): Boolean {
        if (value == null ||
            PreferencesUtil.getIsPlaying() != true ||
            value.javaClass.name != "com.tencent.mm.pluginsdk.location.Location"
        ) {
            return false
        }

        LocationUtil.updateLocation()
        val latitude = setCoordinateFields(value, setOf("d"), LocationUtil.latitude)
        val longitude = setCoordinateFields(value, setOf("e"), LocationUtil.longitude)
        return latitude + longitude > 0
    }

    private fun spoofWeChatBizLocation(value: Any?): Boolean {
        if (value == null ||
            PreferencesUtil.getIsPlaying() != true ||
            value.javaClass.name != "com.tencent.pigeon.biz.BizUserLocationInfo"
        ) {
            return false
        }

        LocationUtil.updateLocation()
        val longitude = setCoordinateFields(value, setOf("longitude"), LocationUtil.longitude)
        val latitude = setCoordinateFields(value, setOf("latitude"), LocationUtil.latitude)
        return longitude + latitude > 0
    }

    private fun spoofWeChatLocationInfo(value: Any?): Boolean {
        if (value == null ||
            PreferencesUtil.getIsPlaying() != true ||
            value.javaClass.name != "com.tencent.mm.plugin.location.model.LocationInfo"
        ) {
            return false
        }

        LocationUtil.updateLocation()
        val latitude = setCoordinateFields(value, setOf("e"), LocationUtil.latitude)
        val longitude = setCoordinateFields(value, setOf("f"), LocationUtil.longitude)
        return latitude + longitude > 0
    }

    private fun setCoordinateFields(value: Any, names: Set<String>, coordinate: Double): Int {
        var changed = 0
        generateSequence(value.javaClass as Class<*>?) { it.superclass }.forEach { current ->
            current.declaredFields
                .filter { it.name in names && isCoordinateType(it.type) }
                .forEach { field ->
                    try {
                        field.isAccessible = true
                        when (field.type) {
                            java.lang.Double.TYPE -> field.setDouble(value, coordinate)
                            java.lang.Float.TYPE -> field.setFloat(value, coordinate.toFloat())
                            java.lang.Double::class.java -> field.set(value, coordinate)
                            java.lang.Float::class.java -> field.set(value, coordinate.toFloat())
                        }
                        changed++
                    } catch (e: Throwable) {
                        module.log(
                            Log.WARN,
                            tag,
                            "Cannot set ${current.name}.${field.name}: ${e.message}"
                        )
                    }
                }
        }
        return changed
    }

    private fun installHook(executable: Executable, label: String, hooker: Hooker): Boolean {
        if (!hookedExecutables.add(executable)) return false
        return try {
            module.hook(executable).intercept(hooker)
            true
        } catch (e: Throwable) {
            hookedExecutables.remove(executable)
            module.log(Log.WARN, tag, "Cannot hook $label: ${e.message}")
            false
        }
    }

    private fun findClass(className: String): Class<*>? {
        return try {
            Class.forName(className, false, classLoader)
        } catch (_: Throwable) {
            module.log(Log.DEBUG, tag, "$className not found in this process, skipping")
            null
        }
    }

    private fun isCoordinateType(type: Class<*>): Boolean {
        return type == java.lang.Double.TYPE ||
            type == java.lang.Double::class.java ||
            type == java.lang.Float.TYPE ||
            type == java.lang.Float::class.java
    }

    private fun isDoubleType(type: Class<*>): Boolean {
        return type == java.lang.Double.TYPE || type == java.lang.Double::class.java
    }

    private fun convertCoordinate(value: Double, type: Class<*>): Any {
        return if (type == java.lang.Float.TYPE || type == java.lang.Float::class.java) {
            value.toFloat()
        } else {
            value
        }
    }

    private fun reportRuntimeOnce(key: String, message: String) {
        if (runtimeMarkers.add(key)) {
            Log.i(runtimeTag, message)
        }
    }
}
