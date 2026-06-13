# Implementation Plan - Rulebook V2 Update

This plan outlines the steps to update 'Just Send It' from the current
implementation to the Rulebook V2 specifications.

## Objective

Align the codebase with the new V2 rules, including:

- Dice-based resolution for skiing/riding (Green d4, Blue d6, Black d8).
- Wobble and crash mechanics.
- Slopes: Difficulty, Grade, Condition, Hazards, Slow.
- Lifts: Direction, Color, Mountain Exit.
- New Skill Card and Player Card structures.
- Shop system with sale tokens.
- Updated turn and round structure.

## Phase 1: Cleanup [IN PROGRESS]

- [x] Delete `src/main/proto/com/redpup/justsendit/model/player/chip.proto`.
- [x] 
  Delete `src/main/kotlin/com/redpup/justsendit/model/player/TrainingChipExtensions.kt`.
- [x] Remove `TerrainPark`
  from `src/main/proto/com/redpup/justsendit/model/board/tile/slope.proto`.
- [x] Remove all references to `TrainingChip` and `TerrainPark`.
- [ ] Remove all existing Player implementations and tests
  from `src/main/kotlin/com/redpup/justsendit/model/player/cards/friday`, `src/main/kotlin/com/redpup/justsendit/model/player/cards/saturday`,
  and `src/main/kotlin/com/redpup/justsendit/model/player/cards/sunday`, along
  with their tests.

## Phase 2: Proto Updates [COMPLETE]

- [x] 
  Create `src/main/proto/com/redpup/justsendit/model/supply/skill_card.proto`.
- [x] Update `src/main/proto/com/redpup/justsendit/model/player/player.proto`.
- [x] Update `src/main/proto/com/redpup/justsendit/model/player/decision.proto`.
- [x] 
  Update `src/main/proto/com/redpup/justsendit/model/board/tile/lift.proto` (
  LiftColor update).

## Phase 3: Core Model Implementation [IN PROGRESS]

- [/] Skill Cards & Decks:
    - [x] Update `SkillDecks.kt` to load and provide `SkillCard` objects.
    - [x] Implement starter deck (10 cards) and shop deck.
    - [ ] Implement `Skill` wrapper type similar to `Apres` in kotlin vs `ApresCard` in proto. 
- [x] Player State:
    - [x] Update `MutablePlayer` to track:
        - `hand`: List of `SkillCard`.
        - `inPlay`: List of `SkillCard`.
        - `discardPile`: List of `SkillCard`.
        - `wobbles`: Int.
        - `studyValue`: Calculated from hand + matching icons.
- [x] Game Model Logic:
    - [x] Update `startDay()`: Distribute starting fun (Leader: 10, 2nd: 12,
      etc.).
    - [x] Implement `turn()`: Round-robin turns until all pass. Advance leader
      token.
    - [x] Implement `executeSkiRide()`:
        - Handle movement.
        - Implement the 6-step resolution loop.
        - Dice rolling: Green(d4), Blue(d6), Black(d8).
        - Terrain effects (Ice, Powder, Moguls, Trees, Cliffs).
        - Wobble calculation and Crash check (3+ wobbles).
        - Wipeout check (Crash with no cards in hand).
    - [x] Implement `executePass()`:
        - Reveal hand, calculate study value.
        - Handle Shop purchase.
    - [x] Implement `executeLift()`:
        - Free movement onto lift.
        - Upward movement cost (discard cards).
        - Trashing logic.
- [x] Shop System:
    - [x] 5 cards in shop.
    - [x] Sale tokens (-1 cost per token, max 2).
    - [x] Round-end replenishment and token addition.

## Phase 4: Verification & Testing [IN PROGRESS]

- [x] **Update all existing tests:**
    - [x] `GameModelTest.kt`: Update to reflect round-robin turns, starter
      decks, and new ski/ride resolution.
    - [x] `PlayerTest.kt`: Update to use `SkillCard` objects and new state (
      wobbles, etc.).
    - [x] `SkillDeckTest.kt`: Update for `SkillCard` loading and qualified
      decks.
    - [x] Apres Card Tests (`BarTest.kt`, `FireworksTest.kt`, etc.): Update
      triggers and rewards to match dice-based logic.
    - [x] `PlayerControllerTest.kt`: Update to use new resolution actions and
      lift/trash logic.
- [ ] **Add new V2-specific tests:**
    - [ ] Dice rolling and wobble accumulation logic.
    - [ ] Terrain effect application during ski/ride resolution.
    - [ ] Shop cost calculation with sale tokens and icon matching.
    - [ ] Round-robin turn structure and leader token advancement.
    - [ ] Lift cost and trashing mechanism.
