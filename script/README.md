Eclipse Phase 1E Companion Script
=================================

This is a companion script to the [Eclipse Phase 1E sheet](https://github.com/Bathtor/EPSheet) for use with the API on [Roll20](http://roll20.net). The vast majority of its features will not work with any other sheet and no such support is planned/considered.

**Note**: If you are using with Roll20's 1-Click install, be aware that sometimes inconsistent updates between the sheet and the script can leave things not working, and you have little to no control over this (and neither have I, really). If you want a more reliable experience, install both sheet and script manually instead of using Roll20's automatic systems.

Contents
--------
<!-- run `doctoc README.md` to update this file -->
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Command Overview](#command-overview)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation-1)
    - [EPCompendium](#epcompendium)
    - [Updating](#updating)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Command Overview
----------------
In this section we give a quick overview over all available commands. They describe in detail in later individual sections.

**Note**: You can always run `!<cmd> --help` in Roll20 chat to get a quick description of the options available for each `<cmd>`.


- **Battle Manager** (`!epbattleman`) manages Action Turns and Action Phases based on character speed, as well as rerolls iniative for everyone at the beginning of a new turn automatically.
- **Character Cleaner** (`!epclean`) resets or empties certain sections on the character sheet, in order to automate backups, or egocasts.
- **Character Tools** (`!epchar`) applies damage, wounds, stress, and traumas automatically, taking armour into account as appropriate.
- **GM Tools** (`!epgmtools`) can be configured to show skills for a whole party, in order to easily roll Perception checks, for example.
- **Group Rolls** (`!epgroup-roll`) can be used to roll EP specific common rolls like *Fray/2*, *Initiative*, or simply an arbitrary skill check, for a group of selected tokens.
- **Special Rolls** (`!epspecialroll`) is a script that manages rolling common EP rolls, without involving a character sheet. Convenient for the improvising GM, who doesn't have an NPC ready to import, but still needs it to shoot something, for example.
- **Token Setup** (`!eptoken`) generate token abilities for skills, *Fray/2*, or *Initiative*.
- **Compendium** manages data from the [Eclipse Phase Compendium](https://github.com/Bathtor/EPCompendium):
	- *Data* (`!epcompendium-data`) manages search, lookups, and display of compendium data to the chat.
	- *Export* (`!epcompendium-export`) can export custom morphs in a Compendium-compatible format, so they can be imported into other character sheets.
	- *Import* (!epcompendium-import) manages character, trait, morph, and item imports.

Installation
------------

### Prerequisites

- You must be a **Pro** subscriber of Roll20 to use API scripts in general, and this script in particular.
- You'll need a recent version of the [Eclipse Phase 1E sheet](https://github.com/Bathtor/EPSheet). I'll try to be clear in the documentation, which script version works with which sheet version, but generally the latest release of each should always work together. And if you picked the wrong versions, it should complain and not break anything.

### Installation

- Open the **raw** text of the latest release of the script. At the time of writing that is [v0.9.0](https://github.com/Bathtor/EPSheet/releases/download/script-v0.9.0/ep-script.js).
- Copy *all* the text -- **Ctrl+A** followed by **Ctrl+C** (**Cmd+A**, **Cmd+C** on a Mac)
- Go to the API Script page for your Roll20 campaign
- Paste the text into a new script, or override an older version if updating. Don't forget to give it a sensible name, e.g. `epscript.js`!
- Save the script
- Leave the Roll20 API Script page open, and open up your campaign view in a different tab/window. That should cause your sandbox to spin up. If no errors are reported, you should be good to go.

#### EPCompendium
If you would like access to the EPCompendium-related features, you must also install the data script for the compendium. Follow the same procedure as above for the latest release of the EPCompendium, [v4.0.0](https://github.com/Bathtor/EPCompendium/releases/tag/v4.0.0) at the time of writing. Each Compendium release also contains a number of macros prepared to use with the script that you can just c&p into your campaign.

#### Updating
If you need to update one of the script installed above, simply follow the same instructions as for a normal installation. Do make sure, that you always override old script versions, and never have two versions running in parallel!

