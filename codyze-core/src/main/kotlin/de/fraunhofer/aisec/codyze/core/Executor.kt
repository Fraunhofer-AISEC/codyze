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
package de.fraunhofer.aisec.codyze.core

import io.github.detekt.sarif4k.Result

/**
 * An executor that drives the validation of a specification language against source code and
 * provides evaluation results.
 *
 * For our dependency injection library (Koin), this interface is a 'service interface' and all
 * implementations are 'services'.
 *
 * @since v3.0.0
 */
interface Executor {
    fun evaluate(): List<Result>
}
