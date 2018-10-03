package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.util.RotatingNode
import com.example.mende.kotlintestapp.util.toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_model_scene.*
import kotlinx.android.synthetic.main.activity_model_test_button_ar.*


/**
 * references:
 *
 * https://heartbeat.fritz.ai/build-you-first-android-ar-app-with-arcore-and-sceneform-in-5-minutes-af02dc56efd6
 *
 * -------------------------------------------------------------------------------------------------
 * Temporary: Is An Activity that gets initiated. If the intention is to mimic IOS app, this is the best way.
 * Concerns:
 *  - Activity maybe is kinda heavy to re-create whenever needed. But fragments are not necessarily the better option. Investigate.
 *
 */
class TestButtonARActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var arSceneView: ArSceneView
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false

    private var descriptionBubbleRenderable: ViewRenderable? = null
    private lateinit var descriptionBubble: DescriptionBubble
    private lateinit var bubbleNode : Node

//    ArSceneView directly. That one behaves like a default Android
//    View so you can use an onTouchListener and use a GestureDetector to
//    detect the gestures. But in this case you have to do rotation and
//    translation of your objects on your own.


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_test_button_ar)

        initResources()

        arFragment = sceneform_button_fragment as ArFragment
        // arSceneView = sceneform_button_fragment as ArSceneView
        // Adds a listener to the ARSceneView
        // Called before processing each frame

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
           arFragment.onUpdate(frameTime)
          onUpdate(frameTime)
         }

    // Set the onclick lister for our button
    // Change this string to point to the .sfb file of your choice :)
    floatingActionButton.setOnClickListener { addObject(Uri.parse("Cupcake.sfb")) }
    showFab(false)
}
    // itemDescriptionRenderable
    @SuppressLint("SetTextI18n")
    private fun initResources() {

        title_text.text = "Party Cupcake"
        item_cost.text = "$10.99"

        descriptionBubble = DescriptionBubble(this)

        descriptionBubble.onStartTapped = {
            // Can Add initiation stuff here later on
        }
        bubbleNode = Node()

        // create a xml renderable (asynchronous operation,
        // result is delivered to `thenAccept` method)
        ViewRenderable.builder()
                .setView(this, descriptionBubble)
                .build()
                .thenAccept {
                    it.isShadowReceiver = true
                    descriptionBubbleRenderable = it
                }
                .exceptionally { it.toast(this) }
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
    private fun onUpdate(frameTime: FrameTime) {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                showFab(isHitting)
            }
        }
        if(bubbleNode.isEnabled)
        {
            val cameraPosition = arFragment.arSceneView.scene.camera.worldPosition
            val cardPosition = bubbleNode.getWorldPosition()
            val direction = Vector3.subtract(cameraPosition, cardPosition)
            val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
            bubbleNode.setWorldRotation(lookRotation)
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
        val view = this.findViewById<View>(android.R.id.content)
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
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    return@exceptionally null
                }

        //TODO: If using obj, must also download sfb file, which I think is pretty big, Possible I am wrong, and we only need to download an sfb file without needing obj,mtl,jpg, in which case need to find a way to compress that
        //TODO: use this if planned to download a glTf, or glb on the fly, run time, if can compress, probably best option, Draco only does OBJ and PLY? I think
//        ModelRenderable.builder()
//                .setSource(fragment.context, RenderableSource.builder().setSource(
//                        fragment.context,
//                        model, //Uri.parse(GLTF_ASSET) when downloading
//                        RenderableSource.SourceType.GLTF2).build())
////   idk what this does vvvvvv letts find out !             .setScale(0.5f)  // Scale the original model to 50%.
////                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                .setRegistryId(model) //GLTF_ASSET instead of model when downlaoding
//                .build()
//                .thenAccept {
//                    addNodeToScene(fragment, anchor, it)
//                }
//                .exceptionally {
//                    Toast.makeText(this@TestButtonARActivity, "Could not fetch model from $model", Toast.LENGTH_SHORT).show()
//                    return@exceptionally null
//                }
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
    @SuppressLint("SetTextI18n")
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renDerable: ModelRenderable) {

        val anchorNode = AnchorNode(anchor)
        val rotatingNode = RotatingNode()
        val transformableNode = TransformableNode(fragment.transformationSystem)
        val transformableBubbleNode = TransformableNode(fragment.transformationSystem)
        // TransformableNode means the user to move, scale and rotate the model

        bubbleNode.setParent(transformableBubbleNode)
        bubbleNode.setEnabled(false)
        bubbleNode.setLocalPosition(Vector3(0f, .3f, 0f))

        ViewRenderable.builder()
                .setView(fragment.context, R.layout.model_description_view)
                .build()
                .thenAccept(
                        { renderable ->
                            bubbleNode.setRenderable(descriptionBubbleRenderable)
                            val textView = renderable.getView() as TextView
                            textView.setText("something different")
                        })
                .exceptionally(
                        { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                            return@exceptionally null })


        //transformableBubbleNode.renderable = descriptionBubbleRenderable
        transformableBubbleNode.scaleController.maxScale = 1f
        transformableBubbleNode.setParent(anchorNode)
        //TODO: Remove this renderable node, instead apply how SolarSystem applies 2D views
        //for ar button
        // maybe reference code to have an on click listener on food for description bubble
//        base.setRenderable(exampleLayoutRenderable);
//        Context c = this;
//        // Add  listeners etc here
//        View eView = exampleLayoutRenderable.getView();
//        eView.setOnTouchListener((v, event) -> {
//            Toast.makeText(
//                    c, "Location marker touched.", Toast.LENGTH_LONG)
//                    .show();

//        Node base = new Node();
//        base.setRenderable(andyRenderable);
//        Context c = this;
//        base.setOnTapListener((v, event) -> {
//            Toast.makeText(
//                    c, "Andy touched.", Toast.LENGTH_LONG)
//                    .show();
//        });
//        retur

        descriptionBubble.let {
            transformableBubbleNode.apply {
              //  localPosition = Vector3(0f, .3f, 0f)
                localScale = Vector3(.05f, .05f, .05f)
            }
        }
        bubbleNode.setEnabled(true)

        //transformableNode.renderable = renderable
        transformableNode.scaleController.maxScale = 1f
        transformableNode.setParent(anchorNode)


        //TODO: Pause the rotating node when being rotated by transformable node

        rotatingNode.renderable = renDerable
        rotatingNode.setParent(transformableNode)
        //rotatingNode.setParent(anchorNode)

        fragment.arSceneView.scene.addChild(anchorNode)
        transformableBubbleNode.select()
        //transformableNode.select()

        //TODO: After successful placement of item on FIRST access, remove floating button and display the choices
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