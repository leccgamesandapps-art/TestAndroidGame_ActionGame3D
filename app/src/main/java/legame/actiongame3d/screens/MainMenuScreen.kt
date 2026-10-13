package legame.actiongame3d.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.viewport.FitViewport
import legame.actiongame3d.ActionGame3D

class MainMenuScreen(private val game: ActionGame3D) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1280f, 720f, camera)
    private val layout = GlyphLayout()

    override fun show() {
        Gdx.input.isCursorCatched = false
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        game.spriteBatch.projectionMatrix = camera.combined
        game.spriteBatch.begin()

        game.font.data.setScale(3.2f)
        game.font.color = Color.valueOf("4FC3F7")
        layout.setText(game.font, "ActionGame3D")
        game.font.draw(game.spriteBatch, layout, (1280f - layout.width) / 2f, 560f)

        game.font.data.setScale(1.2f)
        game.font.color = Color.LIGHT_GRAY
        layout.setText(game.font, "v0.1.2  •  Mobile FPS")
        game.font.draw(game.spriteBatch, layout, (1280f - layout.width) / 2f, 500f)

        game.font.data.setScale(1.9f)
        game.font.color = Color.WHITE
        layout.setText(game.font, "TAP ANYWHERE TO START")
        game.font.draw(game.spriteBatch, layout, (1280f - layout.width) / 2f, 400f)

        game.font.data.setScale(1.05f)
        game.font.color = Color.LIGHT_GRAY
        val lines = listOf(
            "LEFT stick  = Move",
            "RIGHT drag  = Look",
            "RED button  = Shoot",
            "BLUE        = Jump",
            "YELLOW      = Reload",
            "GREEN       = Sprint"
        )
        var y = 320f
        lines.forEach {
            layout.setText(game.font, it)
            game.font.draw(game.spriteBatch, layout, (1280f - layout.width) / 2f, y)
            y -= 30f
        }

        game.font.data.setScale(1f)
        game.font.color = Color.GOLD
        layout.setText(game.font, "Kills: ${game.totalKills}  |  High Score: ${game.highScore}")
        game.font.draw(game.spriteBatch, layout, (1280f - layout.width) / 2f, 70f)
        game.spriteBatch.end()

        if (Gdx.input.justTouched()) game.startGame()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }
}
