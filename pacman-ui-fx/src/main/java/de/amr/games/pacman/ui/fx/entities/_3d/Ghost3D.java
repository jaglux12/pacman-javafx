package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	//@formatter:off
	private static final int[][][] ROTATION_INTERVALS = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} },
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} },
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} },
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} },
	};
	//@formatter:on

	private static int indexOfDir(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationInterval(Direction from, Direction to) {
		int row = indexOfDir(from), col = indexOfDir(to);
		return ROTATION_INTERVALS[row][col];
	}

	private class FlashingAnimation extends Transition {

		public FlashingAnimation() {
			setCycleCount(INDEFINITE);
			setCycleDuration(Duration.seconds(0.1));
			setAutoReverse(true);
		}

		@Override
		protected void interpolate(double frac) {
			flashingSkin.setDiffuseColor(Color.rgb((int) (frac * 120), (int) (frac * 180), 255));
		}
	};

	private final Ghost ghost;

	private final Rendering2D rendering2D;
	private final PhongMaterial normalSkin;
	private final PhongMaterial blueSkin;
	private final PhongMaterial flashingSkin;
	private final Group coloredGhost;
	private final RotateTransition coloredGhostRotateTransition;
	private final Transition flashingAnimation = new FlashingAnimation();

	private final Group deadGhost;
	private final RotateTransition deadGhostRotateTransition;

	private final Box bountyShape;

	private Direction targetDir;

	public Ghost3D(Ghost ghost, Rendering2D rendering2D) {
		this.ghost = ghost;
		targetDir = ghost.dir();
		int[] rotation = rotationInterval(ghost.dir(), ghost.dir());

		this.rendering2D = rendering2D;
		Color ghostColor = Rendering2D_Assets.getGhostColor(ghost.id);

		normalSkin = new PhongMaterial(ghostColor);
		normalSkin.setSpecularColor(ghostColor.brighter());

		blueSkin = new PhongMaterial(Rendering2D_Assets.getGhostBlueColor());
		blueSkin.setSpecularColor(Rendering2D_Assets.getGhostBlueColor().brighter());

		flashingSkin = new PhongMaterial();

		coloredGhost = GianmarcosModel3D.createGhost();
		coloredGhost.setRotationAxis(Rotate.Z_AXIS);
		coloredGhost.setRotate(rotation[0]);

		coloredGhostRotateTransition = new RotateTransition(Duration.seconds(0.5), coloredGhost);
		coloredGhostRotateTransition.setAxis(Rotate.Z_AXIS);

		stopBlueMode();

		bountyShape = new Box(8, 8, 8);
		bountyShape.setMaterial(new PhongMaterial());

		deadGhost = GianmarcosModel3D.createGhostEyes();
		deadGhostRotateTransition = new RotateTransition(Duration.seconds(0.5), deadGhost);
		deadGhostRotateTransition.setAxis(Rotate.Z_AXIS);
		deadGhost.setRotationAxis(Rotate.Z_AXIS);
		deadGhost.setRotate(rotation[0]);
	}
	
	private MeshView getColoredGhostBody() {
		return (MeshView) coloredGhost.getChildren().get(0);
	}

	public void startBlueMode() {
		MeshView body = getColoredGhostBody();
		body.setMaterial(blueSkin);
		log("Start blue mode for %s", ghost.name);
	}

	public void stopBlueMode() {
		MeshView body = getColoredGhostBody();
		body.setMaterial(normalSkin);
		log("Stop blue mode for %s", ghost.name);
	}

	public void startFlashing() {
		MeshView body = getColoredGhostBody();
		body.setMaterial(flashingSkin);
		flashingAnimation.playFromStart();
	}

	public void stopFlashing() {
		flashingAnimation.stop();
		MeshView body = getColoredGhostBody();
		body.setMaterial(normalSkin);
	}

	private void setBounty(int value) {
		Image sprite = rendering2D.subImage(rendering2D.getBountyNumberSpritesMap().get(value));
		PhongMaterial material = (PhongMaterial) bountyShape.getMaterial();
		material.setBumpMap(sprite);
		material.setDiffuseMap(sprite);
	}

	public void update() {
		setVisible(ghost.visible);
		setTranslateX(ghost.position.x);
		setTranslateY(ghost.position.y);
		if (ghost.bounty > 0) {
			getChildren().setAll(bountyShape);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			setBounty(ghost.bounty);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			getChildren().setAll(deadGhost);
			rotateTowardsMoveDir();
		} else {
			getChildren().setAll(coloredGhost);
			rotateTowardsMoveDir();
		}
	}

	private void rotateTowardsMoveDir() {
		if (targetDir != ghost.dir()) {
			int[] rotationInterval = rotationInterval(targetDir, ghost.dir());
			coloredGhostRotateTransition.stop();
			coloredGhostRotateTransition.setFromAngle(rotationInterval[0]);
			coloredGhostRotateTransition.setToAngle(rotationInterval[1]);
			coloredGhostRotateTransition.play();
			deadGhostRotateTransition.stop();
			deadGhostRotateTransition.setFromAngle(rotationInterval[0]);
			deadGhostRotateTransition.setToAngle(rotationInterval[1]);
			deadGhostRotateTransition.play();
			targetDir = ghost.dir();
		}
	}
}