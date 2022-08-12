package catheart97.vocala

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import catheart97.vocala.data.Storage

/**
 * Splash Activity for Storage Application
 *
 * Handles permissions and preference loading
 *
 * @author Ronja Schnur 
 */
class SplashActivity : VocalaAActivity()
{
    companion object
    {
        private const val SPLASH_DISPLAY_LENGTH = 500 // if screen shown to short
        
        private val PERMISSIONS_LIST = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_PERMISSIONS = 1
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
    
    override fun finish()
    {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    private fun launchMainActivity()
    {
        // Load data into intent if available
        val vIntent = Intent(this, VocalaActivity::class.java)
        if (intent.action == Intent.ACTION_VIEW)
        {
            if (intent.type!!.contains("wav")) vIntent.putExtra(getString(R.string.contentType), "wav")
            else if (intent.data!!.path!!.endsWith(".exercise")) vIntent.putExtra(
                getString(R.string.contentType), "exercise"
            )
            else throw RuntimeException("Invalid file type")
            vIntent.data = intent.data
            vIntent.action = intent.action
        }
        
        Handler().postDelayed({
            startActivity(vIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode)
        {
            REQUEST_PERMISSIONS ->
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) launchMainActivity()
            }
            else -> finish() // Doesn't allow non microphone usage
        }
    }
    
    override fun onResume()
    {
        super.onResume()
        
        Storage.loadPreferences(this)
        
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) // NEED TO BE CHECKED FOR EACH SEPARATELY
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )
            )
            {
                Toast.makeText(
                    this, R.string.splash_please_grant_microphone, Toast.LENGTH_LONG
                ).show()
                ActivityCompat.requestPermissions(
                    this, PERMISSIONS_LIST, REQUEST_PERMISSIONS
                )
            }
            else
            {
                ActivityCompat.requestPermissions(
                    this, PERMISSIONS_LIST, REQUEST_PERMISSIONS
                )
            }
        }
        else launchMainActivity()
    }
}
