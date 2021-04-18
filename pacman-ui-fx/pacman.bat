:: Usage: pacman.bat or pacman.bat -mspacman
@ECHO OFF
@SETLOCAL
SET "JAVA_HOME=C:\Program Files\Java\jdk-15.0.2"
SET "JFX_LIB=C:\Program Files\Java\javafx-sdk-15.0.1\lib"
SET "PACMAN_LIB=..\..\pacman-basic\pacman-core\target\pacman-core-1.0.jar"
SET "PACMAN_UI_FX=.\target\pacman-ui-fx-1.0.jar"
SET CLASSPATH="%PACMAN_LIB%;%PACMAN_UI_FX%;%JFX_LIB%\javafx.base.jar;%JFX_LIB%\javafx.controls.jar;%JFX_LIB%\javafx.media.jar;..\interactivemesh\jars\jimObjModelImporterJFX.jar"
::"%JAVA_HOME%"\bin\java.exe --module-path "%JFX_LIB%" --add-modules javafx.controls -cp %CLASSPATH% de.amr.games.pacman.ui.fx.app.PacManGameAppFX %*



"C:\Program Files\Java\jdk-15.0.2\bin\javaw.exe" -Dfile.encoding=UTF-8 -p "C:\Users\armin\git\pacman-javafx\pacman-ui-fx\target\classes;C:\Users\armin\git\pacman-javafx\interactivemesh\jars\jimObjModelImporterJFX.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1-win.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1-win.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1-win.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1-win.jar;C:\Users\armin\git\pacman-basic\pacman-core\target\classes" -classpath "C:\Users\armin\.m2\repository\org\openjfx\javafx-controls\15.0.1\javafx-controls-15.0.1.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-graphics\15.0.1\javafx-graphics-15.0.1.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-base\15.0.1\javafx-base-15.0.1.jar;C:\Users\armin\.m2\repository\org\openjfx\javafx-media\15.0.1\javafx-media-15.0.1.jar" -m de.amr.games.pacman.ui.fx/de.amr.games.pacman.ui.fx.app.PacManGameAppFX



@ENDLOCAL