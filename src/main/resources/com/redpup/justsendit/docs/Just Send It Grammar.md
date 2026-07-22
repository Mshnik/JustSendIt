# Just Send It: Card Effect Grammar & Expected Value Specification

Version 3.2 - Formalized Grammar & EV Model

---

## 1. Overview & Core Design Principles

This document defines the formal grammar for skill cards in *Just Send It*,
along with rules for parsing effect timing, optionality, atomic costs,
conditional
scaling, and calculating expected value (EV) for automated card evaluation and
generation.

### Key Rules & Invariants:

1. **Optionality & Atomic Costs**: All card effects are optional. However, if an
   effect includes a cost, the cost is atomic with the effect (i.e., you must
   pay the complete cost to gain the effect). Costs always precede the gain
   clause in card text (e.g., *"Discard 1 card to gain +5 Skill"* or *"Gain 1
   Wobble to gain +5 Skill"*).
2. **Timing Mutually Exclusive**: Every card effect has exactly one timing
   rule (`PLAY`, `FIRST`, `LAST`, `PASS`, or `LIFT`).
3. **Single Card Stack Rules**: A single played card acting alone in a stack
   qualifies as both `FIRST` and `LAST`.
4. **Action Restrictions**:
    - `PLAY`, `FIRST`, `LAST`: Resolved sequentially during a Ski/Ride attempt
      based on played card order. 
      - Effects referencing dice can **only** occur on `PLAY`, `FIRST`, and `LAST`.
      - Effects referencing rolled dice values can **only** occur on `LAST`. 
    - `PASS`: Used during a Pass action. `+buy` and any effect modifying or
      interacting with the shop can **only** occur on `PASS`.
    - `LIFT`: Used strictly during a Lift action.
5. **Sequential Die Modifications**: Every die modification (reroll/shift)
   resolves individually in sequential order. Any valid die can be affected
   multiple times if targeted by multiple sequential effects.
6. **Implicit Caps on Scaling**: Conditional scaling effects (e.g., *"For each
   Wobble..."*) rely on game-state limits (e.g., 3 Wobbles cause a crash) rather
   than explicit grammar caps.

---

## 2. EBNF Grammar Specification

```
Card               ::= "Name: " Text ", Flavor Text: " Text ", Bonus: " Bonuses ", Dice: " Dice ", Effect: " Effect

Bonuses            ::= Bonus{0-2}
Bonus              ::= Grade | Condition | Hazard

Grade              ::= "Green" | "Blue" | "Black" | "Double Black"
Condition          ::= "Groomed" | "Powder" | "Ice"
Hazard             ::= "Moguls" | "Trees" | "Cliffs"

Dice               ::= Die{0-3}

Effect             ::= TimingRule ":" [ CostClause " to " ] EffectClause
TimingRule         ::= "PLAY" | "FIRST" | "LAST" | "PASS" | "LIFT"

CostClause         ::= SimpleEffect
EffectClause       ::= ConditionalClause | CompoundEffect | SimpleEffect

ConditionalClause  ::= Condition ", " EffectClause
Condition          ::= "For each " ConditionType

ConditionType      ::= RollCondition
                       | StackCondition
                       | StateCondition
                       | ShopCondition

RollCondition      ::= Number " rolled on a " DieType " die"
StackCondition     ::= "card " RelativePosition
StateCondition     ::= "wobble you have"
ShopCondition      ::= "card bought"

RelativePosition   ::= "above" | "below" | "played" | "in hand"

CompoundEffect     ::= SimpleEffect " and " SimpleEffect

SimpleEffect       ::= DieEffect
                       | SkillEffect
                       | FunEffect
                       | CardEffect
                       | TrashEffect
                       | ShopEffect
                       | WobbleEffect

DieEffect          ::= DieAction " " DieTarget
DieAction          ::= "Reroll" | "Shift +1" | "Shift -1" | "Shift"
DieTarget          ::= TargetQuantity " " DieType " die" [ " showing " Number ]
TargetQuantity     ::= "a" | "any" | Number

SkillEffect        ::= "+" Number " Skill"
FunEffect          ::= "Gain " Number " Fun"

CardEffect         ::= "Draw " Number " card" [ "s" ]
                       | "Discard " Number " card" [ "s" ]
                       | "Put " Number " card" [ "s" ] " on top of deck from " Zone

TrashEffect        ::= "Trash " Number " card from " Zone
Zone               ::= "hand" | "discard" | "shop" | "any visible zone"

ShopEffect         ::= "Replenish the shop" | "Gain " Number " buy" [ "s" ]

WobbleEffect       ::= "Ignore " Number " wobble" [ "s" ] " gained this turn"
                       | "Gain " Number " wobble" [ "s" ]

Die                ::= "Green" | "Blue" | "Black"
DieType            ::= "Green" | "Blue" | "Black" | "Wild"

Text               ::= [a-zA-Z0-9\s\"\.]+
Number             ::= [0-9]+
```

---

## 3. Expected Value (EV) Calibration System

### 3.1 Base Component Benchmarks

| Component                    | EV Baseline | Notes / Context                                                             |
|:-----------------------------|:------------|:----------------------------------------------------------------------------|
| **+1 Skill**                 | `+1.0 EV`   | Direct contribution to tile difficulty threshold.                           |
| **+1 Fun**                   | `+0.8 EV`   | Victory points. Slower pacing, valued slightly below Skill.                 |
| **Draw 1 Card**              | `+5.0 EV`   | Highly valuable (increases options, dice pool, and resources).              |
| **Discard 1 Card (Cost)**    | `-4.0 EV`   | Discarding from hand reduces flexibility and potential skill.               |
| **Ignore 1 Wobble**          | `+1.0 EV`   | Protects run from crash failure state.                                      |
| **Gain 1 Wobble (Cost)**     | `-1.0 EV`   | Hazard penalty (increases crash likelihood).                                |
| **Trash 1 Card**             | `+2.0 EV`   | Deck thinning / upgrading value.                                            |
| **+1 Buy**                   | `+2.0 EV`   | Engine building on PASS actions.                                            |
| **Die Reroll (Conditional)** | `~+0.42 EV` | e.g., Rerolling a 1 on d6 (avg 3.5): $1/6 \times 2.5 \approx 0.42$ per die. |

---

### 3.2 Total Card EV Formulas

Total EV calculation depends on how the card is utilized (Ski/Ride, Pass, or
Lift):

#### 1. Played Cards (Ski / Ride Attempt)

When played in a vertical stack during a Ski/Ride check:
$$\text{EV}_{\text{Total}} = \text{EV}_{\text{Dice}} + \text{EV}
_{\text{Effect}} + \text{EV}_{\text{BonusTag}}$$

#### 2. Pass Option

When used as a Pass action:
$$\text{EV}_{\text{Total}} = \text{optionMax}(\text{EV}_{\text{Dice}}, \text{EV}
_{\text{Effect}}) + \text{EV}_{\text{BonusTag}}$$

#### 3. Lift Option

When used as a Lift action:
$$\text{EV}_{\text{Total}} = \text{optionMax}(\text{EV}_{\text{Dice}} +
\text{EV}_{\text{BonusTag}}, \text{EV}_{\text{Effect}})$$

#### 4. The `optionMax` Flex-Multiplier Function

Because Pass and Lift offer modal choices, players pick the optimal mode for
their situation. The standard maximum is scaled by a flexibility factor
$\lambda$ (where $\lambda \approx 1.1 \text{ to } 1.15$):
$$\text{optionMax}(A, B) = \max(A, B) \times \lambda$$

---

## 4. Parsed Card Examples & Calculated EV

### Example 1: Basic Skill Boost with Atomic Cost

* **Text**: `"PLAY: Discard 1 card to gain +5 Skill."`
* **Grammar Parsing**: `PLAY : Discard 1 card (Cost) -> Gain +5 Skill (Effect)`
* **EV Breakdown**:
    - Discard 1 Card: `-4.0 EV`
    - Gain +5 Skill: `+5.0 EV`
    - **Net Effect EV**: `-4.0 + 5.0 = +1.0 EV`

### Example 2: Conditional Reroll on First Position

* **Text**: `"FIRST: Reroll any Blue die showing 1."`
* **Grammar Parsing**: `FIRST : Condition (Blue die showing 1) -> Reroll`
* **EV Breakdown**:
    - Probability of rolling 1 on d6 $= 1/6 \approx 0.167$
    - Expected improvement when rerolling 1 to average ($3.5$) $= +2.5 \text{
      Skill}$
    - **Net Effect EV**: $1/6 \times 2.5 \approx \mathbf{+0.42 \text{ EV per
      Blue die in pool}}$

### Example 3: Shop Engine Builder (Pass Action)

* **Text**: `"PASS: Trash 1 card from hand to gain 1 buy."`
* **Grammar Parsing
  **: `PASS : Trash 1 card from hand (Cost) -> Gain 1 buy (Effect)`
* **EV Breakdown**:
    - Discard/Loss of hand card: `-4.0 EV`
    - Trash card value: `+2.0 EV`
    - Gain +1 Buy: `+2.0 EV`
    - **Net Effect EV**: `-4.0 + 2.0 + 2.0 = \mathbf{0.0 \text{ EV}}`

### Example 4: Hazard Mitigation on Play

* **Text**: `"PLAY: Discard 1 card to ignore 2 wobbles gained this turn."`
* **Grammar Parsing
  **: `PLAY : Discard 1 card (Cost) -> Ignore 2 wobbles (Effect)`
* **EV Breakdown**:
    - Discard 1 Card: `-4.0 EV`
    - Ignore 2 Wobbles: $2 \times 1.0 = +2.0 \text{ EV}$
    - **Net Effect EV**: `-4.0 + 2.0 = \mathbf{-2.0 \text{ EV}}` *(Strategic
      situational card used in high-hazard runs).*

---

## 5. Follow-Up Questions & Continued Research

To further refine this grammar and EV engine for automated card generation,
please consider the following design edge cases:

1. **Top-Deck Zone Valuation**: How should *"Put 1 card on top of deck
   from [zone]"* be calibrated? Does top-decking from the discard pile carry a
   higher EV than top-decking from hand due to deck-sifting and card-quality
   filtering?
2. **Multi-Die Reroll Probability Chains**: When multiple reroll or shift
   effects exist across a stack, should reroll EV calculations assume
   independent probabilities, or should a discount factor be applied for stacked
   conditional rerolls on the same dice pool?
3. **Flexibility Factor ($\lambda$) Tuning**: Should $\lambda$ in `optionMax`
   scale dynamically based on the day of the trip (e.g., Friday hand size 4 vs.
   Sunday hand size 6)?
4. **Shop Manipulation EV**: What is the benchmark EV for *"Replenish the
   shop"*? Is its value tied to whether a player currently holds available buys
   or unspent study currency?