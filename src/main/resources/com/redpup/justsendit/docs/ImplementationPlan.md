# Implementation Plan - Rulebook V2 Update

This plan outlines the steps to update 'Just Send It' from the current implementation to the Rulebook V2 specifications.

## Objective
Align the codebase with the new V2 rules, including:
- Dice-based resolution for skiing/riding (Green d4, Blue d6, Black d8).
- Wobble and crash mechanics.
- Slopes: Difficulty, Grade, Condition, Hazards, Slow.
- Lifts: Direction, Color, Mountain Exit.
- New Skill Card and Player Card structures.
- Shop system with sale tokens.
- Updated turn and round structure.

## Phase 1: Cleanup [COMPLETE]
- [x] Delete `src/main/proto/com/redpup/justsendit/model/player/chip.proto`.
- [x] Delete `src/main/kotlin/com/redpup/justsendit/model/player/TrainingChipExtensions.kt`.
- [x] Remove `TerrainPark` from `src/main/proto/com/redpup/justsendit/model/board/tile/slope.proto`.
- [x] Remove all references to `TrainingChip` and `TerrainPark`.

## Phase 2: Proto Updates [COMPLETE]
- [x] Create `src/main/proto/com/redpup/justsendit/model/supply/skill_card.proto`.
- [x] Update `src/main/proto/com/redpup/justsendit/model/player/player.proto`.
- [x] Update `src/main/proto/com/redpup/justsendit/model/player/decision.proto`.
- [x] Update `src/main/proto/com/redpup/justsendit/model/board/tile/lift.proto` (LiftColor update).

## Phase 3: Core Model Implementation [IN PROGRESS]
- [/] **Skill Cards & Decks:**
    - [x] Update `SkillDecks.kt` to load and provide `SkillCard` objects.
    - [ ] Implement starter deck (10 cards) and shop deck.
- [/] **Player State:**
    - [x] Update `MutablePlayer` to track:
        - `hand`: List of `SkillCard`.
        - `inPlay`: List of `SkillCard`.
        - `discardPile`: List of `SkillCard`.
        - `wobbles`: Int.
        - `studyValue`: Calculated from hand + matching icons.
- [ ] **Game Model Logic:**
    - [x] Update `startDay()`: Distribute starting fun (Leader: 10, 2nd: 12, etc.).
    - [x] Implement `turn()`: Round-robin turns until all pass. Advance leader token.
    - [ ] Implement `executeSkiRide()`:
        - Handle movement.
        - Implement the 6-step resolution loop.
        - Dice rolling: Green(d4), Blue(d6), Black(d8).
        - Terrain effects (Ice, Powder, Moguls, Trees, Cliffs).
        - Wobble calculation and Crash check (3+ wobbles).
        - Wipeout check (Crash with no cards in hand).
    - [ ] Implement `executePass()`:
        - Reveal hand, calculate study value.
        - Handle Shop purchase.
    - [ ] Implement `executeLift()`:
        - Free movement onto lift.
        - Upward movement cost (discard cards).
        - Trashing logic.
- [ ] **Shop System:**
    - 5 cards in shop.
    - Sale tokens (-1 cost per token, max 2).
    - Round-end replenishment and token addition.

## Phase 4: Verification
- [ ] Update existing tests.
- [ ] Add new tests for:
    - Dice rolling and wobble logic.
    - Terrain effect application.
    - Shop cost and sale token logic.
    - Round-robin turn structure.

## Unknowns & TODOs
- [ ] Exact dice values for Green (d4), Blue (d6), Black (d8) - assuming standard dice.
- [ ] Exact composition of the 10-card starter deck.
