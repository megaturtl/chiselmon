package cc.turtl.chiselmon.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import org.apache.logging.log4j.Logger;

/**
 * Smart Pokemon/Entity dumper for debugging.
 *
 * CONFIGURATION:
 * - Adjust SKIP_FIELDS to hide noisy fields
 * - Adjust SKIP_OBJECTS to stop recursion into infrastructure
 * - Adjust MAX_COLLECTION_ITEMS to show more/fewer array items
 * - Adjust MAX_DEPTH to control how deep we recurse
 */
public class ObjectDumper {

    // ============================================
    // CONFIGURATION - Adjust these as needed
    // ============================================

    /** How many items to show from arrays/collections before truncating */
    private static final int MAX_COLLECTION_ITEMS = 15;

    /** Maximum recursion depth for nested objects */
    private static final int MAX_DEPTH = 3;

    /** Maximum string length before truncation */
    private static final int MAX_STRING_LENGTH = 500;

    /**
     * Field names to completely skip (rendering, internal state, duplicates, etc.)
     * Add more here if you see noisy fields in your output.
     */
    private static final Set<String> SKIP_FIELDS = Set.of(
            // === Rendering & Client-side ===
            "delegate", "currentModel", "runtime", "ridingAnimationData",
            "rideSoundManager", "ridingController", "locatorStates", "renderQueue",
            "activeAnimations", "poseParticles", "quirks", "animations", "poses",
            "animationItems", "numbers", "renderMarkers", "functions", "struct",
            "levelRenderer", "effects", "chunkSource", "frustumPoints", "frustumPos",
            "rainSizeX", "rainSizeZ", "fabric$id",

            // === Internal Infrastructure ===
            "subscriptions", "schedulingTracker", "removalObservable",
            "evolutionProxy", "storeCoordinates", "changeFunction",
            "standardForm$delegate", "runtime$delegate",

            // === Minecraft Internals ===
            "levelCallback", "packetPositionCodec", "brain",
            "goalSelector", "targetSelector", "sensing", "navigation",
            "lookControl", "moveControl", "jumpControl", "bodyRotationControl",
            "pathFinder", "nodeEvaluator",

            // === World/Level References (too verbose) ===
            "tickingEntities", "entityStorage", "connection", "blockEntityTickers",
            "neighborUpdater", "pendingBlockEntityTickers", "profiler", "worldBorder",
            "biomeManager", "damageSources", "loadedChunks", "lightUpdateQueue",
            "blockStatePredictionHandler", "customColorCache", "thread",
            "dimensionTypeRegistration", "tickRateManager", "tintCaches",

            // === Duplicate/Previous Values ===
            "xo", "yo", "zo", "yRotO", "xRotO", "xOld", "yOld", "zOld",
            "oAttackAnim", "attackAnim", "oRun", "animStepO", "walkDistO",
            "swimAmountO", "yBodyRotO", "yHeadRotO", "oRainLevel", "oThunderLevel",

            // === Very Verbose Internal State ===
            "pathfindingMalus", "attributes", "entityData", "inBlockState",
            "random", "gaussianSource", "threadSafeRandom", "combatTracker"
    );

    /**
     * Object types to stop recursing into (show summary only).
     * These are classes that contain too much infrastructure to be useful.
     */
    private static final Set<String> SKIP_OBJECTS = Set.of(
            "net.minecraft.client.multiplayer.ClientLevel",
            "net.minecraft.server.level.ServerLevel",
            "net.minecraft.world.level.Level",
            "net.minecraft.client.multiplayer.ClientPacketListener",
            "net.minecraft.server.network.ServerGamePacketListener",
            "net.minecraft.client.renderer.LevelRenderer",
            "net.minecraft.world.entity.ai.Brain"
    );

    // ============================================
    // Public API
    // ============================================

    public static void dump(Logger logger, Object obj) {
        if (obj == null) {
            logger.info("[ObjectDumper] null");
            return;
        }

        logger.info("╔══════════════════════════════════════");
        logger.info("║ {} ", obj.getClass().getSimpleName());
        logger.info("╠══════════════════════════════════════");
        DumpContext ctx = new DumpContext(logger);
        ctx.dump(obj, "", 0);
        logger.info("╚══════════════════════════════════════");
    }

    // ============================================
    // Implementation
    // ============================================

    private static class DumpContext {
        final Logger logger;
        final Set<Integer> visited = new HashSet<>();

        DumpContext(Logger logger) {
            this.logger = logger;
        }

        void dump(Object obj, String path, int depth) {
            if (obj == null) return;
            if (depth > MAX_DEPTH) return;

            Class<?> cls = obj.getClass();

            // Cycle detection
            int objId = System.identityHashCode(obj);
            if (visited.contains(objId)) return;
            visited.add(objId);

            // Stop at infrastructure objects
            if (shouldStopAtObject(cls)) {
                return; // Just skip entirely
            }

            // Handle by type
            if (isDataValue(cls)) {
                logValue(path, obj, cls);
            } else if (cls.isArray()) {
                dumpArray(obj, path, depth);
            } else if (obj instanceof Collection) {
                dumpCollection((Collection<?>) obj, path, depth);
            } else if (obj instanceof Map) {
                dumpMap((Map<?, ?>) obj, path, depth);
            } else if (obj instanceof UUID) {
                logValue(path, obj.toString(), cls);
            } else if (shouldExplore(cls)) {
                dumpObject(obj, path, depth);
            }
        }

