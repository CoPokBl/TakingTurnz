# Taking Turnz
Taking Turnz is a Minecraft game mode where players take turns playing the game to accomplish
a specific goal. After a configurable amount of time, the current player is switched out for the next player,
the new players keeps the progress made by the previous player, and continues working towards the goal.

All health, hunger, saturation, inventory, breath, xp, position, fall distance, bed spawn, and status effects are preserved between turns.
While players are not active they are placed in a creative world where they can freely play around
while they are waiting.

For best experience you should play with **2-6 players** (more players = longer wait times between turns),
a timer between **1-5 minutes**, **no communication** between players during the game, and 
**chat disabled** (in chat settings) to hide achievements and death messages.

The plugin is designed for and tested on **Minecraft 1.21.11** but will likely work on other versions as well.
It is built against Spigot but was tested on Paper.

## Usage
1. Download the latest build from [Serble Jenkins](https://serble.com/jenkins/job/Taking%20Turnz/).
2. Place the `.jar` into the plugins folder and start the server.
3. Have all players join:
4. Start the game:
   1. `/takingturnz <seconds-per-turn> <player1> [player2] [player3]...` - Specify all players
   2. `/takingturnz <seconds-per-turn> random` - Start with all online players (in a random order)

## Known Limitations/Issues (Easy PRs)
- If anyone disconnects during the game it will break. **DO NOT DISCONNECT**.
- The game config will not persist between server restarts. (Just start it again with the same players)
- Sleeping in the creative world as it becomes your turn will prevent you from being teleported to the survival world.
- Agro state of mobs is not preserved between turns (Neutral mobs will not be mad at next person).

