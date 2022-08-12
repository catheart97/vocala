package catheart97.vocala

import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Storage Activity for Storage
 *
 * @author Ronja Schnur 
 */
class SettingsActivity : VocalaAActivity()
{
    override fun finish()
    {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.right2left_in)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            android.R.id.home ->
            {
                this.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_layout_actionbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
