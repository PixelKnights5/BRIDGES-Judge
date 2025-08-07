# BRIDGES Judge
*For Minecraft 1.21.8 and Fabric 0.131.0*

This is a small utility mod to help judge a BRIDGES game.

## Dependencies
- Minecraft 1.21.1
- Fabric 0.110.0
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin) 

## Installation
Copy the mod jar from releases and all dependencies into your `mods` folder

## Usage

1. Go to the BRIDGES game and stand on the trapdoor on the ground floor of the center tower. In chat, type
`/bridges setCenterTower` This will set the center tower coordinate for the game. The coordinate will be
saved the mod config folder so this only needs to be done once.

2. Make sure that all chunks for the game are loaded for the game and run the command `/bridges scan` to judge the game. 
The mod will scan the game and output the results in chat. Lines detecting the paths will appear between nodes. 

## Screenshots 

![Screenshot 1](/.docs/screenshots/2025-01-13_22.03.02.png)
![Screenshot 2](/.docs/screenshots/2025-01-13_22.03.25.png)
![Screenshot 3](/.docs/screenshots/2025-01-13_22.04.26.png)
