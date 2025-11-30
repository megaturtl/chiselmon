package cc.turtl.cobbleaid.util;

import java.lang.reflect.Field;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

import java.util.Arrays;
import java.util.Collection;

public class ObjectDumper {

    // Maximum recursion depth to prevent StackOverflowError on circular references.
    // Set to 2 as per the user's latest provided code.
    private static final int MAX_RECURSION_DEPTH = 2; 

    /**
     * Public entry point for dumping an object. Starts the recursion at depth 0.
     *
     * @param logger The custom Logger instance to use for output.
     * @param obj The object instance to inspect and log.
     */
    public static void logObjectFields(CobbleAidLogger logger, Object obj) {
        logObjectFields(logger, obj, 0, MAX_RECURSION_DEPTH);
    }

    /**
     * Generates an indentation string based on the current depth level.
     */
    private static String getIndentation(int depth) {
        // Use 4 spaces per depth level for readability
        return " ".repeat(depth * 4); 
    }

    /**
     * Checks if a type is considered a "complex object" that should be recursively
     * inspected, or a simple type (primitive, String, array, wrapper, or Collection) 
     * that should just be logged directly using its toString() method.
     */
    private static boolean isComplexObject(Class<?> type) {
        // Check for common simple/utility types first
        return !type.isPrimitive() && 
               !type.isArray() &&
               !type.equals(String.class) &&
               !type.isEnum() &&
               // Check if it's a standard Java Collection (List, Set, Map, etc.)
               !Collection.class.isAssignableFrom(type) &&
               // Check if it belongs to standard java namespaces that should be filtered
               !type.getName().startsWith("java.lang.") &&
               !Number.class.isAssignableFrom(type) &&
               !Boolean.class.isAssignableFrom(type) &&
               !Character.class.isAssignableFrom(type);
    }

    /**
     * The core recursive method for dumping object fields.
     */
    private static void logObjectFields(CobbleAidLogger logger, Object obj, int depth, int maxDepth) {
        if (obj == null) {
            // Log null check message at INFO level
            logger.info(getIndentation(depth) + "Attempted to dump fields for a null object.");
            return;
        }

        if (depth > maxDepth) {
            // Log depth limit message at TRACE level (suppressed from INFO output)
            logger.trace(getIndentation(depth) + "[DEPTH LIMIT] Object of type {} reached max recursion depth ({}). Skipping...", 
                        obj.getClass().getSimpleName(), maxDepth);
            return;
        }

        Class<?> clazz = obj.getClass();
        String className = clazz.getName();
        String indent = getIndentation(depth);

        // --- Class Filtering Check ---
        // Skip reflection for core Java types to avoid InaccessibleObjectException.
        if (className.startsWith("java.") || 
            className.startsWith("javax.") ||
            className.startsWith("jdk.") ||
            className.startsWith("sun.")) 
        {
            // Log filtering at TRACE level (suppressed from INFO output)
            logger.trace(indent + "Skipping field dump for core Java type: {}", className);
            return;
        }
        // --- END Class Filtering Check ---

        // Log DUMPING marker at INFO level
        logger.info(indent + "--- DUMPING: {} (Depth: {}) ---", className, depth);

        Field[] fields = clazz.getDeclaredFields();

        if (fields.length == 0) {
            // Log 'No fields found' at INFO level
            logger.info(indent + "No fields found in class {}.", className);
            return;
        }

        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue = null;

            try {
                // Bypass Java language access checking (required for private fields)
                field.setAccessible(true); 
                
                fieldValue = field.get(obj);
                String typeName = field.getType().getSimpleName();
                
                // Get the full class name of the nested object for detailed output
                String nestedClassName = (fieldValue != null) ? fieldValue.getClass().getName() : "null";

                // SUCCESS messages are at INFO level
                if (field.getType().isArray()) {
                    // Handle arrays separately for clean output
                    String arrayString = (fieldValue != null) ? Arrays.toString((Object[]) fieldValue) : "null";
                     logger.info(indent + "  [SUCCESS] {}: {} (Type: {})", 
                                fieldName, arrayString, typeName);
                } else if (isComplexObject(field.getType())) {
                    // MODIFIED: Log the full class name instead of "<Nested Object>"
                    logger.info(indent + "  [SUCCESS] {}: <{}> (Type: {})", 
                                fieldName, nestedClassName, typeName);
                    // Recursive call with incremented depth
                    logObjectFields(logger, fieldValue, depth + 1, maxDepth);
                } else {
                    // Simple object, Wrapper, String, Enum, or Collection (prints via toString())
                    logger.info(indent + "  [SUCCESS] {}: {} (Type: {})", 
                                fieldName, fieldValue, typeName);
                }

            } catch (Exception e) {
                // Skip/Error messages are now at TRACE level (suppressed from INFO output)
                // Catching all reflection exceptions safely
                logger.trace(indent + "  [SKIP] Could not access field '{}'. Reason: {}. Continuing...", 
                            fieldName, 
                            e.getMessage());
            } 
        }
        // Log END DUMP marker at INFO level
        logger.info(indent + "--- END DUMP: {} ---", className);
    }
}