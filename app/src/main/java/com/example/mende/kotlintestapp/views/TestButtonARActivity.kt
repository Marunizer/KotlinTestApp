package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.ItemCircleViewAdapter
import com.example.mende.kotlintestapp.objects.ItemCircle
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.AnimatedNode
import com.example.mende.kotlintestapp.util.MenuListHolder
import com.example.mende.kotlintestapp.util.RotatingNode
import com.example.mende.kotlintestapp.util.toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
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

    private val TAG = TestButtonARActivity::class.java.simpleName
    private lateinit var arFragment: ArFragment
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false
    private var firstTimeWinkyFace : Boolean = true
    private var nodeAllocated : Boolean = false

    private var descriptionBubbleRenderable: ViewRenderable? = null
    private lateinit var descriptionBubble: DescriptionBubble
    lateinit var anchorNode: AnchorNode
    lateinit var transformableNode : TransformableNode
    lateinit var rotatingNode : RotatingNode
    lateinit var animatedNode: AnimatedNode
    private lateinit var bubbleNode : Node
    private lateinit var mAdapter: ItemCircleViewAdapter
    private lateinit var mHandler: Handler
    private var restaurantMenuItem : RestaurantMenuItem? = null
    private var testData: ArrayList<ItemCircle?> = ArrayList()

    private lateinit var restaurantKey: String
    private var currentIndex : Long = 0
    private val MIN_OPENGL_VERSION = 3.0

    private val scaleMin : Float = 0.0f
    private val scaleMax : Float = 1.0f
    private var currentScale: Float = 0.0f

