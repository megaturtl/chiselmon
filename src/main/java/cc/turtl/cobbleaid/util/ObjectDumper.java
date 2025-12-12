package cc.turtl.cobbleaid.util;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Improved, readable Object dumper:
 * - prints one logical line per field
 * - supports arrays and Collections
 * - avoids infinite recursion via identity-based visited set
 * - has a configurable max recursion depth
 */
public class ObjectDumper {

    private static final int DEFAULT_MAX_DEPTH = 3;
    private static final int INDENT_SPACES = 2;

    public static void logObjectFields(CobbleAidLogger logger, Object obj) {
        logObjectFields(logger, obj, DEFAULT_MAX_DEPTH);
    }

    public static void logObjectFields(CobbleAidLogger logger, Object obj, int maxDepth) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        if (obj == null) {
            logger.info("[ObjectDumper] Attempted to dump a null object.");
            return;
        }
        dump(logger, obj, 0, maxDepth, visited);
    }

    private static String indent(int depth) {
        return " ".repeat(Math.max(0, depth * INDENT_SPACES));
    }

    private static void dump(CobbleAidLogger logger, Object obj, int depth, int maxDepth, Set<Object> visited) {
        if (obj == null) {
            logger.info("{}null", indent(depth));
            return;
        }

        if (depth > maxDepth) {
            logger.info("{}[DEPTH LIMIT] {} (skipping details)", indent(depth), obj.getClass().getName());
            return;
        }

        // Avoid cycles by identity
        if (visited.contains(obj)) {
            logger.info("{}[CYCLE] {} (already visited)", indent(depth), obj.getClass().getName());
            return;
        }
        visited.add(obj);

        Class<?> clazz = obj.getClass();
        String header = clazz.getName();

        // Skip dumping full internals of JDK/core classes — print concise
        // representation instead
        if (isCoreJavaClass(clazz)) {
            logger.info("{}{} => {}", indent(depth), header, obj.toString());
            return;
        }

        logger.info("{}--- DUMP: {} ---", indent(depth), header);

        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            logger.info("{}(no declared fields)", indent(depth + 1));
            logger.info("{}--- END DUMP: {} ---", indent(depth), header);
            return;
        }

        for (Field field : fields) {
            String name = field.getName();
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                Class<?> type = field.getType();

                if (value == null) {
                    logger.info("{}{}: null (Type: {})", indent(depth + 1), name, type.getSimpleName());
                    continue;
                }

                if (type.isArray()) {
                    int length = Array.getLength(value);
                    List<String> elems = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        Object el = Array.get(value, i);
                        elems.add(simpleRepresentation(el));
                    }
                    logger.info("{}{}: [{}] (Type: {}[])",
                            indent(depth + 1), name, String.join(", ", elems), type.getComponentType().getSimpleName());
                } else if (Collection.class.isAssignableFrom(type)) {
                    Collection<?> col = (Collection<?>) value;
                    String repr = col.stream().map(ObjectDumper::simpleRepresentation).limit(20)
                            .collect(Collectors.joining(", "));
                    if (col.size() > 20)
                        repr += ", ... (+" + (col.size() - 20) + ")";
                    logger.info("{}{}: [{}] (Type: Collection, size={})", indent(depth + 1), name, repr, col.size());
                } else if (Map.class.isAssignableFrom(type)) {
                    Map<?, ?> m = (Map<?, ?>) value;
                    String repr = m.entrySet().stream()
                            .map(e -> simpleRepresentation(e.getKey()) + "=" + simpleRepresentation(e.getValue()))
                            .limit(20)
                            .collect(Collectors.joining(", "));
                    if (m.size() > 20)
                        repr += ", ... (+" + (m.size() - 20) + ")";
                    logger.info("{}{}: {{{}}} (Type: Map, size={})", indent(depth + 1), name, repr, m.size());
                } else if (isSimpleType(type)) {
                    logger.info("{}{}: {} (Type: {})", indent(depth + 1), name, simpleRepresentation(value),
                            type.getSimpleName());
                } else {
                    // Complex object: print summary and then recurse
                    logger.info("{}{}: <{}> (Type: {})", indent(depth + 1), name, value.getClass().getName(),
                            type.getSimpleName());
                    dump(logger, value, depth + 1, maxDepth, visited);
                }

            } catch (Exception ex) {
                logger.trace("{}[SKIP] {}: (could not access) - {}", indent(depth + 1), name, ex.getMessage());
            }
        }

        logger.info("{}--- END DUMP: {} ---", indent(depth), header);
    }

    private static boolean isSimpleType(Class<?> cls) {
        return cls.isPrimitive() ||
                Number.class.isAssignableFrom(cls) ||
                Boolean.class.isAssignableFrom(cls) ||
                Character.class.isAssignableFrom(cls) ||
                String.class.equals(cls) ||
                cls.isEnum() ||
                cls.getName().startsWith("java.");
    }

    private static boolean isCoreJavaClass(Class<?> cls) {
        String n = cls.getName();
        return n.startsWith("java.") || n.startsWith("javax.") || n.startsWith("sun.") || n.startsWith("jdk.");
    }

    private static String simpleRepresentation(Object o) {
        if (o == null)
            return "null";
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            List<String> items = new ArrayList<>(len);
            for (int i = 0; i < len && i < 10; i++) {
                items.add(simpleRepresentation(Array.get(o, i)));
            }
            if (len > 10)
                items.add("... (+" + (len - 10) + ")");
            return "[" + String.join(", ", items) + "]";
        }
        if (o instanceof Collection<?> c) {
            return "[" + c.stream().map(ObjectDumper::simpleRepresentation).limit(10).collect(Collectors.joining(", "))
                    + "]";
        }
        if (o instanceof Map<?, ?> m) {
            return "{" + m.entrySet().stream()
                    .map(e -> simpleRepresentation(e.getKey()) + "=" + simpleRepresentation(e.getValue())).limit(10)
                    .collect(Collectors.joining(", ")) + "}";
        }
        return o.toString();
    }
}