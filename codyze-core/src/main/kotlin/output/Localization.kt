package de.fraunhofer.aisec.codyze_core.output

/** Strings to use for help output and error messages */
interface Localization {
    /** The given spec files do not share the same file type */
    fun invalidSpecFileType() = "All given specification files must be of the same file type."

    /** All given paths must point to a file and not a directory */
    fun pathsMustBeFiles() = "All given spec paths must be files."
}

val localization = object : Localization {}
