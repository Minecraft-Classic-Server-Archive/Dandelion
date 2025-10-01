package org.dandelion.server.particles

import org.dandelion.server.entity.player.Player
import org.dandelion.server.entity.player.PlayerRegistry
import org.dandelion.server.network.packets.cpe.server.ServerDefineEffect
import org.dandelion.server.network.packets.cpe.server.ServerSpawnEffect
import org.dandelion.server.types.Position
import java.io.File

object ParticleRegistry {
    private val registry: Array<Particle?> = arrayOfNulls(256)
    private val particleNames: MutableMap<String, Byte> = mutableMapOf()

    @JvmStatic
    fun init() {
        val particlesDir = File("particles")
        if (!particlesDir.exists()) {
            particlesDir.mkdirs()
            println("Created particles directory")
        }

        val propertyFiles = particlesDir.listFiles { file ->
            file.isFile && file.extension.equals("properties", ignoreCase = true)
        }

        if (propertyFiles != null) {
            for (file in propertyFiles) {
                val particle = ParticleParser.parseParticleFile(file)
                if (particle != null) {
                    val success = addParticle(particle)
                    if (success) {
                        val particleName = file.nameWithoutExtension
                        particleNames[particleName] = particle.effectId
                        println("Loaded particle: $particleName (ID: ${particle.effectId})")
                    } else {
                        println("Failed to register particle from file: ${file.name}")
                    }
                }
            }
        }

        println("Loaded ${particleNames.size} particles")
    }

    @JvmStatic
    fun getParticleByName(name: String): Particle? {
        val effectId = particleNames[name] ?: return null
        return getParticle(effectId)
    }

    @JvmStatic
    fun addParticle(particle: Particle): Boolean {
        val definedCount = registry.count { it != null }
        if (definedCount >= 255) return false

        val freeIndex = registry.indexOfFirst { it == null }
        if (freeIndex == -1) return false

        particle.effectId = freeIndex.toByte()
        registry[freeIndex] = particle

        defineParticle(particle)

        return true
    }

    @JvmStatic
    fun removeParticle(effectId: Byte): Boolean {
        val index = effectId.toInt() and 0xFF

        val present = registry[index] != null
        registry[index] = null

        return present
    }

    @JvmStatic
    fun getParticle(effectId: Byte): Particle? = registry[effectId.toInt() and 0xFF]

    private fun defineParticle(particle: Particle) {
        val red = (particle.tint.red.toInt() and 0xFF).toByte()
        val green = (particle.tint.green.toInt() and 0xFF).toByte()
        val blue = (particle.tint.blue.toInt() and 0xFF).toByte()

        val sizeVariation = (particle.sizeVariation * 100f).toDouble().toLong().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val gravity = (particle.gravity * 10000f).toDouble().toLong().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val baseLifetime = (particle.baseLifetime * 10000f).toDouble().toLong().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val lifetimeVariation = (particle.lifetimeVariation * 100f).toDouble().toLong().coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

        val collideFlags = particle.collisionFlags.toByte()
        val fullBright: Byte = if (particle.fullBright) 1 else 0

        val packet = ServerDefineEffect(
            effectId = particle.effectId,
            u1 = particle.u1,
            v1 = particle.v1,
            u2 = particle.u2,
            v2 = particle.v2,
            redTint = red,
            greenTint = green,
            blueTint = blue,
            frameCount = particle.frameCount,
            particleCount = particle.particleCount,
            particleSize = particle.size,
            sizeVariation = sizeVariation,
            spread = particle.spread,
            speed = particle.speed,
            gravity = gravity,
            baseLifetime = baseLifetime,
            lifetimeVariation = lifetimeVariation,
            collideFlags = collideFlags,
            fullBright = fullBright,
        )

        PlayerRegistry.getAll().forEach { packet.send(it) }
    }

    @JvmStatic
    fun spawnParticleFor(player: Player, particle: Particle, location: Position, origin: Position) {
        val px = (location.x * 32f).toInt()
        val py = (location.y * 32f).toInt()
        val pz = (location.z * 32f).toInt()
        val ox = (origin.x * 32f).toInt()
        val oy = (origin.y * 32f).toInt()
        val oz = (origin.z * 32f).toInt()

        val packet = ServerSpawnEffect(
            effectId = particle.effectId,
            positionX = px,
            positionY = py,
            positionZ = pz,
            originX = ox,
            originY = oy,
            originZ = oz,
        )

        packet.send(player)
    }

    @JvmStatic
    fun spawnParticleFor(player: Player, effectId: Byte, location: Position, origin: Position) {
        val particle = getParticle(effectId) ?: return
        spawnParticleFor(player, particle, location, origin)
    }

    @JvmStatic
    fun spawnParticleByName(player: Player, name: String, location: Position, origin: Position) {
        val particle = getParticleByName(name) ?: return
        spawnParticleFor(player, particle, location, origin)
    }
}