package legame.actiongame3d

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.sqrt

/** On-screen mobile controls: left stick, right look, shoot/jump/reload/sprint. */
class MobileControls {
    val stage = Stage(ScreenViewport())
    private val shape = ShapeRenderer()

    var moveX = 0f; private set
    var moveY = 0f; private set
    var lookDeltaX = 0f; private set
    var lookDeltaY = 0f; private set
    var shootPressed = false; private set
    var shootJustPressed = false; private set
    var jumpJustPressed = false; private set
    var reloadJustPressed = false; private set
    var sprintPressed = false; private set

    private var prevShoot = false
    private var lookPointer = -1
    private var lastLookX = 0f
    private var lastLookY = 0f

    private val w get() = Gdx.graphics.width.toFloat()
    private val h get() = Gdx.graphics.height.toFloat()
    private val joyCX get() = w * 0.18f
    private val joyCY get() = h * 0.22f
    private val joyR get() = h * 0.14f
    private val shootCX get() = w * 0.88f
    private val shootCY get() = h * 0.28f
    private val shootR get() = h * 0.09f
    private val jumpCX get() = w * 0.72f
    private val jumpCY get() = h * 0.18f
    private val btnR get() = h * 0.055f
    private val reloadCX get() = w * 0.88f
    private val reloadCY get() = h * 0.48f
    private val sprintCX get() = w * 0.72f
    private val sprintCY get() = h * 0.38f

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun update() {
        lookDeltaX = 0f; lookDeltaY = 0f
        jumpJustPressed = false; reloadJustPressed = false; shootJustPressed = false
        moveX = 0f; moveY = 0f; shootPressed = false; sprintPressed = false

        for (i in 0 until 5) {
            if (!Gdx.input.isTouched(i)) {
                if (lookPointer == i) lookPointer = -1
                continue
            }
            val tx = Gdx.input.getX(i).toFloat()
            val ty = h - Gdx.input.getY(i).toFloat()

            if (tx < w * 0.45f) {
                val dx = tx - joyCX
                val dy = ty - joyCY
                val len = sqrt(dx * dx + dy * dy)
                if (len > 1f) {
                    val scale = (len.coerceAtMost(joyR)) / len
                    moveX = (dx * scale) / joyR
                    moveY = (dy * scale) / joyR
                }
                continue
            }
            if (dist(tx, ty, shootCX, shootCY) <= shootR * 1.2f) {
                shootPressed = true; continue
            }
            if (dist(tx, ty, jumpCX, jumpCY) <= btnR * 1.3f) {
                if (Gdx.input.justTouched()) jumpJustPressed = true
                continue
            }
            if (dist(tx, ty, reloadCX, reloadCY) <= btnR * 1.3f) {
                if (Gdx.input.justTouched()) reloadJustPressed = true
                continue
            }
            if (dist(tx, ty, sprintCX, sprintCY) <= btnR * 1.3f) {
                sprintPressed = true; continue
            }
            if (tx > w * 0.5f) {
                if (lookPointer == -1 || lookPointer == i) {
                    if (lookPointer == -1) {
                        lookPointer = i; lastLookX = tx; lastLookY = ty
                    } else {
                        lookDeltaX = tx - lastLookX
                        lookDeltaY = ty - lastLookY
                        lastLookX = tx; lastLookY = ty
                    }
                }
            }
        }
        if (shootPressed && !prevShoot) shootJustPressed = true
        prevShoot = shootPressed
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2; val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    fun render() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shape.projectionMatrix = stage.camera.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(1f, 1f, 1f, 0.25f)
        shape.circle(joyCX, joyCY, joyR)
        shape.color = Color(1f, 1f, 1f, 0.55f)
        shape.circle(joyCX + moveX * joyR, joyCY + moveY * joyR, joyR * 0.4f)
        shape.color = if (shootPressed) Color(1f, 0.3f, 0.3f, 0.7f) else Color(1f, 0.2f, 0.2f, 0.45f)
        shape.circle(shootCX, shootCY, shootR)
        shape.color = Color(0.3f, 0.8f, 1f, 0.45f)
        shape.circle(jumpCX, jumpCY, btnR)
        shape.color = Color(1f, 0.85f, 0.2f, 0.45f)
        shape.circle(reloadCX, reloadCY, btnR)
        shape.color = if (sprintPressed) Color(0.4f, 1f, 0.4f, 0.7f) else Color(0.3f, 0.9f, 0.3f, 0.45f)
        shape.circle(sprintCX, sprintCY, btnR)
        shape.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun dispose() {
        shape.dispose()
        stage.dispose()
    }
}
