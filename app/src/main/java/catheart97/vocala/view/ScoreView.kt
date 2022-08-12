package catheart97.vocala.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import catheart97.vocala.data.Exercise

/**
 * View showing Music Scored in a WebView using Vex.Flow
 *
 * @author Ronja Schnur 
 */
class ScoreView : WebView
{
    internal lateinit var exercise: Exercise
    
    val exerciseSize
        @JavascriptInterface get() = this.exercise.sheetSize
    
    val clef
        @JavascriptInterface get() = exercise.vexFlowClef
    
    val numBeats
        @JavascriptInterface get() = exercise.vexFlowNumBeats
    
    val beatValue
        @JavascriptInterface get() = Exercise.BEAT_VALUE
    
    constructor(context: Context?) : super(context!!)
    {
        init()
    }
    
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    {
        init()
    }
    
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)
    {
        init()
    }
    
    @SuppressLint("SetJavaScriptEnabled") private fun init()
    {
        this.exercise = Exercise()
        settings.javaScriptEnabled = true
        addJavascriptInterface(this, "ScoreView")
        loadUrl("file:///android_asset/scoreview.html")
        webViewClient = ScoreViewClient()
    }
    
    private inner class ScoreViewClient : WebViewClient()
    {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean
        {
            animate(view, android.R.anim.fade_out)
            view.visibility = View.GONE
            return false
        }
        
        override fun onPageFinished(view: WebView, url: String)
        {
            animate(view, android.R.anim.fade_in)
            view.visibility = View.VISIBLE
            super.onPageFinished(view, url)
        }
    }
    
    private fun animate(view: WebView, anim_resource: Int)
    {
        val anim = AnimationUtils.loadAnimation(context, anim_resource)
        view.startAnimation(anim)
    }
    
    @JavascriptInterface fun getDuration(i: Int): String
    {
        return this.exercise.getTone(i).vexFlowDurationString
    }
    
    @JavascriptInterface fun getTone(i: Int): String
    {
        return exercise.getVexFlowTone(i)
    }
    
    @JavascriptInterface fun isBlackKey(i: Int): Boolean
    {
        return !exercise.getTone(i).whiteKey
    }
    
    fun setExercise(exercise: Exercise)
    {
        this.exercise = exercise
        this.reload()
    }
}
