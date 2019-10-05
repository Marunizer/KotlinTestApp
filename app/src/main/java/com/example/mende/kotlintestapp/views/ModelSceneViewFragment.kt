package com.example.mende.kotlintestapp.views

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.mende.kotlintestapp.R
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.activity_model_scene.*
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.AnimatedNode
import com.example.mende.kotlintestapp.util.RotatingNode
import android.view.ScaleGestureDetector
import com.google.ar.sceneform.*
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
//import com.example.mende.kotlintestapp.util.RotationGestureDetector
import com.google.ar.sceneform.math.Quaternion


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
 *  https://www.youtube.com/watch?v=t8TU1ZB_MfQ
 *          -- Pinch to zoom
 *
 *  https://ryanharter.com/blog/2014/10/08/using-gestures/
 *          -- I think I looked at this for scale detection and Gesture Detection in general
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *
 * - TODO It appears that when the user has left the screen and comes back app either crashes or 2nd model on top of first is created
 *  - Prediction: maybe when the user has left the activity for a long period of time, the onStop() function of the fragment is evoked
 *  so then when user comes back, it's not just onResume() that's happening, but onStart() !!!
 *
 * -------------------------------------------------------------------------------------------------
 * Notes from Original Noni:  (Could still be applicable, must review and move up if so)
 *  *
 *
 * */

class ModelSceneViewFragment : Fragment(){//, RotationGestureDetector.OnRotationGestureListener {

//    override fun onRotation(rotationDetector: RotationGestureDetector?) {
//        rotationAngle = rotationDetector?.angle
//    }

    private val TAG = ModelSceneViewFragment::class.java.simpleName

    var containerActivity: FragmentActivity? = null
    lateinit var sceneContext: Context
    lateinit var scene: Scene
    lateinit var sceneView: SceneView
    var firstTimeWinkyFace: Boolean = true
    private var nodeAllocated: Boolean = false
    private var inSession: Boolean = false

    private lateinit var oldItemModelNode: Node
    private lateinit var itemModelNode: Node
    private lateinit var trackableGestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
 //   private lateinit var rotatingGestureDetector: RotationGestureDetector
    private var rotationAngle: Float? = null

    private val scaleMin: Float = 0.0f
    private val scaleMax: Float = 1.25f
    private var currentScale: Float = 0.0f
    private var oldScale: Float = 1.25f
    private var scale: Float = 1f //maybe this should stay 1? nopt sure

    private var oldAngle: Float = 1.0f
    private var currentAngle: Float = 0.0f

    private var nodeW: Float = 0f
    private var nodeX: Float = 0f
    private var nodeY: Float = 0f
    private var nodeZ: Float = 0f

    lateinit var oldAnimatedNode: AnimatedNode
    lateinit var animatedNode: AnimatedNode
    lateinit var rotatingNode: RotatingNode

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

        if (inSession) {
            return
        }
        inSession = true

//        val camera = sceneView.scene.camera
//        camera.localRotation = Quaternion.axisAngle(Vector3.right(), -30.0f)

