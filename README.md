ClaimTag
========

This plugin enables players to tag certain other players designated as runners and obtain a prize for doing so.

===
In-Game Config Changing and Setup
===

`/ct` will bring up the current config options.  These can be altered in-game with:

`/ct [debug-mode|runner-tag|suppress-broadcasts] [true|false]`

Runners can be added or removed from the list with:

`/ct addrunner <playername>` or `/ct delrunner <index>`

The prize for tagging a particular player can be set by holding it and running:

`/ct setprize <runnername>`

Copies of the prizes can be obtained for inspection with:

`/ct getprize <runnername>`.  

The prize is given only if inventory is not full.

Configuration changes can be saved to file with `/ct save` and reloaded from file with `/ct reload`.

These commands are intended to be reserved for server administrators.

An additional command, `/runners`, is intended for all players to run, to see who is on the list and who they have not yet tagged.

===
Tagging mechanics
===

To tag a runner, a player needs only run up to them and punch them.  If they have not tagged the runner before and their inventory has an empty slot, they will receive the prize set for the runner.  Further attempts to tag the runner will result in an alert mentioning that they have tagged the runner and should find someone else.  Tagging will fail if the player has a full inventory when attempting to tag the runner.  The prize will not be given but the player will be able to tag the runner for a prize later, after they have cleared some space in their inventory to receive it.

===
Changelog
===

Known issues and feature requests:
* Prize item is not dispensed when the original item stack used for `/setprize` is combined with another or split up.
* `/delrunner` does not cleanly remove runners from the list across restarts.
* `/delrunner` does not remove the runners' previous list of UUIDs in config.
* Debug messages are too verbose for the purpose of logging player activity (eg. in case of modreq because player tagged a runner but got no prize).  Add another level of debug notifications that condenses this a bit.
* Add command to list which runners a specified player has tagged/not tagged.
* Add a broadcast when a player tags a runner, and toggle that with a config option separate from `suppress-broadcasts`.

0.0.6: 
* Updated wording across the plugin and README so the configuration option to disable broadcasts going out to the server (eg. on event-tag enable or disable) is now listed as `suppress-broadcasts` and not `suppress-alerts`.
* (Untested) Fixed players not being set with the correct metadata on tagging a player.  (This should alleviate the problems of UUIDs being listed multiple times in the config as well as players being able to obtain the prize from tagging a runner multiple times.)
* Added a sound effect that plays for players when they tag a runner under any circumstance (not tagged and received prize, not tagged but inventory full, already tagged).
* Added a personal notification for runners whenever someone tags them.

0.0.5: 
* Initial release.