//    ArSceneView directly. That one behaves like a default Android
//    View so you can use an onTouchListener and use a GestureDetector to
//    detect the gestures. But in this case you have to do rotation and
//    translation of your objects on your own.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_test_button_ar)

        if (!checkIsSupportedDeviceOrFinish(this)) { return }

        arFragment = sceneform_button_fragment as ArFragment
        restaurantKey = intent.getStringExtra("card_key")
        currentIndex = intent.getLongExtra("current_index", 112)

        initResources()

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
           arFragment.onUpdate(frameTime)
          onUpdate(frameTime)
         }

        Log.d(TAG, "FloatingButton : expected from id: $currentIndex menu item: ${restaurantMenuItem?.name}")
        floatingActionButton.setOnClickListener { addObject(Uri.parse("${restaurantMenuItem?.name}.sfb"),restaurantMenuItem?.name) }
        showFab(false)
    }

    // itemDescriptionRenderable
    @SuppressLint("SetTextI18n")
    private fun initResources() {

        addTestData(getItemList())

        // Initialize
        mHandler = Handler()
        bubbleNode = Node()

        title_text_ar.text = restaurantMenuItem?.name
        item_cost_ar.text = "$${restaurantMenuItem?.cost}"
        circle_item_ar_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        descriptionBubble = DescriptionBubble(this)

        descriptionBubble.onStartTapped = {
            // Can Add initiation stuff here later on or just delete
        }

        // Access the RecyclerView Adapter and load the data into it
        circle_item_ar_recycler_view.visibility = View.INVISIBLE
        mAdapter = ItemCircleViewAdapter(circle_item_ar_recycler_view, this, testData, true)
        { itemCircle : ItemCircle?-> onCircleClick(itemCircle) }
        circle_item_ar_recycler_view.adapter = mAdapter


        // create a xml renderable (asynchronous operation,
        // result is delivered to `thenAccept` method)
        ViewRenderable.builder()
                .setView(this, descriptionBubble)
                .build()
                .thenAccept {
                    it.isShadowReceiver = true
                    descriptionBubbleRenderable = it
                }
                .exceptionally { //TODO: delete on release
                     it.toast(this) }
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
            //onStart up, show or remove floating button that allocates model
            if (hitTestChanged) {
                if(firstTimeWinkyFace)
                    showFab(isHitting)
            }


            //Make the description bubble always face the camera
            if(bubbleNode.isEnabled)
            {
                val cameraPosition = arFragment.arSceneView.scene.camera.worldPosition
                val cardPosition = bubbleNode.getWorldPosition()
                val direction = Vector3.subtract(cameraPosition, cardPosition)
                val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
                bubbleNode.setWorldRotation(lookRotation)
            }

            if(nodeAllocated)
            {
                //Idk if we need this variable? it might be good enough to use curreentItem idk
                if(animatedNode.isSelected)
                {
                    //if animation has not finished , despite it being selected, continue !
                    if(!animatedNode.isFullSizeAnimationDone)
                    {
                        Log.d(TAG, "SCALING FUN: $currentScale")
                        //Want animation to last for .4 seconds. //1f(second) == 30frames
                        currentScale = currentScale + (1f/12f)

                        if(currentScale >= scaleMax) {
                            animatedNode.localScale = Vector3(scaleMax, scaleMax, scaleMax)
                            animatedNode.isFullSizeAnimationDone = true
                        }
                        else if(currentScale < scaleMax) {
                            animatedNode.localScale = Vector3(currentScale, currentScale, currentScale)
                        }
                    }
                }
                else if (!animatedNode.isSelected)
                {
                    // in this flow, we would deal with minimizing the node animation before removing and going to next node
                }
            }

            //whenever there is a transformation happening, disable rotation
            if (!firstTimeWinkyFace && nodeAllocated)
            {

                if(transformableNode.isTransforming) {
                    rotatingNode.onPauseAnimation()
                }
                else if (!rotatingNode.isAnimated()){
                    rotatingNode.onResumeAnimation()
                    arFragment.transformationSystem.selectionVisualizer.removeSelectionVisual(transformableNode)
                }
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
        val view = this.findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    private fun onCircleClick(circleView : ItemCircle?) {

        Log.d(TAG, "CLICKED: circleView = text: ${circleView?.restaurantMenuItem?.name}")
        title_text_ar.text = circleView?.restaurantMenuItem?.name
        item_cost_ar.text = circleView?.restaurantMenuItem?.cost
        restaurantMenuItem = circleView?.restaurantMenuItem
        currentIndex = circleView!!.id
        onChangeModel(restaurantMenuItem)
        //TODO: try to make a model on the fly programmatically using obj,mtl,jpg, NOTE: gltf models are the best for sceneform
    }

    private fun onChangeModel(restaurantMenuItem : RestaurantMenuItem?) {
        //replace with new restaurant menu item selected
        Log.d("MAGIC SPEAKER", "${restaurantMenuItem?.name} =?= ")

        if (restaurantMenuItem?.name != anchorNode.name)
        {
            nodeAllocated = false
            arFragment.arSceneView.scene.removeChild(anchorNode)
            addObject(Uri.parse("${restaurantMenuItem?.name}.sfb"), restaurantMenuItem?.name)
        }
    }

    /**
     * @param model The Uri of our 3D sfb file
     *
     * This method takes in our 3D model and performs a hit test to determine where to place it
     */
    private fun addObject(model: Uri, name: String?) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), model, name)
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
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri, name :String?) {
        ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()
                .thenAccept {
                    addNodeToScene(fragment, anchor, it, name)
                }
                .exceptionally {
                    //TODO: delete on release
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
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renDerable: ModelRenderable, name : String?) {

        anchorNode = AnchorNode(anchor)
        val rotatingNode = RotatingNode()
        val animatedNode = AnimatedNode()
        val transformableNode = TransformableNode(fragment.transformationSystem)

        // TransformableNode means the user to move, scale and rotate the model

        bubbleNode.setParent(anchorNode)
        bubbleNode.setEnabled(false)
        bubbleNode.setLocalPosition(Vector3(0f, .3f, 0f))

        ViewRenderable.builder()
                .setView(fragment.context, R.layout.model_description_view)
                .build()
                .thenAccept(
                        { renderable ->
                            bubbleNode.setRenderable(descriptionBubbleRenderable)
                        })
                .exceptionally(
                        //TODO: delete on release
                        { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                            return@exceptionally null })

        //TODO: make bubble show up when clicked
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
//        return


        bubbleNode.setEnabled(true)

        //transformableNode.scaleController.isEnabled = false
        transformableNode.setParent(anchorNode)

        //rotatingNode.renderable = renDerable
        rotatingNode.setParent(transformableNode)

        animatedNode.renderable = renDerable
        animatedNode.setParent(rotatingNode)
        animatedNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
        currentScale = scaleMin

        this.animatedNode = animatedNode
        this.rotatingNode = rotatingNode
        this.transformableNode = transformableNode

        fragment.arSceneView.scene.addChild(anchorNode)
        //val animation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)



        //set Transparency of model

        nodeAllocated = true

        if(firstTimeWinkyFace) {
            showFab(false)
            floatingActionButton.background = null
            if(circle_item_ar_recycler_view.visibility == View.GONE || circle_item_ar_recycler_view.visibility == View.INVISIBLE)
                circle_item_ar_recycler_view.visibility = View.VISIBLE
            firstTimeWinkyFace = false
        }
    }


    //fake function for sample item data
    fun getItemList() : ArrayList<RestaurantMenuItem>? {
        return MenuListHolder().getList(restaurantKey)
    }

    fun addTestData(itemList: ArrayList<RestaurantMenuItem>?) {

        var id : Long = 112

        for(item in itemList!!)
        {
            testData.add(ItemCircle(id,item))

            if (id == currentIndex)
                restaurantMenuItem = item

            id++
        }
    }

    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
            activity.finish()
            return false
        }
        return true
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