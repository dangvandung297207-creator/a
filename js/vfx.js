import * as THREE from 'three';
import { createAfterimage } from './weapon.js';

/**
 * Geometry-first VFX system for Ender Blade.
 * Mesh rifts, ribbons, rings, afterimages — particles only as support.
 */

function disposeObject(obj) {
  obj.traverse((c) => {
    if (c.geometry) c.geometry.dispose();
    if (c.material) {
      if (Array.isArray(c.material)) c.material.forEach((m) => m.dispose?.());
      else c.material.dispose?.();
    }
  });
}

function makeRingGeo(inner, outer, seg = 48) {
  return new THREE.RingGeometry(inner, outer, seg);
}

function makeArcRibbon(radius, width, startAngle, arcLen, segs = 24) {
  // Build a curved plane strip
  const positions = [];
  const uvs = [];
  const indices = [];
  for (let i = 0; i <= segs; i++) {
    const t = i / segs;
    const a = startAngle + arcLen * t;
    const c = Math.cos(a);
    const s = Math.sin(a);
    // two edges
    positions.push(c * radius, s * radius, 0);
    positions.push(c * (radius + width), s * (radius + width), 0);
    uvs.push(t, 0, t, 1);
    if (i < segs) {
      const a0 = i * 2;
      indices.push(a0, a0 + 1, a0 + 2, a0 + 1, a0 + 3, a0 + 2);
    }
  }
  const geo = new THREE.BufferGeometry();
  geo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
  geo.setAttribute('uv', new THREE.Float32BufferAttribute(uvs, 2));
  geo.setIndex(indices);
  geo.computeVertexNormals();
  return geo;
}

