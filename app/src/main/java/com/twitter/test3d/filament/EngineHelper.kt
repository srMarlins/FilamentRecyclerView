package com.twitter.test3d.filament;

import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderLoader
import com.google.android.filament.utils.*
import java.nio.Buffer

class EngineHelper {
    lateinit var engine: Engine
    private lateinit var assetLoader: AssetLoader
    private lateinit var resourceLoader: ResourceLoader

    private lateinit var indirectLight: IndirectLight
    private lateinit var skybox: Skybox
    private var light: Int = 0

    val scenes = mutableMapOf<Int, ProductScene>()

    companion object {
        val STATIC_HELPER by lazy { EngineHelper() }
    }

    fun create() {
        engine = Engine.create()
        assetLoader = AssetLoader(engine, UbershaderLoader(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        light = EntityManager.get().create()
        val (r, g, b) = Colors.cct(6_000.0f)
        LightManager.Builder(LightManager.Type.SUN)
            .color(r, g, b)
            .intensity(70_000.0f)
            .direction(0.28f, -0.6f, -0.76f)
            .build(engine, light)
    }

    fun loadModelGlb(
        buffer: Buffer
    ): ProductScene {
        if (!engine.isValid) throw IllegalStateException("Engine must be created before loading models")
        // looks like we do not need destoryModel() in our case

        if (scenes.containsKey(buffer.hashCode()))
            return scenes[buffer.hashCode()]!!

        val scene = engine.createScene()
        val asset = loadGlb(assetLoader, resourceLoader, buffer)
        transformToUnitCube(engine, asset)

//        scene.indirectLight = indirectLight
//        scene.skybox = skybox

        scene.addEntities(asset.entities)

        scene.addEntity(light)
        scenes[buffer.hashCode()] = ProductScene(engine, scene, asset)

        return scenes[buffer.hashCode()]!!
    }

    fun destroy() {
        //destroyModel()
        engine.lightManager.destroy(light)
        engine.destroyEntity(light)
        engine.destroyIndirectLight(indirectLight)
        engine.destroySkybox(skybox)

        scenes.forEach {
            engine.destroyScene(it.value.scene)
            assetLoader.destroyAsset(it.value.asset)
        }

        assetLoader.destroy()
        resourceLoader.destroy()

        engine.destroy()
    }

//    private fun destroyModel() {
//        resourceLoader.asyncCancelLoad()
//        resourceLoader.evictResourceData()
//        asset?.let { asset ->
//            this.scene?.removeEntities(asset.entities)
//            assetLoader.destroyAsset(asset)
//            this.asset = null
//            //this.animator = null
//        }
//    }

    /**
     * Removes the transformation that was set up via transformToUnitCube.
     */
    fun clearRootTransform(asset: FilamentAsset) {
        asset?.let {
            val tm = engine.transformManager
            tm.setTransform(tm.getInstance(it.root), Mat4().toFloatArray())
        }
    }
}
