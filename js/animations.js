import * as THREE from 'three';
import { applyPose } from './mannequin.js';

/**
 * Animation director for Ender Blade showcase.
 * Distinct rhythms per attack; polished equip / ability sequences.
 */

function smooth(t) {
  return t * t * (3 - 2 * t);
}
function smoother(t) {
  return t * t * t * (t * (t * 6 - 15) + 10);
}
function clamp01(t) {
  return Math.max(0, Math.min(1, t));
}
function easeOutBack(t) {
  const c = 1.70158;
  const x = t - 1;
  return 1 + (c + 1) * x * x * x + c * x * x;
}
function seg(t, a, b) {
  if (t <= a) return 0;
  if (t >= b) return 1;
  return smooth((t - a) / (b - a));
}
function pulse(t, a, b) {
  if (t < a || t > b) return 0;
  const m = (a + b) * 0.5;
  return t < m ? seg(t, a, m) : 1 - seg(t, m, b);
}

function clonePose(p) {
  const out = {};
  for (const [k, v] of Object.entries(p)) {
    out[k] = {
      pos: v.pos.clone(),
      rot: v.rot.clone(),
      scl: v.scl.clone(),
    };
  }
  return out;
}

function setBone(pose, name, rot = null, pos = null) {
  if (!pose[name]) return;
  if (rot) {
    if (rot.x !== undefined) pose[name].rot.x = rot.x;
    if (rot.y !== undefined) pose[name].rot.y = rot.y;
    if (rot.z !== undefined) pose[name].rot.z = rot.z;
  }
  if (pos) {
    if (pos.x !== undefined) pose[name].pos.x = pos.x;
    if (pos.y !== undefined) pose[name].pos.y = pos.y;
    if (pos.z !== undefined) pose[name].pos.z = pos.z;
  }
}

export const ANIM_META = {
  idle: { label: 'Idle', duration: Infinity, loop: true },
  equip: { label: 'Equip', duration: 1.35, loop: false },
  stance: { label: 'Combat Stance', duration: Infinity, loop: true },
  attack1: { label: 'Attack 1', duration: 0.55, loop: false },
  attack2: { label: 'Attack 2', duration: 0.62, loop: false },
  attack3: { label: 'Attack 3', duration: 0.85, loop: false },
  heavy: { label: 'Heavy Attack', duration: 1.15, loop: false },
  voidSlash: { label: 'Void Slash', duration: 1.05, loop: false },
  enderEcho: { label: 'Ender Echo', duration: 1.4, loop: false },
  teleport: { label: 'Teleport', duration: 1.1, loop: false },
  voidAnchor: { label: 'Void Anchor', duration: 1.2, loop: false },
  paradoxStep: { label: 'Paradox Step', duration: 1.35, loop: false },
  ultimate: { label: 'Ultimate', duration: 5.5, loop: false },
  riftCollapse: { label: 'Rift Collapse', duration: 1.8, loop: false },
  passive: { label: 'Chém Xuyên Không', duration: 1.0, loop: false },
};

