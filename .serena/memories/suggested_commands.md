# Windows commands

PowerShell repository root: `C:\Users\itsna\Desktop\gym`.

- Scope supported Java for the shell: `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'`.
- Unit tests: `rtk test .\gradlew.bat testDebugUnitTest`.
- Lint + APK: `rtk test .\gradlew.bat lintDebug assembleDebug`.
- Device tests: `rtk test .\gradlew.bat connectedDebugAndroidTest`.
- Device inventory (ADB is not on PATH): `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices -l`.
- Install debug APK: `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk`.
- Git commands use RTK, e.g. `rtk git status`, `rtk git diff`.
- Search uses `rtk rg`; listing uses PowerShell `Get-ChildItem -Name`.