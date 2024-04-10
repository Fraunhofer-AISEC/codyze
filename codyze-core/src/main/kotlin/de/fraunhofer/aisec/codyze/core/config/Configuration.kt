/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.core.config

import de.fraunhofer.aisec.codyze.core.output.OutputBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Holds the main configuration to run Codyze with
 *
 * To add a new CPG configuration option do the following:
 * 1. add a property to [Configuration]
 * 2. add a new CLI option to the [CodyzeOptionGroup]
 * 3. update the [CodyzeOptionGroup.asConfiguration] method
 * 4. Optionally: Add the newly added option as an argument to [ExecutorCommand.getExecutor] to pass it to [Executor]s.
 */
data class Configuration(
    val output: Path,
    val outputBuilder: OutputBuilder,
    val goodFindings: Boolean,
    val pedantic: Boolean,
)
