# Code Quality Review

Focus: small, low-risk fixes (no rewrites) that improve correctness or efficiency.

## Findings

1) **Null-safe species checks in predicates and UI helpers**  
   - **Priority:** High (potential crash)  
   - **Ease:** Easy  
   - **Details:** `PokemonPredicates.IS_LEGENDARY/IS_MYTHICAL/IS_ULTRABEAST/IS_PARADOX` call `SimpleSpeciesRegistry.getByName(...)` and immediately dereference `species.labels`. If the registry is still loading or a species key is missing, this can throw `NullPointerException` and break alerts/tooltips.  
   - **Example fix:**  
     ```java
     SimpleSpecies species = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
     if (species == null) return false;
     return species.labels.contains("legendary");
     ```

2) **Quick-sort hotkey can NPE when the storage widget is absent**  
   - **Priority:** High (user-facing crash risk)  
   - **Ease:** Easy  
   - **Details:** In `PCGUIMixin.chiselmon$executeQuickSort`, a null check guards `resetSelected()` but `storageWidget.getBox()` is still called unconditionally. If the widget is null (e.g., during init errors), middle-click sorting will crash.  
   - **Example fix:**  
     ```java
     if (this.storageWidget == null) return;
     this.storageWidget.resetSelected();
     PcSorter.sortPCBox(this.pc, this.storageWidget.getBox(), config.pc.quickSortMode, hasShiftDown());
     ```

3) **Box index validation missing in `PcSorter.sortPCBox`**  
   - **Priority:** Medium (defensive safety)  
   - **Ease:** Easy  
   - **Details:** The method uses `boxes.get(boxNumber)` without checking `boxNumber < boxes.size()`. An out-of-range value (bad config/state) will throw `IndexOutOfBoundsException` and abort sorting.  
   - **Example fix:**  
     ```java
     List<ClientBox> boxes = clientPC.getBoxes();
     if (boxNumber >= boxes.size()) return false;
     ClientBox currentBox = boxes.get(boxNumber);
     ```

4) **Sorting packet emission does extra work per slot**  
   - **Priority:** Low (micro-optimization)  
   - **Ease:** Easy  
   - **Details:** `applySortedOrder` searches for the occupant of each target slot by scanning `currentPositions` and then streaming `sortedPokemon`. Building a reverse `slot -> uuid` map once avoids repeated O(n²) lookups and keeps the method simpler.  
   - **Example fix:**  
     ```java
     Map<Integer, UUID> slotToUuid = currentPositions.entrySet().stream()
         .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
     // use slotToUuid.get(targetSlot) instead of scanning each time
     ```

5) **Capture estimate can null-deref species and recalculates ball bonus**  
   - **Priority:** Medium (stability & tiny perf gain)  
   - **Ease:** Easy  
   - **Details:** `CaptureChanceEstimator` reads `SimpleSpeciesRegistry.getByName(...).catchRate` without a null guard, so a missing entry or not-yet-loaded registry will throw. It also calls `BallBonusEstimator.calculateBallBonus` twice with identical inputs.  
   - **Example fix:**  
     ```java
     SimpleSpecies species = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
     float C = species != null ? species.catchRate : 0f;
     float ballBonus = BallBonusEstimator.calculateBallBonus(ball, targetEntity, thrower, throwerActiveBattlePokemon, targetStatus);
     // reuse ballBonus instead of recomputing
     ```

## Notes
- Test suite could not be run locally because the build requires JVM 21 while the environment provides Java 17 (`./gradlew test` fails at plugin resolution).
