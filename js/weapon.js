import * as THREE from 'three';

/**
 * Ender Blade (Đoản Kiếm Hư Vô) — refined legendary silhouette.
 *
 * Hierarchy (clean pivots for Blender / MC conversion):
 *   EnderBlade_Root
 *     ├── Blade
 *     ├── Blade_Edge
 *     ├── Void_Cracks
 *     ├── Guard
 *     ├── Handle
 *     ├── Pommel
 *     ├── Ender_Core
 *     ├── Runes
 *     ├── EmissiveParts
 *     ├── Dimensional_Fragments
 *     └── VFXAttachments
 *
 * Units: ~1 unit ≈ player height. Blade ~1.28 long for elegant MC hand feel.
 */
export function createEnderBlade(mats) {
  const root = new THREE.Group();
  root.name = 'EnderBlade_Root';

  const box = (w, h, d, mat, x = 0, y = 0, z = 0) => {
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.position.set(x, y, z);
    m.castShadow = true;
    m.receiveShadow = true;
    return m;
  };

  // =========================================================================
  // BLADE — longer, sharper, slightly asymmetrical fantasy silhouette
  // =========================================================================
  const Blade = new THREE.Group();
  Blade.name = 'Blade';

  // Stacked tapered segments (voxel-faithful, refined proportions)
  // Tip (needle-sharp)
  Blade.add(box(0.028, 0.07, 0.014, mats.bladeBevel, 0.004, 1.28));
  Blade.add(box(0.042, 0.08, 0.016, mats.blade, 0.006, 1.21));
  Blade.add(box(0.058, 0.09, 0.018, mats.blade, 0.008, 1.13));
  // Upper — slight right bias for asymmetry
  Blade.add(box(0.078, 0.11, 0.020, mats.blade, 0.010, 1.03));
  Blade.add(box(0.092, 0.12, 0.022, mats.blade, 0.008, 0.92));
  Blade.add(box(0.102, 0.13, 0.023, mats.blade, 0.004, 0.80));
  // Mid
  Blade.add(box(0.112, 0.14, 0.024, mats.blade, 0.0, 0.67));
  Blade.add(box(0.118, 0.14, 0.025, mats.blade, -0.002, 0.54));
  Blade.add(box(0.122, 0.13, 0.026, mats.blade, -0.004, 0.41));
  // Lower toward guard — slightly wider base
  Blade.add(box(0.128, 0.12, 0.027, mats.blade, -0.002, 0.29));
  Blade.add(box(0.134, 0.10, 0.028, mats.blade, 0.0, 0.18));
  Blade.add(box(0.140, 0.08, 0.030, mats.blade, 0.0, 0.09));
  Blade.add(box(0.145, 0.06, 0.032, mats.bladeDark, 0.0, 0.03));

  // Geometric bevels / fuller (darker inset groove)
  Blade.add(box(0.028, 0.88, 0.007, mats.bladeDark, 0.0, 0.62, 0.014));
  Blade.add(box(0.028, 0.88, 0.007, mats.bladeDark, 0.0, 0.62, -0.014));
  // Secondary thin bevels
  Blade.add(box(0.012, 0.70, 0.005, mats.bladeBevel, -0.035, 0.58, 0.013));
  Blade.add(box(0.012, 0.70, 0.005, mats.bladeBevel, 0.038, 0.58, 0.013));

  // Tip dimensional fractures (small broken geometry near tip)
  const tipFractures = [
    [0.03, 1.18, 0.012, 0.02, 0.035, 0.01, 0.4],
    [-0.025, 1.22, -0.01, 0.018, 0.03, 0.008, -0.3],
    [0.015, 1.26, 0.008, 0.014, 0.022, 0.007, 0.55],
    [-0.01, 1.15, 0.014, 0.016, 0.028, 0.009, -0.2],
  ];
  tipFractures.forEach(([x, y, z, w, h, d, rz]) => {
    const f = box(w, h, d, mats.bladeDark, x, y, z);
    f.rotation.z = rz;
    Blade.add(f);
  });

  // Micro surface instability (restrained — not messy)
  for (let i = 0; i < 5; i++) {
    const s = 0.014 + (i % 3) * 0.006;
    const dm = box(
      s, s * 1.5, s * 0.5, mats.bladeDark,
      (i % 2 === 0 ? 1 : -1) * (0.04 + i * 0.008),
      0.35 + i * 0.14,
      (i % 2 === 0 ? 1 : -1) * 0.017
    );
    dm.rotation.z = (i - 2) * 0.12;
    Blade.add(dm);
  }

  // =========================================================================
  // BLADE EDGE — sharp visual rim, restrained emissive
  // =========================================================================
  const Blade_Edge = new THREE.Group();
  Blade_Edge.name = 'Blade_Edge';

  // Asymmetrical edge chains (left thinner / longer, right slightly offset)
  const leftEdge = [
    [1.26, 0.012, 0.06], [1.14, 0.014, 0.08], [1.00, 0.016, 0.09],
    [0.86, 0.017, 0.10], [0.72, 0.018, 0.10], [0.58, 0.018, 0.10],
    [0.44, 0.019, 0.10], [0.30, 0.020, 0.09], [0.16, 0.022, 0.08], [0.06, 0.024, 0.06],
  ];
  leftEdge.forEach(([y, w, h], i) => {
    Blade_Edge.add(box(w, h, 0.007, mats.bladeEdge, -0.052 - i * 0.0025, y, 0));
  });
  const rightEdge = [
    [1.24, 0.012, 0.055], [1.12, 0.014, 0.075], [0.98, 0.016, 0.085],
    [0.84, 0.017, 0.095], [0.70, 0.018, 0.095], [0.56, 0.018, 0.095],
    [0.42, 0.019, 0.09], [0.28, 0.020, 0.085], [0.14, 0.022, 0.07], [0.05, 0.024, 0.055],
  ];
  rightEdge.forEach(([y, w, h], i) => {
    Blade_Edge.add(box(w, h, 0.007, mats.bladeEdge, 0.058 + i * 0.002, y, 0));
  });
  // Tip point
  Blade_Edge.add(box(0.04, 0.035, 0.009, mats.bladeEdge, 0.005, 1.30, 0));
  Blade_Edge.add(box(0.022, 0.025, 0.008, mats.emissive, 0.005, 1.315, 0));

  // =========================================================================
  // VOID CRACKS — deep purple energy trapped in metal
  // =========================================================================
  const Void_Cracks = new THREE.Group();
  Void_Cracks.name = 'Void_Cracks';

  const crackPaths = [
    // Primary diagonal (front)
    [
      [-0.018, 1.10, 0.015], [0.012, 0.96, 0.015], [-0.02, 0.82, 0.015],
      [0.018, 0.68, 0.015], [-0.014, 0.54, 0.015], [0.022, 0.40, 0.015],
      [-0.01, 0.28, 0.015], [0.016, 0.16, 0.015],
    ],
    // Secondary branch toward edge
    [
      [0.025, 1.02, 0.015], [0.038, 0.88, 0.015], [0.028, 0.74, 0.015],
      [0.042, 0.58, 0.015], [0.030, 0.44, 0.015],
    ],
    // Reverse face main
    [
      [0.01, 1.06, -0.015], [-0.018, 0.90, -0.015], [0.014, 0.74, -0.015],
      [-0.02, 0.58, -0.015], [0.012, 0.42, -0.015], [-0.008, 0.26, -0.015],
      [0.018, 0.14, -0.015],
    ],
    // Tip fractures (energy leaking near tip)
    [
      [0.0, 1.22, 0.015], [0.02, 1.16, 0.015], [-0.015, 1.12, 0.015],
    ],
    [
      [0.005, 1.20, -0.015], [-0.018, 1.14, -0.015],
    ],
  ];

  crackPaths.forEach((path) => {
    for (let i = 0; i < path.length - 1; i++) {
      const a = new THREE.Vector3(...path[i]);
      const b = new THREE.Vector3(...path[i + 1]);
      const mid = a.clone().add(b).multiplyScalar(0.5);
      const len = a.distanceTo(b);
      const seg = box(0.011, len * 1.08, 0.0055, mats.voidCracks, mid.x, mid.y, mid.z);
      const dir = b.clone().sub(a).normalize();
      const quat = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir);
      seg.quaternion.copy(quat);
      seg.userData.crackSeg = true;
      Void_Cracks.add(seg);
    }
    path.forEach((p, pi) => {
      const node = box(0.016, 0.016, 0.009, mats.voidCracks, p[0], p[1], p[2]);
      node.userData.crackNode = true;
      node.userData.nodePhase = pi * 0.7;
      Void_Cracks.add(node);
    });
  });

  // =========================================================================
  // GUARD — angular Netherite, End-themed, energy channels
  // =========================================================================
  const Guard = new THREE.Group();
  Guard.name = 'Guard';

  // Main crossbar — angular, slightly upswept
  Guard.add(box(0.46, 0.055, 0.085, mats.guard, 0, 0.0, 0));
  Guard.add(box(0.14, 0.075, 0.10, mats.guard, 0, 0.015, 0));

  // Asymmetrical wings (left longer / right higher)
  Guard.add(box(0.09, 0.11, 0.06, mats.guard, -0.22, 0.055, 0));
  Guard.add(box(0.07, 0.13, 0.055, mats.guard, 0.21, 0.07, 0));
  Guard.add(box(0.05, 0.09, 0.048, mats.guard, -0.27, 0.12, 0));
  Guard.add(box(0.045, 0.10, 0.045, mats.guard, 0.26, 0.14, 0));
  // Wing tips fold
  Guard.add(box(0.035, 0.06, 0.035, mats.guard, -0.30, 0.17, 0.01));
  Guard.add(box(0.032, 0.055, 0.032, mats.guard, 0.29, 0.19, -0.01));

  // Lower End-themed plate
  Guard.add(box(0.20, 0.04, 0.12, mats.guard, 0, -0.04, 0));
  Guard.add(box(0.10, 0.03, 0.14, mats.guard, 0, -0.055, 0));

  // Purple energy channels through guard
  Guard.add(box(0.38, 0.012, 0.018, mats.guardChannel, 0, 0.0, 0.04));
  Guard.add(box(0.012, 0.10, 0.018, mats.guardChannel, -0.18, 0.06, 0.03));
  Guard.add(box(0.012, 0.12, 0.018, mats.guardChannel, 0.17, 0.07, 0.03));

  // Ender-eye motif on guard face
  Guard.add(box(0.09, 0.09, 0.04, mats.handleMetal, 0, 0.02, 0.065));
  Guard.add(box(0.055, 0.055, 0.03, mats.emissive, 0, 0.02, 0.085));
  Guard.add(box(0.028, 0.028, 0.02, mats.coreIris, 0, 0.02, 0.10));

  // Crystal fragments (restrained)
  const crystals = [
    [-0.15, 0.06, 0.05, 0.028, 0.04, 0.028],
    [0.15, 0.07, 0.05, 0.025, 0.038, 0.025],
    [-0.20, 0.0, -0.04, 0.03, 0.035, 0.03],
    [0.19, 0.02, -0.04, 0.028, 0.032, 0.028],
  ];
  crystals.forEach(([x, y, z, w, h, d], i) => {
    const c = box(w, h, d, mats.crystal, x, y, z);
    c.rotation.set(0.25 * i, 0.4 * i, 0.15);
    Guard.add(c);
  });

  // =========================================================================
  // HANDLE — detailed wrap, metal reinforcement, MC hand proportions
  // =========================================================================
  const Handle = new THREE.Group();
  Handle.name = 'Handle';

  // Top collar
  Handle.add(box(0.085, 0.04, 0.085, mats.handleMetal, 0, -0.055, 0));
  Handle.add(box(0.07, 0.02, 0.07, mats.guardChannel, 0, -0.075, 0));

  // Wrapped grip — alternating wrap + ridge
  for (let i = 0; i < 6; i++) {
    const y = -0.11 - i * 0.07;
    const wrapMat = i % 2 === 0 ? mats.handle : mats.handleWrap;
    Handle.add(box(0.052, 0.06, 0.052, wrapMat, 0, y, 0));
    // Diagonal wrap ridge suggestion
    Handle.add(box(0.058, 0.012, 0.058, mats.handleMetal, 0, y - 0.028, 0));
    // Slight oval feel
    if (i % 2 === 1) {
      Handle.add(box(0.056, 0.05, 0.048, wrapMat, 0, y, 0));
    }
  }

  // Bottom collar
  Handle.add(box(0.078, 0.032, 0.078, mats.handleMetal, 0, -0.54, 0));
  Handle.add(box(0.06, 0.015, 0.06, mats.guardChannel, 0, -0.555, 0));

  // =========================================================================
  // RUNES — glowing End glyphs on handle
  // =========================================================================
  const Runes = new THREE.Group();
  Runes.name = 'Runes';

  const runeYs = [-0.13, -0.20, -0.27, -0.34, -0.41, -0.48];
  runeYs.forEach((y, i) => {
    // Front glyph body
    Runes.add(box(0.022, 0.028, 0.01, mats.runes, 0, y, 0.03));
    // Glyph crossbar
    Runes.add(box(0.032, 0.008, 0.008, mats.runes, 0, y + 0.005, 0.032));
    if (i % 2 === 0) {
      Runes.add(box(0.01, 0.02, 0.02, mats.runes, 0.032, y, 0));
      Runes.add(box(0.01, 0.02, 0.02, mats.runes, -0.032, y, 0));
    }
    // Tiny side ticks
    if (i % 3 === 0) {
      Runes.add(box(0.008, 0.014, 0.008, mats.runes, 0, y, -0.03));
    }
  });

  // =========================================================================
  // POMMEL + ENDER CORE — Eye of Ender inspired
  // =========================================================================
  const Pommel = new THREE.Group();
  Pommel.name = 'Pommel';

  Pommel.add(box(0.095, 0.045, 0.095, mats.pommel, 0, -0.58, 0));
  Pommel.add(box(0.075, 0.035, 0.075, mats.pommel, 0, -0.615, 0));
  // Cage frame around core
  const cageBars = [
    [0.042, 0, 0.015, 0.085, 0.015],
    [-0.042, 0, 0.015, 0.085, 0.015],
    [0, 0.042, 0.015, 0.015, 0.085],
    [0, -0.042, 0.015, 0.015, 0.085],
    [0.03, 0.03, 0.012, 0.07, 0.012],
    [-0.03, -0.03, 0.012, 0.07, 0.012],
  ];
  cageBars.forEach(([x, z, w, h, d]) => {
    Pommel.add(box(w, h, d, mats.handleMetal, x, -0.67, z));
  });
  Pommel.add(box(0.065, 0.018, 0.065, mats.pommel, 0, -0.735, 0));
  // Bottom spike tip
  Pommel.add(box(0.03, 0.025, 0.03, mats.handleMetal, 0, -0.76, 0));

  const Ender_Core = new THREE.Group();
  Ender_Core.name = 'Ender_Core';
  Ender_Core.position.set(0, -0.67, 0);

  const shell = new THREE.Mesh(new THREE.IcosahedronGeometry(0.042, 1), mats.coreShell);
  shell.name = 'CoreShell';
  Ender_Core.add(shell);

  const inner = new THREE.Mesh(new THREE.IcosahedronGeometry(0.024, 1), mats.coreInner);
  inner.name = 'CoreInner';
  Ender_Core.add(inner);

  // Iris (Eye of Ender pupil)
  const iris = new THREE.Mesh(new THREE.SphereGeometry(0.012, 8, 8), mats.coreIris);
  iris.name = 'CoreIris';
  Ender_Core.add(iris);

  // Orbiting energy bits
  for (let i = 0; i < 5; i++) {
    const bit = box(0.007, 0.007, 0.007, mats.emissive);
    bit.userData.orbit = {
      angle: (i / 5) * Math.PI * 2,
      radius: 0.032 + (i % 2) * 0.006,
      speed: 1.3 + i * 0.18,
      yOff: (i - 2) * 0.004,
    };
    bit.name = 'CoreBit' + i;
    Ender_Core.add(bit);
  }

  // =========================================================================
  // EMISSIVE PARTS — only tip, wing tips, controlled accents
  // =========================================================================
  const EmissiveParts = new THREE.Group();
  EmissiveParts.name = 'EmissiveParts';
  EmissiveParts.add(box(0.028, 0.045, 0.012, mats.emissive, 0.005, 1.30, 0));
  EmissiveParts.add(box(0.028, 0.028, 0.028, mats.emissive, -0.30, 0.18, 0));
  EmissiveParts.add(box(0.028, 0.028, 0.028, mats.emissive, 0.29, 0.20, 0));
  // Tiny edge energy near tip fractures
  EmissiveParts.add(box(0.012, 0.02, 0.01, mats.emissive, 0.03, 1.20, 0.012));
  EmissiveParts.add(box(0.012, 0.018, 0.01, mats.emissive, -0.025, 1.18, -0.01));

  // =========================================================================
  // DIMENSIONAL FRAGMENTS — tiny floating bits around blade (idle life)
  // =========================================================================
  const Dimensional_Fragments = new THREE.Group();
  Dimensional_Fragments.name = 'Dimensional_Fragments';
  for (let i = 0; i < 8; i++) {
    const s = 0.012 + (i % 3) * 0.006;
    const f = box(s, s * (0.6 + (i % 2) * 0.4), s * 0.5, mats.fragment);
    f.userData.frag = {
      phase: i * 0.9,
      radius: 0.12 + (i % 4) * 0.04,
      height: 0.25 + i * 0.12,
      speed: 0.4 + (i % 3) * 0.15,
      spin: 0.3 + i * 0.05,
    };
    // Start positions
    const a = i * 0.9;
    f.position.set(Math.cos(a) * 0.14, 0.3 + i * 0.1, Math.sin(a) * 0.08);
    Dimensional_Fragments.add(f);
  }

  // =========================================================================
  // VFX ATTACHMENTS — empty sockets for trails / slash origins
  // =========================================================================
  const VFXAttachments = new THREE.Group();
  VFXAttachments.name = 'VFXAttachments';

  const tipSocket = new THREE.Object3D();
  tipSocket.name = 'TipSocket';
  tipSocket.position.set(0, 1.32, 0);
  VFXAttachments.add(tipSocket);

  const guardSocket = new THREE.Object3D();
  guardSocket.name = 'GuardSocket';
  guardSocket.position.set(0, 0, 0);
  VFXAttachments.add(guardSocket);

  const coreSocket = new THREE.Object3D();
  coreSocket.name = 'CoreSocket';
  coreSocket.position.set(0, -0.67, 0);
  VFXAttachments.add(coreSocket);

  const slashSocket = new THREE.Object3D();
  slashSocket.name = 'SlashSocket';
  slashSocket.position.set(0, 0.7, 0);
  VFXAttachments.add(slashSocket);

  // Assemble
  root.add(Blade);
  root.add(Blade_Edge);
  root.add(Void_Cracks);
  root.add(Guard);
  root.add(Handle);
  root.add(Pommel);
  root.add(Ender_Core);
  root.add(Runes);
  root.add(EmissiveParts);
  root.add(Dimensional_Fragments);
  root.add(VFXAttachments);

  root.userData.parts = {
    Blade,
    Blade_Edge,
    Void_Cracks,
    Guard,
    Handle,
    Pommel,
    Ender_Core,
    Runes,
    EmissiveParts,
    Dimensional_Fragments,
    VFXAttachments,
  };

  root.userData.sockets = {
    tip: tipSocket,
    guard: guardSocket,
    core: coreSocket,
    slash: slashSocket,
  };

  // Rest orientation
  root.rotation.set(0, 0, 0);

  return root;
}

