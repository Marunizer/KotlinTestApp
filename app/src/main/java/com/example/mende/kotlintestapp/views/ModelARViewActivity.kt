package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.ItemCircleViewAdapter
import com.example.mende.kotlintestapp.objects.ItemCircle
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.*
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_model_ar.*
import kotlinx.android.synthetic.main.model_description_view.view.*

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
 * TODO: Gotta take out some of the onUpdate() logic from the activity to the individual node classes
 */
class ModelARViewActivity : AppCompatActivity() {

    private val TAG = ModelARViewActivity::class.java.simpleName
    private lateinit var arFragment: ArFragment
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false
    private var firstTimeWinkyFace : Boolean = true
    private var nodeAllocated : Boolean = false

    private var descriptionBubbleRenderable: ViewRenderable? = null
    private lateinit var descriptionBubble: DescriptionBubble

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
    private var oldScale: Float = 1.0f

    private lateinit var currentAnchor : Anchor

    lateinit var oldAnchorNode: AnchorNode
    lateinit var anchorNode: AnchorNode
    lateinit var oldAnimatedNode : AnimatedNode
    lateinit var animatedNode: AnimatedNode
    lateinit var transformableNode : TransformableNode
    lateinit var rotatingNode : RotatingNode
    lateinit var transparentNode : Node
    var isTransparentNodePlaced : Boolean = false

