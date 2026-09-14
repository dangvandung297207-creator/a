import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { createMaterials, updateMaterials, setCrackIntensity } from './materials.js';
import { createEnderBlade, updateWeaponIdle } from './weapon.js';
import { createMannequin } from './mannequin.js';
import { createEnvironment } from './environment.js';
import { createVfxSystem } from './vfx.js';
import { createAnimationDirector, ANIM_META } from './animations.js';

/**
 * Ender Blade legendary weapon showcase.
 * Weapon-only inspection + combat mannequin views.
 */

const canvas = document.getElementById('c');
const loaderEl = document.getElementById('loader');
const nowName = document.getElementById('now-name');
const progressEl = document.getElementById('progress');

// ---------- Renderer -------------------------------------------------------
const renderer = new THREE.WebGLRenderer({
  canvas,
  antialias: true,
  alpha: false,
  powerPreference: 'high-performance',
});
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.outputColorSpace = THREE.SRGBColorSpace;
renderer.toneMapping = THREE.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.05;
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;

const scene = new THREE.Scene();
scene.background = new THREE.Color(0x07060c);
scene.fog = new THREE.FogExp2(0x0a0614, 0.028);

const camera = new THREE.PerspectiveCamera(42, window.innerWidth / window.innerHeight, 0.1, 80);
camera.position.set(2.8, 1.8, 4.2);

const controls = new OrbitControls(camera, canvas);
controls.enableDamping = true;
controls.dampingFactor = 0.08;
controls.minDistance = 2.0;
controls.maxDistance = 12;
controls.maxPolarAngle = Math.PI * 0.92;
controls.target.set(0, 1.0, 0);
controls.update();

// ---------- Content --------------------------------------------------------
const mats = createMaterials();
const env = createEnvironment(mats, scene);

const stage = new THREE.Group();
stage.name = 'Stage';
scene.add(stage);

const mannequin = createMannequin(mats);
stage.add(mannequin);

const weapon = createEnderBlade(mats);
const socket = mannequin.userData.bones.WeaponSocket;
socket.add(weapon);
// Grip adjustment for refined proportions
weapon.position.set(0, 0.02, 0.02);
weapon.rotation.set(0.15, 0, 0.05);
weapon.scale.setScalar(0.95);

// Standalone weapon pedestal (inspection mode)
const inspectRoot = new THREE.Group();
inspectRoot.name = 'InspectRoot';
inspectRoot.visible = false;
const inspectWeapon = createEnderBlade(mats);
inspectWeapon.position.set(0, 1.1, 0);
inspectRoot.add(inspectWeapon);
// Soft pedestal glow ring
const pedestal = new THREE.Mesh(
  new THREE.RingGeometry(0.35, 0.55, 40),
  mats.anchorRing.clone()
);
pedestal.rotation.x = -Math.PI / 2;
pedestal.position.y = 0.05;
inspectRoot.add(pedestal);
scene.add(inspectRoot);

const vfx = createVfxSystem({
  scene,
  mats,
  weapon,
  mannequin,
  camera,
});

const director = createAnimationDirector({
  bones: mannequin.userData.bones,
  restPose: mannequin.userData.restPose,
  weapon,
  weaponRoot: weapon,
  vfx,
  mats,
});

// ---------- State ----------------------------------------------------------
const state = {
  autoRotate: true,
  vfxOn: true,
  wireframe: false,
  modelOnly: false,
  inspectMode: false,
  combatView: true,
  speed: 1,
  zoom: 4.8,
  isolatedPart: null,
};

// ---------- UI bindings ----------------------------------------------------
function bindUI() {
  document.querySelectorAll('#anim-buttons button').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#anim-buttons button').forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      const anim = btn.dataset.anim;
      // Ensure combat view for animations
      if (state.inspectMode && anim !== 'idle') {
        setInspectMode(false);
      }
      director.play(anim);
      nowName.textContent = ANIM_META[anim]?.label || anim;
    });
  });

  const btnRotate = document.getElementById('btn-rotate');
  const btnVfx = document.getElementById('btn-vfx');
  const btnWire = document.getElementById('btn-wire');
  const btnModel = document.getElementById('btn-model');
  const btnInspect = document.getElementById('btn-inspect');
  const btnCombat = document.getElementById('btn-combat');
  const btnReset = document.getElementById('btn-reset');

  btnRotate?.addEventListener('click', () => {
    state.autoRotate = !state.autoRotate;
    btnRotate.classList.toggle('active', state.autoRotate);
  });
  btnVfx?.addEventListener('click', () => {
    state.vfxOn = !state.vfxOn;
    btnVfx.classList.toggle('active', state.vfxOn);
    vfx.setEnabled(state.vfxOn);
    // hide dimensional fragments when VFX off still keep model cracks
    const frags = weapon.userData.parts?.Dimensional_Fragments;
    if (frags) frags.visible = state.vfxOn;
    const frags2 = inspectWeapon.userData.parts?.Dimensional_Fragments;
    if (frags2) frags2.visible = state.vfxOn;
  });
  btnWire?.addEventListener('click', () => {
    state.wireframe = !state.wireframe;
    btnWire.classList.toggle('active', state.wireframe);
    setWireframe(state.wireframe);
  });
  btnModel?.addEventListener('click', () => {
    state.modelOnly = !state.modelOnly;
    btnModel.classList.toggle('active', state.modelOnly);
    env.visible = !state.modelOnly;
    mannequin.visible = !state.modelOnly && !state.inspectMode;
    if (state.modelOnly) {
      setInspectMode(true);
    }
  });
  btnInspect?.addEventListener('click', () => {
    setInspectMode(true);
    btnInspect?.classList.add('active');
    btnCombat?.classList.remove('active');
  });
  btnCombat?.addEventListener('click', () => {
    setInspectMode(false);
    btnCombat?.classList.add('active');
    btnInspect?.classList.remove('active');
  });
  btnReset?.addEventListener('click', () => {
    vfx.clearAll();
    director.reset();
    nowName.textContent = 'Idle';
    document.querySelectorAll('#anim-buttons button').forEach((b) => b.classList.remove('active'));
    document.querySelector('#anim-buttons button[data-anim="idle"]')?.classList.add('active');
  });

  const zoom = document.getElementById('zoom');
  const speed = document.getElementById('speed');
  zoom?.addEventListener('input', () => {
    state.zoom = parseFloat(zoom.value);
    const dir = camera.position.clone().sub(controls.target).normalize();
    camera.position.copy(controls.target).addScaledVector(dir, state.zoom);
  });
  speed?.addEventListener('input', () => {
    state.speed = parseFloat(speed.value);
    director.setSpeed(state.speed);
  });

  // Part isolation
  document.querySelectorAll('#parts-list li').forEach((li) => {
    li.addEventListener('click', () => {
      const part = li.dataset.part;
      if (state.isolatedPart === part) {
        state.isolatedPart = null;
        setPartIsolation(null);
        document.querySelectorAll('#parts-list li').forEach((l) => {
          l.classList.remove('hi', 'dim');
        });
      } else {
        state.isolatedPart = part;
        setPartIsolation(part);
        document.querySelectorAll('#parts-list li').forEach((l) => {
          l.classList.toggle('hi', l.dataset.part === part);
          l.classList.toggle('dim', l.dataset.part !== part);
        });
      }
    });
  });
}

