/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.world.World;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * 3D bonus symbol.
 * 
 * @author Armin Reichert
 */
public class Bonus3D {

	private final Bonus bonus;
	private final Image symbolImage;
	private final Image pointsImage;
	private final Box shape;

	private RotateTransition eatenAnimation;
	private RotateTransition edibleAnimation;

	public Bonus3D(Bonus bonus, Image symbolImage, Image pointsImage) {
		checkNotNull(bonus);
		checkNotNull(symbolImage);
		checkNotNull(pointsImage);

		this.bonus = bonus;
		this.symbolImage = symbolImage;
		this.pointsImage = pointsImage;
		this.shape = new Box(TS, TS, TS);

		edibleAnimation = new RotateTransition(Duration.seconds(1), shape);
		edibleAnimation.setAxis(Rotate.Z_AXIS); // to trigger initial change
		edibleAnimation.setFromAngle(0);
		edibleAnimation.setToAngle(360);
		edibleAnimation.setInterpolator(Interpolator.LINEAR);
		edibleAnimation.setCycleCount(Animation.INDEFINITE);

		eatenAnimation = new RotateTransition(Duration.seconds(1), shape);
		eatenAnimation.setAxis(Rotate.X_AXIS);
		eatenAnimation.setFromAngle(0);
		eatenAnimation.setToAngle(360);
		eatenAnimation.setInterpolator(Interpolator.LINEAR);
		eatenAnimation.setRate(2);
	}

	public void update(GameLevel level) {
		setPosition(bonus.entity().center());
		boolean visible = bonus.state() != Bonus.STATE_INACTIVE && !outsideWorld(level.world());
		shape.setVisible(visible);
		updateEdibleAnimation();
	}

	private void updateEdibleAnimation() {
		var rotationAxis = Rotate.X_AXIS; // default for static bonus
		if (bonus instanceof MovingBonus movingBonus) {
			rotationAxis = movingBonus.entity().moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			if (movingBonus.entity().moveDir() == Direction.UP || movingBonus.entity().moveDir() == Direction.RIGHT) {
				edibleAnimation.setRate(-1);
			} else {
				edibleAnimation.setRate(1);
			}
		}
		if (!edibleAnimation.getAxis().equals(rotationAxis)) {
			edibleAnimation.stop();
			edibleAnimation.setAxis(rotationAxis);
			edibleAnimation.play();
		}
	}

	public void showEdible() {
		var imageView = new ImageView(symbolImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(TS);
		showImage(imageView.getImage());
		shape.setWidth(TS);
		updateEdibleAnimation();
		edibleAnimation.playFromStart();
	}

	public void showEaten() {
		var imageView = new ImageView(pointsImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(1.8 * TS);
		showImage(imageView.getImage());
		edibleAnimation.stop();
		eatenAnimation.playFromStart();
		shape.setRotationAxis(Rotate.X_AXIS);
		shape.setRotate(0);
		shape.setWidth(1.8 * TS);
	}

	private void showImage(Image texture) {
		var material = new PhongMaterial(Color.WHITE);
		material.setDiffuseMap(texture);
		shape.setMaterial(material);
	}

	public Node getRoot() {
		return shape;
	}

	public void hide() {
		shape.setVisible(false);
	}

	public void setPosition(Vector2f position) {
		shape.setTranslateX(position.x());
		shape.setTranslateY(position.y());
		shape.setTranslateZ(-HTS);
	}

	private boolean outsideWorld(World world) {
		double x = bonus.entity().center().x();
		return x < HTS || x > world.numCols() * TS - HTS;
	}
}