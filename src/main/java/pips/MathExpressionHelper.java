package pips;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide math expression evaluation functionality.
 * Serves as a helper for evaluating expressions with variables.
 */
public class MathExpressionHelper {

    /**
     * Evaluates if a mathematical expression is satisfied with the given node and value.
     * This method enforces domino pip constraints (values must be between 0 and 6).
     *
     * @param expression The expression to evaluate (e.g., "A>4", "A=B=C", "A+B+C=10")
     * @param node The node name to use as a variable
     * @param value The value to assign to the node
     * @return true if the expression is satisfied, false otherwise
     */
    public static boolean satisfies(String expression, String node, int value) {
        Map<String, Integer> varValues = new HashMap<>();
        varValues.put(node, value);

        return MathExpressionEvaluator.evaluate(expression, varValues, true);
    }

    /**
     * Evaluates if a mathematical expression is satisfied with the given variable values.
     * This method enforces domino pip constraints (values must be between 0 and 6).
     *
     * @param expression The expression to evaluate
     * @param varValues Map of variable names to their values
     * @return true if the expression is satisfied, false otherwise
     */
    public static boolean satisfies(String expression, Map<String, Integer> varValues) {
        if(expression.contains("ANY"))
            return true;
        return MathExpressionEvaluator.evaluate(expression, varValues, true);
    }

    /**
     * Evaluates if a mathematical expression is satisfied with the given variable values,
     * with optional enforcement of domino pip constraints.
     *
     * @param expression The expression to evaluate
     * @param varValues Map of variable names to their values
     * @param checkDominoConstraints Whether to check if unknown variables can have valid domino values
     * @return true if the expression is satisfied, false otherwise
     */
    public static boolean satisfies(String expression, Map<String, Integer> varValues, boolean checkDominoConstraints) {
        return MathExpressionEvaluator.evaluate(expression, varValues, checkDominoConstraints);
    }

}