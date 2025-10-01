package org.dandelion.server.particles

import org.dandelion.server.types.Color
import java.io.File
import java.util.Properties

object ParticleParser {

    fun parseParticleFile(file: File): Particle? {
        return try {
            val properties = Properties()
            properties.load(file.inputStream())
            parseParticle(properties)
        } catch (e: Exception) {
            println("Error parsing particle file ${file.name}: ${e.message}")
            null
        }
    }

    private fun parseParticle(props: Properties): Particle {
        val u1 = props.getProperty("pixelU1", "0").toInt().toByte()
        val v1 = props.getProperty("pixelV1", "0").toInt().toByte()
        val u2 = props.getProperty("pixelU2", "10").toInt().toByte()
        val v2 = props.getProperty("pixelV2", "10").toInt().toByte()

        val tintRed = props.getProperty("tintRed", "255").toInt().toByte()
        val tintGreen = props.getProperty("tintGreen", "255").toInt().toByte()
        val tintBlue = props.getProperty("tintBlue", "255").toInt().toByte()
        val tint = Color(tintRed, tintGreen, tintBlue)

        val frameCount = props.getProperty("frameCount", "1").toInt().toByte()
        val particleCount = props.getProperty("particleCount", "1").toInt().toByte()
        val pixelSize = props.getProperty("pixelSize", "8").toFloat()
        val size = (pixelSize * 2).toInt().toByte()

        val sizeVariation = props.getProperty("sizeVariation", "0").toFloat()
        val spread = (props.getProperty("spread", "0").toFloat() * 32f).toInt().toUShort()
        val speed = (props.getProperty("speed", "0").toFloat() * 32f).toInt()
        val gravity = props.getProperty("gravity", "0").toFloat()
        val baseLifetime = props.getProperty("baseLifetime", "1").toFloat()
        val lifetimeVariation = props.getProperty("lifetimeVariation", "0").toFloat()

        val expireUponTouchingGround = props.getProperty("expireUponTouchingGround", "false").toBoolean()
        val collidesSolid = props.getProperty("collidesSolid", "true").toBoolean()
        val collidesLiquid = props.getProperty("collidesLiquid", "false").toBoolean()
        val collidesLeaves = props.getProperty("collidesLeaves", "false").toBoolean()

        val expirationPolicy = if (expireUponTouchingGround)
            ExpirationPolicy.EXPIRE_ON_ANY_COLLISION
        else
            ExpirationPolicy.EXPIRE_ON_WALL_CEILING_ONLY

        val collisionFlags = ParticleCollisionFlags(
            expirationPolicy = expirationPolicy,
            collideSolidIce = collidesSolid,
            collideWaterLavaRope = collidesLiquid,
            collideLeafDraw = collidesLeaves
        )

        val fullBright = props.getProperty("fullBright", "false").toBoolean()

        return Particle(
            u1 = u1,
            v1 = v1,
            u2 = u2,
            v2 = v2,
            tint = tint,
            frameCount = frameCount,
            particleCount = particleCount,
            size = size,
            sizeVariation = sizeVariation,
            spread = spread,
            speed = speed,
            gravity = gravity,
            baseLifetime = baseLifetime,
            lifetimeVariation = lifetimeVariation,
            collisionFlags = collisionFlags,
            fullBright = fullBright,
            positionX = 0,
            positionY = 0,
            positionZ = 0,
            originX = 0,
            originY = 0,
            originZ = 0
        )
    }
}