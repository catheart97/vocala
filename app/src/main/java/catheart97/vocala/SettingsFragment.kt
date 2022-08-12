package catheart97.vocala

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

/**
 * Storage Fragment for SettingsActivity in Storage
 *
 * @author Ronja Schnur 
 */
class SettingsFragment : PreferenceFragmentCompat(),
                         SharedPreferences.OnSharedPreferenceChangeListener
{
    private var preferences: SharedPreferences? = null
    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String)
    {
        val preference = findPreference<Preference>(key)
        
        if (preference is ListPreference)
        {
            val prefIndex = preference.findIndexOfValue(sharedPreferences!!.getString(key, ""))
            if (prefIndex >= 0) preference.setSummary(preference.entries[prefIndex])
        }
        //            preference.setSummary(sharedPreferences.getString(key, ""));
        //            not needed for current preferences
    }
    
    override fun onResume()
    {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }
    
    override fun onPause()
    {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
    
    override fun onCreatePreferences(bundle: Bundle?, s: String?)
    {
        addPreferencesFromResource(R.xml.settings)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().applicationContext)
        onSharedPreferenceChanged(preferences, getString(R.string.setting_algo_key))
        // Preferences with summary or text shall be here
    }
}
