package legame.actiongame3d.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

class Bot(startX: Float, startZ: Float, modelBuilder: ModelBuilder) : Disposable {
    val position = Vector3(startX, 1.0f, startZ)
    private val targetDir = Vector3()
    var health = 100f
    var isAlive = true
    val color = Color(MathUtils.random(0.4f, 1f), MathUtils.random(0.2f, 0.6f), MathUtils.random(0.2f, 0.6f), 1f)

    enum class State { PATROL, CHASE, ATTACK, DEAD }
    var state = State.PATROL
        private set
    var stateTimer = 0f
        private set
    private var patrolTarget = Vector3()
    private val detectionRange = 28f
    private val attackRange = 18f
    private val moveSpeed = 4.2f
    private val shootCooldown = 0.7f

    private val bodyModel: Model
    private val headModel: Model
    val bodyInstance: ModelInstance
    val headInstance: ModelInstance

    init {
        val attr = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        bodyModel = modelBuilder.createBox(0.7f, 1.4f, 0.5f, Material(ColorAttribute.createDiffuse(color)), attr)
        bodyInstance = ModelInstance(bodyModel)
        headModel = modelBuilder.createSphere(0.45f, 0.45f, 0.45f, 12, 12, Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), attr)
        headInstance = ModelInstance(headModel)
        pickNewPatrolTarget()
        updateTransforms()
    }

    private fun updateTransforms() {
        bodyInstance.transform.setToTranslation(position.x, position.y, position.z)
        headInstance.transform.setToTranslation(position.x, position.y + 0.95f, position.z)
    }

    fun update(delta: Float, player: Player) {
        if (!isAlive) { state = State.DEAD; return }
        stateTimer += delta
        val dist = position.dst(player.position)
        when (state) {
            State.PATROL -> {
                if (dist < detectionRange && player.isAlive) { state = State.CHASE; stateTimer = 0f }
                else {
                    moveToward(patrolTarget, delta)
                    if (position.dst2(patrolTarget) < 4f || stateTimer > 6f) { pickNewPatrolTarget(); stateTimer = 0f }
                }
            }
            State.CHASE -> {
                if (dist > detectionRange * 1.3f || !player.isAlive) { state = State.PATROL; pickNewPatrolTarget() }
                else if (dist < attackRange) state = State.ATTACK
                else moveToward(player.position, delta)
            }
            State.ATTACK -> {
                if (dist > attackRange * 1.4f) state = State.CHASE
                else if (!player.isAlive) { state = State.PATROL; pickNewPatrolTarget() }
                else if (stateTimer > shootCooldown) stateTimer = 0f
            }
            State.DEAD -> {}
        }
        updateTransforms()
    }

    private fun moveToward(target: Vector3, delta: Float) {
        targetDir.set(target).sub(position)
        targetDir.y = 0f
        if (targetDir.len2() > 0.1f) {
            targetDir.nor().scl(moveSpeed * delta)
            position.add(targetDir)
        }
    }

    private fun pickNewPatrolTarget() {
        patrolTarget.set(MathUtils.random(-35f, 35f), 1f, MathUtils.random(-35f, 35f))
    }

    fun takeDamage(amount: Float): Boolean {
        if (!isAlive) return false
        health -= amount
        if (health <= 0f) {
            health = 0f
            isAlive = false
            state = State.DEAD
            return true
        }
        if (state == State.PATROL) state = State.CHASE
        return false
    }

    override fun dispose() {
        bodyModel.dispose()
        headModel.dispose()
    }
}
