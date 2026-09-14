package com.enderblade.client.particle;

import com.enderblade.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@OnlyIn(Dist.CLIENT)
public final class ModParticleProviders {

    private ModParticleProviders() {
    }

    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.VOID_SPARK.get(), VoidSpark::provider);
        event.registerSpriteSet(ModParticles.RIFT_DUST.get(), RiftDust::provider);
        event.registerSpriteSet(ModParticles.AFTERIMAGE_MIST.get(), AfterimageMist::provider);
        event.registerSpriteSet(ModParticles.MARK_RUNE.get(), MarkRune::provider);
    }

    private static class VoidSpark extends TextureSheetParticle {
        protected VoidSpark(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 12 + level.random.nextInt(8);
            this.quadSize = 0.08f + level.random.nextFloat() * 0.06f;
            this.rCol = 0.7f;
            this.gCol = 0.35f;
            this.bCol = 1.0f;
            this.alpha = 0.9f;
            this.xd = vx + (level.random.nextDouble() - 0.5) * 0.02;
            this.yd = vy + 0.01;
            this.zd = vz + (level.random.nextDouble() - 0.5) * 0.02;
            this.gravity = -0.01f;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            alpha = 1f - (float) age / lifetime;
        }

        @Override
        public net.minecraft.client.particle.ParticleRenderType getRenderType() {
            return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
            return (type, level, x, y, z, vx, vy, vz) -> new VoidSpark(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    private static class RiftDust extends TextureSheetParticle {
        protected RiftDust(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 18 + level.random.nextInt(10);
            this.quadSize = 0.12f + level.random.nextFloat() * 0.1f;
            this.rCol = 0.2f;
            this.gCol = 0.05f;
            this.bCol = 0.3f;
            this.alpha = 0.7f;
            this.gravity = 0.01f;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            alpha *= 0.96f;
        }

        @Override
        public net.minecraft.client.particle.ParticleRenderType getRenderType() {
            return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
            return (type, level, x, y, z, vx, vy, vz) -> new RiftDust(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    private static class AfterimageMist extends TextureSheetParticle {
        protected AfterimageMist(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 10 + level.random.nextInt(6);
            this.quadSize = 0.25f;
            this.rCol = 0.55f;
            this.gCol = 0.25f;
            this.bCol = 0.85f;
            this.alpha = 0.5f;
            this.gravity = 0f;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            this.quadSize *= 1.03f;
            alpha = 0.5f * (1f - (float) age / lifetime);
        }

        @Override
        public net.minecraft.client.particle.ParticleRenderType getRenderType() {
            return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
            return (type, level, x, y, z, vx, vy, vz) -> new AfterimageMist(level, x, y, z, vx, vy, vz, sprites);
        }
    }

    private static class MarkRune extends TextureSheetParticle {
        protected MarkRune(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
            super(level, x, y, z, vx, vy, vz);
            this.lifetime = 20;
            this.quadSize = 0.2f;
            this.rCol = 0.8f;
            this.gCol = 0.4f;
            this.bCol = 1f;
            this.alpha = 0.8f;
            this.gravity = 0f;
            pickSprite(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            this.oRoll = this.roll;
            this.roll += 0.15f;
            alpha = 0.8f * (1f - (float) age / lifetime);
        }

        @Override
        public net.minecraft.client.particle.ParticleRenderType getRenderType() {
            return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
            return (type, level, x, y, z, vx, vy, vz) -> new MarkRune(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
