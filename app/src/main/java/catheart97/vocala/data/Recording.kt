package catheart97.vocala.data

import java.util.*

/**
 * Class representing a (Vocal(a))-Record
 * Simply storing its name, date and length
 *
 * @author Ronja Schnur 
 */
class Recording(var name: String?, val date: Date, val length: Long)
{
    var checked = false // FOR LIST ADAPTER
    
    fun toggle()
    {
        this.checked = !this.checked
    }
}
