# Visual Design Document: Just Send It Digital Client

---

## 1. Executive Summary & Layout Philosophy
This document defines the complete front-end layout, visual hierarchy, and client-side UI interaction matrix for the digital version of *Just Send It*. This specification decouples the rendering and presentation layer from the underlying rules engine, detailing a clean, responsive single-screen user interface optimized for mouse interactions, state-driven overlays, and card-focused animations.

---

## 2. Global Screen Layout (The Grid)

The interface utilizes a fixed, full-screen grid view to guarantee the entire mountain board is fully visible on a single screen without scrolling. Secondary panels adapt fluidly along the top, bottom, and right borders.

```
+-------------------------------------------------------------------------+-------------------+
|                           OTHER PLAYERS PANEL                           |                   |
|           (Horizontal Row of Mini Opponent Widgets: Player 2 | Player 3) |                   |
|                                                                         |   RIGHT SIDEBAR   |
+-------------------------------------------------------------------------+                   |
|                                                                         | (Unified Hub)     |
|                           CENTRAL MAIN BOARD                            |                   |
|           - Full-screen view of Hexagonal Mountain Grid                 | - [ SHOP TAB ]    |
|           - Dynamic Hex Overlay & Highlight Layer                       |   Vertical Accord-|
|           - Integrated Scoreboard & Day/Round Trackers                  |   ion Card List   |
|                                                                         |                   |
+-------------------------------------------------------------------------+ - [ LOG TAB ]     |
|                                                                         | - [ TRASH TAB ]   |
|                                                                         |   Vertical Accord-|
|                           ACTIVE PLAYER AREA                            |   ion with Scroll |
| - Left: Active Player Card & Visual Wobble Node Counter Grid            |                   |
| - Center: Phase Chooser Block ---> Active Hand Buffered Row             |                   |
| - Right: Card Count Badges for Draw Deck & Discard Piles                |                   |
+-------------------------------------------------------------------------+-------------------+
```

---

## 3. Component Layout & Pure Client-Side UI States

### 3.1. Phase Chooser Block & Active Player Area
The center-bottom region acts as the player's execution deck. To ensure game-loop clarity, interactions follow a strict sequential layout.

* **The Phase Chooser Block:** Positioned prominently directly above the player's hand. It displays three explicit, styled action buttons: `[ SKI / RIDE ]`, `[ TAKE LIFT ]`, and `[ PASS ]`.
  * **Availability State:** Buttons support an active click state or a `.disabled` utility class state (dimmed to 30% opacity, unclickable). For example, if specific conditions are not met, the `[ TAKE LIFT ]` layout switches to its disabled state.
  * **Selection Staging:** Clicking an available action button toggles its visual state to `.selected` (solid color fill or heavy gold border). The chosen button remains locked in this state until a step confirmation or a manual reset occurs.
* **Active Hand Row:** Positioned cleanly below the Phase Chooser. Cards are arranged in a horizontal row with a small, uniform, non-zero buffer (`margin: 0 6px`) between them to keep edges distinct.
  * **Hover State:** Hovering over a card slides it vertically upward slightly (`transform: translateY(-12px)`) and applies a subtle outer drop-shadow.

### 3.2. Central Main Board (Middle Left)
* **Layout:** Sits beneath the horizontal opponent bar, adjusting its scale dynamically to fill the available canvas space.
* **Interactions:** Renders background tile layers, absolute-positioned player tokens, and toggleable gold grid borders when tracking hex selections.

### 3.3. Right Sidebar (The Unified Tabbed Hub)
The right sidebar acts as an accordion-style inspector layout controlled by a tab selector (`[ SHOP ]`, `[ LOG ]`, `[ TRASH ]`) running along its top edge.

#### Tab 1: The Shop / Market View
* **Layout:** A tight vertical stack of the 5 skill card slots. Cards overlap so that **only the card title header** and base cost are revealed by default.
* **Hover State (Accordion Expansion):** Mousing over a title expands that specific slot down to its full asset height to show rules text and artwork. Adjacent slots smoothly slide away to clear space.

#### Tab 3: The Trash View
* **Layout:** Built to mirror the Shop layout exactly to maintain visual consistency. Thrashed cards are packed in a tight vertical accordion stack showing title headers only.
* **Hover State:** Uses the identical expansion mechanics as the Shop; hovering reveals the full card asset while pushing adjacent elements out of the way.
* **Overflow Handling:** If the volume of thrashed cards exceeds the vertical bounds of the sidebar layout panel, an inner container automatically triggers standard vertical scrolling (`overflow-y: scroll`) without breaking the shape or position of the sidebar tabs.

