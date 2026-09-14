True Metallurgy - audio assets (PLACEHOLDER STATE)
==================================================
The 18 sound events are fully wired (see ../sounds.json and
com.truemetallurgy.registry.SoundRegistry), but no .ogg audio files are
shipped yet: this environment has no audio toolchain, so binary Vorbis
assets cannot be encoded here.

Missing files are harmless: the game logs a warning on resource reload
and the related action simply plays no sound. No crashes, no broken
behaviour.

Contributing real audio: add 44.1 kHz mono/stereo OGG Vorbis files named
  <event>1.ogg  <event>2.ogg
for each event in sounds.json (e.g. hammer_iron1.ogg, quench_oil2.ogg),
keeping each clip under ~2 s except grind_loop (~4 s, seamless loop).
Record foley (anvil strikes, bellows, water quench) or synthesize with:
  ffmpeg -f lavfi -i "sine=frequency=820:duration=0.35" ... out.ogg
See docs/AUDIO.md (when added) for the full per-event spec.
