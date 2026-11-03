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

// The rounded Material icons this module draws that material-icons-core lacks, copied from
// androidx.compose.material:material-icons-extended 1.7.8 (only made internal) so the module
// doesn't depend on that artifact. Copyright 2025 The Android Open Source Project, licensed under
// the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES).

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Icons.Rounded.Accessibility: ImageVector
    get() {
        if (_accessibility != null) {
            return _accessibility!!
        }
        _accessibility = materialIcon(name = "Rounded.Accessibility") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                close()
                moveTo(20.0f, 9.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(9.0f, 9.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _accessibility!!
    }

private var _accessibility: ImageVector? = null

internal val Icons.Rounded.AccountBalance: ImageVector
    get() {
        if (_accountBalance != null) {
            return _accountBalance!!
        }
        _accountBalance = materialIcon(name = "Rounded.AccountBalance") {
            materialPath {
                moveTo(4.0f, 11.5f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveTo(7.0f, 16.33f, 7.0f, 15.5f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(4.0f, 10.67f, 4.0f, 11.5f)
                close()
                moveTo(10.0f, 11.5f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
                close()
                moveTo(3.5f, 22.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
                horizontalLineToRelative(-16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
                reflectiveCurveTo(2.67f, 22.0f, 3.5f, 22.0f)
                close()
                moveTo(16.0f, 11.5f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
                close()
                moveTo(10.57f, 1.49f)
                lineToRelative(-7.9f, 4.16f)
                curveToRelative(-0.41f, 0.21f, -0.67f, 0.64f, -0.67f, 1.1f)
                curveTo(2.0f, 7.44f, 2.56f, 8.0f, 3.25f, 8.0f)
                horizontalLineToRelative(16.51f)
                curveTo(20.44f, 8.0f, 21.0f, 7.44f, 21.0f, 6.75f)
                curveToRelative(0.0f, -0.46f, -0.26f, -0.89f, -0.67f, -1.1f)
                lineToRelative(-7.9f, -4.16f)
                curveToRelative(-0.58f, -0.31f, -1.28f, -0.31f, -1.86f, 0.0f)
                close()
            }
        }
        return _accountBalance!!
    }

private var _accountBalance: ImageVector? = null

internal val Icons.Rounded.AccountBalanceWallet: ImageVector
    get() {
        if (_accountBalanceWallet != null) {
            return _accountBalanceWallet!!
        }
        _accountBalanceWallet = materialIcon(name = "Rounded.AccountBalanceWallet") {
            materialPath {
                moveTo(10.0f, 16.0f)
                lineTo(10.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, 0.89f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(9.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(-9.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                close()
                moveTo(13.0f, 8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(9.0f)
                lineTo(22.0f, 8.0f)
                horizontalLineToRelative(-9.0f)
                close()
                moveTo(16.0f, 13.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }
        return _accountBalanceWallet!!
    }

private var _accountBalanceWallet: ImageVector? = null

internal val Icons.Rounded.AccountTree: ImageVector
    get() {
        if (_accountTree != null) {
            return _accountTree!!
        }
        _accountTree = materialIcon(name = "Rounded.AccountTree") {
            materialPath {
                moveTo(17.0f, 11.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.11f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(9.01f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.11f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 3.0f, 2.0f, 3.9f, 2.0f, 5.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.11f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(7.01f)
                curveToRelative(0.0f, 1.65f, 1.34f, 2.99f, 2.99f, 2.99f)
                horizontalLineTo(15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.11f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.11f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.01f)
                curveTo(13.45f, 16.0f, 13.0f, 15.55f, 13.0f, 15.01f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                curveTo(15.0f, 10.1f, 15.9f, 11.0f, 17.0f, 11.0f)
                close()
            }
        }
        return _accountTree!!
    }

private var _accountTree: ImageVector? = null

internal val Icons.Rounded.AcUnit: ImageVector
    get() {
        if (_acUnit != null) {
            return _acUnit!!
        }
        _acUnit = materialIcon(name = "Rounded.AcUnit") {
            materialPath {
                moveTo(21.0f, 11.0f)
                horizontalLineToRelative(-3.17f)
                lineToRelative(2.54f, -2.54f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                lineTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                lineToRelative(3.95f, -3.95f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.42f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(13.0f, 6.17f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.17f)
                lineTo(8.46f, 3.63f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.42f)
                lineTo(11.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(9.0f)
                lineTo(5.05f, 7.05f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(6.17f, 11.0f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.17f)
                lineToRelative(-2.54f, 2.54f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.42f, 0.0f)
                lineTo(9.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(-3.95f, 3.95f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.42f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(11.0f, 17.83f)
                verticalLineTo(21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.17f)
                lineToRelative(2.54f, 2.54f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.42f)
                lineTo(13.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(3.95f, 3.95f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.42f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(17.83f, 13.0f)
                horizontalLineTo(21.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
            }
        }
        return _acUnit!!
    }

private var _acUnit: ImageVector? = null

internal val Icons.Rounded.Air: ImageVector
    get() {
        if (_air != null) {
            return _air!!
        }
        _air = materialIcon(name = "Rounded.Air") {
            materialPath {
                moveTo(14.35f, 17.95f)
                curveToRelative(-0.28f, 0.89f, -1.01f, 1.62f, -1.9f, 1.9f)
                curveToRelative(-1.51f, 0.48f, -2.94f, -0.23f, -3.59f, -1.42f)
                curveTo(8.51f, 17.78f, 9.03f, 17.0f, 9.77f, 17.0f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.34f, 0.0f, 0.68f, 0.16f, 0.84f, 0.46f)
                curveToRelative(0.17f, 0.32f, 0.5f, 0.54f, 0.89f, 0.54f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.5f)
                curveTo(13.46f, 14.0f, 15.0f, 15.9f, 14.35f, 17.95f)
                close()
                moveTo(18.91f, 5.67f)
                curveToRelative(-0.29f, -1.26f, -1.32f, -2.29f, -2.58f, -2.58f)
                curveToRelative(-1.76f, -0.4f, -3.37f, 0.53f, -4.02f, 1.98f)
                curveTo(12.0f, 5.74f, 12.48f, 6.5f, 13.21f, 6.5f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.39f, 0.0f, 0.75f, -0.22f, 0.9f, -0.57f)
                curveTo(14.34f, 5.38f, 14.87f, 5.0f, 15.5f, 5.0f)
                curveTo(16.33f, 5.0f, 17.0f, 5.67f, 17.0f, 6.5f)
                reflectiveCurveTo(16.33f, 8.0f, 15.5f, 8.0f)
                horizontalLineTo(3.0f)
                curveTo(2.45f, 8.0f, 2.0f, 8.45f, 2.0f, 9.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.5f)
                curveTo(17.7f, 10.0f, 19.43f, 7.96f, 18.91f, 5.67f)
                close()
                moveTo(18.4f, 11.0f)
                lineTo(3.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(15.5f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.63f, -0.38f, 1.16f, -0.93f, 1.39f)
                curveToRelative(-0.36f, 0.15f, -0.57f, 0.51f, -0.57f, 0.9f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.73f, 0.76f, 1.21f, 1.43f, 0.91f)
                curveToRelative(1.43f, -0.64f, 2.35f, -2.21f, 2.0f, -3.93f)
                curveTo(21.59f, 12.13f, 20.07f, 11.0f, 18.4f, 11.0f)
                close()
            }
        }
        return _air!!
    }

private var _air: ImageVector? = null

internal val Icons.Rounded.Alarm: ImageVector
    get() {
        if (_alarm != null) {
            return _alarm!!
        }
        _alarm = materialIcon(name = "Rounded.Alarm") {
            materialPath {
                moveTo(15.87f, 15.25f)
                lineToRelative(-3.37f, -2.0f)
                lineTo(12.5f, 8.72f)
                curveToRelative(0.0f, -0.4f, -0.32f, -0.72f, -0.72f, -0.72f)
                horizontalLineToRelative(-0.06f)
                curveToRelative(-0.4f, 0.0f, -0.72f, 0.32f, -0.72f, 0.72f)
                verticalLineToRelative(4.72f)
                curveToRelative(0.0f, 0.35f, 0.18f, 0.68f, 0.49f, 0.86f)
                lineToRelative(3.65f, 2.19f)
                curveToRelative(0.34f, 0.2f, 0.78f, 0.1f, 0.98f, -0.24f)
                curveToRelative(0.21f, -0.35f, 0.1f, -0.8f, -0.25f, -1.0f)
                close()
                moveTo(21.18f, 5.01f)
                lineTo(18.1f, 2.45f)
                curveToRelative(-0.42f, -0.35f, -1.05f, -0.3f, -1.41f, 0.13f)
                curveToRelative(-0.35f, 0.42f, -0.29f, 1.05f, 0.13f, 1.41f)
                lineToRelative(3.07f, 2.56f)
                curveToRelative(0.42f, 0.35f, 1.05f, 0.3f, 1.41f, -0.13f)
                curveToRelative(0.36f, -0.42f, 0.3f, -1.05f, -0.12f, -1.41f)
                close()
                moveTo(4.1f, 6.55f)
                lineToRelative(3.07f, -2.56f)
                curveToRelative(0.43f, -0.36f, 0.49f, -0.99f, 0.13f, -1.41f)
                curveToRelative(-0.35f, -0.43f, -0.98f, -0.48f, -1.4f, -0.13f)
                lineTo(2.82f, 5.01f)
                curveToRelative(-0.42f, 0.36f, -0.48f, 0.99f, -0.12f, 1.41f)
                curveToRelative(0.35f, 0.43f, 0.98f, 0.48f, 1.4f, 0.13f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
                reflectiveCurveToRelative(4.03f, 9.0f, 9.0f, 9.0f)
                reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
                reflectiveCurveToRelative(-4.03f, -9.0f, -9.0f, -9.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-3.86f, 0.0f, -7.0f, -3.14f, -7.0f, -7.0f)
                reflectiveCurveToRelative(3.14f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.14f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.14f, 7.0f, -7.0f, 7.0f)
                close()
            }
        }
        return _alarm!!
    }

private var _alarm: ImageVector? = null

internal val Icons.Rounded.Album: ImageVector
    get() {
        if (_album != null) {
            return _album!!
        }
        _album = materialIcon(name = "Rounded.Album") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 16.5f)
                curveToRelative(-2.49f, 0.0f, -4.5f, -2.01f, -4.5f, -4.5f)
                reflectiveCurveTo(9.51f, 7.5f, 12.0f, 7.5f)
                reflectiveCurveToRelative(4.5f, 2.01f, 4.5f, 4.5f)
                reflectiveCurveToRelative(-2.01f, 4.5f, -4.5f, 4.5f)
                close()
                moveTo(12.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
            }
        }
        return _album!!
    }

private var _album: ImageVector? = null

internal val Icons.Rounded.AllInclusive: ImageVector
    get() {
        if (_allInclusive != null) {
            return _allInclusive!!
        }
        _allInclusive = materialIcon(name = "Rounded.AllInclusive") {
            materialPath {
                moveTo(20.22f, 6.86f)
                curveToRelative(-2.0f, -0.6f, -4.06f, -0.04f, -5.39f, 1.29f)
                lineTo(12.0f, 10.66f)
                lineTo(10.48f, 12.0f)
                horizontalLineToRelative(0.01f)
                lineTo(7.8f, 14.39f)
                curveToRelative(-0.81f, 0.81f, -1.95f, 1.15f, -3.12f, 0.92f)
                curveToRelative(-1.25f, -0.25f, -2.28f, -1.25f, -2.57f, -2.49f)
                curveToRelative(-0.52f, -2.23f, 1.16f, -4.2f, 3.29f, -4.2f)
                curveToRelative(0.91f, 0.0f, 1.76f, 0.35f, 2.44f, 1.03f)
                lineToRelative(0.47f, 0.41f)
                curveToRelative(0.38f, 0.34f, 0.95f, 0.34f, 1.33f, 0.0f)
                curveToRelative(0.45f, -0.4f, 0.45f, -1.1f, 0.0f, -1.5f)
                lineToRelative(-0.42f, -0.36f)
                curveTo(8.2f, 7.18f, 6.84f, 6.62f, 5.4f, 6.62f)
                curveTo(2.42f, 6.62f, 0.0f, 9.04f, 0.0f, 12.0f)
                reflectiveCurveToRelative(2.42f, 5.38f, 5.4f, 5.38f)
                curveToRelative(1.44f, 0.0f, 2.8f, -0.56f, 3.77f, -1.53f)
                lineToRelative(2.83f, -2.5f)
                lineToRelative(0.01f, 0.01f)
                lineTo(13.52f, 12.0f)
                horizontalLineToRelative(-0.01f)
                lineToRelative(2.69f, -2.39f)
                curveToRelative(0.81f, -0.81f, 1.95f, -1.15f, 3.12f, -0.92f)
                curveToRelative(1.25f, 0.25f, 2.28f, 1.25f, 2.57f, 2.49f)
                curveToRelative(0.52f, 2.23f, -1.16f, 4.2f, -3.29f, 4.2f)
                curveToRelative(-0.9f, 0.0f, -1.76f, -0.35f, -2.44f, -1.03f)
                lineToRelative(-0.48f, -0.42f)
                curveToRelative(-0.38f, -0.34f, -0.95f, -0.34f, -1.33f, 0.0f)
                curveToRelative(-0.45f, 0.4f, -0.45f, 1.1f, 0.0f, 1.5f)
                lineToRelative(0.42f, 0.37f)
                curveToRelative(1.02f, 1.01f, 2.37f, 1.57f, 3.82f, 1.57f)
                curveToRelative(3.27f, 0.0f, 5.86f, -2.9f, 5.33f, -6.25f)
                curveToRelative(-0.3f, -1.99f, -1.77f, -3.69f, -3.7f, -4.26f)
                close()
            }
        }
        return _allInclusive!!
    }

private var _allInclusive: ImageVector? = null

internal val Icons.Rounded.AlternateEmail: ImageVector
    get() {
        if (_alternateEmail != null) {
            return _alternateEmail!!
        }
        _alternateEmail = materialIcon(name = "Rounded.AlternateEmail") {
            materialPath {
                moveTo(12.72f, 2.03f)
                curveTo(6.63f, 1.6f, 1.6f, 6.63f, 2.03f, 12.72f)
                curveTo(2.39f, 18.01f, 7.01f, 22.0f, 12.31f, 22.0f)
                horizontalLineTo(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.67f)
                curveToRelative(-3.73f, 0.0f, -7.15f, -2.42f, -8.08f, -6.03f)
                curveToRelative(-1.49f, -5.8f, 3.91f, -11.21f, 9.71f, -9.71f)
                curveTo(17.58f, 5.18f, 20.0f, 8.6f, 20.0f, 12.33f)
                verticalLineToRelative(1.1f)
                curveToRelative(0.0f, 0.79f, -0.71f, 1.57f, -1.5f, 1.57f)
                reflectiveCurveToRelative(-1.5f, -0.78f, -1.5f, -1.57f)
                verticalLineToRelative(-1.25f)
                curveToRelative(0.0f, -2.51f, -1.78f, -4.77f, -4.26f, -5.12f)
                curveToRelative(-3.4f, -0.49f, -6.27f, 2.45f, -5.66f, 5.87f)
                curveToRelative(0.34f, 1.91f, 1.83f, 3.49f, 3.72f, 3.94f)
                curveToRelative(1.84f, 0.43f, 3.59f, -0.16f, 4.74f, -1.33f)
                curveToRelative(0.89f, 1.22f, 2.67f, 1.86f, 4.3f, 1.21f)
                curveToRelative(1.34f, -0.53f, 2.16f, -1.9f, 2.16f, -3.34f)
                verticalLineToRelative(-1.09f)
                curveToRelative(0.0f, -5.31f, -3.99f, -9.93f, -9.28f, -10.29f)
                close()
                moveTo(12.0f, 15.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                close()
            }
        }
        return _alternateEmail!!
    }

private var _alternateEmail: ImageVector? = null

internal val Icons.Rounded.Anchor: ImageVector
    get() {
        if (_anchor != null) {
            return _anchor!!
        }
        _anchor = materialIcon(name = "Rounded.Anchor") {
            materialPath {
                moveTo(13.0f, 9.0f)
                verticalLineTo(7.82f)
                curveTo(14.16f, 7.4f, 15.0f, 6.3f, 15.0f, 5.0f)
                curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.35f, 9.0f, 5.0f)
                curveToRelative(0.0f, 1.3f, 0.84f, 2.4f, 2.0f, 2.82f)
                verticalLineTo(9.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(8.92f)
                curveToRelative(-2.22f, -0.33f, -4.59f, -1.68f, -5.55f, -3.37f)
                lineToRelative(1.14f, -1.14f)
                curveToRelative(0.22f, -0.22f, 0.19f, -0.57f, -0.05f, -0.75f)
                lineTo(3.8f, 12.6f)
                curveTo(3.47f, 12.35f, 3.0f, 12.59f, 3.0f, 13.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 3.88f, 4.92f, 7.0f, 9.0f, 7.0f)
                reflectiveCurveToRelative(9.0f, -3.12f, 9.0f, -7.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.41f, -0.47f, -0.65f, -0.8f, -0.4f)
                lineToRelative(-2.74f, 2.05f)
                curveToRelative(-0.24f, 0.18f, -0.27f, 0.54f, -0.05f, 0.75f)
                lineToRelative(1.14f, 1.14f)
                curveToRelative(-0.96f, 1.69f, -3.33f, 3.04f, -5.55f, 3.37f)
                verticalLineTo(11.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(13.0f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveTo(11.45f, 4.0f, 12.0f, 4.0f)
                close()
            }
        }
        return _anchor!!
    }

private var _anchor: ImageVector? = null

internal val Icons.Rounded.Apartment: ImageVector
    get() {
        if (_apartment != null) {
            return _apartment!!
        }
        _apartment = materialIcon(name = "Rounded.Apartment") {
            materialPath {
                moveTo(17.0f, 11.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(9.0f)
                curveTo(7.9f, 3.0f, 7.0f, 3.9f, 7.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 7.0f, 3.0f, 7.9f, 3.0f, 9.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(17.0f)
                close()
                moveTo(7.0f, 19.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(7.0f, 15.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(7.0f, 11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 15.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(11.0f, 7.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(15.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(15.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(19.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
            }
        }
        return _apartment!!
    }

private var _apartment: ImageVector? = null

internal val Icons.Rounded.Archive: ImageVector
    get() {
        if (_archive != null) {
            return _archive!!
        }
        _archive = materialIcon(name = "Rounded.Archive") {
            materialPath {
                moveTo(20.54f, 5.23f)
                lineToRelative(-1.39f, -1.68f)
                curveTo(18.88f, 3.21f, 18.47f, 3.0f, 18.0f, 3.0f)
                lineTo(6.0f, 3.0f)
                curveToRelative(-0.47f, 0.0f, -0.88f, 0.21f, -1.16f, 0.55f)
                lineTo(3.46f, 5.23f)
                curveTo(3.17f, 5.57f, 3.0f, 6.02f, 3.0f, 6.5f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 6.5f)
                curveToRelative(0.0f, -0.48f, -0.17f, -0.93f, -0.46f, -1.27f)
                close()
                moveTo(11.65f, 17.15f)
                lineTo(6.5f, 12.0f)
                lineTo(10.0f, 12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.5f)
                lineToRelative(-5.15f, 5.15f)
                curveToRelative(-0.19f, 0.19f, -0.51f, 0.19f, -0.7f, 0.0f)
                close()
                moveTo(5.12f, 5.0f)
                lineToRelative(0.81f, -1.0f)
                horizontalLineToRelative(12.0f)
                lineToRelative(0.94f, 1.0f)
                lineTo(5.12f, 5.0f)
                close()
            }
        }
        return _archive!!
    }

private var _archive: ImageVector? = null

internal val Icons.Rounded.AreaChart: ImageVector
    get() {
        if (_areaChart != null) {
            return _areaChart!!
        }
        _areaChart = materialIcon(name = "Rounded.AreaChart") {
            materialPath {
                moveTo(8.0f, 17.0f)
                lineToRelative(3.39f, -4.66f)
                curveToRelative(0.33f, -0.46f, 0.98f, -0.55f, 1.42f, -0.2f)
                lineTo(21.0f, 18.5f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineToRelative(0.0f, -5.72f)
                curveToRelative(0.22f, 0.0f, 0.44f, 0.07f, 0.62f, 0.22f)
                lineTo(8.0f, 17.0f)
                close()
                moveTo(3.0f, 11.0f)
                curveToRelative(0.44f, 0.0f, 0.88f, 0.15f, 1.25f, 0.44f)
                lineToRelative(3.37f, 2.69f)
                lineToRelative(2.77f, -3.81f)
                curveToRelative(0.66f, -0.91f, 1.95f, -1.1f, 2.85f, -0.4f)
                lineTo(21.0f, 15.97f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-4.18f, -3.34f)
                curveToRelative(-0.45f, -0.36f, -1.1f, -0.27f, -1.44f, 0.2f)
                lineTo(7.0f, 10.0f)
                lineTo(3.6f, 7.45f)
                curveTo(3.42f, 7.32f, 3.21f, 7.25f, 3.0f, 7.25f)
                lineTo(3.0f, 11.0f)
                close()
            }
        }
        return _areaChart!!
    }

private var _areaChart: ImageVector? = null

internal val Icons.Rounded.ArrowDownward: ImageVector
    get() {
        if (_arrowDownward != null) {
            return _arrowDownward!!
        }
        _arrowDownward = materialIcon(name = "Rounded.ArrowDownward") {
            materialPath {
                moveTo(11.0f, 5.0f)
                verticalLineToRelative(11.17f)
                lineToRelative(-4.88f, -4.88f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(6.59f, 6.59f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(6.59f, -6.59f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(13.0f, 16.17f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _arrowDownward!!
    }

private var _arrowDownward: ImageVector? = null

internal val Icons.Rounded.ArrowUpward: ImageVector
    get() {
        if (_arrowUpward != null) {
            return _arrowUpward!!
        }
        _arrowUpward = materialIcon(name = "Rounded.ArrowUpward") {
            materialPath {
                moveTo(13.0f, 19.0f)
                verticalLineTo(7.83f)
                lineToRelative(4.88f, 4.88f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.42f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-6.59f, -6.59f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-6.6f, 6.58f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(11.0f, 7.83f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                close()
            }
        }
        return _arrowUpward!!
    }

private var _arrowUpward: ImageVector? = null

internal val Icons.Rounded.AttachFile: ImageVector
    get() {
        if (_attachFile != null) {
            return _attachFile!!
        }
        _attachFile = materialIcon(name = "Rounded.AttachFile") {
            materialPath {
                moveTo(16.5f, 6.75f)
                verticalLineToRelative(10.58f)
                curveToRelative(0.0f, 2.09f, -1.53f, 3.95f, -3.61f, 4.15f)
                curveToRelative(-2.39f, 0.23f, -4.39f, -1.64f, -4.39f, -3.98f)
                verticalLineTo(5.14f)
                curveToRelative(0.0f, -1.31f, 0.94f, -2.5f, 2.24f, -2.63f)
                curveToRelative(1.5f, -0.15f, 2.76f, 1.02f, 2.76f, 2.49f)
                verticalLineToRelative(10.5f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(6.75f)
                curveToRelative(0.0f, -0.41f, -0.34f, -0.75f, -0.75f, -0.75f)
                reflectiveCurveToRelative(-0.75f, 0.34f, -0.75f, 0.75f)
                verticalLineToRelative(8.61f)
                curveToRelative(0.0f, 1.31f, 0.94f, 2.5f, 2.24f, 2.63f)
                curveToRelative(1.5f, 0.15f, 2.76f, -1.02f, 2.76f, -2.49f)
                verticalLineTo(5.17f)
                curveToRelative(0.0f, -2.09f, -1.53f, -3.95f, -3.61f, -4.15f)
                curveTo(9.01f, 0.79f, 7.0f, 2.66f, 7.0f, 5.0f)
                verticalLineToRelative(12.27f)
                curveToRelative(0.0f, 2.87f, 2.1f, 5.44f, 4.96f, 5.71f)
                curveToRelative(3.29f, 0.3f, 6.04f, -2.26f, 6.04f, -5.48f)
                verticalLineTo(6.75f)
                curveToRelative(0.0f, -0.41f, -0.34f, -0.75f, -0.75f, -0.75f)
                reflectiveCurveToRelative(-0.75f, 0.34f, -0.75f, 0.75f)
                close()
            }
        }
        return _attachFile!!
    }

private var _attachFile: ImageVector? = null

internal val Icons.Rounded.AttachMoney: ImageVector
    get() {
        if (_attachMoney != null) {
            return _attachMoney!!
        }
        _attachMoney = materialIcon(name = "Rounded.AttachMoney") {
            materialPath {
                moveTo(11.8f, 10.9f)
                curveToRelative(-2.27f, -0.59f, -3.0f, -1.2f, -3.0f, -2.15f)
                curveToRelative(0.0f, -1.09f, 1.01f, -1.85f, 2.7f, -1.85f)
                curveToRelative(1.42f, 0.0f, 2.13f, 0.54f, 2.39f, 1.4f)
                curveToRelative(0.12f, 0.4f, 0.45f, 0.7f, 0.87f, 0.7f)
                horizontalLineToRelative(0.3f)
                curveToRelative(0.66f, 0.0f, 1.13f, -0.65f, 0.9f, -1.27f)
                curveToRelative(-0.42f, -1.18f, -1.4f, -2.16f, -2.96f, -2.54f)
                verticalLineTo(4.5f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(10.0f, 3.67f, 10.0f, 4.5f)
                verticalLineToRelative(0.66f)
                curveToRelative(-1.94f, 0.42f, -3.5f, 1.68f, -3.5f, 3.61f)
                curveToRelative(0.0f, 2.31f, 1.91f, 3.46f, 4.7f, 4.13f)
                curveToRelative(2.5f, 0.6f, 3.0f, 1.48f, 3.0f, 2.41f)
                curveToRelative(0.0f, 0.69f, -0.49f, 1.79f, -2.7f, 1.79f)
                curveToRelative(-1.65f, 0.0f, -2.5f, -0.59f, -2.83f, -1.43f)
                curveToRelative(-0.15f, -0.39f, -0.49f, -0.67f, -0.9f, -0.67f)
                horizontalLineToRelative(-0.28f)
                curveToRelative(-0.67f, 0.0f, -1.14f, 0.68f, -0.89f, 1.3f)
                curveToRelative(0.57f, 1.39f, 1.9f, 2.21f, 3.4f, 2.53f)
                verticalLineToRelative(0.67f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineToRelative(-0.65f)
                curveToRelative(1.95f, -0.37f, 3.5f, -1.5f, 3.5f, -3.55f)
                curveToRelative(0.0f, -2.84f, -2.43f, -3.81f, -4.7f, -4.4f)
                close()
            }
        }
        return _attachMoney!!
    }

private var _attachMoney: ImageVector? = null

internal val Icons.Rounded.Attractions: ImageVector
    get() {
        if (_attractions != null) {
            return _attractions!!
        }
        _attractions = materialIcon(name = "Rounded.Attractions") {
            materialPath {
                moveTo(10.44f, 18.75f)
                curveToRelative(0.37f, -0.46f, 0.94f, -0.75f, 1.57f, -0.75f)
                reflectiveCurveToRelative(1.19f, 0.29f, 1.56f, 0.75f)
                curveToRelative(0.39f, -0.09f, 0.76f, -0.21f, 1.12f, -0.36f)
                lineToRelative(-1.42f, -3.18f)
                curveToRelative(-0.39f, 0.15f, -0.82f, 0.23f, -1.26f, 0.23f)
                curveToRelative(-0.46f, 0.0f, -0.9f, -0.09f, -1.3f, -0.25f)
                lineToRelative(-1.43f, 3.19f)
                curveTo(9.65f, 18.54f, 10.03f, 18.67f, 10.44f, 18.75f)
                close()
                moveTo(5.16f, 10.0f)
                curveTo(5.0f, 10.59f, 4.91f, 11.21f, 4.91f, 11.85f)
                curveToRelative(0.0f, 0.75f, 0.12f, 1.47f, 0.33f, 2.15f)
                curveToRelative(0.63f, 0.05f, 1.22f, 0.4f, 1.56f, 0.99f)
                curveToRelative(0.33f, 0.57f, 0.35f, 1.23f, 0.11f, 1.79f)
                curveToRelative(0.27f, 0.27f, 0.56f, 0.53f, 0.87f, 0.76f)
                lineToRelative(1.52f, -3.39f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.47f, -0.58f, -0.75f, -1.32f, -0.75f, -2.13f)
                curveToRelative(0.0f, -1.89f, 1.55f, -3.41f, 3.46f, -3.41f)
                reflectiveCurveToRelative(3.46f, 1.53f, 3.46f, 3.41f)
                curveToRelative(0.0f, 0.82f, -0.29f, 1.57f, -0.78f, 2.16f)
                lineToRelative(1.5f, 3.35f)
                curveToRelative(0.32f, -0.24f, 0.62f, -0.5f, 0.9f, -0.79f)
                curveToRelative(-0.22f, -0.55f, -0.2f, -1.2f, 0.12f, -1.75f)
                curveToRelative(0.33f, -0.57f, 0.9f, -0.92f, 1.52f, -0.99f)
                curveToRelative(0.22f, -0.68f, 0.34f, -1.41f, 0.34f, -2.16f)
                curveToRelative(0.0f, -0.64f, -0.09f, -1.27f, -0.25f, -1.86f)
                curveToRelative(-0.64f, -0.04f, -1.26f, -0.39f, -1.6f, -1.0f)
                curveToRelative(-0.36f, -0.62f, -0.35f, -1.36f, -0.03f, -1.95f)
                curveToRelative(-0.91f, -0.98f, -2.1f, -1.71f, -3.44f, -2.05f)
                curveTo(13.4f, 5.6f, 12.74f, 6.0f, 12.01f, 6.0f)
                reflectiveCurveToRelative(-1.39f, -0.41f, -1.74f, -1.01f)
                curveTo(8.93f, 5.33f, 7.74f, 6.04f, 6.83f, 7.02f)
                curveTo(7.16f, 7.62f, 7.18f, 8.37f, 6.81f, 9.0f)
                curveTo(6.45f, 9.62f, 5.82f, 9.97f, 5.16f, 10.0f)
                close()
                moveTo(3.86f, 9.58f)
                curveTo(3.08f, 8.98f, 2.84f, 7.88f, 3.35f, 7.0f)
                reflectiveCurveToRelative(1.58f, -1.23f, 2.49f, -0.85f)
                curveToRelative(1.11f, -1.17f, 2.56f, -2.03f, 4.18f, -2.42f)
                curveTo(10.15f, 2.75f, 10.99f, 2.0f, 12.01f, 2.0f)
                reflectiveCurveToRelative(1.85f, 0.75f, 1.98f, 1.73f)
                curveToRelative(1.63f, 0.39f, 3.07f, 1.24f, 4.18f, 2.42f)
                curveToRelative(0.91f, -0.38f, 1.99f, -0.03f, 2.49f, 0.85f)
                curveToRelative(0.51f, 0.88f, 0.27f, 1.98f, -0.51f, 2.58f)
                curveToRelative(0.23f, 0.77f, 0.35f, 1.58f, 0.35f, 2.42f)
                reflectiveCurveToRelative(-0.12f, 1.65f, -0.35f, 2.42f)
                curveToRelative(0.78f, 0.6f, 1.02f, 1.7f, 0.51f, 2.58f)
                reflectiveCurveToRelative(-1.58f, 1.23f, -2.49f, 0.85f)
                curveToRelative(-0.4f, 0.43f, -0.85f, 0.81f, -1.34f, 1.15f)
                lineToRelative(0.81f, 1.8f)
                curveToRelative(0.25f, 0.56f, -0.16f, 1.2f, -0.78f, 1.2f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.33f, 0.0f, -0.64f, -0.2f, -0.78f, -0.5f)
                lineToRelative(-0.75f, -1.67f)
                curveToRelative(-0.43f, 0.18f, -0.88f, 0.33f, -1.34f, 0.44f)
                curveTo(13.86f, 21.25f, 13.02f, 22.0f, 12.01f, 22.0f)
                reflectiveCurveToRelative(-1.85f, -0.75f, -1.98f, -1.73f)
                curveTo(9.55f, 20.15f, 9.09f, 20.0f, 8.65f, 19.81f)
                lineTo(7.89f, 21.5f)
                curveTo(7.75f, 21.8f, 7.45f, 22.0f, 7.11f, 22.0f)
                horizontalLineTo(7.1f)
                curveToRelative(-0.62f, 0.0f, -1.03f, -0.64f, -0.77f, -1.2f)
                lineToRelative(0.82f, -1.83f)
                curveToRelative(-0.47f, -0.33f, -0.91f, -0.71f, -1.3f, -1.12f)
                curveToRelative(-0.92f, 0.38f, -1.99f, 0.03f, -2.5f, -0.85f)
                reflectiveCurveToRelative(-0.27f, -1.98f, 0.51f, -2.58f)
                curveTo(3.62f, 13.65f, 3.51f, 12.84f, 3.51f, 12.0f)
                reflectiveCurveTo(3.62f, 10.35f, 3.86f, 9.58f)
                close()
            }
        }
        return _attractions!!
    }

private var _attractions: ImageVector? = null

internal val Icons.Rounded.AutoAwesome: ImageVector
    get() {
        if (_autoAwesome != null) {
            return _autoAwesome!!
        }
        _autoAwesome = materialIcon(name = "Rounded.AutoAwesome") {
            materialPath {
                moveTo(19.46f, 8.0f)
                lineToRelative(0.79f, -1.75f)
                lineTo(22.0f, 5.46f)
                curveToRelative(0.39f, -0.18f, 0.39f, -0.73f, 0.0f, -0.91f)
                lineToRelative(-1.75f, -0.79f)
                lineTo(19.46f, 2.0f)
                curveToRelative(-0.18f, -0.39f, -0.73f, -0.39f, -0.91f, 0.0f)
                lineToRelative(-0.79f, 1.75f)
                lineTo(16.0f, 4.54f)
                curveToRelative(-0.39f, 0.18f, -0.39f, 0.73f, 0.0f, 0.91f)
                lineToRelative(1.75f, 0.79f)
                lineTo(18.54f, 8.0f)
                curveTo(18.72f, 8.39f, 19.28f, 8.39f, 19.46f, 8.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.91f, 6.0f)
                curveTo(9.56f, 5.22f, 8.44f, 5.22f, 8.09f, 6.0f)
                lineTo(6.5f, 9.5f)
                lineTo(3.0f, 11.09f)
                curveToRelative(-0.78f, 0.36f, -0.78f, 1.47f, 0.0f, 1.82f)
                lineToRelative(3.5f, 1.59f)
                lineTo(8.09f, 18.0f)
                curveToRelative(0.36f, 0.78f, 1.47f, 0.78f, 1.82f, 0.0f)
                lineToRelative(1.59f, -3.5f)
                lineToRelative(3.5f, -1.59f)
                curveToRelative(0.78f, -0.36f, 0.78f, -1.47f, 0.0f, -1.82f)
                lineTo(11.5f, 9.5f)
                close()
                moveTo(18.54f, 16.0f)
                lineToRelative(-0.79f, 1.75f)
                lineTo(16.0f, 18.54f)
                curveToRelative(-0.39f, 0.18f, -0.39f, 0.73f, 0.0f, 0.91f)
                lineToRelative(1.75f, 0.79f)
                lineTo(18.54f, 22.0f)
                curveToRelative(0.18f, 0.39f, 0.73f, 0.39f, 0.91f, 0.0f)
                lineToRelative(0.79f, -1.75f)
                lineTo(22.0f, 19.46f)
                curveToRelative(0.39f, -0.18f, 0.39f, -0.73f, 0.0f, -0.91f)
                lineToRelative(-1.75f, -0.79f)
                lineTo(19.46f, 16.0f)
                curveTo(19.28f, 15.61f, 18.72f, 15.61f, 18.54f, 16.0f)
                close()
            }
        }
        return _autoAwesome!!
    }

private var _autoAwesome: ImageVector? = null

internal val Icons.Rounded.AutoStories: ImageVector
    get() {
        if (_autoStories != null) {
            return _autoStories!!
        }
        _autoStories = materialIcon(name = "Rounded.AutoStories") {
            materialPath {
                moveTo(18.15f, 1.35f)
                lineToRelative(-4.0f, 4.0f)
                curveTo(14.05f, 5.45f, 14.0f, 5.57f, 14.0f, 5.71f)
                verticalLineToRelative(8.17f)
                curveToRelative(0.0f, 0.43f, 0.51f, 0.66f, 0.83f, 0.37f)
                lineToRelative(4.0f, -3.6f)
                curveToRelative(0.11f, -0.09f, 0.17f, -0.23f, 0.17f, -0.37f)
                verticalLineTo(1.71f)
                curveTo(19.0f, 1.26f, 18.46f, 1.04f, 18.15f, 1.35f)
                close()
                moveTo(22.47f, 5.2f)
                curveTo(22.0f, 4.96f, 21.51f, 4.76f, 21.0f, 4.59f)
                verticalLineToRelative(12.03f)
                curveTo(19.86f, 16.21f, 18.69f, 16.0f, 17.5f, 16.0f)
                curveToRelative(-1.9f, 0.0f, -3.78f, 0.54f, -5.5f, 1.58f)
                verticalLineTo(5.48f)
                curveTo(10.38f, 4.55f, 8.51f, 4.0f, 6.5f, 4.0f)
                curveTo(4.71f, 4.0f, 3.02f, 4.44f, 1.53f, 5.2f)
                curveTo(1.2f, 5.36f, 1.0f, 5.71f, 1.0f, 6.08f)
                verticalLineToRelative(12.08f)
                curveToRelative(0.0f, 0.76f, 0.81f, 1.23f, 1.48f, 0.87f)
                curveTo(3.69f, 18.4f, 5.05f, 18.0f, 6.5f, 18.0f)
                curveToRelative(2.07f, 0.0f, 3.98f, 0.82f, 5.5f, 2.0f)
                curveToRelative(1.52f, -1.18f, 3.43f, -2.0f, 5.5f, -2.0f)
                curveToRelative(1.45f, 0.0f, 2.81f, 0.4f, 4.02f, 1.04f)
                curveTo(22.19f, 19.4f, 23.0f, 18.93f, 23.0f, 18.17f)
                verticalLineTo(6.08f)
                curveTo(23.0f, 5.71f, 22.8f, 5.36f, 22.47f, 5.2f)
                close()
            }
        }
        return _autoStories!!
    }

private var _autoStories: ImageVector? = null

internal val Icons.Rounded.Backpack: ImageVector
    get() {
        if (_backpack != null) {
            return _backpack!!
        }
        _backpack = materialIcon(name = "Rounded.Backpack") {
            materialPath {
                moveTo(20.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -1.86f, 1.28f, -3.41f, 3.0f, -3.86f)
                verticalLineTo(3.5f)
                curveTo(7.0f, 2.67f, 7.67f, 2.0f, 8.5f, 2.0f)
                horizontalLineToRelative(0.0f)
                curveTo(9.33f, 2.0f, 10.0f, 2.67f, 10.0f, 3.5f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(3.5f)
                curveTo(14.0f, 2.67f, 14.67f, 2.0f, 15.5f, 2.0f)
                horizontalLineToRelative(0.0f)
                curveTo(16.33f, 2.0f, 17.0f, 2.67f, 17.0f, 3.5f)
                verticalLineToRelative(0.64f)
                curveTo(18.72f, 4.59f, 20.0f, 6.14f, 20.0f, 8.0f)
                close()
                moveTo(6.0f, 13.0f)
                lineTo(6.0f, 13.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(9.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(7.0f)
                curveTo(6.45f, 12.0f, 6.0f, 12.45f, 6.0f, 13.0f)
                close()
            }
        }
        return _backpack!!
    }

private var _backpack: ImageVector? = null

internal val Icons.Rounded.Badge: ImageVector
    get() {
        if (_badge != null) {
            return _badge!!
        }
        _badge = materialIcon(name = "Rounded.Badge") {
            materialPath {
                moveTo(20.0f, 7.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(9.9f, 2.0f, 9.0f, 2.9f, 9.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 7.0f, 2.0f, 7.9f, 2.0f, 9.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(9.0f)
                curveTo(22.0f, 7.9f, 21.1f, 7.0f, 20.0f, 7.0f)
                close()
                moveTo(9.0f, 12.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                reflectiveCurveToRelative(-1.5f, -0.67f, -1.5f, -1.5f)
                curveTo(7.5f, 12.67f, 8.17f, 12.0f, 9.0f, 12.0f)
                close()
                moveTo(12.0f, 18.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-0.43f)
                curveToRelative(0.0f, -0.6f, 0.36f, -1.15f, 0.92f, -1.39f)
                curveTo(7.56f, 15.9f, 8.26f, 15.75f, 9.0f, 15.75f)
                reflectiveCurveToRelative(1.44f, 0.15f, 2.08f, 0.43f)
                curveToRelative(0.55f, 0.24f, 0.92f, 0.78f, 0.92f, 1.39f)
                verticalLineTo(18.0f)
                close()
                moveTo(13.0f, 9.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(17.25f, 16.5f)
                horizontalLineToRelative(-2.5f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(2.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(0.0f)
                curveTo(18.0f, 16.16f, 17.66f, 16.5f, 17.25f, 16.5f)
                close()
                moveTo(17.25f, 13.5f)
                horizontalLineToRelative(-2.5f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(2.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(0.0f)
                curveTo(18.0f, 13.16f, 17.66f, 13.5f, 17.25f, 13.5f)
                close()
            }
        }
        return _badge!!
    }

private var _badge: ImageVector? = null

internal val Icons.Rounded.BakeryDining: ImageVector
    get() {
        if (_bakeryDining != null) {
            return _bakeryDining!!
        }
        _bakeryDining = materialIcon(name = "Rounded.BakeryDining") {
            materialPath {
                moveTo(18.77f, 8.55f)
                lineTo(17.6f, 8.08f)
                curveToRelative(-0.62f, -0.25f, -1.31f, 0.17f, -1.37f, 0.84f)
                lineToRelative(-0.74f, 8.08f)
                horizontalLineTo(17.0f)
                lineToRelative(2.6f, -6.5f)
                curveTo(19.91f, 9.73f, 19.54f, 8.85f, 18.77f, 8.55f)
                close()
            }
            materialPath {
                moveTo(6.4f, 8.08f)
                lineTo(5.23f, 8.55f)
                curveTo(4.46f, 8.85f, 4.09f, 9.73f, 4.4f, 10.5f)
                lineToRelative(2.6f, 6.5f)
                horizontalLineToRelative(1.5f)
                lineTo(7.76f, 8.92f)
                curveTo(7.7f, 8.25f, 7.02f, 7.83f, 6.4f, 8.08f)
                close()
            }
            materialPath {
                moveTo(13.36f, 6.0f)
                horizontalLineToRelative(-2.71f)
                curveTo(9.76f, 6.0f, 9.07f, 6.76f, 9.15f, 7.64f)
                lineTo(10.0f, 16.99f)
                horizontalLineToRelative(4.0f)
                lineToRelative(0.85f, -9.36f)
                curveTo(14.93f, 6.76f, 14.24f, 6.0f, 13.36f, 6.0f)
                close()
            }
            materialPath {
                moveTo(3.18f, 13.72f)
                lineToRelative(-1.0f, 1.93f)
                curveToRelative(-0.19f, 0.36f, -0.23f, 0.78f, -0.12f, 1.19f)
                curveToRelative(0.29f, 1.01f, 1.43f, 1.41f, 2.38f, 0.94f)
                lineToRelative(1.05f, -0.52f)
                lineToRelative(-1.4f, -3.49f)
                curveTo(3.93f, 13.37f, 3.38f, 13.34f, 3.18f, 13.72f)
                close()
            }
            materialPath {
                moveTo(21.82f, 15.65f)
                lineToRelative(-1.0f, -1.93f)
                curveToRelative(-0.2f, -0.38f, -0.75f, -0.35f, -0.91f, 0.04f)
                lineToRelative(-1.4f, 3.49f)
                lineToRelative(1.05f, 0.52f)
                curveToRelative(0.94f, 0.47f, 2.09f, 0.07f, 2.38f, -0.94f)
                curveTo(22.05f, 16.43f, 22.01f, 16.01f, 21.82f, 15.65f)
                close()
            }
        }
        return _bakeryDining!!
    }

private var _bakeryDining: ImageVector? = null

internal val Icons.Rounded.BarChart: ImageVector
    get() {
        if (_barChart != null) {
            return _barChart!!
        }
        _barChart = materialIcon(name = "Rounded.BarChart") {
            materialPath {
                moveTo(6.0f, 20.0f)
                lineTo(6.0f, 20.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(7.0f)
                curveTo(4.0f, 19.1f, 4.9f, 20.0f, 6.0f, 20.0f)
                close()
            }
            materialPath {
                moveTo(16.0f, 15.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(0.0f)
                curveTo(16.9f, 13.0f, 16.0f, 13.9f, 16.0f, 15.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 20.0f)
                lineTo(12.0f, 20.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveTo(10.0f, 19.1f, 10.9f, 20.0f, 12.0f, 20.0f)
                close()
            }
        }
        return _barChart!!
    }

private var _barChart: ImageVector? = null

internal val Icons.Rounded.BatteryFull: ImageVector
    get() {
        if (_batteryFull != null) {
            return _batteryFull!!
        }
        _batteryFull = materialIcon(name = "Rounded.BatteryFull") {
            materialPath {
                moveTo(15.67f, 4.0f)
                horizontalLineTo(14.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(8.33f)
                curveTo(7.6f, 4.0f, 7.0f, 4.6f, 7.0f, 5.33f)
                verticalLineToRelative(15.33f)
                curveTo(7.0f, 21.4f, 7.6f, 22.0f, 8.34f, 22.0f)
                horizontalLineToRelative(7.32f)
                curveToRelative(0.74f, 0.0f, 1.34f, -0.6f, 1.34f, -1.33f)
                verticalLineTo(5.33f)
                curveTo(17.0f, 4.6f, 16.4f, 4.0f, 15.67f, 4.0f)
                close()
            }
        }
        return _batteryFull!!
    }

private var _batteryFull: ImageVector? = null

internal val Icons.Rounded.BeachAccess: ImageVector
    get() {
        if (_beachAccess != null) {
            return _beachAccess!!
        }
        _beachAccess = materialIcon(name = "Rounded.BeachAccess") {
            materialPath {
                moveTo(13.13f, 14.56f)
                lineToRelative(1.43f, -1.43f)
                lineToRelative(5.73f, 5.73f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.03f, 0.0f, 1.43f)
                curveToRelative(-0.39f, 0.39f, -1.03f, 0.39f, -1.43f, 0.0f)
                lineToRelative(-5.73f, -5.73f)
                close()
                moveTo(17.42f, 8.83f)
                lineToRelative(1.27f, -1.27f)
                curveToRelative(0.89f, -0.89f, 0.77f, -2.43f, -0.31f, -3.08f)
                curveToRelative(-3.89f, -2.38f, -9.03f, -1.89f, -12.4f, 1.47f)
                curveToRelative(3.93f, -1.3f, 8.31f, -0.25f, 11.44f, 2.88f)
                close()
                moveTo(5.95f, 5.98f)
                curveToRelative(-3.36f, 3.37f, -3.85f, 8.51f, -1.48f, 12.4f)
                curveToRelative(0.66f, 1.08f, 2.19f, 1.21f, 3.08f, 0.31f)
                lineToRelative(1.27f, -1.27f)
                curveTo(5.7f, 14.29f, 4.65f, 9.91f, 5.95f, 5.98f)
                close()
                moveTo(5.97f, 5.96f)
                lineToRelative(-0.01f, 0.01f)
                curveToRelative(-0.38f, 3.01f, 1.17f, 6.88f, 4.3f, 10.02f)
                lineToRelative(5.73f, -5.73f)
                curveToRelative(-3.13f, -3.13f, -7.01f, -4.68f, -10.02f, -4.3f)
                close()
            }
        }
        return _beachAccess!!
    }

private var _beachAccess: ImageVector? = null

internal val Icons.Rounded.Bed: ImageVector
    get() {
        if (_bed != null) {
            return _bed!!
        }
        _bed = materialIcon(name = "Rounded.Bed") {
            materialPath {
                moveTo(21.0f, 10.78f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.77f, 0.0f, -1.47f, 0.3f, -2.0f, 0.78f)
                curveTo(11.47f, 5.3f, 10.77f, 5.0f, 10.0f, 5.0f)
                horizontalLineTo(6.0f)
                curveTo(4.35f, 5.0f, 3.0f, 6.35f, 3.0f, 8.0f)
                verticalLineToRelative(2.78f)
                curveTo(2.39f, 11.33f, 2.0f, 12.12f, 2.0f, 13.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-5.0f)
                curveTo(22.0f, 12.12f, 21.61f, 11.33f, 21.0f, 10.78f)
                close()
                moveTo(14.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineTo(8.0f)
                curveTo(13.0f, 7.45f, 13.45f, 7.0f, 14.0f, 7.0f)
                close()
                moveTo(5.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(8.0f)
                close()
                moveTo(4.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(4.0f)
                close()
            }
        }
        return _bed!!
    }

private var _bed: ImageVector? = null

internal val Icons.Rounded.Biotech: ImageVector
    get() {
        if (_biotech != null) {
            return _biotech!!
        }
        _biotech = materialIcon(name = "Rounded.Biotech") {
            materialPath {
                moveTo(7.0f, 19.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                horizontalLineToRelative(-8.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.09f, 0.59f, -2.04f, 1.46f, -2.56f)
                curveTo(8.17f, 9.03f, 8.0f, 8.54f, 8.0f, 8.0f)
                curveToRelative(0.0f, -0.21f, 0.04f, -0.42f, 0.09f, -0.62f)
                curveTo(6.28f, 8.13f, 5.0f, 9.92f, 5.0f, 12.0f)
                curveToRelative(0.0f, 2.76f, 2.24f, 5.0f, 5.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(7.0f)
                close()
            }
            materialPath {
                moveTo(10.56f, 5.51f)
                curveTo(11.91f, 5.54f, 13.0f, 6.64f, 13.0f, 8.0f)
                curveToRelative(0.0f, 0.75f, -0.33f, 1.41f, -0.85f, 1.87f)
                lineToRelative(0.25f, 0.68f)
                curveToRelative(0.19f, 0.52f, 0.76f, 0.79f, 1.28f, 0.6f)
                curveToRelative(0.19f, 0.52f, 0.76f, 0.79f, 1.28f, 0.6f)
                curveToRelative(0.52f, -0.19f, 0.79f, -0.76f, 0.6f, -1.28f)
                curveToRelative(0.52f, -0.19f, 0.79f, -0.76f, 0.6f, -1.28f)
                lineTo(14.1f, 3.54f)
                curveToRelative(-0.19f, -0.52f, -0.76f, -0.79f, -1.28f, -0.6f)
                curveToRelative(-0.19f, -0.52f, -0.76f, -0.79f, -1.28f, -0.6f)
                curveToRelative(-0.52f, 0.19f, -0.79f, 0.76f, -0.6f, 1.28f)
                curveToRelative(-0.52f, 0.19f, -0.79f, 0.76f, -0.6f, 1.28f)
                lineTo(10.56f, 5.51f)
                close()
            }
            materialPath {
                moveTo(10.5f, 8.0f)
                moveToRelative(-1.5f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, 3.0f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, -3.0f, 0.0f)
            }
        }
        return _biotech!!
    }

private var _biotech: ImageVector? = null

internal val Icons.Rounded.Block: ImageVector
    get() {
        if (_block != null) {
            return _block!!
        }
        _block = materialIcon(name = "Rounded.Block") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(4.0f, 12.0f)
                curveToRelative(0.0f, -4.42f, 3.58f, -8.0f, 8.0f, -8.0f)
                curveToRelative(1.85f, 0.0f, 3.55f, 0.63f, 4.9f, 1.69f)
                lineTo(5.69f, 16.9f)
                curveTo(4.63f, 15.55f, 4.0f, 13.85f, 4.0f, 12.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-1.85f, 0.0f, -3.55f, -0.63f, -4.9f, -1.69f)
                lineTo(18.31f, 7.1f)
                curveTo(19.37f, 8.45f, 20.0f, 10.15f, 20.0f, 12.0f)
                curveToRelative(0.0f, 4.42f, -3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        return _block!!
    }

private var _block: ImageVector? = null

internal val Icons.Rounded.Bluetooth: ImageVector
    get() {
        if (_bluetooth != null) {
            return _bluetooth!!
        }
        _bluetooth = materialIcon(name = "Rounded.Bluetooth") {
            materialPath {
                moveTo(17.0f, 7.0f)
                lineToRelative(-4.29f, -4.29f)
                curveToRelative(-0.63f, -0.63f, -1.71f, -0.19f, -1.71f, 0.7f)
                verticalLineToRelative(6.18f)
                lineTo(7.11f, 5.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(10.59f, 12.0f)
                lineTo(5.7f, 16.89f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(11.0f, 14.41f)
                verticalLineToRelative(6.18f)
                curveToRelative(0.0f, 0.89f, 1.08f, 1.34f, 1.71f, 0.71f)
                lineTo(17.0f, 17.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(13.41f, 12.0f)
                lineTo(17.0f, 8.42f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.42f)
                close()
                moveTo(13.0f, 5.83f)
                lineToRelative(1.88f, 1.88f)
                lineTo(13.0f, 9.59f)
                lineTo(13.0f, 5.83f)
                close()
                moveTo(14.88f, 16.29f)
                lineTo(13.0f, 18.17f)
                verticalLineToRelative(-3.76f)
                lineToRelative(1.88f, 1.88f)
                close()
            }
        }
        return _bluetooth!!
    }

private var _bluetooth: ImageVector? = null

internal val Icons.Rounded.Bolt: ImageVector
    get() {
        if (_bolt != null) {
            return _bolt!!
        }
        _bolt = materialIcon(name = "Rounded.Bolt") {
            materialPath {
                moveTo(10.67f, 21.0f)
                lineTo(10.67f, 21.0f)
                curveToRelative(-0.35f, 0.0f, -0.62f, -0.31f, -0.57f, -0.66f)
                lineTo(11.0f, 14.0f)
                horizontalLineTo(7.5f)
                curveToRelative(-0.88f, 0.0f, -0.33f, -0.75f, -0.31f, -0.78f)
                curveToRelative(1.26f, -2.23f, 3.15f, -5.53f, 5.65f, -9.93f)
                curveToRelative(0.1f, -0.18f, 0.3f, -0.29f, 0.5f, -0.29f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.35f, 0.0f, 0.62f, 0.31f, 0.57f, 0.66f)
                lineTo(13.01f, 10.0f)
                horizontalLineToRelative(3.51f)
                curveToRelative(0.4f, 0.0f, 0.62f, 0.19f, 0.4f, 0.66f)
                curveToRelative(-3.29f, 5.74f, -5.2f, 9.09f, -5.75f, 10.05f)
                curveTo(11.07f, 20.89f, 10.88f, 21.0f, 10.67f, 21.0f)
                close()
            }
        }
        return _bolt!!
    }

private var _bolt: ImageVector? = null

internal val Icons.Rounded.Book: ImageVector
    get() {
        if (_book != null) {
            return _book!!
        }
        _book = materialIcon(name = "Rounded.Book") {
            materialPath {
                moveTo(18.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(6.0f, 4.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(8.0f)
                lineToRelative(-2.5f, -1.5f)
                lineTo(6.0f, 12.0f)
                verticalLineTo(4.0f)
                close()
            }
        }
        return _book!!
    }

private var _book: ImageVector? = null

internal val Icons.Rounded.Bookmark: ImageVector
    get() {
        if (_bookmark != null) {
            return _bookmark!!
        }
        _bookmark = materialIcon(name = "Rounded.Bookmark") {
            materialPath {
                moveTo(17.0f, 3.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                lineToRelative(7.0f, -3.0f)
                lineToRelative(7.0f, 3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _bookmark!!
    }

private var _bookmark: ImageVector? = null

internal val Icons.Rounded.Brush: ImageVector
    get() {
        if (_brush != null) {
            return _brush!!
        }
        _brush = materialIcon(name = "Rounded.Brush") {
            materialPath {
                moveTo(7.0f, 14.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                curveToRelative(0.0f, 1.31f, -1.16f, 2.0f, -2.0f, 2.0f)
                curveToRelative(0.92f, 1.22f, 2.49f, 2.0f, 4.0f, 2.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(20.71f, 4.63f)
                lineToRelative(-1.34f, -1.34f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(9.0f, 12.25f)
                lineTo(11.75f, 15.0f)
                lineToRelative(8.96f, -8.96f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                close()
            }
        }
        return _brush!!
    }

private var _brush: ImageVector? = null

internal val Icons.Rounded.BugReport: ImageVector
    get() {
        if (_bugReport != null) {
            return _bugReport!!
        }
        _bugReport = materialIcon(name = "Rounded.BugReport") {
            materialPath {
                moveTo(19.0f, 8.0f)
                horizontalLineToRelative(-1.81f)
                curveToRelative(-0.45f, -0.78f, -1.07f, -1.45f, -1.82f, -1.96f)
                lineToRelative(0.93f, -0.93f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-1.47f, 1.47f)
                curveTo(12.96f, 5.06f, 12.49f, 5.0f, 12.0f, 5.0f)
                reflectiveCurveToRelative(-0.96f, 0.06f, -1.41f, 0.17f)
                lineTo(9.11f, 3.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.92f, 0.93f)
                curveTo(7.88f, 6.55f, 7.26f, 7.22f, 6.81f, 8.0f)
                lineTo(5.0f, 8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.09f)
                curveToRelative(-0.05f, 0.33f, -0.09f, 0.66f, -0.09f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(5.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.34f, 0.04f, 0.67f, 0.09f, 1.0f)
                lineTo(5.0f, 16.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.81f)
                curveToRelative(1.04f, 1.79f, 2.97f, 3.0f, 5.19f, 3.0f)
                reflectiveCurveToRelative(4.15f, -1.21f, 5.19f, -3.0f)
                lineTo(19.0f, 18.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.09f)
                curveToRelative(0.05f, -0.33f, 0.09f, -0.66f, 0.09f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.34f, -0.04f, -0.67f, -0.09f, -1.0f)
                lineTo(19.0f, 10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(13.0f, 16.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(13.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _bugReport!!
    }

private var _bugReport: ImageVector? = null

internal val Icons.Rounded.Business: ImageVector
    get() {
        if (_business != null) {
            return _business!!
        }
        _business = materialIcon(name = "Rounded.Business") {
            materialPath {
                moveTo(12.0f, 7.0f)
                lineTo(12.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 9.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-8.0f)
                close()
                moveTo(6.0f, 19.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(6.0f, 15.0f)
                lineTo(4.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(6.0f, 11.0f)
                lineTo(4.0f, 11.0f)
                lineTo(4.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(6.0f, 7.0f)
                lineTo(4.0f, 7.0f)
                lineTo(4.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(10.0f, 19.0f)
                lineTo(8.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(10.0f, 15.0f)
                lineTo(8.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(10.0f, 11.0f)
                lineTo(8.0f, 11.0f)
                lineTo(8.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(10.0f, 7.0f)
                lineTo(8.0f, 7.0f)
                lineTo(8.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 19.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(12.0f, 9.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(18.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(18.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                close()
            }
        }
        return _business!!
    }

private var _business: ImageVector? = null

internal val Icons.Rounded.BusinessCenter: ImageVector
    get() {
        if (_businessCenter != null) {
            return _businessCenter!!
        }
        _businessCenter = materialIcon(name = "Rounded.BusinessCenter") {
            materialPath {
                moveTo(13.0f, 16.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(3.01f, 15.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                lineTo(19.0f, 21.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(-7.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(20.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(8.0f, 4.79f, 8.0f, 7.0f)
                lineTo(4.0f, 7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 9.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(10.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                lineTo(9.99f, 7.0f)
                lineTo(10.0f, 7.0f)
                close()
            }
        }
        return _businessCenter!!
    }

private var _businessCenter: ImageVector? = null

internal val Icons.Rounded.Cabin: ImageVector
    get() {
        if (_cabin != null) {
            return _cabin!!
        }
        _cabin = materialIcon(name = "Rounded.Cabin") {
            materialPath {
                moveTo(4.37f, 3.55f)
                curveTo(4.89f, 2.62f, 5.87f, 2.0f, 7.0f, 2.0f)
                curveToRelative(0.38f, 0.0f, 0.72f, -0.22f, 0.89f, -0.53f)
                curveTo(8.04f, 1.16f, 8.39f, 1.0f, 8.73f, 1.0f)
                curveToRelative(0.74f, 0.0f, 1.26f, 0.8f, 0.9f, 1.45f)
                curveTo(9.11f, 3.38f, 8.13f, 4.0f, 7.0f, 4.0f)
                curveTo(6.62f, 4.0f, 6.28f, 4.22f, 6.11f, 4.53f)
                curveTo(5.96f, 4.84f, 5.61f, 5.0f, 5.27f, 5.0f)
                curveTo(4.53f, 5.0f, 4.01f, 4.2f, 4.37f, 3.55f)
                close()
                moveTo(22.39f, 12.19f)
                curveToRelative(-0.34f, 0.44f, -0.96f, 0.52f, -1.4f, 0.19f)
                lineTo(20.0f, 11.62f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-8.38f)
                lineToRelative(-0.99f, 0.76f)
                curveToRelative(-0.44f, 0.34f, -1.07f, 0.25f, -1.4f, -0.19f)
                curveToRelative(-0.33f, -0.44f, -0.25f, -1.07f, 0.19f, -1.4f)
                lineTo(4.0f, 9.11f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.58f)
                lineToRelative(5.39f, -4.12f)
                curveToRelative(0.36f, -0.27f, 0.86f, -0.27f, 1.21f, 0.0f)
                lineToRelative(9.6f, 7.33f)
                curveTo(22.64f, 11.13f, 22.73f, 11.76f, 22.39f, 12.19f)
                close()
                moveTo(10.06f, 7.0f)
                horizontalLineToRelative(3.89f)
                lineTo(12.0f, 5.52f)
                lineTo(10.06f, 7.0f)
                close()
                moveTo(6.0f, 10.1f)
                verticalLineTo(11.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-0.9f)
                lineTo(16.56f, 9.0f)
                horizontalLineTo(7.44f)
                lineTo(6.0f, 10.1f)
                close()
                moveTo(6.0f, 13.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                close()
                moveTo(18.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(18.0f)
                close()
            }
        }
        return _cabin!!
    }

private var _cabin: ImageVector? = null

internal val Icons.Rounded.Cake: ImageVector
    get() {
        if (_cake != null) {
            return _cake!!
        }
        _cake = materialIcon(name = "Rounded.Cake") {
            materialPath {
                moveTo(12.68f, 5.88f)
                curveToRelative(0.7f, -0.24f, 1.22f, -0.9f, 1.3f, -1.64f)
                curveToRelative(0.05f, -0.47f, -0.05f, -0.91f, -0.28f, -1.27f)
                lineTo(12.42f, 0.75f)
                curveToRelative(-0.19f, -0.33f, -0.67f, -0.33f, -0.87f, 0.0f)
                lineToRelative(-1.28f, 2.22f)
                curveToRelative(-0.17f, 0.3f, -0.27f, 0.65f, -0.27f, 1.03f)
                curveToRelative(0.0f, 1.32f, 1.3f, 2.35f, 2.68f, 1.88f)
                close()
                moveTo(16.53f, 15.92f)
                lineToRelative(-1.0f, -1.0f)
                lineToRelative(-1.08f, 1.07f)
                curveToRelative(-1.3f, 1.3f, -3.58f, 1.31f, -4.89f, 0.0f)
                lineToRelative(-1.07f, -1.07f)
                lineToRelative(-1.09f, 1.07f)
                curveTo(6.75f, 16.64f, 5.88f, 17.0f, 4.96f, 17.0f)
                curveToRelative(-0.73f, 0.0f, -1.4f, -0.23f, -1.96f, -0.61f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-3.61f)
                curveToRelative(-0.75f, 0.51f, -1.71f, 0.75f, -2.74f, 0.52f)
                curveToRelative(-0.66f, -0.14f, -1.25f, -0.51f, -1.73f, -0.99f)
                close()
                moveTo(18.0f, 9.0f)
                horizontalLineToRelative(-5.0f)
                lineTo(13.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(6.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                verticalLineToRelative(1.46f)
                curveToRelative(0.0f, 0.85f, 0.5f, 1.67f, 1.31f, 1.94f)
                curveToRelative(0.73f, 0.24f, 1.52f, 0.06f, 2.03f, -0.46f)
                lineToRelative(2.14f, -2.13f)
                lineToRelative(2.13f, 2.13f)
                curveToRelative(0.76f, 0.76f, 2.01f, 0.76f, 2.77f, 0.0f)
                lineToRelative(2.14f, -2.13f)
                lineToRelative(2.13f, 2.13f)
                curveToRelative(0.43f, 0.43f, 1.03f, 0.63f, 1.65f, 0.55f)
                curveToRelative(0.99f, -0.13f, 1.69f, -1.06f, 1.69f, -2.06f)
                verticalLineToRelative(-1.42f)
                curveTo(21.0f, 10.34f, 19.66f, 9.0f, 18.0f, 9.0f)
                close()
            }
        }
        return _cake!!
    }

private var _cake: ImageVector? = null

internal val Icons.Rounded.Calculate: ImageVector
    get() {
        if (_calculate != null) {
            return _calculate!!
        }
        _calculate = materialIcon(name = "Rounded.Calculate") {
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
                moveTo(13.56f, 6.53f)
                lineTo(13.56f, 6.53f)
                curveToRelative(0.29f, -0.29f, 0.77f, -0.29f, 1.06f, 0.0f)
                lineToRelative(0.88f, 0.88f)
                lineToRelative(0.88f, -0.88f)
                curveToRelative(0.29f, -0.29f, 0.77f, -0.29f, 1.06f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.29f, 0.29f, 0.29f, 0.77f, 0.0f, 1.06f)
                lineToRelative(-0.88f, 0.88f)
                lineToRelative(0.88f, 0.88f)
                curveToRelative(0.29f, 0.29f, 0.29f, 0.77f, 0.0f, 1.06f)
                verticalLineToRelative(0.0f)
                curveToRelative(-0.29f, 0.29f, -0.77f, 0.29f, -1.06f, 0.0f)
                lineTo(15.5f, 9.54f)
                lineToRelative(-0.88f, 0.88f)
                curveToRelative(-0.29f, 0.29f, -0.77f, 0.29f, -1.06f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.29f, -0.29f, -0.29f, -0.77f, 0.0f, -1.06f)
                lineToRelative(0.88f, -0.88f)
                lineToRelative(-0.88f, -0.88f)
                curveTo(13.26f, 7.3f, 13.26f, 6.82f, 13.56f, 6.53f)
                close()
                moveTo(7.0f, 7.72f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.41f, -0.34f, 0.75f, -0.75f, 0.75f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineToRelative(0.0f)
                curveTo(6.25f, 8.06f, 6.59f, 7.72f, 7.0f, 7.72f)
                close()
                moveTo(10.75f, 16.0f)
                horizontalLineTo(9.5f)
                verticalLineToRelative(1.25f)
                curveTo(9.5f, 17.66f, 9.16f, 18.0f, 8.75f, 18.0f)
                horizontalLineToRelative(0.0f)
                curveTo(8.34f, 18.0f, 8.0f, 17.66f, 8.0f, 17.25f)
                verticalLineTo(16.0f)
                horizontalLineTo(6.75f)
                curveTo(6.34f, 16.0f, 6.0f, 15.66f, 6.0f, 15.25f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-1.25f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(1.25f)
                horizontalLineToRelative(1.25f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(0.0f)
                curveTo(11.5f, 15.66f, 11.16f, 16.0f, 10.75f, 16.0f)
                close()
                moveTo(17.25f, 17.25f)
                horizontalLineToRelative(-3.5f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                lineToRelative(0.0f, 0.0f)
                curveTo(18.0f, 16.91f, 17.66f, 17.25f, 17.25f, 17.25f)
                close()
                moveTo(17.25f, 14.75f)
                horizontalLineToRelative(-3.5f)
                curveTo(13.34f, 14.75f, 13.0f, 14.41f, 13.0f, 14.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.41f, 0.34f, -0.75f, 0.75f, -0.75f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(0.0f)
                curveTo(18.0f, 14.41f, 17.66f, 14.75f, 17.25f, 14.75f)
                close()
            }
        }
        return _calculate!!
    }

private var _calculate: ImageVector? = null

internal val Icons.Rounded.CalendarMonth: ImageVector
    get() {
        if (_calendarMonth != null) {
            return _calendarMonth!!
        }
        _calendarMonth = materialIcon(name = "Rounded.CalendarMonth") {
            materialPath {
                moveTo(17.0f, 2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveTo(6.0f, 2.45f, 6.0f, 3.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.01f, 4.9f, 3.01f, 6.0f)
                lineTo(3.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(3.0f)
                curveTo(18.0f, 2.45f, 17.55f, 2.0f, 17.0f, 2.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(11.0f, 13.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(11.0f, 13.55f, 11.0f, 13.0f)
                close()
                moveTo(7.0f, 13.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(7.0f, 13.55f, 7.0f, 13.0f)
                close()
                moveTo(15.0f, 13.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(15.0f, 13.55f, 15.0f, 13.0f)
                close()
                moveTo(11.0f, 17.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(11.0f, 17.55f, 11.0f, 17.0f)
                close()
                moveTo(7.0f, 17.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(7.0f, 17.55f, 7.0f, 17.0f)
                close()
                moveTo(15.0f, 17.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveTo(15.0f, 17.55f, 15.0f, 17.0f)
                close()
            }
        }
        return _calendarMonth!!
    }

private var _calendarMonth: ImageVector? = null

internal val Icons.Rounded.CalendarToday: ImageVector
    get() {
        if (_calendarToday != null) {
            return _calendarToday!!
        }
        _calendarToday = materialIcon(name = "Rounded.CalendarToday") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(19.0f, 2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(7.0f, 3.0f)
                lineTo(7.0f, 2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 21.0f)
                lineTo(5.0f, 21.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(4.0f, 8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _calendarToday!!
    }

private var _calendarToday: ImageVector? = null

internal val Icons.Rounded.Campaign: ImageVector
    get() {
        if (_campaign != null) {
            return _campaign!!
        }
        _campaign = materialIcon(name = "Rounded.Campaign") {
            materialPath {
                moveTo(18.0f, 12.0f)
                lineTo(18.0f, 12.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(18.45f, 11.0f, 18.0f, 11.45f, 18.0f, 12.0f)
                close()
            }
            materialPath {
                moveTo(16.59f, 16.82f)
                curveToRelative(-0.33f, 0.44f, -0.24f, 1.05f, 0.2f, 1.37f)
                curveToRelative(0.53f, 0.39f, 1.09f, 0.81f, 1.62f, 1.21f)
                curveToRelative(0.44f, 0.33f, 1.06f, 0.24f, 1.38f, -0.2f)
                curveToRelative(0.0f, -0.01f, 0.01f, -0.01f, 0.01f, -0.02f)
                curveToRelative(0.33f, -0.44f, 0.24f, -1.06f, -0.2f, -1.38f)
                curveToRelative(-0.53f, -0.4f, -1.09f, -0.82f, -1.61f, -1.21f)
                curveToRelative(-0.44f, -0.33f, -1.06f, -0.23f, -1.39f, 0.21f)
                curveTo(16.6f, 16.81f, 16.59f, 16.82f, 16.59f, 16.82f)
                close()
            }
            materialPath {
                moveTo(19.81f, 4.81f)
                curveToRelative(0.0f, -0.01f, -0.01f, -0.01f, -0.01f, -0.02f)
                curveToRelative(-0.33f, -0.44f, -0.95f, -0.53f, -1.38f, -0.2f)
                curveToRelative(-0.53f, 0.4f, -1.1f, 0.82f, -1.62f, 1.22f)
                curveToRelative(-0.44f, 0.33f, -0.52f, 0.95f, -0.19f, 1.38f)
                curveToRelative(0.0f, 0.01f, 0.01f, 0.01f, 0.01f, 0.02f)
                curveToRelative(0.33f, 0.44f, 0.94f, 0.53f, 1.38f, 0.2f)
                curveToRelative(0.53f, -0.39f, 1.09f, -0.82f, 1.62f, -1.22f)
                curveTo(20.05f, 5.87f, 20.13f, 5.25f, 19.81f, 4.81f)
                close()
            }
            materialPath {
                moveTo(8.0f, 9.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(1.0f)
                lineToRelative(5.0f, 3.0f)
                verticalLineTo(6.0f)
                lineTo(8.0f, 9.0f)
                close()
            }
            materialPath {
                moveTo(15.5f, 12.0f)
                curveToRelative(0.0f, -1.33f, -0.58f, -2.53f, -1.5f, -3.35f)
                verticalLineToRelative(6.69f)
                curveTo(14.92f, 14.53f, 15.5f, 13.33f, 15.5f, 12.0f)
                close()
            }
        }
        return _campaign!!
    }

private var _campaign: ImageVector? = null

internal val Icons.Rounded.Cancel: ImageVector
    get() {
        if (_cancel != null) {
            return _cancel!!
        }
        _cancel = materialIcon(name = "Rounded.Cancel") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.47f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
                reflectiveCurveTo(17.53f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(16.3f, 16.3f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                lineTo(12.0f, 13.41f)
                lineTo(9.11f, 16.3f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(10.59f, 12.0f)
                lineTo(7.7f, 9.11f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineTo(12.0f, 10.59f)
                lineToRelative(2.89f, -2.89f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(13.41f, 12.0f)
                lineToRelative(2.89f, 2.89f)
                curveToRelative(0.38f, 0.38f, 0.38f, 1.02f, 0.0f, 1.41f)
                close()
            }
        }
        return _cancel!!
    }

private var _cancel: ImageVector? = null

internal val Icons.Rounded.CardGiftcard: ImageVector
    get() {
        if (_cardGiftcard != null) {
            return _cardGiftcard!!
        }
        _cardGiftcard = materialIcon(name = "Rounded.CardGiftcard") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-2.18f)
                curveToRelative(0.11f, -0.31f, 0.18f, -0.65f, 0.18f, -1.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                curveToRelative(-1.05f, 0.0f, -1.96f, 0.54f, -2.5f, 1.35f)
                lineToRelative(-0.5f, 0.67f)
                lineToRelative(-0.5f, -0.68f)
                curveTo(10.96f, 2.54f, 10.05f, 2.0f, 9.0f, 2.0f)
                curveTo(7.34f, 2.0f, 6.0f, 3.34f, 6.0f, 5.0f)
                curveToRelative(0.0f, 0.35f, 0.07f, 0.69f, 0.18f, 1.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(9.0f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(20.0f, 19.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(20.0f, 14.0f)
                lineTo(4.0f, 14.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.08f)
                lineTo(7.6f, 10.02f)
                curveToRelative(-0.33f, 0.45f, -0.23f, 1.08f, 0.22f, 1.4f)
                curveToRelative(0.44f, 0.32f, 1.07f, 0.22f, 1.39f, -0.22f)
                lineTo(12.0f, 7.4f)
                lineToRelative(2.79f, 3.8f)
                curveToRelative(0.32f, 0.44f, 0.95f, 0.54f, 1.39f, 0.22f)
                curveToRelative(0.45f, -0.32f, 0.55f, -0.95f, 0.22f, -1.4f)
                lineTo(14.92f, 8.0f)
                lineTo(19.0f, 8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(5.0f)
                close()
            }
        }
        return _cardGiftcard!!
    }

private var _cardGiftcard: ImageVector? = null

internal val Icons.Rounded.Casino: ImageVector
    get() {
        if (_casino != null) {
            return _casino!!
        }
        _casino = materialIcon(name = "Rounded.Casino") {
            materialPath {
                moveTo(19.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 15.0f, 7.5f, 15.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 18.0f, 7.5f, 18.0f)
                close()
                moveTo(7.5f, 9.0f)
                curveTo(6.67f, 9.0f, 6.0f, 8.33f, 6.0f, 7.5f)
                reflectiveCurveTo(6.67f, 6.0f, 7.5f, 6.0f)
                reflectiveCurveTo(9.0f, 6.67f, 9.0f, 7.5f)
                reflectiveCurveTo(8.33f, 9.0f, 7.5f, 9.0f)
                close()
                moveTo(12.0f, 13.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(16.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(16.5f, 9.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(15.67f, 6.0f, 16.5f, 6.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(17.33f, 9.0f, 16.5f, 9.0f)
                close()
            }
        }
        return _casino!!
    }

private var _casino: ImageVector? = null

internal val Icons.Rounded.Cast: ImageVector
    get() {
        if (_cast != null) {
            return _cast!!
        }
        _cast = materialIcon(name = "Rounded.Cast") {
            materialPath {
                moveTo(21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(3.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(23.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(2.14f, 14.09f)
                curveToRelative(-0.6f, -0.1f, -1.14f, 0.39f, -1.14f, 1.0f)
                curveToRelative(0.0f, 0.49f, 0.36f, 0.9f, 0.85f, 0.98f)
                curveToRelative(2.08f, 0.36f, 3.72f, 2.0f, 4.08f, 4.08f)
                curveToRelative(0.08f, 0.49f, 0.49f, 0.85f, 0.98f, 0.85f)
                curveToRelative(0.61f, 0.0f, 1.09f, -0.54f, 1.0f, -1.14f)
                curveToRelative(-0.48f, -2.96f, -2.82f, -5.29f, -5.77f, -5.77f)
                close()
                moveTo(1.0f, 18.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(2.1f, 10.05f)
                curveToRelative(-0.59f, -0.05f, -1.1f, 0.41f, -1.1f, 1.0f)
                curveToRelative(0.0f, 0.51f, 0.38f, 0.94f, 0.88f, 0.99f)
                curveToRelative(4.27f, 0.41f, 7.67f, 3.81f, 8.08f, 8.08f)
                curveToRelative(0.05f, 0.5f, 0.48f, 0.87f, 0.99f, 0.87f)
                curveToRelative(0.6f, 0.0f, 1.06f, -0.52f, 1.0f, -1.11f)
                curveToRelative(-0.53f, -5.19f, -4.66f, -9.31f, -9.85f, -9.83f)
                close()
            }
        }
        return _cast!!
    }

private var _cast: ImageVector? = null

internal val Icons.Rounded.Castle: ImageVector
    get() {
        if (_castle != null) {
            return _castle!!
        }
        _castle = materialIcon(name = "Rounded.Castle") {
            materialPath {
                moveTo(22.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveTo(9.0f, 3.45f, 9.0f, 4.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveTo(5.0f, 3.45f, 5.0f, 4.0f)
                verticalLineToRelative(7.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                curveTo(23.0f, 9.45f, 22.55f, 9.0f, 22.0f, 9.0f)
                close()
                moveTo(11.0f, 12.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.0f)
                close()
            }
        }
        return _castle!!
    }

private var _castle: ImageVector? = null

internal val Icons.Rounded.Chair: ImageVector
    get() {
        if (_chair != null) {
            return _chair!!
        }
        _chair = materialIcon(name = "Rounded.Chair") {
            materialPath {
                moveTo(21.0f, 9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.65f, 1.35f, 3.0f, 3.0f, 3.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(1.65f, 0.0f, 3.0f, -1.35f, 3.0f, -3.0f)
                verticalLineToRelative(-5.0f)
                curveTo(23.0f, 9.9f, 22.1f, 9.0f, 21.0f, 9.0f)
                close()
            }
            materialPath {
                moveTo(7.0f, 11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -1.86f, 1.28f, -3.41f, 3.0f, -3.86f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
                horizontalLineTo(7.0f)
                curveTo(5.35f, 3.0f, 4.0f, 4.35f, 4.0f, 6.0f)
                verticalLineToRelative(1.14f)
                curveTo(5.72f, 7.59f, 7.0f, 9.14f, 7.0f, 11.0f)
                close()
            }
        }
        return _chair!!
    }

private var _chair: ImageVector? = null

internal val Icons.Rounded.ChangeHistory: ImageVector
    get() {
        if (_changeHistory != null) {
            return _changeHistory!!
        }
        _changeHistory = materialIcon(name = "Rounded.ChangeHistory") {
            materialPath {
                moveTo(12.0f, 7.77f)
                lineTo(18.39f, 18.0f)
                horizontalLineTo(5.61f)
                lineTo(12.0f, 7.77f)
                moveToRelative(-0.85f, -2.41f)
                lineToRelative(-8.2f, 13.11f)
                curveToRelative(-0.41f, 0.67f, 0.07f, 1.53f, 0.85f, 1.53f)
                horizontalLineToRelative(16.4f)
                curveToRelative(0.79f, 0.0f, 1.26f, -0.86f, 0.85f, -1.53f)
                lineToRelative(-8.2f, -13.11f)
                curveToRelative(-0.39f, -0.63f, -1.31f, -0.63f, -1.7f, 0.0f)
                close()
            }
        }
        return _changeHistory!!
    }

private var _changeHistory: ImageVector? = null

internal val Icons.Rounded.ChatBubble: ImageVector
    get() {
        if (_chatBubble != null) {
            return _chatBubble!!
        }
        _chatBubble = materialIcon(name = "Rounded.ChatBubble") {
            materialPath {
                moveTo(20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _chatBubble!!
    }

private var _chatBubble: ImageVector? = null

internal val Icons.Rounded.Checklist: ImageVector
    get() {
        if (_checklist != null) {
            return _checklist!!
        }
        _checklist = materialIcon(name = "Rounded.Checklist") {
            materialPath {
                moveTo(22.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(7.0f)
                curveTo(21.55f, 9.0f, 22.0f, 8.55f, 22.0f, 8.0f)
                close()
                moveTo(13.0f, 16.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-7.0f)
                curveTo(13.45f, 15.0f, 13.0f, 15.45f, 13.0f, 16.0f)
                close()
                moveTo(10.47f, 4.63f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-4.23f, 4.25f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.42f, 0.0f)
                lineTo(2.7f, 8.16f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(1.42f, 1.42f)
                lineToRelative(3.54f, -3.54f)
                curveTo(9.45f, 4.25f, 10.09f, 4.25f, 10.47f, 4.63f)
                close()
                moveTo(10.48f, 12.64f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-4.23f, 4.25f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.42f, 0.0f)
                lineTo(2.7f, 16.16f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                reflectiveCurveToRelative(1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(1.42f, 1.42f)
                lineToRelative(3.54f, -3.54f)
                curveTo(9.45f, 12.25f, 10.09f, 12.25f, 10.48f, 12.64f)
                lineTo(10.48f, 12.64f)
                close()
            }
        }
        return _checklist!!
    }

private var _checklist: ImageVector? = null

internal val Icons.Rounded.Checkroom: ImageVector
    get() {
        if (_checkroom != null) {
            return _checkroom!!
        }
        _checkroom = materialIcon(name = "Rounded.Checkroom") {
            materialPath {
                moveTo(21.6f, 18.2f)
                lineTo(13.0f, 11.75f)
                verticalLineToRelative(-0.91f)
                curveToRelative(1.65f, -0.49f, 2.8f, -2.17f, 2.43f, -4.05f)
                curveToRelative(-0.26f, -1.31f, -1.3f, -2.4f, -2.61f, -2.7f)
                curveToRelative(-1.76f, -0.4f, -3.37f, 0.53f, -4.02f, 1.98f)
                curveTo(8.5f, 6.74f, 8.98f, 7.5f, 9.71f, 7.5f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.39f, 0.0f, 0.75f, -0.22f, 0.9f, -0.57f)
                curveTo(10.84f, 6.38f, 11.37f, 6.0f, 12.0f, 6.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                curveToRelative(0.0f, 0.84f, -0.69f, 1.52f, -1.53f, 1.5f)
                curveTo(11.43f, 8.99f, 11.0f, 9.45f, 11.0f, 9.99f)
                lineToRelative(0.0f, 1.76f)
                lineTo(2.4f, 18.2f)
                curveTo(1.63f, 18.78f, 2.04f, 20.0f, 3.0f, 20.0f)
                horizontalLineToRelative(9.0f)
                horizontalLineToRelative(9.0f)
                curveTo(21.96f, 20.0f, 22.37f, 18.78f, 21.6f, 18.2f)
                close()
                moveTo(6.0f, 18.0f)
                lineToRelative(6.0f, -4.5f)
                lineToRelative(6.0f, 4.5f)
                horizontalLineTo(6.0f)
                close()
            }
        }
        return _checkroom!!
    }

private var _checkroom: ImageVector? = null

internal val Icons.Rounded.ChildCare: ImageVector
    get() {
        if (_childCare != null) {
            return _childCare!!
        }
        _childCare = materialIcon(name = "Rounded.ChildCare") {
            materialPath {
                moveTo(14.5f, 10.5f)
                moveToRelative(-1.25f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, 2.5f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, -2.5f, 0.0f)
            }
            materialPath {
                moveTo(9.5f, 10.5f)
                moveToRelative(-1.25f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, 2.5f, 0.0f)
                arcToRelative(1.25f, 1.25f, 0.0f, true, true, -2.5f, 0.0f)
            }
            materialPath {
                moveTo(16.1f, 14.0f)
                lineTo(7.9f, 14.0f)
                curveToRelative(-0.19f, 0.0f, -0.32f, 0.2f, -0.23f, 0.37f)
                curveTo(8.5f, 15.94f, 10.13f, 17.0f, 12.0f, 17.0f)
                reflectiveCurveToRelative(3.5f, -1.06f, 4.33f, -2.63f)
                curveToRelative(0.08f, -0.17f, -0.05f, -0.37f, -0.23f, -0.37f)
                close()
                moveTo(22.94f, 11.34f)
                curveToRelative(-0.25f, -1.51f, -1.36f, -2.74f, -2.81f, -3.17f)
                curveToRelative(-0.53f, -1.12f, -1.28f, -2.1f, -2.19f, -2.91f)
                curveTo(16.36f, 3.85f, 14.28f, 3.0f, 12.0f, 3.0f)
                reflectiveCurveToRelative(-4.36f, 0.85f, -5.94f, 2.26f)
                curveToRelative(-0.92f, 0.81f, -1.67f, 1.8f, -2.19f, 2.91f)
                curveToRelative(-1.45f, 0.43f, -2.56f, 1.65f, -2.81f, 3.17f)
                curveToRelative(-0.04f, 0.21f, -0.06f, 0.43f, -0.06f, 0.66f)
                curveToRelative(0.0f, 0.23f, 0.02f, 0.45f, 0.06f, 0.66f)
                curveToRelative(0.25f, 1.51f, 1.36f, 2.74f, 2.81f, 3.17f)
                curveToRelative(0.52f, 1.11f, 1.27f, 2.09f, 2.17f, 2.89f)
                curveTo(7.62f, 20.14f, 9.71f, 21.0f, 12.0f, 21.0f)
                reflectiveCurveToRelative(4.38f, -0.86f, 5.97f, -2.28f)
                curveToRelative(0.9f, -0.8f, 1.65f, -1.79f, 2.17f, -2.89f)
                curveToRelative(1.44f, -0.43f, 2.55f, -1.65f, 2.8f, -3.17f)
                curveToRelative(0.04f, -0.21f, 0.06f, -0.43f, 0.06f, -0.66f)
                curveToRelative(0.0f, -0.23f, -0.02f, -0.45f, -0.06f, -0.66f)
                close()
                moveTo(19.0f, 14.0f)
                curveToRelative(-0.1f, 0.0f, -0.19f, -0.02f, -0.29f, -0.03f)
                curveToRelative(-0.2f, 0.67f, -0.49f, 1.29f, -0.86f, 1.86f)
                curveTo(16.6f, 17.74f, 14.45f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(-4.6f, -1.26f, -5.85f, -3.17f)
                curveToRelative(-0.37f, -0.57f, -0.66f, -1.19f, -0.86f, -1.86f)
                curveToRelative(-0.1f, 0.01f, -0.19f, 0.03f, -0.29f, 0.03f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                curveToRelative(0.1f, 0.0f, 0.19f, 0.02f, 0.29f, 0.03f)
                curveToRelative(0.2f, -0.67f, 0.49f, -1.29f, 0.86f, -1.86f)
                curveTo(7.4f, 6.26f, 9.55f, 5.0f, 12.0f, 5.0f)
                reflectiveCurveToRelative(4.6f, 1.26f, 5.85f, 3.17f)
                curveToRelative(0.37f, 0.57f, 0.66f, 1.19f, 0.86f, 1.86f)
                curveToRelative(0.1f, -0.01f, 0.19f, -0.03f, 0.29f, -0.03f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _childCare!!
    }

private var _childCare: ImageVector? = null

internal val Icons.Rounded.Church: ImageVector
    get() {
        if (_church != null) {
            return _church!!
        }
        _church = materialIcon(name = "Rounded.Church") {
            materialPath {
                moveTo(18.0f, 12.22f)
                verticalLineToRelative(-1.99f)
                curveToRelative(0.0f, -0.76f, -0.43f, -1.45f, -1.11f, -1.79f)
                lineTo(13.0f, 6.5f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-1.0f)
                curveTo(9.45f, 3.0f, 9.0f, 3.45f, 9.0f, 4.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.5f)
                lineTo(7.11f, 8.45f)
                curveTo(6.43f, 8.79f, 6.0f, 9.48f, 6.0f, 10.24f)
                verticalLineToRelative(1.99f)
                lineToRelative(-2.81f, 1.25f)
                curveTo(2.47f, 13.79f, 2.0f, 14.51f, 2.0f, 15.3f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                lineToRelative(0.0f, -2.89f)
                curveToRelative(0.0f, -1.0f, 0.68f, -1.92f, 1.66f, -2.08f)
                curveTo(12.92f, 16.82f, 14.0f, 17.79f, 14.0f, 19.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.7f)
                curveToRelative(0.0f, -0.79f, -0.47f, -1.51f, -1.19f, -1.83f)
                lineTo(18.0f, 12.22f)
                close()
                moveTo(12.0f, 13.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(12.83f, 13.5f, 12.0f, 13.5f)
                close()
            }
        }
        return _church!!
    }

private var _church: ImageVector? = null

internal val Icons.Rounded.Circle: ImageVector
    get() {
        if (_circle != null) {
            return _circle!!
        }
        _circle = materialIcon(name = "Rounded.Circle") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.47f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
                reflectiveCurveTo(17.53f, 2.0f, 12.0f, 2.0f)
                close()
            }
        }
        return _circle!!
    }

private var _circle: ImageVector? = null

internal val Icons.Rounded.Cloud: ImageVector
    get() {
        if (_cloud != null) {
            return _cloud!!
        }
        _cloud = materialIcon(name = "Rounded.Cloud") {
            materialPath {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
                curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
            }
        }
        return _cloud!!
    }

private var _cloud: ImageVector? = null

internal val Icons.Rounded.CloudUpload: ImageVector
    get() {
        if (_cloudUpload != null) {
            return _cloudUpload!!
        }
        _cloudUpload = materialIcon(name = "Rounded.CloudUpload") {
            materialPath {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
                curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
                moveTo(14.0f, 13.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(7.0f)
                lineToRelative(4.65f, -4.65f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineTo(17.0f, 13.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
        }
        return _cloudUpload!!
    }

private var _cloudUpload: ImageVector? = null

internal val Icons.Rounded.Code: ImageVector
    get() {
        if (_code != null) {
            return _code!!
        }
        _code = materialIcon(name = "Rounded.Code") {
            materialPath {
                moveTo(8.7f, 15.9f)
                lineTo(4.8f, 12.0f)
                lineToRelative(3.9f, -3.9f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.01f, 0.0f, -1.4f)
                curveToRelative(-0.39f, -0.39f, -1.01f, -0.39f, -1.4f, 0.0f)
                lineToRelative(-4.59f, 4.59f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.59f, 4.6f)
                curveToRelative(0.39f, 0.39f, 1.01f, 0.39f, 1.4f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.01f, 0.0f, -1.4f)
                close()
                moveTo(15.3f, 15.9f)
                lineToRelative(3.9f, -3.9f)
                lineToRelative(-3.9f, -3.9f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.01f, 0.0f, -1.4f)
                curveToRelative(0.39f, -0.39f, 1.01f, -0.39f, 1.4f, 0.0f)
                lineToRelative(4.59f, 4.59f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-4.59f, 4.6f)
                curveToRelative(-0.39f, 0.39f, -1.01f, 0.39f, -1.4f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.01f, 0.0f, -1.4f)
                close()
            }
        }
        return _code!!
    }

private var _code: ImageVector? = null

internal val Icons.Rounded.Coffee: ImageVector
    get() {
        if (_coffee != null) {
            return _coffee!!
        }
        _coffee = materialIcon(name = "Rounded.Coffee") {
            materialPath {
                moveTo(18.5f, 3.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 3.0f, 4.0f, 3.9f, 4.0f, 5.0f)
                verticalLineToRelative(5.71f)
                curveToRelative(0.0f, 3.83f, 2.95f, 7.18f, 6.78f, 7.29f)
                curveToRelative(3.96f, 0.12f, 7.22f, -3.06f, 7.22f, -7.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(0.5f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(20.43f, 3.0f, 18.5f, 3.0f)
                close()
                moveTo(16.0f, 5.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(16.0f)
                close()
                moveTo(18.5f, 8.0f)
                horizontalLineTo(18.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(0.5f)
                curveTo(19.33f, 5.0f, 20.0f, 5.67f, 20.0f, 6.5f)
                reflectiveCurveTo(19.33f, 8.0f, 18.5f, 8.0f)
                close()
                moveTo(5.0f, 19.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveTo(4.0f, 19.45f, 4.45f, 19.0f, 5.0f, 19.0f)
                close()
            }
        }
        return _coffee!!
    }

private var _coffee: ImageVector? = null

internal val Icons.Rounded.Collections: ImageVector
    get() {
        if (_collections != null) {
            return _collections!!
        }
        _collections = materialIcon(name = "Rounded.Collections") {
            materialPath {
                moveTo(22.0f, 16.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(8.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(11.4f, 12.53f)
                lineToRelative(1.63f, 2.18f)
                lineToRelative(2.58f, -3.22f)
                curveToRelative(0.2f, -0.25f, 0.58f, -0.25f, 0.78f, 0.0f)
                lineToRelative(2.96f, 3.7f)
                curveToRelative(0.26f, 0.33f, 0.03f, 0.81f, -0.39f, 0.81f)
                lineTo(9.0f, 16.0f)
                curveToRelative(-0.41f, 0.0f, -0.65f, -0.47f, -0.4f, -0.8f)
                lineToRelative(2.0f, -2.67f)
                curveToRelative(0.2f, -0.26f, 0.6f, -0.26f, 0.8f, 0.0f)
                close()
                moveTo(2.0f, 7.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(5.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(4.0f, 7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _collections!!
    }

private var _collections: ImageVector? = null

internal val Icons.Rounded.ConfirmationNumber: ImageVector
    get() {
        if (_confirmationNumber != null) {
            return _confirmationNumber!!
        }
        _confirmationNumber = materialIcon(name = "Rounded.ConfirmationNumber") {
            materialPath {
                moveTo(22.0f, 8.54f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                verticalLineToRelative(2.54f)
                curveToRelative(0.0f, 0.69f, 0.33f, 1.37f, 0.94f, 1.69f)
                curveTo(3.58f, 10.58f, 4.0f, 11.24f, 4.0f, 12.0f)
                reflectiveCurveToRelative(-0.43f, 1.43f, -1.06f, 1.76f)
                curveToRelative(-0.6f, 0.33f, -0.94f, 1.01f, -0.94f, 1.7f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.54f)
                curveToRelative(0.0f, -0.69f, -0.34f, -1.37f, -0.94f, -1.7f)
                curveToRelative(-0.63f, -0.34f, -1.06f, -1.0f, -1.06f, -1.76f)
                reflectiveCurveToRelative(0.43f, -1.42f, 1.06f, -1.76f)
                curveToRelative(0.6f, -0.33f, 0.94f, -1.01f, 0.94f, -1.7f)
                close()
                moveTo(13.0f, 17.5f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 8.5f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _confirmationNumber!!
    }

private var _confirmationNumber: ImageVector? = null

internal val Icons.Rounded.Construction: ImageVector
    get() {
        if (_construction != null) {
            return _construction!!
        }
        _construction = materialIcon(name = "Rounded.Construction") {
            materialPath {
                moveTo(20.99f, 17.99f)
                lineToRelative(-4.94f, -4.94f)
                lineToRelative(-2.12f, 2.12f)
                lineToRelative(4.94f, 4.94f)
                curveToRelative(0.59f, 0.59f, 1.54f, 0.59f, 2.12f, 0.0f)
                curveTo(21.57f, 19.52f, 21.57f, 18.57f, 20.99f, 17.99f)
                close()
            }
            materialPath {
                moveTo(17.65f, 10.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                curveToRelative(0.0f, -0.58f, -0.16f, -1.12f, -0.41f, -1.6f)
                lineToRelative(-2.7f, 2.7f)
                lineToRelative(-1.49f, -1.49f)
                lineToRelative(2.7f, -2.7f)
                curveTo(18.77f, 3.16f, 18.23f, 3.0f, 17.65f, 3.0f)
                curveToRelative(-1.93f, 0.0f, -3.5f, 1.57f, -3.5f, 3.5f)
                curveToRelative(0.0f, 0.41f, 0.08f, 0.8f, 0.21f, 1.16f)
                lineToRelative(-1.85f, 1.85f)
                lineToRelative(-1.78f, -1.78f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.71f, -0.71f)
                lineToRelative(2.12f, -2.12f)
                curveToRelative(-1.17f, -1.17f, -3.07f, -1.17f, -4.24f, 0.0f)
                lineTo(5.08f, 6.32f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.71f, 0.71f)
                horizontalLineTo(3.25f)
                curveToRelative(-0.19f, 0.0f, -0.37f, 0.07f, -0.5f, 0.21f)
                curveToRelative(-0.28f, 0.28f, -0.28f, 0.72f, 0.0f, 1.0f)
                lineToRelative(2.54f, 2.54f)
                curveToRelative(0.28f, 0.28f, 0.72f, 0.28f, 1.0f, 0.0f)
                curveToRelative(0.13f, -0.13f, 0.21f, -0.31f, 0.21f, -0.5f)
                verticalLineTo(9.15f)
                lineTo(7.2f, 9.85f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(1.78f, 1.78f)
                lineToRelative(-6.35f, 6.35f)
                curveToRelative(-0.59f, 0.59f, -0.59f, 1.54f, 0.0f, 2.12f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.59f, 0.59f, 1.54f, 0.59f, 2.12f, 0.0f)
                lineTo(16.48f, 9.79f)
                curveTo(16.85f, 9.92f, 17.24f, 10.0f, 17.65f, 10.0f)
                close()
            }
        }
        return _construction!!
    }

private var _construction: ImageVector? = null

internal val Icons.Rounded.ContactPage: ImageVector
    get() {
        if (_contactPage != null) {
            return _contactPage!!
        }
        _contactPage = materialIcon(name = "Rounded.ContactPage") {
            materialPath {
                moveTo(13.17f, 2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 2.0f, 4.0f, 2.9f, 4.0f, 4.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.83f)
                curveToRelative(0.0f, -0.53f, -0.21f, -1.04f, -0.59f, -1.41f)
                lineToRelative(-4.83f, -4.83f)
                curveTo(14.21f, 2.21f, 13.7f, 2.0f, 13.17f, 2.0f)
                close()
                moveTo(12.0f, 10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                curveTo(10.0f, 10.9f, 10.9f, 10.0f, 12.0f, 10.0f)
                close()
                moveTo(16.0f, 18.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-0.57f)
                curveToRelative(0.0f, -0.81f, 0.48f, -1.53f, 1.22f, -1.85f)
                curveTo(10.07f, 15.21f, 11.01f, 15.0f, 12.0f, 15.0f)
                curveToRelative(0.99f, 0.0f, 1.93f, 0.21f, 2.78f, 0.58f)
                curveTo(15.52f, 15.9f, 16.0f, 16.62f, 16.0f, 17.43f)
                verticalLineTo(18.0f)
                close()
            }
        }
        return _contactPage!!
    }

private var _contactPage: ImageVector? = null

internal val Icons.Rounded.ContentCopy: ImageVector
    get() {
        if (_contentCopy != null) {
            return _contentCopy!!
        }
        _contentCopy = materialIcon(name = "Rounded.ContentCopy") {
            materialPath {
                moveTo(15.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveTo(3.45f, 6.0f, 3.0f, 6.45f, 3.0f, 7.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveTo(16.0f, 20.45f, 15.55f, 20.0f, 15.0f, 20.0f)
                close()
                moveTo(20.0f, 16.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(9.0f)
                curveTo(7.9f, 2.0f, 7.0f, 2.9f, 7.0f, 4.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(9.0f)
                curveTo(19.1f, 18.0f, 20.0f, 17.1f, 20.0f, 16.0f)
                close()
                moveTo(18.0f, 16.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(9.0f)
                verticalLineTo(16.0f)
                close()
            }
        }
        return _contentCopy!!
    }

private var _contentCopy: ImageVector? = null

internal val Icons.Rounded.ContentCut: ImageVector
    get() {
        if (_contentCut != null) {
            return _contentCut!!
        }
        _contentCut = materialIcon(name = "Rounded.ContentCut") {
            materialPath {
                moveTo(9.64f, 7.64f)
                curveToRelative(0.29f, -0.62f, 0.42f, -1.33f, 0.34f, -2.09f)
                curveToRelative(-0.19f, -1.73f, -1.54f, -3.2f, -3.26f, -3.49f)
                curveToRelative(-2.77f, -0.48f, -5.14f, 1.89f, -4.66f, 4.65f)
                curveToRelative(0.3f, 1.72f, 1.76f, 3.07f, 3.49f, 3.26f)
                curveToRelative(0.76f, 0.08f, 1.46f, -0.05f, 2.09f, -0.34f)
                lineTo(10.0f, 12.0f)
                lineToRelative(-2.36f, 2.36f)
                curveToRelative(-0.62f, -0.29f, -1.33f, -0.42f, -2.09f, -0.34f)
                curveToRelative(-1.73f, 0.19f, -3.2f, 1.54f, -3.49f, 3.26f)
                curveToRelative(-0.48f, 2.77f, 1.89f, 5.13f, 4.65f, 4.65f)
                curveToRelative(1.72f, -0.3f, 3.07f, -1.76f, 3.26f, -3.49f)
                curveToRelative(0.08f, -0.76f, -0.05f, -1.46f, -0.34f, -2.09f)
                lineTo(12.0f, 14.0f)
                lineToRelative(7.59f, 7.59f)
                curveToRelative(0.89f, 0.89f, 2.41f, 0.26f, 2.41f, -1.0f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0.0f, -0.37f, -0.15f, -0.73f, -0.41f, -1.0f)
                lineTo(9.64f, 7.64f)
                close()
                moveTo(6.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.89f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.89f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(6.0f, 20.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.89f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.89f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(12.0f, 12.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, -0.22f, -0.5f, -0.5f)
                reflectiveCurveToRelative(0.22f, -0.5f, 0.5f, -0.5f)
                reflectiveCurveToRelative(0.5f, 0.22f, 0.5f, 0.5f)
                reflectiveCurveToRelative(-0.22f, 0.5f, -0.5f, 0.5f)
                close()
                moveTo(19.59f, 2.41f)
                lineTo(13.0f, 9.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(6.59f, -6.59f)
                curveToRelative(0.26f, -0.26f, 0.41f, -0.62f, 0.41f, -1.0f)
                lineTo(22.0f, 3.4f)
                curveToRelative(0.0f, -1.25f, -1.52f, -1.88f, -2.41f, -0.99f)
                close()
            }
        }
        return _contentCut!!
    }

private var _contentCut: ImageVector? = null

internal val Icons.Rounded.ContentPaste: ImageVector
    get() {
        if (_contentPaste != null) {
            return _contentPaste!!
        }
        _contentPaste = materialIcon(name = "Rounded.ContentPaste") {
            materialPath {
                moveTo(19.0f, 2.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 0.84f, 13.3f, 0.0f, 12.0f, 0.0f)
                reflectiveCurveTo(9.6f, 0.84f, 9.18f, 2.0f)
                lineTo(5.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(18.0f, 20.0f)
                lineTo(6.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(5.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(17.0f, 4.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _contentPaste!!
    }

private var _contentPaste: ImageVector? = null

internal val Icons.Rounded.Cookie: ImageVector
    get() {
        if (_cookie != null) {
            return _cookie!!
        }
        _cookie = materialIcon(name = "Rounded.Cookie") {
            materialPath {
                moveTo(21.27f, 10.9f)
                curveToRelative(-1.21f, -0.33f, -2.31f, -1.46f, -2.29f, -2.89f)
                curveToRelative(0.01f, -0.56f, -0.4f, -1.02f, -0.96f, -1.01f)
                curveTo(15.83f, 7.03f, 14.0f, 5.22f, 14.0f, 3.02f)
                curveToRelative(0.0f, -0.49f, -0.35f, -0.9f, -0.84f, -0.96f)
                curveTo(6.53f, 1.22f, 2.0f, 6.81f, 2.0f, 12.0f)
                curveToRelative(0.0f, 5.52f, 4.48f, 10.0f, 10.0f, 10.0f)
                curveToRelative(5.61f, 0.0f, 10.11f, -4.62f, 10.0f, -10.18f)
                curveTo(21.99f, 11.38f, 21.69f, 11.01f, 21.27f, 10.9f)
                close()
                moveTo(8.5f, 15.0f)
                curveTo(7.67f, 15.0f, 7.0f, 14.33f, 7.0f, 13.5f)
                reflectiveCurveTo(7.67f, 12.0f, 8.5f, 12.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(9.33f, 15.0f, 8.5f, 15.0f)
                close()
                moveTo(10.5f, 10.0f)
                curveTo(9.67f, 10.0f, 9.0f, 9.33f, 9.0f, 8.5f)
                reflectiveCurveTo(9.67f, 7.0f, 10.5f, 7.0f)
                reflectiveCurveTo(12.0f, 7.67f, 12.0f, 8.5f)
                reflectiveCurveTo(11.33f, 10.0f, 10.5f, 10.0f)
                close()
                moveTo(15.0f, 16.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(16.0f, 15.55f, 15.55f, 16.0f, 15.0f, 16.0f)
                close()
            }
        }
        return _cookie!!
    }

private var _cookie: ImageVector? = null

internal val Icons.Rounded.CoPresent: ImageVector
    get() {
        if (_coPresent != null) {
            return _coPresent!!
        }
        _coPresent = materialIcon(name = "Rounded.CoPresent") {
            materialPath {
                moveTo(21.0f, 3.0f)
                horizontalLineTo(3.0f)
                curveTo(1.9f, 3.0f, 1.0f, 3.9f, 1.0f, 5.0f)
                verticalLineToRelative(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(23.0f, 3.9f, 22.1f, 3.0f, 21.0f, 3.0f)
                close()
            }
            materialPath {
                moveTo(9.0f, 10.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
            materialPath {
                moveTo(15.39f, 16.56f)
                curveTo(13.71f, 15.7f, 11.53f, 15.0f, 9.0f, 15.0f)
                curveToRelative(-2.53f, 0.0f, -4.71f, 0.7f, -6.39f, 1.56f)
                curveTo(1.61f, 17.07f, 1.0f, 18.1f, 1.0f, 19.22f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-2.78f)
                curveTo(17.0f, 18.1f, 16.39f, 17.07f, 15.39f, 16.56f)
                close()
            }
        }
        return _coPresent!!
    }

private var _coPresent: ImageVector? = null

internal val Icons.Rounded.CreditCard: ImageVector
    get() {
        if (_creditCard != null) {
            return _creditCard!!
        }
        _creditCard = materialIcon(name = "Rounded.CreditCard") {
            materialPath {
                moveTo(20.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 18.0f)
                lineTo(5.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(20.0f, 8.0f)
                lineTo(4.0f, 8.0f)
                lineTo(4.0f, 6.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _creditCard!!
    }

private var _creditCard: ImageVector? = null

internal val Icons.Rounded.CropSquare: ImageVector
    get() {
        if (_cropSquare != null) {
            return _cropSquare!!
        }
        _cropSquare = materialIcon(name = "Rounded.CropSquare") {
            materialPath {
                moveTo(18.0f, 4.0f)
                lineTo(6.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 18.0f)
                lineTo(7.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(6.0f, 7.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _cropSquare!!
    }

private var _cropSquare: ImageVector? = null

internal val Icons.Rounded.CrueltyFree: ImageVector
    get() {
        if (_crueltyFree != null) {
            return _crueltyFree!!
        }
        _crueltyFree = materialIcon(name = "Rounded.CrueltyFree") {
            materialPath {
                moveTo(16.84f, 14.52f)
                curveToRelative(-0.26f, -0.19f, -0.62f, -0.63f, -0.79f, -0.84f)
                curveTo(17.24f, 12.01f, 19.0f, 8.87f, 19.0f, 5.0f)
                curveToRelative(0.0f, -1.95f, -0.74f, -3.0f, -2.0f, -3.0f)
                curveToRelative(-1.54f, 0.0f, -3.96f, 2.06f, -5.0f, 5.97f)
                curveTo(10.96f, 4.06f, 8.54f, 2.0f, 7.0f, 2.0f)
                curveTo(5.74f, 2.0f, 5.0f, 3.05f, 5.0f, 5.0f)
                curveToRelative(0.0f, 3.87f, 1.76f, 7.01f, 2.95f, 8.68f)
                curveToRelative(-0.17f, 0.21f, -0.53f, 0.65f, -0.79f, 0.84f)
                curveToRelative(-0.5f, 0.41f, -1.66f, 1.37f, -1.66f, 2.98f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                curveToRelative(1.55f, 0.0f, 2.5f, -0.56f, 2.5f, -0.56f)
                reflectiveCurveToRelative(0.95f, 0.56f, 2.5f, 0.56f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveTo(18.5f, 15.89f, 17.34f, 14.93f, 16.84f, 14.52f)
                close()
                moveTo(9.35f, 12.2f)
                curveTo(8.34f, 10.7f, 7.0f, 8.12f, 7.0f, 5.0f)
                curveToRelative(0.0f, -0.49f, 0.06f, -0.8f, 0.12f, -0.97f)
                curveToRelative(0.94f, 0.31f, 3.24f, 2.71f, 3.38f, 7.64f)
                curveTo(10.03f, 11.79f, 9.66f, 11.97f, 9.35f, 12.2f)
                close()
                moveTo(10.5f, 16.75f)
                curveToRelative(-0.28f, 0.0f, -0.5f, -0.34f, -0.5f, -0.75f)
                curveToRelative(0.0f, -0.41f, 0.22f, -0.75f, 0.5f, -0.75f)
                reflectiveCurveTo(11.0f, 15.59f, 11.0f, 16.0f)
                curveTo(11.0f, 16.41f, 10.78f, 16.75f, 10.5f, 16.75f)
                close()
                moveTo(12.0f, 19.5f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.72f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.28f, 0.45f, -0.5f, 1.0f, -0.5f)
                reflectiveCurveToRelative(1.0f, 0.22f, 1.0f, 0.5f)
                curveTo(13.0f, 18.78f, 12.55f, 19.5f, 12.0f, 19.5f)
                close()
                moveTo(13.5f, 16.75f)
                curveToRelative(-0.28f, 0.0f, -0.5f, -0.34f, -0.5f, -0.75f)
                curveToRelative(0.0f, -0.41f, 0.22f, -0.75f, 0.5f, -0.75f)
                reflectiveCurveTo(14.0f, 15.59f, 14.0f, 16.0f)
                curveTo(14.0f, 16.41f, 13.78f, 16.75f, 13.5f, 16.75f)
                close()
                moveTo(13.5f, 11.67f)
                curveToRelative(0.14f, -4.93f, 2.44f, -7.33f, 3.38f, -7.64f)
                curveTo(16.94f, 4.2f, 17.0f, 4.51f, 17.0f, 5.0f)
                curveToRelative(0.0f, 3.12f, -1.34f, 5.7f, -2.35f, 7.2f)
                curveTo(14.34f, 11.97f, 13.97f, 11.79f, 13.5f, 11.67f)
                close()
            }
        }
        return _crueltyFree!!
    }

private var _crueltyFree: ImageVector? = null

internal val Icons.Rounded.CurrencyPound: ImageVector
    get() {
        if (_currencyPound != null) {
            return _currencyPound!!
        }
        _currencyPound = materialIcon(name = "Rounded.CurrencyPound") {
            materialPath {
                moveTo(17.21f, 17.61f)
                curveToRelative(-0.47f, -0.24f, -1.03f, -0.05f, -1.31f, 0.4f)
                curveTo(15.54f, 18.61f, 14.93f, 19.0f, 14.0f, 19.0f)
                lineToRelative(-4.9f, 0.0f)
                curveToRelative(0.83f, -1.0f, 1.5f, -2.34f, 1.5f, -4.0f)
                curveToRelative(0.0f, -0.35f, -0.03f, -0.69f, -0.08f, -1.0f)
                lineTo(13.0f, 14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineToRelative(-3.18f, 0.0f)
                curveTo(9.0f, 10.42f, 8.0f, 9.6f, 8.0f, 8.0f)
                curveToRelative(0.0f, -1.93f, 1.57f, -3.5f, 3.5f, -3.5f)
                curveToRelative(1.2f, 0.0f, 2.26f, 0.61f, 2.89f, 1.53f)
                curveToRelative(0.27f, 0.4f, 0.77f, 0.59f, 1.22f, 0.4f)
                curveToRelative(0.6f, -0.25f, 0.8f, -0.99f, 0.43f, -1.53f)
                curveToRelative(-0.99f, -1.45f, -2.66f, -2.4f, -4.54f, -2.4f)
                curveTo(8.46f, 2.5f, 6.0f, 4.96f, 6.0f, 8.0f)
                curveToRelative(0.0f, 1.78f, 0.79f, 2.9f, 1.49f, 4.0f)
                lineTo(7.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(1.47f, 0.0f)
                curveToRelative(0.08f, 0.31f, 0.13f, 0.64f, 0.13f, 1.0f)
                curveToRelative(0.0f, 1.9f, -1.29f, 3.11f, -2.06f, 3.66f)
                curveTo(6.2f, 18.9f, 6.0f, 19.29f, 6.0f, 19.71f)
                verticalLineToRelative(0.0f)
                curveTo(6.0f, 20.42f, 6.58f, 21.0f, 7.29f, 21.0f)
                horizontalLineTo(14.0f)
                curveToRelative(1.55f, 0.0f, 2.95f, -0.76f, 3.63f, -2.0f)
                curveTo(17.91f, 18.49f, 17.72f, 17.86f, 17.21f, 17.61f)
                close()
            }
        }
        return _currencyPound!!
    }

private var _currencyPound: ImageVector? = null

internal val Icons.Rounded.CurrencyRupee: ImageVector
    get() {
        if (_currencyRupee != null) {
            return _currencyRupee!!
        }
        _currencyRupee = materialIcon(name = "Rounded.CurrencyRupee") {
            materialPath {
                moveTo(10.5f, 14.0f)
                horizontalLineTo(9.77f)
                lineToRelative(5.1f, 5.31f)
                curveToRelative(0.61f, 0.64f, 0.16f, 1.69f, -0.72f, 1.69f)
                curveToRelative(-0.27f, 0.0f, -0.53f, -0.11f, -0.72f, -0.31f)
                lineTo(7.4f, 14.41f)
                curveTo(7.14f, 14.15f, 7.0f, 13.79f, 7.0f, 13.43f)
                curveTo(7.0f, 12.64f, 7.64f, 12.0f, 8.43f, 12.0f)
                horizontalLineToRelative(2.07f)
                curveToRelative(1.76f, 0.0f, 3.22f, -1.3f, 3.46f, -3.0f)
                lineTo(7.0f, 9.0f)
                curveTo(6.45f, 9.0f, 6.0f, 8.55f, 6.0f, 8.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                lineToRelative(6.66f, 0.0f)
                curveTo(13.1f, 5.82f, 11.9f, 5.0f, 10.5f, 5.0f)
                lineTo(7.0f, 5.0f)
                curveTo(6.45f, 5.0f, 6.0f, 4.55f, 6.0f, 4.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                lineToRelative(-2.26f, 0.0f)
                curveToRelative(0.48f, 0.58f, 0.84f, 1.26f, 1.05f, 2.0f)
                lineTo(17.0f, 7.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                lineToRelative(-1.02f, 0.0f)
                curveTo(15.72f, 11.8f, 13.36f, 14.0f, 10.5f, 14.0f)
                close()
            }
        }
        return _currencyRupee!!
    }

private var _currencyRupee: ImageVector? = null

internal val Icons.Rounded.CurrencyYen: ImageVector
    get() {
        if (_currencyYen != null) {
            return _currencyYen!!
        }
        _currencyYen = materialIcon(name = "Rounded.CurrencyYen") {
            materialPath {
                moveTo(6.82f, 3.0f)
                curveToRelative(0.34f, 0.0f, 0.66f, 0.17f, 0.84f, 0.46f)
                lineTo(12.0f, 10.29f)
                lineToRelative(4.34f, -6.83f)
                curveTo(16.52f, 3.17f, 16.84f, 3.0f, 17.18f, 3.0f)
                curveToRelative(0.79f, 0.0f, 1.27f, 0.87f, 0.84f, 1.54f)
                lineTo(13.92f, 11.0f)
                horizontalLineTo(17.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(3.08f)
                lineTo(5.98f, 4.54f)
                curveTo(5.55f, 3.87f, 6.03f, 3.0f, 6.82f, 3.0f)
                close()
            }
        }
        return _currencyYen!!
    }

private var _currencyYen: ImageVector? = null

internal val Icons.Rounded.Dangerous: ImageVector
    get() {
        if (_dangerous != null) {
            return _dangerous!!
        }
        _dangerous = materialIcon(name = "Rounded.Dangerous") {
            materialPath {
                moveTo(14.9f, 3.0f)
                horizontalLineTo(9.1f)
                curveTo(8.57f, 3.0f, 8.06f, 3.21f, 7.68f, 3.59f)
                lineToRelative(-4.1f, 4.1f)
                curveTo(3.21f, 8.06f, 3.0f, 8.57f, 3.0f, 9.1f)
                verticalLineToRelative(5.8f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
                lineToRelative(4.1f, 4.1f)
                curveTo(8.06f, 20.79f, 8.57f, 21.0f, 9.1f, 21.0f)
                horizontalLineToRelative(5.8f)
                curveToRelative(0.53f, 0.0f, 1.04f, -0.21f, 1.41f, -0.59f)
                lineToRelative(4.1f, -4.1f)
                curveTo(20.79f, 15.94f, 21.0f, 15.43f, 21.0f, 14.9f)
                verticalLineTo(9.1f)
                curveToRelative(0.0f, -0.53f, -0.21f, -1.04f, -0.59f, -1.41f)
                lineToRelative(-4.1f, -4.1f)
                curveTo(15.94f, 3.21f, 15.43f, 3.0f, 14.9f, 3.0f)
                close()
                moveTo(15.54f, 15.54f)
                lineTo(15.54f, 15.54f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                lineTo(12.0f, 13.41f)
                lineToRelative(-2.12f, 2.12f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(10.59f, 12.0f)
                lineTo(8.46f, 9.88f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineTo(12.0f, 10.59f)
                lineToRelative(2.12f, -2.12f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(13.41f, 12.0f)
                lineToRelative(2.12f, 2.12f)
                curveTo(15.93f, 14.51f, 15.93f, 15.15f, 15.54f, 15.54f)
                close()
            }
        }
        return _dangerous!!
    }

private var _dangerous: ImageVector? = null

internal val Icons.Rounded.DarkMode: ImageVector
    get() {
        if (_darkMode != null) {
            return _darkMode!!
        }
        _darkMode = materialIcon(name = "Rounded.DarkMode") {
            materialPath {
                moveTo(11.01f, 3.05f)
                curveTo(6.51f, 3.54f, 3.0f, 7.36f, 3.0f, 12.0f)
                curveToRelative(0.0f, 4.97f, 4.03f, 9.0f, 9.0f, 9.0f)
                curveToRelative(4.63f, 0.0f, 8.45f, -3.5f, 8.95f, -8.0f)
                curveToRelative(0.09f, -0.79f, -0.78f, -1.42f, -1.54f, -0.95f)
                curveToRelative(-0.84f, 0.54f, -1.84f, 0.85f, -2.91f, 0.85f)
                curveToRelative(-2.98f, 0.0f, -5.4f, -2.42f, -5.4f, -5.4f)
                curveToRelative(0.0f, -1.06f, 0.31f, -2.06f, 0.84f, -2.89f)
                curveTo(12.39f, 3.94f, 11.9f, 2.98f, 11.01f, 3.05f)
                close()
            }
        }
        return _darkMode!!
    }

private var _darkMode: ImageVector? = null

internal val Icons.Rounded.Dashboard: ImageVector
    get() {
        if (_dashboard != null) {
            return _dashboard!!
        }
        _dashboard = materialIcon(name = "Rounded.Dashboard") {
            materialPath {
                moveTo(4.0f, 13.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(4.0f, 21.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 15.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(14.0f, 21.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(13.0f, 4.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(21.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _dashboard!!
    }

private var _dashboard: ImageVector? = null

internal val Icons.Rounded.Description: ImageVector
    get() {
        if (_description != null) {
            return _description!!
        }
        _description = materialIcon(name = "Rounded.Description") {
            materialPath {
                moveTo(14.59f, 2.59f)
                curveToRelative(-0.38f, -0.38f, -0.89f, -0.59f, -1.42f, -0.59f)
                lineTo(6.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                lineTo(18.0f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 8.83f)
                curveToRelative(0.0f, -0.53f, -0.21f, -1.04f, -0.59f, -1.41f)
                lineToRelative(-4.82f, -4.83f)
                close()
                moveTo(15.0f, 18.0f)
                lineTo(9.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(15.0f, 14.0f)
                lineTo(9.0f, 14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(13.0f, 8.0f)
                lineTo(13.0f, 3.5f)
                lineTo(18.5f, 9.0f)
                lineTo(14.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                close()
            }
        }
        return _description!!
    }

private var _description: ImageVector? = null

internal val Icons.Rounded.DesignServices: ImageVector
    get() {
        if (_designServices != null) {
            return _designServices!!
        }
        _designServices = materialIcon(name = "Rounded.DesignServices") {
            materialPath {
                moveTo(16.24f, 11.51f)
                lineToRelative(1.57f, -1.57f)
                lineToRelative(-3.75f, -3.75f)
                lineToRelative(-1.57f, 1.57f)
                lineTo(8.35f, 3.63f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineToRelative(-1.9f, 1.9f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                lineToRelative(4.13f, 4.13f)
                lineTo(3.15f, 17.1f)
                curveTo(3.05f, 17.2f, 3.0f, 17.32f, 3.0f, 17.46f)
                verticalLineToRelative(3.04f)
                curveTo(3.0f, 20.78f, 3.22f, 21.0f, 3.5f, 21.0f)
                horizontalLineToRelative(3.04f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.35f, -0.15f)
                lineToRelative(4.62f, -4.62f)
                lineToRelative(4.13f, 4.13f)
                curveToRelative(1.32f, 1.32f, 2.76f, 0.07f, 2.83f, 0.0f)
                lineToRelative(1.9f, -1.9f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineTo(16.24f, 11.51f)
                close()
                moveTo(9.18f, 11.07f)
                lineTo(5.04f, 6.94f)
                lineToRelative(1.89f, -1.9f)
                curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                lineToRelative(1.27f, 1.27f)
                lineTo(7.73f, 6.8f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.48f, -0.48f)
                lineToRelative(1.45f, 1.45f)
                lineTo(9.18f, 11.07f)
                close()
                moveTo(17.06f, 18.96f)
                lineToRelative(-4.13f, -4.13f)
                lineToRelative(1.9f, -1.9f)
                lineToRelative(1.45f, 1.45f)
                lineToRelative(-0.48f, 0.48f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.48f, -0.48f)
                lineToRelative(1.27f, 1.27f)
                lineTo(17.06f, 18.96f)
                close()
            }
            materialPath {
                moveTo(20.71f, 7.04f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-2.34f, -2.34f)
                curveToRelative(-0.47f, -0.47f, -1.12f, -0.29f, -1.41f, 0.0f)
                lineToRelative(-1.83f, 1.83f)
                lineToRelative(3.75f, 3.75f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }
        return _designServices!!
    }

private var _designServices: ImageVector? = null

internal val Icons.Rounded.Diamond: ImageVector
    get() {
        if (_diamond != null) {
            return _diamond!!
        }
        _diamond = materialIcon(name = "Rounded.Diamond") {
            materialPath {
                moveTo(12.16f, 3.0f)
                lineToRelative(-0.32f, 0.0f)
                lineToRelative(-2.63f, 5.25f)
                lineToRelative(5.58f, 0.0f)
                close()
            }
            materialPath {
                moveTo(16.46f, 8.25f)
                horizontalLineToRelative(5.16f)
                lineToRelative(-2.07f, -4.14f)
                curveTo(19.21f, 3.43f, 18.52f, 3.0f, 17.76f, 3.0f)
                horizontalLineToRelative(-3.93f)
                lineTo(16.46f, 8.25f)
                close()
            }
            materialPath {
                moveTo(21.38f, 9.75f)
                lineToRelative(-8.63f, 0.0f)
                lineToRelative(0.0f, 10.35f)
                close()
            }
            materialPath {
                moveTo(11.25f, 20.1f)
                lineToRelative(0.0f, -10.35f)
                lineToRelative(-8.63f, 0.0f)
                close()
            }
            materialPath {
                moveTo(7.54f, 8.25f)
                lineTo(10.16f, 3.0f)
                horizontalLineTo(6.24f)
                curveTo(5.48f, 3.0f, 4.79f, 3.43f, 4.45f, 4.11f)
                lineTo(2.38f, 8.25f)
                horizontalLineTo(7.54f)
                close()
            }
        }
        return _diamond!!
    }

private var _diamond: ImageVector? = null

internal val Icons.Rounded.DirectionsBoat: ImageVector
    get() {
        if (_directionsBoat != null) {
            return _directionsBoat!!
        }
        _directionsBoat = materialIcon(name = "Rounded.DirectionsBoat") {
            materialPath {
                moveTo(20.0f, 21.0f)
                curveToRelative(-1.19f, 0.0f, -2.38f, -0.35f, -3.47f, -0.98f)
                curveToRelative(-0.33f, -0.19f, -0.73f, -0.19f, -1.07f, 0.0f)
                curveToRelative(-2.17f, 1.26f, -4.76f, 1.26f, -6.93f, 0.0f)
                curveToRelative(-0.33f, -0.19f, -0.73f, -0.19f, -1.07f, 0.0f)
                curveTo(6.38f, 20.65f, 5.19f, 21.0f, 4.0f, 21.0f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(1.38f, 0.0f, 2.74f, -0.35f, 4.0f, -0.99f)
                curveToRelative(2.52f, 1.29f, 5.48f, 1.29f, 8.0f, 0.0f)
                curveToRelative(1.26f, 0.65f, 2.62f, 0.99f, 4.0f, 0.99f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(20.0f)
                close()
                moveTo(3.95f, 19.0f)
                horizontalLineTo(4.0f)
                curveToRelative(1.27f, 0.0f, 2.42f, -0.55f, 3.33f, -1.33f)
                curveToRelative(0.39f, -0.34f, 0.95f, -0.34f, 1.34f, 0.0f)
                curveTo(9.58f, 18.45f, 10.73f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(2.42f, -0.55f, 3.33f, -1.33f)
                curveToRelative(0.39f, -0.34f, 0.95f, -0.34f, 1.34f, 0.0f)
                curveTo(17.58f, 18.45f, 18.73f, 19.0f, 20.0f, 19.0f)
                horizontalLineToRelative(0.05f)
                lineToRelative(1.9f, -6.68f)
                curveToRelative(0.11f, -0.37f, 0.04f, -1.06f, -0.66f, -1.28f)
                lineTo(20.0f, 10.62f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 1.0f, 9.0f, 1.45f, 9.0f, 2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 4.0f, 4.0f, 4.9f, 4.0f, 6.0f)
                verticalLineToRelative(4.62f)
                lineToRelative(-1.29f, 0.42f)
                curveToRelative(-0.63f, 0.19f, -0.81f, 0.84f, -0.66f, 1.28f)
                lineTo(3.95f, 19.0f)
                close()
                moveTo(6.0f, 6.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(3.97f)
                lineTo(12.62f, 8.2f)
                curveToRelative(-0.41f, -0.13f, -0.84f, -0.13f, -1.25f, 0.0f)
                lineTo(6.0f, 9.97f)
                verticalLineTo(6.0f)
                close()
            }
        }
        return _directionsBoat!!
    }

private var _directionsBoat: ImageVector? = null

internal val Icons.Rounded.DirectionsBus: ImageVector
    get() {
        if (_directionsBus != null) {
            return _directionsBus!!
        }
        _directionsBus = materialIcon(name = "Rounded.DirectionsBus") {
            materialPath {
                moveTo(4.0f, 16.0f)
                curveToRelative(0.0f, 0.88f, 0.39f, 1.67f, 1.0f, 2.22f)
                verticalLineToRelative(1.28f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveTo(8.0f, 20.33f, 8.0f, 19.5f)
                lineTo(8.0f, 19.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(0.5f)
                curveToRelative(0.0f, 0.82f, 0.67f, 1.5f, 1.5f, 1.5f)
                curveToRelative(0.82f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineToRelative(-1.28f)
                curveToRelative(0.61f, -0.55f, 1.0f, -1.34f, 1.0f, -2.22f)
                lineTo(20.0f, 6.0f)
                curveToRelative(0.0f, -3.5f, -3.58f, -4.0f, -8.0f, -4.0f)
                reflectiveCurveToRelative(-8.0f, 0.5f, -8.0f, 4.0f)
                verticalLineToRelative(10.0f)
                close()
                moveTo(7.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 14.0f, 7.5f, 14.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 17.0f, 7.5f, 17.0f)
                close()
                moveTo(16.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(18.0f, 11.0f)
                lineTo(6.0f, 11.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(5.0f)
                close()
            }
        }
        return _directionsBus!!
    }

private var _directionsBus: ImageVector? = null

internal val Icons.Rounded.DirectionsCar: ImageVector
    get() {
        if (_directionsCar != null) {
            return _directionsCar!!
        }
        _directionsCar = materialIcon(name = "Rounded.DirectionsCar") {
            materialPath {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5.0f, 17.5f, 5.0f)
                horizontalLineToRelative(-11.0f)
                curveToRelative(-0.66f, 0.0f, -1.21f, 0.42f, -1.42f, 1.01f)
                lineToRelative(-1.97f, 5.67f)
                curveToRelative(-0.07f, 0.21f, -0.11f, 0.43f, -0.11f, 0.66f)
                verticalLineToRelative(7.16f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveTo(6.0f, 20.33f, 6.0f, 19.5f)
                lineTo(6.0f, 19.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(0.5f)
                curveToRelative(0.0f, 0.82f, 0.67f, 1.5f, 1.5f, 1.5f)
                curveToRelative(0.82f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineToRelative(-7.16f)
                curveToRelative(0.0f, -0.22f, -0.04f, -0.45f, -0.11f, -0.66f)
                lineToRelative(-1.97f, -5.67f)
                close()
                moveTo(6.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(5.67f, 13.0f, 6.5f, 13.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(7.33f, 16.0f, 6.5f, 16.0f)
                close()
                moveTo(17.5f, 16.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(5.0f, 11.0f)
                lineToRelative(1.27f, -3.82f)
                curveToRelative(0.14f, -0.4f, 0.52f, -0.68f, 0.95f, -0.68f)
                horizontalLineToRelative(9.56f)
                curveToRelative(0.43f, 0.0f, 0.81f, 0.28f, 0.95f, 0.68f)
                lineTo(19.0f, 11.0f)
                lineTo(5.0f, 11.0f)
                close()
            }
        }
        return _directionsCar!!
    }

private var _directionsCar: ImageVector? = null

internal val Icons.Rounded.Discount: ImageVector
    get() {
        if (_discount != null) {
            return _discount!!
        }
        _discount = materialIcon(name = "Rounded.Discount") {
            materialPath {
                moveTo(12.79f, 21.0f)
                lineTo(3.0f, 11.21f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
                lineToRelative(7.79f, 7.79f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(6.21f, -6.21f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineTo(12.79f, 21.0f)
                close()
            }
            materialPath {
                moveTo(11.38f, 17.41f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(6.21f, -6.21f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineToRelative(-7.79f, -7.79f)
                curveTo(12.25f, 0.21f, 11.74f, 0.0f, 11.21f, 0.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 0.0f, 3.0f, 0.9f, 3.0f, 2.0f)
                verticalLineToRelative(6.21f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
                lineTo(11.38f, 17.41f)
                close()
                moveTo(7.25f, 3.0f)
                curveTo(7.94f, 3.0f, 8.5f, 3.56f, 8.5f, 4.25f)
                reflectiveCurveTo(7.94f, 5.5f, 7.25f, 5.5f)
                reflectiveCurveTo(6.0f, 4.94f, 6.0f, 4.25f)
                reflectiveCurveTo(6.56f, 3.0f, 7.25f, 3.0f)
                close()
            }
        }
        return _discount!!
    }

private var _discount: ImageVector? = null

internal val Icons.Rounded.Dns: ImageVector
    get() {
        if (_dns != null) {
            return _dns!!
        }
        _dns = materialIcon(name = "Rounded.Dns") {
            materialPath {
                moveTo(19.0f, 13.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.0f, 19.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.0f, 9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _dns!!
    }

private var _dns: ImageVector? = null

internal val Icons.Rounded.DocumentScanner: ImageVector
    get() {
        if (_documentScanner != null) {
            return _documentScanner!!
        }
        _documentScanner = materialIcon(name = "Rounded.DocumentScanner") {
            materialPath {
                moveTo(3.0f, 6.0f)
                curveTo(2.45f, 6.0f, 2.0f, 5.55f, 2.0f, 5.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveTo(6.55f, 3.0f, 6.0f, 3.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(2.0f)
                curveTo(4.0f, 5.55f, 3.55f, 6.0f, 3.0f, 6.0f)
                close()
                moveTo(17.0f, 2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                curveTo(17.45f, 1.0f, 17.0f, 1.45f, 17.0f, 2.0f)
                close()
                moveTo(3.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(-2.0f)
                curveTo(4.0f, 18.45f, 3.55f, 18.0f, 3.0f, 18.0f)
                close()
                moveTo(17.0f, 22.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(17.45f, 21.0f, 17.0f, 21.45f, 17.0f, 22.0f)
                close()
                moveTo(19.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(9.0f, 9.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 8.0f, 9.0f, 8.45f, 9.0f, 9.0f)
                close()
                moveTo(9.0f, 12.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 11.0f, 9.0f, 11.45f, 9.0f, 12.0f)
                close()
                moveTo(9.0f, 15.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 14.0f, 9.0f, 14.45f, 9.0f, 15.0f)
                close()
            }
        }
        return _documentScanner!!
    }

private var _documentScanner: ImageVector? = null

internal val Icons.Rounded.DoorFront: ImageVector
    get() {
        if (_doorFront != null) {
            return _doorFront!!
        }
        _doorFront = materialIcon(name = "Rounded.DoorFront") {
            materialPath {
                moveTo(20.0f, 19.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 3.0f, 5.0f, 3.9f, 5.0f, 5.0f)
                verticalLineToRelative(14.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveTo(21.0f, 19.45f, 20.55f, 19.0f, 20.0f, 19.0f)
                close()
                moveTo(14.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(15.0f, 12.55f, 14.55f, 13.0f, 14.0f, 13.0f)
                close()
            }
        }
        return _doorFront!!
    }

private var _doorFront: ImageVector? = null

internal val Icons.Rounded.Download: ImageVector
    get() {
        if (_download != null) {
            return _download!!
        }
        _download = materialIcon(name = "Rounded.Download") {
            materialPath {
                moveTo(16.59f, 9.0f)
                horizontalLineTo(15.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(7.41f)
                curveToRelative(-0.89f, 0.0f, -1.34f, 1.08f, -0.71f, 1.71f)
                lineToRelative(4.59f, 4.59f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.59f, -4.59f)
                curveToRelative(0.63f, -0.63f, 0.19f, -1.71f, -0.7f, -1.71f)
                close()
                moveTo(5.0f, 19.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _download!!
    }

private var _download: ImageVector? = null

internal val Icons.Rounded.DragIndicator: ImageVector
    get() {
        if (_dragIndicator != null) {
            return _dragIndicator!!
        }
        _dragIndicator = materialIcon(name = "Rounded.DragIndicator") {
            materialPath {
                moveTo(11.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                close()
                moveTo(9.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(9.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(15.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(15.0f, 16.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _dragIndicator!!
    }

private var _dragIndicator: ImageVector? = null

internal val Icons.Rounded.Eco: ImageVector
    get() {
        if (_eco != null) {
            return _eco!!
        }
        _eco = materialIcon(name = "Rounded.Eco") {
            materialPath {
                moveTo(19.95f, 5.97f)
                curveToRelative(-0.05f, -1.04f, -0.89f, -1.88f, -1.92f, -1.92f)
                curveTo(17.33f, 4.02f, 16.66f, 4.0f, 16.01f, 4.0f)
                curveTo(10.98f, 4.0f, 7.49f, 4.97f, 5.55f, 6.91f)
                curveToRelative(-3.68f, 3.68f, -3.15f, 8.9f, 0.09f, 11.9f)
                curveToRelative(0.01f, 0.0f, 0.01f, 0.0f, 0.01f, 0.0f)
                curveToRelative(1.51f, -4.22f, 4.52f, -7.16f, 7.67f, -8.8f)
                curveToRelative(-0.21f, 0.18f, -4.7f, 3.58f, -5.51f, 10.25f)
                curveToRelative(1.05f, 0.48f, 2.2f, 0.75f, 3.36f, 0.75f)
                curveToRelative(2.05f, 0.0f, 4.16f, -0.8f, 5.92f, -2.55f)
                curveTo(19.28f, 16.26f, 20.23f, 12.1f, 19.95f, 5.97f)
                close()
            }
        }
        return _eco!!
    }

private var _eco: ImageVector? = null

internal val Icons.Rounded.EditNote: ImageVector
    get() {
        if (_editNote != null) {
            return _editNote!!
        }
        _editNote = materialIcon(name = "Rounded.EditNote") {
            materialPath {
                moveTo(14.0f, 11.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(9.0f)
                curveTo(13.55f, 10.0f, 14.0f, 10.45f, 14.0f, 11.0f)
                close()
                moveTo(3.0f, 7.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(9.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.0f)
                curveTo(3.45f, 6.0f, 3.0f, 6.45f, 3.0f, 7.0f)
                close()
                moveTo(10.0f, 15.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.0f)
                curveTo(9.55f, 16.0f, 10.0f, 15.55f, 10.0f, 15.0f)
                close()
                moveTo(18.01f, 12.87f)
                lineToRelative(0.71f, -0.71f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(0.71f, 0.71f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(18.01f, 12.87f)
                close()
                moveTo(17.3f, 13.58f)
                lineToRelative(-5.16f, 5.16f)
                curveTo(12.05f, 18.83f, 12.0f, 18.95f, 12.0f, 19.09f)
                verticalLineToRelative(1.41f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.41f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.35f, -0.15f)
                lineToRelative(5.16f, -5.16f)
                lineTo(17.3f, 13.58f)
                close()
            }
        }
        return _editNote!!
    }

private var _editNote: ImageVector? = null

internal val Icons.Rounded.Egg: ImageVector
    get() {
        if (_egg != null) {
            return _egg!!
        }
        _egg = materialIcon(name = "Rounded.Egg") {
            materialPath {
                moveTo(12.0f, 3.0f)
                curveTo(8.5f, 3.0f, 5.0f, 9.33f, 5.0f, 14.0f)
                curveToRelative(0.0f, 3.87f, 3.13f, 7.0f, 7.0f, 7.0f)
                reflectiveCurveToRelative(7.0f, -3.13f, 7.0f, -7.0f)
                curveTo(19.0f, 9.33f, 15.5f, 3.0f, 12.0f, 3.0f)
                close()
                moveTo(13.0f, 18.0f)
                curveToRelative(-3.0f, 0.0f, -5.0f, -1.99f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 2.92f, 2.42f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveTo(13.55f, 18.0f, 13.0f, 18.0f)
                close()
            }
        }
        return _egg!!
    }

private var _egg: ImageVector? = null

internal val Icons.Rounded.EmojiEvents: ImageVector
    get() {
        if (_emojiEvents != null) {
            return _emojiEvents!!
        }
        _emojiEvents = materialIcon(name = "Rounded.EmojiEvents") {
            materialPath {
                moveTo(19.0f, 5.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                curveTo(7.45f, 3.0f, 7.0f, 3.45f, 7.0f, 4.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 5.0f, 3.0f, 5.9f, 3.0f, 7.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 2.55f, 1.92f, 4.63f, 4.39f, 4.94f)
                curveToRelative(0.63f, 1.5f, 1.98f, 2.63f, 3.61f, 2.96f)
                verticalLineTo(19.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(-3.1f)
                curveToRelative(1.63f, -0.33f, 2.98f, -1.46f, 3.61f, -2.96f)
                curveTo(19.08f, 12.63f, 21.0f, 10.55f, 21.0f, 8.0f)
                verticalLineTo(7.0f)
                curveTo(21.0f, 5.9f, 20.1f, 5.0f, 19.0f, 5.0f)
                close()
                moveTo(5.0f, 8.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.82f)
                curveTo(5.84f, 10.4f, 5.0f, 9.3f, 5.0f, 8.0f)
                close()
                moveTo(19.0f, 8.0f)
                curveToRelative(0.0f, 1.3f, -0.84f, 2.4f, -2.0f, 2.82f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                close()
            }
        }
        return _emojiEvents!!
    }

private var _emojiEvents: ImageVector? = null

internal val Icons.Rounded.Error: ImageVector
    get() {
        if (_error != null) {
            return _error!!
        }
        _error = materialIcon(name = "Rounded.Error") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(11.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(13.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _error!!
    }

private var _error: ImageVector? = null

internal val Icons.Rounded.ErrorOutline: ImageVector
    get() {
        if (_errorOutline != null) {
            return _errorOutline!!
        }
        _errorOutline = materialIcon(name = "Rounded.ErrorOutline") {
            materialPath {
                moveTo(12.0f, 7.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(11.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(13.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _errorOutline!!
    }

private var _errorOutline: ImageVector? = null

internal val Icons.Rounded.Euro: ImageVector
    get() {
        if (_euro != null) {
            return _euro!!
        }
        _euro = materialIcon(name = "Rounded.Euro") {
            materialPath {
                moveTo(15.0f, 18.5f)
                curveToRelative(-2.51f, 0.0f, -4.68f, -1.42f, -5.76f, -3.5f)
                horizontalLineToRelative(5.14f)
                curveToRelative(0.38f, 0.0f, 0.73f, -0.21f, 0.89f, -0.55f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.33f, -0.66f, -0.15f, -1.45f, -0.89f, -1.45f)
                horizontalLineToRelative(-5.8f)
                curveToRelative(-0.05f, -0.33f, -0.08f, -0.66f, -0.08f, -1.0f)
                reflectiveCurveToRelative(0.03f, -0.67f, 0.08f, -1.0f)
                horizontalLineToRelative(5.8f)
                curveToRelative(0.38f, 0.0f, 0.73f, -0.21f, 0.89f, -0.55f)
                lineToRelative(0.0f, 0.0f)
                curveTo(15.61f, 9.78f, 15.13f, 9.0f, 14.38f, 9.0f)
                horizontalLineTo(9.24f)
                curveTo(10.32f, 6.92f, 12.5f, 5.5f, 15.0f, 5.5f)
                curveToRelative(1.25f, 0.0f, 2.42f, 0.36f, 3.42f, 0.97f)
                curveToRelative(0.5f, 0.31f, 1.15f, 0.26f, 1.57f, -0.16f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.58f, -0.58f, 0.45f, -1.53f, -0.25f, -1.96f)
                curveTo(18.36f, 3.5f, 16.73f, 3.0f, 15.0f, 3.0f)
                curveToRelative(-3.92f, 0.0f, -7.24f, 2.51f, -8.48f, 6.0f)
                horizontalLineToRelative(-2.9f)
                curveTo(3.24f, 9.0f, 2.89f, 9.21f, 2.72f, 9.55f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.39f, 10.22f, 2.87f, 11.0f, 3.62f, 11.0f)
                horizontalLineToRelative(2.44f)
                curveTo(6.02f, 11.33f, 6.0f, 11.66f, 6.0f, 12.0f)
                reflectiveCurveToRelative(0.02f, 0.67f, 0.06f, 1.0f)
                horizontalLineTo(3.62f)
                curveToRelative(-0.38f, 0.0f, -0.73f, 0.21f, -0.89f, 0.55f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.39f, 14.22f, 2.87f, 15.0f, 3.62f, 15.0f)
                horizontalLineToRelative(2.9f)
                curveToRelative(1.24f, 3.49f, 4.56f, 6.0f, 8.48f, 6.0f)
                curveToRelative(1.74f, 0.0f, 3.36f, -0.49f, 4.74f, -1.35f)
                curveToRelative(0.69f, -0.43f, 0.82f, -1.39f, 0.24f, -1.97f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.42f, -0.42f, -1.07f, -0.47f, -1.57f, -0.15f)
                curveTo(17.42f, 18.15f, 16.26f, 18.5f, 15.0f, 18.5f)
                close()
            }
        }
        return _euro!!
    }

private var _euro: ImageVector? = null

internal val Icons.Rounded.EventAvailable: ImageVector
    get() {
        if (_eventAvailable != null) {
            return _eventAvailable!!
        }
        _eventAvailable = materialIcon(name = "Rounded.EventAvailable") {
            materialPath {
                moveTo(16.0f, 10.53f)
                curveToRelative(-0.29f, -0.29f, -0.77f, -0.29f, -1.06f, 0.0f)
                lineToRelative(-4.35f, 4.35f)
                lineTo(9.0f, 13.29f)
                curveToRelative(-0.29f, -0.29f, -0.77f, -0.29f, -1.06f, 0.0f)
                curveToRelative(-0.29f, 0.29f, -0.29f, 0.77f, 0.0f, 1.06f)
                lineToRelative(1.94f, 1.94f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.7f, -4.7f)
                curveToRelative(0.3f, -0.29f, 0.3f, -0.77f, 0.01f, -1.06f)
                close()
                moveTo(19.0f, 3.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(18.0f, 2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(8.0f, 3.0f)
                lineTo(8.0f, 2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 19.0f)
                lineTo(6.0f, 19.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(5.0f, 8.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _eventAvailable!!
    }

private var _eventAvailable: ImageVector? = null

internal val Icons.Rounded.Explore: ImageVector
    get() {
        if (_explore != null) {
            return _explore!!
        }
        _explore = materialIcon(name = "Rounded.Explore") {
            materialPath {
                moveTo(12.0f, 10.9f)
                curveToRelative(-0.61f, 0.0f, -1.1f, 0.49f, -1.1f, 1.1f)
                reflectiveCurveToRelative(0.49f, 1.1f, 1.1f, 1.1f)
                curveToRelative(0.61f, 0.0f, 1.1f, -0.49f, 1.1f, -1.1f)
                reflectiveCurveToRelative(-0.49f, -1.1f, -1.1f, -1.1f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(14.19f, 14.19f)
                lineTo(6.0f, 18.0f)
                lineToRelative(3.81f, -8.19f)
                lineTo(18.0f, 6.0f)
                lineToRelative(-3.81f, 8.19f)
                close()
            }
        }
        return _explore!!
    }

private var _explore: ImageVector? = null

internal val Icons.Rounded.Extension: ImageVector
    get() {
        if (_extension != null) {
            return _extension!!
        }
        _extension = materialIcon(name = "Rounded.Extension") {
            materialPath {
                moveTo(20.5f, 11.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.5f)
                curveTo(13.0f, 2.12f, 11.88f, 1.0f, 10.5f, 1.0f)
                reflectiveCurveTo(8.0f, 2.12f, 8.0f, 3.5f)
                verticalLineTo(5.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                verticalLineToRelative(3.8f)
                horizontalLineTo(3.5f)
                curveToRelative(1.49f, 0.0f, 2.7f, 1.21f, 2.7f, 2.7f)
                reflectiveCurveToRelative(-1.21f, 2.7f, -2.7f, 2.7f)
                horizontalLineTo(2.0f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.8f)
                verticalLineToRelative(-1.5f)
                curveToRelative(0.0f, -1.49f, 1.21f, -2.7f, 2.7f, -2.7f)
                reflectiveCurveToRelative(2.7f, 1.21f, 2.7f, 2.7f)
                verticalLineTo(22.0f)
                horizontalLineTo(17.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(1.5f)
                curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                reflectiveCurveTo(21.88f, 11.0f, 20.5f, 11.0f)
                close()
            }
        }
        return _extension!!
    }

private var _extension: ImageVector? = null

internal val Icons.Rounded.Factory: ImageVector
    get() {
        if (_factory != null) {
            return _factory!!
        }
        _factory = materialIcon(name = "Rounded.Factory") {
            materialPath {
                moveTo(14.0f, 10.0f)
                verticalLineTo(8.48f)
                curveToRelative(0.0f, -0.71f, -0.71f, -1.19f, -1.37f, -0.93f)
                lineTo(9.0f, 9.0f)
                verticalLineTo(8.52f)
                curveTo(9.0f, 7.8f, 8.27f, 7.31f, 7.61f, 7.6f)
                lineTo(3.21f, 9.48f)
                curveTo(2.48f, 9.8f, 2.0f, 10.52f, 2.0f, 11.32f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(10.0f)
                horizontalLineTo(14.0f)
                close()
                moveTo(9.0f, 17.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(13.0f, 17.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(17.0f, 17.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineTo(17.0f)
                close()
            }
            materialPath {
                moveTo(20.12f, 2.0f)
                horizontalLineToRelative(-1.23f)
                curveToRelative(-0.51f, 0.0f, -0.93f, 0.38f, -0.99f, 0.88f)
                lineTo(17.2f, 8.5f)
                horizontalLineToRelative(4.6f)
                lineToRelative(-0.69f, -5.62f)
                curveTo(21.05f, 2.38f, 20.62f, 2.0f, 20.12f, 2.0f)
                close()
            }
        }
        return _factory!!
    }

private var _factory: ImageVector? = null

internal val Icons.Rounded.FiberManualRecord: ImageVector
    get() {
        if (_fiberManualRecord != null) {
            return _fiberManualRecord!!
        }
        _fiberManualRecord = materialIcon(name = "Rounded.FiberManualRecord") {
            materialPath {
                moveTo(12.0f, 12.0f)
                moveToRelative(-8.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 16.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -16.0f, 0.0f)
            }
        }
        return _fiberManualRecord!!
    }

private var _fiberManualRecord: ImageVector? = null

internal val Icons.Rounded.FileCopy: ImageVector
    get() {
        if (_fileCopy != null) {
            return _fileCopy!!
        }
        _fileCopy = materialIcon(name = "Rounded.FileCopy") {
            materialPath {
                moveTo(15.0f, 1.0f)
                lineTo(4.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(15.59f, 5.59f)
                lineToRelative(4.83f, 4.83f)
                curveToRelative(0.37f, 0.37f, 0.58f, 0.88f, 0.58f, 1.41f)
                lineTo(21.0f, 21.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                lineTo(7.99f, 23.0f)
                curveTo(6.89f, 23.0f, 6.0f, 22.1f, 6.0f, 21.0f)
                lineToRelative(0.01f, -14.0f)
                curveToRelative(0.0f, -1.1f, 0.89f, -2.0f, 1.99f, -2.0f)
                horizontalLineToRelative(6.17f)
                curveToRelative(0.53f, 0.0f, 1.04f, 0.21f, 1.42f, 0.59f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(4.5f)
                lineTo(14.0f, 6.5f)
                lineTo(14.0f, 11.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
            }
        }
        return _fileCopy!!
    }

private var _fileCopy: ImageVector? = null

internal val Icons.Rounded.FilterDrama: ImageVector
    get() {
        if (_filterDrama != null) {
            return _filterDrama!!
        }
        _filterDrama = materialIcon(name = "Rounded.FilterDrama") {
            materialPath {
                moveTo(19.35f, 10.04f)
                curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
                curveTo(9.11f, 4.0f, 6.61f, 5.64f, 5.36f, 8.04f)
                curveTo(2.35f, 8.36f, 0.0f, 10.9f, 0.0f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
                close()
                moveTo(19.0f, 18.0f)
                horizontalLineTo(6.17f)
                curveToRelative(-2.09f, 0.0f, -3.95f, -1.53f, -4.15f, -3.61f)
                curveTo(1.79f, 12.01f, 3.66f, 10.0f, 6.0f, 10.0f)
                curveToRelative(1.92f, 0.0f, 3.53f, 1.36f, 3.91f, 3.17f)
                curveToRelative(0.1f, 0.48f, 0.5f, 0.83f, 0.98f, 0.83f)
                curveToRelative(0.61f, 0.0f, 1.11f, -0.55f, 0.99f, -1.15f)
                curveToRelative(-0.43f, -2.24f, -2.11f, -4.03f, -4.29f, -4.63f)
                curveToRelative(1.1f, -1.46f, 2.89f, -2.37f, 4.89f, -2.2f)
                curveToRelative(2.88f, 0.25f, 5.01f, 2.82f, 5.01f, 5.71f)
                verticalLineTo(12.0f)
                horizontalLineToRelative(1.37f)
                curveToRelative(1.45f, 0.0f, 2.79f, 0.97f, 3.07f, 2.4f)
                curveToRelative(0.39f, 1.91f, -1.08f, 3.6f, -2.93f, 3.6f)
                close()
            }
        }
        return _filterDrama!!
    }

private var _filterDrama: ImageVector? = null

internal val Icons.Rounded.FilterList: ImageVector
    get() {
        if (_filterList != null) {
            return _filterList!!
        }
        _filterList = materialIcon(name = "Rounded.FilterList") {
            materialPath {
                moveTo(11.0f, 18.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(3.0f, 7.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(7.0f, 13.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(7.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
            }
        }
        return _filterList!!
    }

private var _filterList: ImageVector? = null

internal val Icons.Rounded.Fingerprint: ImageVector
    get() {
        if (_fingerprint != null) {
            return _fingerprint!!
        }
        _fingerprint = materialIcon(name = "Rounded.Fingerprint") {
            materialPath {
                moveTo(17.81f, 4.47f)
                curveToRelative(-0.08f, 0.0f, -0.16f, -0.02f, -0.23f, -0.06f)
                curveTo(15.66f, 3.42f, 14.0f, 3.0f, 12.01f, 3.0f)
                curveToRelative(-1.98f, 0.0f, -3.86f, 0.47f, -5.57f, 1.41f)
                curveToRelative(-0.24f, 0.13f, -0.54f, 0.04f, -0.68f, -0.2f)
                curveToRelative(-0.13f, -0.24f, -0.04f, -0.55f, 0.2f, -0.68f)
                curveTo(7.82f, 2.52f, 9.86f, 2.0f, 12.01f, 2.0f)
                curveToRelative(2.13f, 0.0f, 3.99f, 0.47f, 6.03f, 1.52f)
                curveToRelative(0.25f, 0.13f, 0.34f, 0.43f, 0.21f, 0.67f)
                curveToRelative(-0.09f, 0.18f, -0.26f, 0.28f, -0.44f, 0.28f)
                close()
                moveTo(3.5f, 9.72f)
                curveToRelative(-0.1f, 0.0f, -0.2f, -0.03f, -0.29f, -0.09f)
                curveToRelative(-0.23f, -0.16f, -0.28f, -0.47f, -0.12f, -0.7f)
                curveToRelative(0.99f, -1.4f, 2.25f, -2.5f, 3.75f, -3.27f)
                curveTo(9.98f, 4.04f, 14.0f, 4.03f, 17.15f, 5.65f)
                curveToRelative(1.5f, 0.77f, 2.76f, 1.86f, 3.75f, 3.25f)
                curveToRelative(0.16f, 0.22f, 0.11f, 0.54f, -0.12f, 0.7f)
                curveToRelative(-0.23f, 0.16f, -0.54f, 0.11f, -0.7f, -0.12f)
                curveToRelative(-0.9f, -1.26f, -2.04f, -2.25f, -3.39f, -2.94f)
                curveToRelative(-2.87f, -1.47f, -6.54f, -1.47f, -9.4f, 0.01f)
                curveToRelative(-1.36f, 0.7f, -2.5f, 1.7f, -3.4f, 2.96f)
                curveToRelative(-0.08f, 0.14f, -0.23f, 0.21f, -0.39f, 0.21f)
                close()
                moveTo(9.75f, 21.79f)
                curveToRelative(-0.13f, 0.0f, -0.26f, -0.05f, -0.35f, -0.15f)
                curveToRelative(-0.87f, -0.87f, -1.34f, -1.43f, -2.01f, -2.64f)
                curveToRelative(-0.69f, -1.23f, -1.05f, -2.73f, -1.05f, -4.34f)
                curveToRelative(0.0f, -2.97f, 2.54f, -5.39f, 5.66f, -5.39f)
                reflectiveCurveToRelative(5.66f, 2.42f, 5.66f, 5.39f)
                curveToRelative(0.0f, 0.28f, -0.22f, 0.5f, -0.5f, 0.5f)
                reflectiveCurveToRelative(-0.5f, -0.22f, -0.5f, -0.5f)
                curveToRelative(0.0f, -2.42f, -2.09f, -4.39f, -4.66f, -4.39f)
                reflectiveCurveToRelative(-4.66f, 1.97f, -4.66f, 4.39f)
                curveToRelative(0.0f, 1.44f, 0.32f, 2.77f, 0.93f, 3.85f)
                curveToRelative(0.64f, 1.15f, 1.08f, 1.64f, 1.85f, 2.42f)
                curveToRelative(0.19f, 0.2f, 0.19f, 0.51f, 0.0f, 0.71f)
                curveToRelative(-0.11f, 0.1f, -0.24f, 0.15f, -0.37f, 0.15f)
                close()
                moveTo(16.92f, 19.94f)
                curveToRelative(-1.19f, 0.0f, -2.24f, -0.3f, -3.1f, -0.89f)
                curveToRelative(-1.49f, -1.01f, -2.38f, -2.65f, -2.38f, -4.39f)
                curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                reflectiveCurveToRelative(0.5f, 0.22f, 0.5f, 0.5f)
                curveToRelative(0.0f, 1.41f, 0.72f, 2.74f, 1.94f, 3.56f)
                curveToRelative(0.71f, 0.48f, 1.54f, 0.71f, 2.54f, 0.71f)
                curveToRelative(0.24f, 0.0f, 0.64f, -0.03f, 1.04f, -0.1f)
                curveToRelative(0.27f, -0.05f, 0.53f, 0.13f, 0.58f, 0.41f)
                curveToRelative(0.05f, 0.27f, -0.13f, 0.53f, -0.41f, 0.58f)
                curveToRelative(-0.57f, 0.11f, -1.07f, 0.12f, -1.21f, 0.12f)
                close()
                moveTo(14.91f, 22.0f)
                curveToRelative(-0.04f, 0.0f, -0.09f, -0.01f, -0.13f, -0.02f)
                curveToRelative(-1.59f, -0.44f, -2.63f, -1.03f, -3.72f, -2.1f)
                curveToRelative(-1.4f, -1.39f, -2.17f, -3.24f, -2.17f, -5.22f)
                curveToRelative(0.0f, -1.62f, 1.38f, -2.94f, 3.08f, -2.94f)
                reflectiveCurveToRelative(3.08f, 1.32f, 3.08f, 2.94f)
                curveToRelative(0.0f, 1.07f, 0.93f, 1.94f, 2.08f, 1.94f)
                reflectiveCurveToRelative(2.08f, -0.87f, 2.08f, -1.94f)
                curveToRelative(0.0f, -3.77f, -3.25f, -6.83f, -7.25f, -6.83f)
                curveToRelative(-2.84f, 0.0f, -5.44f, 1.58f, -6.61f, 4.03f)
                curveToRelative(-0.39f, 0.81f, -0.59f, 1.76f, -0.59f, 2.8f)
                curveToRelative(0.0f, 0.78f, 0.07f, 2.01f, 0.67f, 3.61f)
                curveToRelative(0.1f, 0.26f, -0.03f, 0.55f, -0.29f, 0.64f)
                curveToRelative(-0.26f, 0.1f, -0.55f, -0.04f, -0.64f, -0.29f)
                curveToRelative(-0.49f, -1.31f, -0.73f, -2.61f, -0.73f, -3.96f)
                curveToRelative(0.0f, -1.2f, 0.23f, -2.29f, 0.68f, -3.24f)
                curveToRelative(1.33f, -2.79f, 4.28f, -4.6f, 7.51f, -4.6f)
                curveToRelative(4.55f, 0.0f, 8.25f, 3.51f, 8.25f, 7.83f)
                curveToRelative(0.0f, 1.62f, -1.38f, 2.94f, -3.08f, 2.94f)
                reflectiveCurveToRelative(-3.08f, -1.32f, -3.08f, -2.94f)
                curveToRelative(0.0f, -1.07f, -0.93f, -1.94f, -2.08f, -1.94f)
                reflectiveCurveToRelative(-2.08f, 0.87f, -2.08f, 1.94f)
                curveToRelative(0.0f, 1.71f, 0.66f, 3.31f, 1.87f, 4.51f)
                curveToRelative(0.95f, 0.94f, 1.86f, 1.46f, 3.27f, 1.85f)
                curveToRelative(0.27f, 0.07f, 0.42f, 0.35f, 0.35f, 0.61f)
                curveToRelative(-0.05f, 0.23f, -0.26f, 0.38f, -0.47f, 0.38f)
                close()
            }
        }
        return _fingerprint!!
    }

private var _fingerprint: ImageVector? = null

internal val Icons.Rounded.FitnessCenter: ImageVector
    get() {
        if (_fitnessCenter != null) {
            return _fitnessCenter!!
        }
        _fitnessCenter = materialIcon(name = "Rounded.FitnessCenter") {
            materialPath {
                moveTo(20.57f, 14.86f)
                lineToRelative(0.72f, -0.72f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.02f, -0.02f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(17.0f, 15.57f)
                lineTo(8.43f, 7.0f)
                lineToRelative(2.86f, -2.86f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.02f, -0.02f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.72f, 0.72f)
                lineToRelative(-0.72f, -0.72f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                lineTo(5.57f, 4.14f)
                lineToRelative(-0.72f, -0.72f)
                curveToRelative(-0.39f, -0.39f, -1.04f, -0.39f, -1.43f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.04f, 0.0f, 1.43f)
                lineToRelative(0.72f, 0.72f)
                lineTo(2.71f, 7.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.72f, 0.72f)
                lineToRelative(-0.72f, 0.73f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.02f, 0.02f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(7.0f, 8.43f)
                lineTo(15.57f, 17.0f)
                lineToRelative(-2.86f, 2.86f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.02f, 0.02f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.72f, -0.72f)
                lineToRelative(0.72f, 0.72f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(1.43f, -1.43f)
                lineToRelative(0.72f, 0.72f)
                curveToRelative(0.39f, 0.39f, 1.04f, 0.39f, 1.43f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.04f, 0.0f, -1.43f)
                lineToRelative(-0.72f, -0.72f)
                lineTo(21.29f, 17.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.72f, -0.73f)
                close()
            }
        }
        return _fitnessCenter!!
    }

private var _fitnessCenter: ImageVector? = null

internal val Icons.Rounded.Flag: ImageVector
    get() {
        if (_flag != null) {
            return _flag!!
        }
        _flag = materialIcon(name = "Rounded.Flag") {
            materialPath {
                moveTo(14.4f, 6.0f)
                lineToRelative(-0.24f, -1.2f)
                curveToRelative(-0.09f, -0.46f, -0.5f, -0.8f, -0.98f, -0.8f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(15.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(5.6f)
                lineToRelative(0.24f, 1.2f)
                curveToRelative(0.09f, 0.47f, 0.5f, 0.8f, 0.98f, 0.8f)
                horizontalLineTo(19.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.6f)
                close()
            }
        }
        return _flag!!
    }

private var _flag: ImageVector? = null

internal val Icons.Rounded.FlashlightOn: ImageVector
    get() {
        if (_flashlightOn != null) {
            return _flashlightOn!!
        }
        _flashlightOn = materialIcon(name = "Rounded.FlashlightOn") {
            materialPath {
                moveTo(6.0f, 4.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(12.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(8.0f)
                curveTo(6.9f, 2.0f, 6.0f, 2.9f, 6.0f, 4.0f)
                close()
            }
            materialPath {
                moveTo(6.0f, 7.0f)
                verticalLineToRelative(1.0f)
                lineToRelative(2.0f, 3.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                lineToRelative(2.0f, -3.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(6.0f)
                close()
                moveTo(12.0f, 15.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(12.83f, 15.5f, 12.0f, 15.5f)
                close()
            }
        }
        return _flashlightOn!!
    }

private var _flashlightOn: ImageVector? = null

internal val Icons.Rounded.Flight: ImageVector
    get() {
        if (_flight != null) {
            return _flight!!
        }
        _flight = materialIcon(name = "Rounded.Flight") {
            materialPath {
                moveTo(21.0f, 14.58f)
                curveToRelative(0.0f, -0.36f, -0.19f, -0.69f, -0.49f, -0.89f)
                lineTo(13.0f, 9.0f)
                verticalLineTo(3.5f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(10.0f, 2.67f, 10.0f, 3.5f)
                verticalLineTo(9.0f)
                lineToRelative(-7.51f, 4.69f)
                curveToRelative(-0.3f, 0.19f, -0.49f, 0.53f, -0.49f, 0.89f)
                curveToRelative(0.0f, 0.7f, 0.68f, 1.21f, 1.36f, 1.0f)
                lineTo(10.0f, 13.5f)
                verticalLineTo(19.0f)
                lineToRelative(-1.8f, 1.35f)
                curveToRelative(-0.13f, 0.09f, -0.2f, 0.24f, -0.2f, 0.4f)
                verticalLineToRelative(0.59f)
                curveToRelative(0.0f, 0.33f, 0.32f, 0.57f, 0.64f, 0.48f)
                lineTo(11.5f, 21.0f)
                lineToRelative(2.86f, 0.82f)
                curveToRelative(0.32f, 0.09f, 0.64f, -0.15f, 0.64f, -0.48f)
                verticalLineToRelative(-0.59f)
                curveToRelative(0.0f, -0.16f, -0.07f, -0.31f, -0.2f, -0.4f)
                lineTo(13.0f, 19.0f)
                verticalLineToRelative(-5.5f)
                lineToRelative(6.64f, 2.08f)
                curveToRelative(0.68f, 0.21f, 1.36f, -0.3f, 1.36f, -1.0f)
                close()
            }
        }
        return _flight!!
    }

private var _flight: ImageVector? = null

internal val Icons.Rounded.FlightLand: ImageVector
    get() {
        if (_flightLand != null) {
            return _flightLand!!
        }
        _flightLand = materialIcon(name = "Rounded.FlightLand") {
            materialPath {
                moveTo(20.5f, 19.0f)
                horizontalLineToRelative(-17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(17.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(3.51f, 11.61f)
                lineToRelative(15.83f, 4.24f)
                curveToRelative(0.8f, 0.21f, 1.62f, -0.26f, 1.84f, -1.06f)
                curveToRelative(0.21f, -0.8f, -0.26f, -1.62f, -1.06f, -1.84f)
                lineToRelative(-5.31f, -1.42f)
                lineToRelative(-2.58f, -8.45f)
                curveToRelative(-0.11f, -0.36f, -0.39f, -0.63f, -0.75f, -0.73f)
                curveToRelative(-0.68f, -0.18f, -1.35f, 0.33f, -1.35f, 1.04f)
                verticalLineToRelative(6.88f)
                lineTo(5.15f, 8.95f)
                lineTo(4.4f, 7.09f)
                curveToRelative(-0.12f, -0.29f, -0.36f, -0.51f, -0.67f, -0.59f)
                lineToRelative(-0.33f, -0.09f)
                curveToRelative(-0.32f, -0.09f, -0.63f, 0.15f, -0.63f, 0.48f)
                verticalLineToRelative(3.75f)
                curveToRelative(0.0f, 0.46f, 0.3f, 0.85f, 0.74f, 0.97f)
                close()
            }
        }
        return _flightLand!!
    }

private var _flightLand: ImageVector? = null

internal val Icons.Rounded.FlightTakeoff: ImageVector
    get() {
        if (_flightTakeoff != null) {
            return _flightTakeoff!!
        }
        _flightTakeoff = materialIcon(name = "Rounded.FlightTakeoff") {
            materialPath {
                moveTo(20.5f, 19.0f)
                horizontalLineToRelative(-17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(17.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(22.07f, 9.64f)
                curveToRelative(-0.22f, -0.8f, -1.04f, -1.27f, -1.84f, -1.06f)
                lineTo(14.92f, 10.0f)
                lineTo(8.46f, 3.98f)
                curveToRelative(-0.27f, -0.26f, -0.66f, -0.35f, -1.02f, -0.25f)
                curveToRelative(-0.68f, 0.19f, -1.0f, 0.97f, -0.65f, 1.58f)
                lineToRelative(3.44f, 5.96f)
                lineToRelative(-4.97f, 1.33f)
                lineToRelative(-1.57f, -1.24f)
                curveToRelative(-0.25f, -0.19f, -0.57f, -0.26f, -0.88f, -0.18f)
                lineToRelative(-0.33f, 0.09f)
                curveToRelative(-0.32f, 0.08f, -0.47f, 0.45f, -0.3f, 0.73f)
                lineToRelative(1.88f, 3.25f)
                curveToRelative(0.23f, 0.39f, 0.69f, 0.58f, 1.12f, 0.47f)
                lineTo(21.0f, 11.48f)
                curveToRelative(0.8f, -0.22f, 1.28f, -1.04f, 1.07f, -1.84f)
                close()
            }
        }
        return _flightTakeoff!!
    }

private var _flightTakeoff: ImageVector? = null

internal val Icons.Rounded.FlutterDash: ImageVector
    get() {
        if (_flutterDash != null) {
            return _flutterDash!!
        }
        _flutterDash = materialIcon(name = "Rounded.FlutterDash") {
            materialPath {
                moveTo(11.07f, 11.7f)
                curveToRelative(0.29f, -0.39f, 0.81f, -0.56f, 1.27f, -0.37f)
                curveToRelative(0.17f, 0.07f, 0.32f, 0.18f, 0.43f, 0.33f)
                curveToRelative(0.22f, 0.28f, 0.25f, 0.59f, 0.22f, 0.85f)
                curveToRelative(-0.05f, 0.33f, -0.25f, 0.63f, -0.54f, 0.79f)
                curveToRelative(0.0f, 0.0f, -4.87f, 2.95f, -5.07f, 2.69f)
                reflectiveCurveTo(11.07f, 11.7f, 11.07f, 11.7f)
                close()
                moveTo(22.0f, 10.0f)
                curveToRelative(0.0f, 2.5f, -1.0f, 3.0f, -1.5f, 3.0f)
                curveToRelative(-0.23f, 0.0f, -0.44f, -0.1f, -0.62f, -0.26f)
                curveToRelative(-0.48f, 3.32f, -2.36f, 5.31f, -5.33f, 5.99f)
                curveToRelative(0.11f, 0.44f, 0.48f, 0.77f, 0.95f, 0.77f)
                lineToRelative(0.0f, 0.0f)
                horizontalLineToRelative(0.58f)
                curveToRelative(0.22f, 0.0f, 0.41f, 0.15f, 0.48f, 0.36f)
                curveToRelative(0.17f, 0.52f, 0.66f, 1.02f, 1.02f, 1.32f)
                curveToRelative(0.25f, 0.21f, 0.24f, 0.59f, -0.03f, 0.78f)
                curveToRelative(-0.34f, 0.24f, -0.9f, 0.49f, -1.79f, 0.53f)
                curveToRelative(-0.18f, 0.01f, -0.35f, -0.07f, -0.45f, -0.22f)
                curveTo(15.18f, 22.07f, 15.0f, 21.71f, 15.0f, 21.26f)
                curveToRelative(0.0f, -0.3f, 0.04f, -0.57f, 0.09f, -0.8f)
                curveToRelative(-0.78f, -0.16f, -1.39f, -0.78f, -1.55f, -1.56f)
                curveToRelative(-0.49f, 0.06f, -1.0f, 0.1f, -1.54f, 0.1f)
                curveToRelative(-0.88f, 0.0f, -1.7f, -0.09f, -2.45f, -0.25f)
                curveTo(9.53f, 18.83f, 9.5f, 18.91f, 9.5f, 19.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(0.0f, 0.0f)
                horizontalLineToRelative(0.58f)
                curveToRelative(0.22f, 0.0f, 0.41f, 0.15f, 0.48f, 0.36f)
                curveToRelative(0.17f, 0.52f, 0.66f, 1.02f, 1.02f, 1.32f)
                curveToRelative(0.25f, 0.21f, 0.24f, 0.59f, -0.03f, 0.78f)
                curveToRelative(-0.34f, 0.24f, -0.9f, 0.49f, -1.79f, 0.53f)
                curveToRelative(-0.18f, 0.01f, -0.35f, -0.07f, -0.45f, -0.22f)
                curveTo(10.18f, 22.57f, 10.0f, 22.21f, 10.0f, 21.76f)
                curveToRelative(0.0f, -0.3f, 0.04f, -0.57f, 0.09f, -0.8f)
                curveTo(9.19f, 20.77f, 8.5f, 19.96f, 8.5f, 19.0f)
                curveToRelative(0.0f, -0.18f, 0.03f, -0.36f, 0.08f, -0.53f)
                curveToRelative(-2.46f, -0.86f, -4.03f, -2.78f, -4.46f, -5.74f)
                curveTo(3.94f, 12.9f, 3.74f, 13.0f, 3.5f, 13.0f)
                curveTo(3.0f, 13.0f, 2.0f, 12.5f, 2.0f, 10.0f)
                curveToRelative(0.0f, -2.27f, 1.7f, -4.5f, 3.0f, -4.5f)
                curveToRelative(0.43f, 0.0f, 0.49f, 0.49f, 0.5f, 0.85f)
                curveToRelative(1.28f, -1.78f, 3.26f, -3.02f, 5.55f, -3.29f)
                curveTo(11.25f, 2.1f, 12.13f, 1.5f, 13.0f, 1.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.0f, 0.33f, -0.5f, 1.0f, -0.5f)
                curveToRelative(0.67f, 0.0f, 1.0f, 0.5f, 1.0f, 0.5f)
                curveToRelative(-0.49f, 0.0f, -0.85f, 0.35f, -0.96f, 0.77f)
                curveToRelative(1.82f, 0.48f, 3.39f, 1.59f, 4.46f, 3.08f)
                curveTo(18.51f, 5.99f, 18.57f, 5.5f, 19.0f, 5.5f)
                curveTo(20.3f, 5.5f, 22.0f, 7.73f, 22.0f, 10.0f)
                close()
                moveTo(5.0f, 11.0f)
                curveToRelative(0.0f, 0.81f, 0.1f, 1.53f, 0.25f, 2.21f)
                curveToRelative(0.18f, -0.69f, 0.46f, -1.33f, 0.83f, -1.92f)
                curveToRelative(-0.21f, -0.47f, -0.34f, -0.99f, -0.34f, -1.54f)
                curveTo(5.75f, 7.68f, 7.43f, 6.0f, 9.5f, 6.0f)
                curveToRelative(0.96f, 0.0f, 1.84f, 0.37f, 2.5f, 0.97f)
                curveTo(12.66f, 6.37f, 13.54f, 6.0f, 14.5f, 6.0f)
                curveToRelative(2.07f, 0.0f, 3.75f, 1.68f, 3.75f, 3.75f)
                curveToRelative(0.0f, 0.55f, -0.12f, 1.07f, -0.34f, 1.54f)
                curveToRelative(0.37f, 0.59f, 0.66f, 1.24f, 0.84f, 1.94f)
                curveTo(18.9f, 12.55f, 19.0f, 11.82f, 19.0f, 11.0f)
                curveToRelative(0.0f, -3.86f, -3.14f, -7.0f, -7.0f, -7.0f)
                curveTo(8.14f, 4.0f, 5.0f, 7.14f, 5.0f, 11.0f)
                close()
                moveTo(17.98f, 15.29f)
                curveToRelative(0.0f, -0.1f, 0.02f, -0.19f, 0.02f, -0.29f)
                curveToRelative(0.0f, -1.01f, -0.26f, -1.95f, -0.7f, -2.78f)
                curveToRelative(-0.69f, 0.78f, -1.68f, 1.28f, -2.8f, 1.28f)
                curveToRelative(-0.27f, 0.0f, -0.54f, -0.03f, -0.79f, -0.09f)
                curveToRelative(0.14f, -0.23f, 0.23f, -0.49f, 0.27f, -0.77f)
                curveToRelative(0.01f, -0.07f, 0.01f, -0.13f, 0.02f, -0.19f)
                curveToRelative(0.17f, 0.03f, 0.33f, 0.05f, 0.5f, 0.05f)
                curveToRelative(1.52f, 0.0f, 2.75f, -1.23f, 2.75f, -2.75f)
                reflectiveCurveTo(16.02f, 7.0f, 14.5f, 7.0f)
                curveToRelative(-0.67f, 0.0f, -1.32f, 0.25f, -1.83f, 0.72f)
                lineTo(12.0f, 8.32f)
                lineToRelative(-0.67f, -0.6f)
                curveTo(10.82f, 7.25f, 10.17f, 7.0f, 9.5f, 7.0f)
                curveTo(7.98f, 7.0f, 6.75f, 8.23f, 6.75f, 9.75f)
                curveToRelative(0.0f, 1.34f, 0.96f, 2.46f, 2.23f, 2.7f)
                lineToRelative(-0.76f, 0.83f)
                curveToRelative(-0.6f, -0.22f, -1.12f, -0.59f, -1.53f, -1.05f)
                curveTo(6.26f, 13.06f, 6.0f, 14.0f, 6.0f, 15.0f)
                curveToRelative(0.0f, 0.08f, 0.01f, 0.15f, 0.01f, 0.24f)
                curveTo(7.13f, 17.06f, 9.14f, 18.0f, 12.0f, 18.0f)
                curveTo(14.88f, 18.0f, 16.88f, 17.09f, 17.98f, 15.29f)
                close()
                moveTo(16.0f, 9.75f)
                curveToRelative(0.0f, 0.97f, -0.67f, 1.75f, -1.5f, 1.75f)
                reflectiveCurveTo(13.0f, 10.72f, 13.0f, 9.75f)
                reflectiveCurveTo(13.67f, 8.0f, 14.5f, 8.0f)
                reflectiveCurveTo(16.0f, 8.78f, 16.0f, 9.75f)
                close()
                moveTo(15.25f, 8.88f)
                curveToRelative(0.0f, -0.21f, -0.17f, -0.38f, -0.38f, -0.38f)
                reflectiveCurveTo(14.5f, 8.67f, 14.5f, 8.88f)
                reflectiveCurveToRelative(0.17f, 0.38f, 0.38f, 0.38f)
                reflectiveCurveTo(15.25f, 9.08f, 15.25f, 8.88f)
                close()
                moveTo(11.0f, 9.75f)
                curveToRelative(0.0f, 0.97f, -0.67f, 1.75f, -1.5f, 1.75f)
                reflectiveCurveTo(8.0f, 10.72f, 8.0f, 9.75f)
                reflectiveCurveTo(8.67f, 8.0f, 9.5f, 8.0f)
                reflectiveCurveTo(11.0f, 8.78f, 11.0f, 9.75f)
                close()
                moveTo(10.25f, 8.88f)
                curveToRelative(0.0f, -0.21f, -0.17f, -0.38f, -0.38f, -0.38f)
                reflectiveCurveTo(9.5f, 8.67f, 9.5f, 8.88f)
                reflectiveCurveToRelative(0.17f, 0.38f, 0.38f, 0.38f)
                reflectiveCurveTo(10.25f, 9.08f, 10.25f, 8.88f)
                close()
            }
        }
        return _flutterDash!!
    }

private var _flutterDash: ImageVector? = null

internal val Icons.Rounded.Folder: ImageVector
    get() {
        if (_folder != null) {
            return _folder!!
        }
        _folder = materialIcon(name = "Rounded.Folder") {
            materialPath {
                moveTo(10.59f, 4.59f)
                curveTo(10.21f, 4.21f, 9.7f, 4.0f, 9.17f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
        return _folder!!
    }

private var _folder: ImageVector? = null

internal val Icons.Rounded.FolderOpen: ImageVector
    get() {
        if (_folderOpen != null) {
            return _folderOpen!!
        }
        _folderOpen = materialIcon(name = "Rounded.FolderOpen") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-8.0f)
                lineToRelative(-1.41f, -1.41f)
                curveTo(10.21f, 4.21f, 9.7f, 4.0f, 9.17f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 18.0f)
                lineTo(5.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _folderOpen!!
    }

private var _folderOpen: ImageVector? = null

internal val Icons.Rounded.Forest: ImageVector
    get() {
        if (_forest != null) {
            return _forest!!
        }
        _forest = materialIcon(name = "Rounded.Forest") {
            materialPath {
                moveTo(14.14f, 12.0f)
                horizontalLineToRelative(-0.06f)
                curveToRelative(0.81f, 0.0f, 1.28f, -0.91f, 0.82f, -1.57f)
                lineTo(9.82f, 3.17f)
                curveToRelative(-0.4f, -0.57f, -1.24f, -0.57f, -1.64f, 0.0f)
                lineTo(3.1f, 10.43f)
                curveTo(2.64f, 11.09f, 3.11f, 12.0f, 3.92f, 12.0f)
                horizontalLineTo(3.86f)
                lineToRelative(-2.87f, 4.46f)
                curveTo(0.56f, 17.12f, 1.04f, 18.0f, 1.83f, 18.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.17f)
                curveToRelative(0.79f, 0.0f, 1.27f, -0.88f, 0.84f, -1.54f)
                lineTo(14.14f, 12.0f)
                close()
            }
            materialPath {
                moveTo(23.01f, 16.46f)
                lineTo(20.14f, 12.0f)
                horizontalLineToRelative(-0.06f)
                curveToRelative(0.81f, 0.0f, 1.28f, -0.91f, 0.82f, -1.57f)
                lineToRelative(-5.08f, -7.26f)
                curveToRelative(-0.4f, -0.57f, -1.24f, -0.57f, -1.64f, 0.0f)
                lineToRelative(-1.57f, 2.24f)
                lineToRelative(3.11f, 4.44f)
                curveToRelative(0.43f, 0.61f, 0.48f, 1.41f, 0.14f, 2.07f)
                curveToRelative(-0.08f, 0.16f, -0.18f, 0.3f, -0.3f, 0.43f)
                lineToRelative(2.29f, 3.57f)
                curveToRelative(0.4f, 0.62f, 0.42f, 1.4f, 0.07f, 2.04f)
                curveToRelative(-0.01f, 0.02f, -0.02f, 0.03f, -0.03f, 0.04f)
                horizontalLineToRelative(4.28f)
                curveTo(22.96f, 18.0f, 23.44f, 17.12f, 23.01f, 16.46f)
                close()
            }
            materialPath {
                moveTo(13.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(20.0f)
                close()
            }
        }
        return _forest!!
    }

private var _forest: ImageVector? = null

internal val Icons.Rounded.FormatListNumbered: ImageVector
    get() {
        if (_formatListNumbered != null) {
            return _formatListNumbered!!
        }
        _formatListNumbered = materialIcon(name = "Rounded.FormatListNumbered") {
            materialPath {
                moveTo(8.0f, 7.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(8.0f, 5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(20.0f, 17.0f)
                lineTo(8.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(20.0f, 11.0f)
                lineTo(8.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(4.5f, 16.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                lineTo(4.0f, 17.0f)
                verticalLineToRelative(0.5f)
                horizontalLineToRelative(-0.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                lineTo(4.0f, 18.5f)
                verticalLineToRelative(0.5f)
                lineTo(2.5f, 19.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
                moveTo(2.5f, 5.0f)
                lineTo(3.0f, 5.0f)
                verticalLineToRelative(2.5f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                reflectiveCurveToRelative(0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                close()
                moveTo(4.5f, 10.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.3f)
                lineToRelative(-1.68f, 1.96f)
                curveToRelative(-0.08f, 0.09f, -0.12f, 0.21f, -0.12f, 0.32f)
                verticalLineToRelative(0.22f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                reflectiveCurveToRelative(-0.22f, -0.5f, -0.5f, -0.5f)
                lineTo(3.2f, 13.0f)
                lineToRelative(1.68f, -1.96f)
                curveToRelative(0.08f, -0.09f, 0.12f, -0.21f, 0.12f, -0.32f)
                verticalLineToRelative(-0.22f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
            }
        }
        return _formatListNumbered!!
    }

private var _formatListNumbered: ImageVector? = null

internal val Icons.Rounded.FormatQuote: ImageVector
    get() {
        if (_formatQuote != null) {
            return _formatQuote!!
        }
        _formatQuote = materialIcon(name = "Rounded.FormatQuote") {
            materialPath {
                moveTo(7.17f, 17.0f)
                curveToRelative(0.51f, 0.0f, 0.98f, -0.29f, 1.2f, -0.74f)
                lineToRelative(1.42f, -2.84f)
                curveToRelative(0.14f, -0.28f, 0.21f, -0.58f, 0.21f, -0.89f)
                lineTo(10.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(5.0f, 7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-1.03f, 2.06f)
                curveToRelative(-0.45f, 0.89f, 0.2f, 1.94f, 1.2f, 1.94f)
                close()
                moveTo(17.17f, 17.0f)
                curveToRelative(0.51f, 0.0f, 0.98f, -0.29f, 1.2f, -0.74f)
                lineToRelative(1.42f, -2.84f)
                curveToRelative(0.14f, -0.28f, 0.21f, -0.58f, 0.21f, -0.89f)
                lineTo(20.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-1.03f, 2.06f)
                curveToRelative(-0.45f, 0.89f, 0.2f, 1.94f, 1.2f, 1.94f)
                close()
            }
        }
        return _formatQuote!!
    }

private var _formatQuote: ImageVector? = null

internal val Icons.Rounded.Forum: ImageVector
    get() {
        if (_forum != null) {
            return _forum!!
        }
        _forum = materialIcon(name = "Rounded.Forum") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(6.0f, 15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                lineToRelative(4.0f, 4.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 11.0f)
                lineTo(17.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(13.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(9.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
            }
        }
        return _forum!!
    }

private var _forum: ImageVector? = null

internal val Icons.Rounded.Fullscreen: ImageVector
    get() {
        if (_fullscreen != null) {
            return _fullscreen!!
        }
        _fullscreen = materialIcon(name = "Rounded.Fullscreen") {
            materialPath {
                moveTo(6.0f, 14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(7.0f, 17.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(6.0f, 10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(7.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(6.0f, 5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(17.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(14.0f, 6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(19.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
            }
        }
        return _fullscreen!!
    }

private var _fullscreen: ImageVector? = null

internal val Icons.Rounded.FullscreenExit: ImageVector
    get() {
        if (_fullscreenExit != null) {
            return _fullscreenExit!!
        }
        _fullscreenExit = materialIcon(name = "Rounded.FullscreenExit") {
            materialPath {
                moveTo(6.0f, 16.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(6.0f, 14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(8.0f, 8.0f)
                lineTo(6.0f, 8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(10.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(15.0f, 19.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(16.0f, 8.0f)
                lineTo(16.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-2.0f)
                close()
            }
        }
        return _fullscreenExit!!
    }

private var _fullscreenExit: ImageVector? = null

internal val Icons.Rounded.Functions: ImageVector
    get() {
        if (_functions != null) {
            return _functions!!
        }
        _functions = materialIcon(name = "Rounded.Functions") {
            materialPath {
                moveTo(16.5f, 4.0f)
                horizontalLineTo(7.56f)
                curveTo(6.7f, 4.0f, 6.0f, 4.7f, 6.0f, 5.56f)
                curveToRelative(0.0f, 0.28f, 0.12f, 0.55f, 0.32f, 0.74f)
                lineTo(12.5f, 12.0f)
                lineToRelative(-6.18f, 5.7f)
                curveToRelative(-0.2f, 0.19f, -0.32f, 0.46f, -0.32f, 0.74f)
                curveTo(6.0f, 19.3f, 6.7f, 20.0f, 7.56f, 20.0f)
                horizontalLineToRelative(8.94f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                reflectiveCurveToRelative(-0.67f, -1.5f, -1.5f, -1.5f)
                horizontalLineTo(11.0f)
                lineToRelative(3.59f, -3.59f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineTo(11.0f, 7.0f)
                horizontalLineToRelative(5.5f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                reflectiveCurveTo(17.33f, 4.0f, 16.5f, 4.0f)
                close()
            }
        }
        return _functions!!
    }

private var _functions: ImageVector? = null

internal val Icons.Rounded.Grain: ImageVector
    get() {
        if (_grain != null) {
            return _grain!!
        }
        _grain = materialIcon(name = "Rounded.Grain") {
            materialPath {
                moveTo(10.0f, 12.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(6.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(6.0f, 16.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(14.0f, 16.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 12.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(14.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(10.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _grain!!
    }

private var _grain: ImageVector? = null

internal val Icons.Rounded.Grass: ImageVector
    get() {
        if (_grass != null) {
            return _grass!!
        }
        _grass = materialIcon(name = "Rounded.Grass") {
            materialPath {
                moveTo(15.64f, 11.02f)
                curveToRelative(0.55f, -1.47f, 1.43f, -2.78f, 2.56f, -3.83f)
                curveToRelative(0.38f, -0.36f, 0.04f, -1.0f, -0.46f, -0.85f)
                curveToRelative(-3.32f, 0.98f, -5.75f, 4.05f, -5.74f, 7.69f)
                curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                curveTo(12.95f, 12.75f, 14.2f, 11.72f, 15.64f, 11.02f)
                close()
                moveTo(11.42f, 8.85f)
                curveToRelative(-0.6f, -1.56f, -1.63f, -2.91f, -2.96f, -3.87f)
                curveTo(8.04f, 4.68f, 7.5f, 5.17f, 7.74f, 5.63f)
                curveTo(8.54f, 7.15f, 9.0f, 8.88f, 9.0f, 10.71f)
                curveToRelative(0.0f, 0.21f, -0.03f, 0.41f, -0.04f, 0.61f)
                curveToRelative(0.43f, 0.24f, 0.83f, 0.52f, 1.22f, 0.82f)
                curveTo(10.39f, 10.96f, 10.83f, 9.85f, 11.42f, 8.85f)
                close()
                moveTo(12.0f, 20.0f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.75f)
                curveToRelative(-0.57f, -2.19f, -2.04f, -4.02f, -4.0f, -5.06f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.16f, -0.08f, -0.26f, -0.25f, -0.26f, -0.44f)
                curveToRelative(0.0f, -0.27f, 0.22f, -0.49f, 0.49f, -0.5f)
                curveToRelative(0.01f, 0.0f, 0.02f, 0.0f, 0.02f, 0.0f)
                curveTo(8.42f, 12.0f, 12.0f, 15.58f, 12.0f, 20.0f)
                close()
                moveTo(20.26f, 12.94f)
                lineTo(20.26f, 12.94f)
                curveToRelative(-1.96f, 1.04f, -3.44f, 2.87f, -4.0f, 5.06f)
                horizontalLineTo(21.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-5.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.68f, -0.07f, -1.35f, -0.2f, -2.0f)
                curveToRelative(-0.15f, -0.72f, -0.38f, -1.42f, -0.67f, -2.07f)
                curveTo(14.52f, 13.58f, 17.07f, 12.0f, 20.0f, 12.0f)
                curveToRelative(0.01f, 0.0f, 0.02f, 0.0f, 0.02f, 0.0f)
                curveToRelative(0.27f, 0.0f, 0.49f, 0.23f, 0.49f, 0.5f)
                curveTo(20.52f, 12.69f, 20.41f, 12.85f, 20.26f, 12.94f)
                close()
            }
        }
        return _grass!!
    }

private var _grass: ImageVector? = null

internal val Icons.Rounded.GridOn: ImageVector
    get() {
        if (_gridOn != null) {
            return _gridOn!!
        }
        _gridOn = materialIcon(name = "Rounded.GridOn") {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(8.0f, 20.0f)
                lineTo(5.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(8.0f, 14.0f)
                lineTo(4.0f, 14.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(8.0f, 8.0f)
                lineTo(4.0f, 8.0f)
                lineTo(4.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(14.0f, 20.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(14.0f, 14.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(14.0f, 8.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(20.0f, 14.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(20.0f, 8.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(16.0f, 4.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _gridOn!!
    }

private var _gridOn: ImageVector? = null

internal val Icons.Rounded.GridView: ImageVector
    get() {
        if (_gridView != null) {
            return _gridView!!
        }
        _gridView = materialIcon(name = "Rounded.GridView") {
            materialPath {
                moveTo(5.0f, 11.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(4.0f)
                curveTo(3.0f, 10.1f, 3.9f, 11.0f, 5.0f, 11.0f)
                close()
            }
            materialPath {
                moveTo(5.0f, 21.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveTo(3.0f, 20.1f, 3.9f, 21.0f, 5.0f, 21.0f)
                close()
            }
            materialPath {
                moveTo(13.0f, 5.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(13.9f, 3.0f, 13.0f, 3.9f, 13.0f, 5.0f)
                close()
            }
            materialPath {
                moveTo(15.0f, 21.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveTo(13.0f, 20.1f, 13.9f, 21.0f, 15.0f, 21.0f)
                close()
            }
        }
        return _gridView!!
    }

private var _gridView: ImageVector? = null

internal val Icons.Rounded.Group: ImageVector
    get() {
        if (_group != null) {
            return _group!!
        }
        _group = materialIcon(name = "Rounded.Group") {
            materialPath {
                moveTo(16.0f, 11.0f)
                curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                reflectiveCurveTo(17.66f, 5.0f, 16.0f, 5.0f)
                reflectiveCurveToRelative(-3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(8.0f, 11.0f)
                curveToRelative(1.66f, 0.0f, 2.99f, -1.34f, 2.99f, -3.0f)
                reflectiveCurveTo(9.66f, 5.0f, 8.0f, 5.0f)
                reflectiveCurveTo(5.0f, 6.34f, 5.0f, 8.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(8.0f, 13.0f)
                curveToRelative(-2.33f, 0.0f, -7.0f, 1.17f, -7.0f, 3.5f)
                lineTo(1.0f, 18.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.5f)
                curveToRelative(0.0f, -2.33f, -4.67f, -3.5f, -7.0f, -3.5f)
                close()
                moveTo(16.0f, 13.0f)
                curveToRelative(-0.29f, 0.0f, -0.62f, 0.02f, -0.97f, 0.05f)
                curveToRelative(0.02f, 0.01f, 0.03f, 0.03f, 0.04f, 0.04f)
                curveToRelative(1.14f, 0.83f, 1.93f, 1.94f, 1.93f, 3.41f)
                lineTo(17.0f, 18.0f)
                curveToRelative(0.0f, 0.35f, -0.07f, 0.69f, -0.18f, 1.0f)
                lineTo(22.0f, 19.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.5f)
                curveToRelative(0.0f, -2.33f, -4.67f, -3.5f, -7.0f, -3.5f)
                close()
            }
        }
        return _group!!
    }

private var _group: ImageVector? = null

internal val Icons.Rounded.Groups: ImageVector
    get() {
        if (_groups != null) {
            return _groups!!
        }
        _groups = materialIcon(name = "Rounded.Groups") {
            materialPath {
                moveTo(12.0f, 12.75f)
                curveToRelative(1.63f, 0.0f, 3.07f, 0.39f, 4.24f, 0.9f)
                curveToRelative(1.08f, 0.48f, 1.76f, 1.56f, 1.76f, 2.73f)
                lineTo(18.0f, 17.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineToRelative(0.0f, -0.61f)
                curveToRelative(0.0f, -1.18f, 0.68f, -2.26f, 1.76f, -2.73f)
                curveTo(8.93f, 13.14f, 10.37f, 12.75f, 12.0f, 12.75f)
                close()
                moveTo(4.0f, 13.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                curveTo(2.0f, 12.1f, 2.9f, 13.0f, 4.0f, 13.0f)
                close()
                moveTo(5.13f, 14.1f)
                curveTo(4.76f, 14.04f, 4.39f, 14.0f, 4.0f, 14.0f)
                curveToRelative(-0.99f, 0.0f, -1.93f, 0.21f, -2.78f, 0.58f)
                curveTo(0.48f, 14.9f, 0.0f, 15.62f, 0.0f, 16.43f)
                lineTo(0.0f, 17.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(3.5f, 0.0f)
                verticalLineToRelative(-1.61f)
                curveTo(4.5f, 15.56f, 4.73f, 14.78f, 5.13f, 14.1f)
                close()
                moveTo(20.0f, 13.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                curveTo(18.0f, 12.1f, 18.9f, 13.0f, 20.0f, 13.0f)
                close()
                moveTo(24.0f, 16.43f)
                curveToRelative(0.0f, -0.81f, -0.48f, -1.53f, -1.22f, -1.85f)
                curveTo(21.93f, 14.21f, 20.99f, 14.0f, 20.0f, 14.0f)
                curveToRelative(-0.39f, 0.0f, -0.76f, 0.04f, -1.13f, 0.1f)
                curveToRelative(0.4f, 0.68f, 0.63f, 1.46f, 0.63f, 2.29f)
                verticalLineTo(18.0f)
                lineToRelative(3.5f, 0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(24.0f, 16.43f)
                close()
                moveTo(12.0f, 6.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
                curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
                reflectiveCurveToRelative(-3.0f, -1.34f, -3.0f, -3.0f)
                curveTo(9.0f, 7.34f, 10.34f, 6.0f, 12.0f, 6.0f)
                close()
            }
        }
        return _groups!!
    }

private var _groups: ImageVector? = null

internal val Icons.Rounded.Handyman: ImageVector
    get() {
        if (_handyman != null) {
            return _handyman!!
        }
        _handyman = materialIcon(name = "Rounded.Handyman") {
            materialPath {
                moveTo(21.67f, 18.17f)
                lineToRelative(-4.72f, -4.72f)
                curveToRelative(-0.48f, -0.48f, -0.99f, -0.59f, -1.58f, -0.59f)
                lineToRelative(-2.54f, 2.54f)
                curveToRelative(0.0f, 0.59f, 0.11f, 1.11f, 0.59f, 1.58f)
                lineToRelative(4.72f, 4.72f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(2.12f, -2.12f)
                curveTo(22.06f, 19.2f, 22.06f, 18.56f, 21.67f, 18.17f)
                close()
            }
            materialPath {
                moveTo(16.63f, 9.49f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.71f, -0.71f)
                lineToRelative(2.12f, 2.12f)
                curveToRelative(1.17f, -1.17f, 1.17f, -3.07f, 0.0f, -4.24f)
                lineToRelative(-2.83f, -2.83f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.71f, 0.71f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.62f, -0.76f, -0.95f, -1.21f, -0.5f)
                lineToRelative(-2.54f, 2.54f)
                curveToRelative(-0.45f, 0.45f, -0.12f, 1.21f, 0.5f, 1.21f)
                horizontalLineToRelative(2.54f)
                lineToRelative(-0.71f, 0.71f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.35f, 0.35f)
                lineToRelative(-2.89f, 2.89f)
                lineTo(7.85f, 6.48f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.27f, -0.11f, -0.52f, -0.29f, -0.71f)
                lineTo(5.54f, 2.74f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(2.71f, 4.16f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(4.73f, 7.6f)
                curveToRelative(0.19f, 0.19f, 0.44f, 0.29f, 0.71f, 0.29f)
                horizontalLineToRelative(1.0f)
                lineToRelative(4.13f, 4.13f)
                lineToRelative(-0.85f, 0.85f)
                horizontalLineTo(8.42f)
                curveToRelative(-0.53f, 0.0f, -1.04f, 0.21f, -1.41f, 0.59f)
                lineToRelative(-4.72f, 4.72f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(2.12f, 2.12f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.72f, -4.72f)
                curveToRelative(0.38f, -0.38f, 0.59f, -0.88f, 0.59f, -1.41f)
                verticalLineToRelative(-1.29f)
                lineToRelative(5.15f, -5.15f)
                lineTo(16.63f, 9.49f)
                close()
            }
        }
        return _handyman!!
    }

private var _handyman: ImageVector? = null

internal val Icons.Rounded.Headphones: ImageVector
    get() {
        if (_headphones != null) {
            return _headphones!!
        }
        _headphones = materialIcon(name = "Rounded.Headphones") {
            materialPath {
                moveTo(3.0f, 12.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -3.87f, 3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.0f)
                curveToRelative(0.0f, -4.97f, -4.03f, -9.0f, -9.0f, -9.0f)
                reflectiveCurveTo(3.0f, 7.03f, 3.0f, 12.0f)
                close()
            }
        }
        return _headphones!!
    }

private var _headphones: ImageVector? = null

internal val Icons.Rounded.Hexagon: ImageVector
    get() {
        if (_hexagon != null) {
            return _hexagon!!
        }
        _hexagon = materialIcon(name = "Rounded.Hexagon") {
            materialPath {
                moveTo(16.05f, 3.0f)
                horizontalLineTo(7.95f)
                curveTo(7.24f, 3.0f, 6.58f, 3.38f, 6.22f, 4.0f)
                lineToRelative(-4.04f, 7.0f)
                curveToRelative(-0.36f, 0.62f, -0.36f, 1.38f, 0.0f, 2.0f)
                lineToRelative(4.04f, 7.0f)
                curveToRelative(0.36f, 0.62f, 1.02f, 1.0f, 1.73f, 1.0f)
                horizontalLineToRelative(8.09f)
                curveToRelative(0.71f, 0.0f, 1.37f, -0.38f, 1.73f, -1.0f)
                lineToRelative(4.04f, -7.0f)
                curveToRelative(0.36f, -0.62f, 0.36f, -1.38f, 0.0f, -2.0f)
                lineToRelative(-4.04f, -7.0f)
                curveTo(17.42f, 3.38f, 16.76f, 3.0f, 16.05f, 3.0f)
                close()
            }
        }
        return _hexagon!!
    }

private var _hexagon: ImageVector? = null

internal val Icons.Rounded.History: ImageVector
    get() {
        if (_history != null) {
            return _history!!
        }
        _history = materialIcon(name = "Rounded.History") {
            materialPath {
                moveTo(13.26f, 3.0f)
                curveTo(8.17f, 2.86f, 4.0f, 6.95f, 4.0f, 12.0f)
                lineTo(2.21f, 12.0f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.35f, 0.85f)
                lineToRelative(2.79f, 2.8f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(2.79f, -2.8f)
                curveToRelative(0.31f, -0.31f, 0.09f, -0.85f, -0.36f, -0.85f)
                lineTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.9f, 3.18f, -7.05f, 7.1f, -7.0f)
                curveToRelative(3.72f, 0.05f, 6.85f, 3.18f, 6.9f, 6.9f)
                curveToRelative(0.05f, 3.91f, -3.1f, 7.1f, -7.0f, 7.1f)
                curveToRelative(-1.61f, 0.0f, -3.1f, -0.55f, -4.28f, -1.48f)
                curveToRelative(-0.4f, -0.31f, -0.96f, -0.28f, -1.32f, 0.08f)
                curveToRelative(-0.42f, 0.42f, -0.39f, 1.13f, 0.08f, 1.49f)
                curveTo(9.0f, 20.29f, 10.91f, 21.0f, 13.0f, 21.0f)
                curveToRelative(5.05f, 0.0f, 9.14f, -4.17f, 9.0f, -9.26f)
                curveToRelative(-0.13f, -4.69f, -4.05f, -8.61f, -8.74f, -8.74f)
                close()
                moveTo(12.75f, 8.0f)
                curveToRelative(-0.41f, 0.0f, -0.75f, 0.34f, -0.75f, 0.75f)
                verticalLineToRelative(3.68f)
                curveToRelative(0.0f, 0.35f, 0.19f, 0.68f, 0.49f, 0.86f)
                lineToRelative(3.12f, 1.85f)
                curveToRelative(0.36f, 0.21f, 0.82f, 0.09f, 1.03f, -0.26f)
                curveToRelative(0.21f, -0.36f, 0.09f, -0.82f, -0.26f, -1.03f)
                lineToRelative(-2.88f, -1.71f)
                verticalLineToRelative(-3.4f)
                curveToRelative(0.0f, -0.4f, -0.34f, -0.74f, -0.75f, -0.74f)
                close()
            }
        }
        return _history!!
    }

private var _history: ImageVector? = null

internal val Icons.Rounded.Hotel: ImageVector
    get() {
        if (_hotel != null) {
            return _hotel!!
        }
        _hotel = materialIcon(name = "Rounded.Hotel") {
            materialPath {
                moveTo(7.0f, 13.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveTo(8.66f, 7.0f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(19.0f, 7.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                lineTo(3.0f, 14.0f)
                lineTo(3.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(13.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                close()
            }
        }
        return _hotel!!
    }

private var _hotel: ImageVector? = null

internal val Icons.Rounded.HourglassEmpty: ImageVector
    get() {
        if (_hourglassEmpty != null) {
            return _hourglassEmpty!!
        }
        _hourglassEmpty = materialIcon(name = "Rounded.HourglassEmpty") {
            materialPath {
                moveTo(8.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.17f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.42f)
                lineTo(10.0f, 12.0f)
                lineToRelative(-3.42f, 3.42f)
                curveToRelative(-0.37f, 0.38f, -0.58f, 0.89f, -0.58f, 1.42f)
                lineTo(6.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-3.16f)
                curveToRelative(0.0f, -0.53f, -0.21f, -1.04f, -0.58f, -1.41f)
                lineTo(14.0f, 12.0f)
                lineToRelative(3.41f, -3.4f)
                curveToRelative(0.38f, -0.38f, 0.59f, -0.89f, 0.59f, -1.42f)
                lineTo(18.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(8.0f, 2.0f)
                close()
                moveTo(16.0f, 16.5f)
                lineTo(16.0f, 19.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(9.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.5f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(4.0f, 4.0f)
                close()
                moveTo(12.0f, 11.5f)
                lineToRelative(-4.0f, -4.0f)
                lineTo(8.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.5f)
                lineToRelative(-4.0f, 4.0f)
                close()
            }
        }
        return _hourglassEmpty!!
    }

private var _hourglassEmpty: ImageVector? = null

internal val Icons.Rounded.HowToReg: ImageVector
    get() {
        if (_howToReg != null) {
            return _howToReg!!
        }
        _howToReg = materialIcon(name = "Rounded.HowToReg") {
            materialPath {
                moveTo(12.0f, 20.0f)
                lineToRelative(-0.86f, -0.86f)
                curveToRelative(-1.18f, -1.18f, -1.17f, -3.1f, 0.02f, -4.26f)
                lineToRelative(0.84f, -0.82f)
                curveToRelative(-0.39f, -0.04f, -0.68f, -0.06f, -1.0f, -0.06f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(9.0f)
                close()
                moveTo(11.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
            }
            materialPath {
                moveTo(16.18f, 19.78f)
                curveToRelative(-0.39f, 0.39f, -1.03f, 0.39f, -1.42f, 0.0f)
                lineToRelative(-2.07f, -2.09f)
                curveToRelative(-0.38f, -0.39f, -0.38f, -1.01f, 0.0f, -1.39f)
                lineToRelative(0.01f, -0.01f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.4f, 0.0f)
                lineToRelative(1.37f, 1.37f)
                lineToRelative(4.43f, -4.46f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(0.01f, 0.01f)
                curveToRelative(0.38f, 0.39f, 0.38f, 1.01f, 0.0f, 1.39f)
                lineToRelative(-5.14f, 5.18f)
                close()
            }
        }
        return _howToReg!!
    }

private var _howToReg: ImageVector? = null

internal val Icons.Rounded.Hub: ImageVector
    get() {
        if (_hub != null) {
            return _hub!!
        }
        _hub = materialIcon(name = "Rounded.Hub") {
            materialPath {
                moveTo(8.4f, 18.2f)
                curveTo(8.78f, 18.7f, 9.0f, 19.32f, 9.0f, 20.0f)
                curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
                reflectiveCurveToRelative(-3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                curveToRelative(0.44f, 0.0f, 0.85f, 0.09f, 1.23f, 0.26f)
                lineToRelative(1.41f, -1.77f)
                curveToRelative(-0.92f, -1.03f, -1.29f, -2.39f, -1.09f, -3.69f)
                lineToRelative(-2.03f, -0.68f)
                curveTo(4.98f, 11.95f, 4.06f, 12.5f, 3.0f, 12.5f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                curveToRelative(0.0f, 0.07f, 0.0f, 0.14f, -0.01f, 0.21f)
                lineToRelative(2.03f, 0.68f)
                curveToRelative(0.64f, -1.21f, 1.82f, -2.09f, 3.22f, -2.32f)
                lineToRelative(0.0f, -2.16f)
                curveTo(9.96f, 5.57f, 9.0f, 4.4f, 9.0f, 3.0f)
                curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                curveToRelative(0.0f, 1.4f, -0.96f, 2.57f, -2.25f, 2.91f)
                verticalLineToRelative(2.16f)
                curveToRelative(1.4f, 0.23f, 2.58f, 1.11f, 3.22f, 2.32f)
                lineToRelative(2.03f, -0.68f)
                curveTo(18.0f, 9.64f, 18.0f, 9.57f, 18.0f, 9.5f)
                curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                curveToRelative(-1.06f, 0.0f, -1.98f, -0.55f, -2.52f, -1.37f)
                lineToRelative(-2.03f, 0.68f)
                curveToRelative(0.2f, 1.29f, -0.16f, 2.65f, -1.09f, 3.69f)
                lineToRelative(1.41f, 1.77f)
                curveTo(17.15f, 17.09f, 17.56f, 17.0f, 18.0f, 17.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                reflectiveCurveToRelative(-3.0f, -1.34f, -3.0f, -3.0f)
                curveToRelative(0.0f, -0.68f, 0.22f, -1.3f, 0.6f, -1.8f)
                lineToRelative(-1.41f, -1.77f)
                curveToRelative(-1.35f, 0.75f, -3.01f, 0.76f, -4.37f, 0.0f)
                lineTo(8.4f, 18.2f)
                close()
            }
        }
        return _hub!!
    }

private var _hub: ImageVector? = null

internal val Icons.Rounded.Icecream: ImageVector
    get() {
        if (_icecream != null) {
            return _icecream!!
        }
        _icecream = materialIcon(name = "Rounded.Icecream") {
            materialPath {
                moveTo(18.38f, 6.24f)
                curveTo(17.79f, 3.24f, 15.14f, 1.0f, 12.0f, 1.0f)
                reflectiveCurveTo(6.21f, 3.24f, 5.62f, 6.24f)
                curveTo(4.08f, 6.81f, 3.0f, 8.29f, 3.0f, 10.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                curveToRelative(0.12f, 0.0f, 0.23f, -0.02f, 0.34f, -0.02f)
                lineToRelative(3.83f, 7.31f)
                curveToRelative(0.38f, 0.72f, 1.41f, 0.71f, 1.78f, -0.01f)
                lineToRelative(3.73f, -7.31f)
                curveTo(16.79f, 13.98f, 16.89f, 14.0f, 17.0f, 14.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                curveTo(21.0f, 8.29f, 19.92f, 6.81f, 18.38f, 6.24f)
                close()
                moveTo(12.05f, 18.63f)
                lineToRelative(-2.73f, -5.21f)
                curveTo(10.15f, 13.79f, 11.06f, 14.0f, 12.0f, 14.0f)
                curveToRelative(0.95f, 0.0f, 1.88f, -0.21f, 2.72f, -0.6f)
                lineTo(12.05f, 18.63f)
                close()
            }
        }
        return _icecream!!
    }

private var _icecream: ImageVector? = null

internal val Icons.Rounded.Image: ImageVector
    get() {
        if (_image != null) {
            return _image!!
        }
        _image = materialIcon(name = "Rounded.Image") {
            materialPath {
                moveTo(21.0f, 19.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                close()
                moveTo(8.9f, 13.98f)
                lineToRelative(2.1f, 2.53f)
                lineToRelative(3.1f, -3.99f)
                curveToRelative(0.2f, -0.26f, 0.6f, -0.26f, 0.8f, 0.01f)
                lineToRelative(3.51f, 4.68f)
                curveToRelative(0.25f, 0.33f, 0.01f, 0.8f, -0.4f, 0.8f)
                horizontalLineTo(6.02f)
                curveToRelative(-0.42f, 0.0f, -0.65f, -0.48f, -0.39f, -0.81f)
                lineTo(8.12f, 14.0f)
                curveToRelative(0.19f, -0.26f, 0.57f, -0.27f, 0.78f, -0.02f)
                close()
            }
        }
        return _image!!
    }

private var _image: ImageVector? = null

internal val Icons.Rounded.Inbox: ImageVector
    get() {
        if (_inbox != null) {
            return _inbox!!
        }
        _inbox = materialIcon(name = "Rounded.Inbox") {
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
                moveTo(19.0f, 5.0f)
                verticalLineToRelative(9.0f)
                horizontalLineToRelative(-3.56f)
                curveToRelative(-0.36f, 0.0f, -0.68f, 0.19f, -0.86f, 0.5f)
                curveTo(14.06f, 15.4f, 13.11f, 16.0f, 12.0f, 16.0f)
                reflectiveCurveToRelative(-2.06f, -0.6f, -2.58f, -1.5f)
                curveTo(9.24f, 14.19f, 8.91f, 14.0f, 8.56f, 14.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(19.0f)
                close()
            }
        }
        return _inbox!!
    }

private var _inbox: ImageVector? = null

internal val Icons.Rounded.Inventory: ImageVector
    get() {
        if (_inventory != null) {
            return _inventory!!
        }
        _inventory = materialIcon(name = "Rounded.Inventory") {
            materialPath {
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 1.84f, 13.3f, 1.0f, 12.0f, 1.0f)
                reflectiveCurveTo(9.6f, 1.84f, 9.18f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                close()
                moveTo(12.0f, 3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveTo(11.45f, 3.0f, 12.0f, 3.0f)
                close()
            }
            materialPath {
                moveTo(21.75f, 12.25f)
                curveToRelative(-0.41f, -0.41f, -1.09f, -0.41f, -1.5f, 0.0f)
                lineTo(15.51f, 17.0f)
                lineToRelative(-2.26f, -2.25f)
                curveToRelative(-0.41f, -0.41f, -1.08f, -0.41f, -1.5f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.41f, 0.41f, -0.41f, 1.09f, 0.0f, 1.5f)
                lineToRelative(3.05f, 3.04f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(5.53f, -5.54f)
                curveTo(22.16f, 13.34f, 22.16f, 12.66f, 21.75f, 12.25f)
                close()
            }
        }
        return _inventory!!
    }

private var _inventory: ImageVector? = null

internal val Icons.Rounded.Inventory2: ImageVector
    get() {
        if (_inventory2 != null) {
            return _inventory2!!
        }
        _inventory2 = materialIcon(name = "Rounded.Inventory2") {
            materialPath {
                moveTo(20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(3.0f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                verticalLineToRelative(3.01f)
                curveTo(2.0f, 7.73f, 2.43f, 8.35f, 3.0f, 8.7f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 1.1f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.9f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.7f)
                curveToRelative(0.57f, -0.35f, 1.0f, -0.97f, 1.0f, -1.69f)
                verticalLineTo(4.0f)
                curveTo(22.0f, 2.9f, 21.0f, 2.0f, 20.0f, 2.0f)
                close()
                moveTo(14.0f, 14.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveTo(15.0f, 13.55f, 14.55f, 14.0f, 14.0f, 14.0f)
                close()
                moveTo(20.0f, 7.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(7.0f)
                close()
            }
        }
        return _inventory2!!
    }

private var _inventory2: ImageVector? = null

internal val Icons.Rounded.KebabDining: ImageVector
    get() {
        if (_kebabDining != null) {
            return _kebabDining!!
        }
        _kebabDining = materialIcon(name = "Rounded.KebabDining") {
            materialPath {
                moveTo(7.75f, 13.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(8.5f)
                curveToRelative(1.38f, 0.0f, 2.5f, 1.12f, 2.5f, 2.5f)
                curveToRelative(0.0f, 1.38f, -1.12f, 2.5f, -2.5f, 2.5f)
                horizontalLineTo(7.75f)
                verticalLineToRelative(3.25f)
                curveTo(7.75f, 22.66f, 7.41f, 23.0f, 7.0f, 23.0f)
                reflectiveCurveToRelative(-0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineTo(19.0f)
                horizontalLineTo(5.5f)
                curveTo(4.12f, 19.0f, 3.0f, 17.88f, 3.0f, 16.5f)
                curveTo(3.0f, 15.12f, 4.12f, 14.0f, 5.5f, 14.0f)
                horizontalLineToRelative(0.75f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.25f)
                verticalLineTo(7.0f)
                horizontalLineTo(5.5f)
                curveTo(4.12f, 7.0f, 3.0f, 5.88f, 3.0f, 4.5f)
                curveTo(3.0f, 3.12f, 4.12f, 2.0f, 5.5f, 2.0f)
                horizontalLineToRelative(0.75f)
                verticalLineTo(1.75f)
                curveTo(6.25f, 1.34f, 6.59f, 1.0f, 7.0f, 1.0f)
                reflectiveCurveToRelative(0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineTo(2.0f)
                horizontalLineTo(8.5f)
                curveTo(9.88f, 2.0f, 11.0f, 3.12f, 11.0f, 4.5f)
                curveTo(11.0f, 5.88f, 9.88f, 7.0f, 8.5f, 7.0f)
                horizontalLineTo(7.75f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(7.75f)
                close()
                moveTo(17.75f, 13.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(0.75f)
                curveToRelative(1.38f, 0.0f, 2.5f, 1.12f, 2.5f, 2.5f)
                curveToRelative(0.0f, 1.38f, -1.12f, 2.5f, -2.5f, 2.5f)
                horizontalLineToRelative(-0.75f)
                verticalLineToRelative(3.25f)
                curveToRelative(0.0f, 0.41f, -0.34f, 0.75f, -0.75f, 0.75f)
                reflectiveCurveToRelative(-0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineTo(19.0f)
                horizontalLineTo(15.5f)
                curveToRelative(-1.38f, 0.0f, -2.5f, -1.12f, -2.5f, -2.5f)
                curveToRelative(0.0f, -1.38f, 1.12f, -2.5f, 2.5f, -2.5f)
                horizontalLineToRelative(0.75f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.25f)
                verticalLineTo(7.0f)
                horizontalLineTo(15.5f)
                curveTo(14.12f, 7.0f, 13.0f, 5.88f, 13.0f, 4.5f)
                curveTo(13.0f, 3.12f, 14.12f, 2.0f, 15.5f, 2.0f)
                horizontalLineToRelative(0.75f)
                verticalLineTo(1.75f)
                curveTo(16.25f, 1.34f, 16.59f, 1.0f, 17.0f, 1.0f)
                reflectiveCurveToRelative(0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(0.75f)
                curveTo(19.88f, 2.0f, 21.0f, 3.12f, 21.0f, 4.5f)
                curveTo(21.0f, 5.88f, 19.88f, 7.0f, 18.5f, 7.0f)
                horizontalLineToRelative(-0.75f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(20.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(17.75f)
                close()
            }
        }
        return _kebabDining!!
    }

private var _kebabDining: ImageVector? = null

internal val Icons.Rounded.Key: ImageVector
    get() {
        if (_key != null) {
            return _key!!
        }
        _key = materialIcon(name = "Rounded.Key") {
            materialPath {
                moveTo(20.59f, 10.0f)
                horizontalLineToRelative(-7.94f)
                curveTo(11.7f, 7.31f, 8.89f, 5.5f, 5.77f, 6.12f)
                curveToRelative(-2.29f, 0.46f, -4.15f, 2.3f, -4.63f, 4.58f)
                curveTo(0.32f, 14.58f, 3.26f, 18.0f, 7.0f, 18.0f)
                curveToRelative(2.61f, 0.0f, 4.83f, -1.67f, 5.65f, -4.0f)
                horizontalLineTo(13.0f)
                lineToRelative(1.29f, 1.29f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(17.0f, 14.0f)
                lineToRelative(1.29f, 1.29f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.42f, 0.0f)
                lineToRelative(2.59f, -2.61f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, -0.01f, -1.42f)
                lineToRelative(-0.99f, -0.97f)
                curveTo(21.1f, 10.1f, 20.85f, 10.0f, 20.59f, 10.0f)
                close()
                moveTo(7.0f, 15.0f)
                curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.35f, 3.0f, 3.0f)
                curveTo(10.0f, 13.65f, 8.65f, 15.0f, 7.0f, 15.0f)
                close()
            }
        }
        return _key!!
    }

private var _key: ImageVector? = null

internal val Icons.Rounded.Keyboard: ImageVector
    get() {
        if (_keyboard != null) {
            return _keyboard!!
        }
        _keyboard = materialIcon(name = "Rounded.Keyboard") {
            materialPath {
                moveTo(20.0f, 5.0f)
                lineTo(4.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 17.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(11.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 8.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(8.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 10.0f)
                lineTo(8.0f, 8.0f)
                close()
                moveTo(8.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(8.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(7.0f, 13.0f)
                lineTo(5.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 10.0f)
                lineTo(5.0f, 10.0f)
                lineTo(5.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(15.0f, 17.0f)
                lineTo(9.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(16.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 10.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(14.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(19.0f, 10.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(17.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _keyboard!!
    }

private var _keyboard: ImageVector? = null

internal val Icons.Rounded.KeyboardDoubleArrowLeft: ImageVector
    get() {
        if (_keyboardDoubleArrowLeft != null) {
            return _keyboardDoubleArrowLeft!!
        }
        _keyboardDoubleArrowLeft = materialIcon(name = "Rounded.KeyboardDoubleArrowLeft") {
            materialPath {
                moveTo(18.29f, 17.29f)
                lineTo(18.29f, 17.29f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(14.42f, 12.0f)
                lineToRelative(3.88f, -3.88f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-4.59f, 4.59f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.59f, 4.59f)
                curveTo(17.27f, 17.68f, 17.9f, 17.68f, 18.29f, 17.29f)
                close()
            }
            materialPath {
                moveTo(11.7f, 17.29f)
                lineTo(11.7f, 17.29f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(7.83f, 12.0f)
                lineToRelative(3.88f, -3.88f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-4.59f, 4.59f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.59f, 4.59f)
                curveTo(10.68f, 17.68f, 11.31f, 17.68f, 11.7f, 17.29f)
                close()
            }
        }
        return _keyboardDoubleArrowLeft!!
    }

private var _keyboardDoubleArrowLeft: ImageVector? = null

internal val Icons.Rounded.KeyboardDoubleArrowRight: ImageVector
    get() {
        if (_keyboardDoubleArrowRight != null) {
            return _keyboardDoubleArrowRight!!
        }
        _keyboardDoubleArrowRight = materialIcon(name = "Rounded.KeyboardDoubleArrowRight") {
            materialPath {
                moveTo(5.7f, 6.71f)
                lineTo(5.7f, 6.71f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(9.58f, 12.0f)
                lineTo(5.7f, 15.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.59f, -4.59f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(7.12f, 6.71f)
                curveTo(6.73f, 6.32f, 6.09f, 6.32f, 5.7f, 6.71f)
                close()
            }
            materialPath {
                moveTo(12.29f, 6.71f)
                lineTo(12.29f, 6.71f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(16.17f, 12.0f)
                lineToRelative(-3.88f, 3.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.59f, -4.59f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-4.59f, -4.59f)
                curveTo(13.32f, 6.32f, 12.68f, 6.32f, 12.29f, 6.71f)
                close()
            }
        }
        return _keyboardDoubleArrowRight!!
    }

private var _keyboardDoubleArrowRight: ImageVector? = null

internal val Icons.Rounded.KingBed: ImageVector
    get() {
        if (_kingBed != null) {
            return _kingBed!!
        }
        _kingBed = materialIcon(name = "Rounded.KingBed") {
            materialPath {
                moveTo(20.0f, 10.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 5.0f, 4.0f, 5.9f, 4.0f, 7.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(1.33f)
                lineToRelative(0.51f, 1.53f)
                curveTo(3.94f, 18.81f, 4.2f, 19.0f, 4.5f, 19.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.3f, 0.0f, 0.56f, -0.19f, 0.66f, -0.47f)
                lineTo(5.67f, 17.0f)
                horizontalLineToRelative(12.67f)
                lineToRelative(0.51f, 1.53f)
                curveTo(18.94f, 18.81f, 19.2f, 19.0f, 19.5f, 19.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.3f, 0.0f, 0.56f, -0.19f, 0.66f, -0.47f)
                lineTo(20.67f, 17.0f)
                horizontalLineTo(22.0f)
                verticalLineToRelative(-5.0f)
                curveTo(22.0f, 10.9f, 21.1f, 10.0f, 20.0f, 10.0f)
                close()
                moveTo(11.0f, 10.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(18.0f, 10.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineTo(10.0f)
                close()
            }
        }
        return _kingBed!!
    }

private var _kingBed: ImageVector? = null

internal val Icons.Rounded.Kitchen: ImageVector
    get() {
        if (_kitchen != null) {
            return _kitchen!!
        }
        _kitchen = materialIcon(name = "Rounded.Kitchen") {
            materialPath {
                moveTo(18.0f, 2.01f)
                lineTo(6.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.89f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 4.0f)
                curveToRelative(0.0f, -1.11f, -0.9f, -1.99f, -2.0f, -1.99f)
                close()
                moveTo(17.0f, 20.0f)
                lineTo(7.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-7.02f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                lineTo(18.0f, 19.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(17.0f, 9.0f)
                lineTo(7.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(6.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(9.0f, 5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(8.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(9.0f, 12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                close()
            }
        }
        return _kitchen!!
    }

private var _kitchen: ImageVector? = null

internal val Icons.Rounded.Landscape: ImageVector
    get() {
        if (_landscape != null) {
            return _landscape!!
        }
        _landscape = materialIcon(name = "Rounded.Landscape") {
            materialPath {
                moveTo(13.2f, 7.07f)
                lineTo(10.25f, 11.0f)
                lineToRelative(2.25f, 3.0f)
                curveToRelative(0.33f, 0.44f, 0.24f, 1.07f, -0.2f, 1.4f)
                curveToRelative(-0.44f, 0.33f, -1.07f, 0.25f, -1.4f, -0.2f)
                curveToRelative(-1.05f, -1.4f, -2.31f, -3.07f, -3.1f, -4.14f)
                curveToRelative(-0.4f, -0.53f, -1.2f, -0.53f, -1.6f, 0.0f)
                lineToRelative(-4.0f, 5.33f)
                curveToRelative(-0.49f, 0.67f, -0.02f, 1.61f, 0.8f, 1.61f)
                horizontalLineToRelative(18.0f)
                curveToRelative(0.82f, 0.0f, 1.29f, -0.94f, 0.8f, -1.6f)
                lineToRelative(-7.0f, -9.33f)
                curveToRelative(-0.4f, -0.54f, -1.2f, -0.54f, -1.6f, 0.0f)
                close()
            }
        }
        return _landscape!!
    }

private var _landscape: ImageVector? = null

internal val Icons.Rounded.Laptop: ImageVector
    get() {
        if (_laptop != null) {
            return _laptop!!
        }
        _laptop = materialIcon(name = "Rounded.Laptop") {
            materialPath {
                moveTo(20.0f, 18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineTo(1.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(22.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(5.0f, 6.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                close()
            }
        }
        return _laptop!!
    }

private var _laptop: ImageVector? = null

internal val Icons.Rounded.Layers: ImageVector
    get() {
        if (_layers != null) {
            return _layers!!
        }
        _layers = materialIcon(name = "Rounded.Layers") {
            materialPath {
                moveTo(12.6f, 18.06f)
                curveToRelative(-0.36f, 0.28f, -0.87f, 0.28f, -1.23f, 0.0f)
                lineToRelative(-6.15f, -4.78f)
                curveToRelative(-0.36f, -0.28f, -0.86f, -0.28f, -1.22f, 0.0f)
                curveToRelative(-0.51f, 0.4f, -0.51f, 1.17f, 0.0f, 1.57f)
                lineToRelative(6.76f, 5.26f)
                curveToRelative(0.72f, 0.56f, 1.73f, 0.56f, 2.46f, 0.0f)
                lineToRelative(6.76f, -5.26f)
                curveToRelative(0.51f, -0.4f, 0.51f, -1.17f, 0.0f, -1.57f)
                lineToRelative(-0.01f, -0.01f)
                curveToRelative(-0.36f, -0.28f, -0.86f, -0.28f, -1.22f, 0.0f)
                lineToRelative(-6.15f, 4.79f)
                close()
                moveTo(13.23f, 15.04f)
                lineToRelative(6.76f, -5.26f)
                curveToRelative(0.51f, -0.4f, 0.51f, -1.18f, 0.0f, -1.58f)
                lineToRelative(-6.76f, -5.26f)
                curveToRelative(-0.72f, -0.56f, -1.73f, -0.56f, -2.46f, 0.0f)
                lineTo(4.01f, 8.21f)
                curveToRelative(-0.51f, 0.4f, -0.51f, 1.18f, 0.0f, 1.58f)
                lineToRelative(6.76f, 5.26f)
                curveToRelative(0.72f, 0.56f, 1.74f, 0.56f, 2.46f, -0.01f)
                close()
            }
        }
        return _layers!!
    }

private var _layers: ImageVector? = null

internal val Icons.Rounded.Light: ImageVector
    get() {
        if (_light != null) {
            return _light!!
        }
        _light = materialIcon(name = "Rounded.Light") {
            materialPath {
                moveTo(13.0f, 6.06f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.06f)
                curveToRelative(-4.5f, 0.5f, -8.0f, 4.31f, -8.0f, 8.93f)
                curveTo(3.0f, 16.1f, 3.9f, 17.0f, 5.01f, 17.0f)
                lineTo(8.0f, 17.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                lineToRelative(2.99f, 0.0f)
                curveTo(20.1f, 17.0f, 21.0f, 16.1f, 21.0f, 14.99f)
                curveTo(21.0f, 10.37f, 17.5f, 6.56f, 13.0f, 6.06f)
                close()
                moveTo(12.0f, 15.0f)
                lineToRelative(-7.0f, 0.0f)
                curveToRelative(0.0f, -3.86f, 3.14f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.14f, 7.0f, 7.0f)
                lineTo(12.0f, 15.0f)
                close()
            }
        }
        return _light!!
    }

private var _light: ImageVector? = null

internal val Icons.Rounded.Lightbulb: ImageVector
    get() {
        if (_lightbulb != null) {
            return _lightbulb!!
        }
        _lightbulb = materialIcon(name = "Rounded.Lightbulb") {
            materialPath {
                moveTo(12.0f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(10.0f, 21.1f, 10.9f, 22.0f, 12.0f, 22.0f)
                close()
            }
            materialPath {
                moveTo(9.0f, 19.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveTo(8.0f, 18.55f, 8.45f, 19.0f, 9.0f, 19.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(7.86f, 2.0f, 4.5f, 5.36f, 4.5f, 9.5f)
                curveToRelative(0.0f, 3.82f, 2.66f, 5.86f, 3.77f, 6.5f)
                horizontalLineToRelative(7.46f)
                curveToRelative(1.11f, -0.64f, 3.77f, -2.68f, 3.77f, -6.5f)
                curveTo(19.5f, 5.36f, 16.14f, 2.0f, 12.0f, 2.0f)
                close()
            }
        }
        return _lightbulb!!
    }

private var _lightbulb: ImageVector? = null

internal val Icons.Rounded.Link: ImageVector
    get() {
        if (_link != null) {
            return _link!!
        }
        _link = materialIcon(name = "Rounded.Link") {
            materialPath {
                moveTo(17.0f, 7.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.65f, 0.0f, 3.0f, 1.35f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.35f, 3.0f, -3.0f, 3.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveTo(19.76f, 7.0f, 17.0f, 7.0f)
                close()
                moveTo(8.0f, 12.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(9.0f)
                curveTo(8.45f, 11.0f, 8.0f, 11.45f, 8.0f, 12.0f)
                close()
                moveTo(10.0f, 15.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.35f, -3.0f, 3.0f, -3.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveTo(11.0f, 15.45f, 10.55f, 15.0f, 10.0f, 15.0f)
                close()
            }
        }
        return _link!!
    }

private var _link: ImageVector? = null

internal val Icons.Rounded.LocalBar: ImageVector
    get() {
        if (_localBar != null) {
            return _localBar!!
        }
        _localBar = materialIcon(name = "Rounded.LocalBar") {
            materialPath {
                moveTo(21.0f, 4.45f)
                curveToRelative(0.0f, -0.8f, -0.65f, -1.45f, -1.45f, -1.45f)
                horizontalLineTo(4.45f)
                curveTo(3.65f, 3.0f, 3.0f, 3.65f, 3.0f, 4.45f)
                curveToRelative(0.0f, 0.35f, 0.13f, 0.7f, 0.37f, 0.96f)
                lineTo(11.0f, 14.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-5.0f)
                lineToRelative(7.63f, -8.59f)
                curveToRelative(0.24f, -0.26f, 0.37f, -0.61f, 0.37f, -0.96f)
                close()
                moveTo(7.43f, 7.0f)
                lineTo(5.66f, 5.0f)
                horizontalLineToRelative(12.69f)
                lineToRelative(-1.78f, 2.0f)
                horizontalLineTo(7.43f)
                close()
            }
        }
        return _localBar!!
    }

private var _localBar: ImageVector? = null

internal val Icons.Rounded.LocalDrink: ImageVector
    get() {
        if (_localDrink != null) {
            return _localDrink!!
        }
        _localDrink = materialIcon(name = "Rounded.LocalDrink") {
            materialPath {
                moveTo(5.23f, 2.0f)
                curveTo(4.04f, 2.0f, 3.11f, 3.04f, 3.24f, 4.22f)
                lineToRelative(1.77f, 16.01f)
                curveTo(5.13f, 21.23f, 5.97f, 22.0f, 7.0f, 22.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.03f, 0.0f, 1.87f, -0.77f, 1.99f, -1.77f)
                lineToRelative(1.77f, -16.01f)
                curveToRelative(0.13f, -1.18f, -0.8f, -2.22f, -1.99f, -2.22f)
                lineTo(5.23f, 2.0f)
                close()
                moveTo(12.0f, 19.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.55f, 1.81f, -3.95f, 2.62f, -4.94f)
                curveToRelative(0.2f, -0.25f, 0.57f, -0.25f, 0.77f, 0.0f)
                curveToRelative(0.81f, 1.0f, 2.62f, 3.39f, 2.62f, 4.94f)
                curveToRelative(-0.01f, 1.66f, -1.35f, 3.0f, -3.01f, 3.0f)
                close()
                moveTo(18.33f, 8.0f)
                lineTo(5.67f, 8.0f)
                lineToRelative(-0.32f, -2.89f)
                curveToRelative(-0.06f, -0.59f, 0.4f, -1.11f, 1.0f, -1.11f)
                horizontalLineToRelative(11.3f)
                curveToRelative(0.59f, 0.0f, 1.06f, 0.52f, 0.99f, 1.11f)
                lineTo(18.33f, 8.0f)
                close()
            }
        }
        return _localDrink!!
    }

private var _localDrink: ImageVector? = null

internal val Icons.Rounded.LocalFireDepartment: ImageVector
    get() {
        if (_localFireDepartment != null) {
            return _localFireDepartment!!
        }
        _localFireDepartment = materialIcon(name = "Rounded.LocalFireDepartment") {
            materialPath {
                moveTo(12.0f, 12.9f)
                lineToRelative(-2.03f, 2.0f)
                curveToRelative(-0.46f, 0.46f, -0.82f, 1.03f, -0.93f, 1.67f)
                curveTo(8.74f, 18.41f, 10.18f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(3.26f, -1.59f, 2.96f, -3.42f)
                curveToRelative(-0.11f, -0.64f, -0.46f, -1.22f, -0.93f, -1.67f)
                lineTo(12.0f, 12.9f)
                close()
            }
            materialPath {
                moveTo(15.56f, 6.55f)
                lineTo(15.56f, 6.55f)
                curveTo(14.38f, 8.02f, 12.0f, 7.19f, 12.0f, 5.3f)
                verticalLineTo(3.77f)
                curveToRelative(0.0f, -0.8f, -0.89f, -1.28f, -1.55f, -0.84f)
                curveTo(8.12f, 4.49f, 4.0f, 7.97f, 4.0f, 13.0f)
                curveToRelative(0.0f, 2.92f, 1.56f, 5.47f, 3.89f, 6.86f)
                curveToRelative(-0.71f, -1.02f, -1.06f, -2.31f, -0.81f, -3.68f)
                curveToRelative(0.19f, -1.04f, 0.75f, -1.98f, 1.51f, -2.72f)
                lineToRelative(2.71f, -2.67f)
                curveToRelative(0.39f, -0.38f, 1.01f, -0.38f, 1.4f, 0.0f)
                lineToRelative(2.73f, 2.69f)
                curveToRelative(0.74f, 0.73f, 1.3f, 1.65f, 1.48f, 2.68f)
                curveToRelative(0.25f, 1.36f, -0.07f, 2.64f, -0.77f, 3.66f)
                curveToRelative(1.89f, -1.15f, 3.29f, -3.06f, 3.71f, -5.3f)
                curveToRelative(0.61f, -3.27f, -0.81f, -6.37f, -3.22f, -8.1f)
                curveTo(16.3f, 6.17f, 15.83f, 6.22f, 15.56f, 6.55f)
                close()
            }
        }
        return _localFireDepartment!!
    }

private var _localFireDepartment: ImageVector? = null

internal val Icons.Rounded.LocalFlorist: ImageVector
    get() {
        if (_localFlorist != null) {
            return _localFlorist!!
        }
        _localFlorist = materialIcon(name = "Rounded.LocalFlorist") {
            materialPath {
                moveTo(12.0f, 22.0f)
                curveToRelative(4.56f, 0.0f, 8.33f, -3.4f, 8.92f, -7.8f)
                curveToRelative(0.09f, -0.64f, -0.48f, -1.21f, -1.12f, -1.12f)
                curveToRelative(-4.4f, 0.59f, -7.8f, 4.36f, -7.8f, 8.92f)
                close()
                moveTo(5.6f, 10.25f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                curveToRelative(0.53f, 0.0f, 1.01f, -0.16f, 1.42f, -0.44f)
                lineToRelative(-0.02f, 0.19f)
                curveToRelative(0.0f, 1.38f, 1.12f, 2.5f, 2.5f, 2.5f)
                reflectiveCurveToRelative(2.5f, -1.12f, 2.5f, -2.5f)
                lineToRelative(-0.02f, -0.19f)
                curveToRelative(0.4f, 0.28f, 0.89f, 0.44f, 1.42f, 0.44f)
                curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                curveToRelative(0.0f, -1.0f, -0.59f, -1.85f, -1.43f, -2.25f)
                curveToRelative(0.84f, -0.4f, 1.43f, -1.25f, 1.43f, -2.25f)
                curveToRelative(0.0f, -1.38f, -1.12f, -2.5f, -2.5f, -2.5f)
                curveToRelative(-0.53f, 0.0f, -1.01f, 0.16f, -1.42f, 0.44f)
                lineToRelative(0.02f, -0.19f)
                curveTo(14.5f, 2.12f, 13.38f, 1.0f, 12.0f, 1.0f)
                reflectiveCurveTo(9.5f, 2.12f, 9.5f, 3.5f)
                lineToRelative(0.02f, 0.19f)
                curveToRelative(-0.4f, -0.28f, -0.89f, -0.44f, -1.42f, -0.44f)
                curveToRelative(-1.38f, 0.0f, -2.5f, 1.12f, -2.5f, 2.5f)
                curveToRelative(0.0f, 1.0f, 0.59f, 1.85f, 1.43f, 2.25f)
                curveToRelative(-0.84f, 0.4f, -1.43f, 1.25f, -1.43f, 2.25f)
                close()
                moveTo(12.0f, 5.5f)
                curveToRelative(1.38f, 0.0f, 2.5f, 1.12f, 2.5f, 2.5f)
                reflectiveCurveToRelative(-1.12f, 2.5f, -2.5f, 2.5f)
                reflectiveCurveTo(9.5f, 9.38f, 9.5f, 8.0f)
                reflectiveCurveToRelative(1.12f, -2.5f, 2.5f, -2.5f)
                close()
                moveTo(3.08f, 14.2f)
                curveTo(3.67f, 18.6f, 7.44f, 22.0f, 12.0f, 22.0f)
                curveToRelative(0.0f, -4.56f, -3.4f, -8.33f, -7.8f, -8.92f)
                curveToRelative(-0.64f, -0.09f, -1.21f, 0.48f, -1.12f, 1.12f)
                close()
            }
        }
        return _localFlorist!!
    }

private var _localFlorist: ImageVector? = null

internal val Icons.Rounded.LocalGasStation: ImageVector
    get() {
        if (_localGasStation != null) {
            return _localGasStation!!
        }
        _localGasStation = materialIcon(name = "Rounded.LocalGasStation") {
            materialPath {
                moveTo(19.77f, 7.23f)
                lineToRelative(0.01f, -0.01f)
                lineToRelative(-3.19f, -3.19f)
                curveToRelative(-0.29f, -0.29f, -0.77f, -0.29f, -1.06f, 0.0f)
                curveToRelative(-0.29f, 0.29f, -0.29f, 0.77f, 0.0f, 1.06f)
                lineToRelative(1.58f, 1.58f)
                curveToRelative(-1.05f, 0.4f, -1.76f, 1.47f, -1.58f, 2.71f)
                curveToRelative(0.16f, 1.1f, 1.1f, 1.99f, 2.2f, 2.11f)
                curveToRelative(0.47f, 0.05f, 0.88f, -0.03f, 1.27f, -0.2f)
                verticalLineToRelative(7.21f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(17.0f, 14.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(14.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(6.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(15.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-6.5f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(4.86f)
                curveToRelative(0.0f, 1.31f, 0.94f, 2.5f, 2.24f, 2.63f)
                curveToRelative(1.5f, 0.15f, 2.76f, -1.02f, 2.76f, -2.49f)
                lineTo(20.5f, 9.0f)
                curveToRelative(0.0f, -0.69f, -0.28f, -1.32f, -0.73f, -1.77f)
                close()
                moveTo(12.0f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(18.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _localGasStation!!
    }

private var _localGasStation: ImageVector? = null

internal val Icons.Rounded.LocalHospital: ImageVector
    get() {
        if (_localHospital != null) {
            return _localHospital!!
        }
        _localHospital = materialIcon(name = "Rounded.LocalHospital") {
            materialPath {
                moveTo(19.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 14.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                lineTo(7.0f, 14.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(3.0f)
                lineTo(10.0f, 7.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _localHospital!!
    }

private var _localHospital: ImageVector? = null

internal val Icons.Rounded.LocalParking: ImageVector
    get() {
        if (_localParking != null) {
            return _localParking!!
        }
        _localParking = materialIcon(name = "Rounded.LocalParking") {
            materialPath {
                moveTo(12.79f, 3.0f)
                lineTo(8.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(3.57f, 0.0f, 6.42f, -3.13f, 5.95f, -6.79f)
                curveTo(18.56f, 5.19f, 15.84f, 3.0f, 12.79f, 3.0f)
                close()
                moveTo(13.2f, 11.0f)
                lineTo(10.0f, 11.0f)
                lineTo(10.0f, 7.0f)
                horizontalLineToRelative(3.2f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _localParking!!
    }

private var _localParking: ImageVector? = null

internal val Icons.Rounded.LocalPizza: ImageVector
    get() {
        if (_localPizza != null) {
            return _localPizza!!
        }
        _localPizza = materialIcon(name = "Rounded.LocalPizza") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(9.01f, 2.0f, 6.28f, 3.08f, 4.17f, 4.88f)
                curveToRelative(-0.71f, 0.61f, -0.86f, 1.65f, -0.4f, 2.46f)
                lineToRelative(7.36f, 13.11f)
                curveToRelative(0.38f, 0.68f, 1.36f, 0.68f, 1.74f, 0.0f)
                lineToRelative(7.36f, -13.11f)
                curveToRelative(0.46f, -0.81f, 0.31f, -1.86f, -0.4f, -2.46f)
                curveTo(17.73f, 3.09f, 14.99f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(7.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 15.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _localPizza!!
    }

private var _localPizza: ImageVector? = null

internal val Icons.Rounded.LocalShipping: ImageVector
    get() {
        if (_localShipping != null) {
            return _localShipping!!
        }
        _localShipping = materialIcon(name = "Rounded.LocalShipping") {
            materialPath {
                moveTo(19.5f, 8.0f)
                lineTo(17.0f, 8.0f)
                lineTo(17.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(3.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(9.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.33f)
                curveToRelative(0.0f, -0.43f, -0.14f, -0.85f, -0.4f, -1.2f)
                lineTo(20.3f, 8.4f)
                curveToRelative(-0.19f, -0.25f, -0.49f, -0.4f, -0.8f, -0.4f)
                close()
                moveTo(6.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(19.5f, 9.5f)
                lineToRelative(1.96f, 2.5f)
                lineTo(17.0f, 12.0f)
                lineTo(17.0f, 9.5f)
                horizontalLineToRelative(2.5f)
                close()
                moveTo(18.0f, 18.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _localShipping!!
    }

private var _localShipping: ImageVector? = null

internal val Icons.Rounded.LockOpen: ImageVector
    get() {
        if (_lockOpen != null) {
            return _lockOpen!!
        }
        _lockOpen = materialIcon(name = "Rounded.LockOpen") {
            materialPath {
                moveTo(12.0f, 13.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 8.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(17.0f, 6.0f)
                curveToRelative(0.0f, -2.76f, -2.24f, -5.0f, -5.0f, -5.0f)
                curveToRelative(-2.28f, 0.0f, -4.27f, 1.54f, -4.84f, 3.75f)
                curveToRelative(-0.14f, 0.54f, 0.18f, 1.08f, 0.72f, 1.22f)
                curveToRelative(0.53f, 0.14f, 1.08f, -0.18f, 1.22f, -0.72f)
                curveTo(9.44f, 3.93f, 10.63f, 3.0f, 12.0f, 3.0f)
                curveToRelative(1.65f, 0.0f, 3.0f, 1.35f, 3.0f, 3.0f)
                verticalLineToRelative(2.0f)
                lineTo(6.0f, 8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 10.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 19.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(7.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                close()
            }
        }
        return _lockOpen!!
    }

private var _lockOpen: ImageVector? = null

internal val Icons.Rounded.Luggage: ImageVector
    get() {
        if (_luggage != null) {
            return _luggage!!
        }
        _luggage = materialIcon(name = "Rounded.Luggage") {
            materialPath {
                moveTo(17.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 2.0f, 9.0f, 2.45f, 9.0f, 3.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 6.0f, 5.0f, 6.9f, 5.0f, 8.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(19.0f, 6.9f, 18.1f, 6.0f, 17.0f, 6.0f)
                close()
                moveTo(8.75f, 18.0f)
                lineTo(8.75f, 18.0f)
                curveTo(8.34f, 18.0f, 8.0f, 17.66f, 8.0f, 17.25f)
                verticalLineToRelative(-7.5f)
                curveTo(8.0f, 9.34f, 8.34f, 9.0f, 8.75f, 9.0f)
                horizontalLineToRelative(0.0f)
                curveTo(9.16f, 9.0f, 9.5f, 9.34f, 9.5f, 9.75f)
                verticalLineToRelative(7.5f)
                curveTo(9.5f, 17.66f, 9.16f, 18.0f, 8.75f, 18.0f)
                close()
                moveTo(12.0f, 18.0f)
                lineTo(12.0f, 18.0f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineToRelative(-7.5f)
                curveTo(11.25f, 9.34f, 11.59f, 9.0f, 12.0f, 9.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.41f, 0.0f, 0.75f, 0.34f, 0.75f, 0.75f)
                verticalLineToRelative(7.5f)
                curveTo(12.75f, 17.66f, 12.41f, 18.0f, 12.0f, 18.0f)
                close()
                moveTo(13.5f, 6.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(3.5f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(15.25f, 18.0f)
                lineTo(15.25f, 18.0f)
                curveToRelative(-0.41f, 0.0f, -0.75f, -0.34f, -0.75f, -0.75f)
                verticalLineToRelative(-7.5f)
                curveTo(14.5f, 9.34f, 14.84f, 9.0f, 15.25f, 9.0f)
                horizontalLineToRelative(0.0f)
                curveTo(15.66f, 9.0f, 16.0f, 9.34f, 16.0f, 9.75f)
                verticalLineToRelative(7.5f)
                curveTo(16.0f, 17.66f, 15.66f, 18.0f, 15.25f, 18.0f)
                close()
            }
        }
        return _luggage!!
    }

private var _luggage: ImageVector? = null

internal val Icons.Rounded.LunchDining: ImageVector
    get() {
        if (_lunchDining != null) {
            return _lunchDining!!
        }
        _lunchDining = materialIcon(name = "Rounded.LunchDining") {
            materialPath {
                moveTo(3.37f, 14.28f)
                curveToRelative(0.79f, -0.29f, 1.17f, -0.78f, 1.99f, -0.78f)
                curveToRelative(1.19f, 0.0f, 1.42f, 1.0f, 3.33f, 1.0f)
                curveToRelative(1.95f, 0.0f, 2.09f, -1.0f, 3.33f, -1.0f)
                curveToRelative(1.19f, 0.0f, 1.42f, 1.0f, 3.33f, 1.0f)
                curveToRelative(1.95f, 0.0f, 2.09f, -1.0f, 3.33f, -1.0f)
                curveToRelative(0.81f, 0.0f, 1.17f, 0.46f, 1.93f, 0.76f)
                curveToRelative(0.67f, 0.26f, 1.39f, -0.25f, 1.39f, -0.96f)
                curveToRelative(0.0f, -0.43f, -0.28f, -0.81f, -0.69f, -0.96f)
                curveToRelative(-0.97f, -0.35f, -1.22f, -0.83f, -2.65f, -0.83f)
                curveToRelative(-1.95f, 0.0f, -2.09f, 1.0f, -3.33f, 1.0f)
                curveToRelative(-1.19f, 0.0f, -1.42f, -1.0f, -3.33f, -1.0f)
                curveToRelative(-1.95f, 0.0f, -2.09f, 1.0f, -3.33f, 1.0f)
                curveToRelative(-1.19f, 0.0f, -1.42f, -1.0f, -3.33f, -1.0f)
                curveToRelative(-1.55f, 0.0f, -1.96f, 0.63f, -2.68f, 0.89f)
                curveToRelative(-0.39f, 0.14f, -0.65f, 0.52f, -0.65f, 0.94f)
                curveTo(2.01f, 14.03f, 2.71f, 14.52f, 3.37f, 14.28f)
                close()
            }
            materialPath {
                moveTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineTo(19.0f)
                close()
            }
            materialPath {
                moveTo(22.0f, 9.0f)
                curveToRelative(0.02f, -4.0f, -4.28f, -6.0f, -10.0f, -6.0f)
                curveTo(6.29f, 3.0f, 2.0f, 5.0f, 2.0f, 9.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(18.0f)
                curveTo(21.55f, 10.0f, 22.0f, 9.55f, 22.0f, 9.0f)
                lineTo(22.0f, 9.0f)
                lineTo(22.0f, 9.0f)
                close()
            }
        }
        return _lunchDining!!
    }

private var _lunchDining: ImageVector? = null

internal val Icons.Rounded.Mail: ImageVector
    get() {
        if (_mail != null) {
            return _mail!!
        }
        _mail = materialIcon(name = "Rounded.Mail") {
            materialPath {
                moveTo(20.0f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.6f, 8.25f)
                lineToRelative(-6.54f, 4.09f)
                curveToRelative(-0.65f, 0.41f, -1.47f, 0.41f, -2.12f, 0.0f)
                lineTo(4.4f, 8.25f)
                curveToRelative(-0.25f, -0.16f, -0.4f, -0.43f, -0.4f, -0.72f)
                curveToRelative(0.0f, -0.67f, 0.73f, -1.07f, 1.3f, -0.72f)
                lineTo(12.0f, 11.0f)
                lineToRelative(6.7f, -4.19f)
                curveToRelative(0.57f, -0.35f, 1.3f, 0.05f, 1.3f, 0.72f)
                curveToRelative(0.0f, 0.29f, -0.15f, 0.56f, -0.4f, 0.72f)
                close()
            }
        }
        return _mail!!
    }

private var _mail: ImageVector? = null

internal val Icons.Rounded.ManageAccounts: ImageVector
    get() {
        if (_manageAccounts != null) {
            return _manageAccounts!!
        }
        _manageAccounts = materialIcon(name = "Rounded.ManageAccounts") {
            materialPath {
                moveTo(10.67f, 13.02f)
                curveTo(10.45f, 13.01f, 10.23f, 13.0f, 10.0f, 13.0f)
                curveToRelative(-2.42f, 0.0f, -4.68f, 0.67f, -6.61f, 1.82f)
                curveTo(2.51f, 15.34f, 2.0f, 16.32f, 2.0f, 17.35f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(8.26f)
                curveTo(10.47f, 18.87f, 10.0f, 17.49f, 10.0f, 16.0f)
                curveTo(10.0f, 14.93f, 10.25f, 13.93f, 10.67f, 13.02f)
                close()
            }
            materialPath {
                moveTo(10.0f, 8.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
            materialPath {
                moveTo(20.75f, 16.0f)
                curveToRelative(0.0f, -0.22f, -0.03f, -0.42f, -0.06f, -0.63f)
                lineToRelative(0.84f, -0.73f)
                curveToRelative(0.18f, -0.16f, 0.22f, -0.42f, 0.1f, -0.63f)
                lineToRelative(-0.59f, -1.02f)
                curveToRelative(-0.12f, -0.21f, -0.37f, -0.3f, -0.59f, -0.22f)
                lineToRelative(-1.06f, 0.36f)
                curveToRelative(-0.32f, -0.27f, -0.68f, -0.48f, -1.08f, -0.63f)
                lineToRelative(-0.22f, -1.09f)
                curveToRelative(-0.05f, -0.23f, -0.25f, -0.4f, -0.49f, -0.4f)
                horizontalLineToRelative(-1.18f)
                curveToRelative(-0.24f, 0.0f, -0.44f, 0.17f, -0.49f, 0.4f)
                lineToRelative(-0.22f, 1.09f)
                curveToRelative(-0.4f, 0.15f, -0.76f, 0.36f, -1.08f, 0.63f)
                lineToRelative(-1.06f, -0.36f)
                curveToRelative(-0.23f, -0.08f, -0.47f, 0.02f, -0.59f, 0.22f)
                lineToRelative(-0.59f, 1.02f)
                curveToRelative(-0.12f, 0.21f, -0.08f, 0.47f, 0.1f, 0.63f)
                lineToRelative(0.84f, 0.73f)
                curveToRelative(-0.03f, 0.21f, -0.06f, 0.41f, -0.06f, 0.63f)
                reflectiveCurveToRelative(0.03f, 0.42f, 0.06f, 0.63f)
                lineToRelative(-0.84f, 0.73f)
                curveToRelative(-0.18f, 0.16f, -0.22f, 0.42f, -0.1f, 0.63f)
                lineToRelative(0.59f, 1.02f)
                curveToRelative(0.12f, 0.21f, 0.37f, 0.3f, 0.59f, 0.22f)
                lineToRelative(1.06f, -0.36f)
                curveToRelative(0.32f, 0.27f, 0.68f, 0.48f, 1.08f, 0.63f)
                lineToRelative(0.22f, 1.09f)
                curveToRelative(0.05f, 0.23f, 0.25f, 0.4f, 0.49f, 0.4f)
                horizontalLineToRelative(1.18f)
                curveToRelative(0.24f, 0.0f, 0.44f, -0.17f, 0.49f, -0.4f)
                lineToRelative(0.22f, -1.09f)
                curveToRelative(0.4f, -0.15f, 0.76f, -0.36f, 1.08f, -0.63f)
                lineToRelative(1.06f, 0.36f)
                curveToRelative(0.23f, 0.08f, 0.47f, -0.02f, 0.59f, -0.22f)
                lineToRelative(0.59f, -1.02f)
                curveToRelative(0.12f, -0.21f, 0.08f, -0.47f, -0.1f, -0.63f)
                lineToRelative(-0.84f, -0.73f)
                curveTo(20.72f, 16.42f, 20.75f, 16.22f, 20.75f, 16.0f)
                close()
                moveTo(17.0f, 18.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveTo(18.1f, 18.0f, 17.0f, 18.0f)
                close()
            }
        }
        return _manageAccounts!!
    }

private var _manageAccounts: ImageVector? = null

internal val Icons.Rounded.Map: ImageVector
    get() {
        if (_map != null) {
            return _map!!
        }
        _map = materialIcon(name = "Rounded.Map") {
            materialPath {
                moveTo(14.65f, 4.98f)
                lineToRelative(-5.0f, -1.75f)
                curveToRelative(-0.42f, -0.15f, -0.88f, -0.15f, -1.3f, -0.01f)
                lineTo(4.36f, 4.56f)
                curveTo(3.55f, 4.84f, 3.0f, 5.6f, 3.0f, 6.46f)
                verticalLineToRelative(11.85f)
                curveToRelative(0.0f, 1.41f, 1.41f, 2.37f, 2.72f, 1.86f)
                lineToRelative(2.93f, -1.14f)
                curveToRelative(0.22f, -0.09f, 0.47f, -0.09f, 0.69f, -0.01f)
                lineToRelative(5.0f, 1.75f)
                curveToRelative(0.42f, 0.15f, 0.88f, 0.15f, 1.3f, 0.01f)
                lineToRelative(3.99f, -1.34f)
                curveToRelative(0.81f, -0.27f, 1.36f, -1.04f, 1.36f, -1.9f)
                verticalLineTo(5.69f)
                curveToRelative(0.0f, -1.41f, -1.41f, -2.37f, -2.72f, -1.86f)
                lineToRelative(-2.93f, 1.14f)
                curveToRelative(-0.22f, 0.08f, -0.46f, 0.09f, -0.69f, 0.01f)
                close()
                moveTo(15.0f, 18.89f)
                lineToRelative(-6.0f, -2.11f)
                verticalLineTo(5.11f)
                lineToRelative(6.0f, 2.11f)
                verticalLineToRelative(11.67f)
                close()
            }
        }
        return _map!!
    }

private var _map: ImageVector? = null

internal val Icons.Rounded.MedicalServices: ImageVector
    get() {
        if (_medicalServices != null) {
            return _medicalServices!!
        }
        _medicalServices = materialIcon(name = "Rounded.MedicalServices") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(8.9f, 2.0f, 8.0f, 2.9f, 8.0f, 4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 6.0f, 2.0f, 6.9f, 2.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(22.0f, 6.9f, 21.1f, 6.0f, 20.0f, 6.0f)
                close()
                moveTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(15.0f, 15.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(16.0f, 14.55f, 15.55f, 15.0f, 15.0f, 15.0f)
                close()
            }
        }
        return _medicalServices!!
    }

private var _medicalServices: ImageVector? = null

internal val Icons.Rounded.Medication: ImageVector
    get() {
        if (_medication != null) {
            return _medication!!
        }
        _medication = materialIcon(name = "Rounded.Medication") {
            materialPath {
                moveTo(17.0f, 3.0f)
                horizontalLineTo(7.0f)
                curveTo(6.45f, 3.0f, 6.0f, 3.45f, 6.0f, 4.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveTo(18.0f, 3.45f, 17.55f, 3.0f, 17.0f, 3.0f)
                close()
            }
            materialPath {
                moveTo(17.0f, 6.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 6.0f, 5.0f, 6.9f, 5.0f, 8.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(19.0f, 6.9f, 18.1f, 6.0f, 17.0f, 6.0f)
                close()
                moveTo(14.5f, 15.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(-1.0f)
                curveTo(8.67f, 15.0f, 8.0f, 14.33f, 8.0f, 13.5f)
                verticalLineToRelative(0.0f)
                curveTo(8.0f, 12.67f, 8.67f, 12.0f, 9.5f, 12.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, 0.67f, 1.5f, 1.5f)
                verticalLineToRelative(0.0f)
                curveTo(16.0f, 14.33f, 15.33f, 15.0f, 14.5f, 15.0f)
                close()
            }
        }
        return _medication!!
    }

private var _medication: ImageVector? = null

internal val Icons.Rounded.Memory: ImageVector
    get() {
        if (_memory != null) {
            return _memory!!
        }
        _memory = materialIcon(name = "Rounded.Memory") {
            materialPath {
                moveTo(14.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(13.0f, 13.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(21.0f, 10.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                lineTo(19.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(15.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(7.0f, 5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                close()
                moveTo(16.0f, 17.0f)
                lineTo(8.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(7.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _memory!!
    }

private var _memory: ImageVector? = null

internal val Icons.Rounded.Mic: ImageVector
    get() {
        if (_mic != null) {
            return _mic!!
        }
        _mic = materialIcon(name = "Rounded.Mic") {
            materialPath {
                moveTo(12.0f, 14.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                lineTo(15.0f, 5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(17.91f, 11.0f)
                curveToRelative(-0.49f, 0.0f, -0.9f, 0.36f, -0.98f, 0.85f)
                curveTo(16.52f, 14.2f, 14.47f, 16.0f, 12.0f, 16.0f)
                reflectiveCurveToRelative(-4.52f, -1.8f, -4.93f, -4.15f)
                curveToRelative(-0.08f, -0.49f, -0.49f, -0.85f, -0.98f, -0.85f)
                curveToRelative(-0.61f, 0.0f, -1.09f, 0.54f, -1.0f, 1.14f)
                curveToRelative(0.49f, 3.0f, 2.89f, 5.35f, 5.91f, 5.78f)
                lineTo(11.0f, 20.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.08f)
                curveToRelative(3.02f, -0.43f, 5.42f, -2.78f, 5.91f, -5.78f)
                curveToRelative(0.1f, -0.6f, -0.39f, -1.14f, -1.0f, -1.14f)
                close()
            }
        }
        return _mic!!
    }

private var _mic: ImageVector? = null

internal val Icons.Rounded.Microwave: ImageVector
    get() {
        if (_microwave != null) {
            return _microwave!!
        }
        _microwave = materialIcon(name = "Rounded.Microwave") {
            materialPath {
                moveTo(6.15f, 9.97f)
                lineTo(6.15f, 9.97f)
                curveTo(5.69f, 9.51f, 5.77f, 8.73f, 6.33f, 8.4f)
                curveTo(6.73f, 8.18f, 7.21f, 8.0f, 7.75f, 8.0f)
                curveToRelative(0.8f, 0.0f, 1.39f, 0.39f, 1.81f, 0.67f)
                curveTo(9.87f, 8.88f, 10.07f, 9.0f, 10.25f, 9.0f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.39f, -0.12f)
                curveToRelative(0.39f, -0.22f, 0.88f, -0.16f, 1.2f, 0.16f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.46f, 0.46f, 0.38f, 1.24f, -0.18f, 1.56f)
                curveToRelative(-0.39f, 0.23f, -0.87f, 0.4f, -1.41f, 0.4f)
                curveToRelative(-0.79f, 0.0f, -1.37f, -0.38f, -1.79f, -0.66f)
                curveTo(8.13f, 10.12f, 7.94f, 10.0f, 7.75f, 10.0f)
                curveToRelative(-0.13f, 0.0f, -0.26f, 0.05f, -0.39f, 0.12f)
                curveTo(6.96f, 10.35f, 6.47f, 10.28f, 6.15f, 9.97f)
                close()
                moveTo(7.75f, 15.0f)
                curveToRelative(0.19f, 0.0f, 0.38f, 0.12f, 0.71f, 0.34f)
                curveToRelative(0.42f, 0.28f, 1.0f, 0.66f, 1.79f, 0.66f)
                curveToRelative(0.54f, 0.0f, 1.02f, -0.17f, 1.41f, -0.4f)
                curveToRelative(0.56f, -0.32f, 0.64f, -1.1f, 0.18f, -1.56f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.32f, -0.32f, -0.81f, -0.38f, -1.2f, -0.16f)
                curveTo(10.51f, 13.95f, 10.38f, 14.0f, 10.25f, 14.0f)
                curveToRelative(-0.18f, 0.0f, -0.38f, -0.12f, -0.69f, -0.33f)
                curveTo(9.14f, 13.39f, 8.55f, 13.0f, 7.75f, 13.0f)
                curveToRelative(-0.54f, 0.0f, -1.02f, 0.18f, -1.42f, 0.4f)
                curveToRelative(-0.56f, 0.33f, -0.64f, 1.11f, -0.18f, 1.56f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.32f, 0.32f, 0.81f, 0.38f, 1.2f, 0.16f)
                curveTo(7.49f, 15.05f, 7.62f, 15.0f, 7.75f, 15.0f)
                close()
                moveTo(22.0f, 6.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(16.0f)
                curveTo(21.1f, 4.0f, 22.0f, 4.9f, 22.0f, 6.0f)
                close()
                moveTo(14.0f, 6.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(12.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(19.0f, 16.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveTo(18.55f, 17.0f, 19.0f, 16.55f, 19.0f, 16.0f)
                close()
                moveTo(19.0f, 12.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                curveTo(18.55f, 13.0f, 19.0f, 12.55f, 19.0f, 12.0f)
                close()
                moveTo(19.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
            }
        }
        return _microwave!!
    }

private var _microwave: ImageVector? = null

internal val Icons.Rounded.MilitaryTech: ImageVector
    get() {
        if (_militaryTech != null) {
            return _militaryTech!!
        }
        _militaryTech = materialIcon(name = "Rounded.MilitaryTech") {
            materialPath {
                moveTo(17.0f, 10.43f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                curveTo(7.45f, 2.0f, 7.0f, 2.45f, 7.0f, 3.0f)
                verticalLineToRelative(7.43f)
                curveToRelative(0.0f, 0.35f, 0.18f, 0.68f, 0.49f, 0.86f)
                lineToRelative(4.18f, 2.51f)
                lineToRelative(-0.99f, 2.34f)
                lineToRelative(-2.22f, 0.19f)
                curveTo(8.0f, 16.37f, 7.82f, 16.92f, 8.16f, 17.21f)
                lineToRelative(1.69f, 1.46f)
                lineToRelative(-0.51f, 2.18f)
                curveToRelative(-0.1f, 0.43f, 0.37f, 0.77f, 0.75f, 0.54f)
                lineTo(12.0f, 20.23f)
                lineToRelative(1.91f, 1.15f)
                curveToRelative(0.38f, 0.23f, 0.85f, -0.11f, 0.75f, -0.54f)
                lineToRelative(-0.51f, -2.18f)
                lineToRelative(1.69f, -1.46f)
                curveToRelative(0.33f, -0.29f, 0.16f, -0.84f, -0.29f, -0.88f)
                lineToRelative(-2.22f, -0.19f)
                lineToRelative(-0.99f, -2.34f)
                lineToRelative(4.18f, -2.51f)
                curveTo(16.82f, 11.11f, 17.0f, 10.79f, 17.0f, 10.43f)
                close()
                moveTo(13.0f, 12.23f)
                lineToRelative(-1.0f, 0.6f)
                lineToRelative(-1.0f, -0.6f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.23f)
                close()
            }
        }
        return _militaryTech!!
    }

private var _militaryTech: ImageVector? = null

internal val Icons.Rounded.MonetizationOn: ImageVector
    get() {
        if (_monetizationOn != null) {
            return _monetizationOn!!
        }
        _monetizationOn = materialIcon(name = "Rounded.MonetizationOn") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(13.41f, 18.09f)
                verticalLineToRelative(0.58f)
                curveToRelative(0.0f, 0.73f, -0.6f, 1.33f, -1.33f, 1.33f)
                horizontalLineToRelative(-0.01f)
                curveToRelative(-0.73f, 0.0f, -1.33f, -0.6f, -1.33f, -1.33f)
                verticalLineToRelative(-0.6f)
                curveToRelative(-1.33f, -0.28f, -2.51f, -1.01f, -3.01f, -2.24f)
                curveToRelative(-0.23f, -0.55f, 0.2f, -1.16f, 0.8f, -1.16f)
                horizontalLineToRelative(0.24f)
                curveToRelative(0.37f, 0.0f, 0.67f, 0.25f, 0.81f, 0.6f)
                curveToRelative(0.29f, 0.75f, 1.05f, 1.27f, 2.51f, 1.27f)
                curveToRelative(1.96f, 0.0f, 2.4f, -0.98f, 2.4f, -1.59f)
                curveToRelative(0.0f, -0.83f, -0.44f, -1.61f, -2.67f, -2.14f)
                curveToRelative(-2.48f, -0.6f, -4.18f, -1.62f, -4.18f, -3.67f)
                curveToRelative(0.0f, -1.72f, 1.39f, -2.84f, 3.11f, -3.21f)
                verticalLineToRelative(-0.6f)
                curveToRelative(0.0f, -0.73f, 0.6f, -1.33f, 1.33f, -1.33f)
                horizontalLineToRelative(0.01f)
                curveToRelative(0.73f, 0.0f, 1.33f, 0.6f, 1.33f, 1.33f)
                verticalLineToRelative(0.62f)
                curveToRelative(1.38f, 0.34f, 2.25f, 1.2f, 2.63f, 2.26f)
                curveToRelative(0.2f, 0.55f, -0.22f, 1.13f, -0.81f, 1.13f)
                horizontalLineToRelative(-0.26f)
                curveToRelative(-0.37f, 0.0f, -0.67f, -0.26f, -0.77f, -0.62f)
                curveToRelative(-0.23f, -0.76f, -0.86f, -1.25f, -2.12f, -1.25f)
                curveToRelative(-1.5f, 0.0f, -2.4f, 0.68f, -2.4f, 1.64f)
                curveToRelative(0.0f, 0.84f, 0.65f, 1.39f, 2.67f, 1.91f)
                reflectiveCurveToRelative(4.18f, 1.39f, 4.18f, 3.91f)
                curveToRelative(-0.02f, 1.83f, -1.39f, 2.83f, -3.13f, 3.16f)
                close()
            }
        }
        return _monetizationOn!!
    }

private var _monetizationOn: ImageVector? = null

internal val Icons.Rounded.Monitor: ImageVector
    get() {
        if (_monitor != null) {
            return _monitor!!
        }
        _monitor = materialIcon(name = "Rounded.Monitor") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 3.0f, 2.0f, 3.9f, 2.0f, 5.0f)
                verticalLineToRelative(11.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(-0.55f, 0.55f, -1.0f, 0.87f, -1.0f, 1.59f)
                verticalLineToRelative(0.0f)
                curveTo(6.0f, 20.37f, 6.63f, 21.0f, 7.41f, 21.0f)
                horizontalLineToRelative(9.17f)
                curveToRelative(0.78f, 0.0f, 1.41f, -0.63f, 1.41f, -1.41f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.72f, -0.44f, -1.03f, -1.0f, -1.59f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(22.0f, 3.9f, 21.1f, 3.0f, 20.0f, 3.0f)
                close()
                moveTo(20.0f, 16.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(16.0f)
                close()
            }
        }
        return _monitor!!
    }

private var _monitor: ImageVector? = null

internal val Icons.Rounded.MonitorHeart: ImageVector
    get() {
        if (_monitorHeart != null) {
            return _monitorHeart!!
        }
        _monitorHeart = materialIcon(name = "Rounded.MonitorHeart") {
            materialPath {
                moveTo(15.11f, 12.45f)
                lineTo(14.0f, 10.24f)
                lineToRelative(-3.11f, 6.21f)
                curveTo(10.73f, 16.79f, 10.38f, 17.0f, 10.0f, 17.0f)
                reflectiveCurveToRelative(-0.73f, -0.21f, -0.89f, -0.55f)
                lineTo(7.38f, 13.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(-6.0f)
                curveTo(15.62f, 13.0f, 15.27f, 12.79f, 15.11f, 12.45f)
                close()
            }
            materialPath {
                moveTo(20.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.38f, 0.0f, 0.73f, 0.21f, 0.89f, 0.55f)
                lineTo(10.0f, 13.76f)
                lineToRelative(3.11f, -6.21f)
                curveToRelative(0.34f, -0.68f, 1.45f, -0.68f, 1.79f, 0.0f)
                lineTo(16.62f, 11.0f)
                horizontalLineTo(22.0f)
                verticalLineTo(6.0f)
                curveTo(22.0f, 4.9f, 21.1f, 4.0f, 20.0f, 4.0f)
                close()
            }
        }
        return _monitorHeart!!
    }

private var _monitorHeart: ImageVector? = null

internal val Icons.Rounded.MoreHoriz: ImageVector
    get() {
        if (_moreHoriz != null) {
            return _moreHoriz!!
        }
        _moreHoriz = materialIcon(name = "Rounded.MoreHoriz") {
            materialPath {
                moveTo(6.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(18.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _moreHoriz!!
    }

private var _moreHoriz: ImageVector? = null

internal val Icons.Rounded.Mouse: ImageVector
    get() {
        if (_mouse != null) {
            return _mouse!!
        }
        _mouse = materialIcon(name = "Rounded.Mouse") {
            materialPath {
                moveTo(13.0f, 1.07f)
                lineTo(13.0f, 9.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(0.0f, -4.08f, -3.05f, -7.44f, -7.0f, -7.93f)
                close()
                moveTo(4.0f, 15.0f)
                curveToRelative(0.0f, 4.42f, 3.58f, 8.0f, 8.0f, 8.0f)
                reflectiveCurveToRelative(8.0f, -3.58f, 8.0f, -8.0f)
                verticalLineToRelative(-4.0f)
                lineTo(4.0f, 11.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(11.0f, 1.07f)
                curveTo(7.05f, 1.56f, 4.0f, 4.92f, 4.0f, 9.0f)
                horizontalLineToRelative(7.0f)
                lineTo(11.0f, 1.07f)
                close()
            }
        }
        return _mouse!!
    }

private var _mouse: ImageVector? = null

internal val Icons.Rounded.Movie: ImageVector
    get() {
        if (_movie != null) {
            return _movie!!
        }
        _movie = materialIcon(name = "Rounded.Movie") {
            materialPath {
                moveTo(18.0f, 4.0f)
                lineToRelative(1.82f, 3.64f)
                curveToRelative(0.08f, 0.16f, -0.04f, 0.36f, -0.22f, 0.36f)
                horizontalLineToRelative(-1.98f)
                curveToRelative(-0.38f, 0.0f, -0.73f, -0.21f, -0.89f, -0.55f)
                lineTo(15.0f, 4.0f)
                horizontalLineToRelative(-2.0f)
                lineToRelative(1.82f, 3.64f)
                curveToRelative(0.08f, 0.16f, -0.04f, 0.36f, -0.22f, 0.36f)
                horizontalLineToRelative(-1.98f)
                curveToRelative(-0.38f, 0.0f, -0.73f, -0.21f, -0.89f, -0.55f)
                lineTo(10.0f, 4.0f)
                horizontalLineTo(8.0f)
                lineToRelative(1.82f, 3.64f)
                curveToRelative(0.08f, 0.16f, -0.04f, 0.36f, -0.22f, 0.36f)
                horizontalLineTo(7.62f)
                curveToRelative(-0.38f, 0.0f, -0.73f, -0.21f, -0.9f, -0.55f)
                lineTo(5.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                close()
            }
        }
        return _movie!!
    }

private var _movie: ImageVector? = null

internal val Icons.Rounded.MusicNote: ImageVector
    get() {
        if (_musicNote != null) {
            return _musicNote!!
        }
        _musicNote = materialIcon(name = "Rounded.MusicNote") {
            materialPath {
                moveTo(12.0f, 5.0f)
                verticalLineToRelative(8.55f)
                curveToRelative(-0.94f, -0.54f, -2.1f, -0.75f, -3.33f, -0.32f)
                curveToRelative(-1.34f, 0.48f, -2.37f, 1.67f, -2.61f, 3.07f)
                curveToRelative(-0.46f, 2.74f, 1.86f, 5.08f, 4.59f, 4.65f)
                curveToRelative(1.96f, -0.31f, 3.35f, -2.11f, 3.35f, -4.1f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                close()
            }
        }
        return _musicNote!!
    }

private var _musicNote: ImageVector? = null

internal val Icons.Rounded.MyLocation: ImageVector
    get() {
        if (_myLocation != null) {
            return _myLocation!!
        }
        _myLocation = materialIcon(name = "Rounded.MyLocation") {
            materialPath {
                moveTo(12.0f, 8.0f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
                moveTo(20.94f, 11.0f)
                curveToRelative(-0.46f, -4.17f, -3.77f, -7.48f, -7.94f, -7.94f)
                lineTo(13.0f, 2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.06f)
                curveTo(6.83f, 3.52f, 3.52f, 6.83f, 3.06f, 11.0f)
                lineTo(2.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.06f)
                curveToRelative(0.46f, 4.17f, 3.77f, 7.48f, 7.94f, 7.94f)
                lineTo(11.0f, 22.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.06f)
                curveToRelative(4.17f, -0.46f, 7.48f, -3.77f, 7.94f, -7.94f)
                lineTo(22.0f, 13.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-1.06f)
                close()
                moveTo(12.0f, 19.0f)
                curveToRelative(-3.87f, 0.0f, -7.0f, -3.13f, -7.0f, -7.0f)
                reflectiveCurveToRelative(3.13f, -7.0f, 7.0f, -7.0f)
                reflectiveCurveToRelative(7.0f, 3.13f, 7.0f, 7.0f)
                reflectiveCurveToRelative(-3.13f, 7.0f, -7.0f, 7.0f)
                close()
            }
        }
        return _myLocation!!
    }

private var _myLocation: ImageVector? = null

internal val Icons.Rounded.Navigation: ImageVector
    get() {
        if (_navigation != null) {
            return _navigation!!
        }
        _navigation = materialIcon(name = "Rounded.Navigation") {
            materialPath {
                moveTo(12.93f, 4.26f)
                lineToRelative(6.15f, 14.99f)
                curveToRelative(0.34f, 0.83f, -0.51f, 1.66f, -1.33f, 1.29f)
                lineToRelative(-5.34f, -2.36f)
                curveToRelative(-0.26f, -0.11f, -0.55f, -0.11f, -0.81f, 0.0f)
                lineToRelative(-5.34f, 2.36f)
                curveToRelative(-0.82f, 0.36f, -1.67f, -0.46f, -1.33f, -1.29f)
                lineToRelative(6.15f, -14.99f)
                curveToRelative(0.33f, -0.83f, 1.51f, -0.83f, 1.85f, 0.0f)
                close()
            }
        }
        return _navigation!!
    }

private var _navigation: ImageVector? = null

internal val Icons.Rounded.Newspaper: ImageVector
    get() {
        if (_newspaper != null) {
            return _newspaper!!
        }
        _newspaper = materialIcon(name = "Rounded.Newspaper") {
            materialPath {
                moveTo(21.15f, 3.85f)
                lineToRelative(-0.82f, 0.82f)
                lineToRelative(-0.95f, -0.96f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.42f, 0.0f)
                lineTo(17.0f, 4.67f)
                lineToRelative(-0.96f, -0.96f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                lineToRelative(-0.95f, 0.96f)
                lineToRelative(-0.96f, -0.96f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.96f, 0.96f)
                lineTo(9.38f, 3.71f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.42f, 0.0f)
                lineTo(7.0f, 4.67f)
                lineTo(6.04f, 3.71f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                lineTo(3.67f, 4.67f)
                lineTo(2.85f, 3.85f)
                curveTo(2.54f, 3.54f, 2.0f, 3.76f, 2.0f, 4.21f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                lineToRelative(16.0f, 0.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.21f)
                curveTo(22.0f, 3.76f, 21.46f, 3.54f, 21.15f, 3.85f)
                close()
                moveTo(11.0f, 19.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(20.0f, 19.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(20.0f, 15.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(20.0f, 11.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(11.0f)
                close()
            }
        }
        return _newspaper!!
    }

private var _newspaper: ImageVector? = null

internal val Icons.Rounded.NorthEast: ImageVector
    get() {
        if (_northEast != null) {
            return _northEast!!
        }
        _northEast = materialIcon(name = "Rounded.NorthEast") {
            materialPath {
                moveTo(9.0f, 6.0f)
                lineTo(9.0f, 6.0f)
                curveToRelative(0.0f, 0.56f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.59f)
                lineTo(4.7f, 17.89f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(17.0f, 8.41f)
                verticalLineTo(14.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineTo(18.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(10.0f)
                curveTo(9.45f, 5.0f, 9.0f, 5.45f, 9.0f, 6.0f)
                close()
            }
        }
        return _northEast!!
    }

private var _northEast: ImageVector? = null

internal val Icons.Rounded.NorthWest: ImageVector
    get() {
        if (_northWest != null) {
            return _northWest!!
        }
        _northWest = materialIcon(name = "Rounded.NorthWest") {
            materialPath {
                moveTo(6.0f, 15.0f)
                lineTo(6.0f, 15.0f)
                curveToRelative(0.56f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(8.41f)
                lineTo(17.89f, 19.3f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(8.41f, 7.0f)
                horizontalLineTo(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(6.0f)
                curveTo(5.45f, 5.0f, 5.0f, 5.45f, 5.0f, 6.0f)
                verticalLineTo(14.0f)
                curveTo(5.0f, 14.55f, 5.45f, 15.0f, 6.0f, 15.0f)
                close()
            }
        }
        return _northWest!!
    }

private var _northWest: ImageVector? = null

internal val Icons.Rounded.NotificationsActive: ImageVector
    get() {
        if (_notificationsActive != null) {
            return _notificationsActive!!
        }
        _notificationsActive = materialIcon(name = "Rounded.NotificationsActive") {
            materialPath {
                moveTo(18.0f, 16.0f)
                verticalLineToRelative(-5.0f)
                curveToRelative(0.0f, -3.07f, -1.64f, -5.64f, -4.5f, -6.32f)
                lineTo(13.5f, 4.0f)
                curveToRelative(0.0f, -0.83f, -0.68f, -1.5f, -1.51f, -1.5f)
                reflectiveCurveTo(10.5f, 3.17f, 10.5f, 4.0f)
                verticalLineToRelative(0.68f)
                curveTo(7.63f, 5.36f, 6.0f, 7.92f, 6.0f, 11.0f)
                verticalLineToRelative(5.0f)
                lineToRelative(-1.3f, 1.29f)
                curveToRelative(-0.63f, 0.63f, -0.19f, 1.71f, 0.7f, 1.71f)
                horizontalLineToRelative(13.17f)
                curveToRelative(0.89f, 0.0f, 1.34f, -1.08f, 0.71f, -1.71f)
                lineTo(18.0f, 16.0f)
                close()
                moveTo(11.99f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(6.77f, 4.73f)
                curveToRelative(0.42f, -0.38f, 0.43f, -1.03f, 0.03f, -1.43f)
                curveToRelative(-0.38f, -0.38f, -1.0f, -0.39f, -1.39f, -0.02f)
                curveTo(3.7f, 4.84f, 2.52f, 6.96f, 2.14f, 9.34f)
                curveToRelative(-0.09f, 0.61f, 0.38f, 1.16f, 1.0f, 1.16f)
                curveToRelative(0.48f, 0.0f, 0.9f, -0.35f, 0.98f, -0.83f)
                curveToRelative(0.3f, -1.94f, 1.26f, -3.67f, 2.65f, -4.94f)
                close()
                moveTo(18.6f, 3.28f)
                curveToRelative(-0.4f, -0.37f, -1.02f, -0.36f, -1.4f, 0.02f)
                curveToRelative(-0.4f, 0.4f, -0.38f, 1.04f, 0.03f, 1.42f)
                curveToRelative(1.38f, 1.27f, 2.35f, 3.0f, 2.65f, 4.94f)
                curveToRelative(0.07f, 0.48f, 0.49f, 0.83f, 0.98f, 0.83f)
                curveToRelative(0.61f, 0.0f, 1.09f, -0.55f, 0.99f, -1.16f)
                curveToRelative(-0.38f, -2.37f, -1.55f, -4.48f, -3.25f, -6.05f)
                close()
            }
        }
        return _notificationsActive!!
    }

private var _notificationsActive: ImageVector? = null

internal val Icons.Rounded.OpenWith: ImageVector
    get() {
        if (_openWith != null) {
            return _openWith!!
        }
        _openWith = materialIcon(name = "Rounded.OpenWith") {
            materialPath {
                moveTo(10.5f, 9.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                lineTo(14.0f, 6.0f)
                horizontalLineToRelative(1.79f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineToRelative(-3.79f, -3.79f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineTo(7.85f, 5.15f)
                curveToRelative(-0.31f, 0.31f, -0.09f, 0.85f, 0.36f, 0.85f)
                lineTo(10.0f, 6.0f)
                verticalLineToRelative(2.5f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                close()
                moveTo(8.5f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 8.21f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-3.79f, 3.79f)
                curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
                lineToRelative(3.79f, 3.79f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.36f)
                lineTo(6.0f, 14.0f)
                horizontalLineToRelative(2.5f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
                moveTo(22.65f, 11.65f)
                lineToRelative(-3.79f, -3.79f)
                curveToRelative(-0.32f, -0.32f, -0.86f, -0.1f, -0.86f, 0.35f)
                lineTo(18.0f, 10.0f)
                horizontalLineToRelative(-2.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                lineTo(18.0f, 14.0f)
                verticalLineToRelative(1.79f)
                curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
                lineToRelative(3.79f, -3.79f)
                curveToRelative(0.2f, -0.19f, 0.2f, -0.51f, 0.01f, -0.7f)
                close()
                moveTo(13.5f, 15.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                lineTo(10.0f, 18.0f)
                lineTo(8.21f, 18.0f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.35f, 0.85f)
                lineToRelative(3.79f, 3.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(3.79f, -3.79f)
                curveToRelative(0.31f, -0.31f, 0.09f, -0.85f, -0.35f, -0.85f)
                lineTo(14.0f, 18.0f)
                verticalLineToRelative(-2.5f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
            }
        }
        return _openWith!!
    }

private var _openWith: ImageVector? = null

internal val Icons.Rounded.Palette: ImageVector
    get() {
        if (_palette != null) {
            return _palette!!
        }
        _palette = materialIcon(name = "Rounded.Palette") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.49f, 2.0f, 2.0f, 6.49f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.49f, 10.0f, 10.0f, 10.0f)
                curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                curveToRelative(0.0f, -0.61f, -0.23f, -1.2f, -0.64f, -1.67f)
                curveToRelative(-0.08f, -0.1f, -0.13f, -0.21f, -0.13f, -0.33f)
                curveToRelative(0.0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                horizontalLineTo(16.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, -2.69f, 6.0f, -6.0f)
                curveTo(22.0f, 6.04f, 17.51f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(17.5f, 13.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                curveToRelative(0.0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                curveTo(19.0f, 12.33f, 18.33f, 13.0f, 17.5f, 13.0f)
                close()
                moveTo(14.5f, 9.0f)
                curveTo(13.67f, 9.0f, 13.0f, 8.33f, 13.0f, 7.5f)
                curveTo(13.0f, 6.67f, 13.67f, 6.0f, 14.5f, 6.0f)
                reflectiveCurveTo(16.0f, 6.67f, 16.0f, 7.5f)
                curveTo(16.0f, 8.33f, 15.33f, 9.0f, 14.5f, 9.0f)
                close()
                moveTo(5.0f, 11.5f)
                curveTo(5.0f, 10.67f, 5.67f, 10.0f, 6.5f, 10.0f)
                reflectiveCurveTo(8.0f, 10.67f, 8.0f, 11.5f)
                curveTo(8.0f, 12.33f, 7.33f, 13.0f, 6.5f, 13.0f)
                reflectiveCurveTo(5.0f, 12.33f, 5.0f, 11.5f)
                close()
                moveTo(11.0f, 7.5f)
                curveTo(11.0f, 8.33f, 10.33f, 9.0f, 9.5f, 9.0f)
                reflectiveCurveTo(8.0f, 8.33f, 8.0f, 7.5f)
                curveTo(8.0f, 6.67f, 8.67f, 6.0f, 9.5f, 6.0f)
                reflectiveCurveTo(11.0f, 6.67f, 11.0f, 7.5f)
                close()
            }
        }
        return _palette!!
    }

private var _palette: ImageVector? = null

internal val Icons.Rounded.Park: ImageVector
    get() {
        if (_park != null) {
            return _park!!
        }
        _park = materialIcon(name = "Rounded.Park") {
            materialPath {
                moveTo(16.96f, 12.0f)
                horizontalLineToRelative(0.08f)
                curveToRelative(0.81f, 0.0f, 1.28f, -0.91f, 0.82f, -1.57f)
                lineToRelative(-5.08f, -7.25f)
                curveToRelative(-0.4f, -0.57f, -1.24f, -0.57f, -1.64f, 0.0f)
                lineTo(6.1f, 10.43f)
                curveTo(5.64f, 11.09f, 6.12f, 12.0f, 6.93f, 12.0f)
                horizontalLineToRelative(0.04f)
                lineToRelative(-2.9f, 4.46f)
                curveTo(3.63f, 17.12f, 4.11f, 18.0f, 4.91f, 18.0f)
                horizontalLineToRelative(5.08f)
                verticalLineToRelative(2.02f)
                curveToRelative(0.0f, 1.09f, 0.89f, 1.98f, 1.98f, 1.98f)
                horizontalLineToRelative(0.0f)
                curveToRelative(1.09f, 0.0f, 1.98f, -0.89f, 1.98f, -1.98f)
                verticalLineTo(18.0f)
                horizontalLineToRelative(5.15f)
                curveToRelative(0.8f, 0.0f, 1.28f, -0.89f, 0.83f, -1.55f)
                lineTo(16.96f, 12.0f)
                close()
            }
        }
        return _park!!
    }

private var _park: ImageVector? = null

internal val Icons.Rounded.Pause: ImageVector
    get() {
        if (_pause != null) {
            return _pause!!
        }
        _pause = materialIcon(name = "Rounded.Pause") {
            materialPath {
                moveTo(8.0f, 19.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(10.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(14.0f, 7.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(18.0f, 7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
                close()
            }
        }
        return _pause!!
    }

private var _pause: ImageVector? = null

internal val Icons.Rounded.Payments: ImageVector
    get() {
        if (_payments != null) {
            return _payments!!
        }
        _payments = materialIcon(name = "Rounded.Payments") {
            materialPath {
                moveTo(23.0f, 8.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveTo(23.0f, 7.45f, 23.0f, 8.0f)
                close()
                moveTo(4.0f, 16.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(4.0f)
                close()
                moveTo(7.0f, 10.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(7.0f, 8.34f, 7.0f, 10.0f)
                close()
            }
        }
        return _payments!!
    }

private var _payments: ImageVector? = null

internal val Icons.Rounded.Percent: ImageVector
    get() {
        if (_percent != null) {
            return _percent!!
        }
        _percent = materialIcon(name = "Rounded.Percent") {
            materialPath {
                moveTo(7.5f, 4.0f)
                curveTo(5.57f, 4.0f, 4.0f, 5.57f, 4.0f, 7.5f)
                reflectiveCurveTo(5.57f, 11.0f, 7.5f, 11.0f)
                reflectiveCurveTo(11.0f, 9.43f, 11.0f, 7.5f)
                reflectiveCurveTo(9.43f, 4.0f, 7.5f, 4.0f)
                close()
                moveTo(7.5f, 9.0f)
                curveTo(6.67f, 9.0f, 6.0f, 8.33f, 6.0f, 7.5f)
                reflectiveCurveTo(6.67f, 6.0f, 7.5f, 6.0f)
                reflectiveCurveTo(9.0f, 6.67f, 9.0f, 7.5f)
                reflectiveCurveTo(8.33f, 9.0f, 7.5f, 9.0f)
                close()
                moveTo(16.5f, 13.0f)
                curveToRelative(-1.93f, 0.0f, -3.5f, 1.57f, -3.5f, 3.5f)
                reflectiveCurveToRelative(1.57f, 3.5f, 3.5f, 3.5f)
                reflectiveCurveToRelative(3.5f, -1.57f, 3.5f, -3.5f)
                reflectiveCurveTo(18.43f, 13.0f, 16.5f, 13.0f)
                close()
                moveTo(16.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(17.33f, 18.0f, 16.5f, 18.0f)
                close()
                moveTo(19.29f, 4.71f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(6.12f, 19.29f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                reflectiveCurveToRelative(-0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(17.88f, 4.71f)
                curveTo(18.27f, 4.32f, 18.9f, 4.32f, 19.29f, 4.71f)
                close()
            }
        }
        return _percent!!
    }

private var _percent: ImageVector? = null

internal val Icons.Rounded.PersonAdd: ImageVector
    get() {
        if (_personAdd != null) {
            return _personAdd!!
        }
        _personAdd = materialIcon(name = "Rounded.PersonAdd") {
            materialPath {
                moveTo(15.0f, 12.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                close()
                moveTo(6.0f, 10.0f)
                lineTo(6.0f, 8.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                lineTo(2.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(6.0f, 10.0f)
                close()
                moveTo(15.0f, 14.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                close()
            }
        }
        return _personAdd!!
    }

private var _personAdd: ImageVector? = null

internal val Icons.Rounded.PersonRemove: ImageVector
    get() {
        if (_personRemove != null) {
            return _personRemove!!
        }
        _personRemove = materialIcon(name = "Rounded.PersonRemove") {
            materialPath {
                moveTo(14.0f, 8.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(6.0f, 5.79f, 6.0f, 8.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveTo(14.0f, 10.21f, 14.0f, 8.0f)
                close()
                moveTo(2.0f, 18.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                reflectiveCurveTo(2.0f, 15.34f, 2.0f, 18.0f)
                close()
                moveTo(18.0f, 10.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveTo(17.0f, 10.45f, 17.45f, 10.0f, 18.0f, 10.0f)
                close()
            }
        }
        return _personRemove!!
    }

private var _personRemove: ImageVector? = null

internal val Icons.Rounded.Pets: ImageVector
    get() {
        if (_pets != null) {
            return _pets!!
        }
        _pets = materialIcon(name = "Rounded.Pets") {
            materialPath {
                moveTo(4.5f, 9.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 5.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(15.0f, 5.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(19.5f, 9.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
            materialPath {
                moveTo(17.34f, 14.86f)
                curveToRelative(-0.87f, -1.02f, -1.6f, -1.89f, -2.48f, -2.91f)
                curveToRelative(-0.46f, -0.54f, -1.05f, -1.08f, -1.75f, -1.32f)
                curveToRelative(-0.11f, -0.04f, -0.22f, -0.07f, -0.33f, -0.09f)
                curveToRelative(-0.25f, -0.04f, -0.52f, -0.04f, -0.78f, -0.04f)
                reflectiveCurveToRelative(-0.53f, 0.0f, -0.79f, 0.05f)
                curveToRelative(-0.11f, 0.02f, -0.22f, 0.05f, -0.33f, 0.09f)
                curveToRelative(-0.7f, 0.24f, -1.28f, 0.78f, -1.75f, 1.32f)
                curveToRelative(-0.87f, 1.02f, -1.6f, 1.89f, -2.48f, 2.91f)
                curveToRelative(-1.31f, 1.31f, -2.92f, 2.76f, -2.62f, 4.79f)
                curveToRelative(0.29f, 1.02f, 1.02f, 2.03f, 2.33f, 2.32f)
                curveToRelative(0.73f, 0.15f, 3.06f, -0.44f, 5.54f, -0.44f)
                horizontalLineToRelative(0.18f)
                curveToRelative(2.48f, 0.0f, 4.81f, 0.58f, 5.54f, 0.44f)
                curveToRelative(1.31f, -0.29f, 2.04f, -1.31f, 2.33f, -2.32f)
                curveToRelative(0.31f, -2.04f, -1.3f, -3.49f, -2.61f, -4.8f)
                close()
            }
        }
        return _pets!!
    }

private var _pets: ImageVector? = null

internal val Icons.Rounded.PhoneInTalk: ImageVector
    get() {
        if (_phoneInTalk != null) {
            return _phoneInTalk!!
        }
        _phoneInTalk = materialIcon(name = "Rounded.PhoneInTalk") {
            materialPath {
                moveTo(12.88f, 5.05f)
                curveToRelative(3.18f, 0.4f, 5.67f, 2.89f, 6.07f, 6.07f)
                curveToRelative(0.06f, 0.51f, 0.49f, 0.88f, 0.99f, 0.88f)
                curveToRelative(0.04f, 0.0f, 0.08f, 0.0f, 0.12f, -0.01f)
                curveToRelative(0.55f, -0.07f, 0.94f, -0.57f, 0.87f, -1.12f)
                curveToRelative(-0.51f, -4.09f, -3.72f, -7.3f, -7.81f, -7.81f)
                curveToRelative(-0.55f, -0.06f, -1.05f, 0.33f, -1.11f, 0.88f)
                curveToRelative(-0.07f, 0.55f, 0.32f, 1.05f, 0.87f, 1.11f)
                close()
                moveTo(13.26f, 7.16f)
                curveToRelative(-0.53f, -0.14f, -1.08f, 0.18f, -1.22f, 0.72f)
                reflectiveCurveToRelative(0.18f, 1.08f, 0.72f, 1.22f)
                curveToRelative(1.05f, 0.27f, 1.87f, 1.09f, 2.15f, 2.15f)
                curveToRelative(0.12f, 0.45f, 0.52f, 0.75f, 0.97f, 0.75f)
                curveToRelative(0.08f, 0.0f, 0.17f, -0.01f, 0.25f, -0.03f)
                curveToRelative(0.53f, -0.14f, 0.85f, -0.69f, 0.72f, -1.22f)
                curveToRelative(-0.47f, -1.77f, -1.84f, -3.14f, -3.59f, -3.59f)
                close()
                moveTo(19.23f, 15.26f)
                lineToRelative(-2.54f, -0.29f)
                curveToRelative(-0.61f, -0.07f, -1.21f, 0.14f, -1.64f, 0.57f)
                lineToRelative(-1.84f, 1.84f)
                curveToRelative(-2.83f, -1.44f, -5.15f, -3.75f, -6.59f, -6.59f)
                lineToRelative(1.85f, -1.85f)
                curveToRelative(0.43f, -0.43f, 0.64f, -1.03f, 0.57f, -1.64f)
                lineToRelative(-0.29f, -2.52f)
                curveToRelative(-0.12f, -1.01f, -0.97f, -1.77f, -1.99f, -1.77f)
                lineTo(5.03f, 3.01f)
                curveToRelative(-1.13f, 0.0f, -2.07f, 0.94f, -2.0f, 2.07f)
                curveToRelative(0.53f, 8.54f, 7.36f, 15.36f, 15.89f, 15.89f)
                curveToRelative(1.13f, 0.07f, 2.07f, -0.87f, 2.07f, -2.0f)
                verticalLineToRelative(-1.73f)
                curveToRelative(0.01f, -1.01f, -0.75f, -1.86f, -1.76f, -1.98f)
                close()
            }
        }
        return _phoneInTalk!!
    }

private var _phoneInTalk: ImageVector? = null

internal val Icons.Rounded.PhotoCamera: ImageVector
    get() {
        if (_photoCamera != null) {
            return _photoCamera!!
        }
        _photoCamera = materialIcon(name = "Rounded.PhotoCamera") {
            materialPath {
                moveTo(12.0f, 12.0f)
                moveToRelative(-3.0f, 0.0f)
                arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
                arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
            }
            materialPath {
                moveTo(20.0f, 4.0f)
                horizontalLineToRelative(-3.17f)
                lineToRelative(-1.24f, -1.35f)
                curveToRelative(-0.37f, -0.41f, -0.91f, -0.65f, -1.47f, -0.65f)
                lineTo(9.88f, 2.0f)
                curveToRelative(-0.56f, 0.0f, -1.1f, 0.24f, -1.48f, 0.65f)
                lineTo(7.17f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
            }
        }
        return _photoCamera!!
    }

private var _photoCamera: ImageVector? = null

internal val Icons.Rounded.PieChart: ImageVector
    get() {
        if (_pieChart != null) {
            return _pieChart!!
        }
        _pieChart = materialIcon(name = "Rounded.PieChart") {
            materialPath {
                moveTo(11.0f, 3.18f)
                verticalLineToRelative(17.64f)
                curveToRelative(0.0f, 0.64f, -0.59f, 1.12f, -1.21f, 0.98f)
                curveTo(5.32f, 20.8f, 2.0f, 16.79f, 2.0f, 12.0f)
                reflectiveCurveToRelative(3.32f, -8.8f, 7.79f, -9.8f)
                curveToRelative(0.62f, -0.14f, 1.21f, 0.34f, 1.21f, 0.98f)
                close()
                moveTo(13.03f, 3.18f)
                verticalLineToRelative(6.81f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.79f)
                curveToRelative(0.64f, 0.0f, 1.12f, -0.59f, 0.98f, -1.22f)
                curveToRelative(-0.85f, -3.76f, -3.8f, -6.72f, -7.55f, -7.57f)
                curveToRelative(-0.63f, -0.14f, -1.22f, 0.34f, -1.22f, 0.98f)
                close()
                moveTo(13.03f, 14.01f)
                verticalLineToRelative(6.81f)
                curveToRelative(0.0f, 0.64f, 0.59f, 1.12f, 1.22f, 0.98f)
                curveToRelative(3.76f, -0.85f, 6.71f, -3.82f, 7.56f, -7.58f)
                curveToRelative(0.14f, -0.62f, -0.35f, -1.22f, -0.98f, -1.22f)
                horizontalLineToRelative(-6.79f)
                curveToRelative(-0.56f, 0.01f, -1.01f, 0.46f, -1.01f, 1.01f)
                close()
            }
        }
        return _pieChart!!
    }

private var _pieChart: ImageVector? = null

internal val Icons.Rounded.Podcasts: ImageVector
    get() {
        if (_podcasts != null) {
            return _podcasts!!
        }
        _podcasts = materialIcon(name = "Rounded.Podcasts") {
            materialPath {
                moveTo(14.0f, 12.0f)
                curveToRelative(0.0f, 0.74f, -0.4f, 1.38f, -1.0f, 1.72f)
                verticalLineTo(21.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-7.28f)
                curveToRelative(-0.6f, -0.35f, -1.0f, -0.98f, -1.0f, -1.72f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveTo(14.0f, 10.9f, 14.0f, 12.0f)
                close()
                moveTo(10.75f, 6.13f)
                curveToRelative(-2.27f, 0.46f, -4.12f, 2.28f, -4.61f, 4.55f)
                curveToRelative(-0.4f, 1.86f, 0.07f, 3.62f, 1.08f, 4.94f)
                curveToRelative(0.35f, 0.45f, 1.03f, 0.47f, 1.43f, 0.07f)
                lineToRelative(0.07f, -0.07f)
                curveToRelative(0.34f, -0.34f, 0.34f, -0.87f, 0.06f, -1.25f)
                curveToRelative(-0.68f, -0.9f, -0.98f, -2.1f, -0.66f, -3.37f)
                curveToRelative(0.35f, -1.42f, 1.52f, -2.57f, 2.95f, -2.88f)
                curveTo(13.69f, 7.52f, 16.0f, 9.49f, 16.0f, 12.0f)
                curveToRelative(0.0f, 0.87f, -0.28f, 1.67f, -0.76f, 2.32f)
                curveToRelative(-0.3f, 0.41f, -0.29f, 0.97f, 0.07f, 1.33f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.44f, 0.44f, 1.17f, 0.37f, 1.54f, -0.14f)
                curveTo(17.57f, 14.53f, 18.0f, 13.31f, 18.0f, 12.0f)
                curveTo(18.0f, 8.28f, 14.61f, 5.35f, 10.75f, 6.13f)
                close()
                moveTo(10.83f, 2.07f)
                curveTo(6.3f, 2.58f, 2.61f, 6.25f, 2.07f, 10.78f)
                curveToRelative(-0.35f, 2.95f, 0.59f, 5.67f, 2.32f, 7.7f)
                curveToRelative(0.37f, 0.43f, 1.03f, 0.46f, 1.43f, 0.06f)
                lineToRelative(0.05f, -0.05f)
                curveToRelative(0.35f, -0.35f, 0.38f, -0.92f, 0.05f, -1.3f)
                curveToRelative(-1.56f, -1.83f, -2.33f, -4.37f, -1.7f, -7.06f)
                curveToRelative(0.7f, -3.01f, 3.18f, -5.39f, 6.22f, -5.97f)
                curveTo(15.53f, 3.18f, 20.0f, 7.08f, 20.0f, 12.0f)
                curveToRelative(0.0f, 1.96f, -0.72f, 3.76f, -1.9f, 5.16f)
                curveToRelative(-0.34f, 0.4f, -0.31f, 0.98f, 0.05f, 1.35f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.42f, 0.42f, 1.11f, 0.39f, 1.49f, -0.07f)
                curveTo(21.11f, 16.7f, 22.0f, 14.46f, 22.0f, 12.0f)
                curveTo(22.0f, 6.09f, 16.87f, 1.38f, 10.83f, 2.07f)
                close()
            }
        }
        return _podcasts!!
    }

private var _podcasts: ImageVector? = null

internal val Icons.Rounded.Pool: ImageVector
    get() {
        if (_pool != null) {
            return _pool!!
        }
        _pool = materialIcon(name = "Rounded.Pool") {
            materialPath {
                moveTo(6.11f, 5.56f)
                curveTo(7.3f, 5.7f, 8.14f, 6.14f, 9.0f, 7.0f)
                lineToRelative(1.0f, 1.0f)
                lineToRelative(-3.25f, 3.25f)
                curveToRelative(0.31f, 0.12f, 0.56f, 0.27f, 0.77f, 0.39f)
                curveToRelative(0.37f, 0.23f, 0.59f, 0.36f, 1.15f, 0.36f)
                reflectiveCurveToRelative(0.78f, -0.13f, 1.15f, -0.36f)
                curveToRelative(0.46f, -0.27f, 1.08f, -0.64f, 2.19f, -0.64f)
                reflectiveCurveToRelative(1.73f, 0.37f, 2.18f, 0.64f)
                curveToRelative(0.37f, 0.22f, 0.6f, 0.36f, 1.15f, 0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, -0.13f, 1.15f, -0.36f)
                curveToRelative(0.12f, -0.07f, 0.26f, -0.15f, 0.41f, -0.23f)
                lineTo(10.48f, 5.0f)
                curveTo(9.22f, 3.74f, 8.04f, 3.2f, 6.3f, 3.05f)
                curveTo(5.6f, 2.99f, 5.0f, 3.56f, 5.0f, 4.26f)
                verticalLineToRelative(0.09f)
                curveToRelative(0.0f, 0.63f, 0.49f, 1.13f, 1.11f, 1.21f)
                close()
                moveTo(21.35f, 18.91f)
                curveToRelative(-0.17f, -0.06f, -0.32f, -0.15f, -0.5f, -0.27f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.6f, 0.36f, -1.15f, 0.36f)
                curveToRelative(-0.55f, 0.0f, -0.78f, -0.14f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.19f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.59f, 0.36f, -1.15f, 0.36f)
                reflectiveCurveToRelative(-0.78f, -0.13f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.19f, 0.64f)
                curveToRelative(-0.18f, 0.11f, -0.33f, 0.2f, -0.5f, 0.27f)
                curveToRelative(-0.38f, 0.13f, -0.65f, 0.45f, -0.65f, 0.85f)
                verticalLineToRelative(0.12f)
                curveToRelative(0.0f, 0.67f, 0.66f, 1.13f, 1.3f, 0.91f)
                curveToRelative(0.37f, -0.13f, 0.65f, -0.3f, 0.89f, -0.44f)
                curveToRelative(0.37f, -0.22f, 0.6f, -0.35f, 1.15f, -0.35f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.13f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                reflectiveCurveToRelative(1.73f, -0.37f, 2.19f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                reflectiveCurveToRelative(1.72f, -0.37f, 2.18f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.23f, 0.14f, 0.51f, 0.31f, 0.88f, 0.44f)
                curveToRelative(0.63f, 0.22f, 1.3f, -0.24f, 1.3f, -0.91f)
                verticalLineToRelative(-0.12f)
                curveToRelative(0.0f, -0.41f, -0.27f, -0.73f, -0.65f, -0.86f)
                close()
                moveTo(3.11f, 16.35f)
                curveToRelative(0.47f, -0.13f, 0.81f, -0.33f, 1.09f, -0.49f)
                curveToRelative(0.37f, -0.23f, 0.6f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                reflectiveCurveToRelative(1.73f, -0.37f, 2.18f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.45f, 0.27f, 1.07f, 0.64f, 2.18f, 0.64f)
                reflectiveCurveToRelative(1.73f, -0.37f, 2.18f, -0.64f)
                curveToRelative(0.37f, -0.23f, 0.59f, -0.36f, 1.15f, -0.36f)
                curveToRelative(0.55f, 0.0f, 0.78f, 0.14f, 1.15f, 0.36f)
                curveToRelative(0.23f, 0.14f, 0.5f, 0.3f, 0.85f, 0.43f)
                curveToRelative(0.63f, 0.23f, 1.31f, -0.24f, 1.31f, -0.91f)
                verticalLineToRelative(-0.12f)
                curveToRelative(0.0f, -0.4f, -0.27f, -0.72f, -0.64f, -0.86f)
                curveToRelative(-0.17f, -0.06f, -0.32f, -0.15f, -0.51f, -0.26f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.6f, 0.36f, -1.15f, 0.36f)
                reflectiveCurveToRelative(-0.78f, -0.14f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.37f, 0.23f, -0.59f, 0.36f, -1.15f, 0.36f)
                curveToRelative(-0.55f, 0.0f, -0.78f, -0.14f, -1.15f, -0.36f)
                curveToRelative(-0.45f, -0.27f, -1.07f, -0.64f, -2.18f, -0.64f)
                reflectiveCurveToRelative(-1.73f, 0.37f, -2.18f, 0.64f)
                curveToRelative(-0.18f, 0.11f, -0.33f, 0.2f, -0.5f, 0.27f)
                curveToRelative(-0.38f, 0.13f, -0.65f, 0.45f, -0.65f, 0.85f)
                verticalLineToRelative(0.23f)
                curveToRelative(0.0f, 0.58f, 0.55f, 1.02f, 1.11f, 0.86f)
                close()
            }
            materialPath {
                moveTo(16.5f, 5.5f)
                moveToRelative(-2.5f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, 5.0f, 0.0f)
                arcToRelative(2.5f, 2.5f, 0.0f, true, true, -5.0f, 0.0f)
            }
        }
        return _pool!!
    }

private var _pool: ImageVector? = null

internal val Icons.Rounded.Power: ImageVector
    get() {
        if (_power != null) {
            return _power!!
        }
        _power = materialIcon(name = "Rounded.Power") {
            materialPath {
                moveTo(16.01f, 7.0f)
                lineTo(16.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-0.01f)
                curveTo(6.9f, 7.0f, 6.0f, 7.9f, 6.0f, 8.99f)
                verticalLineToRelative(4.66f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.58f, 1.41f)
                lineTo(9.5f, 18.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                lineToRelative(2.92f, -2.92f)
                curveToRelative(0.37f, -0.38f, 0.58f, -0.89f, 0.58f, -1.42f)
                verticalLineTo(8.99f)
                curveTo(18.0f, 7.89f, 17.11f, 7.0f, 16.01f, 7.0f)
                close()
            }
        }
        return _power!!
    }

private var _power: ImageVector? = null

internal val Icons.Rounded.PowerSettingsNew: ImageVector
    get() {
        if (_powerSettingsNew != null) {
            return _powerSettingsNew!!
        }
        _powerSettingsNew = materialIcon(name = "Rounded.PowerSettingsNew") {
            materialPath {
                moveTo(12.0f, 3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(13.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(17.14f, 5.86f)
                curveToRelative(-0.39f, 0.39f, -0.38f, 1.0f, -0.01f, 1.39f)
                curveToRelative(1.13f, 1.2f, 1.83f, 2.8f, 1.87f, 4.57f)
                curveToRelative(0.09f, 3.83f, -3.08f, 7.13f, -6.91f, 7.17f)
                curveTo(8.18f, 19.05f, 5.0f, 15.9f, 5.0f, 12.0f)
                curveToRelative(0.0f, -1.84f, 0.71f, -3.51f, 1.87f, -4.76f)
                curveToRelative(0.37f, -0.39f, 0.37f, -1.0f, -0.01f, -1.38f)
                curveToRelative(-0.4f, -0.4f, -1.05f, -0.39f, -1.43f, 0.02f)
                curveTo(3.98f, 7.42f, 3.07f, 9.47f, 3.0f, 11.74f)
                curveToRelative(-0.14f, 4.88f, 3.83f, 9.1f, 8.71f, 9.25f)
                curveToRelative(5.1f, 0.16f, 9.29f, -3.93f, 9.29f, -9.0f)
                curveToRelative(0.0f, -2.37f, -0.92f, -4.51f, -2.42f, -6.11f)
                curveToRelative(-0.38f, -0.41f, -1.04f, -0.42f, -1.44f, -0.02f)
                close()
            }
        }
        return _powerSettingsNew!!
    }

private var _powerSettingsNew: ImageVector? = null

internal val Icons.Rounded.Print: ImageVector
    get() {
        if (_print != null) {
            return _print!!
        }
        _print = materialIcon(name = "Rounded.Print") {
            materialPath {
                moveTo(19.0f, 8.0f)
                lineTo(5.0f, 8.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(15.0f, 19.0f)
                lineTo(9.0f, 19.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(19.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(17.0f, 3.0f)
                lineTo(7.0f, 3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(18.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
            }
        }
        return _print!!
    }

private var _print: ImageVector? = null

internal val Icons.Rounded.Psychology: ImageVector
    get() {
        if (_psychology != null) {
            return _psychology!!
        }
        _psychology = materialIcon(name = "Rounded.Psychology") {
            materialPath {
                moveTo(13.0f, 8.57f)
                curveToRelative(-0.79f, 0.0f, -1.43f, 0.64f, -1.43f, 1.43f)
                reflectiveCurveToRelative(0.64f, 1.43f, 1.43f, 1.43f)
                reflectiveCurveToRelative(1.43f, -0.64f, 1.43f, -1.43f)
                reflectiveCurveTo(13.79f, 8.57f, 13.0f, 8.57f)
                close()
            }
            materialPath {
                moveTo(13.21f, 3.0f)
                curveToRelative(-3.84f, -0.11f, -7.0f, 2.87f, -7.19f, 6.64f)
                lineTo(4.1f, 12.2f)
                curveTo(3.85f, 12.53f, 4.09f, 13.0f, 4.5f, 13.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.68f)
                curveToRelative(2.44f, -1.16f, 4.1f, -3.68f, 4.0f, -6.58f)
                curveTo(19.86f, 6.12f, 16.82f, 3.11f, 13.21f, 3.0f)
                close()
                moveTo(16.0f, 10.0f)
                curveToRelative(0.0f, 0.13f, -0.01f, 0.26f, -0.02f, 0.39f)
                lineToRelative(0.83f, 0.66f)
                curveToRelative(0.08f, 0.06f, 0.1f, 0.16f, 0.05f, 0.25f)
                lineToRelative(-0.8f, 1.39f)
                curveToRelative(-0.05f, 0.09f, -0.16f, 0.12f, -0.24f, 0.09f)
                lineToRelative(-0.99f, -0.4f)
                curveToRelative(-0.21f, 0.16f, -0.43f, 0.29f, -0.67f, 0.39f)
                lineTo(14.0f, 13.83f)
                curveToRelative(-0.01f, 0.1f, -0.1f, 0.17f, -0.2f, 0.17f)
                horizontalLineToRelative(-1.6f)
                curveToRelative(-0.1f, 0.0f, -0.18f, -0.07f, -0.2f, -0.17f)
                lineToRelative(-0.15f, -1.06f)
                curveToRelative(-0.25f, -0.1f, -0.47f, -0.23f, -0.68f, -0.39f)
                lineToRelative(-0.99f, 0.4f)
                curveToRelative(-0.09f, 0.03f, -0.2f, 0.0f, -0.25f, -0.09f)
                lineToRelative(-0.8f, -1.39f)
                curveToRelative(-0.05f, -0.08f, -0.03f, -0.19f, 0.05f, -0.25f)
                lineToRelative(0.84f, -0.66f)
                curveTo(10.01f, 10.26f, 10.0f, 10.13f, 10.0f, 10.0f)
                curveToRelative(0.0f, -0.13f, 0.02f, -0.27f, 0.04f, -0.39f)
                lineTo(9.19f, 8.95f)
                curveToRelative(-0.08f, -0.06f, -0.1f, -0.16f, -0.05f, -0.26f)
                lineToRelative(0.8f, -1.38f)
                curveToRelative(0.05f, -0.09f, 0.15f, -0.12f, 0.24f, -0.09f)
                lineToRelative(1.0f, 0.4f)
                curveToRelative(0.2f, -0.15f, 0.43f, -0.29f, 0.67f, -0.39f)
                lineToRelative(0.15f, -1.06f)
                curveTo(12.02f, 6.07f, 12.1f, 6.0f, 12.2f, 6.0f)
                horizontalLineToRelative(1.6f)
                curveToRelative(0.1f, 0.0f, 0.18f, 0.07f, 0.2f, 0.17f)
                lineToRelative(0.15f, 1.06f)
                curveToRelative(0.24f, 0.1f, 0.46f, 0.23f, 0.67f, 0.39f)
                lineToRelative(1.0f, -0.4f)
                curveToRelative(0.09f, -0.03f, 0.2f, 0.0f, 0.24f, 0.09f)
                lineToRelative(0.8f, 1.38f)
                curveToRelative(0.05f, 0.09f, 0.03f, 0.2f, -0.05f, 0.26f)
                lineToRelative(-0.85f, 0.66f)
                curveTo(15.99f, 9.73f, 16.0f, 9.86f, 16.0f, 10.0f)
                close()
            }
        }
        return _psychology!!
    }

private var _psychology: ImageVector? = null

internal val Icons.Rounded.Public: ImageVector
    get() {
        if (_public != null) {
            return _public!!
        }
        _public = materialIcon(name = "Rounded.Public") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(11.0f, 19.93f)
                curveToRelative(-3.95f, -0.49f, -7.0f, -3.85f, -7.0f, -7.93f)
                curveToRelative(0.0f, -0.62f, 0.08f, -1.21f, 0.21f, -1.79f)
                lineTo(9.0f, 15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                verticalLineToRelative(1.93f)
                close()
                moveTo(17.9f, 17.39f)
                curveToRelative(-0.26f, -0.81f, -1.0f, -1.39f, -1.9f, -1.39f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(8.0f, 12.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-0.41f)
                curveToRelative(2.93f, 1.19f, 5.0f, 4.06f, 5.0f, 7.41f)
                curveToRelative(0.0f, 2.08f, -0.8f, 3.97f, -2.1f, 5.39f)
                close()
            }
        }
        return _public!!
    }

private var _public: ImageVector? = null

internal val Icons.Rounded.QrCode: ImageVector
    get() {
        if (_qrCode != null) {
            return _qrCode!!
        }
        _qrCode = materialIcon(name = "Rounded.QrCode") {
            materialPath {
                moveTo(5.0f, 11.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(4.0f)
                curveTo(3.0f, 10.1f, 3.9f, 11.0f, 5.0f, 11.0f)
                close()
                moveTo(5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(5.0f)
                close()
            }
            materialPath {
                moveTo(5.0f, 21.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(4.0f)
                curveTo(3.0f, 20.1f, 3.9f, 21.0f, 5.0f, 21.0f)
                close()
                moveTo(5.0f, 15.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(15.0f)
                close()
            }
            materialPath {
                moveTo(13.0f, 5.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(13.9f, 3.0f, 13.0f, 3.9f, 13.0f, 5.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(9.0f)
                close()
            }
            materialPath {
                moveTo(21.0f, 20.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.0f)
                curveTo(20.78f, 21.0f, 21.0f, 20.78f, 21.0f, 20.5f)
                close()
            }
            materialPath {
                moveTo(13.0f, 13.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveTo(13.22f, 13.0f, 13.0f, 13.22f, 13.0f, 13.5f)
                close()
            }
            materialPath {
                moveTo(16.5f, 15.0f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveTo(17.0f, 15.22f, 16.78f, 15.0f, 16.5f, 15.0f)
                close()
            }
            materialPath {
                moveTo(13.0f, 17.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveTo(13.22f, 17.0f, 13.0f, 17.22f, 13.0f, 17.5f)
                close()
            }
            materialPath {
                moveTo(15.5f, 21.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveTo(15.0f, 20.78f, 15.22f, 21.0f, 15.5f, 21.0f)
                close()
            }
            materialPath {
                moveTo(17.5f, 19.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveTo(17.0f, 18.78f, 17.22f, 19.0f, 17.5f, 19.0f)
                close()
            }
            materialPath {
                moveTo(18.5f, 13.0f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveTo(19.0f, 13.22f, 18.78f, 13.0f, 18.5f, 13.0f)
                close()
            }
            materialPath {
                moveTo(19.5f, 17.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-1.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                verticalLineToRelative(1.0f)
                curveTo(19.0f, 16.78f, 19.22f, 17.0f, 19.5f, 17.0f)
                close()
            }
        }
        return _qrCode!!
    }

private var _qrCode: ImageVector? = null

internal val Icons.Rounded.Radio: ImageVector
    get() {
        if (_radio != null) {
            return _radio!!
        }
        _radio = materialIcon(name = "Rounded.Radio") {
            materialPath {
                moveTo(3.24f, 6.15f)
                curveTo(2.51f, 6.43f, 2.0f, 7.17f, 2.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(8.3f, 6.0f)
                lineToRelative(7.43f, -3.0f)
                curveToRelative(0.46f, -0.19f, 0.68f, -0.71f, 0.49f, -1.17f)
                curveToRelative(-0.19f, -0.46f, -0.71f, -0.68f, -1.17f, -0.49f)
                lineTo(3.24f, 6.15f)
                close()
                moveTo(7.0f, 20.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                close()
                moveTo(20.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(4.0f, 12.0f)
                lineTo(4.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _radio!!
    }

private var _radio: ImageVector? = null

internal val Icons.Rounded.RadioButtonChecked: ImageVector
    get() {
        if (_radioButtonChecked != null) {
            return _radioButtonChecked!!
        }
        _radioButtonChecked = materialIcon(name = "Rounded.RadioButtonChecked") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 12.0f)
                moveToRelative(-5.0f, 0.0f)
                arcToRelative(5.0f, 5.0f, 0.0f, true, true, 10.0f, 0.0f)
                arcToRelative(5.0f, 5.0f, 0.0f, true, true, -10.0f, 0.0f)
            }
        }
        return _radioButtonChecked!!
    }

private var _radioButtonChecked: ImageVector? = null

internal val Icons.Rounded.RamenDining: ImageVector
    get() {
        if (_ramenDining != null) {
            return _ramenDining!!
        }
        _ramenDining = materialIcon(name = "Rounded.RamenDining") {
            materialPath {
                moveTo(22.0f, 2.84f)
                lineTo(22.0f, 2.84f)
                curveToRelative(0.0f, -0.45f, -0.39f, -0.79f, -0.83f, -0.75f)
                lineTo(4.89f, 3.9f)
                curveTo(4.38f, 3.95f, 4.0f, 4.38f, 4.0f, 4.89f)
                verticalLineTo(12.0f)
                horizontalLineTo(3.08f)
                curveToRelative(-0.6f, 0.0f, -1.08f, 0.53f, -1.0f, 1.13f)
                curveTo(2.52f, 16.33f, 4.83f, 19.0f, 8.0f, 20.25f)
                verticalLineTo(21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-0.75f)
                curveToRelative(3.17f, -1.25f, 5.48f, -3.92f, 5.92f, -7.12f)
                curveToRelative(0.08f, -0.6f, -0.4f, -1.13f, -1.0f, -1.13f)
                horizontalLineTo(10.5f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(10.75f)
                curveTo(21.66f, 8.0f, 22.0f, 7.66f, 22.0f, 7.25f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.41f, -0.34f, -0.75f, -0.75f, -0.75f)
                horizontalLineTo(10.5f)
                verticalLineTo(4.78f)
                lineToRelative(10.83f, -1.19f)
                curveTo(21.71f, 3.54f, 22.0f, 3.22f, 22.0f, 2.84f)
                close()
                moveTo(6.5f, 5.22f)
                verticalLineTo(6.5f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(5.34f)
                lineTo(6.5f, 5.22f)
                close()
                moveTo(5.5f, 8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(8.0f)
                close()
                moveTo(9.0f, 12.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(9.0f, 6.5f)
                horizontalLineTo(8.0f)
                verticalLineTo(5.06f)
                lineToRelative(1.0f, -0.11f)
                verticalLineTo(6.5f)
                close()
            }
        }
        return _ramenDining!!
    }

private var _ramenDining: ImageVector? = null

internal val Icons.Rounded.Receipt: ImageVector
    get() {
        if (_receipt != null) {
            return _receipt!!
        }
        _receipt = materialIcon(name = "Rounded.Receipt") {
            materialPath {
                moveTo(21.0f, 2.21f)
                curveToRelative(-0.13f, 0.0f, -0.26f, 0.05f, -0.35f, 0.15f)
                lineToRelative(-0.79f, 0.79f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, -0.79f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, 0.79f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, -0.79f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, 0.79f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, -0.79f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, 0.79f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.8f, -0.8f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, 0.8f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, -0.8f)
                curveToRelative(-0.2f, -0.2f, -0.51f, -0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, 0.8f)
                curveToRelative(-0.2f, 0.2f, -0.51f, 0.2f, -0.71f, 0.0f)
                lineToRelative(-0.79f, -0.8f)
                curveToRelative(-0.09f, -0.09f, -0.22f, -0.14f, -0.35f, -0.14f)
                lineTo(3.0f, 21.8f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.35f, -0.15f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.2f, 0.2f, 0.51f, 0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, -0.79f)
                curveToRelative(0.2f, -0.2f, 0.51f, -0.2f, 0.71f, 0.0f)
                lineToRelative(0.79f, 0.79f)
                curveToRelative(0.1f, 0.1f, 0.23f, 0.15f, 0.35f, 0.15f)
                lineTo(20.99f, 2.21f)
                close()
                moveTo(17.0f, 17.0f)
                lineTo(7.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(17.0f, 13.0f)
                lineTo(7.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(17.0f, 9.0f)
                lineTo(7.0f, 9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _receipt!!
    }

private var _receipt: ImageVector? = null

internal val Icons.Rounded.RecordVoiceOver: ImageVector
    get() {
        if (_recordVoiceOver != null) {
            return _recordVoiceOver!!
        }
        _recordVoiceOver = materialIcon(name = "Rounded.RecordVoiceOver") {
            materialPath {
                moveTo(9.0f, 9.0f)
                moveToRelative(-4.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 15.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                close()
                moveTo(15.47f, 7.77f)
                curveToRelative(0.32f, 0.79f, 0.32f, 1.67f, 0.0f, 2.46f)
                curveToRelative(-0.19f, 0.47f, -0.11f, 1.0f, 0.25f, 1.36f)
                lineToRelative(0.03f, 0.03f)
                curveToRelative(0.58f, 0.58f, 1.57f, 0.46f, 1.95f, -0.27f)
                curveToRelative(0.76f, -1.45f, 0.76f, -3.15f, -0.02f, -4.66f)
                curveToRelative(-0.38f, -0.74f, -1.38f, -0.88f, -1.97f, -0.29f)
                lineToRelative(-0.01f, 0.01f)
                curveToRelative(-0.34f, 0.35f, -0.42f, 0.89f, -0.23f, 1.36f)
                close()
                moveTo(19.18f, 2.89f)
                curveToRelative(-0.4f, 0.4f, -0.46f, 1.02f, -0.13f, 1.48f)
                curveToRelative(1.97f, 2.74f, 1.96f, 6.41f, -0.03f, 9.25f)
                curveToRelative(-0.32f, 0.45f, -0.25f, 1.07f, 0.14f, 1.46f)
                lineToRelative(0.03f, 0.03f)
                curveToRelative(0.49f, 0.49f, 1.32f, 0.45f, 1.74f, -0.1f)
                curveToRelative(2.75f, -3.54f, 2.76f, -8.37f, 0.0f, -12.02f)
                curveToRelative(-0.42f, -0.55f, -1.26f, -0.59f, -1.75f, -0.1f)
                close()
            }
        }
        return _recordVoiceOver!!
    }

private var _recordVoiceOver: ImageVector? = null

internal val Icons.Rounded.Recycling: ImageVector
    get() {
        if (_recycling != null) {
            return _recycling!!
        }
        _recycling = materialIcon(name = "Rounded.Recycling") {
            materialPath {
                moveTo(6.2f, 7.41f)
                curveTo(5.97f, 7.27f, 5.89f, 6.96f, 6.03f, 6.73f)
                lineTo(7.2f, 4.78f)
                lineToRelative(1.03f, -1.71f)
                curveToRelative(0.39f, -0.65f, 1.33f, -0.65f, 1.72f, 0.0f)
                lineToRelative(1.48f, 2.46f)
                lineToRelative(-1.23f, 2.06f)
                lineToRelative(-0.72f, 1.2f)
                curveTo(9.32f, 9.02f, 9.01f, 9.1f, 8.77f, 8.96f)
                lineTo(6.2f, 7.41f)
                close()
                moveTo(21.72f, 12.97f)
                lineToRelative(-1.34f, -2.24f)
                curveToRelative(-0.14f, -0.23f, -0.44f, -0.31f, -0.68f, -0.18f)
                lineToRelative(-2.6f, 1.5f)
                curveToRelative(-0.24f, 0.14f, -0.32f, 0.45f, -0.18f, 0.69f)
                lineTo(18.87f, 16.0f)
                lineToRelative(1.09f, 0.0f)
                curveToRelative(0.61f, 0.0f, 1.2f, -0.26f, 1.59f, -0.73f)
                curveTo(21.85f, 14.9f, 22.0f, 14.45f, 22.0f, 14.0f)
                curveTo(22.0f, 13.64f, 21.9f, 13.29f, 21.72f, 12.97f)
                close()
                moveTo(16.0f, 21.0f)
                horizontalLineToRelative(1.5f)
                curveToRelative(0.76f, 0.0f, 1.45f, -0.43f, 1.79f, -1.11f)
                lineTo(20.74f, 17.0f)
                horizontalLineTo(16.0f)
                verticalLineToRelative(-0.79f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-2.79f, 2.79f)
                curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
                lineToRelative(2.79f, 2.79f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.35f)
                verticalLineTo(21.0f)
                close()
                moveTo(9.5f, 17.0f)
                horizontalLineTo(5.7f)
                lineToRelative(-0.84f, 1.41f)
                curveToRelative(-0.3f, 0.5f, -0.32f, 1.12f, -0.06f, 1.65f)
                lineToRelative(0.0f, 0.0f)
                curveTo(5.08f, 20.63f, 5.67f, 21.0f, 6.32f, 21.0f)
                horizontalLineTo(9.5f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.0f)
                curveTo(10.0f, 17.22f, 9.78f, 17.0f, 9.5f, 17.0f)
                close()
                moveTo(6.12f, 14.35f)
                lineToRelative(0.7f, 0.42f)
                curveToRelative(0.38f, 0.23f, 0.85f, -0.12f, 0.74f, -0.55f)
                lineTo(6.6f, 10.38f)
                curveTo(6.54f, 10.12f, 6.27f, 9.95f, 6.0f, 10.02f)
                lineToRelative(-3.83f, 0.96f)
                curveToRelative(-0.43f, 0.11f, -0.52f, 0.68f, -0.14f, 0.91f)
                lineToRelative(0.66f, 0.4f)
                lineToRelative(-0.41f, 0.69f)
                curveToRelative(-0.35f, 0.59f, -0.38f, 1.31f, -0.07f, 1.92f)
                lineToRelative(1.63f, 3.26f)
                lineTo(6.12f, 14.35f)
                close()
                moveTo(17.02f, 5.14f)
                lineToRelative(-1.3f, -2.17f)
                curveTo(15.35f, 2.37f, 14.7f, 2.0f, 14.0f, 2.0f)
                horizontalLineToRelative(-3.53f)
                lineToRelative(3.12f, 5.2f)
                lineTo(12.9f, 7.61f)
                curveToRelative(-0.38f, 0.23f, -0.3f, 0.81f, 0.14f, 0.91f)
                lineToRelative(3.83f, 0.96f)
                curveToRelative(0.27f, 0.07f, 0.54f, -0.1f, 0.61f, -0.36f)
                lineToRelative(0.96f, -3.83f)
                curveToRelative(0.11f, -0.43f, -0.36f, -0.78f, -0.74f, -0.55f)
                lineTo(17.02f, 5.14f)
                close()
            }
        }
        return _recycling!!
    }

private var _recycling: ImageVector? = null

internal val Icons.Rounded.Remove: ImageVector
    get() {
        if (_remove != null) {
            return _remove!!
        }
        _remove = materialIcon(name = "Rounded.Remove") {
            materialPath {
                moveTo(18.0f, 13.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _remove!!
    }

private var _remove: ImageVector? = null

internal val Icons.Rounded.RemoveCircle: ImageVector
    get() {
        if (_removeCircle != null) {
            return _removeCircle!!
        }
        _removeCircle = materialIcon(name = "Rounded.RemoveCircle") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(16.0f, 13.0f)
                lineTo(8.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _removeCircle!!
    }

private var _removeCircle: ImageVector? = null

internal val Icons.Rounded.Repeat: ImageVector
    get() {
        if (_repeat != null) {
            return _repeat!!
        }
        _repeat = materialIcon(name = "Rounded.Repeat") {
            materialPath {
                moveTo(7.0f, 7.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(1.79f)
                curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
                lineToRelative(2.79f, -2.79f)
                curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
                lineToRelative(-2.79f, -2.79f)
                curveToRelative(-0.31f, -0.31f, -0.85f, -0.09f, -0.85f, 0.36f)
                lineTo(17.0f, 5.0f)
                lineTo(6.0f, 5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(7.0f, 7.0f)
                close()
                moveTo(17.0f, 17.0f)
                lineTo(7.0f, 17.0f)
                verticalLineToRelative(-1.79f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-2.79f, 2.79f)
                curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
                lineToRelative(2.79f, 2.79f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.36f)
                lineTo(7.0f, 19.0f)
                horizontalLineToRelative(11.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                close()
            }
        }
        return _repeat!!
    }

private var _repeat: ImageVector? = null

internal val Icons.Rounded.Replay: ImageVector
    get() {
        if (_replay != null) {
            return _replay!!
        }
        _replay = materialIcon(name = "Rounded.Replay") {
            materialPath {
                moveTo(12.0f, 5.0f)
                verticalLineTo(2.21f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-3.8f, 3.79f)
                curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0.0f, 0.71f)
                lineToRelative(3.79f, 3.79f)
                curveToRelative(0.32f, 0.31f, 0.86f, 0.09f, 0.86f, -0.36f)
                verticalLineTo(7.0f)
                curveToRelative(3.73f, 0.0f, 6.68f, 3.42f, 5.86f, 7.29f)
                curveToRelative(-0.47f, 2.27f, -2.31f, 4.1f, -4.57f, 4.57f)
                curveToRelative(-3.57f, 0.75f, -6.75f, -1.7f, -7.23f, -5.01f)
                curveToRelative(-0.07f, -0.48f, -0.49f, -0.85f, -0.98f, -0.85f)
                curveToRelative(-0.6f, 0.0f, -1.08f, 0.53f, -1.0f, 1.13f)
                curveToRelative(0.62f, 4.39f, 4.8f, 7.64f, 9.53f, 6.72f)
                curveToRelative(3.12f, -0.61f, 5.63f, -3.12f, 6.24f, -6.24f)
                curveTo(20.84f, 9.48f, 16.94f, 5.0f, 12.0f, 5.0f)
                close()
            }
        }
        return _replay!!
    }

private var _replay: ImageVector? = null

internal val Icons.Rounded.Restaurant: ImageVector
    get() {
        if (_restaurant != null) {
            return _restaurant!!
        }
        _restaurant = materialIcon(name = "Rounded.Restaurant") {
            materialPath {
                moveTo(16.0f, 6.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(21.0f, 3.13f)
                curveToRelative(0.0f, -0.65f, -0.61f, -1.13f, -1.24f, -0.98f)
                curveTo(17.6f, 2.68f, 16.0f, 4.51f, 16.0f, 6.0f)
                close()
                moveTo(11.0f, 9.0f)
                lineTo(9.0f, 9.0f)
                lineTo(9.0f, 3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                lineTo(5.0f, 9.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-8.0f)
                curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
                lineTo(13.0f, 3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
        return _restaurant!!
    }

private var _restaurant: ImageVector? = null

internal val Icons.Rounded.RocketLaunch: ImageVector
    get() {
        if (_rocketLaunch != null) {
            return _rocketLaunch!!
        }
        _rocketLaunch = materialIcon(name = "Rounded.RocketLaunch") {
            materialPath {
                moveTo(9.19f, 6.35f)
                curveToRelative(-2.04f, 2.29f, -3.44f, 5.58f, -3.57f, 5.89f)
                lineToRelative(-2.26f, -0.97f)
                curveToRelative(-0.65f, -0.28f, -0.81f, -1.13f, -0.31f, -1.63f)
                lineToRelative(3.01f, -3.01f)
                curveToRelative(0.47f, -0.47f, 1.15f, -0.68f, 1.81f, -0.55f)
                lineTo(9.19f, 6.35f)
                lineTo(9.19f, 6.35f)
                close()
                moveTo(10.68f, 16.51f)
                curveToRelative(0.3f, 0.3f, 0.74f, 0.38f, 1.12f, 0.2f)
                curveToRelative(1.16f, -0.54f, 3.65f, -1.81f, 5.26f, -3.42f)
                curveToRelative(4.59f, -4.59f, 4.63f, -8.33f, 4.36f, -9.93f)
                curveToRelative(-0.07f, -0.4f, -0.39f, -0.72f, -0.79f, -0.79f)
                curveToRelative(-1.6f, -0.27f, -5.34f, -0.23f, -9.93f, 4.36f)
                curveToRelative(-1.61f, 1.61f, -2.87f, 4.1f, -3.42f, 5.26f)
                curveToRelative(-0.18f, 0.38f, -0.09f, 0.83f, 0.2f, 1.12f)
                lineTo(10.68f, 16.51f)
                close()
                moveTo(17.65f, 14.81f)
                curveToRelative(-2.29f, 2.04f, -5.58f, 3.44f, -5.89f, 3.57f)
                lineToRelative(0.97f, 2.26f)
                curveToRelative(0.28f, 0.65f, 1.13f, 0.81f, 1.63f, 0.31f)
                lineToRelative(3.01f, -3.01f)
                curveToRelative(0.47f, -0.47f, 0.68f, -1.15f, 0.55f, -1.81f)
                lineTo(17.65f, 14.81f)
                lineTo(17.65f, 14.81f)
                close()
                moveTo(8.94f, 17.41f)
                curveToRelative(0.2f, 1.06f, -0.15f, 2.04f, -0.82f, 2.71f)
                curveToRelative(-0.77f, 0.77f, -3.16f, 1.34f, -4.71f, 1.64f)
                curveToRelative(-0.69f, 0.13f, -1.3f, -0.48f, -1.17f, -1.17f)
                curveToRelative(0.3f, -1.55f, 0.86f, -3.94f, 1.64f, -4.71f)
                curveToRelative(0.67f, -0.67f, 1.65f, -1.02f, 2.71f, -0.82f)
                curveTo(7.76f, 15.28f, 8.72f, 16.24f, 8.94f, 17.41f)
                close()
                moveTo(13.0f, 9.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveTo(13.0f, 10.1f, 13.0f, 9.0f)
                close()
            }
        }
        return _rocketLaunch!!
    }

private var _rocketLaunch: ImageVector? = null

internal val Icons.Rounded.Route: ImageVector
    get() {
        if (_route != null) {
            return _route!!
        }
        _route = materialIcon(name = "Rounded.Route") {
            materialPath {
                moveTo(19.0f, 15.18f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                reflectiveCurveToRelative(-2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(8.82f)
                curveTo(8.16f, 8.4f, 9.0f, 7.3f, 9.0f, 6.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(3.0f, 4.34f, 3.0f, 6.0f)
                curveToRelative(0.0f, 1.3f, 0.84f, 2.4f, 2.0f, 2.82f)
                verticalLineTo(17.0f)
                curveToRelative(0.0f, 2.21f, 1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(8.18f)
                curveToRelative(-1.16f, 0.41f, -2.0f, 1.51f, -2.0f, 2.82f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                curveTo(21.0f, 16.7f, 20.16f, 15.6f, 19.0f, 15.18f)
                close()
            }
        }
        return _route!!
    }

private var _route: ImageVector? = null

internal val Icons.Rounded.Sailing: ImageVector
    get() {
        if (_sailing != null) {
            return _sailing!!
        }
        _sailing = materialIcon(name = "Rounded.Sailing") {
            materialPath {
                moveTo(11.0f, 13.0f)
                verticalLineTo(3.59f)
                curveToRelative(0.0f, -0.49f, -0.63f, -0.69f, -0.91f, -0.29f)
                lineToRelative(-6.54f, 9.41f)
                curveToRelative(-0.23f, 0.33f, 0.01f, 0.79f, 0.41f, 0.79f)
                horizontalLineToRelative(6.54f)
                curveTo(10.78f, 13.5f, 11.0f, 13.28f, 11.0f, 13.0f)
                close()
                moveTo(20.99f, 12.98f)
                curveTo(20.72f, 7.07f, 15.9f, 2.32f, 13.4f, 1.23f)
                curveToRelative(-0.37f, -0.16f, -0.77f, 0.2f, -0.67f, 0.59f)
                curveToRelative(0.3f, 1.13f, 0.76f, 3.28f, 0.76f, 5.68f)
                curveToRelative(0.0f, 2.44f, -0.49f, 4.39f, -0.78f, 5.35f)
                curveToRelative(-0.1f, 0.32f, 0.14f, 0.65f, 0.48f, 0.65f)
                horizontalLineToRelative(7.28f)
                curveTo(20.76f, 13.5f, 21.0f, 13.26f, 20.99f, 12.98f)
                close()
                moveTo(20.62f, 15.0f)
                horizontalLineTo(3.38f)
                curveToRelative(-0.73f, 0.0f, -1.22f, 0.76f, -0.92f, 1.42f)
                curveToRelative(0.43f, 0.92f, 1.07f, 1.71f, 1.86f, 2.31f)
                curveToRelative(0.38f, -0.16f, 0.74f, -0.38f, 1.06f, -0.63f)
                curveToRelative(0.35f, -0.29f, 0.87f, -0.29f, 1.23f, 0.0f)
                curveTo(7.28f, 18.63f, 8.1f, 19.0f, 9.0f, 19.0f)
                curveToRelative(0.9f, 0.0f, 1.72f, -0.37f, 2.39f, -0.91f)
                curveToRelative(0.35f, -0.28f, 0.87f, -0.28f, 1.22f, 0.0f)
                curveTo(13.28f, 18.63f, 14.1f, 19.0f, 15.0f, 19.0f)
                curveToRelative(0.9f, 0.0f, 1.72f, -0.37f, 2.39f, -0.91f)
                curveToRelative(0.35f, -0.29f, 0.87f, -0.28f, 1.23f, 0.0f)
                curveToRelative(0.32f, 0.26f, 0.67f, 0.48f, 1.06f, 0.63f)
                curveToRelative(0.79f, -0.6f, 1.43f, -1.39f, 1.86f, -2.31f)
                curveTo(21.84f, 15.76f, 21.35f, 15.0f, 20.62f, 15.0f)
                close()
                moveTo(22.0f, 22.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.87f, 0.0f, -1.73f, -0.24f, -2.53f, -0.7f)
                curveToRelative(-0.29f, -0.16f, -0.65f, -0.17f, -0.94f, 0.0f)
                curveToRelative(-1.59f, 0.9f, -3.47f, 0.9f, -5.06f, 0.0f)
                curveToRelative(-0.29f, -0.16f, -0.65f, -0.16f, -0.94f, 0.0f)
                curveToRelative(-1.59f, 0.9f, -3.47f, 0.9f, -5.06f, 0.0f)
                curveToRelative(-0.29f, -0.16f, -0.65f, -0.16f, -0.94f, 0.0f)
                curveTo(4.73f, 20.76f, 3.87f, 21.0f, 3.0f, 21.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(1.15f, 0.0f, 2.3f, -0.31f, 3.33f, -0.94f)
                curveToRelative(1.66f, 1.11f, 3.78f, 1.01f, 5.58f, 0.14f)
                curveToRelative(1.91f, 1.05f, 4.17f, 1.07f, 6.09f, 0.05f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.95f, 0.5f, 1.97f, 0.75f, 3.0f, 0.75f)
                horizontalLineToRelative(0.0f)
                curveTo(21.55f, 23.0f, 22.0f, 22.55f, 22.0f, 22.0f)
                close()
            }
        }
        return _sailing!!
    }

private var _sailing: ImageVector? = null

internal val Icons.Rounded.SatelliteAlt: ImageVector
    get() {
        if (_satelliteAlt != null) {
            return _satelliteAlt!!
        }
        _satelliteAlt = materialIcon(name = "Rounded.SatelliteAlt") {
            materialPath {
                moveTo(20.95f, 14.88f)
                curveToRelative(-0.4f, 3.18f, -2.89f, 5.67f, -6.07f, 6.07f)
                curveTo(14.37f, 21.01f, 14.0f, 21.44f, 14.0f, 21.94f)
                curveToRelative(0.0f, 0.04f, 0.0f, 0.08f, 0.01f, 0.12f)
                curveToRelative(0.07f, 0.55f, 0.57f, 0.94f, 1.12f, 0.87f)
                curveToRelative(4.09f, -0.51f, 7.3f, -3.72f, 7.81f, -7.81f)
                curveToRelative(0.06f, -0.55f, -0.33f, -1.05f, -0.88f, -1.11f)
                curveTo(21.51f, 13.94f, 21.01f, 14.33f, 20.95f, 14.88f)
                close()
                moveTo(18.84f, 15.26f)
                curveToRelative(0.14f, -0.53f, -0.18f, -1.08f, -0.72f, -1.22f)
                curveToRelative(-0.54f, -0.14f, -1.08f, 0.18f, -1.22f, 0.72f)
                curveToRelative(-0.27f, 1.05f, -1.09f, 1.87f, -2.15f, 2.15f)
                curveTo(14.3f, 17.03f, 14.0f, 17.43f, 14.0f, 17.88f)
                curveToRelative(0.0f, 0.08f, 0.01f, 0.17f, 0.03f, 0.25f)
                curveToRelative(0.14f, 0.53f, 0.69f, 0.85f, 1.22f, 0.72f)
                curveTo(17.02f, 18.38f, 18.39f, 17.01f, 18.84f, 15.26f)
                close()
                moveTo(21.8f, 4.12f)
                lineToRelative(-3.54f, -3.54f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineToRelative(-3.18f, 3.18f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                lineToRelative(1.24f, 1.24f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(11.55f, 7.3f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineTo(7.3f, 8.72f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                lineToRelative(1.24f, 1.24f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(6.6f, 12.25f)
                curveToRelative(-0.78f, -0.78f, -2.05f, -0.78f, -2.83f, 0.0f)
                lineToRelative(-3.18f, 3.18f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0.0f, 2.83f)
                lineToRelative(3.54f, 3.54f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(3.18f, -3.18f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineToRelative(-1.24f, -1.24f)
                lineToRelative(0.71f, -0.71f)
                lineToRelative(1.24f, 1.24f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(1.41f, -1.41f)
                curveToRelative(0.78f, -0.78f, 0.78f, -2.05f, 0.0f, -2.83f)
                lineTo(13.84f, 9.6f)
                lineToRelative(0.71f, -0.71f)
                lineToRelative(1.24f, 1.24f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(3.18f, -3.18f)
                curveTo(22.58f, 6.17f, 22.58f, 4.9f, 21.8f, 4.12f)
                close()
                moveTo(5.54f, 20.38f)
                lineTo(2.0f, 16.85f)
                lineToRelative(1.06f, -1.06f)
                lineToRelative(3.54f, 3.54f)
                lineTo(5.54f, 20.38f)
                close()
                moveTo(7.66f, 18.26f)
                lineToRelative(-3.54f, -3.54f)
                lineToRelative(1.06f, -1.06f)
                lineToRelative(3.54f, 3.54f)
                lineTo(7.66f, 18.26f)
                close()
                moveTo(17.2f, 8.72f)
                lineToRelative(-3.54f, -3.54f)
                lineToRelative(1.06f, -1.06f)
                lineToRelative(3.54f, 3.54f)
                lineTo(17.2f, 8.72f)
                close()
                moveTo(19.32f, 6.6f)
                lineToRelative(-3.54f, -3.54f)
                lineTo(16.85f, 2.0f)
                lineToRelative(3.54f, 3.54f)
                lineTo(19.32f, 6.6f)
                close()
            }
        }
        return _satelliteAlt!!
    }

private var _satelliteAlt: ImageVector? = null

internal val Icons.Rounded.Save: ImageVector
    get() {
        if (_save != null) {
            return _save!!
        }
        _save = materialIcon(name = "Rounded.Save") {
            materialPath {
                moveTo(17.59f, 3.59f)
                curveToRelative(-0.38f, -0.38f, -0.89f, -0.59f, -1.42f, -0.59f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(21.0f, 7.83f)
                curveToRelative(0.0f, -0.53f, -0.21f, -1.04f, -0.59f, -1.41f)
                lineToRelative(-2.82f, -2.83f)
                close()
                moveTo(12.0f, 19.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
                reflectiveCurveToRelative(1.34f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
                reflectiveCurveToRelative(-1.34f, 3.0f, -3.0f, 3.0f)
                close()
                moveTo(13.0f, 9.0f)
                lineTo(7.0f, 9.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _save!!
    }

private var _save: ImageVector? = null

internal val Icons.Rounded.Savings: ImageVector
    get() {
        if (_savings != null) {
            return _savings!!
        }
        _savings = materialIcon(name = "Rounded.Savings") {
            materialPath {
                moveTo(19.83f, 7.5f)
                lineToRelative(-2.27f, -2.27f)
                curveToRelative(0.07f, -0.42f, 0.18f, -0.81f, 0.32f, -1.15f)
                curveToRelative(0.11f, -0.26f, 0.15f, -0.56f, 0.09f, -0.87f)
                curveTo(17.84f, 2.49f, 17.14f, 1.99f, 16.4f, 2.0f)
                curveToRelative(-1.59f, 0.03f, -3.0f, 0.81f, -3.9f, 2.0f)
                lineToRelative(-5.0f, 0.0f)
                curveTo(4.46f, 4.0f, 2.0f, 6.46f, 2.0f, 9.5f)
                curveToRelative(0.0f, 2.25f, 1.37f, 7.48f, 2.08f, 10.04f)
                curveTo(4.32f, 20.4f, 5.11f, 21.0f, 6.01f, 21.0f)
                lineTo(8.0f, 21.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(0.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                lineToRelative(2.01f, 0.0f)
                curveToRelative(0.88f, 0.0f, 1.66f, -0.58f, 1.92f, -1.43f)
                lineToRelative(1.25f, -4.16f)
                lineToRelative(2.14f, -0.72f)
                curveToRelative(0.41f, -0.14f, 0.68f, -0.52f, 0.68f, -0.95f)
                verticalLineTo(8.5f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(19.83f)
                close()
                moveTo(12.0f, 9.0f)
                horizontalLineTo(9.0f)
                curveTo(8.45f, 9.0f, 8.0f, 8.55f, 8.0f, 8.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveTo(13.0f, 8.55f, 12.55f, 9.0f, 12.0f, 9.0f)
                close()
                moveTo(16.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(17.0f, 10.55f, 16.55f, 11.0f, 16.0f, 11.0f)
                close()
            }
        }
        return _savings!!
    }

private var _savings: ImageVector? = null

internal val Icons.Rounded.Scale: ImageVector
    get() {
        if (_scale != null) {
            return _scale!!
        }
        _scale = materialIcon(name = "Rounded.Scale") {
            materialPath {
                moveTo(16.0f, 21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                lineToRelative(3.43f, 0.0f)
                curveToRelative(0.87f, 0.0f, 1.58f, -0.75f, 1.5f, -1.62f)
                curveTo(21.34f, 14.18f, 17.4f, 11.68f, 14.0f, 11.0f)
                verticalLineTo(8.0f)
                curveToRelative(3.31f, -0.42f, 6.03f, -1.86f, 7.27f, -3.73f)
                curveTo(21.92f, 3.3f, 21.15f, 2.0f, 19.98f, 2.0f)
                horizontalLineTo(4.02f)
                curveTo(2.85f, 2.0f, 2.08f, 3.3f, 2.73f, 4.27f)
                curveTo(3.97f, 6.14f, 6.69f, 7.58f, 10.0f, 8.0f)
                lineToRelative(0.0f, 3.0f)
                curveToRelative(-3.4f, 0.68f, -7.34f, 3.18f, -7.93f, 9.38f)
                curveTo(1.99f, 21.25f, 2.7f, 22.0f, 3.57f, 22.0f)
                lineTo(7.0f, 22.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.13f)
                curveToRelative(0.93f, -6.83f, 6.65f, -7.2f, 7.87f, -7.2f)
                reflectiveCurveToRelative(6.94f, 0.37f, 7.87f, 7.2f)
                horizontalLineTo(17.0f)
                curveTo(16.45f, 20.0f, 16.0f, 20.45f, 16.0f, 21.0f)
                close()
                moveTo(11.5f, 21.94f)
                curveToRelative(-0.7f, -0.17f, -1.27f, -0.74f, -1.44f, -1.44f)
                curveToRelative(-0.18f, -0.74f, 0.06f, -1.44f, 0.53f, -1.91f)
                curveToRelative(0.55f, -0.55f, 2.91f, -1.57f, 4.33f, -2.15f)
                curveToRelative(0.41f, -0.17f, 0.82f, 0.24f, 0.65f, 0.65f)
                curveToRelative(-0.58f, 1.42f, -1.6f, 3.78f, -2.15f, 4.33f)
                curveTo(12.95f, 21.88f, 12.25f, 22.12f, 11.5f, 21.94f)
                close()
            }
        }
        return _scale!!
    }

private var _scale: ImageVector? = null

internal val Icons.Rounded.Schedule: ImageVector
    get() {
        if (_schedule != null) {
            return _schedule!!
        }
        _schedule = materialIcon(name = "Rounded.Schedule") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(11.78f, 7.0f)
                horizontalLineToRelative(-0.06f)
                curveToRelative(-0.4f, 0.0f, -0.72f, 0.32f, -0.72f, 0.72f)
                verticalLineToRelative(4.72f)
                curveToRelative(0.0f, 0.35f, 0.18f, 0.68f, 0.49f, 0.86f)
                lineToRelative(4.15f, 2.49f)
                curveToRelative(0.34f, 0.2f, 0.78f, 0.1f, 0.98f, -0.24f)
                curveToRelative(0.21f, -0.34f, 0.1f, -0.79f, -0.25f, -0.99f)
                lineToRelative(-3.87f, -2.3f)
                lineTo(12.5f, 7.72f)
                curveToRelative(0.0f, -0.4f, -0.32f, -0.72f, -0.72f, -0.72f)
                close()
            }
        }
        return _schedule!!
    }

private var _schedule: ImageVector? = null

internal val Icons.Rounded.School: ImageVector
    get() {
        if (_school != null) {
            return _school!!
        }
        _school = materialIcon(name = "Rounded.School") {
            materialPath {
                moveTo(5.0f, 13.18f)
                verticalLineToRelative(2.81f)
                curveToRelative(0.0f, 0.73f, 0.4f, 1.41f, 1.04f, 1.76f)
                lineToRelative(5.0f, 2.73f)
                curveToRelative(0.6f, 0.33f, 1.32f, 0.33f, 1.92f, 0.0f)
                lineToRelative(5.0f, -2.73f)
                curveToRelative(0.64f, -0.35f, 1.04f, -1.03f, 1.04f, -1.76f)
                verticalLineToRelative(-2.81f)
                lineToRelative(-6.04f, 3.3f)
                curveToRelative(-0.6f, 0.33f, -1.32f, 0.33f, -1.92f, 0.0f)
                lineTo(5.0f, 13.18f)
                close()
                moveTo(11.04f, 3.52f)
                lineToRelative(-8.43f, 4.6f)
                curveToRelative(-0.69f, 0.38f, -0.69f, 1.38f, 0.0f, 1.76f)
                lineToRelative(8.43f, 4.6f)
                curveToRelative(0.6f, 0.33f, 1.32f, 0.33f, 1.92f, 0.0f)
                lineTo(21.0f, 10.09f)
                lineTo(21.0f, 16.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(23.0f, 9.59f)
                curveToRelative(0.0f, -0.37f, -0.2f, -0.7f, -0.52f, -0.88f)
                lineToRelative(-9.52f, -5.19f)
                curveToRelative(-0.6f, -0.32f, -1.32f, -0.32f, -1.92f, 0.0f)
                close()
            }
        }
        return _school!!
    }

private var _school: ImageVector? = null

internal val Icons.Rounded.Science: ImageVector
    get() {
        if (_science != null) {
            return _science!!
        }
        _science = materialIcon(name = "Rounded.Science") {
            materialPath {
                moveTo(20.54f, 17.73f)
                lineTo(15.0f, 11.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                curveTo(7.45f, 3.0f, 7.0f, 3.45f, 7.0f, 4.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(-5.54f, 6.73f)
                curveTo(3.14f, 18.12f, 3.0f, 18.56f, 3.0f, 19.0f)
                curveToRelative(0.01f, 1.03f, 0.82f, 2.0f, 2.0f, 2.0f)
                horizontalLineTo(19.0f)
                curveToRelative(1.19f, 0.0f, 2.0f, -0.97f, 2.0f, -2.0f)
                curveTo(21.0f, 18.56f, 20.86f, 18.12f, 20.54f, 17.73f)
                close()
            }
        }
        return _science!!
    }

private var _science: ImageVector? = null

internal val Icons.Rounded.Sell: ImageVector
    get() {
        if (_sell != null) {
            return _sell!!
        }
        _sell = materialIcon(name = "Rounded.Sell") {
            materialPath {
                moveTo(21.41f, 11.41f)
                lineToRelative(-8.83f, -8.83f)
                curveTo(12.21f, 2.21f, 11.7f, 2.0f, 11.17f, 2.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 2.0f, 2.0f, 2.9f, 2.0f, 4.0f)
                verticalLineToRelative(7.17f)
                curveToRelative(0.0f, 0.53f, 0.21f, 1.04f, 0.59f, 1.41f)
                lineToRelative(8.83f, 8.83f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0.0f)
                lineToRelative(7.17f, -7.17f)
                curveTo(22.2f, 13.46f, 22.2f, 12.2f, 21.41f, 11.41f)
                close()
                moveTo(6.5f, 8.0f)
                curveTo(5.67f, 8.0f, 5.0f, 7.33f, 5.0f, 6.5f)
                reflectiveCurveTo(5.67f, 5.0f, 6.5f, 5.0f)
                reflectiveCurveTo(8.0f, 5.67f, 8.0f, 6.5f)
                reflectiveCurveTo(7.33f, 8.0f, 6.5f, 8.0f)
                close()
            }
        }
        return _sell!!
    }

private var _sell: ImageVector? = null

internal val Icons.Rounded.SentimentSatisfied: ImageVector
    get() {
        if (_sentimentSatisfied != null) {
            return _sentimentSatisfied!!
        }
        _sentimentSatisfied = materialIcon(name = "Rounded.SentimentSatisfied") {
            materialPath {
                moveTo(15.5f, 9.5f)
                moveToRelative(-1.5f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, 3.0f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, -3.0f, 0.0f)
            }
            materialPath {
                moveTo(8.5f, 9.5f)
                moveToRelative(-1.5f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, 3.0f, 0.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, true, true, -3.0f, 0.0f)
            }
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(16.41f, 13.89f)
                curveToRelative(-0.35f, -0.22f, -0.82f, -0.11f, -1.03f, 0.24f)
                curveToRelative(-0.74f, 1.17f, -2.0f, 1.87f, -3.38f, 1.87f)
                reflectiveCurveToRelative(-2.64f, -0.7f, -3.38f, -1.88f)
                curveToRelative(-0.22f, -0.35f, -0.68f, -0.46f, -1.03f, -0.24f)
                curveToRelative(-0.35f, 0.22f, -0.46f, 0.68f, -0.24f, 1.03f)
                curveTo(8.37f, 16.54f, 10.1f, 17.5f, 12.0f, 17.5f)
                reflectiveCurveToRelative(3.63f, -0.97f, 4.65f, -2.58f)
                curveToRelative(0.22f, -0.35f, 0.11f, -0.81f, -0.24f, -1.03f)
                close()
            }
        }
        return _sentimentSatisfied!!
    }

private var _sentimentSatisfied: ImageVector? = null

internal val Icons.Rounded.SetMeal: ImageVector
    get() {
        if (_setMeal != null) {
            return _setMeal!!
        }
        _setMeal = materialIcon(name = "Rounded.SetMeal") {
            materialPath {
                moveTo(20.3f, 17.6f)
                lineTo(3.83f, 18.46f)
                curveToRelative(-0.41f, 0.02f, -0.77f, -0.3f, -0.79f, -0.71f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.02f, -0.41f, 0.3f, -0.77f, 0.71f, -0.79f)
                lineToRelative(16.48f, -0.86f)
                curveToRelative(0.41f, -0.02f, 0.77f, 0.3f, 0.79f, 0.71f)
                verticalLineToRelative(0.0f)
                curveTo(21.04f, 17.22f, 20.72f, 17.58f, 20.3f, 17.6f)
                close()
                moveTo(20.25f, 19.48f)
                horizontalLineTo(3.75f)
                curveTo(3.34f, 19.48f, 3.0f, 19.82f, 3.0f, 20.23f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.0f, 0.41f, 0.34f, 0.75f, 0.75f, 0.75f)
                horizontalLineToRelative(16.5f)
                curveToRelative(0.41f, 0.0f, 0.75f, -0.34f, 0.75f, -0.75f)
                lineToRelative(0.0f, 0.0f)
                curveTo(21.0f, 19.82f, 20.66f, 19.48f, 20.25f, 19.48f)
                close()
                moveTo(22.0f, 5.0f)
                verticalLineToRelative(7.0f)
                curveToRelative(0.0f, 1.1f, -0.9f, 2.0f, -2.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(16.0f)
                curveTo(21.1f, 3.0f, 22.0f, 3.9f, 22.0f, 5.0f)
                close()
                moveTo(19.12f, 6.09f)
                curveToRelative(-1.25f, 0.27f, -2.19f, 1.11f, -2.33f, 2.14f)
                curveTo(16.15f, 7.5f, 14.06f, 5.5f, 10.25f, 5.5f)
                curveToRelative(-3.44f, 0.0f, -5.48f, 1.63f, -6.31f, 2.49f)
                curveToRelative(-0.28f, 0.29f, -0.28f, 0.74f, 0.0f, 1.03f)
                curveToRelative(0.83f, 0.86f, 2.87f, 2.49f, 6.31f, 2.49f)
                curveToRelative(3.81f, 0.0f, 5.9f, -2.0f, 6.54f, -2.73f)
                curveToRelative(0.14f, 1.02f, 1.08f, 1.86f, 2.33f, 2.14f)
                curveToRelative(0.46f, 0.1f, 0.88f, -0.28f, 0.88f, -0.74f)
                verticalLineTo(6.84f)
                curveTo(20.0f, 6.37f, 19.57f, 5.99f, 19.12f, 6.09f)
                close()
            }
        }
        return _setMeal!!
    }

private var _setMeal: ImageVector? = null

internal val Icons.Rounded.Shield: ImageVector
    get() {
        if (_shield != null) {
            return _shield!!
        }
        _shield = materialIcon(name = "Rounded.Shield") {
            materialPath {
                moveTo(11.3f, 2.26f)
                lineToRelative(-6.0f, 2.25f)
                curveTo(4.52f, 4.81f, 4.0f, 5.55f, 4.0f, 6.39f)
                verticalLineToRelative(4.7f)
                curveToRelative(0.0f, 4.83f, 3.13f, 9.37f, 7.43f, 10.75f)
                curveToRelative(0.37f, 0.12f, 0.77f, 0.12f, 1.14f, 0.0f)
                curveToRelative(4.3f, -1.38f, 7.43f, -5.91f, 7.43f, -10.75f)
                verticalLineToRelative(-4.7f)
                curveToRelative(0.0f, -0.83f, -0.52f, -1.58f, -1.3f, -1.87f)
                lineToRelative(-6.0f, -2.25f)
                curveTo(12.25f, 2.09f, 11.75f, 2.09f, 11.3f, 2.26f)
                close()
            }
        }
        return _shield!!
    }

private var _shield: ImageVector? = null

internal val Icons.Rounded.ShoppingBag: ImageVector
    get() {
        if (_shoppingBag != null) {
            return _shoppingBag!!
        }
        _shoppingBag = materialIcon(name = "Rounded.ShoppingBag") {
            materialPath {
                moveTo(18.0f, 6.0f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(0.0f, -2.21f, -1.79f, -4.0f, -4.0f, -4.0f)
                reflectiveCurveTo(8.0f, 3.79f, 8.0f, 6.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 6.0f, 4.0f, 6.9f, 4.0f, 8.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                curveTo(20.0f, 6.9f, 19.1f, 6.0f, 18.0f, 6.0f)
                close()
                moveTo(10.0f, 10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(10.0f, 4.9f, 10.9f, 4.0f, 12.0f, 4.0f)
                close()
                moveTo(16.0f, 10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(10.0f)
                close()
            }
        }
        return _shoppingBag!!
    }

private var _shoppingBag: ImageVector? = null

internal val Icons.Rounded.ShoppingBasket: ImageVector
    get() {
        if (_shoppingBasket != null) {
            return _shoppingBasket!!
        }
        _shoppingBasket = materialIcon(name = "Rounded.ShoppingBasket") {
            materialPath {
                moveTo(22.0f, 9.0f)
                horizontalLineToRelative(-4.79f)
                lineToRelative(-4.39f, -6.57f)
                curveToRelative(-0.4f, -0.59f, -1.27f, -0.59f, -1.66f, 0.0f)
                lineTo(6.77f, 9.0f)
                horizontalLineTo(2.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.09f, 0.01f, 0.18f, 0.04f, 0.27f)
                lineToRelative(2.54f, 9.27f)
                curveToRelative(0.23f, 0.84f, 1.0f, 1.46f, 1.92f, 1.46f)
                horizontalLineToRelative(13.0f)
                curveToRelative(0.92f, 0.0f, 1.69f, -0.62f, 1.93f, -1.46f)
                lineToRelative(2.54f, -9.27f)
                lineTo(23.0f, 10.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                close()
                moveTo(11.99f, 4.79f)
                lineTo(14.8f, 9.0f)
                horizontalLineTo(9.18f)
                lineToRelative(2.81f, -4.21f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                reflectiveCurveToRelative(0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveToRelative(-0.9f, 2.0f, -2.0f, 2.0f)
                close()
            }
        }
        return _shoppingBasket!!
    }

private var _shoppingBasket: ImageVector? = null

internal val Icons.Rounded.Shuffle: ImageVector
    get() {
        if (_shuffle != null) {
            return _shuffle!!
        }
        _shuffle = materialIcon(name = "Rounded.Shuffle") {
            materialPath {
                moveTo(10.59f, 9.17f)
                lineTo(6.12f, 4.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.46f, 4.46f)
                lineToRelative(1.42f, -1.4f)
                close()
                moveTo(15.35f, 4.85f)
                lineToRelative(1.19f, 1.19f)
                lineTo(4.7f, 17.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(17.96f, 7.46f)
                lineToRelative(1.19f, 1.19f)
                curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.36f)
                lineTo(20.0f, 4.5f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                horizontalLineToRelative(-3.79f)
                curveToRelative(-0.45f, 0.0f, -0.67f, 0.54f, -0.36f, 0.85f)
                close()
                moveTo(14.83f, 13.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(3.13f, 3.13f)
                lineToRelative(-1.2f, 1.2f)
                curveToRelative(-0.31f, 0.31f, -0.09f, 0.85f, 0.36f, 0.85f)
                horizontalLineToRelative(3.79f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                verticalLineToRelative(-3.79f)
                curveToRelative(0.0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
                lineToRelative(-1.19f, 1.19f)
                lineToRelative(-3.13f, -3.14f)
                close()
            }
        }
        return _shuffle!!
    }

private var _shuffle: ImageVector? = null

internal val Icons.Rounded.Signpost: ImageVector
    get() {
        if (_signpost != null) {
            return _signpost!!
        }
        _signpost = materialIcon(name = "Rounded.Signpost") {
            materialPath {
                moveTo(13.0f, 10.0f)
                lineToRelative(5.0f, 0.0f)
                lineToRelative(3.0f, -3.0f)
                lineToRelative(-3.0f, -3.0f)
                lineToRelative(-5.0f, 0.0f)
                lineToRelative(0.0f, -2.0f)
                lineToRelative(-2.0f, 0.0f)
                lineToRelative(0.0f, 2.0f)
                lineToRelative(-7.0f, 0.0f)
                lineToRelative(0.0f, 6.0f)
                lineToRelative(7.0f, 0.0f)
                lineToRelative(0.0f, 2.0f)
                lineToRelative(-5.0f, 0.0f)
                lineToRelative(-3.0f, 3.0f)
                lineToRelative(3.0f, 3.0f)
                lineToRelative(5.0f, 0.0f)
                lineToRelative(0.0f, 4.0f)
                lineToRelative(2.0f, 0.0f)
                lineToRelative(0.0f, -4.0f)
                lineToRelative(7.0f, 0.0f)
                lineToRelative(0.0f, -6.0f)
                lineToRelative(-7.0f, 0.0f)
                close()
            }
        }
        return _signpost!!
    }

private var _signpost: ImageVector? = null

internal val Icons.Rounded.Smartphone: ImageVector
    get() {
        if (_smartphone != null) {
            return _smartphone!!
        }
        _smartphone = materialIcon(name = "Rounded.Smartphone") {
            materialPath {
                moveTo(17.0f, 1.01f)
                lineTo(7.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -1.99f, -2.0f, -1.99f)
                close()
                moveTo(17.0f, 19.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(14.0f)
                close()
            }
        }
        return _smartphone!!
    }

private var _smartphone: ImageVector? = null

internal val Icons.Rounded.SmartToy: ImageVector
    get() {
        if (_smartToy != null) {
            return _smartToy!!
        }
        _smartToy = materialIcon(name = "Rounded.SmartToy") {
            materialPath {
                moveTo(20.0f, 9.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 5.0f, 4.0f, 5.9f, 4.0f, 7.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                curveTo(23.0f, 10.34f, 21.66f, 9.0f, 20.0f, 9.0f)
                close()
                moveTo(7.5f, 11.5f)
                curveTo(7.5f, 10.67f, 8.17f, 10.0f, 9.0f, 10.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(9.83f, 13.0f, 9.0f, 13.0f)
                reflectiveCurveTo(7.5f, 12.33f, 7.5f, 11.5f)
                close()
                moveTo(15.0f, 17.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveTo(16.0f, 16.55f, 15.55f, 17.0f, 15.0f, 17.0f)
                close()
                moveTo(15.0f, 13.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(14.17f, 10.0f, 15.0f, 10.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(15.83f, 13.0f, 15.0f, 13.0f)
                close()
            }
        }
        return _smartToy!!
    }

private var _smartToy: ImageVector? = null

internal val Icons.Rounded.SoupKitchen: ImageVector
    get() {
        if (_soupKitchen != null) {
            return _soupKitchen!!
        }
        _soupKitchen = materialIcon(name = "Rounded.SoupKitchen") {
            materialPath {
                moveTo(6.15f, 13.5f)
                curveToRelative(-0.46f, 0.0f, -0.8f, -0.42f, -0.71f, -0.87f)
                curveTo(5.48f, 12.45f, 5.5f, 12.24f, 5.5f, 12.0f)
                curveToRelative(0.0f, -1.0f, -1.0f, -2.85f, -1.0f, -3.62f)
                curveToRelative(0.0f, -0.29f, 0.03f, -0.59f, 0.17f, -0.93f)
                curveTo(4.78f, 7.18f, 5.04f, 7.0f, 5.34f, 7.0f)
                curveToRelative(0.45f, 0.0f, 0.8f, 0.42f, 0.71f, 0.86f)
                curveTo(6.01f, 8.04f, 6.0f, 8.21f, 6.0f, 8.38f)
                curveTo(6.0f, 9.15f, 7.0f, 11.0f, 7.0f, 12.0f)
                curveToRelative(0.0f, 0.42f, -0.08f, 0.76f, -0.17f, 1.01f)
                curveTo(6.73f, 13.31f, 6.46f, 13.5f, 6.15f, 13.5f)
                close()
                moveTo(12.65f, 13.5f)
                curveToRelative(0.31f, 0.0f, 0.58f, -0.19f, 0.68f, -0.49f)
                curveToRelative(0.09f, -0.25f, 0.17f, -0.59f, 0.17f, -1.01f)
                curveToRelative(0.0f, -1.0f, -1.0f, -2.85f, -1.0f, -3.62f)
                curveToRelative(0.0f, -0.17f, 0.01f, -0.34f, 0.04f, -0.51f)
                curveTo(12.63f, 7.42f, 12.29f, 7.0f, 11.84f, 7.0f)
                curveToRelative(-0.29f, 0.0f, -0.56f, 0.18f, -0.67f, 0.45f)
                curveTo(11.03f, 7.79f, 11.0f, 8.08f, 11.0f, 8.38f)
                curveTo(11.0f, 9.15f, 12.0f, 11.0f, 12.0f, 12.0f)
                curveToRelative(0.0f, 0.24f, -0.02f, 0.45f, -0.06f, 0.63f)
                curveTo(11.85f, 13.08f, 12.19f, 13.5f, 12.65f, 13.5f)
                close()
                moveTo(9.4f, 13.5f)
                curveToRelative(0.31f, 0.0f, 0.58f, -0.19f, 0.68f, -0.49f)
                curveToRelative(0.09f, -0.25f, 0.17f, -0.59f, 0.17f, -1.01f)
                curveToRelative(0.0f, -1.0f, -1.0f, -2.85f, -1.0f, -3.62f)
                curveToRelative(0.0f, -0.17f, 0.01f, -0.34f, 0.04f, -0.51f)
                curveTo(9.38f, 7.42f, 9.04f, 7.0f, 8.59f, 7.0f)
                curveTo(8.29f, 7.0f, 8.03f, 7.18f, 7.92f, 7.45f)
                curveTo(7.78f, 7.79f, 7.75f, 8.08f, 7.75f, 8.38f)
                curveToRelative(0.0f, 0.77f, 1.0f, 2.63f, 1.0f, 3.62f)
                curveToRelative(0.0f, 0.24f, -0.02f, 0.45f, -0.06f, 0.63f)
                curveTo(8.6f, 13.08f, 8.94f, 13.5f, 9.4f, 13.5f)
                close()
                moveTo(20.46f, 6.37f)
                curveToRelative(0.57f, 0.07f, 1.08f, -0.34f, 1.12f, -0.91f)
                curveTo(21.59f, 5.28f, 21.6f, 5.12f, 21.6f, 5.0f)
                curveToRelative(0.0f, -1.65f, -1.35f, -3.0f, -3.0f, -3.0f)
                curveToRelative(-1.54f, 0.0f, -2.81f, 1.16f, -2.98f, 2.65f)
                lineTo(14.53f, 15.0f)
                horizontalLineTo(3.99f)
                curveToRelative(-0.6f, 0.0f, -1.07f, 0.54f, -0.98f, 1.14f)
                curveTo(3.54f, 19.46f, 6.39f, 22.0f, 9.75f, 22.0f)
                curveToRelative(3.48f, 0.0f, 6.34f, -2.73f, 6.71f, -6.23f)
                lineToRelative(1.15f, -10.87f)
                curveTo(17.66f, 4.39f, 18.08f, 4.0f, 18.6f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 0.07f, -0.01f, 0.18f, -0.01f, 0.31f)
                curveTo(19.55f, 5.84f, 19.93f, 6.3f, 20.46f, 6.37f)
                lineTo(20.46f, 6.37f)
                close()
            }
        }
        return _soupKitchen!!
    }

private var _soupKitchen: ImageVector? = null

internal val Icons.Rounded.SouthEast: ImageVector
    get() {
        if (_southEast != null) {
            return _southEast!!
        }
        _southEast = materialIcon(name = "Rounded.SouthEast") {
            materialPath {
                moveTo(18.0f, 9.0f)
                lineTo(18.0f, 9.0f)
                curveToRelative(-0.56f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(5.59f)
                lineTo(6.12f, 4.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(15.59f, 17.0f)
                horizontalLineTo(10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineTo(18.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineTo(18.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(10.0f)
                curveTo(19.0f, 9.45f, 18.55f, 9.0f, 18.0f, 9.0f)
                close()
            }
        }
        return _southEast!!
    }

private var _southEast: ImageVector? = null

internal val Icons.Rounded.Spa: ImageVector
    get() {
        if (_spa != null) {
            return _spa!!
        }
        _spa = materialIcon(name = "Rounded.Spa") {
            materialPath {
                moveTo(15.49f, 9.63f)
                curveToRelative(-0.16f, -2.42f, -1.03f, -4.79f, -2.64f, -6.76f)
                curveToRelative(-0.41f, -0.5f, -1.16f, -0.5f, -1.57f, 0.0f)
                curveToRelative(-1.65f, 1.98f, -2.57f, 4.35f, -2.77f, 6.76f)
                curveToRelative(1.28f, 0.68f, 2.46f, 1.56f, 3.49f, 2.63f)
                curveToRelative(1.03f, -1.06f, 2.21f, -1.94f, 3.49f, -2.63f)
                close()
                moveTo(8.99f, 12.28f)
                curveToRelative(-0.14f, -0.1f, -0.3f, -0.19f, -0.45f, -0.29f)
                curveToRelative(0.15f, 0.11f, 0.31f, 0.19f, 0.45f, 0.29f)
                close()
                moveTo(15.41f, 12.03f)
                curveToRelative(-0.13f, 0.09f, -0.27f, 0.16f, -0.4f, 0.26f)
                curveToRelative(0.13f, -0.1f, 0.27f, -0.17f, 0.4f, -0.26f)
                close()
                moveTo(12.0f, 15.45f)
                curveToRelative(-1.95f, -2.97f, -5.14f, -5.03f, -8.83f, -5.39f)
                curveToRelative(-0.64f, -0.06f, -1.17f, 0.47f, -1.11f, 1.11f)
                curveToRelative(0.45f, 4.8f, 3.65f, 8.78f, 7.98f, 10.33f)
                curveToRelative(0.63f, 0.23f, 1.29f, 0.4f, 1.97f, 0.51f)
                curveToRelative(0.68f, -0.12f, 1.33f, -0.29f, 1.97f, -0.51f)
                curveToRelative(4.33f, -1.55f, 7.53f, -5.52f, 7.98f, -10.33f)
                curveToRelative(0.06f, -0.64f, -0.48f, -1.17f, -1.11f, -1.11f)
                curveToRelative(-3.71f, 0.36f, -6.9f, 2.42f, -8.85f, 5.39f)
                close()
            }
        }
        return _spa!!
    }

private var _spa: ImageVector? = null

internal val Icons.Rounded.Speed: ImageVector
    get() {
        if (_speed != null) {
            return _speed!!
        }
        _speed = materialIcon(name = "Rounded.Speed") {
            materialPath {
                moveTo(19.46f, 10.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.07f, 1.0f)
                arcToRelative(7.55f, 7.55f, 0.0f, false, true, 0.52f, 1.81f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -0.69f, 4.73f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, true, -0.89f, 0.53f)
                horizontalLineTo(5.68f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, true, -0.89f, -0.54f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 13.0f, 6.06f)
                arcToRelative(7.69f, 7.69f, 0.0f, false, true, 2.11f, 0.56f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.0f, -0.07f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.17f, -1.76f)
                arcTo(10.0f, 10.0f, 0.0f, false, false, 3.35f, 19.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 1.72f, 1.0f)
                horizontalLineToRelative(13.85f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 1.74f, -1.0f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, false, 0.55f, -8.89f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.75f, -0.11f)
                close()
            }
            materialPath {
                moveTo(10.59f, 12.59f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 2.83f, 2.83f)
                lineToRelative(5.66f, -8.49f)
                close()
            }
        }
        return _speed!!
    }

private var _speed: ImageVector? = null

internal val Icons.Rounded.SportsBar: ImageVector
    get() {
        if (_sportsBar != null) {
            return _sportsBar!!
        }
        _sportsBar = materialIcon(name = "Rounded.SportsBar") {
            materialPath {
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-1.56f)
                curveToRelative(0.33f, -0.55f, 0.53f, -1.18f, 0.55f, -1.86f)
                curveToRelative(0.04f, -1.03f, -0.43f, -1.99f, -1.16f, -2.71f)
                curveToRelative(-1.54f, -1.54f, -2.74f, -1.56f, -3.82f, -1.29f)
                curveTo(12.2f, 2.45f, 11.16f, 2.02f, 10.0f, 2.02f)
                curveToRelative(-1.89f, 0.0f, -3.51f, 1.11f, -4.27f, 2.71f)
                curveTo(4.15f, 5.26f, 3.0f, 6.74f, 3.0f, 8.5f)
                curveToRelative(0.0f, 1.86f, 1.28f, 3.41f, 3.0f, 3.86f)
                lineTo(6.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(0.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-6.0f)
                curveTo(21.0f, 9.9f, 20.1f, 9.0f, 19.0f, 9.0f)
                close()
                moveTo(7.0f, 10.5f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                curveToRelative(0.0f, -0.85f, 0.55f, -1.6f, 1.37f, -1.88f)
                lineToRelative(0.8f, -0.27f)
                lineToRelative(0.36f, -0.76f)
                curveTo(8.0f, 4.62f, 8.94f, 4.02f, 10.0f, 4.02f)
                curveToRelative(0.79f, 0.0f, 1.39f, 0.35f, 1.74f, 0.65f)
                lineToRelative(0.78f, 0.65f)
                curveToRelative(0.0f, 0.0f, 0.64f, -0.32f, 1.47f, -0.32f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                curveToRelative(0.0f, 0.0f, -3.0f, 0.0f, -3.0f, 0.0f)
                curveTo(9.67f, 7.0f, 9.15f, 10.5f, 7.0f, 10.5f)
                close()
                moveTo(19.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
        return _sportsBar!!
    }

private var _sportsBar: ImageVector? = null

internal val Icons.Rounded.SportsEsports: ImageVector
    get() {
        if (_sportsEsports != null) {
            return _sportsEsports!!
        }
        _sportsEsports = materialIcon(name = "Rounded.SportsEsports") {
            materialPath {
                moveTo(21.58f, 16.09f)
                lineToRelative(-1.09f, -7.66f)
                curveTo(20.21f, 6.46f, 18.52f, 5.0f, 16.53f, 5.0f)
                horizontalLineTo(7.47f)
                curveTo(5.48f, 5.0f, 3.79f, 6.46f, 3.51f, 8.43f)
                lineToRelative(-1.09f, 7.66f)
                curveTo(2.2f, 17.63f, 3.39f, 19.0f, 4.94f, 19.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.68f, 0.0f, 1.32f, -0.27f, 1.8f, -0.75f)
                lineTo(9.0f, 16.0f)
                horizontalLineToRelative(6.0f)
                lineToRelative(2.25f, 2.25f)
                curveToRelative(0.48f, 0.48f, 1.13f, 0.75f, 1.8f, 0.75f)
                horizontalLineToRelative(0.0f)
                curveTo(20.61f, 19.0f, 21.8f, 17.63f, 21.58f, 16.09f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(15.0f, 10.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(16.0f, 9.55f, 15.55f, 10.0f, 15.0f, 10.0f)
                close()
                moveTo(17.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(18.0f, 12.55f, 17.55f, 13.0f, 17.0f, 13.0f)
                close()
            }
        }
        return _sportsEsports!!
    }

private var _sportsEsports: ImageVector? = null

internal val Icons.Rounded.SportsFootball: ImageVector
    get() {
        if (_sportsFootball != null) {
            return _sportsFootball!!
        }
        _sportsFootball = materialIcon(name = "Rounded.SportsFootball") {
            materialPath {
                moveTo(3.02f, 15.62f)
                curveToRelative(-0.08f, 2.42f, 0.32f, 4.34f, 0.67f, 4.69f)
                reflectiveCurveToRelative(2.28f, 0.76f, 4.69f, 0.67f)
                lineTo(3.02f, 15.62f)
                close()
            }
            materialPath {
                moveTo(13.08f, 3.28f)
                curveTo(10.75f, 3.7f, 8.29f, 4.62f, 6.46f, 6.46f)
                reflectiveCurveToRelative(-2.76f, 4.29f, -3.18f, 6.62f)
                lineToRelative(7.63f, 7.63f)
                curveToRelative(2.34f, -0.41f, 4.79f, -1.34f, 6.62f, -3.18f)
                reflectiveCurveToRelative(2.76f, -4.29f, 3.18f, -6.62f)
                lineTo(13.08f, 3.28f)
                close()
                moveTo(14.8f, 10.6f)
                lineToRelative(-4.2f, 4.2f)
                curveToRelative(-0.39f, 0.39f, -1.01f, 0.39f, -1.4f, 0.0f)
                reflectiveCurveToRelative(-0.39f, -1.01f, 0.0f, -1.4f)
                lineToRelative(4.2f, -4.2f)
                curveToRelative(0.39f, -0.39f, 1.01f, -0.39f, 1.4f, 0.0f)
                reflectiveCurveTo(15.19f, 10.21f, 14.8f, 10.6f)
                close()
            }
            materialPath {
                moveTo(20.98f, 8.38f)
                curveToRelative(0.08f, -2.42f, -0.32f, -4.34f, -0.67f, -4.69f)
                reflectiveCurveToRelative(-2.28f, -0.76f, -4.69f, -0.67f)
                lineTo(20.98f, 8.38f)
                close()
            }
        }
        return _sportsFootball!!
    }

private var _sportsFootball: ImageVector? = null

internal val Icons.Rounded.SportsScore: ImageVector
    get() {
        if (_sportsScore != null) {
            return _sportsScore!!
        }
        _sportsScore = materialIcon(name = "Rounded.SportsScore") {
            materialPath {
                moveTo(11.0f, 6.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                close()
                moveTo(15.0f, 4.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(9.0f, 14.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(19.0f, 10.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(19.0f)
                close()
                moveTo(19.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(19.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(19.0f, 4.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(4.0f)
                close()
                moveTo(13.0f, 8.0f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(13.0f)
                close()
                moveTo(7.0f, 10.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveTo(5.45f, 4.0f, 5.0f, 4.45f, 5.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(11.0f, 10.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(11.0f)
                close()
                moveTo(9.0f, 8.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(9.0f)
                close()
                moveTo(13.0f, 10.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(10.0f)
                close()
                moveTo(15.0f, 6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(15.0f)
                close()
            }
        }
        return _sportsScore!!
    }

private var _sportsScore: ImageVector? = null

internal val Icons.Rounded.SportsVolleyball: ImageVector
    get() {
        if (_sportsVolleyball != null) {
            return _sportsVolleyball!!
        }
        _sportsVolleyball = materialIcon(name = "Rounded.SportsVolleyball") {
            materialPath {
                moveTo(6.0f, 4.01f)
                curveTo(3.58f, 5.84f, 2.0f, 8.73f, 2.0f, 12.0f)
                curveToRelative(0.0f, 1.46f, 0.32f, 2.85f, 0.89f, 4.11f)
                lineTo(6.0f, 14.31f)
                verticalLineTo(4.01f)
                close()
            }
            materialPath {
                moveTo(11.0f, 11.42f)
                verticalLineTo(2.05f)
                curveTo(9.94f, 2.16f, 8.93f, 2.43f, 8.0f, 2.84f)
                verticalLineToRelative(10.32f)
                lineTo(11.0f, 11.42f)
                close()
            }
            materialPath {
                moveTo(12.0f, 13.15f)
                lineToRelative(-8.11f, 4.68f)
                curveToRelative(0.61f, 0.84f, 1.34f, 1.59f, 2.18f, 2.2f)
                lineTo(15.0f, 14.89f)
                lineTo(12.0f, 13.15f)
                close()
            }
            materialPath {
                moveTo(13.0f, 7.96f)
                verticalLineToRelative(3.46f)
                lineToRelative(8.11f, 4.68f)
                curveToRelative(0.42f, -0.93f, 0.7f, -1.93f, 0.82f, -2.98f)
                lineTo(13.0f, 7.96f)
                close()
            }
            materialPath {
                moveTo(8.07f, 21.2f)
                curveTo(9.28f, 21.71f, 10.6f, 22.0f, 12.0f, 22.0f)
                curveToRelative(3.34f, 0.0f, 6.29f, -1.65f, 8.11f, -4.16f)
                lineTo(17.0f, 16.04f)
                lineTo(8.07f, 21.2f)
                close()
            }
            materialPath {
                moveTo(21.92f, 10.81f)
                curveToRelative(-0.55f, -4.63f, -4.26f, -8.3f, -8.92f, -8.76f)
                verticalLineToRelative(3.6f)
                lineTo(21.92f, 10.81f)
                close()
            }
        }
        return _sportsVolleyball!!
    }

private var _sportsVolleyball: ImageVector? = null

internal val Icons.Rounded.Storage: ImageVector
    get() {
        if (_storage != null) {
            return _storage!!
        }
        _storage = materialIcon(name = "Rounded.Storage") {
            materialPath {
                moveTo(4.0f, 20.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 16.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(4.0f, 17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(2.0f, 6.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                close()
                moveTo(6.0f, 7.0f)
                lineTo(4.0f, 7.0f)
                lineTo(4.0f, 5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(4.0f, 14.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
                lineTo(4.0f, 10.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
                close()
                moveTo(4.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                close()
            }
        }
        return _storage!!
    }

private var _storage: ImageVector? = null

internal val Icons.Rounded.Storefront: ImageVector
    get() {
        if (_storefront != null) {
            return _storefront!!
        }
        _storefront = materialIcon(name = "Rounded.Storefront") {
            materialPath {
                moveTo(21.9f, 7.89f)
                lineToRelative(-1.05f, -3.37f)
                curveToRelative(-0.22f, -0.9f, -1.0f, -1.52f, -1.91f, -1.52f)
                horizontalLineTo(5.05f)
                curveToRelative(-0.9f, 0.0f, -1.69f, 0.63f, -1.9f, 1.52f)
                lineTo(2.1f, 7.89f)
                curveTo(1.64f, 9.86f, 2.95f, 11.0f, 3.0f, 11.06f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-7.94f)
                curveTo(22.12f, 9.94f, 22.09f, 8.65f, 21.9f, 7.89f)
                close()
                moveTo(13.0f, 5.0f)
                horizontalLineToRelative(1.96f)
                lineToRelative(0.54f, 3.52f)
                curveTo(15.59f, 9.23f, 15.11f, 10.0f, 14.22f, 10.0f)
                curveTo(13.55f, 10.0f, 13.0f, 9.41f, 13.0f, 8.69f)
                verticalLineTo(5.0f)
                close()
                moveTo(6.44f, 8.86f)
                curveTo(6.36f, 9.51f, 5.84f, 10.0f, 5.23f, 10.0f)
                curveTo(4.3f, 10.0f, 3.88f, 9.03f, 4.04f, 8.36f)
                lineTo(5.05f, 5.0f)
                horizontalLineToRelative(1.97f)
                lineTo(6.44f, 8.86f)
                close()
                moveTo(11.0f, 8.69f)
                curveTo(11.0f, 9.41f, 10.45f, 10.0f, 9.71f, 10.0f)
                curveToRelative(-0.75f, 0.0f, -1.3f, -0.7f, -1.22f, -1.48f)
                lineTo(9.04f, 5.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(8.69f)
                close()
                moveTo(18.77f, 10.0f)
                curveToRelative(-0.61f, 0.0f, -1.14f, -0.49f, -1.21f, -1.14f)
                lineTo(16.98f, 5.0f)
                lineToRelative(1.93f, -0.01f)
                lineToRelative(1.05f, 3.37f)
                curveTo(20.12f, 9.03f, 19.71f, 10.0f, 18.77f, 10.0f)
                close()
            }
        }
        return _storefront!!
    }

private var _storefront: ImageVector? = null

internal val Icons.Rounded.Straighten: ImageVector
    get() {
        if (_straighten != null) {
            return _straighten!!
        }
        _straighten = materialIcon(name = "Rounded.Straighten") {
            materialPath {
                moveTo(21.0f, 6.0f)
                lineTo(3.0f, 6.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(23.0f, 8.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 16.0f)
                lineTo(4.0f, 16.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(3.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(7.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(15.0f, 8.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(19.0f, 8.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _straighten!!
    }

private var _straighten: ImageVector? = null

internal val Icons.Rounded.SubdirectoryArrowRight: ImageVector
    get() {
        if (_subdirectoryArrowRight != null) {
            return _subdirectoryArrowRight!!
        }
        _subdirectoryArrowRight = materialIcon(name = "Rounded.SubdirectoryArrowRight") {
            materialPath {
                moveTo(18.29f, 15.71f)
                lineToRelative(-4.58f, 4.58f)
                curveToRelative(-0.39f, 0.39f, -1.03f, 0.39f, -1.42f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.03f, 0.0f, -1.42f)
                lineTo(15.17f, 16.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(9.0f)
                horizontalLineToRelative(9.17f)
                lineToRelative(-2.88f, -2.87f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.03f, 0.0f, -1.42f)
                curveToRelative(0.39f, -0.39f, 1.03f, -0.39f, 1.42f, 0.0f)
                lineToRelative(4.58f, 4.58f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.03f, 0.0f, 1.42f)
                close()
            }
        }
        return _subdirectoryArrowRight!!
    }

private var _subdirectoryArrowRight: ImageVector? = null

internal val Icons.Rounded.TableChart: ImageVector
    get() {
        if (_tableChart != null) {
            return _tableChart!!
        }
        _tableChart = materialIcon(name = "Rounded.TableChart") {
            materialPath {
                moveTo(10.0f, 10.02f)
                horizontalLineToRelative(5.0f)
                lineTo(15.0f, 21.0f)
                horizontalLineToRelative(-5.0f)
                lineTo(10.0f, 10.02f)
                close()
                moveTo(17.0f, 21.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineToRelative(-5.0f)
                verticalLineToRelative(11.0f)
                close()
                moveTo(20.0f, 3.0f)
                lineTo(5.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(19.0f)
                lineTo(22.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(3.0f, 19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                lineTo(8.0f, 10.0f)
                lineTo(3.0f, 10.0f)
                verticalLineToRelative(9.0f)
                close()
            }
        }
        return _tableChart!!
    }

private var _tableChart: ImageVector? = null

internal val Icons.Rounded.Tablet: ImageVector
    get() {
        if (_tablet != null) {
            return _tablet!!
        }
        _tablet = materialIcon(name = "Rounded.Tablet") {
            materialPath {
                moveTo(21.0f, 4.0f)
                lineTo(3.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(1.1f, 0.0f, 1.99f, -0.9f, 1.99f, -2.0f)
                lineTo(23.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 18.0f)
                lineTo(5.0f, 18.0f)
                lineTo(5.0f, 6.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(12.0f)
                close()
            }
        }
        return _tablet!!
    }

private var _tablet: ImageVector? = null

internal val Icons.Rounded.TableView: ImageVector
    get() {
        if (_tableView != null) {
            return _tableView!!
        }
        _tableView = materialIcon(name = "Rounded.TableView") {
            materialPath {
                moveTo(19.0f, 7.0f)
                horizontalLineTo(9.0f)
                curveTo(7.9f, 7.0f, 7.0f, 7.9f, 7.0f, 9.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(9.0f)
                curveTo(21.0f, 7.9f, 20.1f, 7.0f, 19.0f, 7.0f)
                close()
                moveTo(19.0f, 10.0f)
                lineTo(19.0f, 10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(8.0f)
                curveTo(18.55f, 9.0f, 19.0f, 9.45f, 19.0f, 10.0f)
                close()
                moveTo(13.0f, 15.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(13.0f)
                close()
                moveTo(15.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(15.0f)
                close()
                moveTo(11.0f, 15.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(15.0f)
                close()
                moveTo(17.0f, 13.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(13.0f)
                close()
                moveTo(9.0f, 17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(9.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(17.0f, 19.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(17.0f)
                close()
                moveTo(6.0f, 17.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, 0.9f, 2.0f, 2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(10.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
        return _tableView!!
    }

private var _tableView: ImageVector? = null

internal val Icons.Rounded.Tag: ImageVector
    get() {
        if (_tag != null) {
            return _tag!!
        }
        _tag = materialIcon(name = "Rounded.Tag") {
            materialPath {
                moveTo(20.0f, 9.0f)
                lineTo(20.0f, 9.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveTo(8.45f, 4.0f, 8.0f, 4.45f, 8.0f, 5.0f)
                verticalLineToRelative(3.0f)
                horizontalLineTo(5.0f)
                curveTo(4.45f, 8.0f, 4.0f, 8.45f, 4.0f, 9.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(4.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(3.0f)
                curveTo(19.55f, 10.0f, 20.0f, 9.55f, 20.0f, 9.0f)
                close()
                moveTo(14.0f, 14.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(14.0f)
                close()
            }
        }
        return _tag!!
    }

private var _tag: ImageVector? = null

internal val Icons.Rounded.Terminal: ImageVector
    get() {
        if (_terminal != null) {
            return _terminal!!
        }
        _terminal = materialIcon(name = "Rounded.Terminal") {
            materialPath {
                moveTo(20.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveTo(2.89f, 4.0f, 2.0f, 4.9f, 2.0f, 6.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(22.0f, 4.9f, 21.11f, 4.0f, 20.0f, 4.0f)
                close()
                moveTo(20.0f, 18.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(8.0f)
                horizontalLineToRelative(16.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(12.0f, 16.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(12.45f, 17.0f, 12.0f, 16.55f, 12.0f, 16.0f)
                close()
                moveTo(6.79f, 9.71f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineToRelative(2.59f, 2.59f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-2.59f, 2.59f)
                curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(8.67f, 13.0f)
                lineToRelative(-1.88f, -1.88f)
                curveTo(6.4f, 10.73f, 6.4f, 10.1f, 6.79f, 9.71f)
                close()
            }
        }
        return _terminal!!
    }

private var _terminal: ImageVector? = null

internal val Icons.Rounded.Terrain: ImageVector
    get() {
        if (_terrain != null) {
            return _terrain!!
        }
        _terrain = materialIcon(name = "Rounded.Terrain") {
            materialPath {
                moveTo(13.2f, 7.07f)
                lineTo(10.25f, 11.0f)
                lineToRelative(2.25f, 3.0f)
                curveToRelative(0.33f, 0.44f, 0.24f, 1.07f, -0.2f, 1.4f)
                curveToRelative(-0.44f, 0.33f, -1.07f, 0.25f, -1.4f, -0.2f)
                curveToRelative(-1.05f, -1.4f, -2.31f, -3.07f, -3.1f, -4.14f)
                curveToRelative(-0.4f, -0.53f, -1.2f, -0.53f, -1.6f, 0.0f)
                lineToRelative(-4.0f, 5.33f)
                curveToRelative(-0.49f, 0.67f, -0.02f, 1.61f, 0.8f, 1.61f)
                horizontalLineToRelative(18.0f)
                curveToRelative(0.82f, 0.0f, 1.29f, -0.94f, 0.8f, -1.6f)
                lineToRelative(-7.0f, -9.33f)
                curveToRelative(-0.4f, -0.54f, -1.2f, -0.54f, -1.6f, 0.0f)
                close()
            }
        }
        return _terrain!!
    }

private var _terrain: ImageVector? = null

internal val Icons.Rounded.TextFields: ImageVector
    get() {
        if (_textFields != null) {
            return _textFields!!
        }
        _textFields = materialIcon(name = "Rounded.TextFields") {
            materialPath {
                moveTo(2.5f, 5.5f)
                curveTo(2.5f, 6.33f, 3.17f, 7.0f, 4.0f, 7.0f)
                horizontalLineToRelative(3.5f)
                verticalLineToRelative(10.5f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineTo(7.0f)
                horizontalLineTo(14.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                reflectiveCurveTo(14.83f, 4.0f, 14.0f, 4.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
                close()
                moveTo(20.0f, 9.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, 0.67f, -1.5f, 1.5f)
                reflectiveCurveTo(13.17f, 12.0f, 14.0f, 12.0f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(5.5f)
                curveToRelative(0.0f, 0.83f, 0.67f, 1.5f, 1.5f, 1.5f)
                reflectiveCurveToRelative(1.5f, -0.67f, 1.5f, -1.5f)
                verticalLineTo(12.0f)
                horizontalLineTo(20.0f)
                curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                reflectiveCurveTo(20.83f, 9.0f, 20.0f, 9.0f)
                close()
            }
        }
        return _textFields!!
    }

private var _textFields: ImageVector? = null

internal val Icons.Rounded.Thermostat: ImageVector
    get() {
        if (_thermostat != null) {
            return _thermostat!!
        }
        _thermostat = materialIcon(name = "Rounded.Thermostat") {
            materialPath {
                moveTo(15.0f, 13.0f)
                lineTo(15.0f, 5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(-1.21f, 0.91f, -2.0f, 2.37f, -2.0f, 4.0f)
                curveToRelative(0.0f, 2.76f, 2.24f, 5.0f, 5.0f, 5.0f)
                reflectiveCurveToRelative(5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -1.63f, -0.79f, -3.09f, -2.0f, -4.0f)
                close()
                moveTo(13.0f, 11.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                horizontalLineToRelative(-0.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(0.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-0.5f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineToRelative(0.5f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _thermostat!!
    }

private var _thermostat: ImageVector? = null

internal val Icons.Rounded.ThumbDown: ImageVector
    get() {
        if (_thumbDown != null) {
            return _thumbDown!!
        }
        _thumbDown = materialIcon(name = "Rounded.ThumbDown") {
            materialPath {
                moveTo(10.88f, 21.94f)
                lineToRelative(5.53f, -5.54f)
                curveToRelative(0.37f, -0.37f, 0.58f, -0.88f, 0.58f, -1.41f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineTo(6.0f)
                curveToRelative(-0.8f, 0.0f, -1.52f, 0.48f, -1.83f, 1.21f)
                lineTo(0.91f, 11.82f)
                curveTo(0.06f, 13.8f, 1.51f, 16.0f, 3.66f, 16.0f)
                horizontalLineToRelative(5.65f)
                lineToRelative(-0.95f, 4.58f)
                curveToRelative(-0.1f, 0.5f, 0.05f, 1.01f, 0.41f, 1.37f)
                curveToRelative(0.59f, 0.58f, 1.53f, 0.58f, 2.11f, -0.01f)
                close()
                moveTo(21.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(8.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _thumbDown!!
    }

private var _thumbDown: ImageVector? = null

internal val Icons.Rounded.Thunderstorm: ImageVector
    get() {
        if (_thunderstorm != null) {
            return _thunderstorm!!
        }
        _thunderstorm = materialIcon(name = "Rounded.Thunderstorm") {
            materialPath {
                moveTo(17.92f, 7.02f)
                curveTo(17.45f, 4.18f, 14.97f, 2.0f, 12.0f, 2.0f)
                curveTo(9.82f, 2.0f, 7.83f, 3.18f, 6.78f, 5.06f)
                curveTo(4.09f, 5.41f, 2.0f, 7.74f, 2.0f, 10.5f)
                curveTo(2.0f, 13.53f, 4.47f, 16.0f, 7.5f, 16.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(2.48f, 0.0f, 4.5f, -2.02f, 4.5f, -4.5f)
                curveTo(22.0f, 9.16f, 20.21f, 7.23f, 17.92f, 7.02f)
                close()
            }
            materialPath {
                moveTo(15.95f, 20.11f)
                lineToRelative(-0.84f, -0.42f)
                lineToRelative(0.9f, -1.03f)
                curveToRelative(0.36f, -0.42f, 0.32f, -1.05f, -0.09f, -1.41f)
                curveToRelative(-0.42f, -0.36f, -1.05f, -0.32f, -1.41f, 0.09f)
                lineToRelative(-1.75f, 2.0f)
                curveToRelative(-0.2f, 0.23f, -0.29f, 0.55f, -0.23f, 0.85f)
                curveToRelative(0.06f, 0.3f, 0.26f, 0.56f, 0.53f, 0.7f)
                lineToRelative(0.84f, 0.42f)
                lineTo(13.0f, 22.34f)
                curveToRelative(-0.36f, 0.42f, -0.32f, 1.05f, 0.09f, 1.41f)
                curveToRelative(0.19f, 0.17f, 0.42f, 0.25f, 0.66f, 0.25f)
                curveToRelative(0.28f, 0.0f, 0.55f, -0.12f, 0.75f, -0.34f)
                lineToRelative(1.75f, -2.0f)
                curveToRelative(0.2f, -0.23f, 0.29f, -0.55f, 0.23f, -0.85f)
                curveTo(16.42f, 20.5f, 16.22f, 20.24f, 15.95f, 20.11f)
                close()
            }
            materialPath {
                moveTo(9.95f, 20.11f)
                lineTo(9.1f, 19.68f)
                lineToRelative(0.9f, -1.03f)
                curveToRelative(0.36f, -0.42f, 0.32f, -1.05f, -0.09f, -1.41f)
                curveToRelative(-0.42f, -0.36f, -1.05f, -0.32f, -1.41f, 0.09f)
                lineToRelative(-1.75f, 2.0f)
                curveToRelative(-0.2f, 0.23f, -0.29f, 0.55f, -0.23f, 0.85f)
                curveToRelative(0.06f, 0.3f, 0.26f, 0.56f, 0.53f, 0.7f)
                lineToRelative(0.84f, 0.42f)
                lineTo(7.0f, 22.34f)
                curveToRelative(-0.36f, 0.42f, -0.32f, 1.05f, 0.09f, 1.41f)
                curveTo(7.28f, 23.92f, 7.52f, 24.0f, 7.75f, 24.0f)
                curveToRelative(0.28f, 0.0f, 0.55f, -0.12f, 0.75f, -0.34f)
                lineToRelative(1.75f, -2.0f)
                curveToRelative(0.2f, -0.23f, 0.29f, -0.55f, 0.23f, -0.85f)
                curveTo(10.42f, 20.5f, 10.22f, 20.24f, 9.95f, 20.11f)
                close()
            }
        }
        return _thunderstorm!!
    }

private var _thunderstorm: ImageVector? = null

internal val Icons.Rounded.Timer: ImageVector
    get() {
        if (_timer != null) {
            return _timer!!
        }
        _timer = materialIcon(name = "Rounded.Timer") {
            materialPath {
                moveTo(10.0f, 3.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-4.0f)
                curveTo(9.45f, 1.0f, 9.0f, 1.45f, 9.0f, 2.0f)
                curveTo(9.0f, 2.55f, 9.45f, 3.0f, 10.0f, 3.0f)
                close()
            }
            materialPath {
                moveTo(19.03f, 7.39f)
                lineToRelative(0.75f, -0.75f)
                curveToRelative(0.38f, -0.38f, 0.39f, -1.01f, 0.0f, -1.4f)
                curveToRelative(0.0f, 0.0f, -0.01f, -0.01f, -0.01f, -0.01f)
                curveToRelative(-0.39f, -0.39f, -1.01f, -0.38f, -1.4f, 0.0f)
                lineToRelative(-0.75f, 0.75f)
                curveTo(16.07f, 4.74f, 14.12f, 4.0f, 12.0f, 4.0f)
                curveToRelative(-4.8f, 0.0f, -8.88f, 3.96f, -9.0f, 8.76f)
                curveTo(2.87f, 17.84f, 6.94f, 22.0f, 12.0f, 22.0f)
                curveToRelative(4.98f, 0.0f, 9.0f, -4.03f, 9.0f, -9.0f)
                curveTo(21.0f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
                close()
                moveTo(13.0f, 13.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(9.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineTo(13.0f)
                close()
            }
        }
        return _timer!!
    }

private var _timer: ImageVector? = null

internal val Icons.Rounded.Toll: ImageVector
    get() {
        if (_toll != null) {
            return _toll!!
        }
        _toll = materialIcon(name = "Rounded.Toll") {
            materialPath {
                moveTo(15.0f, 4.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, 3.58f, -8.0f, 8.0f)
                reflectiveCurveToRelative(3.58f, 8.0f, 8.0f, 8.0f)
                reflectiveCurveToRelative(8.0f, -3.58f, 8.0f, -8.0f)
                reflectiveCurveToRelative(-3.58f, -8.0f, -8.0f, -8.0f)
                close()
                moveTo(15.0f, 18.0f)
                curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
                reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
                reflectiveCurveToRelative(6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                close()
                moveTo(3.0f, 12.0f)
                curveToRelative(0.0f, -2.39f, 1.4f, -4.46f, 3.43f, -5.42f)
                curveToRelative(0.34f, -0.16f, 0.57f, -0.47f, 0.57f, -0.84f)
                verticalLineToRelative(-0.19f)
                curveToRelative(0.0f, -0.68f, -0.71f, -1.11f, -1.32f, -0.82f)
                curveTo(2.92f, 5.99f, 1.0f, 8.77f, 1.0f, 12.0f)
                reflectiveCurveToRelative(1.92f, 6.01f, 4.68f, 7.27f)
                curveToRelative(0.61f, 0.28f, 1.32f, -0.14f, 1.32f, -0.82f)
                verticalLineToRelative(-0.18f)
                curveToRelative(0.0f, -0.37f, -0.23f, -0.69f, -0.57f, -0.85f)
                curveTo(4.4f, 16.46f, 3.0f, 14.39f, 3.0f, 12.0f)
                close()
            }
        }
        return _toll!!
    }

private var _toll: ImageVector? = null

internal val Icons.Rounded.TouchApp: ImageVector
    get() {
        if (_touchApp != null) {
            return _touchApp!!
        }
        _touchApp = materialIcon(name = "Rounded.TouchApp") {
            materialPath {
                moveTo(8.79f, 9.24f)
                verticalLineTo(5.5f)
                curveToRelative(0.0f, -1.38f, 1.12f, -2.5f, 2.5f, -2.5f)
                reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f)
                verticalLineToRelative(3.74f)
                curveToRelative(1.21f, -0.81f, 2.0f, -2.18f, 2.0f, -3.74f)
                curveToRelative(0.0f, -2.49f, -2.01f, -4.5f, -4.5f, -4.5f)
                reflectiveCurveToRelative(-4.5f, 2.01f, -4.5f, 4.5f)
                curveTo(6.79f, 7.06f, 7.58f, 8.43f, 8.79f, 9.24f)
                close()
                moveTo(14.29f, 11.71f)
                curveToRelative(-0.28f, -0.14f, -0.58f, -0.21f, -0.89f, -0.21f)
                horizontalLineToRelative(-0.61f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
                verticalLineToRelative(10.74f)
                lineToRelative(-3.44f, -0.72f)
                curveToRelative(-0.37f, -0.08f, -0.76f, 0.04f, -1.03f, 0.31f)
                curveToRelative(-0.43f, 0.44f, -0.43f, 1.14f, 0.0f, 1.58f)
                lineToRelative(4.01f, 4.01f)
                curveTo(9.71f, 21.79f, 10.22f, 22.0f, 10.75f, 22.0f)
                horizontalLineToRelative(6.1f)
                curveToRelative(1.0f, 0.0f, 1.84f, -0.73f, 1.98f, -1.72f)
                lineToRelative(0.63f, -4.47f)
                curveToRelative(0.12f, -0.85f, -0.32f, -1.69f, -1.09f, -2.07f)
                lineTo(14.29f, 11.71f)
                close()
            }
        }
        return _touchApp!!
    }

private var _touchApp: ImageVector? = null

internal val Icons.Rounded.TrackChanges: ImageVector
    get() {
        if (_trackChanges != null) {
            return _trackChanges!!
        }
        _trackChanges = materialIcon(name = "Rounded.TrackChanges") {
            materialPath {
                moveTo(18.32f, 5.68f)
                curveToRelative(-0.36f, 0.36f, -0.39f, 0.92f, -0.07f, 1.32f)
                curveToRelative(1.45f, 1.82f, 2.21f, 4.31f, 1.53f, 6.92f)
                curveToRelative(-0.79f, 3.05f, -3.18f, 5.33f, -6.21f, 5.94f)
                curveTo(8.47f, 20.87f, 4.0f, 16.93f, 4.0f, 12.0f)
                curveToRelative(0.0f, -4.08f, 3.05f, -7.44f, 7.0f, -7.93f)
                verticalLineToRelative(2.02f)
                curveToRelative(-3.13f, 0.53f, -5.43f, 3.46f, -4.93f, 6.83f)
                curveToRelative(0.39f, 2.61f, 2.56f, 4.71f, 5.18f, 5.03f)
                curveTo(14.89f, 18.4f, 18.0f, 15.56f, 18.0f, 12.0f)
                curveToRelative(0.0f, -1.25f, -0.38f, -2.4f, -1.03f, -3.36f)
                curveToRelative(-0.34f, -0.5f, -1.07f, -0.53f, -1.5f, -0.11f)
                lineToRelative(-0.01f, 0.01f)
                curveToRelative(-0.34f, 0.34f, -0.37f, 0.87f, -0.11f, 1.27f)
                curveToRelative(0.6f, 0.92f, 0.84f, 2.1f, 0.49f, 3.32f)
                curveToRelative(-0.39f, 1.37f, -1.54f, 2.46f, -2.94f, 2.77f)
                curveToRelative(-2.6f, 0.57f, -4.9f, -1.39f, -4.9f, -3.9f)
                curveToRelative(0.0f, -1.86f, 1.28f, -3.41f, 3.0f, -3.86f)
                verticalLineToRelative(2.14f)
                curveToRelative(-0.6f, 0.35f, -1.0f, 0.98f, -1.0f, 1.72f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
                curveToRelative(0.0f, -0.74f, -0.4f, -1.38f, -1.0f, -1.72f)
                verticalLineTo(2.71f)
                curveToRelative(0.0f, -0.39f, -0.32f, -0.71f, -0.71f, -0.71f)
                curveToRelative(-5.36f, -0.2f, -9.98f, 4.06f, -10.27f, 9.4f)
                curveToRelative(-0.36f, 6.55f, 5.41f, 11.82f, 12.01f, 10.4f)
                curveToRelative(3.88f, -0.83f, 6.88f, -3.8f, 7.75f, -7.67f)
                curveToRelative(0.71f, -3.16f, -0.2f, -6.16f, -1.97f, -8.37f)
                curveToRelative(-0.37f, -0.47f, -1.07f, -0.5f, -1.49f, -0.08f)
                close()
            }
        }
        return _trackChanges!!
    }

private var _trackChanges: ImageVector? = null

internal val Icons.Rounded.Train: ImageVector
    get() {
        if (_train != null) {
            return _train!!
        }
        _train = materialIcon(name = "Rounded.Train") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveToRelative(-4.0f, 0.0f, -8.0f, 0.5f, -8.0f, 4.0f)
                verticalLineToRelative(9.5f)
                curveTo(4.0f, 17.43f, 5.57f, 19.0f, 7.5f, 19.0f)
                lineToRelative(-1.15f, 1.15f)
                curveToRelative(-0.31f, 0.31f, -0.09f, 0.85f, 0.36f, 0.85f)
                lineTo(7.8f, 21.0f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.35f, -0.15f)
                lineTo(10.0f, 19.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(1.85f, 1.85f)
                curveToRelative(0.09f, 0.09f, 0.22f, 0.15f, 0.35f, 0.15f)
                horizontalLineToRelative(1.09f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineTo(16.5f, 19.0f)
                curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
                lineTo(20.0f, 6.0f)
                curveToRelative(0.0f, -3.5f, -4.0f, -4.0f, -8.0f, -4.0f)
                close()
                moveTo(7.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 14.0f, 7.5f, 14.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 17.0f, 7.5f, 17.0f)
                close()
                moveTo(11.0f, 10.0f)
                lineTo(6.0f, 10.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(4.0f)
                close()
                moveTo(16.5f, 17.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(18.0f, 10.0f)
                horizontalLineToRelative(-5.0f)
                lineTo(13.0f, 6.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(4.0f)
                close()
            }
        }
        return _train!!
    }

private var _train: ImageVector? = null

internal val Icons.Rounded.Tram: ImageVector
    get() {
        if (_tram != null) {
            return _tram!!
        }
        _tram = materialIcon(name = "Rounded.Tram") {
            materialPath {
                moveTo(13.0f, 5.0f)
                lineToRelative(0.75f, -1.5f)
                lineTo(17.0f, 3.5f)
                lineTo(17.0f, 2.0f)
                lineTo(7.0f, 2.0f)
                verticalLineToRelative(1.5f)
                horizontalLineToRelative(4.75f)
                lineTo(11.0f, 5.0f)
                curveToRelative(-3.13f, 0.09f, -6.0f, 0.73f, -6.0f, 3.5f)
                lineTo(5.0f, 17.0f)
                curveToRelative(0.0f, 1.5f, 1.11f, 2.73f, 2.55f, 2.95f)
                lineToRelative(-1.19f, 1.19f)
                curveToRelative(-0.32f, 0.32f, -0.1f, 0.86f, 0.35f, 0.86f)
                lineTo(7.8f, 22.0f)
                curveToRelative(0.13f, 0.0f, 0.26f, -0.05f, 0.35f, -0.15f)
                lineTo(10.0f, 20.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(1.85f, 1.85f)
                curveToRelative(0.09f, 0.09f, 0.22f, 0.15f, 0.35f, 0.15f)
                horizontalLineToRelative(1.09f)
                curveToRelative(0.45f, 0.0f, 0.67f, -0.54f, 0.35f, -0.85f)
                lineToRelative(-1.19f, -1.19f)
                curveTo(17.89f, 19.73f, 19.0f, 18.5f, 19.0f, 17.0f)
                lineTo(19.0f, 8.5f)
                curveToRelative(0.0f, -2.77f, -2.87f, -3.41f, -6.0f, -3.5f)
                close()
                moveTo(12.0f, 18.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(17.0f, 14.0f)
                lineTo(7.0f, 14.0f)
                lineTo(7.0f, 9.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(5.0f)
                close()
            }
        }
        return _tram!!
    }

private var _tram: ImageVector? = null

internal val Icons.Rounded.Translate: ImageVector
    get() {
        if (_translate != null) {
            return _translate!!
        }
        _translate = materialIcon(name = "Rounded.Translate") {
            materialPath {
                moveTo(12.65f, 15.67f)
                curveToRelative(0.14f, -0.36f, 0.05f, -0.77f, -0.23f, -1.05f)
                lineToRelative(-2.09f, -2.06f)
                lineToRelative(0.03f, -0.03f)
                curveToRelative(1.74f, -1.94f, 2.98f, -4.17f, 3.71f, -6.53f)
                horizontalLineToRelative(1.94f)
                curveToRelative(0.54f, 0.0f, 0.99f, -0.45f, 0.99f, -0.99f)
                verticalLineToRelative(-0.02f)
                curveToRelative(0.0f, -0.54f, -0.45f, -0.99f, -0.99f, -0.99f)
                lineTo(10.0f, 4.0f)
                lineTo(10.0f, 3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                lineTo(1.99f, 4.0f)
                curveToRelative(-0.54f, 0.0f, -0.99f, 0.45f, -0.99f, 0.99f)
                curveToRelative(0.0f, 0.55f, 0.45f, 0.99f, 0.99f, 0.99f)
                horizontalLineToRelative(10.18f)
                curveTo(11.5f, 7.92f, 10.44f, 9.75f, 9.0f, 11.35f)
                curveToRelative(-0.81f, -0.89f, -1.49f, -1.86f, -2.06f, -2.88f)
                curveToRelative(-0.16f, -0.29f, -0.45f, -0.47f, -0.78f, -0.47f)
                curveToRelative(-0.69f, 0.0f, -1.13f, 0.75f, -0.79f, 1.35f)
                curveToRelative(0.63f, 1.13f, 1.4f, 2.21f, 2.3f, 3.21f)
                lineTo(3.3f, 16.87f)
                curveToRelative(-0.4f, 0.39f, -0.4f, 1.03f, 0.0f, 1.42f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.42f, 0.0f)
                lineTo(9.0f, 14.0f)
                lineToRelative(2.02f, 2.02f)
                curveToRelative(0.51f, 0.51f, 1.38f, 0.32f, 1.63f, -0.35f)
                close()
                moveTo(17.5f, 10.0f)
                curveToRelative(-0.6f, 0.0f, -1.14f, 0.37f, -1.35f, 0.94f)
                lineToRelative(-3.67f, 9.8f)
                curveToRelative(-0.24f, 0.61f, 0.22f, 1.26f, 0.87f, 1.26f)
                curveToRelative(0.39f, 0.0f, 0.74f, -0.24f, 0.88f, -0.61f)
                lineToRelative(0.89f, -2.39f)
                horizontalLineToRelative(4.75f)
                lineToRelative(0.9f, 2.39f)
                curveToRelative(0.14f, 0.36f, 0.49f, 0.61f, 0.88f, 0.61f)
                curveToRelative(0.65f, 0.0f, 1.11f, -0.65f, 0.88f, -1.26f)
                lineToRelative(-3.67f, -9.8f)
                curveToRelative(-0.22f, -0.57f, -0.76f, -0.94f, -1.36f, -0.94f)
                close()
                moveTo(15.88f, 17.0f)
                lineToRelative(1.62f, -4.33f)
                lineTo(19.12f, 17.0f)
                horizontalLineToRelative(-3.24f)
                close()
            }
        }
        return _translate!!
    }

private var _translate: ImageVector? = null

internal val Icons.Rounded.Tune: ImageVector
    get() {
        if (_tune != null) {
            return _tune!!
        }
        _tune = materialIcon(name = "Rounded.Tune") {
            materialPath {
                moveTo(3.0f, 18.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-2.0f)
                lineTo(4.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(3.0f, 6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(9.0f)
                lineTo(13.0f, 5.0f)
                lineTo(4.0f, 5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(13.0f, 20.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                close()
                moveTo(7.0f, 10.0f)
                verticalLineToRelative(1.0f)
                lineTo(4.0f, 11.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(21.0f, 12.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(9.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                close()
                moveTo(16.0f, 9.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(17.0f, 7.0f)
                horizontalLineToRelative(3.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                lineTo(17.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
            }
        }
        return _tune!!
    }

private var _tune: ImageVector? = null

internal val Icons.Rounded.Tv: ImageVector
    get() {
        if (_tv != null) {
            return _tv!!
        }
        _tv = materialIcon(name = "Rounded.Tv") {
            materialPath {
                moveTo(21.0f, 3.0f)
                lineTo(3.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(5.0f)
                curveToRelative(1.1f, 0.0f, 1.99f, -0.9f, 1.99f, -2.0f)
                lineTo(23.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 17.0f)
                lineTo(4.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(3.0f, 6.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                close()
            }
        }
        return _tv!!
    }

private var _tv: ImageVector? = null

internal val Icons.Rounded.Umbrella: ImageVector
    get() {
        if (_umbrella != null) {
            return _umbrella!!
        }
        _umbrella = materialIcon(name = "Rounded.Umbrella") {
            materialPath {
                moveTo(17.12f, 6.28f)
                lineTo(14.5f, 6.92f)
                lineTo(13.0f, 5.77f)
                verticalLineTo(3.88f)
                verticalLineTo(3.4f)
                curveToRelative(0.0f, -0.26f, 0.22f, -0.48f, 0.5f, -0.48f)
                curveToRelative(0.23f, 0.0f, 0.43f, 0.16f, 0.49f, 0.36f)
                curveTo(14.1f, 3.7f, 14.49f, 4.0f, 14.94f, 4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.1f, -0.02f, -0.2f, -0.05f, -0.3f)
                curveTo(15.59f, 1.72f, 14.63f, 1.0f, 13.5f, 1.0f)
                curveTo(12.12f, 1.0f, 11.0f, 2.07f, 11.0f, 3.4f)
                verticalLineToRelative(0.48f)
                verticalLineToRelative(1.89f)
                lineTo(9.5f, 6.92f)
                lineTo(6.88f, 6.28f)
                curveTo(6.5f, 6.19f, 6.16f, 6.55f, 6.28f, 6.92f)
                lineToRelative(4.77f, 14.39f)
                curveTo(11.2f, 21.77f, 11.6f, 22.0f, 12.0f, 22.0f)
                reflectiveCurveToRelative(0.8f, -0.23f, 0.95f, -0.69f)
                lineToRelative(4.77f, -14.39f)
                curveTo(17.84f, 6.55f, 17.5f, 6.19f, 17.12f, 6.28f)
                close()
                moveTo(11.0f, 14.8f)
                lineTo(9.03f, 8.86f)
                lineToRelative(0.92f, 0.23f)
                lineToRelative(0.76f, -0.58f)
                lineTo(11.0f, 8.29f)
                verticalLineTo(14.8f)
                close()
                moveTo(13.0f, 14.8f)
                verticalLineTo(8.29f)
                lineToRelative(0.28f, 0.22f)
                lineToRelative(0.76f, 0.58f)
                lineToRelative(0.92f, -0.23f)
                lineTo(13.0f, 14.8f)
                close()
            }
        }
        return _umbrella!!
    }

private var _umbrella: ImageVector? = null

internal val Icons.Rounded.UnfoldMore: ImageVector
    get() {
        if (_unfoldMore != null) {
            return _unfoldMore!!
        }
        _unfoldMore = materialIcon(name = "Rounded.UnfoldMore") {
            materialPath {
                moveTo(12.0f, 5.83f)
                lineToRelative(2.46f, 2.46f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(12.7f, 3.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(8.12f, 6.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineTo(12.0f, 5.83f)
                close()
                moveTo(12.0f, 18.17f)
                lineToRelative(-2.46f, -2.46f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(3.17f, 3.18f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(3.17f, -3.17f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(12.0f, 18.17f)
                close()
            }
        }
        return _unfoldMore!!
    }

private var _unfoldMore: ImageVector? = null

internal val Icons.Rounded.Upload: ImageVector
    get() {
        if (_upload != null) {
            return _upload!!
        }
        _upload = materialIcon(name = "Rounded.Upload") {
            materialPath {
                moveTo(10.0f, 16.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(1.59f)
                curveToRelative(0.89f, 0.0f, 1.34f, -1.08f, 0.71f, -1.71f)
                lineTo(12.71f, 3.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineTo(6.71f, 8.29f)
                curveToRelative(-0.63f, 0.63f, -0.19f, 1.71f, 0.7f, 1.71f)
                lineTo(9.0f, 10.0f)
                verticalLineToRelative(5.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(6.0f, 18.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                lineTo(6.0f, 20.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
            }
        }
        return _upload!!
    }

private var _upload: ImageVector? = null

internal val Icons.Rounded.Vaccines: ImageVector
    get() {
        if (_vaccines != null) {
            return _vaccines!!
        }
        _vaccines = materialIcon(name = "Rounded.Vaccines") {
            materialPath {
                moveTo(7.0f, 22.5f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(1.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(7.5f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(0.5f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(1.5f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                verticalLineTo(15.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(4.5f)
                curveTo(6.0f, 22.05f, 6.45f, 22.5f, 7.0f, 22.5f)
                close()
                moveTo(9.0f, 9.0f)
                horizontalLineTo(7.25f)
                curveTo(6.84f, 9.0f, 6.5f, 9.34f, 6.5f, 9.75f)
                curveToRelative(0.0f, 0.41f, 0.34f, 0.75f, 0.75f, 0.75f)
                horizontalLineTo(9.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(7.25f)
                curveToRelative(-0.41f, 0.0f, -0.75f, 0.34f, -0.75f, 0.75f)
                curveToRelative(0.0f, 0.41f, 0.34f, 0.75f, 0.75f, 0.75f)
                horizontalLineTo(9.0f)
                lineTo(9.0f, 15.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(7.5f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(19.5f, 10.5f)
                verticalLineTo(10.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-5.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                verticalLineToRelative(0.5f)
                curveToRelative(0.0f, 0.5f, -1.5f, 1.16f, -1.5f, 3.0f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-6.5f)
                curveTo(21.0f, 11.66f, 19.5f, 11.0f, 19.5f, 10.5f)
                close()
                moveTo(16.5f, 10.5f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(0.5f)
                curveToRelative(0.0f, 1.6f, 1.5f, 2.0f, 1.5f, 3.0f)
                verticalLineTo(14.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(0.0f, -0.21f, 0.0f, -0.39f, 0.0f, -0.5f)
                curveTo(15.0f, 12.5f, 16.5f, 12.1f, 16.5f, 10.5f)
                close()
                moveTo(15.0f, 20.0f)
                curveToRelative(0.0f, 0.0f, 0.0f, -0.63f, 0.0f, -1.5f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(20.0f)
                horizontalLineTo(15.0f)
                close()
            }
        }
        return _vaccines!!
    }

private var _vaccines: ImageVector? = null

internal val Icons.Rounded.Verified: ImageVector
    get() {
        if (_verified != null) {
            return _verified!!
        }
        _verified = materialIcon(name = "Rounded.Verified") {
            materialPath {
                moveTo(23.0f, 12.0f)
                lineToRelative(-2.44f, -2.79f)
                lineToRelative(0.34f, -3.69f)
                lineToRelative(-3.61f, -0.82f)
                lineTo(15.4f, 1.5f)
                lineTo(12.0f, 2.96f)
                lineTo(8.6f, 1.5f)
                lineTo(6.71f, 4.69f)
                lineTo(3.1f, 5.5f)
                lineTo(3.44f, 9.2f)
                lineTo(1.0f, 12.0f)
                lineToRelative(2.44f, 2.79f)
                lineToRelative(-0.34f, 3.7f)
                lineToRelative(3.61f, 0.82f)
                lineTo(8.6f, 22.5f)
                lineToRelative(3.4f, -1.47f)
                lineToRelative(3.4f, 1.46f)
                lineToRelative(1.89f, -3.19f)
                lineToRelative(3.61f, -0.82f)
                lineToRelative(-0.34f, -3.69f)
                lineTo(23.0f, 12.0f)
                close()
                moveTo(9.38f, 16.01f)
                lineTo(7.0f, 13.61f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(0.07f, -0.07f)
                curveToRelative(0.39f, -0.39f, 1.03f, -0.39f, 1.42f, 0.0f)
                lineToRelative(1.61f, 1.62f)
                lineToRelative(5.15f, -5.16f)
                curveToRelative(0.39f, -0.39f, 1.03f, -0.39f, 1.42f, 0.0f)
                lineToRelative(0.07f, 0.07f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-5.92f, 5.94f)
                curveTo(10.41f, 16.4f, 9.78f, 16.4f, 9.38f, 16.01f)
                close()
            }
        }
        return _verified!!
    }

private var _verified: ImageVector? = null

internal val Icons.Rounded.VerifiedUser: ImageVector
    get() {
        if (_verifiedUser != null) {
            return _verifiedUser!!
        }
        _verifiedUser = materialIcon(name = "Rounded.VerifiedUser") {
            materialPath {
                moveTo(11.19f, 1.36f)
                lineToRelative(-7.0f, 3.11f)
                curveTo(3.47f, 4.79f, 3.0f, 5.51f, 3.0f, 6.3f)
                lineTo(3.0f, 11.0f)
                curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
                curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
                lineTo(21.0f, 6.3f)
                curveToRelative(0.0f, -0.79f, -0.47f, -1.51f, -1.19f, -1.83f)
                lineToRelative(-7.0f, -3.11f)
                curveToRelative(-0.51f, -0.23f, -1.11f, -0.23f, -1.62f, 0.0f)
                close()
                moveTo(9.29f, 16.29f)
                lineTo(6.7f, 13.7f)
                curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0.0f, -1.41f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                lineTo(10.0f, 14.17f)
                lineToRelative(5.88f, -5.88f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(-6.59f, 6.59f)
                curveToRelative(-0.38f, 0.39f, -1.02f, 0.39f, -1.41f, 0.0f)
                close()
            }
        }
        return _verifiedUser!!
    }

private var _verifiedUser: ImageVector? = null

internal val Icons.Rounded.Videocam: ImageVector
    get() {
        if (_videocam != null) {
            return _videocam!!
        }
        _videocam = materialIcon(name = "Rounded.Videocam") {
            materialPath {
                moveTo(17.0f, 10.5f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(10.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-3.5f)
                lineToRelative(2.29f, 2.29f)
                curveToRelative(0.63f, 0.63f, 1.71f, 0.18f, 1.71f, -0.71f)
                verticalLineTo(8.91f)
                curveToRelative(0.0f, -0.89f, -1.08f, -1.34f, -1.71f, -0.71f)
                lineTo(17.0f, 10.5f)
                close()
            }
        }
        return _videocam!!
    }

private var _videocam: ImageVector? = null

internal val Icons.Rounded.ViewKanban: ImageVector
    get() {
        if (_viewKanban != null) {
            return _viewKanban!!
        }
        _viewKanban = materialIcon(name = "Rounded.ViewKanban") {
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
                moveTo(8.0f, 17.0f)
                lineTo(8.0f, 17.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(8.0f)
                curveTo(9.0f, 16.55f, 8.55f, 17.0f, 8.0f, 17.0f)
                close()
                moveTo(12.0f, 12.0f)
                lineTo(12.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(3.0f)
                curveTo(13.0f, 11.55f, 12.55f, 12.0f, 12.0f, 12.0f)
                close()
                moveTo(16.0f, 15.0f)
                lineTo(16.0f, 15.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                verticalLineTo(8.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveTo(17.0f, 14.55f, 16.55f, 15.0f, 16.0f, 15.0f)
                close()
            }
        }
        return _viewKanban!!
    }

private var _viewKanban: ImageVector? = null

internal val Icons.Rounded.Visibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = materialIcon(name = "Rounded.Visibility") {
            materialPath {
                moveTo(12.0f, 4.0f)
                curveTo(7.0f, 4.0f, 2.73f, 7.11f, 1.0f, 11.5f)
                curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
                close()
                moveTo(12.0f, 16.5f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 8.5f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _visibility: ImageVector? = null

internal val Icons.Rounded.VisibilityOff: ImageVector
    get() {
        if (_visibilityOff != null) {
            return _visibilityOff!!
        }
        _visibilityOff = materialIcon(name = "Rounded.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 6.5f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                curveToRelative(0.0f, 0.51f, -0.1f, 1.0f, -0.24f, 1.46f)
                lineToRelative(3.06f, 3.06f)
                curveToRelative(1.39f, -1.23f, 2.49f, -2.77f, 3.18f, -4.53f)
                curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
                curveToRelative(-1.27f, 0.0f, -2.49f, 0.2f, -3.64f, 0.57f)
                lineToRelative(2.17f, 2.17f)
                curveToRelative(0.47f, -0.14f, 0.96f, -0.24f, 1.47f, -0.24f)
                close()
                moveTo(2.71f, 3.16f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(1.97f, 1.97f)
                curveTo(3.06f, 7.83f, 1.77f, 9.53f, 1.0f, 11.5f)
                curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
                curveToRelative(1.52f, 0.0f, 2.97f, -0.3f, 4.31f, -0.82f)
                lineToRelative(2.72f, 2.72f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(4.13f, 3.16f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                close()
                moveTo(12.0f, 16.5f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.77f, 0.18f, -1.5f, 0.49f, -2.14f)
                lineToRelative(1.57f, 1.57f)
                curveToRelative(-0.03f, 0.18f, -0.06f, 0.37f, -0.06f, 0.57f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.2f, 0.0f, 0.38f, -0.03f, 0.57f, -0.07f)
                lineTo(14.14f, 16.0f)
                curveToRelative(-0.65f, 0.32f, -1.37f, 0.5f, -2.14f, 0.5f)
                close()
                moveTo(14.97f, 11.17f)
                curveToRelative(-0.15f, -1.4f, -1.25f, -2.49f, -2.64f, -2.64f)
                lineToRelative(2.64f, 2.64f)
                close()
            }
        }
        return _visibilityOff!!
    }

private var _visibilityOff: ImageVector? = null

internal val Icons.Rounded.Warehouse: ImageVector
    get() {
        if (_warehouse != null) {
            return _warehouse!!
        }
        _warehouse = materialIcon(name = "Rounded.Warehouse") {
            materialPath {
                moveTo(22.0f, 19.0f)
                verticalLineTo(8.35f)
                curveToRelative(0.0f, -0.82f, -0.5f, -1.55f, -1.26f, -1.86f)
                lineToRelative(-8.0f, -3.2f)
                curveToRelative(-0.48f, -0.19f, -1.01f, -0.19f, -1.49f, 0.0f)
                lineToRelative(-8.0f, 3.2f)
                curveTo(2.5f, 6.8f, 2.0f, 7.54f, 2.0f, 8.35f)
                verticalLineTo(19.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-9.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(9.0f)
                horizontalLineToRelative(3.0f)
                curveTo(21.1f, 21.0f, 22.0f, 20.1f, 22.0f, 19.0f)
                close()
                moveTo(11.0f, 19.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(13.0f, 16.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(16.0f)
                close()
                moveTo(15.0f, 19.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(19.0f)
                close()
            }
        }
        return _warehouse!!
    }

private var _warehouse: ImageVector? = null

internal val Icons.Rounded.Watch: ImageVector
    get() {
        if (_watch != null) {
            return _watch!!
        }
        _watch = materialIcon(name = "Rounded.Watch") {
            materialPath {
                moveTo(20.0f, 12.0f)
                curveToRelative(0.0f, -2.54f, -1.19f, -4.81f, -3.04f, -6.27f)
                lineToRelative(-0.68f, -4.06f)
                curveTo(16.12f, 0.71f, 15.28f, 0.0f, 14.31f, 0.0f)
                horizontalLineTo(9.7f)
                curveToRelative(-0.98f, 0.0f, -1.82f, 0.71f, -1.98f, 1.67f)
                lineToRelative(-0.67f, 4.06f)
                curveTo(5.19f, 7.19f, 4.0f, 9.45f, 4.0f, 12.0f)
                reflectiveCurveToRelative(1.19f, 4.81f, 3.05f, 6.27f)
                lineToRelative(0.67f, 4.06f)
                curveToRelative(0.16f, 0.96f, 1.0f, 1.67f, 1.98f, 1.67f)
                horizontalLineToRelative(4.61f)
                curveToRelative(0.98f, 0.0f, 1.81f, -0.71f, 1.97f, -1.67f)
                lineToRelative(0.68f, -4.06f)
                curveTo(18.81f, 16.81f, 20.0f, 14.54f, 20.0f, 12.0f)
                close()
                moveTo(6.0f, 12.0f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                reflectiveCurveToRelative(6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                reflectiveCurveToRelative(-6.0f, -2.69f, -6.0f, -6.0f)
                close()
            }
        }
        return _watch!!
    }

private var _watch: ImageVector? = null

internal val Icons.Rounded.WaterDrop: ImageVector
    get() {
        if (_waterDrop != null) {
            return _waterDrop!!
        }
        _waterDrop = materialIcon(name = "Rounded.WaterDrop") {
            materialPath {
                moveTo(12.66f, 2.58f)
                curveToRelative(-0.38f, -0.33f, -0.95f, -0.33f, -1.33f, 0.0f)
                curveTo(6.45f, 6.88f, 4.0f, 10.62f, 4.0f, 13.8f)
                curveToRelative(0.0f, 4.98f, 3.8f, 8.2f, 8.0f, 8.2f)
                reflectiveCurveToRelative(8.0f, -3.22f, 8.0f, -8.2f)
                curveTo(20.0f, 10.62f, 17.55f, 6.88f, 12.66f, 2.58f)
                close()
                moveTo(7.83f, 14.0f)
                curveToRelative(0.37f, 0.0f, 0.67f, 0.26f, 0.74f, 0.62f)
                curveToRelative(0.41f, 2.22f, 2.28f, 2.98f, 3.64f, 2.87f)
                curveToRelative(0.43f, -0.02f, 0.79f, 0.32f, 0.79f, 0.75f)
                curveToRelative(0.0f, 0.4f, -0.32f, 0.73f, -0.72f, 0.75f)
                curveToRelative(-2.13f, 0.13f, -4.62f, -1.09f, -5.19f, -4.12f)
                curveTo(7.01f, 14.42f, 7.37f, 14.0f, 7.83f, 14.0f)
                close()
            }
        }
        return _waterDrop!!
    }

private var _waterDrop: ImageVector? = null

internal val Icons.Rounded.Waves: ImageVector
    get() {
        if (_waves != null) {
            return _waves!!
        }
        _waves = materialIcon(name = "Rounded.Waves") {
            materialPath {
                moveTo(17.0f, 16.99f)
                curveToRelative(-1.35f, 0.0f, -2.2f, 0.42f, -2.95f, 0.8f)
                curveToRelative(-0.65f, 0.33f, -1.18f, 0.6f, -2.05f, 0.6f)
                curveToRelative(-0.9f, 0.0f, -1.4f, -0.25f, -2.05f, -0.6f)
                curveToRelative(-0.75f, -0.38f, -1.57f, -0.8f, -2.95f, -0.8f)
                reflectiveCurveToRelative(-2.2f, 0.42f, -2.95f, 0.8f)
                curveToRelative(-0.43f, 0.22f, -0.81f, 0.41f, -1.27f, 0.52f)
                curveToRelative(-0.45f, 0.1f, -0.78f, 0.46f, -0.78f, 0.91f)
                verticalLineToRelative(0.1f)
                curveToRelative(0.0f, 0.6f, 0.56f, 1.03f, 1.14f, 0.91f)
                curveToRelative(0.74f, -0.15f, 1.3f, -0.43f, 1.81f, -0.69f)
                curveToRelative(0.65f, -0.33f, 1.17f, -0.6f, 2.05f, -0.6f)
                reflectiveCurveToRelative(1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.75f, 0.38f, 1.57f, 0.8f, 2.95f, 0.8f)
                reflectiveCurveToRelative(2.2f, -0.42f, 2.95f, -0.8f)
                curveToRelative(0.65f, -0.33f, 1.18f, -0.6f, 2.05f, -0.6f)
                curveToRelative(0.9f, 0.0f, 1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.52f, 0.26f, 1.08f, 0.55f, 1.83f, 0.7f)
                curveToRelative(0.58f, 0.11f, 1.12f, -0.33f, 1.12f, -0.91f)
                verticalLineToRelative(-0.09f)
                curveToRelative(0.0f, -0.46f, -0.34f, -0.82f, -0.79f, -0.92f)
                curveToRelative(-0.46f, -0.1f, -0.83f, -0.29f, -1.26f, -0.52f)
                curveToRelative(-0.75f, -0.39f, -1.6f, -0.81f, -2.95f, -0.81f)
                close()
                moveTo(17.0f, 12.54f)
                curveToRelative(-1.35f, 0.0f, -2.2f, 0.43f, -2.95f, 0.8f)
                curveToRelative(-0.65f, 0.32f, -1.18f, 0.6f, -2.05f, 0.6f)
                curveToRelative(-0.9f, 0.0f, -1.4f, -0.25f, -2.05f, -0.6f)
                curveToRelative(-0.75f, -0.38f, -1.57f, -0.8f, -2.95f, -0.8f)
                reflectiveCurveToRelative(-2.2f, 0.43f, -2.95f, 0.8f)
                curveToRelative(-0.43f, 0.21f, -0.81f, 0.41f, -1.28f, 0.52f)
                curveToRelative(-0.44f, 0.1f, -0.77f, 0.46f, -0.77f, 0.91f)
                verticalLineToRelative(0.1f)
                curveToRelative(0.0f, 0.59f, 0.54f, 1.03f, 1.12f, 0.91f)
                curveToRelative(0.75f, -0.15f, 1.31f, -0.44f, 1.83f, -0.69f)
                curveToRelative(0.65f, -0.35f, 1.15f, -0.6f, 2.05f, -0.6f)
                reflectiveCurveToRelative(1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.75f, 0.38f, 1.57f, 0.8f, 2.95f, 0.8f)
                reflectiveCurveToRelative(2.2f, -0.43f, 2.95f, -0.8f)
                curveToRelative(0.65f, -0.35f, 1.15f, -0.6f, 2.05f, -0.6f)
                reflectiveCurveToRelative(1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.52f, 0.26f, 1.08f, 0.55f, 1.83f, 0.7f)
                curveToRelative(0.58f, 0.11f, 1.12f, -0.33f, 1.12f, -0.92f)
                verticalLineToRelative(-0.09f)
                curveToRelative(0.0f, -0.46f, -0.34f, -0.82f, -0.79f, -0.92f)
                curveToRelative(-0.46f, -0.1f, -0.83f, -0.29f, -1.26f, -0.52f)
                curveToRelative(-0.75f, -0.38f, -1.6f, -0.8f, -2.95f, -0.8f)
                close()
                moveTo(19.95f, 4.46f)
                curveToRelative(-0.75f, -0.38f, -1.58f, -0.8f, -2.95f, -0.8f)
                reflectiveCurveToRelative(-2.2f, 0.42f, -2.95f, 0.8f)
                curveToRelative(-0.65f, 0.32f, -1.18f, 0.6f, -2.05f, 0.6f)
                curveToRelative(-0.9f, 0.0f, -1.4f, -0.25f, -2.05f, -0.6f)
                curveToRelative(-0.75f, -0.37f, -1.57f, -0.8f, -2.95f, -0.8f)
                reflectiveCurveToRelative(-2.2f, 0.42f, -2.95f, 0.8f)
                curveToRelative(-0.43f, 0.22f, -0.81f, 0.41f, -1.27f, 0.52f)
                curveToRelative(-0.45f, 0.1f, -0.78f, 0.46f, -0.78f, 0.91f)
                verticalLineToRelative(0.07f)
                curveToRelative(0.0f, 0.6f, 0.54f, 1.04f, 1.12f, 0.92f)
                curveToRelative(0.75f, -0.15f, 1.31f, -0.44f, 1.83f, -0.69f)
                curveToRelative(0.65f, -0.33f, 1.17f, -0.6f, 2.05f, -0.6f)
                reflectiveCurveToRelative(1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.75f, 0.38f, 1.57f, 0.8f, 2.95f, 0.8f)
                reflectiveCurveToRelative(2.2f, -0.43f, 2.95f, -0.8f)
                curveToRelative(0.65f, -0.32f, 1.18f, -0.6f, 2.05f, -0.6f)
                curveToRelative(0.9f, 0.0f, 1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.52f, 0.26f, 1.08f, 0.55f, 1.83f, 0.7f)
                curveToRelative(0.58f, 0.11f, 1.12f, -0.33f, 1.12f, -0.92f)
                verticalLineToRelative(-0.09f)
                curveToRelative(0.0f, -0.46f, -0.34f, -0.82f, -0.79f, -0.92f)
                curveToRelative(-0.46f, -0.1f, -0.83f, -0.28f, -1.26f, -0.5f)
                close()
                moveTo(17.0f, 8.09f)
                curveToRelative(-1.35f, 0.0f, -2.2f, 0.43f, -2.95f, 0.8f)
                curveToRelative(-0.65f, 0.35f, -1.15f, 0.6f, -2.05f, 0.6f)
                reflectiveCurveToRelative(-1.4f, -0.25f, -2.05f, -0.6f)
                curveToRelative(-0.75f, -0.38f, -1.57f, -0.8f, -2.95f, -0.8f)
                reflectiveCurveToRelative(-2.2f, 0.43f, -2.95f, 0.8f)
                curveToRelative(-0.43f, 0.23f, -0.8f, 0.42f, -1.26f, 0.52f)
                curveToRelative(-0.45f, 0.1f, -0.79f, 0.46f, -0.79f, 0.92f)
                verticalLineToRelative(0.09f)
                curveToRelative(0.0f, 0.59f, 0.54f, 1.03f, 1.12f, 0.91f)
                curveToRelative(0.75f, -0.15f, 1.31f, -0.44f, 1.83f, -0.69f)
                curveToRelative(0.65f, -0.32f, 1.18f, -0.6f, 2.05f, -0.6f)
                reflectiveCurveToRelative(1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.75f, 0.38f, 1.57f, 0.8f, 2.95f, 0.8f)
                reflectiveCurveToRelative(2.2f, -0.43f, 2.95f, -0.8f)
                curveToRelative(0.65f, -0.32f, 1.18f, -0.6f, 2.05f, -0.6f)
                curveToRelative(0.9f, 0.0f, 1.4f, 0.25f, 2.05f, 0.6f)
                curveToRelative(0.52f, 0.26f, 1.08f, 0.55f, 1.83f, 0.7f)
                curveToRelative(0.58f, 0.11f, 1.12f, -0.33f, 1.12f, -0.91f)
                verticalLineToRelative(-0.09f)
                curveToRelative(0.0f, -0.46f, -0.34f, -0.82f, -0.79f, -0.92f)
                curveToRelative(-0.46f, -0.1f, -0.83f, -0.29f, -1.26f, -0.52f)
                curveToRelative(-0.75f, -0.39f, -1.6f, -0.81f, -2.95f, -0.81f)
                close()
            }
        }
        return _waves!!
    }

private var _waves: ImageVector? = null

internal val Icons.Rounded.WbCloudy: ImageVector
    get() {
        if (_wbCloudy != null) {
            return _wbCloudy!!
        }
        _wbCloudy = materialIcon(name = "Rounded.WbCloudy") {
            materialPath {
                moveTo(19.37f, 10.04f)
                curveTo(18.68f, 6.59f, 15.65f, 4.0f, 12.01f, 4.0f)
                curveToRelative(-2.89f, 0.0f, -5.4f, 1.64f, -6.65f, 4.04f)
                curveTo(2.35f, 8.36f, 0.01f, 10.91f, 0.01f, 14.0f)
                curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(13.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.64f, -4.96f)
                close()
            }
        }
        return _wbCloudy!!
    }

private var _wbCloudy: ImageVector? = null

internal val Icons.Rounded.WbSunny: ImageVector
    get() {
        if (_wbSunny != null) {
            return _wbSunny!!
        }
        _wbSunny = materialIcon(name = "Rounded.WbSunny") {
            materialPath {
                moveTo(6.05f, 4.14f)
                lineToRelative(-0.39f, -0.39f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.38f, -1.4f, 0.0f)
                lineToRelative(-0.01f, 0.01f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.4f)
                lineToRelative(0.39f, 0.39f)
                curveToRelative(0.39f, 0.39f, 1.01f, 0.39f, 1.4f, 0.0f)
                lineToRelative(0.01f, -0.01f)
                curveToRelative(0.39f, -0.38f, 0.39f, -1.02f, 0.0f, -1.4f)
                close()
                moveTo(3.01f, 10.5f)
                lineTo(1.99f, 10.5f)
                curveToRelative(-0.55f, 0.0f, -0.99f, 0.44f, -0.99f, 0.99f)
                verticalLineToRelative(0.01f)
                curveToRelative(0.0f, 0.55f, 0.44f, 0.99f, 0.99f, 0.99f)
                lineTo(3.0f, 12.49f)
                curveToRelative(0.56f, 0.01f, 1.0f, -0.43f, 1.0f, -0.98f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0.0f, -0.56f, -0.44f, -1.0f, -0.99f, -1.0f)
                close()
                moveTo(12.01f, 0.55f)
                lineTo(12.0f, 0.55f)
                curveToRelative(-0.56f, 0.0f, -1.0f, 0.44f, -1.0f, 0.99f)
                verticalLineToRelative(0.96f)
                curveToRelative(0.0f, 0.55f, 0.44f, 0.99f, 0.99f, 0.99f)
                lineTo(12.0f, 3.49f)
                curveToRelative(0.56f, 0.01f, 1.0f, -0.43f, 1.0f, -0.98f)
                verticalLineToRelative(-0.97f)
                curveToRelative(0.0f, -0.55f, -0.44f, -0.99f, -0.99f, -0.99f)
                close()
                moveTo(19.75f, 3.76f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, -0.01f)
                lineToRelative(-0.39f, 0.39f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.4f)
                lineToRelative(0.01f, 0.01f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.4f, 0.0f)
                lineToRelative(0.39f, -0.39f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.01f, 0.0f, -1.4f)
                close()
                moveTo(17.94f, 18.86f)
                lineToRelative(0.39f, 0.39f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(-0.39f, -0.39f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.38f, -1.4f, 0.0f)
                curveToRelative(-0.4f, 0.4f, -0.4f, 1.02f, -0.01f, 1.41f)
                close()
                moveTo(20.0f, 11.49f)
                verticalLineToRelative(0.01f)
                curveToRelative(0.0f, 0.55f, 0.44f, 0.99f, 0.99f, 0.99f)
                lineTo(22.0f, 12.49f)
                curveToRelative(0.55f, 0.0f, 0.99f, -0.44f, 0.99f, -0.99f)
                verticalLineToRelative(-0.01f)
                curveToRelative(0.0f, -0.55f, -0.44f, -0.99f, -0.99f, -0.99f)
                horizontalLineToRelative(-1.01f)
                curveToRelative(-0.55f, 0.0f, -0.99f, 0.44f, -0.99f, 0.99f)
                close()
                moveTo(12.0f, 5.5f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
                reflectiveCurveToRelative(-2.69f, -6.0f, -6.0f, -6.0f)
                close()
                moveTo(11.99f, 22.45f)
                lineTo(12.0f, 22.45f)
                curveToRelative(0.55f, 0.0f, 0.99f, -0.44f, 0.99f, -0.99f)
                verticalLineToRelative(-0.96f)
                curveToRelative(0.0f, -0.55f, -0.44f, -0.99f, -0.99f, -0.99f)
                horizontalLineToRelative(-0.01f)
                curveToRelative(-0.55f, 0.0f, -0.99f, 0.44f, -0.99f, 0.99f)
                verticalLineToRelative(0.96f)
                curveToRelative(0.0f, 0.55f, 0.44f, 0.99f, 0.99f, 0.99f)
                close()
                moveTo(4.25f, 19.24f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.39f, -0.39f)
                curveToRelative(0.39f, -0.39f, 0.38f, -1.02f, 0.0f, -1.4f)
                lineToRelative(-0.01f, -0.01f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.39f, 0.39f)
                curveToRelative(-0.38f, 0.4f, -0.38f, 1.02f, 0.01f, 1.41f)
                close()
            }
        }
        return _wbSunny!!
    }

private var _wbSunny: ImageVector? = null

internal val Icons.Rounded.WbTwilight: ImageVector
    get() {
        if (_wbTwilight != null) {
            return _wbTwilight!!
        }
        _wbTwilight = materialIcon(name = "Rounded.WbTwilight") {
            materialPath {
                moveTo(19.07f, 9.37f)
                lineToRelative(0.71f, -0.71f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-0.71f, 0.71f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                curveTo(18.04f, 9.76f, 18.68f, 9.76f, 19.07f, 9.37f)
                close()
            }
            materialPath {
                moveTo(21.0f, 18.0f)
                horizontalLineTo(3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(18.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveTo(21.55f, 18.0f, 21.0f, 18.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 7.0f)
                lineTo(12.0f, 7.0f)
                curveToRelative(0.56f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                curveTo(11.0f, 6.55f, 11.45f, 7.0f, 12.0f, 7.0f)
                close()
            }
            materialPath {
                moveTo(4.96f, 9.34f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(5.66f, 7.22f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineTo(4.96f, 9.34f)
                close()
            }
            materialPath {
                moveTo(19.0f, 16.0f)
                curveToRelative(0.0f, -3.87f, -3.13f, -7.0f, -7.0f, -7.0f)
                reflectiveCurveToRelative(-7.0f, 3.13f, -7.0f, 7.0f)
                horizontalLineTo(19.0f)
                close()
            }
        }
        return _wbTwilight!!
    }

private var _wbTwilight: ImageVector? = null

internal val Icons.Rounded.Widgets: ImageVector
    get() {
        if (_widgets != null) {
            return _widgets!!
        }
        _widgets = materialIcon(name = "Rounded.Widgets") {
            materialPath {
                moveTo(13.0f, 14.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(4.0f, 21.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 13.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                close()
                moveTo(3.0f, 4.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                lineTo(11.0f, 4.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                lineTo(4.0f, 3.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                close()
                moveTo(15.95f, 2.4f)
                lineTo(11.7f, 6.64f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
                lineToRelative(4.25f, 4.25f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(4.25f, -4.25f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
                lineTo(17.37f, 2.4f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
                close()
            }
        }
        return _widgets!!
    }

private var _widgets: ImageVector? = null

internal val Icons.Rounded.Wifi: ImageVector
    get() {
        if (_wifi != null) {
            return _wifi!!
        }
        _wifi = materialIcon(name = "Rounded.Wifi") {
            materialPath {
                moveTo(2.06f, 10.06f)
                curveToRelative(0.51f, 0.51f, 1.32f, 0.56f, 1.87f, 0.1f)
                curveToRelative(4.67f, -3.84f, 11.45f, -3.84f, 16.13f, -0.01f)
                curveToRelative(0.56f, 0.46f, 1.38f, 0.42f, 1.89f, -0.09f)
                curveToRelative(0.59f, -0.59f, 0.55f, -1.57f, -0.1f, -2.1f)
                curveToRelative(-5.71f, -4.67f, -13.97f, -4.67f, -19.69f, 0.0f)
                curveToRelative(-0.65f, 0.52f, -0.7f, 1.5f, -0.1f, 2.1f)
                close()
                moveTo(9.82f, 17.82f)
                lineToRelative(1.47f, 1.47f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
                lineToRelative(1.47f, -1.47f)
                curveToRelative(0.47f, -0.47f, 0.37f, -1.28f, -0.23f, -1.59f)
                curveToRelative(-1.22f, -0.63f, -2.68f, -0.63f, -3.91f, 0.0f)
                curveToRelative(-0.57f, 0.31f, -0.68f, 1.12f, -0.21f, 1.59f)
                close()
                moveTo(6.09f, 14.09f)
                curveToRelative(0.49f, 0.49f, 1.26f, 0.54f, 1.83f, 0.13f)
                curveToRelative(2.44f, -1.73f, 5.72f, -1.73f, 8.16f, 0.0f)
                curveToRelative(0.57f, 0.4f, 1.34f, 0.36f, 1.83f, -0.13f)
                lineToRelative(0.01f, -0.01f)
                curveToRelative(0.6f, -0.6f, 0.56f, -1.62f, -0.13f, -2.11f)
                curveToRelative(-3.44f, -2.49f, -8.13f, -2.49f, -11.58f, 0.0f)
                curveToRelative(-0.69f, 0.5f, -0.73f, 1.51f, -0.12f, 2.12f)
                close()
            }
        }
        return _wifi!!
    }

private var _wifi: ImageVector? = null

internal val Icons.Rounded.WineBar: ImageVector
    get() {
        if (_wineBar != null) {
            return _wineBar!!
        }
        _wineBar = materialIcon(name = "Rounded.WineBar") {
            materialPath {
                moveTo(7.0f, 3.0f)
                curveTo(6.45f, 3.0f, 6.0f, 3.45f, 6.0f, 4.0f)
                lineToRelative(0.0f, 5.0f)
                curveToRelative(0.0f, 2.97f, 2.16f, 5.43f, 5.0f, 5.91f)
                verticalLineTo(19.0f)
                horizontalLineTo(9.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-4.09f)
                curveToRelative(2.84f, -0.48f, 5.0f, -2.94f, 5.0f, -5.91f)
                lineToRelative(0.0f, -5.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(16.0f, 8.0f)
                horizontalLineTo(8.0f)
                lineToRelative(0.0f, -3.0f)
                horizontalLineToRelative(8.0f)
                curveTo(16.0f, 5.0f, 16.0f, 8.0f, 16.0f, 8.0f)
                close()
            }
        }
        return _wineBar!!
    }

private var _wineBar: ImageVector? = null

internal val Icons.Rounded.Work: ImageVector
    get() {
        if (_work != null) {
            return _work!!
        }
        _work = materialIcon(name = "Rounded.Work") {
            materialPath {
                moveTo(20.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(16.0f, 4.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                curveToRelative(-1.11f, 0.0f, -2.0f, 0.89f, -2.0f, 2.0f)
                verticalLineToRelative(2.0f)
                lineTo(4.0f, 6.0f)
                curveToRelative(-1.11f, 0.0f, -1.99f, 0.89f, -1.99f, 2.0f)
                lineTo(2.0f, 19.0f)
                curveToRelative(0.0f, 1.11f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.11f, 0.0f, 2.0f, -0.89f, 2.0f, -2.0f)
                lineTo(22.0f, 8.0f)
                curveToRelative(0.0f, -1.11f, -0.89f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(14.0f, 6.0f)
                horizontalLineToRelative(-4.0f)
                lineTo(10.0f, 4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _work!!
    }

private var _work: ImageVector? = null

internal val Icons.Rounded.WorkspacePremium: ImageVector
    get() {
        if (_workspacePremium != null) {
            return _workspacePremium!!
        }
        _workspacePremium = materialIcon(name = "Rounded.WorkspacePremium") {
            materialPath {
                moveTo(10.92f, 12.75f)
                lineTo(12.0f, 11.93f)
                lineToRelative(1.07f, 0.81f)
                curveToRelative(0.39f, 0.29f, 0.92f, -0.08f, 0.78f, -0.55f)
                lineToRelative(-0.42f, -1.36f)
                lineToRelative(1.2f, -0.95f)
                curveTo(15.0f, 9.6f, 14.79f, 9.0f, 14.31f, 9.0f)
                horizontalLineToRelative(-1.4f)
                lineToRelative(-0.43f, -1.34f)
                curveToRelative(-0.15f, -0.46f, -0.8f, -0.46f, -0.95f, 0.0f)
                lineTo(11.09f, 9.0f)
                horizontalLineTo(9.68f)
                curveTo(9.21f, 9.0f, 9.0f, 9.6f, 9.37f, 9.89f)
                lineToRelative(1.19f, 0.95f)
                lineToRelative(-0.42f, 1.36f)
                curveTo(10.0f, 12.67f, 10.53f, 13.04f, 10.92f, 12.75f)
                close()
                moveTo(6.0f, 21.61f)
                curveToRelative(0.0f, 0.68f, 0.67f, 1.16f, 1.32f, 0.95f)
                lineTo(12.0f, 21.0f)
                lineToRelative(4.68f, 1.56f)
                curveTo(17.33f, 22.78f, 18.0f, 22.3f, 18.0f, 21.61f)
                verticalLineToRelative(-6.33f)
                curveToRelative(1.24f, -1.41f, 2.0f, -3.25f, 2.0f, -5.28f)
                curveToRelative(0.0f, -4.42f, -3.58f, -8.0f, -8.0f, -8.0f)
                reflectiveCurveToRelative(-8.0f, 3.58f, -8.0f, 8.0f)
                curveToRelative(0.0f, 2.03f, 0.76f, 3.87f, 2.0f, 5.28f)
                verticalLineTo(21.61f)
                close()
                moveTo(12.0f, 4.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, 2.69f, 6.0f, 6.0f)
                reflectiveCurveToRelative(-2.69f, 6.0f, -6.0f, 6.0f)
                reflectiveCurveToRelative(-6.0f, -2.69f, -6.0f, -6.0f)
                reflectiveCurveTo(8.69f, 4.0f, 12.0f, 4.0f)
                close()
            }
        }
        return _workspacePremium!!
    }

private var _workspacePremium: ImageVector? = null

internal val Icons.Rounded.ZoomIn: ImageVector
    get() {
        if (_zoomIn != null) {
            return _zoomIn!!
        }
        _zoomIn = materialIcon(name = "Rounded.ZoomIn") {
            materialPath {
                moveTo(15.5f, 14.0f)
                horizontalLineToRelative(-0.79f)
                lineToRelative(-0.28f, -0.27f)
                curveToRelative(1.2f, -1.4f, 1.82f, -3.31f, 1.48f, -5.34f)
                curveToRelative(-0.47f, -2.78f, -2.79f, -5.0f, -5.59f, -5.34f)
                curveToRelative(-4.23f, -0.52f, -7.78f, 3.04f, -7.27f, 7.27f)
                curveToRelative(0.34f, 2.8f, 2.56f, 5.12f, 5.34f, 5.59f)
                curveToRelative(2.03f, 0.34f, 3.94f, -0.28f, 5.34f, -1.48f)
                lineToRelative(0.27f, 0.28f)
                verticalLineToRelative(0.79f)
                lineToRelative(4.26f, 4.25f)
                curveToRelative(0.41f, 0.41f, 1.07f, 0.41f, 1.48f, 0.0f)
                lineToRelative(0.01f, -0.01f)
                curveToRelative(0.41f, -0.41f, 0.41f, -1.07f, 0.0f, -1.48f)
                lineTo(15.5f, 14.0f)
                close()
                moveTo(9.5f, 14.0f)
                curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
                reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
                reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
                reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
                close()
                moveTo(9.5f, 7.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                lineTo(9.0f, 9.0f)
                lineTo(7.5f, 9.0f)
                curveToRelative(-0.28f, 0.0f, -0.5f, 0.22f, -0.5f, 0.5f)
                reflectiveCurveToRelative(0.22f, 0.5f, 0.5f, 0.5f)
                lineTo(9.0f, 10.0f)
                verticalLineToRelative(1.5f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                reflectiveCurveToRelative(0.5f, -0.22f, 0.5f, -0.5f)
                lineTo(10.0f, 10.0f)
                horizontalLineToRelative(1.5f)
                curveToRelative(0.28f, 0.0f, 0.5f, -0.22f, 0.5f, -0.5f)
                reflectiveCurveToRelative(-0.22f, -0.5f, -0.5f, -0.5f)
                lineTo(10.0f, 9.0f)
                lineTo(10.0f, 7.5f)
                curveToRelative(0.0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f)
                close()
            }
        }
        return _zoomIn!!
    }

private var _zoomIn: ImageVector? = null
