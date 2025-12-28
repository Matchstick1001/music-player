<#
	Consolidated setup script (DO NOT RUN automatically).

	Purpose:
	- Optionally remove existing JDK installations (interactive).
	- Install a supported JDK (Temurin / Eclipse Adoptium JDK 17) and set it globally.
	- Download and install Gradle (8.6) to C:\GRADLE and add it to PATH.
	- Generate the project Gradle wrapper (`gradle wrapper --gradle-version 8.6`).

	Notes / Safety:
	- This script contains the necessary commands but will NOT be executed by me.
	- Running the uninstall/remove steps is potentially destructive; review before executing.
	- Administrative privileges are required for system-wide installs and `setx /M` operations.
	- Adjust variables below before running on your machine.
#>

## Configuration - adjust as needed
$DesiredJdkMajor = 17
$DesiredJdkFolder = "C:\Program Files\Java\jdk-$DesiredJdkMajor"
$GradleVersion = '8.6'
$GradleDir = "C:\GRADLE\gradle-$GradleVersion"
$UseWinget = $true    # set to $false to use direct download + msiexec

Write-Host "[INFO] Script prepared to install JDK $DesiredJdkMajor and Gradle $GradleVersion"

## 1) Detect existing JDK installations
Write-Host "\n[STEP] Detecting installed JDKs under 'C:\Program Files\Java'..."
$installedJdks = @()
if (Test-Path 'C:\Program Files\Java') {
	$installedJdks = Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue | ForEach-Object {
		@{ Name = $_.Name; Path = $_.FullName }
	}
}
if (-not $installedJdks) { Write-Host "  No JDK directories found." } else { $installedJdks | Format-Table }

## 2) Interactive removal of existing JDKs (uncomment to enable)
<#
foreach ($j in $installedJdks) {
	Write-Host "\nFound JDK: $($j.Name) at $($j.Path)"
	$confirm = Read-Host "Remove this JDK folder? (Y/N)"
	if ($confirm -match '^[Yy]') {
		Write-Host "  -> Removing $($j.Path) (requires admin)"
		# Remove-Item -Recurse -Force -Path $j.Path
	}
}
#>

## 3) Install JDK 17 (Temurin) - preferred methods
Write-Host "\n[STEP] Preparing JDK $DesiredJdkMajor installation commands (not executed)"
if ($UseWinget -and (Get-Command winget -ErrorAction SilentlyContinue)) {
	Write-Host "  Winget is available. Recommended command to install Temurin $DesiredJdkMajor:"
	Write-Host "    winget install --id Eclipse.Adoptium.Temurin.$DesiredJdkMajor -e --silent"
} else {
	Write-Host "  Winget not available or disabled. Use direct MSI download and msiexec. Example (update URL as needed):"
	Write-Host "    $msiUrl = 'https://github.com/adoptium/temurin17-binaries/releases/latest/download/OpenJDK17U-jdk_x64_windows_hotspot.msi'"
	Write-Host "    Invoke-WebRequest -Uri $msiUrl -OutFile C:\Temp\temurin17.msi"
	Write-Host "    Start-Process -FilePath msiexec.exe -ArgumentList '/i','C:\Temp\temurin17.msi','/qn','/norestart' -Wait -NoNewWindow"
}

Write-Host "\n[STEP] After installation, run as admin to set global JAVA_HOME and update PATH:"
Write-Host "  setx /M JAVA_HOME \"$DesiredJdkFolder\""
Write-Host "  setx /M PATH \"%PATH%;$DesiredJdkFolder\\bin;$GradleDir\\bin\""

## 4) Download & extract Gradle $GradleVersion (binary-only) to C:\GRADLE
Write-Host "\n[STEP] Gradle presence check and notes (no download performed):"
if (Test-Path $GradleDir) {
	Write-Host "  Found Gradle installation at: $GradleDir"
} else {
	Write-Host "  Gradle not found at $GradleDir. You already downloaded a Gradle distribution earlier; if it's located elsewhere, update the `\$GradleDir` variable."
	Write-Host "  If you need to download manually, these are the steps (not executed):"
	Write-Host "    $gradleZip = \"https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip\""
	Write-Host "    Invoke-WebRequest -Uri $gradleZip -OutFile C:\GRADLE\gradle-$GradleVersion-bin.zip"
	Write-Host "    Expand-Archive -Path C:\GRADLE\gradle-$GradleVersion-bin.zip -DestinationPath C:\GRADLE -Force"
}

Write-Host "  Compatibility note: Gradle $GradleVersion works with JDK 17 or 21 for Android/AGP workflows. Avoid using very new JDKs (e.g. Java 25) — they may produce class files newer than Gradle/Groovy can handle."

## 5) Generate project Gradle wrapper (uses gradle on PATH or the installed gradle)
Write-Host "\n[STEP] Generate project wrapper in project root (run after installing Gradle and setting JAVA_HOME):"
Write-Host "  cd 'C:\Users\hruth\Documents\music player'"
Write-Host "  C:\GRADLE\gradle-$GradleVersion\bin\gradle wrapper --gradle-version $GradleVersion"
Write-Host "  .\gradlew --version"

## 6) Troubleshooting notes
Write-Host "\n[Troubleshooting] If you see native library / jansi errors, try these options:" 
Write-Host " - Ensure JAVA_HOME points to a supported JDK (17 or 21)."
Write-Host " - Remove or fix permissions on C:\Users\<you>\.gradle\native or use GRADLE_USER_HOME to a writable path."
Write-Host " - Use an alternate GRADLE_USER_HOME for the wrapper generation: `$env:GRADLE_USER_HOME='C:\temp\.gradle'`"

Write-Host "\n[FINAL] This file contains all commands you requested. Review and run manually as admin when ready."