export function createAnimationDirector(ctx) {
  const {
    bones,
    restPose,
    weapon,
    weaponRoot,
    vfx,
    mats,
  } = ctx;

  let current = 'idle';
  let time = 0;
  let speed = 1;
  let playing = true;
  let eventsFired = new Set();
  let crackBoost = 1;
  let weaponExtra = { rot: new THREE.Euler(), pos: new THREE.Vector3(), scl: 1 };

  // Base combat stance offsets relative to rest
  function stancePose(base) {
    const p = clonePose(base);
    setBone(p, 'RArm', { x: 0.45, y: -0.25, z: 0.35 });
    setBone(p, 'LArm', { x: 0.25, y: 0.15, z: -0.2 });
    setBone(p, 'Chest', { y: -0.12, x: 0.05 });
    setBone(p, 'Hips', { y: 0.08 });
    setBone(p, 'Head', { y: 0.1 });
    setBone(p, 'WeaponSocket', { x: -0.35, y: 0.15, z: 0.4 });
    return p;
  }

  const combatRest = stancePose(restPose);

  function fireOnce(id, at, t, fn) {
    const key = id + '@' + at;
    if (t >= at && !eventsFired.has(key)) {
      eventsFired.add(key);
      fn();
    }
  }

  function resetWeaponExtra() {
    weaponExtra.rot.set(0, 0, 0);
    weaponExtra.pos.set(0, 0, 0);
    weaponExtra.scl = 1;
  }

  function applyWeaponExtra() {
    if (!weapon) return;
    // Applied on top of socket — local offset
    weapon.rotation.x = weaponExtra.rot.x;
    weapon.rotation.y = weaponExtra.rot.y;
    weapon.rotation.z = weaponExtra.rot.z;
    weapon.position.x = weaponExtra.pos.x;
    weapon.position.y = weaponExtra.pos.y;
    weapon.position.z = weaponExtra.pos.z;
    weapon.scale.setScalar(weaponExtra.scl);
  }

  function play(name) {
    if (!ANIM_META[name]) name = 'idle';
    current = name;
    time = 0;
    playing = true;
    eventsFired = new Set();
    crackBoost = 1;
    resetWeaponExtra();
    applyPose(bones, restPose, 1);
    if (vfx?.onAnimStart) vfx.onAnimStart(name);
  }

  function getProgress() {
    const meta = ANIM_META[current];
    if (!meta || !isFinite(meta.duration)) return 0;
    return clamp01(time / meta.duration);
  }

  function update(dt, worldT) {
    if (!playing && ANIM_META[current]?.loop) playing = true;
    if (playing) time += dt * speed;

    const meta = ANIM_META[current] || ANIM_META.idle;
    const dur = meta.duration;
    const t = isFinite(dur) ? clamp01(time / dur) : time;
    crackBoost = 1;

    switch (current) {
      case 'idle':
        updateIdle(worldT);
        break;
      case 'stance':
        updateStance(worldT);
        break;
      case 'equip':
        updateEquip(t, worldT);
        break;
      case 'attack1':
        updateAttack1(t);
        break;
      case 'attack2':
        updateAttack2(t);
        break;
      case 'attack3':
        updateAttack3(t);
        break;
      case 'heavy':
        updateHeavy(t);
        break;
      case 'voidSlash':
        updateVoidSlash(t);
        break;
      case 'enderEcho':
        updateEnderEcho(t);
        break;
      case 'teleport':
        updateTeleport(t);
        break;
      case 'voidAnchor':
        updateVoidAnchor(t);
        break;
      case 'paradoxStep':
        updateParadoxStep(t);
        break;
      case 'ultimate':
        updateUltimate(t);
        break;
      case 'riftCollapse':
        updateRiftCollapse(t);
        break;
      case 'passive':
        updatePassive(t);
        break;
      default:
        updateIdle(worldT);
    }

    applyWeaponExtra();

    if (isFinite(dur) && time >= dur) {
      if (meta.loop) {
        time = 0;
        eventsFired = new Set();
      } else {
        playing = false;
        // settle into stance
        current = 'stance';
        time = 0;
        playing = true;
        eventsFired = new Set();
        if (vfx?.onAnimEnd) vfx.onAnimEnd(meta.label || 'done');
      }
    }

    return {
      name: current,
      label: ANIM_META[current]?.label || current,
      progress: getProgress(),
      crackBoost,
      time,
    };
  }

  // ----- IDLE --------------------------------------------------------------
  function ensureVisible() {
    if (bones.root) {
      bones.root.visible = true;
      bones.root.scale.set(1, 1, 1);
    }
  }

  function updateIdle(worldT) {
    ensureVisible();
    applyPose(bones, restPose, 1);
    // Subtle assassin-ready breathe
    const breathe = Math.sin(worldT * 1.6) * 0.03;
    const sway = Math.sin(worldT * 0.9) * 0.02;
    bones.RArm.rotation.x = 0.28 + breathe;
    bones.RArm.rotation.z = 0.18 + sway;
    bones.LArm.rotation.x = 0.15 + breathe * 0.5;
    bones.Chest.rotation.y = sway * 0.5;
    bones.Head.rotation.y = Math.sin(worldT * 0.5) * 0.05;
    bones.WeaponSocket.rotation.x = -0.2 + breathe * 0.5;
    bones.WeaponSocket.rotation.z = 0.12;
    // Weapon micro life
    weaponExtra.rot.z = Math.sin(worldT * 1.2) * 0.03;
    weaponExtra.rot.x = Math.sin(worldT * 0.8) * 0.02;
    weaponExtra.pos.y = Math.sin(worldT * 1.5) * 0.008;
    crackBoost = 0.85 + Math.sin(worldT * 2.0) * 0.1;
  }

  function updateStance(worldT) {
    ensureVisible();
    applyPose(bones, combatRest, 1);
    const breathe = Math.sin(worldT * 2.0) * 0.025;
    bones.RArm.rotation.x = combatRest.RArm.rot.x + breathe;
    bones.Chest.rotation.y = combatRest.Chest.rot.y + Math.sin(worldT * 0.7) * 0.04;
    bones.Hips.rotation.y = Math.sin(worldT * 0.6) * 0.03;
    weaponExtra.rot.z = Math.sin(worldT * 1.4) * 0.025;
    crackBoost = 1.0 + Math.sin(worldT * 2.2) * 0.12;
  }

  // ----- EQUIP: emerge from dimensional rift --------------------------------
  function updateEquip(t) {
    // Phases: 0-0.15 crack · 0.15-0.35 expand · 0.35-0.55 gather · 0.55-0.75 emerge
    //         0.75-0.88 grab · 0.88-1.0 snap + pulse
    applyPose(bones, restPose, 1);

    fireOnce('equip', 0.02, t, () => vfx?.spawnEquipRift?.());
    fireOnce('equip', 0.18, t, () => vfx?.expandEquipRift?.());
    fireOnce('equip', 0.4, t, () => vfx?.gatherEquipEnergy?.());
    fireOnce('equip', 0.58, t, () => vfx?.spawnAfterimage?.('equip'));
    fireOnce('equip', 0.9, t, () => {
      vfx?.closeEquipRift?.();
      vfx?.bladeEnergyPulse?.();
    });

    const emerge = seg(t, 0.5, 0.78);
    const grab = seg(t, 0.72, 0.88);
    const settle = seg(t, 0.85, 1.0);

    // Blade starts invisible / below, rises from rift
    weaponExtra.scl = 0.15 + emerge * 0.85;
    weaponExtra.pos.y = THREE.MathUtils.lerp(-0.55, 0, easeOutBack(clamp01(emerge)));
    weaponExtra.rot.x = THREE.MathUtils.lerp(-1.2, -0.2, smoother(emerge));
    weaponExtra.rot.z = THREE.MathUtils.lerp(1.5, 0.12, smoother(emerge));
    weaponExtra.rot.y = Math.sin(emerge * Math.PI) * 0.4;

    // Arm reaches then grips
    bones.RArm.rotation.x = THREE.MathUtils.lerp(0.1, 0.55, grab) - settle * 0.15;
    bones.RArm.rotation.z = THREE.MathUtils.lerp(0.6, 0.2, grab);
    bones.RArm.rotation.y = THREE.MathUtils.lerp(-0.4, -0.15, grab);
    bones.Chest.rotation.y = -0.15 + grab * 0.2;
    bones.LArm.rotation.x = 0.2 * grab;

    crackBoost = 0.5 + emerge * 1.5 + pulse(t, 0.88, 1.0) * 1.2;

    if (t > 0.55 && t < 0.85) {
      fireOnce('equip-ai', Math.floor(t * 20) / 20, t, () => vfx?.spawnAfterimage?.('equip'));
    }
  }

  // ----- ATTACK 1: fast horizontal -----------------------------------------
  function updateAttack1(t) {
    applyPose(bones, combatRest, 1);
    // Anticipation 0-0.22 · strike 0.22-0.48 · follow 0.48-1
    const ant = seg(t, 0.0, 0.22);
    const strike = seg(t, 0.22, 0.48);
    const follow = seg(t, 0.48, 1.0);

    fireOnce('a1', 0.28, t, () => {
      vfx?.spawnSlashTrail?.('horizontal', 0.9);
      crackBoost = 1.6;
    });
    fireOnce('a1', 0.35, t, () => vfx?.spawnSpatialCut?.(0.6));

    bones.RArm.rotation.y = THREE.MathUtils.lerp(-0.9, 1.1, smoother(strike)) - follow * 0.3;
    bones.RArm.rotation.x = 0.25 - ant * 0.15 + strike * 0.1;
    bones.RArm.rotation.z = 0.4 - strike * 0.5;
    bones.Chest.rotation.y = -0.35 * ant + strike * 0.7 - follow * 0.2;
    bones.Hips.rotation.y = bones.Chest.rotation.y * 0.5;
    bones.WeaponSocket.rotation.z = -0.6 * ant + strike * 1.4;
    bones.WeaponSocket.rotation.x = -0.2 + strike * 0.3;
    bones.LArm.rotation.z = -0.3 - strike * 0.2;

    weaponExtra.rot.z = strike * 0.4;
    crackBoost = 1 + ant * 0.3 + strike * 0.8 + (1 - follow) * 0.2;
  }

  // ----- ATTACK 2: diagonal aggressive -------------------------------------
  function updateAttack2(t) {
    applyPose(bones, combatRest, 1);
    const ant = seg(t, 0.0, 0.2);
    const strike = seg(t, 0.2, 0.52);
    const follow = seg(t, 0.52, 1.0);

    fireOnce('a2', 0.28, t, () => {
      vfx?.spawnSlashTrail?.('diagonal', 1.1);
      vfx?.spawnDistortionRibbon?.(1.0);
    });
    fireOnce('a2', 0.4, t, () => vfx?.spawnSpatialCut?.(0.75));

    bones.RArm.rotation.x = 0.9 * ant - strike * 1.5 + follow * 0.3;
    bones.RArm.rotation.y = -0.4 + strike * 1.0;
    bones.RArm.rotation.z = 0.2 + strike * 0.6;
    bones.Chest.rotation.y = -0.2 * ant + strike * 0.55;
    bones.Chest.rotation.x = 0.1 * ant - strike * 0.15;
    bones.Hips.rotation.y = strike * 0.3;
    bones.WeaponSocket.rotation.x = 0.5 * ant - strike * 1.2;
    bones.WeaponSocket.rotation.z = -0.3 + strike * 0.9;
    bones.LArm.rotation.x = 0.3 + strike * 0.2;

    weaponExtra.rot.x = -0.3 * ant + strike * 0.5;
    crackBoost = 1.1 + strike * 1.0;
  }

  // ----- ATTACK 3: heavy downward ------------------------------------------
  function updateAttack3(t) {
    applyPose(bones, combatRest, 1);
    const ant = seg(t, 0.0, 0.32);
    const strike = seg(t, 0.32, 0.58);
    const impact = pulse(t, 0.55, 0.75);
    const follow = seg(t, 0.58, 1.0);

    fireOnce('a3', 0.1, t, () => vfx?.gatherBladeEnergy?.(0.6));
    fireOnce('a3', 0.4, t, () => {
      vfx?.spawnSlashTrail?.('heavy', 1.4);
      vfx?.spawnSpatialCut?.(1.2);
    });
    fireOnce('a3', 0.55, t, () => vfx?.spawnImpactBurst?.(1.0));

    bones.RArm.rotation.x = -0.85 * ant + strike * 1.9;
    bones.RArm.rotation.y = -0.2 + strike * 0.4;
    bones.RArm.rotation.z = 0.15;
    bones.Chest.rotation.x = -0.2 * ant + strike * 0.35;
    bones.Hips.position.y = combatRest.Hips.pos.y - ant * 0.04 + strike * 0.02;
    bones.WeaponSocket.rotation.x = -0.9 * ant + strike * 1.6;
    bones.LArm.rotation.x = 0.5 * ant;
    bones.Head.rotation.x = 0.15 * ant - strike * 0.1;

    weaponExtra.rot.x = -0.4 * ant + strike * 0.6;
    crackBoost = 1.2 + ant * 1.3 + strike * 1.5 + impact * 0.8;
  }

  // ----- HEAVY ATTACK ------------------------------------------------------
  function updateHeavy(t) {
    applyPose(bones, combatRest, 1);
    const pull = seg(t, 0.0, 0.35);
    const gather = seg(t, 0.2, 0.5);
    const strike = seg(t, 0.5, 0.72);
    const hold = pulse(t, 0.7, 0.95);

    fireOnce('hv', 0.15, t, () => vfx?.gatherBladeEnergy?.(1.2));
    fireOnce('hv', 0.35, t, () => vfx?.spawnDistortionField?.(0.8));
    fireOnce('hv', 0.55, t, () => {
      vfx?.spawnHeavySlash?.(1.6);
      vfx?.spawnSlashTrail?.('heavy', 1.8);
    });
    fireOnce('hv', 0.72, t, () => vfx?.spawnSpatialCut?.(1.5));

    bones.RArm.rotation.x = -1.0 * pull + strike * 2.1;
    bones.RArm.rotation.y = -0.5 * pull + strike * 0.9;
    bones.Chest.rotation.y = -0.4 * pull + strike * 0.7;
    bones.Chest.rotation.x = -0.15 * pull + strike * 0.3;
    bones.Hips.rotation.y = bones.Chest.rotation.y * 0.6;
    bones.WeaponSocket.rotation.x = -1.1 * pull + strike * 1.8;
    bones.WeaponSocket.rotation.z = 0.3 * gather;
    bones.LArm.rotation.x = 0.6 * pull;

    weaponExtra.rot.z = Math.sin(gather * Math.PI) * 0.25;
    crackBoost = 1.3 + gather * 1.8 + strike * 1.2 + hold * 0.5;
  }

  // ----- VOID SLASH --------------------------------------------------------
  function updateVoidSlash(t) {
    applyPose(bones, combatRest, 1);
    const ant = seg(t, 0.0, 0.28);
    const cut = seg(t, 0.28, 0.55);
    const collapse = seg(t, 0.55, 1.0);

    fireOnce('vs', 0.12, t, () => vfx?.gatherBladeEnergy?.(1.0));
    fireOnce('vs', 0.32, t, () => vfx?.spawnVoidSlashRift?.(1.4));
    fireOnce('vs', 0.55, t, () => vfx?.collapseVoidSlash?.());

    bones.RArm.rotation.x = -0.5 * ant + cut * 1.5;
    bones.RArm.rotation.y = 0.3 * ant + cut * 0.5;
    bones.RArm.rotation.z = -0.2 + cut * 0.8;
    bones.Chest.rotation.y = cut * 0.45;
    bones.WeaponSocket.rotation.z = cut * 1.5;
    bones.WeaponSocket.rotation.x = -0.3 * ant + cut * 0.8;

    crackBoost = 1.4 + ant * 0.8 + cut * 2.0 - collapse * 0.8;
  }

  // ----- ENDER ECHO --------------------------------------------------------
  function updateEnderEcho(t) {
    applyPose(bones, combatRest, 1);
    const wind = seg(t, 0.0, 0.3);
    const cast = seg(t, 0.3, 0.5);
    const hold = seg(t, 0.5, 1.0);

    fireOnce('ee', 0.35, t, () => vfx?.launchEcho?.());
    fireOnce('ee', 0.7, t, () => {}); // echo travels via vfx update

    bones.RArm.rotation.x = 0.1 - wind * 0.5 - cast * 0.2 + hold * 0.3;
    bones.RArm.rotation.y = -0.3 * wind;
    bones.WeaponSocket.rotation.x = -0.5 * wind + cast * 0.3;
    bones.Chest.rotation.y = -0.15 * wind;
    bones.LArm.rotation.x = 0.3 * wind;

    weaponExtra.pos.z = -wind * 0.05 + cast * 0.08;
    crackBoost = 1.2 + cast * 0.8;
  }

  // ----- TELEPORT ----------------------------------------------------------
  function updateTeleport(t) {
    applyPose(bones, combatRest, 1);
    const distort = seg(t, 0.0, 0.3);
    const collapse = seg(t, 0.25, 0.5);
    const emerge = seg(t, 0.5, 0.75);
    const settle = seg(t, 0.75, 1.0);

    fireOnce('tp', 0.05, t, () => vfx?.teleportDepartStart?.());
    fireOnce('tp', 0.28, t, () => vfx?.teleportCollapse?.());
    fireOnce('tp', 0.52, t, () => vfx?.teleportArrive?.());
    fireOnce('tp', 0.7, t, () => vfx?.teleportShockwave?.());

    const hide = collapse * (1 - emerge);
    if (bones.root) {
      bones.root.visible = hide < 0.85;
      const s = 1 - collapse * 0.7 + emerge * 0.7;
      bones.root.scale.setScalar(THREE.MathUtils.clamp(s, 0.15, 1));
    }
    // Shift position slightly on arrive
    bones.Hips.position.x = combatRest.Hips.pos.x + emerge * 1.8 - settle * 0.2;
    bones.Hips.position.z = combatRest.Hips.pos.z - emerge * 0.5;

    crackBoost = 1.5 + distort * 1.0 + emerge * 1.2;
  }

  // ----- VOID ANCHOR -------------------------------------------------------
  function updateVoidAnchor(t) {
    applyPose(bones, combatRest, 1);
    const plant = seg(t, 0.0, 0.4);
    const pulseP = pulse(t, 0.4, 0.7);
    const recall = seg(t, 0.7, 1.0);

    fireOnce('va', 0.35, t, () => vfx?.placeAnchor?.());
    fireOnce('va', 0.8, t, () => vfx?.recallAnchor?.());

    bones.RArm.rotation.x = 0.9 * plant - recall * 0.4;
    bones.WeaponSocket.rotation.x = 0.6 * plant;
    bones.Chest.rotation.x = 0.15 * plant;
    crackBoost = 1.1 + pulseP * 0.8;
  }

  // ----- PARADOX STEP ------------------------------------------------------
  function updateParadoxStep(t) {
    applyPose(bones, combatRest, 1);
    const freeze = seg(t, 0.0, 0.18);
    const after = seg(t, 0.15, 0.4);
    const vanish = seg(t, 0.35, 0.5);
    const behind = seg(t, 0.5, 0.7);
    const slash = seg(t, 0.68, 0.9);
    const settle = seg(t, 0.9, 1.0);

    fireOnce('px', 0.08, t, () => vfx?.paradoxFreeze?.());
    fireOnce('px', 0.2, t, () => vfx?.spawnAfterimage?.('paradox', 3));
    fireOnce('px', 0.4, t, () => vfx?.paradoxVanish?.());
    fireOnce('px', 0.55, t, () => vfx?.paradoxEmergeBehind?.());
    fireOnce('px', 0.72, t, () => {
      vfx?.spawnSlashTrail?.('crit', 1.5);
      vfx?.spawnSpatialCut?.(1.3);
    });

    // Freeze hold
    bones.RArm.rotation.x = combatRest.RArm.rot.x + freeze * 0.3;
    if (vanish > 0 && behind < 1) {
      if (bones.root) {
        bones.root.scale.setScalar(1 - vanish * 0.85 + behind * 0.85);
      }
    }
    bones.Hips.position.x = combatRest.Hips.pos.x + behind * 2.2;
    bones.Hips.position.z = combatRest.Hips.pos.z - behind * 1.2;
    bones.Hips.rotation.y = behind * Math.PI * 0.7 - settle * 0.2;

    // Crit slash
    bones.RArm.rotation.y = -0.5 + slash * 1.8;
    bones.RArm.rotation.x = 0.3 - slash * 0.2 + settle * 0.1;
    bones.WeaponSocket.rotation.z = slash * 1.2;
    bones.Chest.rotation.y = slash * 0.5;

    crackBoost = 1.3 + after * 0.5 + slash * 1.8;
  }

  // ----- ULTIMATE: End Dimension -------------------------------------------
  function updateUltimate(t) {
    applyPose(bones, combatRest, 1);
    // 0-0.12 drive into ground · 0.12-0.25 ring expand · 0.25-0.75 domain
    // 0.75-0.88 pull · 0.88-1.0 collapse
    const drive = seg(t, 0.0, 0.12);
    const expand = seg(t, 0.1, 0.28);
    const domain = seg(t, 0.25, 0.75);
    const pull = seg(t, 0.72, 0.88);
    const collapse = seg(t, 0.85, 1.0);

    fireOnce('ult', 0.08, t, () => vfx?.ultimateDrive?.());
    fireOnce('ult', 0.15, t, () => vfx?.ultimateRingExpand?.());
    fireOnce('ult', 0.3, t, () => vfx?.ultimateDomainStart?.());
    fireOnce('ult', 0.78, t, () => vfx?.ultimatePull?.());
    fireOnce('ult', 0.9, t, () => vfx?.ultimateCollapse?.());

    bones.RArm.rotation.x = 1.4 * drive - domain * 0.3 + pull * 0.4;
    bones.WeaponSocket.rotation.x = 1.2 * drive;
    bones.Chest.rotation.x = 0.3 * drive;
    bones.Hips.position.y = combatRest.Hips.pos.y - drive * 0.08;
    bones.LArm.rotation.x = 0.5 * drive;

    // Raise sword during domain
    if (domain > 0 && pull < 0.5) {
      bones.RArm.rotation.x = 0.6 + Math.sin(t * 20) * 0.03;
      bones.WeaponSocket.rotation.x = 0.2;
    }

    crackBoost = 1.5 + expand * 1.0 + domain * 0.8 + pull * 1.5 + collapse * 2.0;
  }

  // ----- RIFT COLLAPSE -----------------------------------------------------
  function updateRiftCollapse(t) {
    applyPose(bones, combatRest, 1);
    const bend = seg(t, 0.0, 0.25);
    const spiral = seg(t, 0.2, 0.55);
    const compress = seg(t, 0.5, 0.75);
    const burst = seg(t, 0.72, 0.9);
    const fade = seg(t, 0.88, 1.0);

    fireOnce('rc', 0.05, t, () => vfx?.riftBendStart?.());
    fireOnce('rc', 0.25, t, () => vfx?.riftSpiralIn?.());
    fireOnce('rc', 0.55, t, () => vfx?.riftCompress?.());
    fireOnce('rc', 0.78, t, () => vfx?.riftBurst?.());

    bones.RArm.rotation.x = 0.5 + spiral * 0.3;
    bones.Chest.rotation.y = Math.sin(spiral * Math.PI * 2) * 0.1;
    crackBoost = 1.5 + bend * 0.5 + spiral * 1.2 + compress * 1.5 + burst * 2.0 - fade;
  }

  // ----- PASSIVE: Chém Xuyên Không ----------------------------------------
  function updatePassive(t) {
    applyPose(bones, combatRest, 1);
    const strike = seg(t, 0.0, 0.35);
    const displace = seg(t, 0.35, 0.7);
    const settle = seg(t, 0.7, 1.0);

    fireOnce('ps', 0.2, t, () => vfx?.spawnSlashTrail?.('horizontal', 0.8));
    fireOnce('ps', 0.4, t, () => vfx?.spatialDisplace?.());

    bones.RArm.rotation.y = -0.5 + strike * 1.2;
    bones.RArm.rotation.x = 0.3;
    bones.WeaponSocket.rotation.z = strike * 0.8;
    crackBoost = 1.2 + displace * 1.0;
  }

  return {
    play,
    update,
    get current() {
      return current;
    },
    get progress() {
      return getProgress();
    },
    get crackBoost() {
      return crackBoost;
    },
    setSpeed(s) {
      speed = s;
    },
    getSpeed() {
      return speed;
    },
    reset() {
      play('idle');
    },
  };
}
