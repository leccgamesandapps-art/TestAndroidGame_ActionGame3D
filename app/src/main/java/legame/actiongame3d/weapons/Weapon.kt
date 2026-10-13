package legame.actiongame3d.weapons

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Timer

enum class WeaponType(
    val displayName: String,
    val damage: Float,
    val fireRate: Float,
    val magSize: Int,
    val reloadTime: Float,
    val range: Float,
    val spread: Float,
    val isAutomatic: Boolean
) {
    PISTOL("Pistol", 25f, 3.5f, 12, 1.4f, 80f, 1.5f, false),
    RIFLE("Assault Rifle", 32f, 9.0f, 30, 2.1f, 120f, 2.8f, true),
    SHOTGUN("Shotgun", 18f, 1.2f, 6, 2.6f, 35f, 8.0f, false)
}

class Weapon(val type: WeaponType) {
    var ammoInMag = type.magSize
    var reserveAmmo = type.magSize * 4
    var isReloading = false
    private var lastShotTime = 0f

    val canShoot: Boolean
        get() = !isReloading && ammoInMag > 0 &&
                (System.currentTimeMillis() / 1000f - lastShotTime) >= (1f / type.fireRate)

    fun shoot(): Boolean {
        if (!canShoot) return false
        ammoInMag--
        lastShotTime = System.currentTimeMillis() / 1000f
        return true
    }

    fun reload() {
        if (isReloading || ammoInMag == type.magSize || reserveAmmo <= 0) return
        isReloading = true
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                val needed = type.magSize - ammoInMag
                val toLoad = needed.coerceAtMost(reserveAmmo)
                ammoInMag += toLoad
                reserveAmmo -= toLoad
                isReloading = false
            }
        }, type.reloadTime)
    }

    fun reset() {
        ammoInMag = type.magSize
        reserveAmmo = type.magSize * 4
        isReloading = false
        lastShotTime = 0f
    }

    fun getSpreadRadians(): Float = type.spread * MathUtils.degreesToRadians
}
