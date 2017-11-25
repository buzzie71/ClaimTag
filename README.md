ClaimTag
========

This plugin enables players to tag certain other players designated as runners and obtain a prize for doing so.

===
In-Game Config Changing and Setup
===

`/ct` will bring up the current config options.  These can be altered in-game with:

`/ct [debug-mode|runner-tag|suppress-alerts] [true|false]`

Runners can be added or removed from the list with:

`/ct addrunner <playername>` or `/ct delrunner <index>`

The prize for tagging a particular player can be set by holding it and running:

`/ct setprize <runnername>`

Copies of the prizes can be obtained for inspection with `/ct getprize <runnername>`.  The prize is given only if inventory is not full.

Configuration changes can be saved to file with `/ct save` and reloaded from file with `/ct reload`.

These commands are intended to be reserved for server administrators.

An additional command, `/runners`, is intended for all players to run, to see who is on the list and who they have not yet tagged.

===
Tagging mechanics
===

To tag a runner, a player needs only run up to them and punch them.  If they have not tagged the runner before and their inventory has an empty slot, they will receive the prize set for the runner.  Further attempts to tag the runner will result in an alert mentioning that they have tagged the runner and should find someone else.  Tagging will fail if the player has a full inventory when attempting to tag the runner.  The prize will not be given but the player will be able to tag the runner for a prize later, after they have cleared some space in their inventory to receive it.