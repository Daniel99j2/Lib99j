package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.impl.ServerParticleManager;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * A server-side visual effect with position, velocity, collision, and additional render properties.
 *
 * <p>
 * Note: This was mostly copied from {@link Particle}, so most code can be copied over.
 */
public abstract class ServerParticle {
    private static final Box EMPTY_BOUNDING_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final double MAX_SQUARED_COLLISION_CHECK_DISTANCE = MathHelper.square(100.0);
    protected final ServerWorld world;
    protected final Random random = Random.create();
    protected final TextDisplayElement display;
    private final ElementHolder elementHolder;
    protected double x;
    protected double y;
    protected double z;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    protected boolean onGround;
    protected boolean collidesWithWorld = true;
    protected boolean dead;
    protected float spacingXZ = 0.6F;
    protected float spacingY = 1.8F;
    protected int age;
    protected int maxAge;
    protected float gravityStrength;
    protected float red = 1.0F;
    protected float green = 1.0F;
    protected float blue = 1.0F;
    protected float alpha = 1.0F;
    protected float angle;
    protected float velocityMultiplier = 0.98F;
    private Box boundingBox = EMPTY_BOUNDING_BOX;
    private boolean stopped;

    /**
     * Children of ServerParticle should have a sprites value
     * <p>{@code private static final ArrayList<ServerParticleManager.ParticleFrame> sprites}
     */

    public ServerParticle(ServerWorld world, double x, double y, double z) {
        this.world = world;
        this.elementHolder = new ElementHolder();
        this.setBoundingBoxSpacing(0.2F, 0.2F);
        this.setPos(x, y, z);
        this.maxAge = (int) (4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
        this.display = new TextDisplayElement() {
        };
        this.display.setSeeThrough(false);
        this.display.setBackground(26);
        this.display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.display.setText(getSprites().get(0).text());
        this.display.setInterpolationDuration(1);
        this.display.setTeleportDuration(1);
        this.display.setInvisible(true);
//        this.display.setTranslation(new Vector3f(((float) getSprites().get(0).width /2/16), ((float) getSprites().get(0).height /2/16), 0));
        this.elementHolder.addElement(this.display);
        ServerParticleManager.addParticle(this);
        ChunkAttachment.ofTicking(this.elementHolder, this.world, this.getPos());
    }

    protected ServerParticle(ServerWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        this(world, x, y, z);
        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * 0.4F;
        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * 0.4F;
        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * 0.4F;
        double d = (Math.random() + Math.random() + 1.0) * 0.15F;
        double e = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
        this.velocityX = this.velocityX / e * d * 0.4F;
        this.velocityY = this.velocityY / e * d * 0.4F + 0.1F;
        this.velocityZ = this.velocityZ / e * d * 0.4F;
    }

    /**
     * This is in place to prevent all ServerParticles from having the same sprite.
     */
    public abstract ArrayList<ServerParticleManager.ParticleFrame> getSprites();

    public Vec3d getPos() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * Called each game tick (20 times per second), and should be used to do core particle logic, such as movement and collision.
     */
    public void tick() {
        try {
            if (this.age++ >= this.maxAge) {
                this.markDead();
            } else {
                this.velocityY = this.velocityY - 0.04 * (double) this.gravityStrength;
                this.move(this.velocityX, this.velocityY, this.velocityZ);

                this.velocityX = this.velocityX * (double) this.velocityMultiplier;
                this.velocityY = this.velocityY * (double) this.velocityMultiplier;
                this.velocityZ = this.velocityZ * (double) this.velocityMultiplier;
                if (this.onGround) {
                    this.velocityX *= 0.7F;
                    this.velocityZ *= 0.7F;
                }
            }

            if (this.dead) this.elementHolder.destroy();
            else {
                customTick();
                this.display.setOverridePos(new Vec3d(this.x, this.y, this.z));
                this.display.setText(getSprites().get(getCurrentFrame()).text().withColor(ColorHelper.fromFloats(this.alpha, this.red, this.green, this.blue)));
                this.display.startInterpolation();
                this.elementHolder.tick();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.elementHolder.destroy();
            this.markDead();
        }
    }

    protected void customTick() {

    }

    public int getCurrentFrame() {
        return Math.min((int) ((float) this.age / this.getMaxAge() * getSprites().size()), getSprites().size() - 1);
    }

    public void readNbt(NbtCompound data) {
        this.alpha = data.getFloat("alpha", this.alpha);
        this.red = data.getFloat("red", this.red);
        this.green = data.getFloat("green", this.green);
        this.blue = data.getFloat("blue", this.blue);
        this.velocityX = data.getDouble("velocityX", this.velocityX);
        this.velocityY = data.getDouble("velocityY", this.velocityY);
        this.velocityZ = data.getDouble("velocityZ", this.velocityZ);
        this.display.setScale(new Vector3f(data.getFloat("scaleX", this.display.getScale().x()), data.getFloat("scaleY", this.display.getScale().y()), data.getFloat("scaleZ", this.display.getScale().z())));
    }

    /**
     * Multiplies this particle's current velocity by the target {@code speed} amount.
     *
     * @param speed the velocity multiplier to apply to this particle
     */
    public ServerParticle move(float speed) {
        this.velocityX *= speed;
        this.velocityY = (this.velocityY - 0.1F) * (double) speed + 0.1F;
        this.velocityZ *= speed;
        return this;
    }

    /**
     * Updates this particle's velocity to the target X, Y, and Z values.
     *
     * @param velocityX the new x-velocity of this particle
     * @param velocityY the new y-velocity of this particle
     * @param velocityZ the new z-velocity of this particle
     */
    public void setVelocity(double velocityX, double velocityY, double velocityZ) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    /**
     * Scales the size of this particle by the given {@code scale} amount.
     *
     * @param scale the amount to scale this particle's size by
     * @return this particle
     */
    public ServerParticle scale(float scale) {
        this.setBoundingBoxSpacing(0.2F * scale, 0.2F * scale);
        return this;
    }

    /**
     * Updates the rendering color of this particle.
     * Each value should be between 0.0 (no channel color) and 1.0 (full channel color).
     *
     * @param red   the target red color to use while rendering
     * @param green the target green color to use while rendering
     * @param blue  the target blue color to use while rendering
     */
    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Updates the alpha value of this particle to use while rendering.
     *
     * <p>
     * Note that a particle cannot render with transparency unless {@link Particle#getType()} is
     * {@link ParticleTextureSheet#PARTICLE_SHEET_TRANSLUCENT}, or another sheet that supports transparency.
     *
     * <p>
     * Also note that the default particle shader (core/particle.fsh) will discard all transparent pixels below 0.1 alpha.
     *
     * @param alpha the new alpha value of this particle
     */
    protected void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * {@return the maximum age, in ticks, of this particle}
     * If this particle's age exceeds this value, it will be removed from the world.
     */
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * Sets the maximum age, in ticks, that this particle can exist for.
     *
     * @param maxAge the new maximum age of this particle, in ticks
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String toString() {
        return this.getClass().getSimpleName()
                + ", Pos ("
                + this.x
                + ","
                + this.y
                + ","
                + this.z
                + "), RGBA ("
                + this.red
                + ","
                + this.green
                + ","
                + this.blue
                + ","
                + this.alpha
                + "), Age "
                + this.age;
    }

    /**
     * Marks this particle as ready to be removed from the containing {@link ServerWorld}.
     */
    public void markDead() {
        this.dead = true;
    }

    protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
        if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
            this.spacingXZ = spacingXZ;
            this.spacingY = spacingY;
            Box box = this.getBoundingBox();
            double d = (box.minX + box.maxX - (double) spacingXZ) / 2.0;
            double e = (box.minZ + box.maxZ - (double) spacingXZ) / 2.0;
            this.setBoundingBox(new Box(d, box.minY, e, d + (double) this.spacingXZ, box.minY + (double) this.spacingY, e + (double) this.spacingXZ));
        }
    }

    /**
     * Updates the position and bounding box of this particle to the target {@code x}, {@code y}, {@code z} position.
     *
     * @param y the y position to move this particle to
     * @param x the x position to move this particle to
     * @param z the z position to move this particle to
     */
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float f = this.spacingXZ / 2.0F;
        float g = this.spacingY;
        this.setBoundingBox(new Box(x - (double) f, y, z - (double) f, x + (double) f, y + (double) g, z + (double) f));
    }

    /**
     * Moves this particle by the specified delta amounts, re-positioning bounding boxes and adjusting movement for collision with the world.
     *
     * @param dx the delta x to move this particle by
     * @param dz the delta z to move this particle by
     * @param dy the delta y to move this particle by
     */
    public void move(double dx, double dy, double dz) {
        if (!this.stopped) {
            double d = dx;
            double e = dy;
            double f = dz;
            if (this.collidesWithWorld && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < MAX_SQUARED_COLLISION_CHECK_DISTANCE) {
                Vec3d vec3d = Entity.adjustMovementForCollisions(null, new Vec3d(dx, dy, dz), this.getBoundingBox(), this.world, List.of());
                dx = vec3d.x;
                dy = vec3d.y;
                dz = vec3d.z;
            }

            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
                this.repositionFromBoundingBox();
            }

            if (Math.abs(e) >= 1.0E-5F && Math.abs(dy) < 1.0E-5F) {
                this.stopped = true;
            }

            this.onGround = e != dy && e < 0.0;
            if (d != dx) {
                this.velocityX = 0.0;
            }

            if (f != dz) {
                this.velocityZ = 0.0;
            }
        }
    }

