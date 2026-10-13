package legame.actiongame3d.entities

import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import legame.actiongame3d.weapons.Weapon
import legame.actiongame3d.weapons.WeaponType

class Player(val camera: PerspectiveCamera) {
    val position = Vector3(0f, 1.7f, 0f)
    private val velocity = Vector3()
    private val forward = Vector3()
    private val right = Vector3()

    var health = 100f
    var maxHealth = 100f
    var isAlive = true
    var kills = 0
    var isSprinting = false
    private var isOnGround = true
    private var jumpVelocity = 0f

    private val walkSpeed = 6.5f
    private val sprintSpeed = 10.5f
    private val jumpForce = 8.5f
    private val gravity = -22f
    private var yaw = 0f
    private var pitch = 0f
    private val touchLookSensitivity = 0.12f

    val weapons = mutableListOf(
        Weapon(WeaponType.PISTOL),
        Weapon(WeaponType.RIFLE),
        Weapon(WeaponType.SHOTGUN)
    )
    var currentWeaponIndex = 0
    val currentWeapon: Weapon get() = weapons[currentWeaponIndex]

    init { camera.position.set(position) }

    fun updateMobile(
        delta: Float, worldBounds: Float,
        moveX: Float, moveY: Float,
        lookDX: Float, lookDY: Float,
        sprint: Boolean, jump: Boolean, reload: Boolean
    ) {
        if (!isAlive) return
        yaw -= lookDX * touchLookSensitivity
        pitch += lookDY * touchLookSensitivity
        pitch = MathUtils.clamp(pitch, -89f, 89f)
        applyLook()

        isSprinting = sprint
        val speed = if (isSprinting) sprintSpeed else walkSpeed
        forward.set(camera.direction).also { it.y = 0f; it.nor() }
        right.set(forward).crs(Vector3.Y).nor()
        velocity.setZero()
        velocity.add(forward.x * moveY + right.x * moveX, 0f, forward.z * moveY + right.z * moveX)
        if (velocity.len2() > 0.0001f) {
            velocity.nor().scl(speed * delta)
            position.add(velocity)
        }
        position.x = MathUtils.clamp(position.x, -worldBounds, worldBounds)
        position.z = MathUtils.clamp(position.z, -worldBounds, worldBounds)

        if (jump && isOnGround) {
            isOnGround = false
            jumpVelocity = jumpForce
        }
        if (reload) currentWeapon.reload()

        if (!isOnGround) {
            jumpVelocity += gravity * delta
            position.y += jumpVelocity * delta
            if (position.y <= 1.7f) {
                position.y = 1.7f
                jumpVelocity = 0f
                isOnGround = true
            }
        } else {
            position.y = 1.7f
        }
        camera.position.set(position)
        camera.update()
    }

    private fun applyLook() {
        camera.direction.set(0f, 0f, -1f)
        camera.up.set(0f, 1f, 0f)
        camera.rotate(Vector3.Y, yaw)
        camera.rotate(camera.direction.cpy().crs(Vector3.Y).nor(), pitch)
    }

    fun takeDamage(amount: Float) {
        if (!isAlive) return
        health -= amount
        if (health <= 0f) { health = 0f; isAlive = false }
    }

    fun reset() {
        position.set(0f, 1.7f, 0f)
        health = maxHealth
        isAlive = true
        kills = 0
        jumpVelocity = 0f
        isOnGround = true
        yaw = 0f
        pitch = 0f
        weapons.forEach { it.reset() }
        currentWeaponIndex = 0
        camera.position.set(position)
        camera.direction.set(0f, 0f, -1f)
        camera.up.set(0f, 1f, 0f)
        camera.update()
    }
}
