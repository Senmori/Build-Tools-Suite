# Build-Tools-Suite
This program aims to be a replacement for the current BuildTools implementation.

Ideally, a user should be able to open the program and hit 'Run BuildTools' without changing anything and
it will produce the latest version of Spigot available.

Users also have several options that are currently available in BuildTools with one key difference.
This program can output the final jar to several different directories.

## Build Options

**Disable Certificate Check**: This options disables certificate checks when polling websites.

**Don't Update**: Do not update the version of the jar being made. This will result in the same mappings being used again.

**Skip Compile**: Do not compile any jars. This will only update the repositories.

**Generate Sources**: Generate source jars.

**Generate Documentation**: Generate javadoc jars.

**Invalidate Cache**: Delete all directories in the working directory of BuildToolsSuite.
- This should only be used if you encounter a problem.

**Versions**: Select a version from the dropdown list. If not version is selected this defaults to the default version
as specifiied in the settings.

**Add Output Directory**: Add an output directory for the final jars.
If no directories are added, the working directory of BuildToolsSuite is used.

**Run BuildTools**: Runs BuildToolsSuite.

To update the versions (in case a new version of Spigot/Bukkit is released), simply
check the ```Update Versions``` checkbox and click the button that shows up.


## Minecraft Options

**ReleaseType**: is the release type of minecraft jars. The valid options are:
  - Snapshot, _Release_, Old Beta, and Old Alpha. _Release_ is the default.
 
**Version**: The version of minecraft to download.
 
**Release Date**: The date that version of minecraft was released.
  - The date format is [ISO_OFFSET_DATE_TIME](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_OFFSET_DATE_TIME)

**SHA-1**: The sha-1 hash of the minecraft server jar if you want to verify the integrity of the download.

The update versions process for minecraft is the same as it is for Spigot versions.
Simply check the ```Update Versions``` checkbox and then click the button that shows up.

## Technical Limitations

Do as much work off the application thread as possible.

All new tabs must have their own controller. See current fxml controllers for examples.
UI is built with SceneBuilder, use it to make things easier for everyone.


## TODO

Create Tab for settings.
 - Edit/Save settings.
 - Cannot Save while building a project.
 - Input verification for certain fields (directories)

Compile project specific jars, like CraftBukkit OR Spigot.

Implement some kind of versioning system to talk with Spigot to tell when BuildToolSuite is out of date.
- Notify when newer version is available *if* connected to internet. Otherwise no alert.
