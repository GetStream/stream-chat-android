/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.a2ui.core

/** The conformance cases this renderer doesn't run, each with the reason. */
internal object ConformanceSkips {

    private val OTHER_VERSIONS = setOf("v0.8", "v0.9", "v0.9.1")

    private const val V09_SYNTAX =
        "Written in v0.9 syntax (`path`, a bare `name`), which a 1.0 renderer reads as literals"
    private const val AGENT_CALL_IDS =
        "Calls to the agent come from bindings and actions, with call ids of the renderer's own"
    private const val CALL_CONFIG = "Per-call validation config of the optional agent_renderer_app feature"

    private val BY_NAME: Map<String, String> = mapOf(
        "test_action_event_with_data_binding" to V09_SYNTAX,
        "test_action_event_with_relative_scope" to V09_SYNTAX,
        "test_action_direct_name_shorthand" to V09_SYNTAX,
        "test_rpc_call_agent_function_dispatch" to AGENT_CALL_IDS,
        "test_rpc_call_agent_function_timeout_handling" to AGENT_CALL_IDS,
        "test_rpc_call_agent_function_duplicate_call_id_error" to AGENT_CALL_IDS,
        "test_rpc_call_renderer_function_requires_user_activation" to
            "A call from the agent carries no gesture of the person's, " +
            "so the renderer never runs a requiresUserActivation function for it",
        "test_rpc_call_renderer_function_protocol_version_mismatch" to "A v0.9 catalog: this renderer reads 1.0",
        "test_v10_get_renderer_data_model_filters_by_surface_protocol_version" to
            "A v0.9 surface: this renderer reads 1.0",
        "test_v10_get_renderer_capabilities" to
            "Renderer capabilities belong to the A2A transport, which this module leaves out",
        "test_v10_get_renderer_capabilities_inline_catalogs" to
            "Renderer capabilities belong to the A2A transport, which this module leaves out",
        "test_v10_call_validation_config_relaxes_strict_processor" to CALL_CONFIG,
        "test_v10_call_validation_config_tightens_permissive_processor" to CALL_CONFIG,
        "test_v10_call_validation_config_leaves_out_rules_as_strict" to CALL_CONFIG,
        "test_v10_call_to_undeclared_function_without_strict_mode_accepted" to
            "The processor always checks a call against its catalog, so an undeclared function " +
            "is reported rather than forwarded to the agent",
    )

    /** Why [case] doesn't apply, or `null` when it runs. */
    fun reason(suite: String, name: String, case: Map<String, Any?>): String? {
        BY_NAME[name]?.let { return it }
        val version = case["protocolVersion"] as? String
        if (version in OTHER_VERSIONS) return "A2UI $version: this renderer reads 1.0"
        if (version == null && usesOtherVersion(case)) {
            return "Messages of another A2UI version: this renderer reads 1.0"
        }
        return null
    }

    private fun usesOtherVersion(value: Any?): Boolean = when (value) {
        is Map<*, *> -> value["version"] in OTHER_VERSIONS || value.values.any(::usesOtherVersion)
        is List<*> -> value.any(::usesOtherVersion)
        else -> false
    }
}
