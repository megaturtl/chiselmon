package cc.turtl.chiselmon.feature.spawnalert.condition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;

/**
 * Unit tests for the AlertConditionRegistry.
 * 
 * <p>These tests demonstrate how the Strategy pattern enables testable,
 * decoupled condition evaluation without requiring Minecraft runtime.
 */
public class AlertConditionRegistryTest {

    @BeforeEach
    void setUp() {
        // Clear registry before each test to ensure isolation
        AlertConditionRegistry.clear();
    }

    @Test
    void registryStartsEmpty() {
        assertTrue(AlertConditionRegistry.getConditions().isEmpty());
    }

    @Test
    void canRegisterSingleCondition() {
        AlertCondition mockCondition = (entity, config) -> AlertPriority.CUSTOM;
        
        AlertConditionRegistry.register(mockCondition);
        
        assertEquals(1, AlertConditionRegistry.getConditions().size());
    }

    @Test
    void canRegisterMultipleConditions() {
        AlertCondition condition1 = (entity, config) -> AlertPriority.CUSTOM;
        AlertCondition condition2 = (entity, config) -> AlertPriority.SHINY;
        AlertCondition condition3 = (entity, config) -> AlertPriority.LEGENDARY;
        
        AlertConditionRegistry.registerAll(condition1, condition2, condition3);
        
        assertEquals(3, AlertConditionRegistry.getConditions().size());
    }

    @Test
    void clearRemovesAllConditions() {
        AlertConditionRegistry.register((entity, config) -> AlertPriority.CUSTOM);
        AlertConditionRegistry.register((entity, config) -> AlertPriority.SHINY);
        
        AlertConditionRegistry.clear();
        
        assertTrue(AlertConditionRegistry.getConditions().isEmpty());
    }

    @Test
    void getConditionsReturnsUnmodifiableList() {
        AlertConditionRegistry.register((entity, config) -> AlertPriority.CUSTOM);
        
        var conditions = AlertConditionRegistry.getConditions();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            conditions.add((entity, config) -> AlertPriority.SHINY);
        });
    }
}
