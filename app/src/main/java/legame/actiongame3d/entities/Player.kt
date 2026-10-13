package legame.actiongame3d.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
    var isCrouching = false
    private var isOnGround = true
    private var jumpVelocity = 0f

    private val walkSpeed = 6.5f
    private val sprintSpeed = 10.5f
    private val crouchSpeed = 3.2f
    private val jumpForce = 8.5f
    private val gravity = -22f

    private var yaw = 0f
    private var pitch = 0f
    private val mouseSensitivity = 0.15f

    val weapons = mutableListOf(
        Weapon(WeaponType.PISTOL),
        Weapon(WeaponType.RIFLE),
        Weapon(WeaponType.SHOTGUN)
    )
    var currentWeaponIndex = 0
    val currentWeapon: Weapon get() = weapons[currentWeaponIndex]

    init {
        camera.position.set(position)
    }

    fun update(delta: Float, worldBounds: Float = 40f) {
        if (!isAlive) return
        handleMouseLook()
        handleMovement(delta, worldBounds)
        handleWeaponInput()

        if (!isOnGround) {
            jumpVelocity += gravity * delta
            position.y += jumpVelocity * delta
            val floor = if (isCrouching) 1.0f else 1.7f
            if (position.y <= floor) {
                position.y = floor
                jumpVelocity = 0f
                isOnGround = true
            }
        }
        camera.position.set(position)
        camera.update()
    }

    private fun handleMouseLook() {
        if (!Gdx.input.isCursorCatched) return
        yaw -= Gdx.input.deltaX * mouseSensitivity
        pitch -= Gdx.input.deltaY * mouseSensitivity
        pitch = MathUtils.clamp(pitch, -89f, 89f)
        camera.direction.set(0f, 0f, -1f)
        camera.up.set(0f, 1f, 0f)
        camera.rotate(Vector3.Y, yaw)
        camera.rotate(camera.direction.cpy().crs(Vector3.Y).nor(), pitch)
    }

    private fun handleMovement(delta: Float, worldBounds: Float) {
        isSprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !isCrouching
        isCrouching = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
        val targetH = if (isCrouching) 1.0f else 1.7f
        if (isOnGround) position.y = MathUtils.lerp(position.y, targetH, 10f * delta)

        val speed = when {
            isCrouching -> crouchSpeed
            isSprinting -> sprintSpeed
            else -> walkSpeed
        }
        forward.set(camera.direction).also { it.y = 0f; it.nor() }
        right.set(forward).crs(Vector3.Y).nor()
        velocity.setZero()
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) velocity.add(forward)
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) velocity.sub(forward)
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) velocity.sub(right)
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velocity.add(right)
        if (velocity.len2() > 0) {
            velocity.nor().scl(speed * delta)
            position.add(velocity)
        }
        position.x = MathUtils.clamp(position.x, -worldBounds, worldBounds)
        position.z = MathUtils.clamp(position.z, -worldBounds, worldBounds)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            isOnGround = false
            jumpVelocity = jumpForce
        }
    }

    private fun handleWeaponInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) currentWeaponIndex = 0
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && weapons.size > 1) currentWeaponIndex = 1
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && weapons.size > 2) currentWeaponIndex = 2
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) currentWeapon.reload()
    }

    fun takeDamage(amount: Float) {
        if (!isAlive) return
        health -= amount
        if (health <= 0f) {
            health = 0f
            isAlive = false
        }
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
