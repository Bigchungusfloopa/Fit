package com.example.feet.ui.theme // Or any other appropriate package

// The #define from the original code
private const val MAX_COLORS = 8

// The fully translated AGSL shader string
const val AGSL_SHADER_STRING = """
    // All the uniforms from the original shader
    uniform vec2 uResolution;
    uniform float uTime;
    uniform float uSpeed;
    uniform vec2 uRot;
    uniform int uColorCount;
    
    // --- FIX ---
    // Replaced the array with 8 individual color uniforms
    // This is required for API 33 compatibility
    uniform vec3 uColor0;
    uniform vec3 uColor1;
    uniform vec3 uColor2;
    uniform vec3 uColor3;
    uniform vec3 uColor4;
    uniform vec3 uColor5;
    uniform vec3 uColor6;
    uniform vec3 uColor7;
    // --- END FIX ---
    
    uniform int uTransparent;
    uniform float uScale;
    uniform float uFrequency;
    uniform float uWarpStrength;
    uniform vec2 uPointer; // in NDC [-1,1]
    uniform float uMouseInfluence;
    uniform float uParallax;
    uniform float uNoise;

    vec4 main(vec2 fragCoord) {
        // 1. Calculate vUv (UV coords [0,1]) from fragCoord (pixel coords)
        vec2 vUv = fragCoord / uResolution.xy;

        // --- ALL THE ORIGINAL SHADER LOGIC STARTS HERE ---
        // (Copied directly from your 'frag' variable)

        float t = uTime * uSpeed;
        vec2 p = vUv * 2.0 - 1.0; // Convert UV [0,1] to NDC [-1,1]
        p += uPointer * uParallax * 0.1;
        vec2 rp = vec2(p.x * uRot.x - p.y * uRot.y, p.x * uRot.y + p.y * uRot.x);
        vec2 q = vec2(rp.x * (uResolution.x / uResolution.y), rp.y); // Use uResolution instead of uCanvas
        q /= max(uScale, 0.0001);
        q /= 0.5 + 0.2 * dot(q, q);
        q += 0.2 * cos(t) - 7.56;
        vec2 toward = (uPointer - rp);
        q += toward * uMouseInfluence * 0.2;

        vec3 col = vec3(0.0);
        float a = 1.0;

        if (uColorCount > 0) {
            vec2 s = q;
            vec3 sumCol = vec3(0.0);
            float cover = 0.0;
            for (int i = 0; i < $MAX_COLORS; ++i) {
                if (i >= uColorCount) break;
                s -= 0.01;
                vec2 r = sin(1.5 * (s.yx * uFrequency) + 2.0 * cos(s * uFrequency));
                float m0 = length(r + sin(5.0 * r.y * uFrequency - 3.0 * t + float(i)) / 4.0);
                float kBelow = clamp(uWarpStrength, 0.0, 1.0);
                float kMix = pow(kBelow, 0.3); // strong response across 0..1
                float gain = 1.0 + max(uWarpStrength - 1.0, 0.0); // allow >1 to amplify displacement
                vec2 disp = (r - s) * kBelow;
                vec2 warped = s + disp * gain;
                float m1 = length(warped + sin(5.0 * warped.y * uFrequency - 3.0 * t + float(i)) / 4.0);
                float m = mix(m0, m1, kMix);
                float w = 1.0 - exp(-6.0 / exp(6.0 * m));
                
                // --- FIX ---
                // Get the correct color based on the loop index
                vec3 color = vec3(0.0);
                if (i == 0) color = uColor0;
                else if (i == 1) color = uColor1;
                else if (i == 2) color = uColor2;
                else if (i == 3) color = uColor3;
                else if (i == 4) color = uColor4;
                else if (i == 5) color = uColor5;
                else if (i == 6) color = uColor6;
                else if (i == 7) color = uColor7;

                sumCol += color * w;
                // --- END FIX ---

                cover = max(cover, w);
            }
            col = clamp(sumCol, 0.0, 1.0);
            a = uTransparent > 0 ? cover : 1.0;
        } else {
            vec2 s = q;
            for (int k = 0; k < 3; ++k) {
                s -= 0.01;
                vec2 r = sin(1.5 * (s.yx * uFrequency) + 2.0 * cos(s * uFrequency));
                float m0 = length(r + sin(5.0 * r.y * uFrequency - 3.0 * t + float(k)) / 4.0);
                float kBelow = clamp(uWarpStrength, 0.0, 1.0);
                float kMix = pow(kBelow, 0.3);
                float gain = 1.0 + max(uWarpStrength - 1.0, 0.0);
                vec2 disp = (r - s) * kBelow;
                vec2 warped = s + disp * gain;
                float m1 = length(warped + sin(5.0 * warped.y * uFrequency - 3.0 * t + float(k)) / 4.0);
                float m = mix(m0, m1, kMix);
                col[k] = 1.0 - exp(-6.0 / exp(6.0 * m));
            }
            a = uTransparent > 0 ? max(max(col.r, col.g), col.b) : 1.0;
        }

        if (uNoise > 0.0001) {
            float n = fract(sin(dot(fragCoord + vec2(uTime), vec2(12.9898, 78.233))) * 43758.5453123);
            col += (n - 0.5) * uNoise;
            col = clamp(col, 0.0, 1.0);
        }

        vec3 rgb = (uTransparent > 0) ? col * a : col;
        
        // 2. The output is 'return' instead of 'gl_FragColor'
        return vec4(rgb, a);
    }
"""