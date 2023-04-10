/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class Section3D extends Section {

	private final ComboBox<Perspective> comboPerspective;
	private final CheckBox cbEnergizerExplodes;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final ColorPicker pickerLightColor;
	private final ComboBox<String> comboFloorTexture;
	private final ColorPicker pickerFloorColor;
	private final CheckBox cbPacLighted;
	private final CheckBox cbPacNodding;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public Section3D(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.d3_perspectivePy.set(comboPerspective.getValue()));
		addInfo("Camera", () -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.camInfo() : "")
				.available(() -> gameScene().is3D());
		pickerLightColor = addColorPicker("Light color", Env.d3_lightColorPy.get());
		pickerLightColor.setOnAction(e -> Env.d3_lightColorPy.set(pickerLightColor.getValue()));
		sliderWallHeight = addSlider("Wall height", 0.1, 10.0, Env.d3_mazeWallHeightPy.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallHeightPy.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall thickness", 0.1, 2.0, Env.d3_mazeWallThicknessPy.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallThicknessPy.set(newVal.doubleValue()));
		comboFloorTexture = addComboBox("Floor texture", textureItems());
		comboFloorTexture.setOnAction(e -> Env.d3_floorTexturePy.set(comboFloorTexture.getValue()));
		pickerFloorColor = addColorPicker("Floor color", Env.d3_floorColorPy.get());
		pickerFloorColor.setOnAction(e -> Env.d3_floorColorPy.set(pickerFloorColor.getValue()));
		cbEnergizerExplodes = addCheckBox("Energizer Explosion", () -> Ufx.toggle(Env.d3_energizerExplodesPy));
		cbPacLighted = addCheckBox("Pac-Man lighted", () -> Ufx.toggle(Env.d3_pacLightedPy));
		cbPacNodding = addCheckBox("Pac-Man nodding", () -> Ufx.toggle(Env.d3_pacNoddingPy));
		cbAxesVisible = addCheckBox("Show axes", () -> Ufx.toggle(Env.d3_axesVisiblePy));
		cbWireframeMode = addCheckBox("Wireframe mode", Actions::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(Env.d3_perspectivePy.get());
		sliderWallHeight.setValue(Env.d3_mazeWallHeightPy.get());
		comboFloorTexture.setValue(Env.d3_floorTexturePy.get());
		cbEnergizerExplodes.setSelected(Env.d3_energizerExplodesPy.get());
		cbPacLighted.setSelected(Env.d3_pacLightedPy.get());
		cbPacNodding.setSelected(Env.d3_pacNoddingPy.get());
		cbAxesVisible.setSelected(Env.d3_axesVisiblePy.get());
		cbWireframeMode.setSelected(Env.d3_drawModePy.get() == DrawMode.LINE);
	}

	private String[] textureItems() {
		var textureKeys = AppResources.textureKeys();
		var items = new String[textureKeys.length + 1];
		items[0] = AppResources.KEY_NO_TEXTURE;
		System.arraycopy(textureKeys, 0, items, 1, textureKeys.length);
		return items;
	}
}