    protected void repositionFromBoundingBox() {
        Box box = this.getBoundingBox();
        this.x = (box.minX + box.maxX) / 2.0;
        this.y = box.minY;
        this.z = (box.minZ + box.maxZ) / 2.0;
    }

    /**
     * {@return the packed light level this particle should render at}
     *
     * @see net.minecraft.client.render.LightmapTextureManager
     */
    protected int getBrightness(float tint) {
        BlockPos blockPos = BlockPos.ofFloored(this.x, this.y, this.z);
        return this.world.isChunkLoaded(blockPos) ? WorldRenderer.getLightmapCoordinates(this.world, blockPos) : 0;
    }

    /**
     * {@return {@code false} if this particle is finished and should be removed from the parent {@link ParticleManager }, otherwise {@code true} if the particle is still alive}
     */
    public boolean isAlive() {
        return !this.dead;
    }

    /**
     * {@return the bounding {@link Box} of this particle used for collision and movement logic}
     *
     * <p>
     * By default, this bounding box is automatically repositioned when a particle moves in {@link Particle#tick()}.
     * To adjust the size of the returned box, visit {@link ServerParticle#setBoundingBoxSpacing(float, float)}.
     * To directly update the current bounding box, visit {@link ServerParticle#setBoundingBox(Box)};
     */
    public Box getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(Box boundingBox) {
        this.boundingBox = boundingBox;
    }
}