function setInspectMode(on) {
  state.inspectMode = on;
  inspectRoot.visible = on;
  mannequin.visible = !on && !state.modelOnly;
  stage.visible = !on;
  if (on) {
    controls.target.set(0, 1.1, 0);
    camera.position.set(2.2, 1.6, 3.2);
    director.play('idle');
  } else {
    controls.target.set(0, 1.0, 0);
    camera.position.set(2.8, 1.8, 4.2);
  }
  controls.update();
}

function setWireframe(on) {
  const apply = (root) => {
    root.traverse((o) => {
      if (o.isMesh && o.material) {
        const matsArr = Array.isArray(o.material) ? o.material : [o.material];
        matsArr.forEach((m) => {
          if ('wireframe' in m) m.wireframe = on;
        });
      }
    });
  };
  apply(weapon);
  apply(inspectWeapon);
}

function setPartIsolation(partName) {
  const apply = (w) => {
    const parts = w.userData.parts;
    if (!parts) return;
    Object.entries(parts).forEach(([name, obj]) => {
      if (!partName) {
        obj.visible = true;
        return;
      }
      // map HTML data-part to internal names
      const map = {
        Blade: 'Blade',
        Blade_Edge: 'Blade_Edge',
        Void_Cracks: 'Void_Cracks',
        Guard: 'Guard',
        Handle: 'Handle',
        Pommel: 'Pommel',
        Ender_Core: 'Ender_Core',
        Runes: 'Runes',
        EmissiveParts: 'EmissiveParts',
        Dimensional_Fragments: 'Dimensional_Fragments',
        VFXAttachments: 'VFXAttachments',
      };
      const key = map[partName] || partName;
      obj.visible = name === key;
    });
  };
  apply(weapon);
  apply(inspectWeapon);
}

// ---------- Resize ---------------------------------------------------------
function onResize() {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}
window.addEventListener('resize', onResize);

// ---------- Loop -----------------------------------------------------------
const clock = new THREE.Clock();
let worldT = 0;

function frame() {
  requestAnimationFrame(frame);
  const dt = Math.min(0.05, clock.getDelta());
  worldT += dt * state.speed;

  if (state.autoRotate) {
    stage.rotation.y += dt * 0.15;
    if (state.inspectMode) {
      inspectRoot.rotation.y += dt * 0.25;
    }
  }

  controls.update();
  env.userData.animate?.(worldT);

  // Animation director (combat weapon)
  const info = director.update(dt, worldT);
  if (nowName && info.label) {
    // keep label in sync during auto-transitions
    if (!document.querySelector('#anim-buttons button.active') || info.name === 'stance') {
      // no-op sticky
    }
  }
  if (progressEl) {
    progressEl.style.width = isFinite(ANIM_META[info.name]?.duration)
      ? `${(info.progress * 100).toFixed(1)}%`
      : `${((worldT % 3) / 3 * 100).toFixed(1)}%`;
  }

  // Materials + weapon idle life
  const intensity = info.crackBoost || 1;
  updateMaterials(mats, worldT, intensity);
  setCrackIntensity(mats, intensity);
  updateWeaponIdle(weapon, worldT, state.vfxOn ? intensity : 0.5);
  updateWeaponIdle(inspectWeapon, worldT, state.vfxOn ? 1 : 0.5);

  // Inspect weapon gentle float
  if (state.inspectMode) {
    inspectWeapon.position.y = 1.1 + Math.sin(worldT * 1.2) * 0.04;
    inspectWeapon.rotation.y = Math.sin(worldT * 0.4) * 0.15;
  }

  vfx.update(dt, worldT);

  renderer.render(scene, camera);
}

// ---------- Boot -----------------------------------------------------------
function boot() {
  bindUI();
  director.play('idle');
  nowName.textContent = 'Idle';

  // Hide loader
  requestAnimationFrame(() => {
    setTimeout(() => loaderEl?.classList.add('hide'), 400);
  });

  frame();
}

boot();
