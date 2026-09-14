package com.masterblacksmith.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Billowing quench steam (and grinding dust via a second texture). */
public class SteamParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    protected SteamParticle(ClientLevel level, double x, double y, double z,
                            SpriteSet sprites, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy + 0.35, vz);
        this.spriteSet = sprites;
        this.friction = 0.94F;
        this.gravity = -0.06F;
        this.lifetime = 30 + random.nextInt(30);
        this.quadSize = 0.35F + random.nextFloat() * 0.35F;
        float shade = 0.82F + random.nextFloat() * 0.14F;
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.alpha = 0.75F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SteamParticle(level, x, y, z, sprites, vx, vy, vz);
        }
    }
}
