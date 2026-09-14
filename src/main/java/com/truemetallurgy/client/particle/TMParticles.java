package com.truemetallurgy.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Workshop particles. Short-lived, capped counts, no entities - all server
 * spawns additionally pass through the global particle budget.
 */
public final class TMParticles {
    private TMParticles() {}

    /** Bright hammer spark with gravity. */
    public static class Spark extends TextureSheetParticle {
        protected Spark(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 12 + random.nextInt(18);
            this.gravity = 0.9F;
            this.friction = 0.94F;
            this.quadSize = 0.06F + random.nextFloat() * 0.05F;
            this.setColor(1.0F, 0.62F + random.nextFloat() * 0.2F, 0.15F);
        }

        @Override
        public void tick() {
            super.tick();
            float f = 1.0F - (float) age / (float) lifetime;
            this.setColor(1.0F, 0.6F * f + 0.1F, 0.12F * f);
        }

        @Override
        protected float getQuadSize(float scaleFactor) {
            float f = 1.0F - ((float) age / (float) lifetime);
            return this.quadSize * Math.max(0.0F, f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level,
                    double x, double y, double z, double vx, double vy, double vz) {
                Spark p = new Spark(level, x, y, z, vx, vy, vz);
                p.pickSprite(sprites);
                return p;
            }
        }
    }

    /** Rising forge ember with flicker. */
    public static class Ember extends TextureSheetParticle {
        protected Ember(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 30 + random.nextInt(40);
            this.gravity = -0.12F;
            this.friction = 0.96F;
            this.quadSize = 0.05F + random.nextFloat() * 0.05F;
            this.setColor(1.0F, 0.45F, 0.1F);
        }

        @Override
        public void tick() {
            super.tick();
            float f = (float) age / (float) lifetime;
            float flicker = 0.85F + 0.15F * (float) Math.sin(age * 1.7);
            this.setColor(1.0F * flicker, (0.45F - 0.3F * f) * flicker, 0.1F * (1.0F - f));
        }

        @Override
        protected float getQuadSize(float scaleFactor) {
            float f = 1.0F - ((float) age / (float) lifetime);
            return this.quadSize * Math.max(0.2F, f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level,
                    double x, double y, double z, double vx, double vy, double vz) {
                Ember p = new Ember(level, x, y, z, vx, vy, vz);
                p.pickSprite(sprites);
                return p;
            }
        }
    }

    /** Growing quench steam cloud. */
    public static class Steam extends TextureSheetParticle {
        protected Steam(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 40 + random.nextInt(40);
            this.gravity = -0.04F;
            this.friction = 0.94F;
            this.quadSize = 0.35F + random.nextFloat() * 0.25F;
            float shade = 0.82F + random.nextFloat() * 0.12F;
            this.setColor(shade, shade, shade + 0.03F);
            this.setAlpha(0.55F);
        }

        @Override
        public void tick() {
            super.tick();
            float f = (float) age / (float) lifetime;
            this.setAlpha(0.55F * (1.0F - f));
        }

        @Override
        protected float getQuadSize(float scaleFactor) {
            float f = (float) age / (float) lifetime;
            return this.quadSize * (1.0F + f * 1.6F);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level,
                    double x, double y, double z, double vx, double vy, double vz) {
                Steam p = new Steam(level, x, y, z, vx, vy, vz);
                p.pickSprite(sprites);
                return p;
            }
        }
    }

    /** Falling grinding dust and crack fragments. */
    public static class MetalDust extends TextureSheetParticle {
        protected MetalDust(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 18 + random.nextInt(16);
            this.gravity = 0.45F;
            this.friction = 0.92F;
            this.quadSize = 0.07F + random.nextFloat() * 0.05F;
            float shade = 0.45F + random.nextFloat() * 0.2F;
            this.setColor(shade, shade, shade + 0.05F);
        }

        @Override
        protected float getQuadSize(float scaleFactor) {
            float f = 1.0F - ((float) age / (float) lifetime);
            return this.quadSize * Math.max(0.0F, f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level,
                    double x, double y, double z, double vx, double vy, double vz) {
                MetalDust p = new MetalDust(level, x, y, z, vx, vy, vz);
                p.pickSprite(sprites);
                return p;
            }
        }
    }
}
