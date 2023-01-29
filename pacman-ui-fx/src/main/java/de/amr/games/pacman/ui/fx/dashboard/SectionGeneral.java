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
package de.amr.games.pacman.ui.fx.dashboard;

import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * General settings.
 * 
 * @author Armin Reichert
 */
public class SectionGeneral extends Section {

	public static final int MIN_FRAMERATE = 5;
	public static final int MAX_FRAMERATE = 120;

	private Button[] btnsSimulation;
	private Spinner<Integer> spinnerSimulationSteps;
	private Slider sliderTargetFPS;
	private CheckBox cbUsePlayScene3D;
	private CheckBox cbDebugUI;
	private CheckBox cbTimeMeasured;
	private ImageView iconPlay = new ImageView(Ufx.image("icons/play.png"));
	private ImageView iconStop = new ImageView(Ufx.image("icons/stop.png"));
	private ImageView iconStep = new ImageView(Ufx.image("icons/step.png"));
	private Tooltip tooltipPlay = new Tooltip("Play");
	private Tooltip tooltipStop = new Tooltip("Stop");
	private Tooltip tooltipStep = new Tooltip("Single Step Mode");
	private Slider sliderPiPSceneHeight;
	private Slider sliderPiPOpacity;
	private final ColorPicker pickerBgColor;

	public SectionGeneral(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);

		btnsSimulation = addButtonList("Simulation", "Pause", "Step(s)");
		Button btnPlayPause = btnsSimulation[0];
		Button btnStep = btnsSimulation[1];

		btnPlayPause.setText(null);
		btnPlayPause.setStyle("-fx-background-color: transparent");
		btnPlayPause.setOnAction(e -> Actions.togglePaused());

		btnStep.setGraphic(iconStep);
		btnStep.setStyle("-fx-background-color: transparent");
		btnStep.setText(null);
		btnStep.setTooltip(tooltipStep);
		btnStep.setOnAction(e -> ui.gameLoop().nsteps(Env.simulationStepsPy.get(), true));

		spinnerSimulationSteps = addSpinner("Num Steps", 1, 50, Env.simulationStepsPy.get());
		spinnerSimulationSteps.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.simulationStepsPy.set(newVal.intValue()));

		addInfo("Total Updates", ui.gameLoop()::getUpdateCount);

		sliderTargetFPS = addSlider("Target Framerate", MIN_FRAMERATE, MAX_FRAMERATE, 60);
		sliderTargetFPS.setShowTickLabels(false);
		sliderTargetFPS.setShowTickMarks(false);
		sliderTargetFPS.valueProperty()
				.addListener((obs, oldValue, newValue) -> Env.targetFrameratePy.set(newValue.intValue()));

		addInfo("",
				() -> String.format("%d Hz (Target: %d Hz)", ui.gameLoop().getFPS(), ui.gameLoop().getTargetFramerate()));

		addInfo("Main scene", () -> String.format("w=%.0f h=%.0f", ui.mainScene().getWidth(), ui.mainScene().getHeight()));

		pickerBgColor = addColorPicker("Background color", Env.mainSceneBgColorPy.get());
		pickerBgColor.setOnAction(e -> Env.mainSceneBgColorPy.set(pickerBgColor.getValue()));

		sliderPiPSceneHeight = addSlider("PiP Size", ui.pipView().minSize().y(), ui.pipView().maxSize().y(),
				Env.PiP.sceneHeightPy.get());
		sliderPiPSceneHeight.valueProperty()
				.addListener((obs, oldValue, newValue) -> Env.PiP.sceneHeightPy.set(newValue.doubleValue()));

		sliderPiPOpacity = addSlider("PiP Transparency", 0.0, 1.0, Env.PiP.opacityPy.get());
		sliderPiPOpacity.valueProperty()
				.addListener((obs, oldValue, newValue) -> Env.PiP.opacityPy.set(newValue.doubleValue()));

		cbUsePlayScene3D = addCheckBox("Use 3D play scene", Actions::toggleUse3DScene);
		cbDebugUI = addCheckBox("Show UI Debug Stuff", () -> Ufx.toggle(Env.showDebugInfoPy));
		cbTimeMeasured = addCheckBox("Measure time", () -> Ufx.toggle(Env.timeMeasuredPy));
	}

	@Override
	public void update() {
		super.update();
		btnsSimulation[0].setGraphic(Env.pausedPy.get() ? iconPlay : iconStop);
		btnsSimulation[0].setTooltip(Env.pausedPy.get() ? tooltipPlay : tooltipStop);
		btnsSimulation[1].setDisable(!Env.pausedPy.get());
		spinnerSimulationSteps.getValueFactory().setValue(Env.simulationStepsPy.get());
		sliderTargetFPS.setValue(Env.targetFrameratePy.get());
		sliderPiPSceneHeight.setValue(Env.PiP.sceneHeightPy.get());
		sliderPiPOpacity.setValue(Env.PiP.opacityPy.get());
		cbUsePlayScene3D.setSelected(Env.ThreeD.enabledPy.get());
		cbTimeMeasured.setSelected(Env.timeMeasuredPy.get());
		cbDebugUI.setSelected(Env.showDebugInfoPy.get());
	}
}