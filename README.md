ClaimTag
========

This plugin enables players to tag certain other players designated as runners and obtain a prize for doing so.

===
In-Game Config Changing and Setup
===

`/ct` will bring up the current config options.  These can be altered in-game with:

`/ct [debug-mode|verbose-debug|runner-tag|suppress-broadcasts|tagging-announcements] [true|false]`

Runners can be added or removed from the list with:

`/ct addrunner <playername>` or `/ct delrunner <index>`

The prize for tagging a particular player can be set by holding it and running:

`/ct setprize <runnername>`

Copies of the prizes can be obtained for inspection with:

`/ct getprize <runnername>`.  

The prize is given only if inventory is not full.

A color-coded list of runners for a particular player can be seen with:

`/ct viewtagged <playername>`

Green denotes a runner that the player has tagged; red denotes a runner that the player has not tagged.

Configuration changes can be saved to file with `/ct save` and reloaded from file with `/ct reload`.

These commands are intended to be reserved for server administrators.

An additional command, `/runners`, is intended for all players to run, to see who is on the list and who they have not yet tagged.  The runner names will be color-coded according to whether or not the player running the command has tagged them (green for tagged, red for not tagged).

===
Tagging mechanics
===

To tag a runner, a player needs only run up to them and punch them.  If they have not tagged the runner before and their inventory has an empty slot, they will receive the prize set for the runner.  Further attempts to tag the runner will result in an alert mentioning that they have tagged the runner and should find someone else.  Tagging will fail if the player has a full inventory when attempting to tag the runner.  The prize will not be given but the player will be able to tag the runner for a prize later, after they have cleared some space in their inventory to receive it.

===
Changelog
===

Known issues and feature requests:
* Prize item is not dispensed when the original item stack used to set the prize on the command runner is combined with another or split up.  (This can be mitigated by having the prize be set by someone else.)
* A player's tagging status will sometimes desync with the config, eg. when a runner is removed and then re-added after tagging.  (`/ct clearmeta <player>` will clear metadata on a player so tagging status can be drawn straight from the config, but it is not the most elegant fix.  Care during setup will mitigate this.)

0.0.7:
* Fixed configuration booleans not actually being saved to file on server shutdown or `/ct save`.
* Fixed runner list not being updated properly when reloading an altered configuration file.
* Fixed `/delrunner` not removing the specified runner's associated UUID list in the config file, if it exists.
* Added labels to all debug messages from this plugin.  [CT-2] denotes basic debug messages related to player/administrator use of the plugin, while [CT-1] denotes debug messages that relate more to the plugin code's operations.
* Added `verbose-debug` config option that can be set to true or false with `/ct verbose-debug <true|false>`.  If true, all debug messages labeled with [CT-1] will be displayed in the console.  If false, these will be hidden from view.  (Note that `debug-mode false` will hide both [CT-1] and [CT-2] messages regardless of `verbose-debug`.)
* Added `tagging-announcements` config option that can be set to true or false with `/ct tagging-announcements <true|false>`.  If true, the server will broadcast `[CT] <player> has tagged <runner>!` in green to all online players when a player tags a runner.  If false, this announcement will not be displayed.  Note that, while this is a server broadcast, this will not be toggled with `suppress-broadcasts`.
* Added `/ct viewtagged <player>`.  This command will allow administrators and others with permission to use `/ct` commands to see who a player has tagged or not tagged.  Runner names are color-coded similarly for a player who runs `/runners` - green denotes runners the specified player has tagged, while red denotes runners the specified player has not tagged.  Information will be reported to logs if debug-mode is enabled.
* Added `/ct clearmeta <player>`.  This command will clear the metadata on a player.  Metadata is used to keep track of which runners a player has tagged, but can be desynced from the config if a runner is removed after metadata on a player is set; clearing the metadata will require it to be reset based on current config data.  Runners whose associated metadata were cleared from the player will be listed.
* Fixed (hopefully) the ConcurrentModificationException that occasionally happens when removing a runner.
* `/ct setprize <player>` will now report the custom name of the prize item in console, and `null` if the prize item has no custom name.

0.0.6: 
* Updated wording across the plugin and README so the configuration option to disable broadcasts going out to the server (eg. on event-tag enable or disable) is now listed as `suppress-broadcasts` and not `suppress-alerts`.
* Fixed UUIDs being listed multiple times in the config when tagging a runner.
* Fixed players being able to obtain the prize from tagging a runner multiple times.
* Added a sound effect that plays for players when they tag a runner under any circumstance (not tagged and received prize, not tagged but inventory full, already tagged).
* Added a personal notification for runners whenever someone tags them.

0.0.5: 
* Initial release.