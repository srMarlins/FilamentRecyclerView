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

    private var asset: FilamentAsset? = null
    var scene: Scene? = null

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

    fun destroy() {
        destroyModel()
        engine.lightManager.destroy(light)
        engine.destroyEntity(light)
        engine.destroyIndirectLight(indirectLight)
        engine.destroySkybox(skybox)

        /*scenes.forEach {
            engine.destroyScene(it.value.scene)
            assetLoader.destroyAsset(it.value.asset)
        }*/

        assetLoader.destroy()
        resourceLoader.destroy()

        engine.destroy()
    }

    fun loadModelGlb(
        buffer: Buffer
    ): FilamentAsset {
        if (!engine.isValid) throw IllegalStateException("Engine must be created before loading models")
        destroyModel()
        val asset = assetLoader.createAssetFromBinary(buffer)
        asset?.apply {
            resourceLoader.asyncBeginLoad(asset)
            asset.releaseSourceData()
        }
        this.asset = asset
        createScene(asset!!)
        return asset
    }

    fun transformToUnitCube(asset: FilamentAsset) {
        val tm = engine.transformManager
        val center = asset.boundingBox.center.let { v -> Float3(v[0], v[1], v[2]) }
        val halfExtent = asset.boundingBox.halfExtent.let { v -> Float3(v[0], v[1], v[2]) }
        val maxExtent = 2.0f * max(halfExtent)
        val scaleFactor = 2.0f / maxExtent
        val transform = scale(Float3(scaleFactor)) * translation(Float3(-center))
        tm.setTransform(tm.getInstance(asset.root), transpose(transform).toFloatArray())
    }

    private fun destroyModel() {
        resourceLoader.asyncCancelLoad()
        resourceLoader.evictResourceData()
        asset?.let { asset ->
            this.scene?.removeEntities(asset.entities)
            assetLoader.destroyAsset(asset)
            this.asset = null
            //this.animator = null
        }
    }

    private fun createScene(asset: FilamentAsset/*name: String, gltf: String*/) {
        val scene = engine.createScene()
        /*val asset = readCompressedAsset(this, gltf).let {
            val asset = loadModelGlb(assetLoader, resourceLoader, it)
            transformToUnitCube(engine, asset)
            asset
        }*/
        //scene.indirectLight = indirectLight
        //scene.skybox = skybox

        scene.addEntities(asset.entities)

        scene.addEntity(light)
        this.scene = scene

        //scenes[name] = ProductScene(engine, scene, asset)
    }
}
