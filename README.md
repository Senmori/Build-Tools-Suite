# Build-Tools-Suite
This program aims to be a replacement for the current BuildTools implementation.

Ideally, a user should be able to open the program and hit 'Run BuildTools' without changing anything and
it will produce the latest version of Spigot available.

Users also have several options that are currently available in BuildTools with one key difference.
This program can output the final jar to several different directories.

## Options

**Disable Certificate Check**: This options disables certificate checks when polling websites.

**Don't Update**: Do not update the version of the jar being made. This will result in the same mappings being used again.

**Skip Compile**: Do not compile any jars. This will only update the repositories.

**Generate Sources**: Generate source jars.

**Generate Documentation**: Generate javadoc jars.

**Versions**: Select a version from the dropdown list. If not version is selected this defaults to the default version
as specifiied in the settings.

**Add Output Directory**: Add an output directory for the final jars.
If no directories are added, the working directory of BuildToolsSuite is used.

**Run BuildTools**: Runs BuildTools.


## Technical Limitations

Do as much work off the application thread as possible.

All new tabs must have their own controller. See current fxml controllers for examples.
UI is built with SceneBuilder, use it to make things easier for everyone.


## TODO

ProgressBar for BuildTools.
Splash Screen

Create Tab for settings.
 - Edit/Save settings.
 - Cannot Save while building a project.
 - Input verification for certain fields (directories)

Compile project specific jars, like CraftBukkit OR Spigot.

Create Tab for downloading certain Minecraft Jars.
 - Use new launchermeta link.
 - Cache versions (up to what version?)
     - Only cache what a user has downloaded?
 - Choose between client/server?

Implement some kind of versioning system to talk with Spigot to tell when BuildToolSuite is out of date.
- Notify when newer version is available *if* connected to internet. Otherwise no alert.
