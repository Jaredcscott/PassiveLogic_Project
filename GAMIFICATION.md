# Gamifying the Heat Transfer Simulator

An evaluation of this project (the PassiveLogic heat-flow simulator) and how it
could become a game, drawing on the math already in
[Code-Library](https://github.com/Jaredcscott/code-library).

Note: this project already lives in Code-Library as
`Java/JavaFX_GUI_Example/passivelogic_project`. The only difference is the
author line in the header comment.

---

## 1. What the project is today

| Piece | What it does |
|---|---|
| `passivelogic_project/Main.java` (183 lines) | JavaFX form with 4 inputs (panel m², water L, start °C, minutes); a results dialog |
| `In_Case_The_Java_Project_Wont_Run/HeatTransferSim.py` (69 lines) | The same model as a terminal script |
| `PassiveLogic_Project.jar`, `Demo.mp4` | Build output and a demo video |

The model is `Q = m·c·ΔT`. Each simulated second adds `0.47 × 1360 × A` joules,
and the temperature is capped at 66 °C.

### Why it isn't a game yet

A game needs choices with trade-offs. In this model, **temperature rises in a
straight line** with no losses, so "bigger panel, less water" is always the
answer. There is nothing to balance. The loop also isn't needed, because the
whole simulation reduces to one formula:

```
t_to_66 = m · c · (66 − T0) / (0.47 · 1360 · A)
```

For example, 2 m², 100 L and 20 °C give 251.04 min, which matches the script's
output.

### Bugs to fix before building on it

Checked by running the Python version and reading the Java:

1. **Java reports whole minutes only.** `maxTempTime = i/60;` in `Main.java`
   uses integer division, so 251.05 min is shown as 251.0.
2. **"Heat added" contradicts "final temperature" once the cap is hit.** The
   2 m² / 100 L / 20 °C / 300 min run prints *Total Heat Added: 54.97 °C* and
   *Final Temperature: 66*. Both the energy total and ΔT keep growing after
   the cap.
3. **0 L of water crashes Python** (`ZeroDivisionError`). Java prints
   `Infinity`.
4. **0 minutes crashes Python** (`NameError: tempDifSystem`), because the loop
   never runs.
5. **Negative panel size, volume or time are accepted.**
6. **1360 W/m² is the solar constant *above* the atmosphere.** At the surface,
   peak sunlight is about 1000 W/m². This matters once scores depend on it.
7. **JavaFX setup is the main obstacle** (JDK 10 only, per `readme.txt`). A game
   version should run in a browser.

---

## 2. The change that makes it a game: add heat loss

Replace the hard 66 °C cap with Newton cooling. The system loses heat in
proportion to how much warmer it is than the air:

```
m·c·dT/dt = η·G·A − U·(T − T_air)

T(t)   = T_eq + (T0 − T_eq)·e^(−t/τ)
T_eq   = T_air + η·G·A / U        (the temperature it levels off at)
τ      = m·c / U                  (how fast it gets there)
```

Here `η` is panel efficiency, `G` is sunlight in W/m², `A` is panel area and `U`
is the heat-loss coefficient.

This is still one line of math, which is the "simple model" the original
comments wanted. It also creates real trade-offs:

- More water stores more heat but warms more slowly (larger τ).
- A bigger panel raises the level-off temperature but costs more.
- Insulation (lower U) beats extra panels once the tank is hot.
- The 66 °C cap disappears, because a level-off temperature comes out of the
  physics.

Example: 2 m², 100 L, 20 °C, G = 1000, U = 15 W/K gives T_eq ≈ 82.7 °C and
τ ≈ 465 min. Reaching 45 °C takes about 237 min.

---

## 3. Code-Library math that fits

| Code-Library file | Use in the game |
|---|---|
| `Python/Math/polygonProperties.py` (regular n-gon area) | Hexagonal and triangular panel tiles. Area per tile = panel `A` |
| `Python/Math/cuboidProperties.py` (volume, surface area) | Build the tank from L×W×H: volume → litres (**file uses ft³: ×28.317**); surface area → heat loss `U` |
| `Python/Math/relatedCircles.py` (circle overlap) | Tree-shadow discs covering panels: overlapping → shaded |
| `Python/Pattern_Generators/Branching_Tree_Generator` | Generate the trees that cast those shadows, one layout per level |
| `Python/Pattern_Generators/Spiral_Generator` (φ / π / τ spirals) | Layouts for fields of sun-tracking mirrors. The golden-angle spiral is a known real result: a sunflower-pattern mirror layout saves land and reduces mirrors shading each other |
| `Python/Charts_Graphs_Data_Rendering/simpleGraphs.py` (sine plots) | A day cycle: `G(t) = G_peak · max(0, sin(π·t/daylength))` |
| `Python/Finance/futureValueOfInvestment.py` (compound interest) | Payback period: energy saved × price, compounded, against build cost |
| `Python/Games/monteCarlo.py` + `Simulators/*_Sensor_Sim.py` (random sensor values) | Weather: cloud cover and pressure drawn from the sensor simulators' ranges; Monte Carlo odds for a forecast screen |
| `Python/Math/computeQuadratic.py` | Cost curves (panel price per m² rises with size) |
| `Python/Games/Rat_Slayer` (XP and level progression) | The progression engine: unlock parts, harder days, a "boss" winter week |
| `Python/Algorithms/binarySearch.py` | The solver behind hints: search for the smallest panel that hits the target |

---

## 4. Game concepts

### A. Sun Budget: puzzle levels (recommended first build)

Each level gives a goal, for example "150 L at 45 °C by 5 pm, $800 budget, one
oak tree", and a roof grid.

- Place hex or triangle panel tiles (polygon area). Size the tank by dragging
  L×W×H (cuboid volume and surface area). Buy insulation.
- Tree shadows move across the roof during the day (circle overlap plus the
  tree generator).
- Press **Run Day** to watch the temperature curve animate with the sine
  sunlight curve and heat loss.
- Stars: ★ goal met, ★★ under budget, ★★★ within 5% of the solver's cheapest
  build (binary search over panel area and tank size).

It uses the most Code-Library math, and each level is just data (a JSON goal,
tree seed and budget), so levels are cheap to write.

### B. Solar Estimator: daily guessing puzzle

Show one scenario, for example "3 m², 200 L, 15 °C, clear day". The player
guesses the minutes to 50 °C and gets six tries with hotter/colder hints,
Wordle-style. Scoring uses % error against the closed-form answer, so it
teaches intuition for `m·c·ΔT`. It fits the **Euler Daily** idea from the
Code-Library guide as a physics category.

### C. Heat Slayer: a Rat Slayer season mode

Rat Slayer's loop, with a house to keep warm instead of a hero:

- **Days are fights.** The night's heat demand is the enemy. Stored hot water
  (`m·c·(T − T_min)` joules) is your HP.
- **XP and levels unlock parts:** bigger tanks, better panels (η 0.47 → 0.6),
  insulation, and a pump that shifts heat between two tanks.
- **Weather is the randomness.** Cloud cover per day comes from the sensor
  simulators' `random.uniform` ranges. A forecast screen shows Monte Carlo
  odds of surviving the week.
- **Boss:** a winter week with short days and low sun angle.

It reuses the table-driven combat engine proposed for Rat Slayer Remastered,
with "monsters" becoming weather days.

### D. Sunflower Field: art toy and optimiser (stretch)

Place mirrors for a solar-tower plant using the spiral generator. The player
tunes the divergence angle (137.5° golden angle, π, τ) and sees land used,
shading losses and output. The best score is near the golden angle. It pairs
the existing φ/π/τ spiral images with a real engineering result.

---

## 5. Suggested architecture

One small shared engine, reused by every concept:

```
engine/
  thermal.ts     // T(t) closed form + per-tick step, day-cycle G(t)
  geometry.ts    // polygonArea, cuboid vol/surface, circleOverlap (ports of Code-Library)
  weather.ts     // seeded RNG, cloud model, Monte Carlo forecast
  economy.ts     // cost curves, payback (futureValue port)
  solver.ts      // binary search for par builds / hint system
games/
  sun-budget/  estimator/  heat-slayer/  sunflower/
```

- **Stack:** TypeScript with Canvas on GitHub Pages, the same stack the
  Code-Library guide recommends, so every game can be played from a link. It
  also removes the JavaFX/JDK 10 problem.
- **Keep the Python model** as the reference. A small test checks that the TS
  engine matches it (251.04 min for the example above) and matches the new
  closed form.
- **Balance by simulation:** run thousands of random builds per level to set
  star thresholds, the same method used for the Rat Slayer balance numbers.

## 6. Roadmap

1. Fix bugs 1–5 in the existing sim; switch to 1000 W/m²; add a heat-loss
   option.
2. Port the engine to TS with the parity test.
3. **Solar Estimator** (smallest; one screen, no art).
4. **Sun Budget**: 10 levels, shadows, star scoring.
5. **Heat Slayer** season mode, sharing Rat Slayer's progression engine.
6. **Sunflower Field** as a stretch or portfolio showpiece.

## Verdict

Yes, it is very gamifiable, but only after one physics change. The current
straight-line model has no trade-offs. Adding heat loss (still one closed-form
line) creates level-off temperatures and diminishing returns, which is where
decisions come from. Code-Library already covers most of the rest: geometry
for building, sine curves for the day, Monte Carlo and sensor simulators for
weather, compound interest for payback, spirals and trees for level layout,
and Rat Slayer for progression. **Sun Budget** is the strongest single game;
**Solar Estimator** is the fastest to ship.
