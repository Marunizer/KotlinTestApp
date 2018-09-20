package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_model_test_button_ar.*


/**
 * references:
 *
 * https://heartbeat.fritz.ai/build-you-first-android-ar-app-with-arcore-and-sceneform-in-5-minutes-af02dc56efd6
 *
 */
class TestButtonARFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private var containerActivity : FragmentActivity? = null
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false
    lateinit var sceneContext : Context

    companion object {

        fun newInstance(): TestButtonARFragment {
            return TestButtonARFragment()
        }
    }

    //used whenever we want to pass in data to fragment from other sources upon creation
    override fun onAttach(context: Context) {
        super.onAttach(context)
        sceneContext = context
        containerActivity = activity

        //SAMPLE ON HOW TO ATTAIN RESOURCES
//        // Get rage face names and descriptions.
//        val resources = context!!.resources
//        names = resources.getStringArray(R.array.names)
//        descriptions = resources.getStringArray(R.array.descriptions)
//        urls = resources.getStringArray(R.array.urls)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.activity_model_test_button_ar, container, false)

//        containerActivity = activity
        return view
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = ArFragment()
        getActivity()!!.getSupportFragmentManager().beginTransaction()
                .replace(R.id.child_fragment_container, arFragment,"findThisFragment")
                .addToBackStack(null)
                .commit()

        val myText : TextView = test_text as TextView

        myText.setTextColor(R.color.background_material_light)

        // Adds a listener to the ARSceneView
        // Called before processing each frame
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
            onUpdate()
        }

        // Set the onclick lister for our button
        // Change this string to point to the .sfb file of your choice :)
        floatingActionButton.setOnClickListener { addObject(Uri.parse("Hamburger.sfb")) }
        showFab(false)


    }


    override fun onStart() {
        super.onStart()

//        arFragment = sceneContext.supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

//        arFragment = sceneform_fragment as ArFragment
//
//        // Adds a listener to the ARSceneView
//        // Called before processing each frame
//        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
//            arFragment.onUpdate(frameTime)
//            onUpdate()
//        }
//
//        // Set the onclick lister for our button
//        // Change this string to point to the .sfb file of your choice :)
//        floatingActionButton.setOnClickListener { addObject(Uri.parse("Hamburger.sfb")) }
//        showFab(false)

    }

    // Simple function to show/hide our FAB
    @SuppressLint("RestrictedApi")
    private fun showFab(enabled: Boolean) {
        if (enabled) {
            floatingActionButton.isEnabled = true
            floatingActionButton.visibility = View.VISIBLE
        } else {
            floatingActionButton.isEnabled = false
            floatingActionButton.visibility = View.GONE
        }
    }

    // Updates the tracking state
    private fun onUpdate() {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                showFab(isHitting)
            }
        }

    }

    // Performs frame.HitTest and returns if a hit is detected
    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    // Makes use of ARCore's camera state and returns true if the tracking state has changed
    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame.camera.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    // Simply returns the center of the screen
    private fun getScreenCenter(): Point {
        val view = containerActivity!!.findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    /**
     * @param model The Uri of our 3D sfb file
     *
     * This method takes in our 3D model and performs a hit test to determine where to place it
     */
    private fun addObject(model: Uri) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor from the hit test
     * @param model our 3D model of choice
     *
     * Uses the ARCore anchor from the hitTest result and builds the Sceneform nodes.
     * It starts the asynchronous loading of the 3D model using the ModelRenderable builder.
     */
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept {
                    addNodeToScene(fragment, anchor, it)
                }
                .exceptionally {
                    Toast.makeText(containerActivity, "Error", Toast.LENGTH_SHORT).show()
                    return@exceptionally null
                }
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor
     * @param renderable our model created as a Sceneform Renderable
     *
     * This method builds two nodes and attaches them to our scene
     * The Anchor nodes is positioned based on the pose of an ARCore Anchor. They stay positioned in the sample place relative to the real world.
     * The Transformable node is our Model
     * Once the nodes are connected we select the TransformableNode so it is available for interactions
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        // TransformableNode means the user to move, scale and rotate the model
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    override fun onPause() {
        super.onPause()
        if (arFragment != null)
             arFragment.onPause()
    }

    override fun onResume() {
        super.onResume()
        if(arFragment != null)
             arFragment.onResume()
    }
}