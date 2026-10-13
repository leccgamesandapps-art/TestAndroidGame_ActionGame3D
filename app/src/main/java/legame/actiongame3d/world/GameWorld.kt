package legame.actiongame3d.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.environment.PointLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable

class GameWorld : Disposable {
    val environment = Environment()
    private val modelBuilder = ModelBuilder()
    private val models = Array<Model>()
    val instances = Array<ModelInstance>()
    val worldSize = 45f

    init {
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.38f, 0.42f, 1f))
        environment.add(DirectionalLight().set(0.85f, 0.82f, 0.75f, -0.4f, -0.9f, -0.3f))
        environment.add(PointLight().set(0.4f, 0.55f, 0.9f, 15f, 6f, 15f, 40f))
        environment.add(PointLight().set(0.9f, 0.5f, 0.3f, -18f, 5f, -12f, 35f))
        buildMap()
    }

    private fun buildMap() {
        val attr = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
        val ground = modelBuilder.createBox(worldSize * 2, 0.4f, worldSize * 2,
            Material(ColorAttribute.createDiffuse(Color.valueOf("2E3A2E"))), attr)
        models.add(ground)
        instances.add(ModelInstance(ground).also { it.transform.setToTranslation(0f, -0.2f, 0f) })

        val wallMat = Material(ColorAttribute.createDiffuse(Color.valueOf("4A5568")))
        val h = 6f
        createBox(-worldSize, h / 2, 0f, 1.2f, h, worldSize * 2, wallMat, attr)
        createBox(worldSize, h / 2, 0f, 1.2f, h, worldSize * 2, wallMat, attr)
        createBox(0f, h / 2, -worldSize, worldSize * 2, h, 1.2f, wallMat, attr)
        createBox(0f, h / 2, worldSize, worldSize * 2, h, 1.2f, wallMat, attr)

        val cover = Material(ColorAttribute.createDiffuse(Color.valueOf("5C6B7A")))
        createBox(-8f, 1.2f, 5f, 6f, 2.4f, 0.8f, cover, attr)
        createBox(8f, 1.2f, -5f, 6f, 2.4f, 0.8f, cover, attr)
        createBox(-12f, 1f, -10f, 0.8f, 2f, 5f, cover, attr)
        createBox(12f, 1f, 10f, 0.8f, 2f, 5f, cover, attr)

        val crate = Material(ColorAttribute.createDiffuse(Color.valueOf("8B6914")))
        listOf(Vector3(-5f, 0.6f, -8f), Vector3(6f, 0.6f, 7f), Vector3(-18f, 0.7f, 5f), Vector3(15f, 0.5f, -15f)).forEach {
            val s = MathUtils.random(0.9f, 1.4f)
            createBox(it.x, it.y, it.z, s, s, s, crate, attr)
        }

        val accent = Material(ColorAttribute.createDiffuse(Color.valueOf("718096")))
        for (i in 0 until 4) {
            val a = i * 90f + 45f
            val r = MathUtils.degreesToRadians * a
            createCylinder(MathUtils.cos(r) * 12f, MathUtils.sin(r) * 12f, 4.5f, 1.1f, accent, attr)
        }
    }

    private fun createBox(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float, mat: Material, attr: Long) {
        val m = modelBuilder.createBox(w, h, d, mat, attr)
        models.add(m)
        instances.add(ModelInstance(m).also { it.transform.setToTranslation(x, y, z) })
    }

    private fun createCylinder(x: Float, z: Float, height: Float, radius: Float, mat: Material, attr: Long) {
        val m = modelBuilder.createCylinder(radius * 2, height, radius * 2, 16, mat, attr)
        models.add(m)
        instances.add(ModelInstance(m).also { it.transform.setToTranslation(x, height / 2, z) })
    }

    fun getModelBuilder() = modelBuilder

    override fun dispose() {
        models.forEach { it.dispose() }
        models.clear()
        instances.clear()
    }
}
