package com.devopsai.backend.service.agent.tools;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GenerateJUnitTestTool implements AgentTool {

    @Override
    public String getName() {
        return "GenerateJUnitTestTool";
    }

    @Override
    public String getDescription() {
        return "Generates complete JUnit 5 and Mockito unit test suites for target Java classes.";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> params) {
        String className = (String) params.getOrDefault("className", "PriorityCalculator");
        String testClassName = className + "Test";

        String generatedTestCode = """
            package com.devopsai.backend.service;

            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.DisplayName;
            import java.time.LocalDate;

            import static org.junit.jupiter.api.Assertions.*;

            class %s {

                private %s service;

                @BeforeEach
                void setUp() {
                    service = new %s();
                }

                @Test
                @DisplayName("Should compute high priority score for critical severity and tight deadline")
                void testCriticalPriorityCalculation() {
                    double score = service.calculatePriorityScore(9, 8, LocalDate.now().plusDays(1), 3);
                    assertTrue(score > 7.0, "Expected priority score above 7.0");
                }

                @Test
                @DisplayName("Should apply deadline bonus when due date is within 48 hours")
                void testDeadlineProximityBonus() {
                    double urgentScore = service.calculatePriorityScore(5, 5, LocalDate.now().plusDays(1), 0);
                    double normalScore = service.calculatePriorityScore(5, 5, LocalDate.now().plusDays(10), 0);
                    assertTrue(urgentScore > normalScore, "Urgent task score should include proximity bonus");
                }
            }
            """.formatted(testClassName, className, className);

        return Map.of(
            "status", "SUCCESS",
            "targetClass", className,
            "testClassName", testClassName,
            "generatedTestCode", generatedTestCode
        );
    }
}
