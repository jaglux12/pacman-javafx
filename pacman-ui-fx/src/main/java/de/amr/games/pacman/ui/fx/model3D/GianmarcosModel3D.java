package de.amr.games.pacman.ui.fx.model3D;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Map;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * This 3D model has been generously provided by Gianmarco Cavallaccio
 * (https://www.artstation.com/gianmart). Thanks Gianmarco!
 * 
 * @author Armin Reichert
 */
public class GianmarcosModel3D {

	public static final GianmarcosModel3D IT = new GianmarcosModel3D();

	private Map<String, MeshView> meshViewsByName;
	private Map<String, PhongMaterial> materialsByName;

	private GianmarcosModel3D() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/gianmarco/pacman.obj"));
			meshViewsByName = objImporter.getNamedMeshViews();
			materialsByName = objImporter.getNamedMaterials();
			log("MeshViews:");
			meshViewsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
			log("");
			log("Materials:");
			materialsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
			log("Pac-Man 3D model loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}
		objImporter.close();
	}

	public Group createPacMan() {
		MeshView body = new MeshView(meshViewsByName.get("Sphere_yellow_packman").getMesh());
		body.setMaterial(materialsByName.get("yellow_packman"));
		body.drawModeProperty().bind(Env.$drawMode);
		Translate centering = Model3DHelper.centerNodeOverOrigin(body);

		MeshView eyes = new MeshView(meshViewsByName.get("Sphere.008_Sphere.010").getMesh());
		eyes.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));
		eyes.drawModeProperty().bind(Env.$drawMode);
		eyes.getTransforms().add(centering);

		Group group = new Group(eyes, body);
		group.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(group, 8);
		return group;
	}

	public Group createGhost() {
		MeshView body = new MeshView(meshViewsByName.get("Sphere.004_Sphere.034").getMesh());
		body.setMaterial(materialsByName.get("blue_ghost"));
		body.drawModeProperty().bind(Env.$drawMode);
		Translate centering = Model3DHelper.centerNodeOverOrigin(body);

		MeshView eyesOuter = new MeshView(meshViewsByName.get("Sphere.009_Sphere.036").getMesh());
		eyesOuter.setMaterial(new PhongMaterial(Color.WHITE));
		eyesOuter.drawModeProperty().bind(Env.$drawMode);
		eyesOuter.getTransforms().add(centering);

		MeshView eyesInner = new MeshView(meshViewsByName.get("Sphere.010_Sphere.039").getMesh());
		eyesInner.setMaterial(new PhongMaterial(Color.BLACK));
		eyesInner.drawModeProperty().bind(Env.$drawMode);
		eyesInner.getTransforms().add(centering);

		Group group = new Group(body, eyesOuter, eyesInner);
		group.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Model3DHelper.scaleNode(group, 8);
		return group;
	}

	public static void main(String[] args) {
		new GianmarcosModel3D();
	}
}
