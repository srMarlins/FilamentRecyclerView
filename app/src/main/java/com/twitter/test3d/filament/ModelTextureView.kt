package com.twitter.test3d.filament

import android.content.Context
import android.util.AttributeSet
import android.view.Choreographer
import android.view.TextureView
import com.google.android.filament.Engine
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import java.nio.Buffer

class ModelTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs) {

    /**
     * [R, G, B, A] float values between 0-1
     * @see ModelViewer currently doesn't handle alpha values properly,
     *  so this value will end up being ignored until fixed
     */
    private val backgroundColor = floatArrayOf(.0f, .0f, .0f, .0f)

    /**
     * Controls the necessary components to view 3d models
     *
     * Currently this relies on the view being destroyed whenever the view is detached.
     * If any actions on the modelViewer cause the engine to be used after detachment,
     *   it will throw an IllegalStateException
     *
     * @see <a href="https://github.com/google/filament/issues/4724">Github Issue</a>
     */
    private var modelViewer: ModelViewer

    private val choreographer = Choreographer.getInstance()

    init {
        modelViewer = createModelViewer()
        // We want to dispatch touch events to the modelViewer to handle rotation of the object
        setOnTouchListener(modelViewer)
    }

    /**
     * Load a glb model, represented as a Buffer, centered and scaled into the current view
     */
    fun loadGlb(buffer: Buffer) {
        // This will asynchronously parse/prepare the glb file & load it into the display
        modelViewer.loadModelGlb(buffer)
        // This will perform a normalized scaling of the model to fit the view
        modelViewer.transformToUnitCube()
        choreographer.postFrameCallback(frameCallback)
    }

    /**
     * Sets the background color
     */
    fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        backgroundColor[0] = r
        backgroundColor[1] = g
        backgroundColor[2] = b
        backgroundColor[3] = a
        modelViewer.setClearOptions()
    }

    fun clear() {
        choreographer.removeFrameCallback(frameCallback)
    }

    /**
     * Sets the clear options for the renderer within the ModelViewer
     * using the current background color to color the cleared pixels
     */
    private fun ModelViewer.setClearOptions() {
        renderer.clearOptions = renderer.clearOptions.apply {
            clearColor = backgroundColor
            clear = true
        }
    }

    /**
     * Creates the ModelViewer with the current backgroundColor
     */
    private fun createModelViewer() =
        ModelViewer(this,
            Engine.create(),
            UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
                isOpaque = false
            }
        ).apply {
            // the following code is necessary for transparency,
            // remove if we need a specific background that isn't dictated by the parent views
            view.blendMode = View.BlendMode.TRANSLUCENT
            scene.skybox = null
            setClearOptions()
        }

    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    val elapsedTimeSeconds = (currentTime - startTime).toDouble() / 1_000_000_000
                    applyAnimation(0, elapsedTimeSeconds.toFloat())
                }
                updateBoneMatrices()
            }
            choreographer.postFrameCallback(this)
            modelViewer.render(currentTime)
        }
    }
}