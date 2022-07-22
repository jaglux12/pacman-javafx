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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.HUD;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.ResizableCanvas;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	protected final V2d unscaledSize;
	protected final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
	protected final StackPane root = new StackPane();
	protected final SubScene fxSubScene;
	protected final ResizableCanvas canvas;
	protected final ResizableCanvas overlayCanvas;
	protected final Pane overlayPane = new Pane();
	protected final HUD hud = new HUD();

	protected SceneContext ctx;

	protected GameScene2D() {
		this(new V2d(ArcadeWorld.WORLD_SIZE));
	}

	protected GameScene2D(V2d size) {
		unscaledSize = size;

		fxSubScene = new SubScene(root, unscaledSize.x(), unscaledSize.y());

		canvas = new ResizableCanvas();
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());

		overlayCanvas = new ResizableCanvas();
		overlayCanvas.widthProperty().bind(fxSubScene.widthProperty());
		overlayCanvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayCanvas.visibleProperty().bind(Env.showDebugInfoPy);
		overlayCanvas.setMouseTransparent(true);

		overlayPane.setVisible(Env.showDebugInfoPy.get());
		overlayPane.visibleProperty().bind(Env.showDebugInfoPy);

		root.getChildren().addAll(canvas, overlayCanvas, overlayPane);
		resize(unscaledSize.y());
	}

	@Override
	public void resize(double height) {
		double aspectRatio = unscaledSize.x() / unscaledSize.y();
		double scaling = height / unscaledSize.y();
		double width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		scalingPy.set(scaling);
		LOGGER.info("Game scene %s resized. Canvas size: %.0f x %.0f scaling: %.2f", getClass().getSimpleName(),
				canvas.getWidth(), canvas.getHeight(), scaling);
	}

	@Override
	public final void updateAndRender() {
		update();
		render(canvas.getGraphicsContext2D());
	}

	protected void render(GraphicsContext g) {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.save();
		g.scale(getScaling(), getScaling());
		g.setFontSmoothingType(FontSmoothingType.LCD);
		drawSceneContent(g);
		g.restore();
		if (overlayCanvas.isVisible()) {
			var og = overlayCanvas.getGraphicsContext2D();
			og.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
			og.save();
			og.scale(getScaling(), getScaling());
			og.setFontSmoothingType(FontSmoothingType.LCD);
			ctx.r2D.drawTileBorders(og);
			og.restore();
		}
		drawHUD(g);
	}

	public void drawHUD(GraphicsContext g) {
		hud.width = canvas.getWidth();
		hud.height = canvas.getHeight();
		hud.scaling = getScaling();
		hud.credit = ctx.game().getCredit();
		hud.score = ctx.game().scores.gameScore;
		hud.highScore = ctx.game().scores.highScore;
		hud.draw(g);
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	public void update() {
	}

	/**
	 * Draws the scene content. Subclasses override this method.
	 */
	public void drawSceneContent(GraphicsContext g) {
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void setResizeBehavior(DoubleExpression width, DoubleExpression height) {
		height.addListener((x, y, h) -> resize(h.doubleValue()));
	}

	@Override
	public SceneContext getSceneContext() {
		return ctx;
	}

	@Override
	public void setSceneContext(SceneContext context) {
		ctx = context;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	public StackPane getRoot() {
		return root;
	}

	public Canvas getGameSceneCanvas() {
		return canvas;
	}

	public Canvas getOverlayCanvas() {
		return overlayCanvas;
	}

	public V2d getUnscaledSize() {
		return unscaledSize;
	}

	@Override
	public double getScaling() {
		return scalingPy.get();
	}
}