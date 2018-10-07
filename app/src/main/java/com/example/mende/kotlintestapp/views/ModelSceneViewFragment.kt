package com.example.mende.kotlintestapp.views

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mende.kotlintestapp.R
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.*
import kotlinx.android.synthetic.main.activity_model_scene.*
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.RotatingNode


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

    var containerActivity : FragmentActivity? = null
    lateinit var sceneContext : Context
    lateinit var scene: Scene
    var firstTimeWinkyFace : Boolean = true

    lateinit var itemModelNode: Node
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

        val container : ModelContainerViewActivity  = activity as ModelContainerViewActivity
        container.begin()

//        transformationSystem = makeTransformationSystem()
//
//        gestureDetector = GestureDetector(
//                context,
//                object : GestureDetector.SimpleOnGestureListener() {
//                    override fun onSingleTapUp(e: MotionEvent): Boolean {
//                        onSingleTap(e)
//                        return true
//                    }
//
//                    override fun onDown(e: MotionEvent): Boolean {
//                        return true
//                    }
//                })
//
//        scene.addOnPeekTouchListener(this)
//        arSceneView.getScene().addOnUpdateListener(this)


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
//            // Create the transformable andy and add it to the anchor.
            //val fragment = ArFragment()
            //fragment.transformationSystem

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

            rotatingNode.renderable = model
            rotatingNode.setParent(transformableNode)

            scene.addChild(itemModelNode)
        }
    }

    fun animatePlacement(model : TransformableNode) {



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
            scene.removeChild(itemModelNode)
            renderObject(Uri.parse("${restaurantMenuItem?.name}.sfb"), restaurantMenuItem?.name)
        }

    }


}