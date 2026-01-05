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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TtsAnnotation
import androidx.compose.ui.text.UrlAnnotation

internal val AnnotatedString.stringAnnotations: List<AnnotatedString.Range<String>> get() {
    return getStringAnnotations(0, length)
}

internal val AnnotatedString.ttsAnnotations: List<AnnotatedString.Range<TtsAnnotation>> get() {
    return getTtsAnnotations(0, length)
}

@ExperimentalTextApi
internal val AnnotatedString.urlAnnotations: List<AnnotatedString.Range<UrlAnnotation>> get() {
    return getUrlAnnotations(0, length)
}

internal fun AnnotatedString.Builder.addParagraphStyles(styles: List<AnnotatedString.Range<ParagraphStyle>>) {
    for (style in styles) {
        addStyle(style.item, style.start, style.end)
    }
}

internal fun AnnotatedString.Builder.addSpanStyles(styles: List<AnnotatedString.Range<SpanStyle>>) {
    for (style in styles) {
        addStyle(style.item, style.start, style.end)
    }
}

internal fun AnnotatedString.Builder.addStringAnnotations(annotations: List<AnnotatedString.Range<String>>) {
    for (annotation in annotations) {
        addStringAnnotation(annotation.tag, annotation.item, annotation.start, annotation.end)
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun AnnotatedString.Builder.addTtsAnnotations(annotations: List<AnnotatedString.Range<TtsAnnotation>>) {
    for (annotation in annotations) {
        addTtsAnnotation(annotation.item, annotation.start, annotation.end)
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun AnnotatedString.Builder.addUrlAnnotations(annotations: List<AnnotatedString.Range<UrlAnnotation>>) {
    for (annotation in annotations) {
        addUrlAnnotation(annotation.item, annotation.start, annotation.end)
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun AnnotatedString.Builder.merge(annotated: AnnotatedString) {
    addSpanStyles(annotated.spanStyles)
    addParagraphStyles(annotated.paragraphStyles)
    addStringAnnotations(annotated.stringAnnotations)
    addTtsAnnotations(annotated.ttsAnnotations)
    addUrlAnnotations(annotated.urlAnnotations)
}