        sceneView = scene_view
        scene = sceneView.scene // get current scene
        scene.addOnUpdateListener { frameTime ->
            onUpdate(frameTime)
        }


        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                Log.d("TEST_SCALE", "Scale Span: " + scaleGestureDetector.getCurrentSpan())
                scale = scale * detector.scaleFactor
                scale = Math.max(0.5f, Math.min(scale, 2.25f)) // like how many xTimes the ratio
                animatedNode.localScale = Vector3(scale, scale, scale)
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {

                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {

            }
        })

       // rotatingGestureDetector = RotationGestureDetector(this, this.sceneView)

        this.trackableGestureDetector = GestureDetector(activity, MyGestureDetector())


        scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
            scaleGestureDetector.onTouchEvent(motionEvent)
            MyGestureDetector().onSingleTapUp(motionEvent)
            // rotatingGestureDetector.onTouchEvent(motionEvent)
        }

        animatedNode = AnimatedNode() //fake init
        oldAnimatedNode = AnimatedNode() //fake init


        val container: ModelContainerViewActivity = activity as ModelContainerViewActivity
        container.begin()


    }

    private fun onUpdate(frameTime: FrameTime) {

        //nodeIsDown, safe to continue
        if (nodeAllocated) {

            // itemModelNode.localRotation = Quaternion.axisAngle(Vector3(nodeX, nodeY+.5f, nodeZ), nodeW)
            //nodeY = nodeY+.5f
//            nodeW = itemModelNode.localRotation.w
//            nodeX = itemModelNode.localRotation.x
//            nodeY = itemModelNode.localRotation.y
//            nodeZ = itemModelNode.localRotation.z

            if (rotationAngle != null) {

                if (oldAngle != currentAngle) {

                    oldAngle = currentAngle

                    if (rotationAngle!! > currentAngle) {
                        currentAngle = currentAngle + ((1f / 15f) % 360)

                        animatedNode.localRotation = Quaternion.lookRotation(
                                Vector3(currentAngle, currentAngle, currentAngle), Vector3.up())

                    } else if (rotationAngle!! < currentAngle) {
                        currentAngle = currentAngle - ((1f / 15f) % 360)

                        animatedNode.localRotation = Quaternion.lookRotation(
                                Vector3(currentAngle, currentAngle, currentAngle), Vector3.up())
                    }
                }
            }
//                lookRotation(
//                        animatedNode.forward,
//                        Vector3(rotationAngle!!, rotationAngle!!, rotationAngle!!))
//            }


            //Idk if we need this variable? it might be good enough to use curreentItem idk
            if (animatedNode.isInitialized) {

                //if animation has not finished , despite it being selected, continue,
                if (!animatedNode.isFullSizeAnimationDone) {
                    //Want animation to last for .4 seconds. //1f(second) == 30frames
                    currentScale = currentScale + (1f / 15f) //.5r

                    if (currentScale >= scaleMax) {
                        animatedNode.localScale = Vector3(scaleMax, scaleMax, scaleMax)
                        animatedNode.isFullSizeAnimationDone = true
                    } else if (currentScale < scaleMax) {
                        animatedNode.localScale = Vector3(currentScale, currentScale, currentScale)
                    }
                } else if (oldAnimatedNode.isRemoving) {
                    Log.d("MAGIC SPEAKER:e", "still removing?")
                    minimizeItem()
                }
            } else if (!animatedNode.isInitialized) {
                if (!oldAnimatedNode.isRemoveAnimationDone) {
                    oldAnimatedNode.isRemoving = true
                    minimizeItem()
                }
            }
        } else //node NOT allocated, meaning, new item was picked !!!
        {
            Log.d("MAGIC SPEAKER:e", " node allocated: false ")
            if (oldAnimatedNode.isInitialized && !oldAnimatedNode.isRemoveAnimationDone) {
                oldAnimatedNode.isRemoving = true
                minimizeItem()
            }
        }
    }

    private fun minimizeItem() {
        oldScale = oldScale - (1f / 15f) //.5 seconds

        if (oldScale <= scaleMin) {
            oldAnimatedNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
            scene.removeChild(oldItemModelNode)
            oldAnimatedNode.isRemoveAnimationDone = true
            oldAnimatedNode.isRemoving = false
            oldScale = scaleMax

            Log.d("MAGIC SPEAKER: Finished minimizing animatednode", "${itemModelNode.name} should now be displayed ")
            renderObject(Uri.parse("${itemModelNode.name}.sfb"), itemModelNode.name)
        } else if (oldScale > scaleMin) {
            animatedNode.localScale = Vector3(oldScale, oldScale, oldScale)
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
                    addNodeToScene(it, name)
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
    private fun addNodeToScene(model: ModelRenderable?, modelName: String?) {

        model?.let {
            itemModelNode = Node().apply {
                //setParent(scene)
                localPosition = Vector3(0f, -.335f, -1f)
                localScale = Vector3(3f, 3f, 3f)
                name = modelName
            }


            val animatedNode = AnimatedNode()
            val rotatingNode = RotatingNode()

            animatedNode.setOnTouchListener { hitTestResult, motionEvent ->
                MyGestureDetector().onDoubleTap(motionEvent)
            }

            rotatingNode.setParent(itemModelNode)

            this.rotatingNode = rotatingNode

            animatedNode.renderable = model
            animatedNode.setParent(rotatingNode)
            animatedNode.isInitialized = true
            animatedNode.localScale = Vector3(scaleMin, scaleMin, scaleMin)
            currentScale = scaleMin

            this.animatedNode = animatedNode

            scene.addChild(itemModelNode)
            nodeAllocated = true

            nodeW = itemModelNode.localRotation.w
            nodeX = itemModelNode.localRotation.x
            nodeY = itemModelNode.localRotation.y
            nodeZ = itemModelNode.localRotation.z

        }
    }

    inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        private var mLastOnDownEvent: MotionEvent? = null

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTap(e)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            mLastOnDownEvent = e
            return super.onDown(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {//actually only takes one tap

            if (rotatingNode.isAnimatedByUser() && rotatingNode.isAnimated()) {
                rotatingNode.onPauseAnimation()
                rotatingNode.setAnimated(false)
            } else if (!rotatingNode.isAnimatedByUser() && !rotatingNode.isAnimated()) {
                rotatingNode.onResumeAnimation()
                rotatingNode.setAnimated(true)
            }
            return super.onDoubleTap(e)
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


        if (motionEvent != null) {
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
        if (scene.children.isEmpty()) {
            onStart()
        }
    }

    //should instead implement a listener that knows when a different circle item has been selected.
    //the lambda way
    fun passData(restaurantMenuItem: RestaurantMenuItem?) {

        Log.d("MAGIC SPEAKER", "${restaurantMenuItem?.name} =?= ")

        if (firstTimeWinkyFace) {
            renderObject(Uri.parse("${restaurantMenuItem?.name}.sfb"), restaurantMenuItem?.name) // Render the object
            firstTimeWinkyFace = false
        } else if (restaurantMenuItem?.name != itemModelNode.name) {
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