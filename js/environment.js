import * as THREE from 'three';

/**
 * Dark End-themed showcase environment:
 * obsidian platform, end stone islands, purpur pillars, dimensional cracks.
 */
export function createEnvironment(mats, scene) {
  const env = new THREE.Group();
  env.name = 'Environment';

  const box = (w, h, d, mat, x, y, z) => {
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.position.set(x, y, z);
    m.castShadow = true;
    m.receiveShadow = true;
    return m;
  };

  // Main obsidian platform (blocky circle approximation)
  const platform = new THREE.Group();
  platform.name = 'Platform';
  for (let x = -5; x <= 5; x++) {
    for (let z = -5; z <= 5; z++) {
      if (x * x + z * z > 28) continue;
      const h = 0.5 + ((x + z) % 3 === 0 ? 0.15 : 0);
      const mat = (Math.abs(x) + Math.abs(z)) % 5 === 0 ? mats.purpur : mats.obsidian;
      platform.add(box(0.98, h, 0.98, mat, x, -h * 0.5, z));
    }
  }
  // Central end portal frame style ring
  for (let i = 0; i < 12; i++) {
    const a = (i / 12) * Math.PI * 2;
    const r = 2.4;
    platform.add(box(0.5, 0.35, 0.5, mats.obsidian, Math.cos(a) * r, 0.1, Math.sin(a) * r));
  }
  env.add(platform);

  // Floating end-stone islands
  const islands = [
    [6, 2.5, -4, 1.8],
    [-7, 3.2, 3, 1.4],
    [4, 4.5, 7, 1.2],
    [-5, 1.8, -7, 1.6],
    [8, 5.5, 2, 1.0],
    [-3, 6.0, 8, 0.9],
  ];
  islands.forEach(([x, y, z, s], idx) => {
    const g = new THREE.Group();
    g.position.set(x, y, z);
    const n = 3 + (idx % 3);
    for (let i = 0; i < n; i++) {
      for (let j = 0; j < n; j++) {
        if ((i - 1) * (i - 1) + (j - 1) * (j - 1) > 3) continue;
        g.add(box(s * 0.7, s * 0.4, s * 0.7, mats.endStone,
          (i - 1) * s * 0.7, -s * 0.2, (j - 1) * s * 0.7));
      }
    }
    // chorus plant
    if (idx % 2 === 0) {
      g.add(box(0.15, 1.2, 0.15, mats.chorus, 0, 0.5, 0));
      g.add(box(0.35, 0.2, 0.35, mats.crystal, 0, 1.1, 0));
    }
    g.userData.float = { baseY: y, phase: idx * 1.3, amp: 0.15 + idx * 0.02 };
    env.add(g);
  });

  // Purpur pillars
  [[-3.5, 0, -3.5], [3.5, 0, -3.5], [-3.5, 0, 3.5], [3.5, 0, 3.5]].forEach(([x, y, z]) => {
    env.add(box(0.6, 3.2, 0.6, mats.purpur, x, 1.6, z));
    env.add(box(0.9, 0.3, 0.9, mats.obsidian, x, 3.3, z));
    env.add(box(0.25, 0.25, 0.25, mats.emissive, x, 3.55, z));
  });

  // Ground dimensional cracks (flat emissive strips)
  const cracks = new THREE.Group();
  cracks.name = 'GroundCracks';
  const crackMat = mats.voidCracks.clone();
  crackMat.emissiveIntensity = 0.8;
  const crackSegs = [
    [0.2, 0.02, 2.5, 1.2, 0.02, -0.5],
    [1.8, 0.02, 0.15, -0.8, 0.02, 1.5],
    [0.15, 0.02, 1.6, 2.0, 0.02, 1.0],
    [1.2, 0.02, 0.12, -1.5, 0.02, -1.2],
    [0.8, 0.02, 0.1, 0.5, 0.02, 2.2],
  ];
  crackSegs.forEach(([w, h, d, x, y, z], i) => {
    const m = box(w, h, d, crackMat, x, y, z);
    m.rotation.y = i * 0.7;
    cracks.add(m);
  });
  env.add(cracks);

  // Ambient floating void fragments
  const fragments = new THREE.Group();
  fragments.name = 'Fragments';
  for (let i = 0; i < 18; i++) {
    const s = 0.06 + Math.random() * 0.12;
    const f = box(s, s * (0.5 + Math.random()), s * 0.6, mats.obsidian,
      (Math.random() - 0.5) * 14,
      1 + Math.random() * 6,
      (Math.random() - 0.5) * 14
    );
    f.userData.drift = {
      phase: Math.random() * Math.PI * 2,
      speed: 0.3 + Math.random() * 0.5,
      amp: 0.2 + Math.random() * 0.3,
      rot: (Math.random() - 0.5) * 0.5,
    };
    fragments.add(f);
  }
  env.add(fragments);

  // Soft purple fog / atmosphere helper lights
  const amb = new THREE.AmbientLight(0x3a2060, 0.55);
  scene.add(amb);

  const key = new THREE.DirectionalLight(0xd0b0ff, 1.1);
  key.position.set(4, 8, 5);
  key.castShadow = true;
  key.shadow.mapSize.set(2048, 2048);
  key.shadow.camera.near = 0.5;
  key.shadow.camera.far = 40;
  key.shadow.camera.left = -12;
  key.shadow.camera.right = 12;
  key.shadow.camera.top = 12;
  key.shadow.camera.bottom = -12;
  scene.add(key);

  const fill = new THREE.DirectionalLight(0x4020a0, 0.45);
  fill.position.set(-6, 3, -4);
  scene.add(fill);

  const rim = new THREE.PointLight(0xb45cff, 2.2, 18, 2);
  rim.position.set(0, 2.5, 0);
  scene.add(rim);

  // Portal-ish underglow
  const under = new THREE.PointLight(0x6b2ad1, 1.5, 10, 2);
  under.position.set(0, 0.3, 0);
  scene.add(under);

  env.userData.lights = { amb, key, fill, rim, under };
  env.userData.fragments = fragments;
  env.userData.animate = (t) => {
    fragments.children.forEach((f) => {
      const d = f.userData.drift;
      if (!d) return;
      f.position.y += Math.sin(t * d.speed + d.phase) * 0.002;
      f.rotation.x += d.rot * 0.01;
      f.rotation.y += d.rot * 0.015;
    });
    env.children.forEach((c) => {
      if (c.userData.float) {
        const fl = c.userData.float;
        c.position.y = fl.baseY + Math.sin(t * 0.7 + fl.phase) * fl.amp;
      }
    });
  };

  scene.add(env);
  return env;
}
