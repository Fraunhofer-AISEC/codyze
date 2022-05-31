/**
 * Simple constant propagation.
 * <p>
 * Classes in this package allow to resolve values of constants. The constant resolution is "simple", because:
 * <p>
 * - it only operates intraprocedurally, i.e. it does not consider method calls. - it does not consider branches - it does not support all kinds of operators
 * <p>
 * The analysis is sound, in that it only returns a constant if there is only one possible execution path yielding that constant. It is by no means complete, i.e. there
 * are various ways how the analysis may miss a potential constant.
 */

package de.fraunhofer.aisec.codyze.analysis.resolution;