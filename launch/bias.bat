set LAUNCHER_UPDATE_FILE="update_bias.jar"

@if exist "%cd%\%LAUNCHER_UPDATE_FILE%" (
move "%cd%\%LAUNCHER_UPDATE_FILE%" "%cd%\bias.jar"
@echo "Launcher update has been applied."
)

SET JAVA=java

@if not '%JAVA_HOME%' == '' SET JAVA="%JAVA_HOME%\bin\java"

"%JAVA%" -cp "%cd%\bias.jar;%cd%\appcore.jar;%cd%\lib\*;%cd%\addons\*" bias.Bias

