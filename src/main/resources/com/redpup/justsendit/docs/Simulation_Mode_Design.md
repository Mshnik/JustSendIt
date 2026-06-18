# Just Send It - Headless Simulation Mode Design

## Goal
To provide a headless entry point to the "Just Send It" game, allowing for automated playthroughs by AI roles. This is useful for balancing, testing, and verifying game logic without a GUI.

## Architecture

### Simulation Directory
All simulation-related code will live in `com.redpup.justsendit.simulation`.
Controllers live in `com.redpup.justsendit.simulation.controller`.

### AiPlayerControllers
Implementations of `PlayerController` that use different strategies.

#### SimpleAiController
Uses simple heuristics to make "safe" and "productive" decisions.
- **makeMountainDecision**: Prioritizes EXIT, then LIFT (if cards available), then SKI_RIDE.
- **chooseSkiRideResolutionAction**: Plays the card with the highest expected value.

#### RandomAiController
Makes stochastic decisions, useful for exploring the game space and testing edge cases.
- Randomly chooses between valid actions.
- Randomly plays cards from hand.

### SimulationModule
A Guice module that binds:
- `List<PlayerController>` to a list of AI controller instances.
- `Logger` to a `PrintlineLogger` (simple console output).
- Other necessary core modules (`GameModelModule`, `SystemTimeSourceModule`, etc.).

### SimulationMain
The entry point containing the `main` function.
It will:
1. Create a Guice injector with the `SimulationModule`.
2. Get a `SimulationRunner` instance.
3. Run the simulation.
4. Print the final results (scores, winner).

## Implementation Plan
1. Create `src/main/kotlin/com/redpup/justsendit/simulation/AiPlayerController.kt`.
2. Create `src/main/kotlin/com/redpup/justsendit/simulation/SimulationRunner.kt`.
3. Create `src/main/kotlin/com/redpup/justsendit/simulation/SimulationModule.kt`.
4. Create `src/main/kotlin/com/redpup/justsendit/simulation/SimulationMain.kt`.
5. Add a `SimulationLogger` if needed to see progress.
