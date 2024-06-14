package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.cpg.TranslationResult
import java.io.PrintStream

open abstract class InformationExtractor {

    // Todo has a function that is executed to extract information on a TranslationResult
    // Todo Has a Function that invokes a formatter and returns a string
    // Todo Has a function that feeds the generated string into an output backend

    /**
     * Extracts the extractor specific information from the translation result and saves it internally.
     *
     * The information can then be further used or formatted and printed
     */
    public abstract fun extractInformation(result: TranslationResult);

    public fun printInformation(formatter: Formatter, printerF1: PrintStream, printerF2:PrintStream){
        printerF1.print(formatSFInformation(formatter))
        printerF2.print(formatTSFIInformation(formatter))
    }

    /**
     * The extractor specific information needs to be given to the formatter in a key, value base fashion. The
     * implementation of this function can nest key values, generated lists etc.
     */
    protected abstract fun formatTSFIInformation(formatter: Formatter):String;

    /**
     * The extractor specific information needs to be given to the formatter in a key, value base fashion. The
     * implementation of this function can nest key values, generated lists etc.
     *
     */
    protected abstract fun formatSFInformation(formatter: Formatter):String;

}