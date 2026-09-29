package com.morph.engine.util

import com.morph.engine.core.Game
import com.morph.engine.entities.Entity
import com.morph.engine.graphics.LoadedFont
import com.morph.engine.script.ConsoleScript
import com.morph.engine.script.EntityBehavior
import com.morph.engine.script.GameBehavior
import com.morph.engine.script.ScriptContainer
import com.morph.engine.script.debug.Console
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchEvent
import java.util.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.script.SimpleBindings

/**
 * Created on 7/5/2017.
 */
object ScriptUtils {
    private val supportedScriptEngines = HashMap<String, ScriptEngine>()
    private val scriptedEntities = HashMap<String, MutableList<Entity>>()
    private val bindings = SimpleBindings()
    @Volatile private var isRunning: Boolean = false
    private var isInitialized: Boolean = false
    private val initializationTask: Completable = ScriptUtils::load.toCompletable().subscribeOn(Schedulers.io()).cache()

    private val SCRIPT_ENGINE_EXTENSIONS = listOf("kts")

    fun init(game: Game) {
        initializationTask
                .andThen<WatchEvent<Path>>(Observable.create { runReactive(it) })
                .map { event -> Paths.get("scripts/").resolve(event.context()).fileName.toString() }
                .flatMapMaybe { getScriptBehaviorAsync(it) }
                .subscribe ({ behavior ->
                    when (behavior) {
                        is EntityBehavior -> scriptedEntities[behavior.name]?.forEach { entity -> entity.getComponent<ScriptContainer>()?.replaceBehavior(behavior.name!!, behavior) }
                        else -> game.replaceBehavior(behavior.name!!, behavior)
                    }
                })
    }

    private fun load(): Boolean {
        Console.out.println("Morph Script Engine " + Game.VERSION_STRING + " initializing... Please wait...")
        val kotlinEngine = KotlinJsr223DefaultScriptEngineFactory()
        //        PyScriptEngineFactory pythonEngine = new PyScriptEngineFactory();

        val manager = ScriptEngineManager()
        manager.registerEngineExtension("kts", kotlinEngine)
        //        manager.registerEngineExtension("py", pythonEngine);

        supportedScriptEngines["kts"] = kotlinEngine.scriptEngine
        //        supportedScriptEngines.put("py", pythonEngine.getScriptEngine());

        println(Paths.get(System.getProperty("user.dir") + "/src/main/resources/scripts/Test.kts").fileName)
        //        System.out.println("Python support test: " + supportedScriptEngines.get("py").getFactory().getLanguageName());

        isInitialized = true

        Console.out.println("[Morph Script Engine ${Game.VERSION_STRING}] Engine initialized.")

        return true
    }

    private fun runReactive(subscriber: ObservableEmitter<WatchEvent<Path>>) {
        isRunning = true

        try {
            FileSystems.getDefault().newWatchService().use { watchService ->
                Paths.get(System.getProperty("user.dir") + "/src/main/resources/scripts/").register(watchService, ENTRY_MODIFY)
                while (isRunning) {
                    val key = watchService.poll()
                    key?.let {
                        it.pollEvents().forEach { e ->
                            e?.let {
                                val kind = it.kind()

                                if (kind === OVERFLOW) return@forEach

                                (it as? WatchEvent<Path>)?.let { subscriber.onNext(it) }
                            }
                        }

                        it.reset()
                    }
                }
                subscriber.onComplete()
            }
        } catch (e: IOException) {
            subscriber.onError(e)
        }
    }

    fun stop() {
        if (!isRunning) return

        isRunning = false
    }

    fun register(scriptName: String, e: Entity) {
        if (scriptedEntities[scriptName] == null) {
            val eList = ArrayList<Entity>()
            eList.add(e)
            scriptedEntities[scriptName] = eList
        } else {
            scriptedEntities[scriptName]?.add(e)
        }
    }

    private fun genScript(script: String): String {
        return "import com.morph.engine.script.ConsoleScript\n" +
                "class CustomScript : ConsoleScript() {\n" +
                "    override fun run() = " + script + "\n" +
                "}\n" +
                "CustomScript()"
    }

    fun readScriptAsync(script: String, lang: String, console: Console): Completable {
        return getScriptEngine(lang).flatMapCompletable { engine ->
            Completable.fromCallable {
                readScriptDI(script, engine, console)
            }
        }
    }

    @Throws(ScriptException::class)
    private fun readScriptDI(script: String, engine: ScriptEngine, console: Console) {
        val sanitizedScript = script.filter { LoadedFont.isValidChar(it) }
        val behavior = engine.eval(genScript(sanitizedScript), bindings) as ConsoleScript
        behavior.setConsole(console)
        behavior.run()

        bindings.clear()
    }

    // TODO: When initialization is finished, return all requested script behaviors.
    private fun getScriptEngine(lang: String): Maybe<ScriptEngine> {
        return initializationTask.andThen(resolveScriptEngine(lang))
    }

    private fun resolveScriptEngine(lang: String): Maybe<ScriptEngine> {
        return if (supportedScriptEngines.containsKey(lang)) {
            Maybe.fromCallable { supportedScriptEngines[lang] }
        } else {
            Maybe.empty()
        }
    }

    fun getScriptBehaviorAsync(filename: String): Maybe<GameBehavior> {
        return getScriptEngine(getFileExtension(filename)).observeOn(Schedulers.single()).flatMap { scriptEngine -> Maybe.just(getScriptBehaviorDI<GameBehavior>(filename, scriptEngine)!!) }
    }

    private fun <T : GameBehavior> getScriptBehaviorDI(filename: String, engine: ScriptEngine): T? {
        var scriptSource = ""
        val fullFilename = System.getProperty("user.dir") + "/src/main/resources/scripts/" + filename

        val extension = fullFilename.substring(fullFilename.indexOf(".") + 1)

        try {
            scriptSource = IOUtils.getFileAsStringAbsolute(fullFilename)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var behavior: T? = null

        try {
            behavior = engine.eval(scriptSource, bindings) as? T
            if (behavior == null) {
                println("No result from eval, getting script variable")
                behavior = bindings["script"] as T
            }
            behavior.name = filename
        } catch (e: ScriptException) {
            e.printStackTrace()
        }

        bindings.clear()

        return behavior
    }

    private fun getFileExtension(filename: String): String {
        val fullFilename = System.getProperty("user.dir") + "/src/main/resources/scripts/" + filename
        return fullFilename.substring(fullFilename.indexOf(".") + 1)
    }

    fun isSupported(lang: String): Boolean {
        return SCRIPT_ENGINE_EXTENSIONS.contains(lang)
    }
}
