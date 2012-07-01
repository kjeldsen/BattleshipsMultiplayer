# Battleships Multiplayer

![Battleships](https://github.com/omegahm/DBP2P/raw/master/Battleships/res/drawable/icon.png)

### Get the game
The game is available free of charge and ad-free as [Battleships Multiplayer in the Android Market (Google Play)](https://market.android.com/details?id=dk.hotmovinglobster.battleships&feature=search_result).

No data is collected by the application and stored centrally, and the privilleges requested by the application are as minimal as possible.

### Background
The original [Battleships](https://github.com/omegahm/DBP2P/tree/master/Battleships) game came to be as a way of showcasing the Dusty Tuba library for facilitating easy and secure bluetooth communication on Android, that I and two other students at University of Copenhagen created as a part of a CS course called "Project Couse: Development Studio".
No development on the game has happened since the conclusion of that course. Thus the game has received no updates or bugfixes since it was published on the Android Market (Google Play). To ensure a good experience for Android users I felt that the time had come doing something drastical:
* One option was to spend quite some time tryng to fix bugs in the DustyTuba library, however this wouldn't fix the hassle of first time BT pairing that has to happen on legacy Android devices. Giving the users a sub-par experience to showcase a library project that never took off didn't seem like the right thing to do. and thus leaving the user experience sub par,
* Another option (and very easy solution) would be to simply pull the game from the Android Market and have some of the many competitors fill the gap.
* The last option was to update the game, by ditching the use of the DustyTuba library and using Bluetooth as the underlying transport.

### The major overhaul
After an introduction to Qualcomm's Alljoyn technology at #iohack after #io12, I decided to try to spend some time improving upon Battleships Multiplayer.

The overhaul happening right now aims to have two ways of establishing a connection between clients:
* Alljoyn, for connecting to any client in close proximity which has Wifi enabled
* Bump!, for connecting to a specific device over either a cellular network or Wifi.

### Future
With the removal of Dusty Tuba and Bluetooth a lot of the issues that the users are reporting should disappear.
One obvious extension to the game, that I would love to get in there, would be the introduction of a single player mode.

### Improvements and collaboration
The goal is to provide a free Battleships game that is fun for users to try out. Since my time is extremely limited, you're very welcome to welcome to submit a pull-request if you have any changes you'd like to see.