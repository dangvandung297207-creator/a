import * as THREE from 'three';

/**
 * Minecraft-style blocky mannequin with simple bone hierarchy
 * for combat / ability animations.
 *
 * Hierarchy:
 *   Mannequin
 *     Hips → Spine → Chest → Head
 *                    └─ LArm / RArm
 *     LLeg / RLeg
 *     WeaponSocket (on RArm)
 */
export function createMannequin(mats) {
  const root = new THREE.Group();
  root.name = 'Mannequin';

  const box = (w, h, d, mat) => {
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.castShadow = true;
    m.receiveShadow = true;
    return m;
  };

  // ---- Hips ----
  const Hips = new THREE.Group();
  Hips.name = 'Hips';
  Hips.position.y = 0.95;
  root.add(Hips);

  // ---- Legs ----
  const LLeg = new THREE.Group();
  LLeg.name = 'LLeg';
  LLeg.position.set(0.12, 0, 0);
  const lLegMesh = box(0.22, 0.72, 0.22, mats.cloth);
  lLegMesh.position.y = -0.36;
  LLeg.add(lLegMesh);
  // boot
  const lBoot = box(0.24, 0.12, 0.28, mats.armor);
  lBoot.position.set(0, -0.70, 0.02);
  LLeg.add(lBoot);
  Hips.add(LLeg);

  const RLeg = new THREE.Group();
  RLeg.name = 'RLeg';
  RLeg.position.set(-0.12, 0, 0);
  const rLegMesh = box(0.22, 0.72, 0.22, mats.cloth);
  rLegMesh.position.y = -0.36;
  RLeg.add(rLegMesh);
  const rBoot = box(0.24, 0.12, 0.28, mats.armor);
  rBoot.position.set(0, -0.70, 0.02);
  RLeg.add(rBoot);
  Hips.add(RLeg);

  // ---- Spine / Chest ----
  const Spine = new THREE.Group();
  Spine.name = 'Spine';
  Spine.position.y = 0.05;
  Hips.add(Spine);

  const Chest = new THREE.Group();
  Chest.name = 'Chest';
  Chest.position.y = 0.35;
  const torso = box(0.48, 0.70, 0.28, mats.armor);
  torso.position.y = 0.0;
  Chest.add(torso);
  // shoulder pads
  Chest.add(Object.assign(box(0.18, 0.12, 0.18, mats.armor), { position: new THREE.Vector3(0.28, 0.28, 0) }));
  Chest.add(Object.assign(box(0.18, 0.12, 0.18, mats.armor), { position: new THREE.Vector3(-0.28, 0.28, 0) }));
  // ender crystal on chest
  const gem = box(0.1, 0.1, 0.06, mats.emissive);
  gem.position.set(0, 0.05, 0.16);
  Chest.add(gem);
  Spine.add(Chest);

  // ---- Head ----
  const Head = new THREE.Group();
  Head.name = 'Head';
  Head.position.y = 0.48;
  const headMesh = box(0.36, 0.36, 0.36, mats.skin);
  Head.add(headMesh);
  // hood / shadow
  const hood = box(0.40, 0.18, 0.40, mats.cloth);
  hood.position.y = 0.14;
  Head.add(hood);
  // eyes (glowing void)
  const eyeL = box(0.06, 0.04, 0.02, mats.emissive);
  eyeL.position.set(0.08, 0.04, 0.18);
  const eyeR = box(0.06, 0.04, 0.02, mats.emissive);
  eyeR.position.set(-0.08, 0.04, 0.18);
  Head.add(eyeL, eyeR);
  Chest.add(Head);

  // ---- Arms ----
  const LArm = new THREE.Group();
  LArm.name = 'LArm';
  LArm.position.set(0.36, 0.25, 0);
  const lArmMesh = box(0.18, 0.62, 0.18, mats.cloth);
  lArmMesh.position.y = -0.28;
  LArm.add(lArmMesh);
  const lHand = box(0.16, 0.14, 0.18, mats.skin);
  lHand.position.y = -0.62;
  LArm.add(lHand);
  Chest.add(LArm);

  const RArm = new THREE.Group();
  RArm.name = 'RArm';
  RArm.position.set(-0.36, 0.25, 0);
  const rArmMesh = box(0.18, 0.62, 0.18, mats.armor);
  rArmMesh.position.y = -0.28;
  RArm.add(rArmMesh);
  const rHand = box(0.16, 0.14, 0.18, mats.skin);
  rHand.position.y = -0.62;
  RArm.add(rHand);

  // Weapon socket at right hand
  const WeaponSocket = new THREE.Group();
  WeaponSocket.name = 'WeaponSocket';
  WeaponSocket.position.set(0, -0.62, 0.02);
  // Default grip: blade up, slight forward tilt
  WeaponSocket.rotation.set(-0.15, 0, 0.15);
  RArm.add(WeaponSocket);

  Chest.add(RArm);

  root.userData.bones = {
    root, Hips, Spine, Chest, Head, LArm, RArm, LLeg, RLeg, WeaponSocket,
  };

  // Rest pose defaults stored for reset
  root.userData.restPose = capturePose(root.userData.bones);

  return root;
}

export function capturePose(bones) {
  const pose = {};
  for (const [k, b] of Object.entries(bones)) {
    pose[k] = {
      pos: b.position.clone(),
      rot: b.rotation.clone(),
      scl: b.scale.clone(),
    };
  }
  return pose;
}

export function applyPose(bones, pose, alpha = 1) {
  for (const [k, b] of Object.entries(bones)) {
    if (!pose[k]) continue;
    if (alpha >= 1) {
      b.position.copy(pose[k].pos);
      b.rotation.copy(pose[k].rot);
      b.scale.copy(pose[k].scl);
    } else {
      b.position.lerp(pose[k].pos, alpha);
      b.rotation.x += (pose[k].rot.x - b.rotation.x) * alpha;
      b.rotation.y += (pose[k].rot.y - b.rotation.y) * alpha;
      b.rotation.z += (pose[k].rot.z - b.rotation.z) * alpha;
    }
  }
}

export function lerpPose(bones, from, to, t) {
  const tt = THREE.MathUtils.clamp(t, 0, 1);
  // smoothstep
  const s = tt * tt * (3 - 2 * tt);
  for (const [k, b] of Object.entries(bones)) {
    if (!from[k] || !to[k]) continue;
    b.position.lerpVectors(from[k].pos, to[k].pos, s);
    b.rotation.x = THREE.MathUtils.lerp(from[k].rot.x, to[k].rot.x, s);
    b.rotation.y = THREE.MathUtils.lerp(from[k].rot.y, to[k].rot.y, s);
    b.rotation.z = THREE.MathUtils.lerp(from[k].rot.z, to[k].rot.z, s);
    b.scale.lerpVectors(from[k].scl, to[k].scl, s);
  }
}
