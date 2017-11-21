ClaimTag
========

This plugin enables players to tag certain other players designated as runners and obtain a prize for doing so.

===
In-Game Config Changing
===

`/ct` will bring up the current config options.  These can be altered in-game with:

`/ct [debug-mode|runner-tag|suppress-alerts] [true|false]`

Runners can be added or removed from the list with:

`/ct addrunner <playername>` or `/ct delrunner <index>`

and the prize for tagging a particular player can be set by holding it and running:

`/ct setprize <runnername>`

These commands are intended to be reserved for server administrators.

An additional command, `/runners`, is intended for all players to run, to see who is on the list and who they have not yet tagged.