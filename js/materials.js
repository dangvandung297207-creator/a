import * as THREE from 'three';

/**
 * Material library for Đoản Kiếm Hư Vô.
 * Separate surfaces: Netherite, dark metal, void cracks, emissive, core, runes.
 * Only cracks / runes / core / fragments emit — body stays dark metal.
 */

function makeFlowCrackMaterial() {
  // Flowing energy through void cracks via simple UV scroll + pulse
  const mat = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uIntensity: { value: 1.0 },
      uBaseColor: { value: new THREE.Color(0x0a0414) },
      uGlowColor: { value: new THREE.Color(0xb45cff) },
      uHotColor: { value: new THREE.Color(0xff8af0) },
      uOpacity: { value: 0.95 },
    },
    vertexShader: /* glsl */ `
      varying vec3 vPos;
      varying vec3 vNormal;
      void main() {
        vPos = position;
        vNormal = normalize(normalMatrix * normal);
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: /* glsl */ `
      uniform float uTime;
      uniform float uIntensity;
      uniform vec3 uBaseColor;
      uniform vec3 uGlowColor;
      uniform vec3 uHotColor;
      uniform float uOpacity;
      varying vec3 vPos;
      varying vec3 vNormal;

      void main() {
        // Flow along blade height + jagged noise
        float flow = sin(vPos.y * 18.0 - uTime * 3.4 + vPos.x * 22.0) * 0.5 + 0.5;
        float pulse = 0.72 + 0.28 * sin(uTime * 2.1);
        float edge = pow(1.0 - abs(dot(normalize(vNormal), vec3(0.0, 0.0, 1.0))), 1.4);
        float energy = mix(flow, 1.0, 0.25) * pulse * uIntensity;
        vec3 col = mix(uBaseColor, uGlowColor, energy);
        col = mix(col, uHotColor, energy * energy * 0.55);
        col += uGlowColor * edge * 0.35 * uIntensity;
        float alpha = uOpacity * (0.55 + 0.45 * energy);
        gl_FragColor = vec4(col, alpha);
      }
    `,
    transparent: true,
    depthWrite: false,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
  });
  return mat;
}

function makeRuneMaterial() {
  const mat = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uIntensity: { value: 1.0 },
      uColor: { value: new THREE.Color(0xa040ff) },
    },
    vertexShader: /* glsl */ `
      varying vec2 vUv;
      void main() {
        vUv = uv;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: /* glsl */ `
      uniform float uTime;
      uniform float uIntensity;
      uniform vec3 uColor;
      varying vec2 vUv;
      void main() {
        float pulse = 0.75 + 0.25 * sin(uTime * 2.8 + vUv.y * 10.0);
        float core = smoothstep(0.15, 0.5, 1.0 - length(vUv - 0.5) * 1.6);
        vec3 col = uColor * (0.6 + core) * pulse * uIntensity;
        gl_FragColor = vec4(col, 0.85 * pulse);
      }
    `,
    transparent: true,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
  });
  return mat;
}

