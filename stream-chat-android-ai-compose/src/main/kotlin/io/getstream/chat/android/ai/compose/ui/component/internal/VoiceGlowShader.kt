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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * The glow drawn by a shader, every pixel computed on the GPU: color masses warped by noise,
 * concentric waves, light and a halo. It reads the same motion as the Canvas glow.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class VoiceGlowShader {
    private val shader = RuntimeShader(SOURCE)
    private val brush = ShaderBrush(shader)

    fun draw(scope: DrawScope, d: VoiceVisualizationDriver, dark: Float) = with(scope) {
        shader.setFloatUniform("resolution", size.width, size.height)
        for (i in 0 until VoiceVisualizationPalette.MAXIMUM_COUNT) {
            val c = d.color(i)
            shader.setFloatUniform("col$i", c.hue, c.saturation, c.value, 0f)
        }
        shader.setFloatUniform("time", d.time)
        shader.setFloatUniform("count", d.count)
        shader.setFloatUniform("bright", d.bright)
        shader.setFloatUniform("sat", d.sat)
        shader.setFloatUniform("load", d.load)
        shader.setFloatUniform("flow", d.flow)
        shader.setFloatUniform("flowSpin", d.flowSpin)
        shader.setFloatUniform("orbSpin", d.orbSpin)
        shader.setFloatUniform("react", d.react)
        shader.setFloatUniform("dark", dark)
        shader.setFloatUniform("mic", d.mic)
        shader.setFloatUniform("voice", d.voice)
        shader.setFloatUniform("scale", d.scale)
        drawRect(brush)
    }

    private companion object {
        /** AGSL. Pixels past the halo return at once, so only the glow itself costs. */
        const val SOURCE = """
uniform float2 resolution;
uniform float4 col0;
uniform float4 col1;
uniform float4 col2;
uniform float4 col3;
uniform float4 col4;
uniform float time;
uniform float count;
uniform float bright;
uniform float sat;
uniform float load;
uniform float flow;
uniform float flowSpin;
uniform float orbSpin;
uniform float react;
uniform float dark;
uniform float mic;
uniform float voice;
uniform float scale;

float sq(float x) { return x * x; }
float ss(float e0, float e1, float x) {
    float t = clamp((x - e0) / (e1 - e0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}
float3 hsv2rgb(float3 c) {
    float4 K = float4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
float3 desat(float3 c, float s) {
    float l = dot(c, float3(0.299, 0.587, 0.114));
    return mix(float3(l), c, s);
}
float3 saturate3(float3 c, float s) {
    float l = dot(c, float3(0.299, 0.587, 0.114));
    return clamp(mix(float3(l), c, s), 0.0, 1.0);
}
float2 rot2(float2 p, float a) {
    float s = sin(a); float c = cos(a);
    return float2(c * p.x + s * p.y, -s * p.x + c * p.y);
}
float3 vivid(float4 hsv) {
    return hsv2rgb(float3(hsv.x, clamp(hsv.y + dark * 0.06, 0.0, 1.0), hsv.z));
}
float4 permute(float4 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
float4 taylorInvSqrt(float4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
float snoise(float3 v) {
    const float2 C = float2(1.0 / 6.0, 1.0 / 3.0);
    const float4 D = float4(0.0, 0.5, 1.0, 2.0);
    float3 i = floor(v + dot(v, C.yyy));
    float3 x0 = v - i + dot(i, C.xxx);
    float3 g = step(x0.yzx, x0.xyz);
    float3 l = 1.0 - g;
    float3 i1 = min(g.xyz, l.zxy);
    float3 i2 = max(g.xyz, l.zxy);
    float3 x1 = x0 - i1 + 1.0 * C.xxx;
    float3 x2 = x0 - i2 + 2.0 * C.xxx;
    float3 x3 = x0 - 1.0 + 3.0 * C.xxx;
    i = mod(i, 289.0);
    float4 p = permute(permute(permute(
                i.z + float4(0.0, i1.z, i2.z, 1.0))
              + i.y + float4(0.0, i1.y, i2.y, 1.0))
              + i.x + float4(0.0, i1.x, i2.x, 1.0));
    float n_ = 1.0 / 7.0;
    float3 ns = n_ * D.wyz - D.xzx;
    float4 j = p - 49.0 * floor(p * ns.z * ns.z);
    float4 x_ = floor(j * ns.z);
    float4 y_ = floor(j - 7.0 * x_);
    float4 x = x_ * ns.x + ns.yyyy;
    float4 y = y_ * ns.x + ns.yyyy;
    float4 h = 1.0 - abs(x) - abs(y);
    float4 b0 = float4(x.xy, y.xy);
    float4 b1 = float4(x.zw, y.zw);
    float4 s0 = floor(b0) * 2.0 + 1.0;
    float4 s1 = floor(b1) * 2.0 + 1.0;
    float4 sh = -step(h, float4(0.0));
    float4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    float4 a1 = b1.xzyw + s1.xzyw * sh.zzww;
    float3 p0 = float3(a0.xy, h.x);
    float3 p1 = float3(a0.zw, h.y);
    float3 p2 = float3(a1.xy, h.z);
    float3 p3 = float3(a1.zw, h.w);
    float4 norm = taylorInvSqrt(float4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x; p1 *= norm.y; p2 *= norm.z; p3 *= norm.w;
    float4 m = max(0.6 - float4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, float4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}
float speechEnv(float t) {
    float gate = ss(-0.25, 0.35, snoise(float3(t * 0.6, 21.0, 0.0)));
    float syl = 0.55 + 0.45 * snoise(float3(t * 3.0, 5.0, 0.0));
    return clamp(gate * syl, 0.0, 1.0);
}
float2 coords(float2 pos) {
    float m = min(resolution.x, resolution.y);
    return float2(pos.x - 0.5 * resolution.x, 0.5 * resolution.y - pos.y) / m / scale;
}

half4 main(float2 fragCoord) {
    float2 q = coords(fragCoord);
    float t = time;
    float r = length(q);

    float speak = ss(0.6, 1.0, react);
    float speech = react * mix(0.55 + 0.25 * sin(t * 1.6), speechEnv(t), speak);
    speech = mix(speech, mic, voice);
    float speechSmooth = mix(speech, react * (0.55 + 0.25 * sin(t * 1.6)), 0.5 * speak);
    float energy = 0.12 + 0.30 * react + 0.18 * flow + 0.28 * load + 0.06 * speechSmooth;
    float R = 0.24 * (1.0 + speechSmooth * 0.20 + load * 0.05 * sin(t * 2.0) + react * 0.04 * sin(t * 1.6));
    float p = r / R;
    if (p > 2.2) return half4(0.0);

    float core = exp(-2.0 * p * p);
    float body = ss(1.06, 0.62, p);

    float2 P = q / R;
    float pl = length(P);
    float nt = t * 0.15 + flowSpin * 0.05;
    float nAmp = 0.20 + 0.28 * energy;
    float2 sp = P + nAmp * float2(snoise(float3(P * 0.7, nt)), snoise(float3(P * 0.7 + 4.7, nt)));
    float ang = atan(P.y, P.x);
    float wave = (0.018 + 0.025 * energy) * sin(pl * 5.0 - t * 2.0) * ss(0.0, 0.4, pl);
    sp += wave * float2(-sin(ang), cos(ang));
    sp = rot2(sp, flowSpin * 0.30);
    float bt = t;
    float a0 = orbSpin * 0.42 + 2.0 * snoise(float3(bt * 0.12, 0.0, 0.0));
    float a1 = -orbSpin * 0.34 + 2.0 * snoise(float3(bt * 0.10, 5.0, 0.0)) + 2.0;
    float a2 = orbSpin * 0.30 + 2.0 * snoise(float3(bt * 0.11, 9.0, 0.0)) + 1.0;
    float a3 = -orbSpin * 0.26 + 2.0 * snoise(float3(bt * 0.09, 14.0, 0.0)) + 3.5;
    float2 c0 = 0.40 * float2(cos(a0), sin(a0));
    float2 c1 = 0.38 * float2(cos(a1), sin(a1));
    float2 c2c = 0.36 * float2(cos(a2), sin(a2));
    float2 c3c = 0.34 * float2(cos(a3), sin(a3));
    float b0 = ss(1.15, 0.1, length(sp - c0));
    float b1 = ss(1.30, 0.1, length(sp - c1));
    float b2 = ss(1.20, 0.1, length(sp - c2c));
    float b3 = ss(1.25, 0.1, length(sp - c3c));

    float3 k0 = saturate3(vivid(col0), 1.7);
    float3 kL = saturate3(clamp(vivid(col0) * 1.18, 0.0, 1.0), 1.3);
    float3 kD = saturate3(vivid(col0) * 0.85, 1.3);
    float3 colB = mix(kL, saturate3(vivid(col1), 1.7), clamp(count - 1.0, 0.0, 1.0));
    float3 colC = mix(kD, saturate3(vivid(col2), 1.7), clamp(count - 2.0, 0.0, 1.0));
    float3 colD = saturate3(vivid(col3), 1.7);
    float3 colE = saturate3(vivid(col4), 1.7);
    float3 base = saturate3(mix(vivid(col0), float3(1.0), 0.06), 1.3);
    float3 col = base;
    col = mix(col, k0, b0);
    col = mix(col, colB, b1);
    col = mix(col, colC, b0 * 0.5);
    col = mix(col, colD, b2 * clamp(count - 3.0, 0.0, 1.0));
    col = mix(col, colE, b3 * clamp(count - 4.0, 0.0, 1.0));
    col = saturate3(col, 1.15);
    col = clamp(col * 1.12, 0.0, 1.0);
    col = desat(col, sat);

    float z = sqrt(max(1.0 - p * p, 0.0));
    float3 nrm = normalize(float3(q / R, z + 1e-4));
    float3 Ld = normalize(float3(-0.35, 0.45, 0.85));
    float diff = clamp(dot(nrm, Ld), 0.0, 1.0);
    col *= 0.80 + 0.20 * diff;
    float fres = pow(1.0 - clamp(nrm.z, 0.0, 1.0), 2.5);
    col += fres * 0.10 * mix(float3(1.0), k0, 0.6);

    float3 haloCol = mix(mix(vivid(col0), float3(1.0), 0.75), mix(vivid(col0), float3(1.0), 0.32), dark);
    float halo = exp(-sq(max(r - R * 0.8, 0.0) / 0.09));
    float haloA = halo * (mix(0.05, 0.10, dark) + 0.05 * react);
    float waves = clamp(b0 + 0.8 * b1, 0.0, 1.0);
    float bodyA = clamp(body * (0.15 + 0.60 * waves + 0.06 * core) * (0.92 + 0.08 * fres), 0.0, 1.0);
    float fade = mix(1.0, 0.45 + 0.35 * sin(t * 2.0), load);
    float3 pm = col * bodyA + haloCol * haloA * (1.0 - bodyA);
    float al = bodyA + haloA * (1.0 - bodyA);
    float gain = bright * fade;
    return half4(half3(pm * gain), half(al * gain));
}
"""
    }
}
