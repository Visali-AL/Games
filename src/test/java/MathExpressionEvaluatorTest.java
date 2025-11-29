import pips.MathExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for pips.MathExpressionEvaluator.
 */
public class MathExpressionEvaluatorTest {

    /**
     * Main method with test cases.
     */
    public static void main(String[] args) {
        System.out.println("===== RUNNING MATH EXPRESSION EVALUATOR TESTS =====\n");

        // Test case 1: A > 4 with A = 5
        String expr1 = "A>4";
        Map<String, Integer> values1 = new HashMap<>();
        values1.put("A", 5);
        System.out.println("Expression: " + expr1 + ", Values: " + values1);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr1, values1)); // Should be true

        // Test case 2: A = B = C with A = 1, B = 1, C = 1
        String expr2 = "A=B=C";
        Map<String, Integer> values2 = new HashMap<>();
        values2.put("A", 1);
        values2.put("B", 1);
        values2.put("C", 1);
        System.out.println("Expression: " + expr2 + ", Values: " + values2);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr2, values2)); // Should be true

        // Test case 3: A + B + C = 10 with A = 2, B = 3, C = 5
        String expr3 = "A+B+C=10";
        Map<String, Integer> values3 = new HashMap<>();
        values3.put("A", 2);
        values3.put("B", 3);
        values3.put("C", 5);
        System.out.println("Expression: " + expr3 + ", Values: " + values3);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr3, values3)); // Should be true

        // Test case 4: A + B + C = 10 with A = 1, B = 2, C = 3
        String expr4 = "A=B=C";
        Map<String, Integer> values4 = new HashMap<>();
        values4.put("A", 1);
        values4.put("B", 1);
        values4.put("C", 3);
        System.out.println("Expression: " + expr4 + ", Values: " + values4);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr4, values4)); // Should be false

        // Test case 5: A > B with A = 3, no value for B
        String expr5 = "A>B";
        Map<String, Integer> values5 = new HashMap<>();
        values5.put("A", 3);
        System.out.println("Expression: " + expr5 + ", Values: " + values5);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr5, values5)); // Should be true (missing variables)

        // Test case 6: Missing variable requiring value outside domino range (0-6)
        String expr6 = "A+B+C=18";
        Map<String, Integer> values6 = new HashMap<>();
        values6.put("A", 5);
        values6.put("B", 5);
        // C is missing and would need to be 8, which is outside domino range (0-6)
        System.out.println("Expression: " + expr6 + ", Values: " + values6);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr6, values6, true)); // Should be false
        System.out.println("Result without domino check: " + MathExpressionEvaluator.evaluate(expr6, values6, false)); // Should be true

        // Test case 7: Missing variable with valid domino value
        String expr7 = "A+B+C=9";
        Map<String, Integer> values7 = new HashMap<>();
        values7.put("A", 2);
        values7.put("B", 1);
        // C is missing but would need to be 6, which is within domino range
        System.out.println("Expression: " + expr7 + ", Values: " + values7);
        System.out.println("Result: " + MathExpressionEvaluator.evaluate(expr7, values7, true)); // Should be true
    }
}