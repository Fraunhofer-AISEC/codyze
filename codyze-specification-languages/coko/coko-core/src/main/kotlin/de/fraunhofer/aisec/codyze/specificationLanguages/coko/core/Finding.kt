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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core

import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.Result
import io.github.detekt.sarif4k.ResultKind
import java.nio.file.Path

/**
 * The result of a rule evaluation.
 */
interface Finding {
    val message: String
    val kind: Kind

    fun toSarif(rule: CokoRule, rules: List<CokoRule>, artifacts: Map<Path, Artifact>?): Result

    enum class Kind(val resultKind: ResultKind) {
        Fail(ResultKind.Fail),
        Informational(ResultKind.Informational),
        NotApplicable(ResultKind.NotApplicable),
        Open(ResultKind.Open),
        Pass(ResultKind.Pass),
        Review(ResultKind.Review)
    }
}
