# Changing the SARIF version of the tool output

These instructions are meant for developers to easily update to a new SARIF version.

Whenever the SARIF version of the output needs to be changed, the following steps are required:
1. Navigate to `src/main/resources/json` and replace the existing schema with the new one
   - SARIF schemas (among others) can be found [here](https://www.schemastore.org/json/)
2. Recompile the project, the generated files (`build/generated/sources/js2d/main/de/fraunhofer/aisec/codyze/sarif/schema`) should be replaced
   - In case old files are still present (easily detectable if there is more than one `Sarif*.java` file, where `*` stands for the SARIF version) manually delete the generated classes and recompile
3. Navigate to the `SarifInstantiator.kt` (`src/main/java/de/fraunhofer/aisec/codyze/sarif/`) and update the class members.
   - The important part can be found in the first few lines, most importantly you need to update the class name of the `sarif` variable as well as `schema` and `sarifVersion`
   - Use this occasion to check whether anyone kept the `codyzeVersion` up to date and if the URIs for `download` and `information` are still working
4. Execute `SarifInstantiatorTest.kt` (`src/test/java/de/fraunhofer/aisec/codyze/crymlin/`)
   - The `testToString()` might fail if the output differs in the `results` section of the results file, in this case update it accordingly
   - You can use the generated file of `testOutput()` to test it with the [online validator](https://sarifweb.azurewebsites.net/Validation)
     - Even better yet, use the tool with some of the files in `src/test/resources` and validate the output


**After the validation was finished successfully, the SARIF version has now been updated!**

If there are any new errors while validating the files, additional adaptions in `SarifInstantiator.kt` may be needed (especially the `pushRun(Set<Finding>)` method), however this should NOT be the case unless the new SARIF version has made big changes to the overall structure.