/**
 * Lightweight ghost clone for afterimages.
 */
export function createAfterimage(sourceRoot, mats) {
  const ghost = sourceRoot.clone(true);
  ghost.traverse((o) => {
    if (o.isMesh) {
      o.material = mats.afterimage.clone();
      o.castShadow = false;
      o.receiveShadow = false;
    }
  });
  ghost.name = 'Afterimage';
  return ghost;
}

/**
 * Idle life: core spin, orbit bits, floating fragments, subtle weapon sway.
 * Returns crack intensity multiplier suggestion.
 */
export function updateWeaponIdle(root, t, intensity = 1.0) {
  const parts = root.userData.parts;
  if (!parts) return 1;

  // Ender core rotation
  const core = parts.Ender_Core;
  if (core) {
    core.rotation.y = t * 0.9;
    core.rotation.x = Math.sin(t * 0.7) * 0.15;
    core.rotation.z = Math.cos(t * 0.5) * 0.08;
    core.children.forEach((c) => {
      const orb = c.userData.orbit;
      if (!orb) return;
      const a = orb.angle + t * orb.speed;
      c.position.set(
        Math.cos(a) * orb.radius,
        Math.sin(a * 1.3) * 0.01 + orb.yOff,
        Math.sin(a) * orb.radius
      );
    });
  }

  // Floating dimensional fragments
  const frags = parts.Dimensional_Fragments;
  if (frags) {
    frags.children.forEach((f) => {
      const d = f.userData.frag;
      if (!d) return;
      const a = t * d.speed + d.phase;
      f.position.x = Math.cos(a) * d.radius;
      f.position.z = Math.sin(a * 0.8) * d.radius * 0.6;
      f.position.y = d.height + Math.sin(t * 1.2 + d.phase) * 0.04;
      f.rotation.x += d.spin * 0.02;
      f.rotation.y += d.spin * 0.03;
      f.visible = intensity > 0.15;
    });
  }

  // Crack node pulse scale
  if (parts.Void_Cracks) {
    parts.Void_Cracks.children.forEach((c) => {
      if (c.userData.crackNode) {
        const p = 1 + Math.sin(t * 3.0 + c.userData.nodePhase) * 0.12 * intensity;
        c.scale.setScalar(p);
      }
    });
  }

  return intensity;
}
