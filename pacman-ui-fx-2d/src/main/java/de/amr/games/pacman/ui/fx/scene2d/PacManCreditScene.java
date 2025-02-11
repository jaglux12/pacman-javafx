/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.input.Keyboard;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.KEYS_ADD_CREDIT;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.KEYS_START_GAME;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	@Override
	public boolean isCreditVisible() {
		return true;
	}

	@Override
	public void init() {
		setScoreVisible(true);
	}

	@Override
	public void update() {
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.pressed(KEYS_START_GAME)) {
			context.actionHandler().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var font8 = sceneFont(8);
		var font6 = sceneFont(6);
		drawText("PUSH START BUTTON", context.theme().color("palette.orange"), font8, t(6), t(17));
		drawText("1 PLAYER ONLY", context.theme().color("palette.cyan"), font8, t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", context.theme().color("palette.rose"), font8, t(1), t(25));
		drawText("PTS", context.theme().color("palette.rose"), font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter();
	}
}