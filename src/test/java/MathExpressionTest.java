import pips.MathExpressionEvaluator;
import pips.MathExpressionHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for pips.MathExpressionEvaluator and pips.MathExpressionHelper
 * with more challenging test cases.
 */
public class MathExpressionTest {

    public static void main(String[] args) {
        System.out.println("===== RUNNING COMPREHENSIVE MATH EXPRESSION TESTS =====\n");

        testBasicExpressions();
        testComplexExpressions();
        testEdgeCases();
        testExpressionHelper();
    }

    private static void testBasicExpressions() {
        System.out.println("===== Testing Basic Expressions =====");

        // Test case 1: Comparison - Greater than
        String expr1 = "A>4";
        Map<String, Integer> values1 = new HashMap<>();
        values1.put("A", 5);
        printTest(expr1, values1, MathExpressionEvaluator.evaluate(expr1, values1), true);

        // Test case 2: Comparison - Less than
        String expr2 = "B<3";
        Map<String, Integer> values2 = new HashMap<>();
        values2.put("B", 2);
        printTest(expr2, values2, MathExpressionEvaluator.evaluate(expr2, values2), true);

        // Test case 3: Equality - Multiple variables
        String expr3 = "A=B=C";
        Map<String, Integer> values3 = new HashMap<>();
        values3.put("A", 7);
        values3.put("B", 7);
        printTest(expr3, values3, MathExpressionEvaluator.evaluate(expr3, values3), true);

        // Test case 4: Equality - False case
        String expr4 = "A=B=C";
        Map<String, Integer> values4 = new HashMap<>();
        values4.put("A", 7);
        values4.put("B", 7);
        values4.put("C", 8);
        printTest(expr4, values4, MathExpressionEvaluator.evaluate(expr4, values4), false);

        System.out.println();
    }

    private static void testComplexExpressions() {
        System.out.println("===== Testing Complex Expressions =====");

        // Test case 1: Complex equation with multiple operations
        String expr1 = "A+B*C=D-E";
        Map<String, Integer> values1 = new HashMap<>();
        values1.put("A", 5);
        values1.put("B", 3);
        values1.put("C", 2);
        values1.put("D", 12);
        values1.put("E", 1);
        printTest(expr1, values1, MathExpressionEvaluator.evaluate(expr1, values1), true);

        // Test case 2: Nested parentheses
        String expr2 = "(A+B)*(C-D)=E";
        Map<String, Integer> values2 = new HashMap<>();
        values2.put("A", 3);
        values2.put("B", 2);
        values2.put("C", 7);
        values2.put("D", 2);
        values2.put("E", 25);
        printTest(expr2, values2, MathExpressionEvaluator.evaluate(expr2, values2), true);

        // Test case 3: Mixed comparison operators
        String expr3 = "A>B && C<=D";
        Map<String, Integer> values3 = new HashMap<>();
        values3.put("A", 10);
        values3.put("B", 5);
        values3.put("C", 3);
        values3.put("D", 3);
        // Our evaluator doesn't support logical operators, should return false
        printTest(expr3, values3, MathExpressionEvaluator.evaluate(expr3, values3), false);

        // Test case 4: Complex arithmetic with division
        String expr4 = "A+(B/C)*D=E";
        Map<String, Integer> values4 = new HashMap<>();
        values4.put("A", 10);
        values4.put("B", 9);
        values4.put("C", 3);
        values4.put("D", 2);
        values4.put("E", 16);
        printTest(expr4, values4, MathExpressionEvaluator.evaluate(expr4, values4), true);

        // Test case 5: Negative numbers
        String expr5 = "A-B-C=-D";
        Map<String, Integer> values5 = new HashMap<>();
        values5.put("A", 5);
        values5.put("B", 8);
        values5.put("C", 2);
        values5.put("D", 5);
        printTest(expr5, values5, MathExpressionEvaluator.evaluate(expr5, values5), true);

        System.out.println();
    }

