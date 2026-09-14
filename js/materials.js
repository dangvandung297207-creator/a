import * as THREE from 'three';

/**
 * Material library for Đoản Kiếm Hư Vô.
 * Palette: BLACK + DARK PURPLE + VIOLET + END ENERGY
 */
export function createMaterials() {
  const mats = {};

  // Dark netherite-like blade body
  mats.blade = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a1520),
    metalness: 0.92,
    roughness: 0.32,
    envMapIntensity: 1.2,
    flatShading: true,
  });

  // Sharper edge with subtle violet emissive
  mats.bladeEdge = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a1a38),
    metalness: 0.95,
    roughness: 0.18,
    emissive: new THREE.Color(0x6b2ad1),
    emissiveIntensity: 0.35,
    flatShading: true,
  });

  // Void cracks — animated emissive flow
  mats.voidCracks = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0a0414),
    metalness: 0.4,
    roughness: 0.55,
    emissive: new THREE.Color(0xb45cff),
    emissiveIntensity: 1.4,
    flatShading: true,
    transparent: true,
    opacity: 0.95,
  });

  // Guard — dark netherite
  mats.guard = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x14101a),
    metalness: 0.88,
    roughness: 0.38,
    flatShading: true,
  });

  // Purple crystal fragments on guard
  mats.crystal = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x5a1a9a),
    metalness: 0.15,
    roughness: 0.2,
    emissive: new THREE.Color(0x8a2be2),
    emissiveIntensity: 0.7,
    transparent: true,
    opacity: 0.9,
    flatShading: true,
  });

  // Handle wrap
  mats.handle = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x0c0812),
    metalness: 0.25,
    roughness: 0.78,
    flatShading: true,
  });

  // Metallic handle rings / fittings
  mats.handleMetal = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a2035),
    metalness: 0.95,
    roughness: 0.25,
    flatShading: true,
  });

  // Pommel shell
  mats.pommel = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x16101f),
    metalness: 0.9,
    roughness: 0.3,
    flatShading: true,
  });

  // Ender core outer shell (glass-like dark)
  mats.coreShell = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a0a30),
    metalness: 0.1,
    roughness: 0.15,
    emissive: new THREE.Color(0x4a1a8a),
    emissiveIntensity: 0.5,
    transparent: true,
    opacity: 0.55,
    flatShading: false,
  });

  // Inner energy sphere
  mats.coreInner = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x2a0848),
    emissive: new THREE.Color(0xc060ff),
    emissiveIntensity: 2.2,
    metalness: 0.0,
    roughness: 0.4,
    transparent: true,
    opacity: 0.85,
  });

  // Glowing runes on handle
  mats.runes = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x1a0a28),
    emissive: new THREE.Color(0xa040ff),
    emissiveIntensity: 1.6,
    metalness: 0.3,
    roughness: 0.4,
    flatShading: true,
  });

  // General emissive accent
  mats.emissive = new THREE.MeshStandardMaterial({
    color: new THREE.Color(0x180828),
    emissive: new THREE.Color(0xb45cff),
    emissiveIntensity: 1.8,
    metalness: 0.2,
    roughness: 0.35,
    flatShading: true,
  });

  // Afterimage ghost
  mats.afterimage = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x8a40d0),
    transparent: true,
    opacity: 0.35,
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

  // Rift / crack interior
  mats.riftVoid = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x05020a),
    transparent: true,
    opacity: 0.92,
    side: THREE.DoubleSide,
  });

  mats.riftEdge = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xc070ff),
    transparent: true,
    opacity: 0.85,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  // Environment blocks
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
    color: new THREE.Color(0x1a0830),
    metalness: 0.2,
    roughness: 0.25,
    emissive: new THREE.Color(0x6a20b0),
    emissiveIntensity: 0.8,
    transparent: true,
    opacity: 0.7,
  });
  mats.echoCore = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0xe0a0ff),
    transparent: true,
    opacity: 0.95,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });
  mats.echoVoid = new THREE.MeshBasicMaterial({
    color: new THREE.Color(0x020006),
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
    opacity: 0.75,
    side: THREE.DoubleSide,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  });

  return mats;
}

/** Soft pulse helper for emissive materials */
export function pulseEmissive(mat, t, base = 1.0, amp = 0.45, speed = 2.2) {
  if (!mat || mat.emissiveIntensity === undefined) return;
  mat.emissiveIntensity = base + Math.sin(t * speed) * amp;
}