export function createMaterials() {
  const mats = {};

  // Netherite blade body — subtle roughness variation via two layers
  mats.blade = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x16121c),
    metalness: 0.94,
    roughness: 0.36,
    envMapIntensity: 1.35,
    flatShading: true,
  });

  mats.bladeDark = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0a080e),
    metalness: 0.88,
    roughness: 0.48,
    envMapIntensity: 0.9,
    flatShading: true,
  });

  mats.bladeBevel = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a2434),
    metalness: 0.96,
    roughness: 0.22,
    envMapIntensity: 1.5,
    flatShading: true,
  });

  // Sharper edge — restrained violet kiss only
  mats.bladeEdge = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1e1428),
    metalness: 0.97,
    roughness: 0.14,
    emissive: new THREE.Color(0x4a1a8a),
    emissiveIntensity: 0.18,
    flatShading: true,
  });

  // Void cracks — flowing shader
  mats.voidCracks = makeFlowCrackMaterial();
  mats.voidCracksSolid = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0a0414),
    metalness: 0.3,
    roughness: 0.55,
    emissive: new THREE.Color(0xb45cff),
    emissiveIntensity: 1.1,
    flatShading: true,
  });

  // Guard — angular dark netherite
  mats.guard = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x12101a),
    metalness: 0.9,
    roughness: 0.34,
    flatShading: true,
  });

  mats.guardChannel = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0c0618),
    metalness: 0.5,
    roughness: 0.4,
    emissive: new THREE.Color(0x6b2ad1),
    emissiveIntensity: 0.55,
    flatShading: true,
  });

  // Purple crystal / end fragments (controlled glow)
  mats.crystal = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x4a1480),
    metalness: 0.12,
    roughness: 0.18,
    emissive: new THREE.Color(0x8a2be2),
    emissiveIntensity: 0.55,
    transparent: true,
    opacity: 0.88,
    flatShading: true,
  });

  // Handle wrap — leather-like dark
  mats.handle = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0e0a14),
    metalness: 0.18,
    roughness: 0.82,
    flatShading: true,
  });

  mats.handleWrap = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a1224),
    metalness: 0.12,
    roughness: 0.88,
    flatShading: true,
  });

  // Metallic fittings
  mats.handleMetal = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2c2438),
    metalness: 0.96,
    roughness: 0.22,
    flatShading: true,
  });

  mats.pommel = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x14101c),
    metalness: 0.92,
    roughness: 0.28,
    flatShading: true,
  });

  // Ender core
  mats.coreShell = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a0a30),
    metalness: 0.08,
    roughness: 0.12,
    emissive: new THREE.Color(0x3a1070),
    emissiveIntensity: 0.4,
    transparent: true,
    opacity: 0.5,
    flatShading: false,
  });

  mats.coreInner = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a0848),
    emissive: new THREE.Color(0xd070ff),
    emissiveIntensity: 2.0,
    metalness: 0.0,
    roughness: 0.35,
    transparent: true,
    opacity: 0.9,
  });

  mats.coreIris = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0a0214),
    emissive: new THREE.Color(0xffa0ff),
    emissiveIntensity: 1.6,
    metalness: 0.2,
    roughness: 0.3,
  });

  // Runes
  mats.runes = makeRuneMaterial();
  mats.runesSolid = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a0a28),
    emissive: new THREE.Color(0xa040ff),
    emissiveIntensity: 1.4,
    metalness: 0.3,
    roughness: 0.4,
    flatShading: true,
  });

  // Controlled emissive accents (tip / wing tips / fragments only)
  mats.emissive = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x140820),
    emissive: new THREE.Color(0xb45cff),
    emissiveIntensity: 1.4,
    metalness: 0.15,
    roughness: 0.35,
    flatShading: true,
  });

  mats.fragment = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a0a28),
    emissive: new THREE.Color(0x8040c0),
    emissiveIntensity: 0.7,
    metalness: 0.4,
    roughness: 0.4,
    transparent: true,
    opacity: 0.85,
    flatShading: true,
  });

  // Afterimage ghost
  mats.afterimage = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x8a40d0),
    transparent: true,
    opacity: 0.32,
    depthWrite: false,
    side: THREE.DoubleSide,
  });

  // Slash trail ribbon
  mats.trail = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xb45cff),
    transparent: true,
    opacity: 0.55,
    side: THREE.DoubleSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
  });

  mats.trailHot = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xffb0ff),
    transparent: true,
    opacity: 0.7,
    side: THREE.DoubleSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending,
  });

  // Rift / crack interior
  mats.riftVoid = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x03010a),
    transparent: true,
    opacity: 0.94,
    side: THREE.DoubleSide,
  });

  mats.riftEdge = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xc070ff),
    transparent: true,
    opacity: 0.9,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  mats.riftFracture = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xff6ae0),
    transparent: true,
    opacity: 0.75,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  // Environment
  mats.obsidian = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0a0610),
    metalness: 0.15,
    roughness: 0.85,
    flatShading: true,
  });
  mats.endStone = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0xc9c17a),
    metalness: 0.05,
    roughness: 0.92,
    flatShading: true,
  });
  mats.purpur = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x8a6a9a),
    metalness: 0.1,
    roughness: 0.8,
    flatShading: true,
  });
  mats.chorus = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x5a3a70),
    metalness: 0.05,
    roughness: 0.7,
    flatShading: true,
  });

  // Mannequin
  mats.skin = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0xc4a07a),
    metalness: 0.05,
    roughness: 0.75,
    flatShading: true,
  });
  mats.cloth = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a1228),
    metalness: 0.1,
    roughness: 0.8,
    flatShading: true,
  });
  mats.armor = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a1a38),
    metalness: 0.7,
    roughness: 0.4,
    flatShading: true,
  });

  // Echo projectile
  mats.echoShell = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x120820),
    metalness: 0.25,
    roughness: 0.2,
    emissive: new THREE.Color(0x5a1890),
    emissiveIntensity: 0.7,
    transparent: true,
    opacity: 0.65,
  });
  mats.echoCore = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xe8b0ff),
    transparent: true,
    opacity: 0.95,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });
  mats.echoVoid = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x020006),
  });
  mats.echoRing = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xc070ff),
    transparent: true,
    opacity: 0.7,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  // Anchor
  mats.anchorRing = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xa050ff),
    transparent: true,
    opacity: 0.7,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  // Mark rune
  mats.mark = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xc060ff),
    transparent: true,
    opacity: 0.8,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });
  mats.markHot = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xff80f0),
    transparent: true,
    opacity: 0.9,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  return mats;
}

/** Drive shader uniforms + restrained emissive pulses. */
export function updateMaterials(mats, t, intensity = 1.0) {
  if (mats.voidCracks?.uniforms) {
    mats.voidCracks.uniforms.uTime.value = t;
    mats.voidCracks.uniforms.uIntensity.value = intensity;
  }
  if (mats.runes?.uniforms) {
    mats.runes.uniforms.uTime.value = t;
    mats.runes.uniforms.uIntensity.value = 0.7 + intensity * 0.5;
  }
  if (mats.coreInner) {
    mats.coreInner.emissiveIntensity = 1.7 + Math.sin(t * 2.4) * 0.35 * intensity;
  }
  if (mats.emissive) {
    mats.emissive.emissiveIntensity = 1.1 + Math.sin(t * 1.8) * 0.25 * intensity;
  }
  if (mats.guardChannel) {
    mats.guardChannel.emissiveIntensity = 0.4 + Math.sin(t * 2.0) * 0.15 * intensity;
  }
  if (mats.bladeEdge) {
    mats.bladeEdge.emissiveIntensity = 0.12 + 0.1 * intensity + Math.sin(t * 1.5) * 0.04;
  }
}

export function pulseEmissive(mat, t, base = 1.0, amp = 0.45, speed = 2.2) {
  if (!mat || mat.emissiveIntensity === undefined) return;
  mat.emissiveIntensity = base + Math.sin(t * speed) * amp;
}

export function setCrackIntensity(mats, value) {
  if (mats.voidCracks?.uniforms) {
    mats.voidCracks.uniforms.uIntensity.value = value;
  }
  if (mats.voidCracksSolid) {
    mats.voidCracksSolid.emissiveIntensity = 0.8 + value * 0.9;
  }
}
