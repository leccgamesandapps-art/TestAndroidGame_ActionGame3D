package legame.actiongame3d

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.ModelBatch
import legame.actiongame3d.screens.MainMenuScreen

class ActionGame3D : Game() {
    lateinit var spriteBatch: SpriteBatch
    lateinit var modelBatch: ModelBatch
    lateinit var font: BitmapFont

    var totalKills = 0
    var highScore = 0

    override fun create() {
        spriteBatch = SpriteBatch()
        modelBatch = ModelBatch()
        font = BitmapFont()
        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        spriteBatch.dispose()
        modelBatch.dispose()
        font.dispose()
        super.dispose()
    }

    fun startGame() {
        setScreen(legame.actiongame3d.screens.GameScreen(this))
    }

    fun goToMenu() {
        setScreen(MainMenuScreen(this))
    }
}
