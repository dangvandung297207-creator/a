package com.masterblacksmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Rising forge ember, full-bright. */
public class EmberParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    protected EmberParticle(ClientLevel level, double x, double y, double z,
                            SpriteSet sprites, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = sprites;
        this.friction = 0.92F;
        this.gravity = -0.03F;
        this.lifetime = 24 + random.nextInt(24);
        this.quadSize = 0.05F + random.nextFloat() * 0.06F;
        this.rCol = 1.0F;
        this.gCol = 0.55F + random.nextFloat() * 0.2F;
        this.bCol = 0.15F;
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
            return new EmberParticle(level, x, y, z, sprites, vx, vy, vz);
        }
    }
}
