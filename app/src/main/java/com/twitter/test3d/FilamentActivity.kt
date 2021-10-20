package com.twitter.test3d

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.filament.utils.Utils
import com.twitter.test3d.filament.EngineHelper
import com.twitter.test3d.filament.ModelRecyclerAdapter
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.nio.ByteBuffer

class FilamentActivity : AppCompatActivity() {

    private lateinit var modelRecyclerView: RecyclerView
    private val glbRelay = BehaviorSubject.create<List<ByteBuffer>>()
    private val viewModel = ModelViewModel(
        glbRelay, listOf(
            "boombox.glb",
            "aerobatic_plane.glb",
            "CornellBox/cornellBox.glb",
            "emoji_heart.glb",
            "solar_system.glb",
            "TrailMeshSpell/greenEnergyBall.glb"
        )
    )

    private val adapter = ModelRecyclerAdapter()

    init {
        Utils.init()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EngineHelper.STATIC_HELPER.create()
        setContentView(R.layout.filament_main)
        modelRecyclerView = findViewById(R.id.model_list)
        modelRecyclerView.layoutManager =
            LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        modelRecyclerView.adapter = adapter
        glbRelay.subscribe {
            adapter.data = it
        }
    }

    override fun onDestroy() {
        viewModel.clear()
        super.onDestroy()
    }


}