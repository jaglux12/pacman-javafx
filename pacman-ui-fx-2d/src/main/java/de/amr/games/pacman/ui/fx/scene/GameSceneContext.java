package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.ActionHandler;
import de.amr.games.pacman.ui.fx.SoundHandler;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.media.AudioClip;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameSceneContext {

  ActionHandler actionHandler();

  Theme theme();

  <S extends SpriteSheet> S spriteSheet();

  SoundHandler soundHandler();

  default AudioClip clip(String key) {
    return soundHandler().audioClip(game().variant(), key);
  }

  default GameController gameController() {
    return GameController.it();
  }

  default GameState gameState() {
    return GameController.it().state();
  }

  default GameModel game() {
    return GameController.it().game();
  }

  default GameVariant gameVariant() {
    return game().variant();
  }

  default Optional<GameLevel> gameLevel() {
    return game().level();
  }

  default Optional<World> gameWorld() {
    return game().level().map(GameLevel::world);
  }
}