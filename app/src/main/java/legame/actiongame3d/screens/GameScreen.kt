package legame.actiongame3d.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.utils.Array
import legame.actiongame3d.ActionGame3D
import legame.actiongame3d.MobileControls
import legame.actiongame3d.entities.Bot
import legame.actiongame3d.entities.Player
import legame.actiongame3d.weapons.WeaponType
import legame.actiongame3d.world.GameWorld

class GameScreen(private val game: ActionGame3D) : ScreenAdapter() {
    private lateinit var camera: PerspectiveCamera
    private lateinit var player: Player
    private lateinit var world: GameWorld
    private lateinit var controls: MobileControls
    private val bots = Array<Bot>()
    private val modelBatch = ModelBatch()
    private val layout = GlyphLayout()
    private val tmpVec = Vector3()
    private var matchTime = 0f
    private var score = 0
    private var gameOver = false
    private var victory = false
    private var muzzleFlashTimer = 0f
    private var hitMarkerTimer = 0f
    private var damageFlashTimer = 0f

    override fun show() {
        camera = PerspectiveCamera(75f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.near = 0.1f
        camera.far = 200f
        camera.position.set(0f, 1.7f, 0f)
        camera.lookAt(0f, 1.7f, -1f)
        camera.update()
        player = Player(camera)
        world = GameWorld()
        controls = MobileControls()
        spawnBots(8)
        Gdx.input.isCursorCatched = false
        matchTime = 0f; score = 0; gameOver = false; victory = false
    }

    private fun spawnBots(count: Int) {
        bots.forEach { it.dispose() }
        bots.clear()
        val mb = world.getModelBuilder()
        for (i in 0 until count) {
            val angle = i * (360f / count)
            val rad = MathUtils.degreesToRadians * angle
            val dist = MathUtils.random(18f, 32f)
            bots.add(Bot(MathUtils.cos(rad) * dist, MathUtils.sin(rad) * dist, mb))
        }
    }

    override fun render(delta: Float) {
        if (!gameOver) { matchTime += delta; update(delta) }
        Gdx.gl.glClearColor(0.45f, 0.62f, 0.85f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        modelBatch.begin(camera)
        modelBatch.render(world.instances, world.environment)
        bots.forEach { bot ->
            if (bot.isAlive) {
                modelBatch.render(bot.bodyInstance, world.environment)
                modelBatch.render(bot.headInstance, world.environment)
            }
        }
        modelBatch.end()
        renderHUD()
        if (!gameOver) controls.render()
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMenu()
        }
        if (gameOver && Gdx.input.justTouched()) {
            player.reset(); spawnBots(8)
            matchTime = 0f; score = 0; gameOver = false; victory = false
        }
    }

    private fun update(delta: Float) {
        controls.update()
        player.updateMobile(
            delta, world.worldSize - 2f,
            controls.moveX, controls.moveY,
            controls.lookDeltaX, controls.lookDeltaY,
            controls.sprintPressed, controls.jumpJustPressed, controls.reloadJustPressed
        )
        if (muzzleFlashTimer > 0) muzzleFlashTimer -= delta
        if (hitMarkerTimer > 0) hitMarkerTimer -= delta
        if (damageFlashTimer > 0) damageFlashTimer -= delta
        if (player.isAlive) {
            val wep = player.currentWeapon
            val shoot = if (wep.type.isAutomatic) controls.shootPressed else controls.shootJustPressed
            if (shoot && wep.shoot()) { muzzleFlashTimer = 0.08f; performShot() }
        }
        var alive = 0
        bots.forEach { bot ->
            bot.update(delta, player)
            if (bot.isAlive) {
                alive++
                if (bot.state == Bot.State.ATTACK && bot.stateTimer < 0.05f) {
                    val dist = bot.position.dst(player.position)
                    if (dist < 20f && MathUtils.random() < 0.55f) {
                        player.takeDamage(MathUtils.random(8f, 18f))
                        damageFlashTimer = 0.25f
                    }
                }
            }
        }
        if (!player.isAlive) {
            gameOver = true; victory = false
            game.totalKills += player.kills
            if (score > game.highScore) game.highScore = score
        } else if (alive == 0) {
            gameOver = true; victory = true
            score += 500; game.totalKills += player.kills
            if (score > game.highScore) game.highScore = score
        }
    }

    private fun performShot() {
        val wep = player.currentWeapon
        val ray = camera.getPickRay(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        val spread = wep.getSpreadRadians()
        if (spread > 0) {
            ray.direction.x += MathUtils.random(-spread, spread)
            ray.direction.y += MathUtils.random(-spread, spread)
            ray.direction.z += MathUtils.random(-spread, spread)
            ray.direction.nor()
        }
        val pellets = if (wep.type == WeaponType.SHOTGUN) 6 else 1
        var anyHit = false
        for (p in 0 until pellets) {
            val pr = Ray(ray.origin, ray.direction.cpy())
            if (p > 0) {
                val s = spread * 1.5f
                pr.direction.x += MathUtils.random(-s, s)
                pr.direction.y += MathUtils.random(-s, s)
                pr.direction.z += MathUtils.random(-s, s)
                pr.direction.nor()
            }
            var closest = wep.type.range
            var hitBot: Bot? = null
            bots.forEach { bot ->
                if (!bot.isAlive) return@forEach
                val toBot = tmpVec.set(bot.position).sub(pr.origin)
                val proj = toBot.dot(pr.direction)
                if (proj < 0 || proj > closest) return@forEach
                val closestPt = tmpVec.set(pr.origin).mulAdd(pr.direction, proj)
                if (closestPt.dst(bot.position) < 0.9f) { closest = proj; hitBot = bot }
            }
            hitBot?.let {
                anyHit = true
                if (it.takeDamage(wep.type.damage)) { player.kills++; score += 100 } else score += 10
            }
        }
        if (anyHit) hitMarkerTimer = 0.2f
    }

    private fun renderHUD() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        game.spriteBatch.begin()
        game.font.data.setScale(1.2f)
        game.font.color = if (hitMarkerTimer > 0) Color.RED else Color.WHITE
        layout.setText(game.font, if (hitMarkerTimer > 0) "X" else "+")
        game.font.draw(game.spriteBatch, layout, (w - layout.width) / 2f, (h + layout.height) / 2f)
        game.font.data.setScale(1.0f)
        game.font.color = Color.WHITE
        game.font.draw(game.spriteBatch, "HP", 20f, h - 20f)
        game.font.color = when {
            player.health > 60 -> Color.GREEN
            player.health > 30 -> Color.YELLOW
            else -> Color.RED
        }
        game.font.draw(game.spriteBatch, "${player.health.toInt()}", 55f, h - 20f)
        val wep = player.currentWeapon
        game.font.color = Color.WHITE
        game.font.draw(game.spriteBatch, wep.type.displayName, w - 200f, h - 20f)
        game.font.color = if (wep.isReloading) Color.ORANGE else Color.CYAN
        game.font.draw(game.spriteBatch, if (wep.isReloading) "RELOAD..." else "${wep.ammoInMag}/${wep.reserveAmmo}", w - 200f, h - 45f)
        game.font.color = Color.GOLD
        game.font.draw(game.spriteBatch, "Score $score  Kills ${player.kills}", 20f, h - 50f)
        game.font.color = Color.LIGHT_GRAY
        game.font.draw(game.spriteBatch, "Enemy ${bots.count { it.isAlive }}", 20f, h - 75f)
        if (gameOver) {
            game.font.data.setScale(2.5f)
            game.font.color = if (victory) Color.GREEN else Color.RED
            layout.setText(game.font, if (victory) "VICTORY" else "YOU DIED")
            game.font.draw(game.spriteBatch, layout, (w - layout.width) / 2f, h * 0.6f)
            game.font.data.setScale(1.2f)
            game.font.color = Color.WHITE
            layout.setText(game.font, "TAP TO RESTART")
            game.font.draw(game.spriteBatch, layout, (w - layout.width) / 2f, h * 0.45f)
        }
        game.spriteBatch.end()
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
        if (::controls.isInitialized) controls.resize(width, height)
    }

    override fun dispose() {
        modelBatch.dispose()
        world.dispose()
        bots.forEach { it.dispose() }
        if (::controls.isInitialized) controls.dispose()
    }
}
