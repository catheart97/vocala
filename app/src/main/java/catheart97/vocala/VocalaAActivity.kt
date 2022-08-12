package catheart97.vocala

import androidx.appcompat.app.AppCompatActivity

/**
 * Enables basic spotlight stuff
 *
 * @author Ronja Schnur 
 */
abstract class VocalaAActivity : AppCompatActivity()
{
    override fun onWindowFocusChanged(hasFocus: Boolean)
    {
        super.onWindowFocusChanged(hasFocus)
        // SpotlightUtils(this).showSpotlight()
    }
}
