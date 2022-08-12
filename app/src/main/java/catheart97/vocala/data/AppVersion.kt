package catheart97.vocala.data

/**
 * Handles AppVersion Comparison
 *
 * @author Ronja Schnur 
 */
class AppVersion(version_string: String) : Comparable<AppVersion>
{
    private val major: Int
    private val minor: Int
    private val patch: Int
    
    init
    {
        val versionStr = version_string.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (versionStr.size != 3) throw RuntimeException("Invalid versionStr!")
        
        major = Integer.parseInt(versionStr[0])
        minor = Integer.parseInt(versionStr[1])
        patch = Integer.parseInt(versionStr[2])
    }
    
    override fun compareTo(other: AppVersion): Int
    {
        val major = major.compareTo(other.major)
        if (major != 0) return major
        
        val minor = minor.compareTo(other.minor)
        return if (minor != 0) minor else patch.compareTo(other.patch)
    }
}
