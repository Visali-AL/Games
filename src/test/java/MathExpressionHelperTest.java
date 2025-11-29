import pips.MathExpressionHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for pips.MathExpressionHelper.
 */
public class MathExpressionHelperTest {

    /**
     * Main method with test cases.
     */
    public static void main(String[] args) {
        System.out.println("===== MATH EXPRESSION HELPER TEST CASES =====\n");

        // Basic Tests
        System.out.println("--- Basic Tests ---");

        // Test 1: Simple comparison
        testSingleVariable("A>4", "A", 5, true);

        // Test 2: Simple comparison (false case)
        testSingleVariable("A>4", "A", 3, false);

        // Test 3: Simple equality with undefined variable
        testSingleVariable("A=B", "A", 3, true);  // true because B is undefined

        // Test 4: Simple arithmetic
        testSingleVariable("A+2=7", "A", 5, true);

        System.out.println("\n--- Complex Tests with Multiple Variables ---");

        // Test 5: Simple addition equation
        Map<String, Integer> values1 = createMap(
            new String[]{"A", "B", "C"},
            new int[]{2, 3, 5}
        );
        testMultiVariable("A+B+C=10", values1, true);

        // Test 6: Complex equation with operations
        Map<String, Integer> values2 = createMap(
            new String[]{"A", "B", "C", "D", "E"},
            new int[]{5, 3, 2, 12, 1}
        );
        testMultiVariable("A+B*C=D-E", values2, true);

        // Test 7: Nested parentheses
        Map<String, Integer> values3 = createMap(
            new String[]{"A", "B", "C", "D", "E"},
            new int[]{3, 2, 7, 2, 25}
        );
        testMultiVariable("(A+B)*(C-D)=E", values3, true);

        // Test 8: Division and complex operation
        Map<String, Integer> values4 = createMap(
            new String[]{"A", "B", "C", "D", "E"},
            new int[]{10, 9, 3, 2, 16}
        );
        testMultiVariable("A+(B/C)*D=E", values4, true);

        System.out.println("\n--- Edge Cases ---");

        // Test 9: Division by zero
        Map<String, Integer> values5 = createMap(
            new String[]{"A", "B", "C"},
            new int[]{10, 0, 5}
        );
        testMultiVariable("A/B=C", values5, false);

        // Test 10: Missing variable with valid domino solution
        Map<String, Integer> values6 = new HashMap<>();
        values6.put("A", 2);
        values6.put("B", 3);
        // C is missing but would need to be 5, which is within domino range
        testMultiVariable("A+B+C=10", values6, true);

        // Test 11: Pythagorean triple using power
        Map<String, Integer> values7 = createMap(
            new String[]{"A", "B", "C"},
            new int[]{3, 4, 5}
        );
        testMultiVariable("A^2 + B^2 = C^2", values7, false); // Our parser doesn't support power operation

        System.out.println("\n--- pips.Domino Constraint Tests ---");

        // Test 12: Missing variable requiring value outside domino range (0-6)
        Map<String, Integer> values8 = new HashMap<>();
        values8.put("A", 5);
        values8.put("B", 5);
        // C is missing and would need to be 8, which is outside domino range (0-6)
        testMultiVariableDomino("A+B+C=18", values8, false);

        // Test 13: Missing variable requiring negative value
        Map<String, Integer> values9 = new HashMap<>();
        values9.put("A", 1);
        values9.put("C", 5);
        // B is missing and would need to be -1, which is outside domino range (0-6)
        testMultiVariableDomino("A+B+C=5", values9, false);

        // Test 14: Missing variable requiring decimal value
        Map<String, Integer> values10 = new HashMap<>();
        values10.put("A", 2);
        values10.put("B", 2);
        // Note: This test is tricky because our evaluator simplifies C/2 first, then solves for C
        // which means C would need to be 2 (which is in range)
        testMultiVariableDomino("A+B+C/2=5", values10, true);

        // Test 15: Missing variable with valid domino value
        Map<String, Integer> values11 = new HashMap<>();
        values11.put("A", 2);
        values11.put("B", 1);
        // C is missing but would need to be 6, which is within domino range
        testMultiVariableDomino("A+B+C=9", values11, true);

        // Test 16: With domino constraints disabled (should allow any value)
        Map<String, Integer> values12 = new HashMap<>();
        values12.put("A", 5);
        values12.put("B", 5);
        // C is missing and would need to be 8, which is outside domino range, but constraints are disabled
        testMultiVariable("A+B+C=18", values12, true, false);
    }

    // Helper methods for testing
    private static void testSingleVariable(String expr, String node, int value, boolean expected) {
        boolean result = MathExpressionHelper.satisfies(expr, node, value);
        System.out.printf("Expression: %-15s Node: %-5s Value: %-5d Result: %-10s Expected: %-10s %s%n",
                expr, node, value, result, expected, result == expected ? "✓" : "✗");
    }

    private static void testMultiVariable(String expr, Map<String, Integer> values, boolean expected) {
        boolean result = MathExpressionHelper.satisfies(expr, values);
        System.out.printf("Expression: %-20s Values: %-35s Result: %-10s Expected: %-10s %s%n",
                expr, values, result, expected, result == expected ? "✓" : "✗");
    }

    private static void testMultiVariable(String expr, Map<String, Integer> values, boolean expected, boolean checkDominoConstraints) {
        boolean result = MathExpressionHelper.satisfies(expr, values, checkDominoConstraints);
        System.out.printf("Expression: %-20s Values: %-35s Result: %-10s Expected: %-10s %s DominoCheck=%b%n",
                expr, values, result, expected, result == expected ? "✓" : "✗", checkDominoConstraints);
    }

    private static void testMultiVariableDomino(String expr, Map<String, Integer> values, boolean expected) {
        boolean result = MathExpressionHelper.satisfies(expr, values, true);
        System.out.printf("Expression: %-20s Values: %-35s Result: %-10s Expected: %-10s %s [DominoConstraint]%n",
                expr, values, result, expected, result == expected ? "✓" : "✗");
    }

    private static Map<String, Integer> createMap(String[] keys, int[] values) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}