package com.example.mende.kotlintestapp.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import com.example.mende.kotlintestapp.R
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.*
import kotlinx.android.synthetic.main.activity_model_scene.*
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.AnimatedNode
import com.example.mende.kotlintestapp.util.RotatingNode
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import android.view.ScaleGestureDetector




/**
 * Created by Marunizer
 * 3D Model View
 * purpose: Interact with 3D model in a stable scene
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *  references:
 *  https://medium.com/@harivigneshjayapalan/arcore-cupcakes-1-render-a-scene-without-ar-for-phones-without-arcore-27d61a43a130'
 *   -- Used to render scene, apperantly does not need ARCore??
 *   -- Might be able to support older phones? but probably not, SDK 24 prob still the only option...
 *
 *  https://www.raywenderlich.com/361-android-fragments-tutorial-an-introduction-with-kotlin
 *          -- Can learn how to interact with Activity from this link later
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 * -
 *
 * -------------------------------------------------------------------------------------------------
 * Notes from Original Noni:  (Could still be applicable, must review and move up if so)
 *  *
 *
 * */

class ModelSceneViewFragment : Fragment() {

    private val TAG = ModelSceneViewFragment::class.java.simpleName

    var containerActivity : FragmentActivity? = null
    lateinit var sceneContext : Context
    lateinit var scene: Scene
    var firstTimeWinkyFace : Boolean = true
    private var nodeAllocated : Boolean = false

    private lateinit var oldItemModelNode: Node
    private lateinit var itemModelNode: Node
    private lateinit var trackableGestureDetector: GestureDetector
    private lateinit var scaleGestureDetector : ScaleGestureDetector

    private val scaleMin : Float = 0.0f
    private val scaleMax : Float = 1.25f
    private var currentScale: Float = 0.0f
    private var oldScale: Float = 1.25f

    lateinit var oldAnimatedNode : AnimatedNode
    lateinit var animatedNode: AnimatedNode

    companion object {

        fun newInstance(): ModelSceneViewFragment {
            return ModelSceneViewFragment()
        }
    }

    //used whenever we want to pass in data to fragment from other sources upon creation
    override fun onAttach(context: Context) {
        sceneContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.activity_model_scene, container, false)
        containerActivity = activity
        return view
    }

    override fun onStart() {
        super.onStart()

//        val camera = sceneView.scene.camera
//        camera.localRotation = Quaternion.axisAngle(Vector3.right(), -30.0f)

        scene = sceneView.scene // get current scene
        scene.sunlight.onDeactivate()
        scene.addOnUpdateListener { frameTime ->
            onUpdate(frameTime) }



        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                Log.d("TEST_SCALE", "Scale Span: " + scaleGestureDetector.getCurrentSpan())
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {

                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                itemModelNode.localScale = Vector3(detector.scaleFactor,detector.scaleFactor,detector.scaleFactor)
            }
        })

        animatedNode = AnimatedNode() //fake init
        oldAnimatedNode = AnimatedNode() //fake init


        // this.trackableGestureDetector = GestureDetector(activity, MyGestureDetector())

