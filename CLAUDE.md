# Project Context


## General Instructions

- When working with this codebase, prioritize readability over cleverness. 
- Ask clarifying questions before making architectural changes.
- When reviewing documentation/reference materials during development, always ensure that they are compatible with the current version of fabric specified by the gradle build. The mod only needs to support a single version of Minecraft at a time. 
- When making a code change, always run a build to confirm there are no compilation errors.
- When possible, add unit tests for changes. High levels of test coverage are not always practical due the mod needing a live session of minecraft running.
- Consider performance implications of changes, particularly for code executed every frame that would affect FPS. 

## About This Project

This is a Minecraft mod to assist with scoring a multiplayer minigame called Bridges. Bridges plays like a board game.
The mod calculates scores of each teams and helps to identify invalid moves. 
The minigame is played on a server called Titancraft. The current season is 11; season 10 is no longer active. Rules may change between each season. 
The minigame is 100% playable without using the mod. The mod is intended to be a lightweight helper. It may not give players any capabilities that are not possible outside of vanilla minecraft, and is not intended to be used in normal Minecraft gameplay outside of the minigame. 


## Key Directories

- `src/client/` - Client-side mod code
- `src/test/` - Unit tests
- `.docs` - Documentation
- `.docs/titancraft-s10` - Rules/docs for Titancraft server, season 10. Provides legacy context for previous game rules in season 10. 
- `.docs/titancraft-s11` - Rules/docs for Titancraft server, season 11. Provides current game rules for the upcoming season
- `run/` - A local minecraft instance for testing
- `resources/assets/bridges-judge/config` - Configuration files for the mod, including color and height map for towers in the game. 

## Standards

- The mod MUST run client-side only, and cannot connect to any external server for any reason.
  - Run lint validations prior to completing, and fix issues that it identifies. 
  - Always use {} blocks rather than same-line or next line implied blocks. For example, the code below is not acceptable
      ```
          // Move continue into {} brackets and not on the same line
          if (condition) return foo
          // should be
          if (condition) {
              return foo
          }
  
          // technically correct, but harder to read and more error prone if additional logic added in the future.. 
          if (condition) 
              return foo
  
      ```

## Common Commands

`./gradlew build` - Build the mod

## Notes
