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

@file:Suppress("MagicNumber") // Vector path data.

package io.getstream.chat.android.ai.openui.icons

// The auto-mirrored rounded Material icons this module draws that material-icons-core lacks, copied from
// androidx.compose.material:material-icons-extended 1.7.8 (only made internal) so the module
// doesn't depend on that artifact. Copyright 2025 The Android Open Source Project, licensed under
// the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES).

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Icons.AutoMirrored.Rounded.Article: ImageVector
    get() {
        if (_article != null) {
            return _article!!
        }
        _article = materialIcon(name = "AutoMirrored.Rounded.Article", autoMirror = true) {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                close()
                moveTo(13.0f, 17.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(14.0f, 16.55f, 13.55f, 17.0f, 13.0f, 17.0f)
                close()
                moveTo(16.0f, 13.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(17.0f, 12.55f, 16.55f, 13.0f, 16.0f, 13.0f)
                close()
                moveTo(16.0f, 9.0f)
                horizontalLineTo(8.0f)
                curveTo(7.45f, 9.0f, 7.0f, 8.55f, 7.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(17.0f, 8.55f, 16.55f, 9.0f, 16.0f, 9.0f)
                close()
            }
        }
        return _article!!
    }

private var _article: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Assignment: ImageVector
    get() {
        if (_assignment != null) {
            return _assignment!!
        }
        _assignment = materialIcon(name = "AutoMirrored.Rounded.Assignment", autoMirror = true) {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 1.84f, 13.3f, 1.0f, 12.0f, 1.0f)
                reflectiveCurveToRelative(-2.4f, 0.84f, -2.82f, 2.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(13.0f, 17.0f)
                lineTo(8.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(16.0f, 13.0f)
                lineTo(8.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(16.0f, 9.0f)
                lineTo(8.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _assignment!!
    }

private var _assignment: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Chat: ImageVector
    get() {
        if (_chat != null) {
            return _chat!!
        }
        _chat = materialIcon(name = "AutoMirrored.Rounded.Chat", autoMirror = true) {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 22.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.0f, 9.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(7.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(13.0f, 14.0f)
                lineTo(7.0f, 14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(17.0f, 8.0f)
                lineTo(7.0f, 8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _chat!!
    }

private var _chat: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.DirectionsBike: ImageVector
    get() {
        if (_directionsBike != null) {
            return _directionsBike!!
        }
        _directionsBike = materialIcon(
            name = "AutoMirrored.Rounded.DirectionsBike",
            autoMirror =
            true,
        ) {
            materialPath {
                moveTo(15.5f, 5.5f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(5.0f, 12.0f)
                curveToRelative(-2.8f, 0.0f, -5.0f, 2.2f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.2f, 5.0f, 5.0f, 5.0f)
                reflectiveCurveToRelative(5.0f, -2.2f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.2f, -5.0f, -5.0f, -5.0f)
                close()
                moveTo(5.0f, 20.5f)
                curveToRelative(-1.9f, 0.0f, -3.5f, -1.6f, -3.5f, -3.5f)
                reflectiveCurveToRelative(1.6f, -3.5f, 3.5f, -3.5f)
                reflectiveCurveToRelative(3.5f, 1.6f, 3.5f, 3.5f)
                reflectiveCurveToRelative(-1.6f, 3.5f, -3.5f, 3.5f)
                close()
                moveTo(10.8f, 10.5f)
                lineToRelative(2.4f, -2.4f)
                lineToRelative(0.8f, 0.8f)
                curveToRelative(1.06f, 1.06f, 2.38f, 1.78f, 3.96f, 2.02f)
                curveToRelative(0.6f, 0.09f, 1.14f, -0.39f, 1.14f, -1.0f)
                curveToRelative(0.0f, -0.49f, -0.37f, -0.91f, -0.85f, -0.99f)
                curveToRelative(-1.11f, -0.18f, -2.02f, -0.71f, -2.75f, -1.43f)
                lineToRelative(-1.9f, -1.9f)
                curveToRelative(-0.5f, -0.4f, -1.0f, -0.6f, -1.6f, -0.6f)
                reflectiveCurveToRelative(-1.1f, 0.2f, -1.4f, 0.6f)
                lineTo(7.8f, 8.4f)
                curveToRelative(-0.4f, 0.4f, -0.6f, 0.9f, -0.6f, 1.4f)
                curveToRelative(0.0f, 0.6f, 0.2f, 1.1f, 0.6f, 1.4f)
                lineTo(11.0f, 14.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.4f)
                curveToRelative(0.0f, -0.52f, -0.2f, -1.01f, -0.55f, -1.38f)
                lineTo(10.8f, 10.5f)
                close()
                moveTo(19.0f, 12.0f)
                curveToRelative(-2.8f, 0.0f, -5.0f, 2.2f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.2f, 5.0f, 5.0f, 5.0f)
                reflectiveCurveToRelative(5.0f, -2.2f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.2f, -5.0f, -5.0f, -5.0f)
                close()
                moveTo(19.0f, 20.5f)
                curveToRelative(-1.9f, 0.0f, -3.5f, -1.6f, -3.5f, -3.5f)
                reflectiveCurveToRelative(1.6f, -3.5f, 3.5f, -3.5f)
                reflectiveCurveToRelative(3.5f, 1.6f, 3.5f, 3.5f)
                reflectiveCurveToRelative(-1.6f, 3.5f, -3.5f, 3.5f)
                close()
            }
        }
        return _directionsBike!!
    }

private var _directionsBike: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.DirectionsRun: ImageVector
    get() {
        if (_directionsRun != null) {
            return _directionsRun!!
        }
        _directionsRun = materialIcon(
            name = "AutoMirrored.Rounded.DirectionsRun",
            autoMirror =
            true,
        ) {
            materialPath {
                moveTo(13.49f, 5.48f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(10.32f, 17.48f)
                lineToRelative(0.57f, -2.5f)
                lineToRelative(2.1f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-5.64f)
                curveToRelative(0.0f, -0.55f, -0.22f, -1.07f, -0.62f, -1.45f)
                lineToRelative(-1.48f, -1.41f)
                lineToRelative(0.6f, -3.0f)
                curveToRelative(1.07f, 1.24f, 2.62f, 2.13f, 4.36f, 2.41f)
                curveToRelative(0.6f, 0.09f, 1.14f, -0.39f, 1.14f, -1.0f)
                curveToRelative(0.0f, -0.49f, -0.36f, -0.9f, -0.85f, -0.98f)
                curveToRelative(-1.52f, -0.25f, -2.78f, -1.15f, -3.45f, -2.33f)
                lineToRelative(-1.0f, -1.6f)
                curveToRelative(-0.4f, -0.6f, -1.0f, -1.0f, -1.7f, -1.0f)
                curveToRelative(-0.3f, 0.0f, -0.5f, 0.1f, -0.8f, 0.1f)
                lineTo(7.21f, 7.76f)
                curveToRelative(-0.74f, 0.32f, -1.22f, 1.04f, -1.22f, 1.85f)
                verticalLineToRelative(2.37f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.4f)
                lineToRelative(1.8f, -0.7f)
                lineToRelative(-1.6f, 8.1f)
                lineToRelative(-3.92f, -0.8f)
                curveToRelative(-0.54f, -0.11f, -1.07f, 0.24f, -1.18f, 0.78f)
                lineTo(3.09f, 17.0f)
                curveToRelative(-0.11f, 0.54f, 0.24f, 1.07f, 0.78f, 1.18f)
                lineToRelative(4.11f, 0.82f)
                curveToRelative(1.06f, 0.21f, 2.1f, -0.46f, 2.34f, -1.52f)
                close()
            }
        }
        return _directionsRun!!
    }

private var _directionsRun: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.DirectionsWalk: ImageVector
    get() {
        if (_directionsWalk != null) {
            return _directionsWalk!!
        }
        _directionsWalk = materialIcon(
            name = "AutoMirrored.Rounded.DirectionsWalk",
            autoMirror =
            true,
        ) {
            materialPath {
                moveTo(13.5f, 5.5f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(9.8f, 8.9f)
                lineTo(7.24f, 21.81f)
                curveToRelative(-0.13f, 0.61f, 0.35f, 1.19f, 0.98f, 1.19f)
                horizontalLineToRelative(0.08f)
                curveToRelative(0.47f, 0.0f, 0.87f, -0.32f, 0.98f, -0.78f)
                lineTo(10.9f, 15.0f)
                lineToRelative(2.1f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-5.64f)
                curveToRelative(0.0f, -0.55f, -0.22f, -1.07f, -0.62f, -1.45f)
                lineTo(12.9f, 13.5f)
                lineToRelative(0.6f, -3.0f)
                curveToRelative(1.07f, 1.24f, 2.62f, 2.13f, 4.36f, 2.41f)
                curveToRelative(0.6f, 0.09f, 1.14f, -0.39f, 1.14f, -1.0f)
                curveToRelative(0.0f, -0.49f, -0.36f, -0.9f, -0.85f, -0.98f)
                curveToRelative(-1.52f, -0.25f, -2.78f, -1.15f, -3.45f, -2.33f)
                lineToRelative(-1.0f, -1.6f)
                curveToRelative(-0.56f, -0.89f, -1.68f, -1.25f, -2.65f, -0.84f)
                lineTo(7.22f, 7.78f)
                curveTo(6.48f, 8.1f, 6.0f, 8.82f, 6.0f, 9.63f)
                verticalLineTo(12.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(9.6f)
                lineToRelative(1.8f, -0.7f)
            }
        }
        return _directionsWalk!!
    }

private var _directionsWalk: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Help: ImageVector
    get() {
        if (_help != null) {
            return _help!!
        }
        _help = materialIcon(name = "AutoMirrored.Rounded.Help", autoMirror = true) {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(15.07f, 11.25f)
                lineToRelative(-0.9f, 0.92f)
                curveToRelative(-0.5f, 0.51f, -0.86f, 0.97f, -1.04f, 1.69f)
                curveToRelative(-0.08f, 0.32f, -0.13f, 0.68f, -0.13f, 1.14f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-0.5f)
                curveToRelative(0.0f, -0.46f, 0.08f, -0.9f, 0.22f, -1.31f)
                curveToRelative(0.2f, -0.58f, 0.53f, -1.1f, 0.95f, -1.52f)
                lineToRelative(1.24f, -1.26f)
                curveToRelative(0.46f, -0.44f, 0.68f, -1.1f, 0.55f, -1.8f)
                curveToRelative(-0.13f, -0.72f, -0.69f, -1.33f, -1.39f, -1.53f)
                curveToRelative(-1.11f, -0.31f, -2.14f, 0.32f, -2.47f, 1.27f)
                curveToRelative(-0.12f, 0.37f, -0.43f, 0.65f, -0.82f, 0.65f)
                horizontalLineToRelative(-0.3f)
                curveTo(8.4f, 9.0f, 8.0f, 8.44f, 8.16f, 7.88f)
                curveToRelative(0.43f, -1.47f, 1.68f, -2.59f, 3.23f, -2.83f)
                curveToRelative(1.52f, -0.24f, 2.97f, 0.55f, 3.87f, 1.8f)
                curveToRelative(1.18f, 1.63f, 0.83f, 3.38f, -0.19f, 4.4f)
                close()
            }
        }
        return _help!!
    }

private var _help: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Login: ImageVector
    get() {
        if (_login != null) {
            return _login!!
        }
        _login = materialIcon(name = "AutoMirrored.Rounded.Login", autoMirror = true) {
            materialPath {
                moveTo(10.3f, 7.7f)
                lineTo(10.3f, 7.7f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.01f, 0.0f, 1.4f)
                lineToRelative(1.9f, 1.9f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(9.2f)
                lineToRelative(-1.9f, 1.9f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.01f, 0.0f, 1.4f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, 0.39f, 1.01f, 0.39f, 1.4f, 0.0f)
                lineToRelative(3.59f, -3.59f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(11.7f, 7.7f)
                curveTo(11.31f, 7.31f, 10.69f, 7.31f, 10.3f, 7.7f)
                close()
                moveTo(20.0f, 19.0f)
                horizontalLineToRelative(-7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(19.0f)
                close()
            }
        }
        return _login!!
    }

private var _login: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Logout: ImageVector
    get() {
        if (_logout != null) {
            return _logout!!
        }
        _logout = materialIcon(name = "AutoMirrored.Rounded.Logout", autoMirror = true) {
            materialPath {
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                close()
            }
            materialPath {
                moveTo(20.65f, 11.65f)
                lineToRelative(-2.79f, -2.79f)
                curveTo(17.54f, 8.54f, 17.0f, 8.76f, 17.0f, 9.21f)
                verticalLineTo(11.0f)
                horizontalLineToRelative(-7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(1.79f)
                curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
                lineToRelative(2.79f, -2.79f)
                curveTo(20.84f, 12.16f, 20.84f, 11.84f, 20.65f, 11.65f)
                close()
            }
        }
        return _logout!!
    }

private var _logout: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.MenuBook: ImageVector
    get() {
        if (_menuBook != null) {
            return _menuBook!!
        }
        _menuBook = materialIcon(name = "AutoMirrored.Rounded.MenuBook", autoMirror = true) {
            materialPath {
                moveTo(17.5f, 4.5f)
                curveToRelative(-1.95f, 0.0f, -4.05f, 0.4f, -5.5f, 1.5f)
                curveToRelative(-1.45f, -1.1f, -3.55f, -1.5f, -5.5f, -1.5f)
                curveToRelative(-1.45f, 0.0f, -2.99f, 0.22f, -4.28f, 0.79f)
                curveTo(1.49f, 5.62f, 1.0f, 6.33f, 1.0f, 7.14f)
                lineToRelative(0.0f, 11.28f)
                curveToRelative(0.0f, 1.3f, 1.22f, 2.26f, 2.48f, 1.94f)
                curveTo(4.46f, 20.11f, 5.5f, 20.0f, 6.5f, 20.0f)
                curveToRelative(1.56f, 0.0f, 3.22f, 0.26f, 4.56f, 0.92f)
                curveToRelative(0.6f, 0.3f, 1.28f, 0.3f, 1.87f, 0.0f)
                curveToRelative(1.34f, -0.67f, 3.0f, -0.92f, 4.56f, -0.92f)
                curveToRelative(1.0f, 0.0f, 2.04f, 0.11f, 3.02f, 0.36f)
                curveToRelative(1.26f, 0.33f, 2.48f, -0.63f, 2.48f, -1.94f)
                lineToRelative(0.0f, -11.28f)
                curveToRelative(0.0f, -0.81f, -0.49f, -1.52f, -1.22f, -1.85f)
                curveTo(20.49f, 4.72f, 18.95f, 4.5f, 17.5f, 4.5f)
                close()
                moveTo(21.0f, 17.23f)
                curveToRelative(0.0f, 0.63f, -0.58f, 1.09f, -1.2f, 0.98f)
                curveToRelative(-0.75f, -0.14f, -1.53f, -0.2f, -2.3f, -0.2f)
                curveToRelative(-1.7f, 0.0f, -4.15f, 0.65f, -5.5f, 1.5f)
                verticalLineTo(8.0f)
                curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
                curveToRelative(0.92f, 0.0f, 1.83f, 0.09f, 2.7f, 0.28f)
                curveToRelative(0.46f, 0.1f, 0.8f, 0.51f, 0.8f, 0.98f)
                verticalLineTo(17.23f)
                close()
            }
            materialPath {
                moveTo(13.98f, 11.01f)
                curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
                curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
                curveToRelative(1.54f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
                curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
                curveToRelative(-0.05f, 0.41f, -0.42f, 0.71f, -0.83f, 0.66f)
                curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
                curveTo(14.13f, 10.99f, 14.05f, 11.01f, 13.98f, 11.01f)
                close()
            }
            materialPath {
                moveTo(13.98f, 13.67f)
                curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
                curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
                curveToRelative(1.53f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
                curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
                curveToRelative(-0.05f, 0.41f, -0.42f, 0.71f, -0.83f, 0.66f)
                curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
                curveTo(14.13f, 13.66f, 14.05f, 13.67f, 13.98f, 13.67f)
                close()
            }
            materialPath {
                moveTo(13.98f, 16.33f)
                curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
                curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
                curveToRelative(1.53f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
                curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
                curveToRelative(-0.05f, 0.41f, -0.42f, 0.7f, -0.83f, 0.66f)
                curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
                curveTo(14.13f, 16.32f, 14.05f, 16.33f, 13.98f, 16.33f)
                close()
            }
        }
        return _menuBook!!
    }

private var _menuBook: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Notes: ImageVector
    get() {
        if (_notes != null) {
            return _notes!!
        }
        _notes = materialIcon(name = "AutoMirrored.Rounded.Notes", autoMirror = true) {
            materialPath {
                moveTo(20.0f, 11.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(4.0f, 18.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(20.0f, 6.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.01f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
            }
        }
        return _notes!!
    }

private var _notes: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.OpenInNew: ImageVector
    get() {
        if (_openInNew != null) {
            return _openInNew!!
        }
        _openInNew = materialIcon(name = "AutoMirrored.Rounded.OpenInNew", autoMirror = true) {
            materialPath {
                moveTo(18.0f, 19.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(14.0f, 4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.59f)
                lineToRelative(-9.13f, 9.13f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(19.0f, 6.41f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _openInNew!!
    }

private var _openInNew: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Redo: ImageVector
    get() {
        if (_redo != null) {
            return _redo!!
        }
        _redo = materialIcon(name = "AutoMirrored.Rounded.Redo", autoMirror = true) {
            materialPath {
                moveTo(18.4f, 10.6f)
                curveTo(16.55f, 8.99f, 14.15f, 8.0f, 11.5f, 8.0f)
                curveToRelative(-4.16f, 0.0f, -7.74f, 2.42f, -9.44f, 5.93f)
                curveToRelative(-0.32f, 0.67f, 0.04f, 1.47f, 0.75f, 1.71f)
                curveToRelative(0.59f, 0.2f, 1.23f, -0.08f, 1.5f, -0.64f)
                curveToRelative(1.3f, -2.66f, 4.03f, -4.5f, 7.19f, -4.5f)
                curveToRelative(1.95f, 0.0f, 3.73f, 0.72f, 5.12f, 1.88f)
                lineToRelative(-1.91f, 1.91f)
                curveToRelative(-0.63f, 0.63f, -0.19f, 1.71f, 0.7f, 1.71f)
                horizontalLineTo(21.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(9.41f)
                curveToRelative(0.0f, -0.89f, -1.08f, -1.34f, -1.71f, -0.71f)
                lineToRelative(-1.89f, 1.9f)
                close()
            }
        }
        return _redo!!
    }

private var _redo: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.ShowChart: ImageVector
    get() {
        if (_showChart != null) {
            return _showChart!!
        }
        _showChart = materialIcon(name = "AutoMirrored.Rounded.ShowChart", autoMirror = true) {
            materialPath {
                moveTo(4.2f, 17.78f)
                lineToRelative(5.3f, -5.3f)
                lineToRelative(3.25f, 3.25f)
                curveToRelative(0.41f, 0.41f, 1.07f, 0.39f, 1.45f, -0.04f)
                lineToRelative(7.17f, -8.07f)
                curveToRelative(0.35f, -0.39f, 0.33f, -0.99f, -0.04f, -1.37f)
                curveToRelative(-0.4f, -0.4f, -1.07f, -0.39f, -1.45f, 0.04f)
                lineToRelative(-6.39f, 7.18f)
                lineToRelative(-3.29f, -3.29f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-6.09f, 6.1f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.09f, 0.09f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.41f, 0.0f)
                close()
            }
        }
        return _showChart!!
    }

private var _showChart: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.TrendingDown: ImageVector
    get() {
        if (_trendingDown != null) {
            return _trendingDown!!
        }
        _trendingDown = materialIcon(
            name = "AutoMirrored.Rounded.TrendingDown",
            autoMirror =
            true,
        ) {
            materialPath {
                moveTo(16.85f, 17.15f)
                lineToRelative(1.44f, -1.44f)
                lineToRelative(-4.88f, -4.88f)
                lineToRelative(-3.29f, 3.29f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                lineToRelative(-6.0f, -6.01f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineTo(9.41f, 12.0f)
                lineToRelative(3.29f, -3.29f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(5.59f, 5.58f)
                lineToRelative(1.44f, -1.44f)
                curveToRelative(0.31f, -0.31f, 0.85f, -0.09f, 0.85f, 0.35f)
                verticalLineToRelative(4.29f)
                curveToRelative(0.0f, 0.28f, -0.22f, 0.5f, -0.5f, 0.5f)
                horizontalLineTo(17.2f)
                curveToRelative(-0.44f, 0.01f, -0.66f, -0.53f, -0.35f, -0.84f)
                close()
            }
        }
        return _trendingDown!!
    }

private var _trendingDown: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.TrendingUp: ImageVector
    get() {
        if (_trendingUp != null) {
            return _trendingUp!!
        }
        _trendingUp = materialIcon(name = "AutoMirrored.Rounded.TrendingUp", autoMirror = true) {
            materialPath {
                moveTo(16.85f, 6.85f)
                lineToRelative(1.44f, 1.44f)
                lineToRelative(-4.88f, 4.88f)
                lineToRelative(-3.29f, -3.29f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-6.0f, 6.01f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(9.41f, 12.0f)
                lineToRelative(3.29f, 3.29f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(5.59f, -5.58f)
                lineToRelative(1.44f, 1.44f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.35f)
                verticalLineTo(6.5f)
                curveToRelative(0.01f, -0.28f, -0.21f, -0.5f, -0.49f, -0.5f)
                horizontalLineToRelative(-4.29f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.36f, 0.85f)
                close()
            }
        }
        return _trendingUp!!
    }

private var _trendingUp: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.Undo: ImageVector
    get() {
        if (_undo != null) {
            return _undo!!
        }
        _undo = materialIcon(name = "AutoMirrored.Rounded.Undo", autoMirror = true) {
            materialPath {
                moveTo(12.5f, 8.0f)
                curveToRelative(-2.65f, 0.0f, -5.05f, 0.99f, -6.9f, 2.6f)
                lineTo(3.71f, 8.71f)
                curveTo(3.08f, 8.08f, 2.0f, 8.52f, 2.0f, 9.41f)
                verticalLineTo(15.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.59f)
                curveToRelative(0.89f, 0.0f, 1.34f, -1.08f, 0.71f, -1.71f)
                lineToRelative(-1.91f, -1.91f)
                curveToRelative(1.39f, -1.16f, 3.16f, -1.88f, 5.12f, -1.88f)
                curveToRelative(3.16f, 0.0f, 5.89f, 1.84f, 7.19f, 4.5f)
                curveToRelative(0.27f, 0.56f, 0.91f, 0.84f, 1.5f, 0.64f)
                curveToRelative(0.71f, -0.23f, 1.07f, -1.04f, 0.75f, -1.72f)
                curveTo(20.23f, 10.42f, 16.65f, 8.0f, 12.5f, 8.0f)
                close()
            }
        }
        return _undo!!
    }

private var _undo: ImageVector? = null

internal val Icons.AutoMirrored.Rounded.VolumeUp: ImageVector
    get() {
        if (_volumeUp != null) {
            return _volumeUp!!
        }
        _volumeUp = materialIcon(name = "AutoMirrored.Rounded.VolumeUp", autoMirror = true) {
            materialPath {
                moveTo(3.0f, 10.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                lineToRelative(3.29f, 3.29f)
                curveToRelative(0.63f, 0.63f, 1.71f, 0.18f, 1.71f, -0.71f)
                lineTo(12.0f, 6.41f)
                curveToRelative(0.0f, -0.89f, -1.08f, -1.34f, -1.71f, -0.71f)
                lineTo(7.0f, 9.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(16.5f, 12.0f)
                curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                verticalLineToRelative(8.05f)
                curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                close()
                moveTo(14.0f, 4.45f)
                verticalLineToRelative(0.2f)
                curveToRelative(0.0f, 0.38f, 0.25f, 0.71f, 0.6f, 0.85f)
                curveTo(17.18f, 6.53f, 19.0f, 9.06f, 19.0f, 12.0f)
                reflectiveCurveToRelative(-1.82f, 5.47f, -4.4f, 6.5f)
                curveToRelative(-0.36f, 0.14f, -0.6f, 0.47f, -0.6f, 0.85f)
                verticalLineToRelative(0.2f)
                curveToRelative(0.0f, 0.63f, 0.63f, 1.07f, 1.21f, 0.85f)
                curveTo(18.6f, 19.11f, 21.0f, 15.84f, 21.0f, 12.0f)
                reflectiveCurveToRelative(-2.4f, -7.11f, -5.79f, -8.4f)
                curveToRelative(-0.58f, -0.23f, -1.21f, 0.22f, -1.21f, 0.85f)
                close()
            }
        }
        return _volumeUp!!
    }

private var _volumeUp: ImageVector? = null