    private lateinit var trackableGestureDetector: GestureDetector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_ar)

        if (!checkIsSupportedDeviceOrFinish(this)) { return }

        arFragment = sceneform_button_fragment as ArFragment
        restaurantKey = intent.getStringExtra("card_key")
        currentIndex = intent.getLongExtra("current_index", 112)

        initResources()

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
           arFragment.onUpdate(frameTime)
          onUpdate(frameTime)
         }

        //set special listener for adding/removing description bubble
        arFragment.arSceneView.scene.addOnPeekTouchListener(this::handleOnTouch)
                this.trackableGestureDetector = GestureDetector(this, MyGestureDetector())

        arFragment.transformationSystem.selectionVisualizer = BlanckSelectionVisualizer()

        floatingActionButton.setOnClickListener { addObject(Uri.parse("${restaurantMenuItem?.name}.sfb"),restaurantMenuItem?.name) }
        showFab(false)

        addTransparentObject(Uri.parse("${restaurantMenuItem?.name}.sfb"),restaurantMenuItem?.name)
    }


    // itemDescriptionRenderable
    @SuppressLint("SetTextI18n")
    private fun initResources() {

        // Initialize
        mHandler = Handler()
        bubbleNode = Node()
        oldAnimatedNode = AnimatedNode() //fake init
        transparentNode = Node()
        transparentNode.isEnabled = false

        addTestData(getItemList())

        item_text_ar.text = restaurantMenuItem?.name
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

    private fun showModel(enabled: Boolean) {

        if(transparentNode.isEnabled){

            if (enabled) {

                val frame = arFragment.arSceneView.arFrame
                val point = getScreenCenter()
                if (frame != null) {
                    val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
                    for (hit in hits) {
                        val trackable = hit.trackable
                        if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {

                            val tempAnchorNode = AnchorNode(hit.createAnchor())

                            transparentNode.localPosition = tempAnchorNode.localPosition
                            break
                        }
                    }
                }

//                val cameraPos = arFragment.arSceneView.scene.camera.worldPosition
//                val cameraForward = arFragment.arSceneView.scene.camera.forward
//                val position = Vector3.add(cameraPos, cameraForward.scaled(1.0f))
//
//                // Create an ARCore Anchor at the position.
//                Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
//                Anchor anchor = arSceneView.getSession().createAnchor(pose);
//
//                mAnchorNode = new AnchorNode(anchor);
//                mAnchorNode.setParent(arSceneView.getScene());


                //transparentNode.worldPosition

                arFragment.arSceneView.scene.addChild(transparentNode)
                isTransparentNodePlaced = true

            } else {
                arFragment.arSceneView.scene.removeChild(transparentNode)
                isTransparentNodePlaced = false
            }
        }
    }

    // Updates the tracking state
    @SuppressLint("RestrictedApi")
    private fun onUpdate(frameTime: FrameTime) {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            //onStart up, show or remove floating button that allocates model
            if (hitTestChanged) {
                if(firstTimeWinkyFace)
                {
                    showFab(isHitting)
                    showModel(isHitting)
                }
            }

            //move around node
            if(isTransparentNodePlaced){
                val frame = arFragment.arSceneView.arFrame
                val point = getScreenCenter()
                if (frame != null) {
                    val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
                    for (hit in hits) {
                        val trackable = hit.trackable
                        if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {

                            val tempAnchorNode = AnchorNode(hit.createAnchor())

                            transparentNode.localPosition = tempAnchorNode.localPosition
                            break
                        }
                    }
                }
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


            //nodeIsDown, safe to continue
            if(nodeAllocated)
            {
                //Re-sets expected view after objected is presented
                if(instructions_bubble.visibility == View.VISIBLE ) {
                    instructions_bubble.visibility = View.GONE
                    item_text_ar.visibility = View.VISIBLE
                    item_cost_ar.visibility = View.VISIBLE
                    order_ar_button.visibility = View.VISIBLE
                    share_ar_button.visibility = View.VISIBLE
                }


                //Idk if we need this variable? it might be good enough to use curreentItem idk
                if(animatedNode.isSelected)
                {
                    //if animation has not finished , despite it being selected, continue,
                    if(!animatedNode.isFullSizeAnimationDone) {
                        //Want animation to last for .4 seconds. //1f(second) == 30frames
                        currentScale = currentScale + (1f/12f)

                        if(currentScale >= scaleMax) {
                            animatedNode.localScale = Vector3(scaleMax, scaleMax, scaleMax)
                            transformableNode.localScale = Vector3(scaleMax, scaleMax, scaleMax)
                            animatedNode.isFullSizeAnimationDone = true
                            descriptionBubble.xml_btn.text = restaurantMenuItem?.description
                        }
                        else if(currentScale < scaleMax) {
                            animatedNode.localScale = Vector3(currentScale, currentScale, currentScale)
                            transformableNode.localScale = Vector3(currentScale, currentScale, currentScale)
                        }
                    }
                    else if(oldAnimatedNode.isRemoving) { minimizeItem() }
                }
                else if(!animatedNode.isSelected) {
                    if(!oldAnimatedNode.isRemoveAnimationDone) {
                        oldAnimatedNode.isRemoving = true
                        minimizeItem()
                    }
                }
            }
            else //node NOT allocated, meaning, new item was picked !!!
            {
                if(oldAnimatedNode.isInitialized && !oldAnimatedNode.isRemoveAnimationDone) {
                    oldAnimatedNode.isRemoving = true
                    minimizeItem()
                }
            }

            //whenever there is a transformation happening, disable rotation
            if (!firstTimeWinkyFace && nodeAllocated)
            {
                if(rotatingNode.isAnimatedByUser())
                {
                    if(transformableNode.isTransforming) {
                        rotatingNode.onPauseAnimation()
                    }
                    else if (!rotatingNode.isAnimated()){
                        rotatingNode.onResumeAnimation()
                    }
                }
            }
        }
    }

    private fun minimizeItem() {
        oldScale = oldScale - (1f/9f) //.3 seconds

        if (oldScale <= scaleMin)
        {
            oldAnimatedNode.localScale = Vector3(scaleMin,scaleMin,scaleMin)
            arFragment.arSceneView.scene.removeChild(oldAnchorNode)
            oldAnimatedNode.isRemoveAnimationDone = true
            oldAnimatedNode.isRemoving = false
            oldScale = scaleMax
            placeObject(arFragment, currentAnchor,Uri.parse("${restaurantMenuItem?.name}.sfb"), restaurantMenuItem?.name)
        }
        else if(oldScale > scaleMin)
        {
            animatedNode.localScale = Vector3(oldScale,oldScale,oldScale)
            transformableNode.localScale = Vector3(oldScale,oldScale,oldScale)
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
        item_text_ar.text = circleView?.restaurantMenuItem?.name
        item_cost_ar.text = circleView?.restaurantMenuItem?.cost
        restaurantMenuItem = circleView?.restaurantMenuItem
        currentIndex = circleView!!.id
        onChangeModel(restaurantMenuItem)
        //TODO: try to make a model on the fly programmatically using obj,mtl,jpg, NOTE: gltf models are the best for sceneform
    }

    private fun onChangeModel(restaurantMenuItem : RestaurantMenuItem?) {
        //replace with new restaurant menu item selected
        Log.d("MAGIC SPEAKER", "${restaurantMenuItem?.name} =?= ")

        if (restaurantMenuItem?.name != anchorNode.name) {
            oldAnchorNode = anchorNode
            animatedNode.isSelected = false
            oldAnimatedNode = animatedNode
            oldAnimatedNode.isInitialized = true
            currentAnchor = anchorNode.anchor
            nodeAllocated = false
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

    // Good starting point if want to try making object transparent again
    //currently changing the material only changes the bottom few places... not good enough, need whole model
    private fun addTransparentObject(model: Uri, name: String?) {

        ModelRenderable.builder()
                .setSource(arFragment.context, model)
                .build()
                .thenAccept {

                    transparentNode.renderable = it
                    transparentNode.isEnabled = true
                }
                .exceptionally {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    return@exceptionally null
                }


//        //Would want to replace this with a solid gray/white color
//        val newTexture : CompletableFuture<Texture> = Texture.builder().setSource(this, R.drawable.transparant_plane).build()
//
//        ModelRenderable.builder()
//                .setSource(arFragment.context, model)
//                .build()
//                .thenAccept {
//
//                    var newRenderable = it.makeCopy()
//
//                    MaterialFactory.makeTransparentWithTexture(this, newTexture.get())
//                            .thenAccept {
//                                newRenderable.material = it
//                                transparentNode.renderable = newRenderable
//                                transparentNode.isEnabled = true
//                            }
//                }
//                .exceptionally {
//                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//                    return@exceptionally null
//                }
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
//                    Toast.makeText(this@ModelARViewActivity, "Could not fetch model from $model", Toast.LENGTH_SHORT).show()
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

        this.currentAnchor = anchor
        anchorNode = AnchorNode(anchor)
        val rotatingNode = RotatingNode()
        val animatedNode = AnimatedNode()
        val transformableNode = TransformableNode(fragment.transformationSystem)

        bubbleNode.setEnabled(false)
        bubbleNode.setLocalPosition(Vector3(0f, .24f, 0f))

        transformableNode.setParent(anchorNode)
        transformableNode.scaleController.isEnabled = false
        rotatingNode.setParent(transformableNode)

        animatedNode.renderable = renDerable
        animatedNode.setParent(rotatingNode)
        animatedNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
        transformableNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
        currentScale = scaleMin

        this.animatedNode = animatedNode
        this.rotatingNode = rotatingNode
        this.transformableNode = transformableNode

        fragment.arSceneView.scene.addChild(anchorNode)

        //set Transparency of model

        nodeAllocated = true

        if(firstTimeWinkyFace) {
            showFab(false)
            showModel(false)
            floatingActionButton.background = null
            if(circle_item_ar_recycler_view.visibility == View.GONE || circle_item_ar_recycler_view.visibility == View.INVISIBLE)
                circle_item_ar_recycler_view.visibility = View.VISIBLE
            firstTimeWinkyFace = false
        }
    }

    private fun addDescriptionBubble() {

        // create a xml renderable (asynchronous operation,
        // result is delivered to `thenAccept` method)
        ViewRenderable.builder()
                .setView(this, descriptionBubble)
                .build()
                .thenAccept {
                    it.isShadowReceiver = true
                    descriptionBubbleRenderable = it
                    bubbleNode.renderable = it
                }
                .exceptionally { //TODO: delete on release
                    it.toast(this) }

        bubbleNode.setParent(transformableNode)
        bubbleNode.isEnabled = true
    }

    inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        private var mLastOnDownEvent: MotionEvent? = null

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTap(e)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (rotatingNode.isAnimatedByUser() && rotatingNode.isAnimated())
            {
                rotatingNode.onPauseAnimation()
                rotatingNode.setAnimated(false)
            }

            else if (!rotatingNode.isAnimatedByUser() && !rotatingNode.isAnimated())
            {
                rotatingNode.onResumeAnimation()
                rotatingNode.setAnimated(true)
            }


            return super.onDoubleTap(e)
        }

        override fun onDown(e: MotionEvent): Boolean {
            //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
            mLastOnDownEvent = e
            return super.onDown(e)
        }
    }

    private fun handleOnTouch(hitTestResults: HitTestResult, motionEvent: MotionEvent){

        if(nodeAllocated)
        {
            arFragment.onPeekTouch(hitTestResults, motionEvent)

            //check for touching a Animated Node
            if ( hitTestResults.node != animatedNode){
                Log.d(TAG, "if animatedNode was not hit, then don't worry about it")
                return
            }

            //Otherwise call gesture detector
            trackableGestureDetector.onTouchEvent(motionEvent)
        }
    }

    private fun onSingleTap(motionEvent: MotionEvent) {
        Log.d("GESTURE CONTROL : OnSingleTap", "${restaurantMenuItem?.name} ")

        val frame = arFragment.arSceneView.arFrame
        if(frame != null && motionEvent != null && frame.camera.trackingState == TrackingState.TRACKING ){
            for( hit in frame.hitTest(motionEvent)) {

                Log.d("GESTURE CONTROL : OnSingleTap - in frame loop, trackable: ${hit.trackable}", "${restaurantMenuItem?.name} ")
                var trackable = hit.trackable

                if(trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)){
                    var plane = trackable
                    //Handle Plane hits if we want to in the future
                }
                else if(trackable is com.google.ar.core.Point) {

                    val point = trackable

                    if (!bubbleNode.isEnabled)
                        addDescriptionBubble()
                    else {
                        anchorNode.removeChild(bubbleNode)
                        bubbleNode.isEnabled = false
                    }
                }
            }
        }
    }

    //fake function for sample item data
    fun getItemList() : ArrayList<RestaurantMenuItem>? {
        return MenuListHolder().getList(restaurantKey)
    }

    fun addTestData(itemList: ArrayList<RestaurantMenuItem>?) {

        var id : Long = 112

        for(item in itemList!!) {
            testData.add(ItemCircle(id,item))
            if (id == currentIndex)
                restaurantMenuItem = item

            id++
        }
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
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

    fun onBackClick(v: View){
        //TODO: Replace this by initiating a completely new SceneViewActivity instead, rather than going back to last session, This way there isnt an entire activity paused. if it doesnt matter, then instead just set new current model on the conainer Activty! and update model
        onBackPressed()
    }
}