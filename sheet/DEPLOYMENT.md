Deployment Procedures
=====================

Development
-----------
Just copy and paste the following three files into their respective fields in the target campaign's settings:

- `ep-sheet.html`
- `ep-sheet.css`
- `translation.json`

Since doing this manually can get tiresome, you can do the same thing from within SBT via the `submit` and `submitFull` tasks. For this to work you must configure the file `assemble.sc` correctly using the values from your campaign to fill in the `campaignId` field in the `Roll20Config` object. Additionally, you need to log in with a browser and then copy&paste your `racksessionid` into the file.

Release Candidates
------------------
To prepare a release candidate, start by tagging the appropriate commit as some version vX.Y.Z and push that tag to the `master` branch.

Then run a `submitFull` task and copy the build product files (same as above) over into your [fork](https://github.com/Bathtor/roll20-character-sheets) of the [roll20-character-sheets repository](https://github.com/Roll20/roll20-character-sheets), preferably after merging in any upstream changes, though there should rarely be any conflicts.
Commit and push these new changes to the fork's master.

This being done, create a stabilisation tracking issue with the title *Tracking Issue for Sheet vX.Y.Z-rc Stabilisation Phase* using the following template:
```markdown
Stablisation tracking issue for <version with link to tag>.

Stabilisation phase is 4 playing sessions without blocking issues.

- [ ] Session 1 (<date>)
- [ ] Session 2 (<date>)
- [ ] Session 3 (<date>)
- [ ] Session 4 (<date>)
```
Then label the issue as *stabilising*.

Then after every playing session, if there were no issues check the box and add the date.
If there were issues fix them, repeat the procedures above and update the tracking issue to point to the new version. Don't start the session count from 0, though. Simply discard the session where the issue occurred.

Roll20 Releases
---------------
Once the session counter in the a stabilisation tracking issue is full, simply do a PR from your fork to Roll20 master, potentially merging in upstream changes again, if necessary/desired.