        void logValue(String path, Object value, Class<?> cls) {
            String display = formatValue(value, cls);
            if (path.isEmpty()) {
                logger.info("  {}", display);
            } else {
                logger.info("  {} = {}", path, display);
            }
        }

        String formatValue(Object value, Class<?> cls) {
            if (cls.isEnum()) {
                return value.toString();
            }
            if (value instanceof String) {
                String s = (String) value;
                return s.length() > MAX_STRING_LENGTH
                        ? s.substring(0, MAX_STRING_LENGTH - 3) + "..."
                        : s;
            }
            return String.valueOf(value);
        }

        void dumpObject(Object obj, String path, int depth) {
            Class<?> cls = obj.getClass();
            List<Field> fields = getRelevantFields(cls);

            if (fields.isEmpty()) return;

            // Show header for nested objects
            if (depth > 0 && !path.isEmpty()) {
                logger.info("  {} ↓", path);
            }

            for (Field field : fields) {
                String fieldName = field.getName();
                String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;

                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    dump(value, fieldPath, depth + 1);
                } catch (Exception e) {
                    // Skip inaccessible
                }
            }
        }

        void dumpArray(Object arr, String path, int depth) {
            int len = Array.getLength(arr);
            Class<?> componentType = arr.getClass().getComponentType();

            // Primitive arrays: show inline
            if (componentType.isPrimitive() || componentType == String.class) {
                StringBuilder sb = new StringBuilder("[");
                int limit = Math.min(len, 20);
                for (int i = 0; i < limit; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(Array.get(arr, i));
                }
                if (len > limit) sb.append(", ... +").append(len - limit);
                sb.append("]");
                logValue(path, sb.toString(), arr.getClass());
                return;
            }

            // Object arrays: expand
            logger.info("  {} = Array[{}]", path, len);
            if (len == 0) return;

            int limit = Math.min(len, MAX_COLLECTION_ITEMS);
            for (int i = 0; i < limit; i++) {
                dump(Array.get(arr, i), path + "[" + i + "]", depth + 1);
            }
            if (len > limit) {
                logger.info("    ... +{} more items", len - limit);
            }
        }

        void dumpCollection(Collection<?> col, String path, int depth) {
            if (col.isEmpty()) {
                logger.info("  {} = [] (empty)", path);
                return;
            }

            // Small collections of simple types: show inline
            if (col.size() <= 5 && col.stream().allMatch(o -> o == null || isDataValue(o.getClass()))) {
                String items = col.stream()
                        .map(o -> o == null ? "null" : String.valueOf(o))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                logger.info("  {} = [{}]", path, items);
                return;
            }

            logger.info("  {} = Collection[{}]", path, col.size());
            int i = 0;
            for (Object item : col) {
                if (i >= MAX_COLLECTION_ITEMS) {
                    logger.info("    ... +{} more items", col.size() - i);
                    break;
                }
                dump(item, path + "[" + i + "]", depth + 1);
                i++;
            }
        }

        void dumpMap(Map<?, ?> map, String path, int depth) {
            if (map.isEmpty()) {
                logger.info("  {} = {{}} (empty)", path);
                return;
            }

            logger.info("  {} = Map[{}]", path, map.size());
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i >= MAX_COLLECTION_ITEMS) {
                    logger.info("    ... +{} more entries", map.size() - i);
                    break;
                }
                String key = String.valueOf(entry.getKey());
                if (key.length() > 40) key = key.substring(0, 37) + "...";
                dump(entry.getValue(), path + "." + key, depth + 1);
                i++;
            }
        }

        boolean isDataValue(Class<?> cls) {
            return cls.isPrimitive() ||
                    cls == String.class ||
                    cls == Boolean.class ||
                    cls == Character.class ||
                    Number.class.isAssignableFrom(cls) ||
                    cls.isEnum();
        }

        boolean shouldExplore(Class<?> cls) {
            String name = cls.getName();
            return name.startsWith("net.minecraft") ||
                    name.startsWith("com.cobblemon") ||
                    name.startsWith("cc.turtl.chiselmon");
        }

        boolean shouldStopAtObject(Class<?> cls) {
            String name = cls.getName();
            return SKIP_OBJECTS.stream().anyMatch(name::equals);
        }

        List<Field> getRelevantFields(Class<?> cls) {
            List<Field> fields = new ArrayList<>();

            Class<?> current = cls;
            while (current != null && shouldExplore(current)) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (field.isSynthetic()) continue;
                    if (SKIP_FIELDS.contains(field.getName())) continue;

                    fields.add(field);
                }
                current = current.getSuperclass();
            }

            return fields;
        }
    }
}