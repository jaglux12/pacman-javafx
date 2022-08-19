/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import de.amr.games.pacman.controller.mspacman.Intermission2Controller;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.ArcadeRendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermissionScene2 extends GameScene2D {

	private Intermission2Controller sceneController;
	private Intermission2Controller.Context icc;

	@Override
	public void setSceneContext(SceneContext sceneContext) {
		super.setSceneContext(sceneContext);
		sceneController = new Intermission2Controller(sceneContext.gameController());
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInState(Intermission2Controller.State.FLAP);
		var clapperboardAnimationSet = new EntityAnimationSet<Integer>(1);
		clapperboardAnimationSet.put(Clapperboard.ACTION, ArcadeRendererMsPacManGame.get().createClapperboardAnimation());
		clapperboardAnimationSet.select(Clapperboard.ACTION);
		icc.flap.setAnimationSet(clapperboardAnimationSet);
		icc.msPacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(icc.msPacMan));
		icc.msPacMan.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		icc.pacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(icc.pacMan));
		icc.pacMan.animationSet().ifPresent(animations -> {
			var munching = ArcadeRendererMsPacManGame.get().createPacManMunchingAnimationMap(icc.pacMan);
			animations.put(AnimKeys.PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		ArcadeRendererMsPacManGame.get().drawFlap(g, icc.flap);
		ctx.r2D().drawPac(g, icc.msPacMan);
		ctx.r2D().drawPac(g, icc.pacMan);
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter);
	}
}