/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;

import java.util.Optional;

/**
 * Pac-Man / Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Pac extends Creature {

	public static final long REST_FOREVER = -1;
	public static final String ANIM_MUNCHING = "munching";
	public static final String ANIM_DYING = "dying";
	/** In Pac-Man cutscene, big Pac-Man appears. */
	public static final String ANIM_BIG_PACMAN = "big_pacman";
	/** In Ms. Pac-Man cutscenes, also Ms. PacMan's husband appears. */
	public static final String ANIM_HUSBAND_MUNCHING = "husband_munching";

	private final TickTimer powerTimer;
	private boolean dead;
	private long restingTicks;
	private long starvingTicks;
	private Steering steering;

	private GameLevel level;

	public Pac(String name) {
		super(name);
		powerTimer = new TickTimer("PacPower");
		reset();
	}

	@Override
	public String toString() {
		return "Pac{" +
			"dead=" + dead +
			", restingTicks=" + restingTicks +
			", starvingTicks=" + starvingTicks +
			", visible=" + visible +
			", pos_x=" + pos_x +
			", pos_y=" + pos_y +
			", vel_x=" + vel_x +
			", vel_y=" + vel_y +
			", acc_x=" + acc_x +
			", acc_y=" + acc_y +
			'}';
	}

	public void setLevel(GameLevel level) {
		this.level = level;
	}

	public GameLevel level() {
		return level;
	}

	@Override
	public World world() {
		return level.world();
	}

	@Override
	public boolean canReverse() {
		return isNewTileEntered();
	}

	@Override
	public void reset() {
		super.reset();
		dead = false;
		restingTicks = 0;
		starvingTicks = 0;
		corneringSpeedUp = 1.5f; // TODO experimental
		powerTimer.reset(0);
		selectAnimation(ANIM_MUNCHING);
	}

	public void update() {
		if (dead || restingTicks == REST_FOREVER) {
			return;
		}
		if (restingTicks == 0) {
			byte speed = powerTimer.isRunning() ? level.pacSpeedPoweredPercentage() : level.pacSpeedPercentage();
			setRelSpeed(speed);
			tryMoving();
			if (moved()) {
				startAnimation();
			} else {
				stopAnimation();
			}
		} else {
			--restingTicks;
		}
		powerTimer.advance();
	}

	public void killed() {
		stopAnimation();
		setPixelSpeed(0);
		dead = true;
		starvingTicks = 0;
		restingTicks = 0;
		powerTimer.stop();
	}

	public boolean isPowerFading() {
		return powerTimer.isRunning() && powerTimer.remaining() <= GameModel.PAC_POWER_FADES_TICKS;
	}

	public TickTimer powerTimer() {
		return powerTimer;
	}

	/* Number of ticks Pac is resting and not moving. */
	public long restingTicks() {
		return restingTicks;
	}

	public void rest(long ticks) {
		if (ticks != REST_FOREVER && ticks < 0) {
			throw new IllegalArgumentException("Resting time cannot be negative, but is: " + ticks);
		}
		restingTicks = ticks;
	}

	/* Number of ticks since Pac has eaten a pellet or energizer. */
	public long starvingTicks() {
		return starvingTicks;
	}

	public void starve() {
		++starvingTicks;
	}

	public void endStarving() {
		starvingTicks = 0;
	}

	public boolean isStandingStill() {
		return velocity().length() == 0 || !moved() || restingTicks == REST_FOREVER;
	}

	public Optional<Steering> steering() {
		return Optional.ofNullable(steering);
	}

	public void setSteering(Steering steering) {
		this.steering = steering;
	}

}