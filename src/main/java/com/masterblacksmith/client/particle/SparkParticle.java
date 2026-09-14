package com.masterblacksmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Fast white-yellow hammer spark with gravity. */
public class SparkParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    protected SparkParticle(ClientLevel level, double x, double y, double z,
                            SpriteSet sprites, double vx, double vy, double vz) {
        super(level, x, y, z, vx * 2.2, vy * 2.2 + 0.6, vz * 2.2);
        this.spriteSet = sprites;
        this.friction = 0.96F;
        this.gravity = 1.4F;
        this.lifetime = 10 + random.nextInt(14);
        this.quadSize = 0.04F + random.nextFloat() * 0.04F;
        this.rCol = 1.0F;
        this.gCol = 0.9F;
        this.bCol = 0.55F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float partial) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SparkParticle(level, x, y, z, sprites, vx, vy, vz);
        }
    }
}