    private static void testEdgeCases() {
        System.out.println("===== Testing Edge Cases =====");

        // Test case 1: Division by zero
        String expr1 = "A/B=C";
        Map<String, Integer> values1 = new HashMap<>();
        values1.put("A", 10);
        values1.put("B", 0);
        values1.put("C", 5);
        printTest(expr1, values1, MathExpressionEvaluator.evaluate(expr1, values1), false);

        // Test case 2: Missing variable - Equation should evaluate to true
        String expr2 = "A+B+C=30";
        Map<String, Integer> values2 = new HashMap<>();
        values2.put("A", 10);
        values2.put("B", 5);
        // C is missing
        printTest(expr2, values2, MathExpressionEvaluator.evaluate(expr2, values2), true);

        // Test case 3: Empty expression
        String expr3 = "";
        Map<String, Integer> values3 = new HashMap<>();
        printTest(expr3, values3, MathExpressionEvaluator.evaluate(expr3, values3), true);

        // Test case 4: Single variable
        String expr4 = "A";
        Map<String, Integer> values4 = new HashMap<>();
        values4.put("A", 1);
        printTest(expr4, values4, MathExpressionEvaluator.evaluate(expr4, values4), true);

        // Test case 5: Invalid syntax
        String expr5 = "A++B=C";
        Map<String, Integer> values5 = new HashMap<>();
        values5.put("A", 1);
        values5.put("B", 2);
        values5.put("C", 3);
        printTest(expr5, values5, MathExpressionEvaluator.evaluate(expr5, values5), false);

        System.out.println();
    }

    private static void testExpressionHelper() {
        System.out.println("===== Testing pips.MathExpressionHelper =====");

        // Test case 1: Single variable method
        String expr1 = "A>4";
        String node1 = "A";
        int value1 = 5;
        boolean result1 = MathExpressionHelper.satisfies(expr1, node1, value1);
        System.out.printf("Expression: %-15s Node: %-5s Value: %-5d Result: %-10s Expected: %-10s %s%n",
                expr1, node1, value1, result1, true, result1 == true ? "✓" : "✗");

        // Test case 2: Multi-variable method
        String expr2 = "(A+B)*C=D";
        Map<String, Integer> values2 = new HashMap<>();
        values2.put("A", 3);
        values2.put("B", 2);
        values2.put("C", 4);
        values2.put("D", 20);
        boolean result2 = MathExpressionHelper.satisfies(expr2, values2);
        System.out.printf("Expression: %-15s Values: %-30s Result: %-10s Expected: %-10s %s%n",
                expr2, values2, result2, true, result2 == true ? "✓" : "✗");

        // Test case 3: Complex inequality
        String expr3 = "A*(B+C-D)>E";
        Map<String, Integer> values3 = new HashMap<>();
        values3.put("A", 5);
        values3.put("B", 3);
        values3.put("C", 7);
        values3.put("D", 2);
        values3.put("E", 35);
        boolean result3 = MathExpressionHelper.satisfies(expr3, values3);
        System.out.printf("Expression: %-15s Values: %-30s Result: %-10s Expected: %-10s %s%n",
                expr3, values3, result3, true, result3 == true ? "✓" : "✗");

        // Test case 4: Complex equation with missing variable
        String expr4 = "A+(B-C)*(D/E)=F";
        Map<String, Integer> values4 = new HashMap<>();
        values4.put("A", 10);
        values4.put("B", 15);
        values4.put("C", 5);
        values4.put("D", 8);
        // E is missing
        values4.put("F", 30);
        boolean result4 = MathExpressionHelper.satisfies(expr4, values4);
        System.out.printf("Expression: %-15s Values: %-30s Result: %-10s Expected: %-10s %s%n",
                expr4, values4, result4, true, result4 == true ? "✓" : "✗");

        // Test case 5: Complex comparison with invalid operation
        String expr5 = "A^2 + B^2 = C^2";
        Map<String, Integer> values5 = new HashMap<>();
        values5.put("A", 3);
        values5.put("B", 4);
        values5.put("C", 5);
        boolean result5 = MathExpressionHelper.satisfies(expr5, values5);
        System.out.printf("Expression: %-15s Values: %-30s Result: %-10s Expected: %-10s %s%n",
                expr5, values5, result5, false, result5 == false ? "✓" : "✗");

        System.out.println();
    }

    private static void printTest(String expr, Map<String, Integer> values, boolean result, boolean expected) {
        System.out.printf("Expression: %-15s Values: %-30s Result: %-10s Expected: %-10s %s%n",
                expr, values, result, expected, result == expected ? "✓" : "✗");
    }
}