### 3.4. Global Right-Click Inspection Layer (The Modal)
* Right-clicking any card element anywhere on the screen suppresses the default browser context menu and triggers a fixed overlay spanning 100% of the viewport (`z-index: 9999`) with a translucent black background (`rgba(0, 0, 0, 0.75)`).
* A high-resolution copy of the card is displayed centered on the screen. Left-clicking anywhere dismisses the modal overlay instantly.

---

## 4. Input Rules & Selection Framework

### 4.1. Invalid Interaction Rule
* **Silent Fail Architecture:** If a user clicks an element that is not currently part of a highlighted valid option set (such as clicking a random hex tile or card in hand out of turn or phase), the client **does nothing**. It remains strictly static, ignores the mouse event, and provides no motion, color flash, or visual alert.

### 4.2. Multi-Card Selection Mode & Indexing
When an active sequence requires selecting multiple cards, the client tracks selection ordering using temporary dynamic UI overlays:

1. **Sequential Click Badging:** Left-clicking a highlighted valid card targets it, shifts its position upward, and places an absolute-positioned badge icon displaying its chronological selection index number (`1`, `2`, `3`, etc.) over the top edge of the asset.
2. **Deselection Repair:** Clicking a currently indexed card removes it from the array and drops its badge layer. The client runs a recalculation script to immediately slide subsequent badge numbers downward sequentially (e.g., if card `2` is removed, card `3` instantly rewrites its text layer to display `2`).
3. **Confirmation Toggle:** A context-sensitive `[CONFIRM]` button sits inside the interaction zone. It remains disabled and unclickable until the count inside the selection array exactly matches the numeric value required by the current interaction step.

---

## 5. UI Interaction Matrix

| Input Action | UI Target Element | Current UI Constraint Context | Client-Side Interface Response |
| :--- | :--- | :--- | :--- |
| **Left-Click** | Phase Chooser Button | Button state is not `.disabled` | Button gains `.selected` state; highlights workspace context color. |
| **Left-Click** | Invalid Element / Tile | Element is outside active valid highlight scope | **No response.** The interface remains completely static. |
| **Mouse Hover** | Shop / Trash Card Header | Element is inside an active sidebar tab view | Active card expands vertically; adjacent card layers slide apart. |
| **Left-Click** | Highlighted Card | Multi-Selection mode is active | Adds card to selection array; applies absolute index number badge overlay; updates `[CONFIRM]` button state. |
| **Left-Click** | Active Numbered Card | Multi-Selection mode is active | Removes card from array; drops its badge overlay; dynamically increments subsequent active badge numbers down. |
| **Right-Click** | Any Card Asset | Anywhere on screen | Triggers full-screen translucent dark backdrop modal with blown-up card center view. |
| Left-Click | Active Modal Backdrop | Inspection layer is open | Dismisses modal view instantly. |

---

## 6. Progress

- [x] **Global Screen Layout**
    - [x] Other Players Panel (Top)
    - [x] Central Main Board (Center)
    - [x] Right Sidebar (Shop, Log, Trash tabs)
    - [x] Active Player Area (Bottom)
- [x] **Components**
    - [x] Phase Chooser Block (Ski/Ride, Lift, Pass buttons)
    - [x] Active Hand Row (Basic implementation, animations added)
    - [x] Shop/Trash Accordion Layout
    - [x] Global Inspection Modal (Right-click)
- [x] **Interaction Logic**
    - [x] Silent fail for invalid interactions
    - [x] Multi-card selection badges and confirmation
    - [x] Integrated GuiController flow (Initial Phase Chooser integration)
- [ ] **Visuals & Polish**
    - [x] CSS Styling (Initial dark theme and component styling)
    - [x] Consolidate all CSS into external files (Removing inline styles)
    - [x] Light Theme Implementation
    - [x] Correct initial window sizing
    - [ ] Accordion View Fixes (Remove jitter, adjust collapsed height to ~25%)
    - [x] Fix accordion card swapping/flickering (Stable Z-order)
    - [x] Fix accordion alignment (Remove right-shift and white space)

    - [ ] Animations (Full hand sliding, smooth accordion expansion)

- [ ] **Advanced Interactions**
    - [x] Player Card Selection Overlay (3 horizontal cards, clickable)
- [ ] **Asset Integration**
    - [x] Board Image Rendering (Background layer)
    - [x] Hex Grid Alignment with Board Image
    - [x] Skill Card Image Rendering (using `filename` from proto)
    - [x] High-Resolution Inspection Modal (using full card images)