export function createVfxSystem({ scene, mats, weapon, mannequin, camera }) {
  const root = new THREE.Group();
  root.name = 'VFXRoot';
  scene.add(root);

  const active = []; // { obj, age, life, update, onEnd }
  const afterimages = [];
  let enabled = true;
  let echo = null;
  let anchor = null;
  let voidSlash = null;
  let domain = null;
  let equipRift = null;
  let markTarget = null;
  let markStacks = 0;

  // Dummy target for mark / collapse showcase
  const dummy = createDummyTarget(mats);
  dummy.position.set(1.6, 0, -0.3);
  dummy.visible = false;
  scene.add(dummy);

  function addFx(obj, life, update, onEnd) {
    root.add(obj);
    active.push({ obj, age: 0, life, update, onEnd });
    return obj;
  }

  function clearAll() {
    for (const fx of active) {
      root.remove(fx.obj);
      disposeObject(fx.obj);
    }
    active.length = 0;
    afterimages.forEach((a) => {
      root.remove(a.obj);
      disposeObject(a.obj);
    });
    afterimages.length = 0;
    if (echo) {
      root.remove(echo);
      disposeObject(echo);
      echo = null;
    }
    if (anchor) {
      root.remove(anchor);
      disposeObject(anchor);
      anchor = null;
    }
    if (voidSlash) {
      root.remove(voidSlash);
      disposeObject(voidSlash);
      voidSlash = null;
    }
    if (domain) {
      root.remove(domain);
      disposeObject(domain);
      domain = null;
    }
    if (equipRift) {
      root.remove(equipRift);
      disposeObject(equipRift);
      equipRift = null;
    }
    if (markTarget) {
      root.remove(markTarget);
      disposeObject(markTarget);
      markTarget = null;
      markStacks = 0;
    }
    dummy.visible = false;
  }

  // ---------- Helpers -------------------------------------------------------
  function worldPos(obj) {
    const v = new THREE.Vector3();
    if (obj) obj.getWorldPosition(v);
    return v;
  }

  function weaponTip() {
    const tip = weapon?.userData?.sockets?.tip;
    return tip ? worldPos(tip) : worldPos(weapon).add(new THREE.Vector3(0, 1.2, 0));
  }

  function playerCenter() {
    const v = new THREE.Vector3();
    if (mannequin) {
      mannequin.getWorldPosition(v);
      v.y += 1.0;
    }
    return v;
  }

  // ---------- Afterimages ---------------------------------------------------
  function spawnAfterimage(kind = 'default', count = 1) {
    if (!enabled || !weapon) return;
    for (let i = 0; i < count; i++) {
      const ghost = createAfterimage(weapon, mats);
      const wp = worldPos(weapon);
      ghost.position.copy(wp);
      ghost.quaternion.copy(weapon.getWorldQuaternion(new THREE.Quaternion()));
      ghost.scale.copy(weapon.getWorldScale(new THREE.Vector3()));
      // slight offset stagger
      ghost.position.x += (i - count / 2) * 0.08;
      ghost.position.z += i * 0.05;
      root.add(ghost);
      afterimages.push({
        obj: ghost,
        age: 0,
        life: kind === 'paradox' ? 0.7 : 0.45,
        fade: mats.afterimage.opacity,
      });
    }
  }

  // ---------- Slash trails (geometry ribbons) -------------------------------
  function spawnSlashTrail(style = 'horizontal', scale = 1) {
    if (!enabled) return;
    const group = new THREE.Group();
    const origin = weaponTip();

    let startA = 0;
    let arc = Math.PI * 0.85;
    let radius = 0.9 * scale;
    let tilt = new THREE.Euler(0, 0, 0);

    if (style === 'horizontal') {
      startA = -Math.PI * 0.55;
      arc = Math.PI * 1.1;
      tilt.set(0.2, 0.3, -0.1);
    } else if (style === 'diagonal') {
      startA = -Math.PI * 0.3;
      arc = Math.PI * 0.95;
      tilt.set(0.8, 0.2, 0.5);
      radius = 1.0 * scale;
    } else if (style === 'heavy' || style === 'crit') {
      startA = -Math.PI * 0.2;
      arc = Math.PI * 1.15;
      tilt.set(1.1, 0.15, 0.2);
      radius = 1.25 * scale;
    }

    const ribbon = new THREE.Mesh(makeArcRibbon(radius, 0.22 * scale, startA, arc, 28), mats.trail.clone());
    ribbon.material.opacity = 0.7;
    ribbon.rotation.copy(tilt);
    group.add(ribbon);

    // hot core edge
    const core = new THREE.Mesh(makeArcRibbon(radius + 0.05, 0.06 * scale, startA, arc, 28), mats.trailHot.clone());
    core.material.opacity = 0.85;
    core.rotation.copy(tilt);
    group.add(core);

    // thin black void line
    const voidLine = new THREE.Mesh(
      makeArcRibbon(radius + 0.08, 0.03 * scale, startA, arc * 0.9, 20),
      mats.riftVoid.clone()
    );
    voidLine.material.opacity = 0.5;
    voidLine.rotation.copy(tilt);
    group.add(voidLine);

    group.position.copy(origin);
    group.position.y -= 0.3;

    addFx(group, style === 'heavy' ? 0.7 : 0.45, (fx, p) => {
      const fade = p < 0.2 ? p / 0.2 : 1 - (p - 0.2) / 0.8;
      group.children.forEach((c, i) => {
        if (c.material) {
          c.material.opacity = fade * (i === 0 ? 0.65 : i === 1 ? 0.8 : 0.45);
        }
        c.scale.set(1, 1, 1 + p * 0.15);
      });
      // slight drift
      group.rotation.y += 0.01;
    });
  }

  function spawnDistortionRibbon(scale = 1) {
    if (!enabled) return;
    const g = new THREE.Group();
    const origin = weaponTip();
    for (let i = 0; i < 3; i++) {
      const m = new THREE.Mesh(
        makeArcRibbon(0.5 + i * 0.15, 0.04, -0.4 + i * 0.1, Math.PI * 0.7, 16),
        mats.riftEdge.clone()
      );
      m.material.opacity = 0.5 - i * 0.1;
      m.rotation.set(0.5 + i * 0.2, i * 0.3, 0.4);
      g.add(m);
    }
    g.position.copy(origin);
    addFx(g, 0.5, (fx, p) => {
      g.scale.setScalar(1 + p * 0.4 * scale);
      g.children.forEach((c) => {
        if (c.material) c.material.opacity = (1 - p) * 0.5;
      });
    });
  }

  function spawnSpatialCut(scale = 1) {
    if (!enabled) return;
    const g = new THREE.Group();
    const p = weaponTip();
    // fractured plane
    const plane = new THREE.Mesh(new THREE.PlaneGeometry(1.2 * scale, 0.08 * scale), mats.riftVoid.clone());
    g.add(plane);
    const edge = new THREE.Mesh(new THREE.PlaneGeometry(1.25 * scale, 0.02 * scale), mats.riftEdge.clone());
    edge.position.z = 0.01;
    g.add(edge);
    // fragments
    for (let i = 0; i < 5; i++) {
      const f = new THREE.Mesh(
        new THREE.BoxGeometry(0.04, 0.04, 0.02),
        mats.fragment
      );
      f.position.set((Math.random() - 0.5) * scale, (Math.random() - 0.5) * 0.3, Math.random() * 0.1);
      f.userData.vel = new THREE.Vector3((Math.random() - 0.5) * 0.8, Math.random() * 0.5, (Math.random() - 0.5) * 0.4);
      g.add(f);
    }
    g.position.copy(p);
    g.lookAt(p.clone().add(new THREE.Vector3(0.3, 0.2, 1)));
    addFx(g, 0.4, (fx, t) => {
      plane.material.opacity = 0.9 * (1 - t);
      edge.material.opacity = 0.9 * (1 - t);
      g.children.forEach((c) => {
        if (c.userData.vel) {
          c.position.addScaledVector(c.userData.vel, 0.016);
          c.rotation.x += 0.1;
        }
      });
    });
  }

  function spawnImpactBurst(scale = 1) {
    if (!enabled) return;
    const g = new THREE.Group();
    const p = weaponTip();
    const ring = new THREE.Mesh(makeRingGeo(0.1, 0.18, 32), mats.riftEdge.clone());
    ring.rotation.x = -Math.PI / 2;
    g.add(ring);
    const ring2 = new THREE.Mesh(makeRingGeo(0.05, 0.1, 24), mats.trailHot.clone());
    ring2.rotation.x = -Math.PI / 2;
    g.add(ring2);
    g.position.copy(p);
    g.position.y = 0.15;
    addFx(g, 0.5, (fx, t) => {
      const s = (0.3 + t * 1.5) * scale;
      ring.scale.set(s, s, s);
      ring2.scale.set(s * 0.7, s * 0.7, s * 0.7);
      ring.material.opacity = (1 - t) * 0.8;
      ring2.material.opacity = (1 - t) * 0.9;
    });
  }

  function spawnHeavySlash(scale = 1) {
    if (!enabled) return;
    // Large curved dimensional slash that briefly remains
    const g = new THREE.Group();
    const origin = playerCenter();
    const slash = new THREE.Mesh(
      makeArcRibbon(1.3 * scale, 0.35 * scale, -0.9, Math.PI * 1.3, 36),
      mats.riftVoid.clone()
    );
    slash.material.opacity = 0.85;
    slash.rotation.set(1.0, 0.2, 0.15);
    g.add(slash);
    const edge = new THREE.Mesh(
      makeArcRibbon(1.35 * scale, 0.06 * scale, -0.9, Math.PI * 1.3, 36),
      mats.riftEdge.clone()
    );
    edge.rotation.copy(slash.rotation);
    g.add(edge);
    const glow = new THREE.Mesh(
      makeArcRibbon(1.2 * scale, 0.12 * scale, -0.85, Math.PI * 1.2, 28),
      mats.trailHot.clone()
    );
    glow.rotation.copy(slash.rotation);
    g.add(glow);
    g.position.copy(origin);
    g.position.y += 0.2;
    addFx(g, 0.85, (fx, t) => {
      const open = t < 0.2 ? t / 0.2 : 1;
      const fade = t > 0.55 ? 1 - (t - 0.55) / 0.45 : 1;
      g.scale.setScalar(0.7 + open * 0.4);
      slash.material.opacity = 0.85 * fade;
      edge.material.opacity = 0.95 * fade;
      glow.material.opacity = 0.7 * fade;
      // fracture jitter
      edge.position.x = Math.sin(t * 40) * 0.01 * fade;
    });
  }

  function gatherBladeEnergy(scale = 1) {
    if (!enabled) return;
    const g = new THREE.Group();
    const tip = weaponTip();
    for (let i = 0; i < 8; i++) {
      const bit = new THREE.Mesh(new THREE.BoxGeometry(0.03, 0.03, 0.03), mats.emissive);
      const a = (i / 8) * Math.PI * 2;
      bit.position.set(Math.cos(a) * 0.5 * scale, Math.sin(a * 2) * 0.2, Math.sin(a) * 0.5 * scale);
      bit.userData.home = bit.position.clone();
      g.add(bit);
    }
    g.position.copy(tip);
    addFx(g, 0.55, (fx, t) => {
      const tipNow = weaponTip();
      g.position.lerp(tipNow, 0.3);
      g.children.forEach((c, i) => {
        const pull = smoother(t);
        c.position.lerp(new THREE.Vector3(0, 0, 0), pull * 0.15);
        c.scale.setScalar(1 - t * 0.5);
      });
    });
  }

  function bladeEnergyPulse() {
    if (!enabled) return;
    const g = new THREE.Group();
    // pulse travels tipward along blade axis
    const ring = new THREE.Mesh(makeRingGeo(0.04, 0.1, 20), mats.riftEdge.clone());
    g.add(ring);
    const base = worldPos(weapon);
    g.position.copy(base);
    addFx(g, 0.5, (fx, t) => {
      g.position.y = base.y + t * 1.3;
      ring.scale.setScalar(1 + Math.sin(t * Math.PI) * 0.5);
      ring.material.opacity = (1 - t) * 0.9;
      ring.rotation.x = Math.PI / 2;
    });
  }

  function spawnDistortionField(scale = 1) {
    if (!enabled) return;
    const g = new THREE.Group();
    const p = playerCenter();
    for (let i = 0; i < 3; i++) {
      const r = new THREE.Mesh(makeRingGeo(0.4 + i * 0.15, 0.45 + i * 0.15, 40), mats.riftEdge.clone());
      r.material.opacity = 0.35;
      r.rotation.x = Math.PI / 2 + i * 0.1;
      g.add(r);
    }
    g.position.copy(p);
    addFx(g, 0.6, (fx, t) => {
      g.rotation.y += 0.04;
      g.scale.setScalar((0.8 + t * 0.4) * scale);
      g.children.forEach((c) => {
        if (c.material) c.material.opacity = (1 - t) * 0.4;
      });
    });
  }

  // ---------- Equip rift ----------------------------------------------------
  function spawnEquipRift() {
    if (!enabled) return;
    if (equipRift) {
      root.remove(equipRift);
      disposeObject(equipRift);
    }
    equipRift = new THREE.Group();
    equipRift.name = 'EquipRift';
    const hand = worldPos(mannequin?.userData?.bones?.WeaponSocket) || playerCenter();

    const voidP = new THREE.Mesh(new THREE.PlaneGeometry(0.05, 0.4), mats.riftVoid.clone());
    equipRift.add(voidP);
    const e1 = new THREE.Mesh(new THREE.PlaneGeometry(0.02, 0.42), mats.riftEdge.clone());
    e1.position.x = -0.03;
    const e2 = e1.clone();
    e2.position.x = 0.03;
    equipRift.add(e1, e2);

    equipRift.position.copy(hand);
    equipRift.position.y -= 0.1;
    root.add(equipRift);
    equipRift.userData.phase = 'open';
    equipRift.userData.t = 0;
  }

  function expandEquipRift() {
    if (!equipRift) return;
    equipRift.userData.phase = 'expand';
  }

  function gatherEquipEnergy() {
    if (!equipRift) return;
    equipRift.userData.phase = 'gather';
    // energy wisps
    for (let i = 0; i < 6; i++) {
      const w = new THREE.Mesh(new THREE.BoxGeometry(0.03, 0.03, 0.03), mats.emissive);
      w.position.set((Math.random() - 0.5) * 0.6, (Math.random() - 0.5) * 0.6, (Math.random() - 0.5) * 0.3);
      w.userData.wisp = true;
      equipRift.add(w);
    }
  }

  function closeEquipRift() {
    if (!equipRift) return;
    equipRift.userData.phase = 'close';
  }

  function updateEquipRift(dt) {
    if (!equipRift) return;
    equipRift.userData.t += dt;
    const t = equipRift.userData.t;
    const phase = equipRift.userData.phase;
    const voidP = equipRift.children[0];
    if (phase === 'open') {
      voidP.scale.x = 0.5 + t * 2;
    } else if (phase === 'expand') {
      voidP.scale.set(3 + t * 2, 1.5, 1);
      equipRift.children[1].scale.y = 1.5;
      equipRift.children[2].scale.y = 1.5;
    } else if (phase === 'gather') {
      equipRift.children.forEach((c) => {
        if (c.userData.wisp) c.position.multiplyScalar(0.92);
      });
    } else if (phase === 'close') {
      equipRift.scale.multiplyScalar(0.85);
      if (equipRift.scale.x < 0.05) {
        root.remove(equipRift);
        disposeObject(equipRift);
        equipRift = null;
      }
    }
    // face camera lightly
    if (equipRift && camera) {
      equipRift.lookAt(camera.position);
    }
  }

  // ---------- Ender Echo projectile ----------------------------------------
  function createEchoMesh() {
    const g = new THREE.Group();
    g.name = 'EnderEcho';

    // dark purple core
    const voidCore = new THREE.Mesh(new THREE.IcosahedronGeometry(0.1, 1), mats.echoVoid);
    g.add(voidCore);

    // bright inner
    const inner = new THREE.Mesh(new THREE.IcosahedronGeometry(0.07, 1), mats.echoCore);
    g.add(inner);

    // shell
    const shell = new THREE.Mesh(new THREE.IcosahedronGeometry(0.13, 1), mats.echoShell);
    g.add(shell);

    // rotating rings
    for (let i = 0; i < 3; i++) {
      const ring = new THREE.Mesh(makeRingGeo(0.14 + i * 0.03, 0.155 + i * 0.03, 32), mats.echoRing.clone());
      ring.rotation.set(i * 0.7, i * 0.5, i * 0.3);
      ring.userData.spin = 1.5 + i * 0.5;
      ring.userData.axis = i;
      g.add(ring);
    }

    // geometric fragments
    for (let i = 0; i < 6; i++) {
      const f = new THREE.Mesh(new THREE.BoxGeometry(0.03, 0.03, 0.015), mats.fragment);
      f.userData.orbit = { a: (i / 6) * Math.PI * 2, r: 0.18, s: 1.2 + i * 0.1 };
      g.add(f);
    }

    // trailing ribbon anchor
    g.userData.trail = [];
    return g;
  }

  function launchEcho() {
    if (!enabled) return;
    if (echo) {
      root.remove(echo);
      disposeObject(echo);
    }
    echo = createEchoMesh();
    const tip = weaponTip();
    echo.position.copy(tip);
    echo.userData.vel = new THREE.Vector3(0.0, 0.05, -1.0).normalize().multiplyScalar(4.5);
    // aim forward from mannequin
    if (mannequin) {
      const dir = new THREE.Vector3(0, 0.05, -1);
      dir.applyQuaternion(mannequin.getWorldQuaternion(new THREE.Quaternion()));
      echo.userData.vel.copy(dir.normalize().multiplyScalar(4.5));
    }
    echo.userData.life = 1.6;
    echo.userData.age = 0;
    echo.userData.hit = false;
    root.add(echo);

    // launch flash
    spawnImpactBurst(0.5);
  }

  function updateEcho(dt) {
    if (!echo) return;
    echo.userData.age += dt;
    const age = echo.userData.age;

    echo.position.addScaledVector(echo.userData.vel, dt);
    echo.rotation.y += dt * 2.5;
    echo.rotation.x += dt * 1.2;

    // rings spin
    echo.children.forEach((c) => {
      if (c.userData.spin) {
        if (c.userData.axis === 0) c.rotation.z += dt * c.userData.spin;
        else if (c.userData.axis === 1) c.rotation.x += dt * c.userData.spin;
        else c.rotation.y += dt * c.userData.spin;
      }
      if (c.userData.orbit) {
        const o = c.userData.orbit;
        const a = o.a + age * o.s;
        c.position.set(Math.cos(a) * o.r, Math.sin(a * 1.3) * 0.05, Math.sin(a) * o.r);
      }
    });

    // trail ribbon points
    echo.userData.trail.push(echo.position.clone());
    if (echo.userData.trail.length > 12) echo.userData.trail.shift();

    // drop short ribbon segments
    if (Math.floor(age * 20) !== Math.floor((age - dt) * 20)) {
      const seg = new THREE.Mesh(
        new THREE.PlaneGeometry(0.08, 0.25),
        mats.trail.clone()
      );
      seg.material.opacity = 0.45;
      seg.position.copy(echo.position);
      seg.lookAt(echo.position.clone().sub(echo.userData.vel));
      addFx(seg, 0.35, (fx, p) => {
        seg.material.opacity = 0.45 * (1 - p);
        seg.scale.y = 1 + p;
      });
    }

    // occasional afterimage
    if (Math.random() < 0.04) {
      const ghost = echo.clone(true);
      ghost.traverse((o) => {
        if (o.isMesh) {
          o.material = mats.afterimage.clone();
          o.material.opacity = 0.25;
        }
      });
      addFx(ghost, 0.3, (fx, p) => {
        ghost.traverse((o) => {
          if (o.material) o.material.opacity = 0.25 * (1 - p);
        });
      });
    }

    // Hit dummy / pass through entity
    if (!echo.userData.hit && dummy.visible) {
      if (echo.position.distanceTo(dummy.position.clone().setY(1)) < 0.55) {
        echo.userData.hit = true;
        onEchoHitEntity(dummy);
      }
    }

    // auto hit around mid for showcase if no dummy
    if (!echo.userData.hit && age > 0.45 && age < 0.5) {
      // soft flash as if passing something
      const flash = new THREE.Mesh(new THREE.IcosahedronGeometry(0.2, 0), mats.trailHot.clone());
      flash.position.copy(echo.position);
      addFx(flash, 0.25, (fx, p) => {
        flash.scale.setScalar(1 + p * 2);
        flash.material.opacity = 0.8 * (1 - p);
      });
    }

    if (age > echo.userData.life || echo.position.length() > 14) {
      root.remove(echo);
      disposeObject(echo);
      echo = null;
    }
  }

  function onEchoHitEntity(target) {
    // silhouette distortion, spatial cut, flash, afterimage, void mark
    const p = target.position.clone();
    p.y += 1.0;

    // flash
    const flash = new THREE.Mesh(new THREE.SphereGeometry(0.25, 12, 12), mats.trailHot.clone());
    flash.position.copy(p);
    addFx(flash, 0.2, (fx, t) => {
      flash.scale.setScalar(1 + t * 3);
      flash.material.opacity = 1 - t;
    });

    // spatial cut
    const cut = new THREE.Mesh(new THREE.PlaneGeometry(0.8, 0.06), mats.riftVoid.clone());
    cut.position.copy(p);
    addFx(cut, 0.35, (fx, t) => {
      cut.scale.x = 1 + t;
      cut.material.opacity = 0.9 * (1 - t);
      cut.rotation.z = t * 0.3;
    });

    // afterimage of target
    const ghost = target.clone(true);
    ghost.traverse((o) => {
      if (o.isMesh) {
        o.material = mats.afterimage.clone();
        o.castShadow = false;
      }
    });
    ghost.position.copy(target.position);
    addFx(ghost, 0.5, (fx, t) => {
      ghost.position.x += 0.01;
      ghost.traverse((o) => {
        if (o.material) o.material.opacity = 0.4 * (1 - t);
      });
    });

    // brief target scale distort
    target.userData.distortT = 0.35;

    applyVoidMark(target);
  }

  // ---------- Void Mark -----------------------------------------------------
  function applyVoidMark(target, stacks) {
    markStacks = stacks ?? Math.min(3, markStacks + 1);
    dummy.visible = true;

    if (markTarget) {
      root.remove(markTarget);
      disposeObject(markTarget);
    }
    markTarget = createMarkRune(markStacks);
    markTarget.position.copy(target.position);
    markTarget.position.y += 1.1;
    root.add(markTarget);

    if (markStacks >= 3) {
      // auto collapse after short delay via external play often
    }
  }

  function createMarkRune(stacks) {
    const g = new THREE.Group();
    g.name = 'VoidMark';
    g.userData.stacks = stacks;

    const size = 0.35 + stacks * 0.12;
    const mat = stacks >= 3 ? mats.markHot : mats.mark;

    // outer ring
    const ring = new THREE.Mesh(makeRingGeo(size * 0.7, size * 0.85, 40), mat.clone());
    ring.rotation.x = -Math.PI / 2;
    g.add(ring);

    // inner rune bars (dimensional glyph)
    for (let i = 0; i < 3 + stacks; i++) {
      const bar = new THREE.Mesh(
        new THREE.PlaneGeometry(size * 0.15, size * (0.5 + (i % 2) * 0.2)),
        mat.clone()
      );
      bar.rotation.x = -Math.PI / 2;
      bar.rotation.z = (i / (3 + stacks)) * Math.PI * 2;
      bar.position.y = 0.01;
      g.add(bar);
    }

    // cross diamond
    const dia = new THREE.Mesh(new THREE.PlaneGeometry(size * 0.35, size * 0.35), mat.clone());
    dia.rotation.x = -Math.PI / 2;
    dia.rotation.z = Math.PI / 4;
    g.add(dia);

    if (stacks >= 2) {
      const ring2 = new THREE.Mesh(makeRingGeo(size * 0.4, size * 0.5, 32), mat.clone());
      ring2.rotation.x = -Math.PI / 2;
      ring2.position.y = 0.02;
      g.add(ring2);
    }

    if (stacks >= 3) {
      // floating fragments + unstable
      for (let i = 0; i < 6; i++) {
        const f = new THREE.Mesh(new THREE.BoxGeometry(0.04, 0.04, 0.04), mats.fragment);
        f.userData.orbit = { a: (i / 6) * Math.PI * 2, r: size * 0.9 };
        g.add(f);
      }
      const core = new THREE.Mesh(new THREE.IcosahedronGeometry(0.08, 0), mats.echoCore);
      g.add(core);
    }

    g.userData.spin = 0.6 + stacks * 0.4;
    return g;
  }

  function updateMark(dt) {
    if (!markTarget) return;
    markTarget.rotation.y += dt * (markTarget.userData.spin || 1);
    markTarget.children.forEach((c) => {
      if (c.userData.orbit) {
        const o = c.userData.orbit;
        o.a += dt * 1.5;
        c.position.set(Math.cos(o.a) * o.r, 0.1 + Math.sin(o.a * 2) * 0.05, Math.sin(o.a) * o.r);
      }
    });
    // pulse scale by stacks
    const pulse = 1 + Math.sin(performance.now() * 0.005 * markStacks) * 0.04 * markStacks;
    markTarget.scale.setScalar(pulse);

    // follow dummy
    if (dummy.visible) {
      markTarget.position.x = dummy.position.x;
      markTarget.position.z = dummy.position.z;
      markTarget.position.y = dummy.position.y + 1.1;
    }
  }

  // ---------- Teleport ------------------------------------------------------
  function teleportDepartStart() {
    if (!enabled) return;
    const p = playerCenter();
    // vertical rift
    const g = new THREE.Group();
    const voidP = new THREE.Mesh(new THREE.PlaneGeometry(0.08, 1.8), mats.riftVoid.clone());
    g.add(voidP);
    const e1 = new THREE.Mesh(new THREE.PlaneGeometry(0.03, 1.85), mats.riftEdge.clone());
    e1.position.x = -0.05;
    const e2 = e1.clone();
    e2.position.x = 0.05;
    g.add(e1, e2);
    g.position.copy(p);
    addFx(g, 0.55, (fx, t) => {
      const open = t < 0.4 ? t / 0.4 : 1 - (t - 0.4) / 0.6;
      voidP.scale.x = 1 + open * 4;
      e1.material.opacity = open;
      e2.material.opacity = open;
      voidP.material.opacity = 0.95 * open;
      g.lookAt(camera.position);
    });
    spawnAfterimage('teleport', 2);
  }

  function teleportCollapse() {
    if (!enabled) return;
    const p = playerCenter();
    const sphere = new THREE.Mesh(new THREE.IcosahedronGeometry(0.3, 1), mats.echoVoid);
    sphere.position.copy(p);
    addFx(sphere, 0.35, (fx, t) => {
      sphere.scale.setScalar(1 - t * 0.9);
    });
    spawnAfterimage('teleport', 3);
  }

  function teleportArrive() {
    if (!enabled) return;
    const p = playerCenter();
    // shift already handled by anim — VFX at new pos
    const g = new THREE.Group();
    const voidP = new THREE.Mesh(new THREE.PlaneGeometry(0.1, 1.8), mats.riftVoid.clone());
    g.add(voidP);
    const edge = new THREE.Mesh(new THREE.PlaneGeometry(0.04, 1.9), mats.riftEdge.clone());
    g.add(edge);
    g.position.copy(p);
    g.position.x += 1.5;
    addFx(g, 0.4, (fx, t) => {
      const open = t < 0.3 ? t / 0.3 : 1 - (t - 0.3) / 0.7;
      voidP.scale.x = open * 5;
      voidP.material.opacity = open;
      edge.material.opacity = open;
      g.lookAt(camera.position);
    });
    spawnAfterimage('teleport', 2);
  }

  function teleportShockwave() {
    if (!enabled) return;
    const p = playerCenter();
    p.x += 1.5;
    const ring = new THREE.Mesh(makeRingGeo(0.2, 0.35, 40), mats.riftEdge.clone());
    ring.rotation.x = -Math.PI / 2;
    ring.position.copy(p);
    ring.position.y = 0.1;
    addFx(ring, 0.5, (fx, t) => {
      ring.scale.setScalar(1 + t * 3);
      ring.material.opacity = (1 - t) * 0.85;
    });
  }

  // ---------- Void Slash rift -----------------------------------------------
  function spawnVoidSlashRift(scale = 1) {
    if (!enabled) return;
    if (voidSlash) {
      root.remove(voidSlash);
      disposeObject(voidSlash);
    }
    voidSlash = new THREE.Group();
    voidSlash.name = 'VoidSlash';

    const p = playerCenter();
    // black/purple interior plane with fractured edge
    const interior = new THREE.Mesh(
      new THREE.PlaneGeometry(2.4 * scale, 1.6 * scale, 8, 4),
      mats.riftVoid.clone()
    );
    // jagged edge via vertex noise
    const pos = interior.geometry.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i);
      const y = pos.getY(i);
      if (Math.abs(x) > 1.0 * scale || Math.abs(y) > 0.6 * scale) {
        pos.setX(i, x + (Math.random() - 0.5) * 0.12);
        pos.setY(i, y + (Math.random() - 0.5) * 0.1);
      }
    }
    pos.needsUpdate = true;
    voidSlash.add(interior);

    // glowing violet edge frame pieces
    const edges = [
      [0, 0.82 * scale, 2.5 * scale, 0.04],
      [0, -0.82 * scale, 2.5 * scale, 0.04],
      [1.25 * scale, 0, 0.04, 1.7 * scale],
      [-1.25 * scale, 0, 0.04, 1.7 * scale],
    ];
    edges.forEach(([x, y, w, h]) => {
      const e = new THREE.Mesh(new THREE.PlaneGeometry(w, h), mats.riftEdge.clone());
      e.position.set(x, y, 0.01);
      voidSlash.add(e);
    });

    // fracture lines
    for (let i = 0; i < 6; i++) {
      const f = new THREE.Mesh(
        new THREE.PlaneGeometry(0.6 + Math.random() * 0.5, 0.02),
        mats.riftFracture.clone()
      );
      f.position.set((Math.random() - 0.5) * 1.5, (Math.random() - 0.5) * 1.0, 0.02);
      f.rotation.z = (Math.random() - 0.5) * 1.2;
      voidSlash.add(f);
    }

    // trailing fragments
    for (let i = 0; i < 8; i++) {
      const f = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.05, 0.02), mats.fragment);
      f.position.set((Math.random() - 0.5) * 2, (Math.random() - 0.5) * 1.4, Math.random() * 0.2);
      f.userData.vel = new THREE.Vector3((Math.random() - 0.5), (Math.random() - 0.5), Math.random() * 0.5);
      voidSlash.add(f);
    }

    voidSlash.position.copy(p);
    voidSlash.position.add(new THREE.Vector3(0, 0.3, -1.5));
    voidSlash.userData.age = 0;
    voidSlash.userData.life = 0.7;
    voidSlash.scale.set(0.1, 0.1, 1);
    root.add(voidSlash);

    // mark dummy if visible
    dummy.visible = true;
    applyVoidMark(dummy);
  }

  function collapseVoidSlash() {
    if (!voidSlash) return;
    voidSlash.userData.collapsing = true;
  }

  function updateVoidSlash(dt) {
    if (!voidSlash) return;
    voidSlash.userData.age += dt;
    const t = voidSlash.userData.age / voidSlash.userData.life;
    const collapsing = voidSlash.userData.collapsing || t > 0.55;

    if (!collapsing) {
      // open fast
      const open = Math.min(1, voidSlash.userData.age / 0.12);
      voidSlash.scale.set(open, open, 1);
      // distortion shimmer
      voidSlash.rotation.z = Math.sin(voidSlash.userData.age * 30) * 0.02;
      voidSlash.children.forEach((c) => {
        if (c.userData.vel) {
          c.position.addScaledVector(c.userData.vel, dt * 0.5);
        }
      });
    } else {
      voidSlash.scale.x *= 0.88;
      voidSlash.scale.y *= 0.9;
      voidSlash.children.forEach((c) => {
        if (c.material && c.material.opacity !== undefined) {
          c.material.opacity *= 0.9;
        }
      });
      if (voidSlash.scale.x < 0.05) {
        root.remove(voidSlash);
        disposeObject(voidSlash);
        voidSlash = null;
      }
    }
  }

  // ---------- Anchor --------------------------------------------------------
  function placeAnchor() {
    if (!enabled) return;
    if (anchor) {
      root.remove(anchor);
      disposeObject(anchor);
    }
    anchor = new THREE.Group();
    const ring = new THREE.Mesh(makeRingGeo(0.35, 0.45, 40), mats.anchorRing.clone());
    ring.rotation.x = -Math.PI / 2;
    anchor.add(ring);
    const ring2 = new THREE.Mesh(makeRingGeo(0.2, 0.28, 32), mats.anchorRing.clone());
    ring2.rotation.x = -Math.PI / 2;
    ring2.position.y = 0.05;
    anchor.add(ring2);
    const core = new THREE.Mesh(new THREE.IcosahedronGeometry(0.1, 1), mats.echoCore);
    core.position.y = 0.15;
    anchor.add(core);
    // vertical beam
    const beam = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.2, 6), mats.riftEdge.clone());
    beam.position.y = 0.6;
    beam.material.opacity = 0.4;
    anchor.add(beam);

    const p = playerCenter();
    anchor.position.set(p.x + 1.2, 0.05, p.z - 0.8);
    anchor.userData.spin = 1;
    root.add(anchor);
  }

  function recallAnchor() {
    if (!anchor) return;
    const node = anchor;
    anchor = null;
    const dest = playerCenter();
    addFx(node, 0.4, (fx, t) => {
      node.scale.setScalar(1 - t);
      node.position.lerp(dest, t * 0.3);
    }, () => {
      teleportShockwave();
    });
  }

  // ---------- Paradox -------------------------------------------------------
  function paradoxFreeze() {
    spawnDistortionField(0.6);
  }
  function paradoxVanish() {
    spawnAfterimage('paradox', 4);
    teleportCollapse();
  }
  function paradoxEmergeBehind() {
    // crack behind enemy (dummy)
    dummy.visible = true;
    const p = dummy.position.clone();
    p.y += 1;
    p.z += 0.5;
    const g = new THREE.Group();
    const voidP = new THREE.Mesh(new THREE.PlaneGeometry(0.08, 1.5), mats.riftVoid.clone());
    g.add(voidP);
    const e = new THREE.Mesh(new THREE.PlaneGeometry(0.03, 1.55), mats.riftEdge.clone());
    g.add(e);
    g.position.copy(p);
    addFx(g, 0.35, (fx, t) => {
      const open = t < 0.3 ? t / 0.3 : 1 - (t - 0.3) / 0.7;
      voidP.scale.x = open * 4;
      voidP.material.opacity = open;
      e.material.opacity = open;
    });
    spawnAfterimage('paradox', 2);
  }

  // ---------- Rift Collapse -------------------------------------------------
  function riftBendStart() {
    dummy.visible = true;
    applyVoidMark(dummy, 3);
    const p = dummy.position.clone();
    p.y += 1;
    // bending rings
    const g = new THREE.Group();
    for (let i = 0; i < 4; i++) {
      const r = new THREE.Mesh(makeRingGeo(0.8 - i * 0.1, 0.88 - i * 0.1, 48), mats.riftEdge.clone());
      r.material.opacity = 0.4;
      r.rotation.x = -Math.PI / 2 + i * 0.05;
      r.userData.phase = i;
      g.add(r);
    }
    g.position.copy(p);
    g.name = 'RiftBend';
    addFx(g, 1.6, (fx, t) => {
      g.rotation.y += 0.03;
      g.children.forEach((c, i) => {
        const shrink = 1 - t * 0.7;
        c.scale.setScalar(shrink * (1 - i * 0.05));
        c.rotation.z = Math.sin(t * 8 + i) * 0.2 * t;
        c.material.opacity = 0.5 * (1 - t * 0.5);
      });
    });
  }

  function riftSpiralIn() {
    const p = dummy.position.clone();
    p.y += 1;
    const g = new THREE.Group();
    for (let i = 0; i < 16; i++) {
      const bit = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.05, 0.05), mats.fragment);
      const a = (i / 16) * Math.PI * 2;
      bit.position.set(Math.cos(a) * 1.4, Math.sin(a * 2) * 0.3, Math.sin(a) * 1.4);
      bit.userData.a = a;
      g.add(bit);
    }
    // purple energy spirals
    for (let i = 0; i < 2; i++) {
      const ribbon = new THREE.Mesh(
        makeArcRibbon(1.0 - i * 0.2, 0.06, 0, Math.PI * 1.8, 24),
        mats.trail.clone()
      );
      ribbon.rotation.x = Math.PI / 2 + i * 0.3;
      g.add(ribbon);
    }
    g.position.copy(p);
    addFx(g, 0.8, (fx, t) => {
      g.rotation.y += 0.12;
      g.children.forEach((c) => {
        if (c.userData.a !== undefined) {
          const r = 1.4 * (1 - t);
          c.position.x = Math.cos(c.userData.a + t * 6) * r;
          c.position.z = Math.sin(c.userData.a + t * 6) * r;
          c.position.y *= 0.98;
        }
      });
    });
  }

  function riftCompress() {
    const p = dummy.position.clone();
    p.y += 1;
    const sphere = new THREE.Mesh(new THREE.IcosahedronGeometry(0.6, 1), mats.echoVoid);
    const shell = new THREE.Mesh(new THREE.IcosahedronGeometry(0.65, 1), mats.echoShell);
    const g = new THREE.Group();
    g.add(sphere, shell);
    g.position.copy(p);
    addFx(g, 0.45, (fx, t) => {
      const s = 1 - smoother(t) * 0.85;
      g.scale.setScalar(s);
      shell.material.opacity = 0.7 * (1 - t);
    });
    // distort dummy
    dummy.userData.distortT = 0.5;
  }

  function riftBurst() {
    const p = dummy.position.clone();
    p.y += 1;
    // short violent void burst — NOT a purple explosion
    const g = new THREE.Group();
    // dark core flash
    const core = new THREE.Mesh(new THREE.IcosahedronGeometry(0.2, 1), mats.echoVoid);
    g.add(core);
    // expanding dark ring
    const ring = new THREE.Mesh(makeRingGeo(0.1, 0.25, 40), mats.riftEdge.clone());
    ring.rotation.x = -Math.PI / 2;
    g.add(ring);
    // fracture shards outward
    for (let i = 0; i < 12; i++) {
      const f = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.02, 0.08), mats.riftFracture.clone());
      const a = (i / 12) * Math.PI * 2;
      f.userData.vel = new THREE.Vector3(Math.cos(a), (Math.random() - 0.3) * 0.5, Math.sin(a)).multiplyScalar(3);
      f.position.set(0, 0, 0);
      g.add(f);
    }
    g.position.copy(p);
    addFx(g, 0.55, (fx, t) => {
      core.scale.setScalar(1 + t * 2);
      core.material.opacity = 1 - t;
      ring.scale.setScalar(1 + t * 5);
      ring.material.opacity = (1 - t) * 0.9;
      g.children.forEach((c) => {
        if (c.userData.vel) {
          c.position.addScaledVector(c.userData.vel, 0.016);
          c.material.opacity = (1 - t) * 0.8;
        }
      });
    });

    // clear mark
    if (markTarget) {
      root.remove(markTarget);
      disposeObject(markTarget);
      markTarget = null;
      markStacks = 0;
    }
  }

  // ---------- Ultimate domain -----------------------------------------------
  function ultimateDrive() {
    if (!enabled) return;
    spawnImpactBurst(1.2);
    const p = playerCenter();
    p.y = 0.05;
    const crack = new THREE.Mesh(new THREE.PlaneGeometry(0.8, 0.1), mats.riftEdge.clone());
    crack.rotation.x = -Math.PI / 2;
    crack.position.copy(p);
    addFx(crack, 0.6, (fx, t) => {
      crack.scale.x = 1 + t * 3;
      crack.material.opacity = 1 - t;
    });
  }

  function ultimateRingExpand() {
    if (!enabled) return;
    if (domain) {
      root.remove(domain);
      disposeObject(domain);
    }
    domain = new THREE.Group();
    domain.name = 'EndDimension';

    const p = playerCenter();
    p.y = 0.08;

    // main ground ring
    const ring = new THREE.Mesh(makeRingGeo(2.8, 3.05, 64), mats.riftEdge.clone());
    ring.rotation.x = -Math.PI / 2;
    ring.material.opacity = 0.85;
    domain.add(ring);

    // inner rings
    for (let i = 0; i < 3; i++) {
      const r = new THREE.Mesh(makeRingGeo(1.2 + i * 0.5, 1.3 + i * 0.5, 48), mats.anchorRing.clone());
      r.rotation.x = -Math.PI / 2;
      r.material.opacity = 0.45;
      r.userData.spin = 0.3 + i * 0.15;
      domain.add(r);
    }

    // floating end fragments
    for (let i = 0; i < 14; i++) {
      const s = 0.08 + Math.random() * 0.1;
      const f = new THREE.Mesh(new THREE.BoxGeometry(s, s * 0.6, s * 0.5), mats.fragment);
      const a = (i / 14) * Math.PI * 2;
      const rad = 1.2 + Math.random() * 1.5;
      f.position.set(Math.cos(a) * rad, 0.3 + Math.random() * 1.2, Math.sin(a) * rad);
      f.userData.float = { a, rad, baseY: f.position.y, speed: 0.4 + Math.random() * 0.4 };
      domain.add(f);
    }

    // void cracks on ground
    for (let i = 0; i < 6; i++) {
      const c = new THREE.Mesh(
        new THREE.PlaneGeometry(0.08, 1.5 + Math.random()),
        mats.voidCracksSolid
      );
      c.rotation.x = -Math.PI / 2;
      c.rotation.z = (i / 6) * Math.PI * 2;
      c.position.y = 0.02;
      domain.add(c);
    }

    // rising energy columns (restrained)
    for (let i = 0; i < 4; i++) {
      const col = new THREE.Mesh(
        new THREE.CylinderGeometry(0.02, 0.04, 1.5, 4),
        mats.riftEdge.clone()
      );
      col.material.opacity = 0.35;
      const a = (i / 4) * Math.PI * 2 + 0.4;
      col.position.set(Math.cos(a) * 2.2, 0.75, Math.sin(a) * 2.2);
      domain.add(col);
    }

    domain.position.set(p.x, 0, p.z);
    domain.scale.setScalar(0.1);
    domain.userData.age = 0;
    domain.userData.mode = 'expand';
    root.add(domain);

    dummy.visible = true;
    applyVoidMark(dummy, 2);
  }

  function ultimateDomainStart() {
    if (domain) domain.userData.mode = 'domain';
    applyVoidMark(dummy, 3);
  }

  function ultimatePull() {
    if (!domain) return;
    domain.userData.mode = 'pull';
    // pull dummy toward center
    dummy.userData.pullTo = domain.position.clone();
  }

  function ultimateCollapse() {
    if (!domain) return;
    domain.userData.mode = 'collapse';
    // big collapse at domain center
    const p = domain.position.clone();
    p.y = 1.2;
    // reuse collapse sequence
    const g = new THREE.Group();
    for (let i = 0; i < 5; i++) {
      const r = new THREE.Mesh(makeRingGeo(2 - i * 0.3, 2.1 - i * 0.3, 48), mats.riftEdge.clone());
      r.rotation.x = -Math.PI / 2;
      r.userData.i = i;
      g.add(r);
    }
    const core = new THREE.Mesh(new THREE.IcosahedronGeometry(0.5, 1), mats.echoVoid);
    g.add(core);
    g.position.copy(p);
    addFx(g, 0.9, (fx, t) => {
      const shrink = 1 - smoother(t);
      g.children.forEach((c) => {
        if (c.userData.i !== undefined) {
          c.scale.setScalar(shrink);
          c.material.opacity = (1 - t) * 0.8;
          c.rotation.z = t * (1 + c.userData.i);
        }
      });
      core.scale.setScalar(Math.max(0.05, 1 - t) * (t > 0.7 ? (t - 0.7) * 8 : 1));
    });

    // burst shards
    setTimeout(() => {
      riftBurstAt(p);
    }, 400);

    // remove domain
    addFx(domain, 0.6, (fx, t) => {
      domain.scale.setScalar(Math.max(0.01, 1 - t));
      domain.children.forEach((c) => {
        if (c.material && c.material.opacity !== undefined) c.material.opacity *= 0.95;
      });
    }, () => {
      domain = null;
    });
  }

  function riftBurstAt(p) {
    const g = new THREE.Group();
    const ring = new THREE.Mesh(makeRingGeo(0.2, 0.4, 40), mats.riftFracture.clone());
    ring.rotation.x = -Math.PI / 2;
    g.add(ring);
    for (let i = 0; i < 16; i++) {
      const f = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.03, 0.1), mats.fragment);
      const a = (i / 16) * Math.PI * 2;
      f.userData.vel = new THREE.Vector3(Math.cos(a), Math.random() * 0.6, Math.sin(a)).multiplyScalar(4);
      g.add(f);
    }
    g.position.copy(p);
    addFx(g, 0.6, (fx, t) => {
      ring.scale.setScalar(1 + t * 4);
      ring.material.opacity = 1 - t;
      g.children.forEach((c) => {
        if (c.userData.vel) c.position.addScaledVector(c.userData.vel, 0.016);
      });
    });
    if (markTarget) {
      root.remove(markTarget);
      disposeObject(markTarget);
      markTarget = null;
      markStacks = 0;
    }
  }

  function spatialDisplace() {
    dummy.visible = true;
    const from = dummy.position.clone();
    const to = from.clone().add(new THREE.Vector3(2.5, 0, -1.2));
    // spatial cut path
    spawnSpatialCut(1.0);
    const ghost = dummy.clone(true);
    ghost.traverse((o) => {
      if (o.isMesh) o.material = mats.afterimage.clone();
    });
    ghost.position.copy(from);
    addFx(ghost, 0.5, (fx, t) => {
      ghost.position.lerpVectors(from, to, t);
      ghost.traverse((o) => {
        if (o.material) o.material.opacity = 0.4 * (1 - t);
      });
    });
    dummy.position.copy(to);
    // distortion burst at both ends
    [from, to].forEach((p) => {
      const r = new THREE.Mesh(makeRingGeo(0.15, 0.25, 24), mats.riftEdge.clone());
      r.rotation.x = -Math.PI / 2;
      r.position.copy(p);
      r.position.y = 0.1;
      addFx(r, 0.4, (fx, t) => {
        r.scale.setScalar(1 + t * 2);
        r.material.opacity = 1 - t;
      });
    });
  }

  // ---------- Dummy target --------------------------------------------------
  function createDummyTarget(mats) {
    const g = new THREE.Group();
    g.name = 'DummyTarget';
    const body = new THREE.Mesh(new THREE.BoxGeometry(0.5, 1.2, 0.3), mats.armor);
    body.position.y = 0.9;
    g.add(body);
    const head = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.35, 0.35), mats.skin);
    head.position.y = 1.7;
    g.add(head);
    g.userData.distortT = 0;
    return g;
  }

  function updateDummy(dt) {
    if (dummy.userData.distortT > 0) {
      dummy.userData.distortT -= dt;
      const d = dummy.userData.distortT;
      dummy.scale.x = 1 + Math.sin(d * 40) * 0.08;
      dummy.scale.y = 1 - Math.sin(d * 40) * 0.05;
    } else {
      dummy.scale.set(1, 1, 1);
    }
    if (dummy.userData.pullTo) {
      dummy.position.lerp(dummy.userData.pullTo, 0.08);
      if (dummy.position.distanceTo(dummy.userData.pullTo) < 0.15) {
        dummy.userData.pullTo = null;
      }
    }
  }

  function updateDomain(dt) {
    if (!domain) return;
    domain.userData.age += dt;
    const mode = domain.userData.mode;
    if (mode === 'expand') {
      const s = Math.min(1, domain.scale.x + dt * 2.5);
      domain.scale.setScalar(s);
    }
    domain.children.forEach((c) => {
      if (c.userData.spin) c.rotation.z += dt * c.userData.spin;
      if (c.userData.float) {
        const f = c.userData.float;
        f.a += dt * f.speed;
        c.position.x = Math.cos(f.a) * f.rad;
        c.position.z = Math.sin(f.a) * f.rad;
        c.position.y = f.baseY + Math.sin(domain.userData.age * 2 + f.a) * 0.1;
        c.rotation.y += dt;
      }
    });
    // occasional spatial flash
    if (mode === 'domain' && Math.random() < 0.02) {
      const flash = new THREE.Mesh(makeRingGeo(0.3, 0.4, 20), mats.riftEdge.clone());
      flash.rotation.x = -Math.PI / 2;
      flash.position.set((Math.random() - 0.5) * 4, 0.1, (Math.random() - 0.5) * 4);
      domain.add(flash);
      setTimeout(() => domain && domain.remove(flash), 300);
    }
    if (mode === 'pull') {
      domain.children.forEach((c) => {
        if (c.userData.float) {
          c.userData.float.rad *= 0.97;
          c.position.y *= 0.99;
        }
      });
    }
  }

  // ---------- Main update ---------------------------------------------------
  function update(dt, worldT) {
    if (!enabled) {
      // still age out nothing
      return;
    }

    for (let i = active.length - 1; i >= 0; i--) {
      const fx = active[i];
      fx.age += dt;
      const p = fx.age / fx.life;
      if (fx.update) fx.update(fx, Math.min(1, p), dt);
      if (fx.age >= fx.life) {
        if (fx.onEnd) fx.onEnd();
        root.remove(fx.obj);
        disposeObject(fx.obj);
        active.splice(i, 1);
      }
    }

    // afterimages
    for (let i = afterimages.length - 1; i >= 0; i--) {
      const a = afterimages[i];
      a.age += dt;
      const p = a.age / a.life;
      a.obj.traverse((o) => {
        if (o.material && o.material.opacity !== undefined) {
          o.material.opacity = a.fade * (1 - p);
        }
      });
      a.obj.position.y += dt * 0.1;
      if (a.age >= a.life) {
        root.remove(a.obj);
        disposeObject(a.obj);
        afterimages.splice(i, 1);
      }
    }

    updateEcho(dt);
    updateVoidSlash(dt);
    updateEquipRift(dt);
    updateMark(dt);
    updateDummy(dt);
    updateDomain(dt);

    if (anchor) {
      anchor.rotation.y += dt * 0.8;
      anchor.children.forEach((c, i) => {
        if (i < 2) c.rotation.z += dt * (i === 0 ? 0.5 : -0.7);
      });
    }
  }

  function onAnimStart(name) {
    // show dummy for combat-related
    if (['voidSlash', 'paradoxStep', 'riftCollapse', 'ultimate', 'passive', 'enderEcho', 'attack3', 'heavy'].includes(name)) {
      dummy.visible = true;
      dummy.position.set(1.8, 0, -0.8);
      dummy.userData.pullTo = null;
    }
    if (name === 'idle' || name === 'equip') {
      // keep clean
    }
    if (name === 'riftCollapse') {
      applyVoidMark(dummy, 3);
    }
  }

  function onAnimEnd() {}

  function setEnabled(v) {
    enabled = v;
    root.visible = v;
    if (!v) clearAll();
  }

  function smoother(t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  return {
    root,
    update,
    setEnabled,
    clearAll,
    onAnimStart,
    onAnimEnd,
    // exposed for animation director
    spawnAfterimage,
    spawnSlashTrail,
    spawnDistortionRibbon,
    spawnSpatialCut,
    spawnImpactBurst,
    spawnHeavySlash,
    gatherBladeEnergy,
    bladeEnergyPulse,
    spawnDistortionField,
    spawnEquipRift,
    expandEquipRift,
    gatherEquipEnergy,
    closeEquipRift,
    launchEcho,
    teleportDepartStart,
    teleportCollapse,
    teleportArrive,
    teleportShockwave,
    spawnVoidSlashRift,
    collapseVoidSlash,
    placeAnchor,
    recallAnchor,
    paradoxFreeze,
    paradoxVanish,
    paradoxEmergeBehind,
    riftBendStart,
    riftSpiralIn,
    riftCompress,
    riftBurst,
    ultimateDrive,
    ultimateRingExpand,
    ultimateDomainStart,
    ultimatePull,
    ultimateCollapse,
    spatialDisplace,
    applyVoidMark: () => applyVoidMark(dummy),
    get markStacks() {
      return markStacks;
    },
  };
}
