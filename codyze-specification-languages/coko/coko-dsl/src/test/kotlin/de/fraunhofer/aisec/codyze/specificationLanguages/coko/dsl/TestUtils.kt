package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString


/**
 *  Resolves any path as an absolute path that is system invariant.
 *
 *  This is especially important when testing with Windows, as paths with backward slashes are not allowed
 *  as Coko imports.
 *  @see java.nio.file.Path.resolve(String)
 */
fun Path.resolveAbsoluteInvariant(other: String): Path {
    return Path.of(this.resolve(other).toAbsolutePath().invariantSeparatorsPathString)
}