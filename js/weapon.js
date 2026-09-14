import * as THREE from 'three';

/**
 * Builds the Ender Blade (Đoản Kiếm Hư Vô) as separated logical objects
 * under EnderBlade_Root, with clean pivots for animation.
 *
 * Units: ~1 unit ≈ player height scale. Blade roughly 1.15 long for MC hand feel.
 */
export function createEnderBlade(mats) {
  const root = new THREE.Group();
  root.name = 'EnderBlade_Root';

  // ---- helpers ------------------------------------------------------------
  const box = (w, h, d, mat, x = 0, y = 0, z = 0) => {
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.position.set(x, y, z);
    m.castShadow = true;
    m.receiveShadow = true;
    return m;
  };

  // Slightly tapered "blade segment" using scaled box
  const bladeSeg = (w, h, d, mat, y, zOff = 0) => box(w, h, d, mat, 0, y, zOff);

  // ---- BLADE body ---------------------------------------------------------
  const Blade = new THREE.Group();
  Blade.name = 'Blade';

  // Long slightly-narrow blade stacked from tip to base (voxel style)
  // Tip
  Blade.add(bladeSeg(0.04, 0.08, 0.018, mats.blade, 1.12));
  Blade.add(bladeSeg(0.06, 0.10, 0.020, mats.blade, 1.04));
  Blade.add(bladeSeg(0.08, 0.12, 0.022, mats.blade, 0.94));
  // Mid upper
  Blade.add(bladeSeg(0.10, 0.16, 0.024, mats.blade, 0.80));
  Blade.add(bladeSeg(0.11, 0.18, 0.025, mats.blade, 0.64));
  // Mid
  Blade.add(bladeSeg(0.12, 0.18, 0.026, mats.blade, 0.47));
  Blade.add(bladeSeg(0.12, 0.16, 0.026, mats.blade, 0.31));
  // Lower toward guard
  Blade.add(bladeSeg(0.13, 0.14, 0.028, mats.blade, 0.17));
  Blade.add(bladeSeg(0.14, 0.10, 0.030, mats.blade, 0.06));

  // Angular "blood groove" indent (darker inset)
  const grooveMat = mats.blade.clone();
  grooveMat.color = new THREE.Color(0x0c0a12);
  grooveMat.metalness = 0.7;
  Blade.add(box(0.03, 0.72, 0.008, grooveMat, 0, 0.55, 0.012));
  Blade.add(box(0.03, 0.72, 0.008, grooveMat, 0, 0.55, -0.012));

  // Distorted micro-blocks (space instability)
  const distMat = mats.blade.clone();
  distMat.color = new THREE.Color(0x221830);
  for (let i = 0; i < 6; i++) {
    const s = 0.018 + Math.random() * 0.02;
    const dm = box(s, s * 1.4, s * 0.6, distMat,
      (Math.random() - 0.5) * 0.1,
      0.3 + Math.random() * 0.7,
      (Math.random() > 0.5 ? 1 : -1) * (0.016 + Math.random() * 0.01)
    );
    dm.rotation.z = (Math.random() - 0.5) * 0.4;
    Blade.add(dm);
  }

  // ---- BLADE EDGE (emissive rim) ------------------------------------------
  const Blade_Edge = new THREE.Group();
  Blade_Edge.name = 'Blade_Edge';
  // Left edge chain
  const edgeYs = [1.10, 0.98, 0.84, 0.68, 0.52, 0.36, 0.22, 0.10];
  const edgeWs = [0.015, 0.018, 0.02, 0.022, 0.022, 0.022, 0.024, 0.026];
  edgeYs.forEach((y, i) => {
    Blade_Edge.add(box(edgeWs[i], 0.11, 0.008, mats.bladeEdge, -0.055 - i * 0.002, y, 0));
    Blade_Edge.add(box(edgeWs[i], 0.11, 0.008, mats.bladeEdge,  0.055 + i * 0.002, y, 0));
  });
  // Tip edge
  Blade_Edge.add(box(0.05, 0.04, 0.01, mats.bladeEdge, 0, 1.16, 0));

  // ---- VOID CRACKS --------------------------------------------------------
  const Void_Cracks = new THREE.Group();
  Void_Cracks.name = 'Void_Cracks';

  // Jagged crack paths as thin boxes across the blade face
  const crackPaths = [
    // main diagonal crack
    [[-0.02, 0.95, 0.014], [0.01, 0.82, 0.014], [-0.015, 0.68, 0.014], [0.02, 0.55, 0.014], [-0.01, 0.40, 0.014], [0.015, 0.25, 0.014]],
    // secondary
    [[0.03, 0.88, 0.014], [0.04, 0.72, 0.014], [0.02, 0.58, 0.014], [0.035, 0.42, 0.014]],
    // reverse face
    [[0.01, 0.90, -0.014], [-0.02, 0.75, -0.014], [0.015, 0.58, -0.014], [-0.01, 0.38, -0.014], [0.02, 0.22, -0.014]],
  ];
  crackPaths.forEach((path) => {
    for (let i = 0; i < path.length - 1; i++) {
      const a = new THREE.Vector3(...path[i]);
      const b = new THREE.Vector3(...path[i + 1]);
      const mid = a.clone().add(b).multiplyScalar(0.5);
      const len = a.distanceTo(b);
      const seg = box(0.012, len * 1.05, 0.006, mats.voidCracks, mid.x, mid.y, mid.z);
      seg.lookAt(b);
      // orient along crack
      const dir = b.clone().sub(a).normalize();
      const quat = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir);
      seg.quaternion.copy(quat);
      Void_Cracks.add(seg);
    }
    // node blobs
    path.forEach((p) => {
      Void_Cracks.add(box(0.018, 0.018, 0.01, mats.voidCracks, p[0], p[1], p[2]));
    });
  });

  // ---- GUARD --------------------------------------------------------------
  const Guard = new THREE.Group();
  Guard.name = 'Guard';

  // Angular crossguard wings
  Guard.add(box(0.42, 0.06, 0.08, mats.guard, 0, 0.0, 0));
  Guard.add(box(0.12, 0.08, 0.10, mats.guard, 0, 0.02, 0));
  // Upswept tips
  Guard.add(box(0.08, 0.10, 0.06, mats.guard, -0.20, 0.06, 0));
  Guard.add(box(0.08, 0.10, 0.06, mats.guard,  0.20, 0.06, 0));
  Guard.add(box(0.05, 0.08, 0.05, mats.guard, -0.24, 0.12, 0));
  Guard.add(box(0.05, 0.08, 0.05, mats.guard,  0.24, 0.12, 0));
  // End-themed lower plate
  Guard.add(box(0.18, 0.04, 0.12, mats.guard, 0, -0.04, 0));

  // Ender-eye motif on guard center
  const eyeFrame = box(0.08, 0.08, 0.04, mats.handleMetal, 0, 0.02, 0.06);
  Guard.add(eyeFrame);
  Guard.add(box(0.05, 0.05, 0.03, mats.emissive, 0, 0.02, 0.08));
  Guard.add(box(0.025, 0.025, 0.02, mats.coreInner, 0, 0.02, 0.095));

  // Purple crystal fragments
  const crystals = [
    [-0.14, 0.05, 0.05], [0.14, 0.05, 0.05],
    [-0.18, 0.0, -0.04], [0.18, 0.0, -0.04],
    [0, 0.08, -0.05],
  ];
  crystals.forEach(([x, y, z], i) => {
    const c = box(0.03 + (i % 2) * 0.01, 0.04, 0.03, mats.crystal, x, y, z);
    c.rotation.set(0.3 * i, 0.5 * i, 0.2);
    Guard.add(c);
  });

  // ---- HANDLE -------------------------------------------------------------
  const Handle = new THREE.Group();
  Handle.name = 'Handle';

  // Wrapped grip segments
  for (let i = 0; i < 5; i++) {
    const y = -0.12 - i * 0.08;
    Handle.add(box(0.055, 0.07, 0.055, mats.handle, 0, y, 0));
    // wrap ridges
    Handle.add(box(0.062, 0.015, 0.062, mats.handleMetal, 0, y - 0.03, 0));
  }
  // Top collar
  Handle.add(box(0.08, 0.04, 0.08, mats.handleMetal, 0, -0.06, 0));
  // Bottom collar before pommel
  Handle.add(box(0.075, 0.035, 0.075, mats.handleMetal, 0, -0.50, 0));

  // ---- RUNES on handle ----------------------------------------------------
  const Runes = new THREE.Group();
  Runes.name = 'Runes';
  const runeYs = [-0.14, -0.22, -0.30, -0.38, -0.46];
  runeYs.forEach((y, i) => {
    // Front rune
    Runes.add(box(0.02, 0.025, 0.01, mats.runes, 0, y, 0.03));
    // Side ticks
    if (i % 2 === 0) {
      Runes.add(box(0.01, 0.02, 0.02, mats.runes, 0.03, y, 0));
      Runes.add(box(0.01, 0.02, 0.02, mats.runes, -0.03, y, 0));
    }
  });

  // ---- POMMEL + ENDER CORE ------------------------------------------------
  const Pommel = new THREE.Group();
  Pommel.name = 'Pommel';
  Pommel.add(box(0.09, 0.05, 0.09, mats.pommel, 0, -0.55, 0));
  Pommel.add(box(0.07, 0.04, 0.07, mats.pommel, 0, -0.59, 0));
  // Cage bars around core
  [[0.04, 0], [-0.04, 0], [0, 0.04], [0, -0.04]].forEach(([x, z]) => {
    Pommel.add(box(0.015, 0.08, 0.015, mats.handleMetal, x, -0.64, z));
  });
  Pommel.add(box(0.06, 0.02, 0.06, mats.pommel, 0, -0.70, 0));

  const Ender_Core = new THREE.Group();
  Ender_Core.name = 'Ender_Core';
  Ender_Core.position.set(0, -0.64, 0);

  // Outer shell
  const shell = new THREE.Mesh(new THREE.IcosahedronGeometry(0.038, 1), mats.coreShell);
  shell.name = 'CoreShell';
  Ender_Core.add(shell);

  // Inner energy sphere
  const inner = new THREE.Mesh(new THREE.IcosahedronGeometry(0.022, 1), mats.coreInner);
  inner.name = 'CoreInner';
  Ender_Core.add(inner);

  // Tiny swirling orbit bits
  for (let i = 0; i < 4; i++) {
    const bit = box(0.008, 0.008, 0.008, mats.emissive);
    bit.userData.orbit = { angle: (i / 4) * Math.PI * 2, radius: 0.03, speed: 1.5 + i * 0.2 };
    bit.name = 'CoreBit' + i;
    Ender_Core.add(bit);
  }

  // ---- EMISSIVE PARTS (grouped accents) -----------------------------------
  const EmissiveParts = new THREE.Group();
  EmissiveParts.name = 'EmissiveParts';
  // Tip glow
  EmissiveParts.add(box(0.03, 0.05, 0.015, mats.emissive, 0, 1.14, 0));
  // Guard wing tips
  EmissiveParts.add(box(0.03, 0.03, 0.03, mats.emissive, -0.24, 0.14, 0));
  EmissiveParts.add(box(0.03, 0.03, 0.03, mats.emissive,  0.24, 0.14, 0));

  // ---- Assemble under root ------------------------------------------------
  // Pivot at handle grip center so hand holds correctly
  // Blade extends +Y, pommel -Y
  root.add(Blade);
  root.add(Blade_Edge);
  root.add(Void_Cracks);
  root.add(Guard);
  root.add(Handle);
  root.add(Pommel);
  root.add(Ender_Core);
  root.add(Runes);
  root.add(EmissiveParts);

  // Store refs for animation / isolation
  root.userData.parts = {
    Blade, Blade_Edge, Void_Cracks, Guard, Handle, Pommel, Ender_Core, Runes, EmissiveParts,
  };

  // Default rest orientation (blade up)
  root.rotation.set(0, 0, 0);

  return root;
}

/**
 * Creates a lightweight ghost clone for afterimages.
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