//        scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
//            scaleGestureDetector.onTouchEvent(motionEvent)
//            MyGestureDetector().onSingleTapUp(motionEvent) }

        // scene.addOnPeekTouchListener(this::handleOnTouch)



        val container : ModelContainerViewActivity  = activity as ModelContainerViewActivity
        container.begin()



    }

    private fun onUpdate(frameTime: FrameTime) {

        //nodeIsDown, safe to continue
        if(nodeAllocated)
        {
            //Idk if we need this variable? it might be good enough to use curreentItem idk
            if(animatedNode.isInitialized)
            {

                //if animation has not finished , despite it being selected, continue,
                if(!animatedNode.isFullSizeAnimationDone) {
                    //Want animation to last for .4 seconds. //1f(second) == 30frames
                    currentScale = currentScale + (1f/15f)

                    if(currentScale >= scaleMax) {
                        animatedNode.localScale = Vector3(scaleMax, scaleMax, scaleMax)
                        animatedNode.isFullSizeAnimationDone = true
                    }
                    else if(currentScale < scaleMax) {
                        animatedNode.localScale = Vector3(currentScale, currentScale, currentScale)
                    }
                }
                else if(oldAnimatedNode.isRemoving) {
                    Log.d("MAGIC SPEAKER:e", "still removing?")
                    minimizeItem() }
            }
            else if(!animatedNode.isInitialized) {
                if(!oldAnimatedNode.isRemoveAnimationDone) {
                    oldAnimatedNode.isRemoving = true
                    minimizeItem()
                }
            }
        }
        else //node NOT allocated, meaning, new item was picked !!!
        {
            Log.d("MAGIC SPEAKER:e", " node allocated: false ")
            if(oldAnimatedNode.isInitialized && !oldAnimatedNode.isRemoveAnimationDone) {
                oldAnimatedNode.isRemoving = true
                minimizeItem()
            }
        }
    }
    private fun minimizeItem() {
        oldScale = oldScale - (1f/9f) //.3 seconds

        if (oldScale <= scaleMin)
        {
            oldAnimatedNode.localScale = Vector3(scaleMin,scaleMin,scaleMin)
            scene.removeChild(oldItemModelNode)
            oldAnimatedNode.isRemoveAnimationDone = true
            oldAnimatedNode.isRemoving = false
            oldScale = scaleMax

            Log.d("MAGIC SPEAKER: Finished minimizing animatednode", "${itemModelNode.name} should now be displayed ")
            renderObject(Uri.parse("${itemModelNode.name}.sfb"), itemModelNode.name)
        }
        else if(oldScale > scaleMin)
        {
            animatedNode.localScale = Vector3(oldScale,oldScale,oldScale)
        }
    }

    /**
     * load the 3D model in the space
     * @param parse URI of the model, imported using Sceneform plugin
     */
    private fun renderObject(parse: Uri, name: String?) {
        ModelRenderable.builder()
                .setSource(sceneContext, parse)
                .build()
                .thenAccept {
                    addNodeToScene(it,name)
                }
                .exceptionally {
                    val builder = AlertDialog.Builder(sceneContext)
                    builder.setMessage(it.message)
                            .setTitle("error!")
                    val dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
    }

    /**
     * Adds a node to the current scene
     * @param model - rendered model
     */
    private fun addNodeToScene(model: ModelRenderable?, modelName : String?) {

        model?.let {
            itemModelNode = Node().apply {
                //setParent(scene)
                localPosition = Vector3(0f, -.4f, -1f)
                localScale = Vector3(3f, 3f, 3f)
                name = modelName
            }
//            scene.addOnPeekTouchListener()
//            scene.addOnPeekTouchListener(
//                    { hitResult: HitResult, motionEvent: MotionEvent ->
//                        //do stuff
//
//                    })

            val animatedNode = AnimatedNode()

            val dm = resources.displayMetrics
            val sv = FootprintSelectionVisualizer()

            val transformationSystem =  TransformationSystem(dm,sv)
            //transformationSystem.addGestureRecognizer()
            val transformableNode = TransformableNode(transformationSystem)
            val rotatingNode = RotatingNode()

            //transformableNode.renderable = cupCakeNode.renderable
            transformableNode.localScale = itemModelNode.localScale
            transformableNode.localPosition = itemModelNode.localPosition
            transformableNode.scaleController.maxScale = 5f
            transformableNode.setParent(itemModelNode) //could make this main node later

            rotatingNode.setParent(transformableNode)

            animatedNode.renderable = model
            animatedNode.setParent(rotatingNode)
            animatedNode.isInitialized = true
            animatedNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
            currentScale = scaleMin

            animatedNode.setOnTouchListener { hitTestResult, motionEvent ->
                scaleGestureDetector.onTouchEvent(motionEvent)
            }

            this.animatedNode = animatedNode

            scene.addChild(itemModelNode)
            nodeAllocated = true

        }
    }

    inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        private var mLastOnDownEvent: MotionEvent? = null

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTap(e)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
            mLastOnDownEvent = e
            return super.onDown(e)
        }
    }

//    private fun handleOnTouch(hitTestResults: HitTestResult, motionEvent: MotionEvent){
//
//        if(nodeAllocated)
//        {
//            scene.hit
//
//
//
//
//
//
//
//             sceneView.onPeekTouch(hitTestResults, motionEvent)
//
//            //check for touching a Animated Node
//            if ( hitTestResults.node != animatedNode){
//                Log.d(TAG, "if animatedNode was not hit, then don't worry about it")
//                return
//            }
//
//            //Otherwise call gesture detector
//            trackableGestureDetector.onTouchEvent(motionEvent)
//        }
//    }

    private fun onSingleTap(motionEvent: MotionEvent) {


        if(motionEvent != null ){
//            for( hit in frame.hitTest(motionEvent)) {
//
//              var trackable = hit.trackable
//
//                if(trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)){
//                    var plane = trackable
//                    //Handle Plane hits if we want to in the future
//                }
//                else if(trackable is com.google.ar.core.Point) {
//
//                    val point = trackable
//
//                    if (!bubbleNode.isEnabled)
//                        addDescriptionBubble()
//                    else {
//                        anchorNode.removeChild(bubbleNode)
//                        bubbleNode.isEnabled = false
//                    }
//                }
//            }
        }
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    //should instead implement a listener that knows when a different circle item has been selected.
    //the lambda way
    fun passData(restaurantMenuItem: RestaurantMenuItem?) {

        Log.d("MAGIC SPEAKER", "${restaurantMenuItem?.name} =?= ")

        if (firstTimeWinkyFace) {
            renderObject(Uri.parse("${restaurantMenuItem?.name}.sfb"), restaurantMenuItem?.name) // Render the object
            firstTimeWinkyFace = false
        }
        else if (restaurantMenuItem?.name != itemModelNode.name)
        {
            oldItemModelNode = itemModelNode
            itemModelNode = Node()
            itemModelNode.name = restaurantMenuItem?.name
            animatedNode.isSelected = false
            oldAnimatedNode = animatedNode
            oldAnimatedNode.isInitialized = true
            nodeAllocated = false
        }
    }


}