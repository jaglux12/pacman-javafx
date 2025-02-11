/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.FsmState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.*;

/**
 * Game states of the Pac-Man/Ms. Pac-Man game.
 * <p>
 * Rule of thumb: Specify what should happen when, not how exactly.
 * </p>
 *
 * @author Armin Reichert
 */
public enum GameState implements FsmState<GameModel> {

	BOOT { // "Das muss das Boot abkönnen!"
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.clearLevelCounter();
			game.score().reset();
			game.loadHighScore();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().changeState(INTRO);
			}
		}
	},

	INTRO {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
			game.setPlaying(false);
			game.removeLevel();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().changeState(READY);
			}
		}
	},

	CREDIT {
		@Override
		public void onUpdate(GameModel game) {
			// nothing to do here
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game) {
			gameController().manualSteering().setEnabled(false);
			gameController().publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
			if (!gameController().hasCredit()) {
				game.reset();
				game.createDemoLevel();
				game.startLevel();
			} else if (game.isPlaying()) {
				game.level().ifPresent(level -> {
					level.letsGetReadyToRumble();
					level.guys().forEach(Entity::show);
				});
			} else {
				game.score().reset();
				game.clearLevelCounter();
				game.reset();
				game.setLevel(1);
				game.startLevel();
				gameController().publishGameEvent(GameEventType.READY_TO_PLAY);
			}
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				final short showGuysTick = 120; // not sure
				final short showGuysDemoLevelTick = 130; // not sure
				final short startGameTick = 240; // not sure
				final short resumeGameTick = 90; // not sure
				if (gameController().hasCredit() && !game.isPlaying()) {
					// start new game
					if (timer.tick() == showGuysTick) {
						level.guys().forEach(Creature::show);
					} else if (timer.tick() == startGameTick) {
						// start game play
						game.setPlaying(true);
						level.startHunting(0);
						gameController().changeState(GameState.HUNTING);
					}
				} else if (game.isPlaying()) {
					// resume game play
					if (timer.tick() == resumeGameTick) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
						gameController().changeState(GameState.HUNTING);
					}
				} else {
					// demo level
					if (timer.tick() == showGuysDemoLevelTick) {
						level.guys().forEach(Creature::show);
						level.startHunting(0);
						gameController().changeState(GameState.HUNTING);
					}
				}
			});
		}
	},

	HUNTING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				gameController().manualSteering().setEnabled(true);
				level.pac().startAnimation();
				level.ghosts().forEach(Ghost::startAnimation);
				level.world().energizerBlinking().restart();
				gameController().publishGameEvent(new GameEvent(GameEventType.HUNTING_PHASE_STARTED, game, null));
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				level.simulateOneFrame();
				if (level.thisFrame().levelCompleted) {
					gameController().changeState(LEVEL_COMPLETE);
				} else if (level.thisFrame().pacKilled) {
					gameController().changeState(PACMAN_DYING);
				} else if (!level.thisFrame().pacPrey.isEmpty()) {
					level.killEdibleGhosts();
					gameController().changeState(GHOST_DYING);
				}
			});
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game) {
			gameController().manualSteering().setEnabled(false);
			timer.restartSeconds(4);
			game.level().ifPresent(GameLevel::end);
			gameController().publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.hasExpired()) {
					if (!gameController().hasCredit()) {
						gameController().changeState(INTRO);
						// attract mode -> back to intro scene
					} else if (level.intermissionNumber() > 0) {
						gameController().changeState(INTERMISSION); // play intermission scene
					} else {
						gameController().changeState(CHANGING_TO_NEXT_LEVEL); // next level
					}
				} else {
					level.pac().stopAnimation();
					level.pac().resetAnimation();
					var flashing = level.world().mazeFlashing();
					if (timer.atSecond(1)) {
						flashing.restart(2 * level.numFlashes());
					} else {
						flashing.tick();
					}
					level.pac().update(level);
				}
			});
		}
	},

	CHANGING_TO_NEXT_LEVEL {
		@Override
		public void onEnter(GameModel game) {
			gameController().manualSteering().setEnabled(false);
			timer.restartSeconds(1);
			game.nextLevel();
			gameController().publishGameEvent(GameEventType.LEVEL_STARTED);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().changeState(READY);
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1);
			game.level().ifPresent(level -> {
				level.pac().hide();
				level.ghosts().forEach(Ghost::stopAnimation);
				gameController().publishGameEvent(GameEventType.GHOST_EATEN);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().resumePreviousState();
			} else {
				game.level().ifPresent(level -> {
					var steering = level.pac().steering().orElse(gameController().steering());
					steering.steer(level, level.pac());
					level.ghosts(GhostState.EATEN, GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
							.forEach(Ghost::updateState);
					level.world().energizerBlinking().tick();
				});
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.level().ifPresent(level -> {
				level.pac().show();
				level.ghosts(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_TO_HOUSE));
				level.ghosts().forEach(Ghost::startAnimation);
			});
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game) {
			game.level().ifPresent(level -> {
				gameController().manualSteering().setEnabled(false);
				timer.restartSeconds(4);
				level.onPacKilled();
				gameController().publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
			});
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (timer.atSecond(1)) {
					level.pac().selectAnimation(Pac.ANIM_DYING);
					level.pac().resetAnimation();
					level.ghosts().forEach(Ghost::hide);
				} else if (timer.atSecond(1.4)) {
					level.pac().startAnimation();
					gameController().publishGameEvent(GameEventType.PAC_DIED);
				} else if (timer.atSecond(3.0)) {
					level.pac().hide();
					game.loseLife();
					if (game.lives() == 0) {
						level.world().energizerBlinking().stop();
					}
				} else if (timer.hasExpired()) {
					if (!gameController().hasCredit()) {
						// end of demo level
						gameController().changeState(INTRO);
					} else {
						gameController().changeState(game.lives() == 0 ? GAME_OVER : READY);
					}
				} else {
					level.world().energizerBlinking().tick();
					level.pac().update(level);
				}
			});
		}

		@Override
		public void onExit(GameModel context) {
			context.level().ifPresent(GameLevel::deactivateBonus);
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game) {
			timer.restartSeconds(1.2); //TODO not sure about exact duration
			game.updateHighScore();
			gameController().manualSteering().setEnabled(false);
			gameController().changeCredit(-1);
			gameController().publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().changeState(gameController().hasCredit() ? CREDIT : INTRO);
			}
		}

		@Override
		public void onExit(GameModel game) {
			game.setPlaying(false);
			game.removeLevel();
		}
	},

	INTERMISSION {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				gameController().changeState(gameController().hasCredit() && game.isPlaying() ? CHANGING_TO_NEXT_LEVEL : INTRO);
			}
		}
	},

	LEVEL_TEST {
		private int lastTestedLevel;

		@Override
		public void onEnter(GameModel game) {
			switch (game.variant()) {
			case MS_PACMAN:
				lastTestedLevel = 18;
				break;
			case PACMAN:
				lastTestedLevel = 20;
				break;
			default:
				break;
			}
			timer.restartIndefinitely();
			game.reset();
			game.setLevel(1);
			game.startLevel();
		}

		@Override
		public void onUpdate(GameModel game) {
			game.level().ifPresent(level -> {
				if (level.number() <= lastTestedLevel) {
					if (timer.atSecond(0.5)) {
						level.guys().forEach(Creature::show);
					} else if (timer.atSecond(1.5)) {
						level.handleBonusReached(0);
					} else if (timer.atSecond(2.5)) {
						level.bonus().ifPresent(bonus -> bonus.setEaten(120));
						gameController().publishGameEvent(GameEventType.BONUS_EATEN);
					} else if (timer.atSecond(4.5)) {
						level.handleBonusReached(1);
					} else if (timer.atSecond(5.5)) {
						level.bonus().ifPresent(bonus -> bonus.setEaten(60));
						level.guys().forEach(Creature::hide);
					} else if (timer.atSecond(6.5)) {
						var flashing = level.world().mazeFlashing();
						flashing.restart(2 * level.numFlashes());
					} else if (timer.atSecond(12.0)) {
						level.end();
						game.nextLevel();
						timer.restartIndefinitely();
						gameController().publishGameEvent(GameEventType.LEVEL_STARTED);
					}
					level.world().energizerBlinking().tick();
					level.world().mazeFlashing().tick();
					level.ghosts().forEach(Ghost::updateState);
					level.bonus().ifPresent(bonus -> bonus.update(level));
				} else {
					gameController().restart(GameState.BOOT);
				}
			});
		}

		@Override
		public void onExit(GameModel game) {
			game.clearLevelCounter();
		}
	},

	INTERMISSION_TEST {
		@Override
		public void onEnter(GameModel game) {
			timer.restartIndefinitely();
		}

		@Override
		public void onUpdate(GameModel game) {
			if (timer.hasExpired()) {
				if (gameController().intermissionTestNumber < 3) {
					++gameController().intermissionTestNumber;
					timer.restartIndefinitely();
					gameController().publishGameEvent(GameEventType.UNSPECIFIED_CHANGE);
				} else {
					gameController().intermissionTestNumber = 1;
					gameController().changeState(INTRO);
				}
			}
		}
	};

	final TickTimer timer = new TickTimer("Timer-" + name());

	GameController gameController() {
		return GameController.it();
	}

	@Override
	public TickTimer timer() {
		return timer;
	}
}