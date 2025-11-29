package pips;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates mathematical expressions with variables.
 * Supports expressions like: Comparisons, Equalities and Equations
 */
public class MathExpressionEvaluator {

    /**
     * Maximum allowed value for a domino pip
     */
    public static final int MAX_DOMINO_VALUE = 6;

    /**
     * Minimum allowed value for a domino pip
     */
    public static final int MIN_DOMINO_VALUE = 0;

    /**
     * Evaluates a mathematical expression with given variable values.
     *
     * @param expr The expression to evaluate (e.g., "A>4", "A=B=C", "A+B+C=10")
     * @param varValues Map of variable names to their values
     * @return true if the expression is satisfied or if not all variables have values,
     *         false if the expression is not satisfied
     */
    public static boolean evaluate(String expr, Map<String, Integer> varValues) {
        return evaluate(expr, varValues, true);
    }

    /**
     * Evaluates a mathematical expression with given variable values.
     *
     * @param expr The expression to evaluate (e.g., "A>4", "A=B=C", "A+B+C=10")
     * @param varValues Map of variable names to their values
     * @param checkDominoConstraints Whether to check domino constraints (values 0-6)
     * @return true if the expression is satisfied or if not all variables have values,
     *         false if the expression is not satisfied
     */
    public static boolean evaluate(String expr, Map<String, Integer> varValues, boolean checkDominoConstraints) {
        if (varValues == null) {
            varValues = new HashMap<>();
        }

        // Handle empty expression
        if (expr == null || expr.trim().isEmpty()) {
            return true;
        }

        // Extract all variables from the expression
        Set<String> variables = extractVariables(expr);

        // Check if all variables have values
        Set<String> missingVariables = new HashSet<>();
        for (String var : variables) {
            if (!varValues.containsKey(var)) {
                missingVariables.add(var);
            }
        }

        // If we have missing variables and need to check domino constraints
        if (!missingVariables.isEmpty() && checkDominoConstraints) {
            // For now, only handle equation case (with = sign)
            if (expr.contains("=") &&
               (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/"))) {
                return canSatisfyEquationWithDominoValues(expr, varValues, missingVariables);
            }

            // For equality expressions like B=C=D where B and C are equal and D is missing
            if (expr.contains("=") && !expr.contains("+") && !expr.contains("-") &&
                !expr.contains("*") && !expr.contains("/")) {
                // Check if all known variables have the same value
                Integer commonValue = null;
                boolean allEqual = true;

                for (String var : variables) {
                    if (varValues.containsKey(var)) {
                        if (commonValue == null) {
                            commonValue = varValues.get(var);
                        } else if (!varValues.get(var).equals(commonValue)) {
                            return false;
                        }
                    }
                }

                // If all known variables are equal, return true as missing variables
                // could potentially have the same value
                if (allEqual && commonValue != null) {
                    return true;
                }
            }

            // Default behavior for other cases
            if(missingVariables.size() > 1)
                return true;
        } else if (!missingVariables.isEmpty()) {
            // If we don't need to check domino constraints, use the old behavior
            return true;
        }

        // Replace variables with their values
        String exprWithValues = replaceVariables(expr, varValues);

        // Handle different types of expressions
        if (exprWithValues.contains(">") || exprWithValues.contains("<") ||
            exprWithValues.contains(">=") || exprWithValues.contains("<=")) {
            return evaluateComparison(exprWithValues);
        } else if (exprWithValues.contains("=")) {
            if (exprWithValues.contains("+") || exprWithValues.contains("-") ||
                exprWithValues.contains("*") || exprWithValues.contains("/")) {
                return evaluateEquation(exprWithValues);
            } else {
                return evaluateEquality(exprWithValues);
            }
        } else {
            // Simple expressions like A+B or A*B without comparison or equality
            try {
                double result = evaluateArithmeticExpression(exprWithValues);
                return result != 0; // Any non-zero value is considered true
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Checks if an equation with missing variables can be satisfied with domino values (0-6).
     *
     * @param expr The expression to evaluate
     * @param varValues Map of variable names to their values
     * @param missingVariables Set of variables that are missing from varValues
     * @return true if the equation can be satisfied with domino values, false otherwise
     */
    private static boolean canSatisfyEquationWithDominoValues(String expr, Map<String, Integer> varValues,
                                                             Set<String> missingVariables) {
        try {
            // Currently only handle addition equations with one missing variable
            if (missingVariables.size() == 1 && expr.contains("+") && expr.contains("=")) {
                String missingVar = missingVariables.iterator().next();

                // Split into left and right sides
                String[] sides = expr.split("=");
                if (sides.length != 2) return true; // Only handle simple equations

                String leftSide = sides[0].trim();
                String rightSide = sides[1].trim();

                // Determine which side has the missing variable
                boolean missingOnLeft = leftSide.contains(missingVar);
                String sideWithMissing = missingOnLeft ? leftSide : rightSide;
                String sideWithoutMissing = missingOnLeft ? rightSide : leftSide;

                // Calculate the value of the side without the missing variable
                double knownValue;
                try {
                    // Replace all known variables with their values
                    String exprWithKnownValues = replaceVariables(sideWithoutMissing, varValues);
                    knownValue = evaluateArithmeticExpression(exprWithKnownValues);
                } catch (Exception e) {
                    return true; // If we can't evaluate, assume it's possible
                }

                // For addition equations of the form A+B+C=value or value=A+B+C
                if (missingOnLeft) {
                    // Compute what the missing variable would need to be: missing = knownValue - (sum of known vars on left)
                    String leftExprWithoutMissing = sideWithMissing.replaceAll("\\b" + missingVar + "\\b", "0");
                    String leftExprWithKnownValues = replaceVariables(leftExprWithoutMissing, varValues);

                    try {
                        double leftValueWithoutMissing = evaluateArithmeticExpression(leftExprWithKnownValues);
                        double missingValue = knownValue - leftValueWithoutMissing;

                        // Check if the missing value is within domino range
                        return isInDominoRange(missingValue);
                    } catch (Exception e) {
                        return true; // If we can't evaluate, assume it's possible
                    }
                } else {
                    // Similar logic for when missing variable is on the right side
                    String rightExprWithoutMissing = sideWithMissing.replaceAll("\\b" + missingVar + "\\b", "0");
                    String rightExprWithKnownValues = replaceVariables(rightExprWithoutMissing, varValues);

                    try {
                        double rightValueWithoutMissing = evaluateArithmeticExpression(rightExprWithKnownValues);
                        double missingValue = knownValue - rightValueWithoutMissing;

                        // Check if the missing value is within domino range
                        return isInDominoRange(missingValue);
                    } catch (Exception e) {
                        return true; // If we can't evaluate, assume it's possible
                    }
                }
            } else if (missingVariables.size() > 1) {
                // For multiple missing variables, we'd need to check if there's any possible combination
                // of domino values that could satisfy the equation.
                // This is a more complex problem and would require solving systems of equations or
                // checking constraints. For now, we'll just return true for these cases.
                return true;
            }

            // Default to true for other cases we don't handle
            return true;
        } catch (Exception e) {
            // If there's any error in the calculation, default to true
            return true;
        }
    }

    /**
     * Checks if a value is within the valid domino range (0-6).
     *
     * @param value The value to check
     * @return true if the value is within the domino range, false otherwise
     */
    private static boolean isInDominoRange(double value) {
        // Check if value is an integer within the domino range
        return value >= MIN_DOMINO_VALUE && value <= MAX_DOMINO_VALUE &&
               Math.abs(value - Math.round(value)) < 0.0001; // Check if it's effectively an integer
    }

    /**
     * Extract all variable names from the expression.
     */
    private static Set<String> extractVariables(String expr) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("[A-Za-z]+"); // Match one or more letters
        Matcher matcher = pattern.matcher(expr);

        while (matcher.find()) {
            variables.add(matcher.group());
        }

        return variables;
    }

    /**
     * Replace variables in the expression with their values.
     */
    private static String replaceVariables(String expr, Map<String, Integer> varValues) {
        // Use StringBuilder for string manipulation
        StringBuilder result = new StringBuilder(expr);

        // Sort variables by length (descending) to avoid partial replacements
        // For example, if we have variables 'A' and 'ABC', we want to replace 'ABC' first
        varValues.keySet().stream()
            .sorted((a, b) -> b.length() - a.length())
            .forEach(var -> {
                // Regular expression to match the variable name as a whole word
                String regex = "\\b" + var + "\\b";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(result);

                // Create a new StringBuilder with replacements
                StringBuilder temp = new StringBuilder();
                int lastEnd = 0;

                while (matcher.find()) {
                    // Append everything from lastEnd to the start of the current match
                    if (matcher.start() > lastEnd) {
                        temp.append(result.substring(lastEnd, matcher.start()));
                    }
                    temp.append(varValues.get(var).toString());
                    lastEnd = matcher.end();
                }

                // Append the remaining part
                if (lastEnd < result.length()) {
                    temp.append(result.substring(lastEnd));
                }

                // Update the result
                if (!temp.isEmpty()) {
                    result.setLength(0);
                    result.append(temp);
                }
            });

        return result.toString();
    }

    /**
     * Evaluate comparison expressions like A>4 or B<C.
     */
    private static boolean evaluateComparison(String expr) {
        try {
            // Handle >=, <=, >, <
            if (expr.contains(">=")) {
                String[] parts = expr.split(">=");
                double left = evaluateArithmeticExpression(parts[0].trim());
                double right = evaluateArithmeticExpression(parts[1].trim());
                return left >= right;
            } else if (expr.contains("<=")) {
                String[] parts = expr.split("<=");
                double left = evaluateArithmeticExpression(parts[0].trim());
                double right = evaluateArithmeticExpression(parts[1].trim());
                return left <= right;
            } else if (expr.contains(">")) {
                String[] parts = expr.split(">");
                double left = evaluateArithmeticExpression(parts[0].trim());
                double right = evaluateArithmeticExpression(parts[1].trim());
                return left > right;
            } else if (expr.contains("<")) {
                String[] parts = expr.split("<");
                double left = evaluateArithmeticExpression(parts[0].trim());
                double right = evaluateArithmeticExpression(parts[1].trim());
                return left < right;
            }
            return false;
        } catch (Exception e) {
            // If there's an exception (likely due to a missing variable),
            // we should return true since the comparison might be valid
            // once all variables have values
            return true;
        }
    }

    /**
     * Evaluate equality expressions like A=B=C.
     */
    private static boolean evaluateEquality(String expr) {
        try {
            String[] parts = expr.split("=");
            if (parts.length < 2) {
                return false;
            }

            double firstValue = evaluateArithmeticExpression(parts[0].trim());
            for (int i = 1; i < parts.length; i++) {
                double value = evaluateArithmeticExpression(parts[i].trim());
                if (Math.abs(value - firstValue) > 0.0001) { // Use epsilon for double comparison
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // If there's an exception (like a missing variable causing "Unexpected character"),
            // we should not automatically fail - it may be a missing variable case that could
            // potentially be valid in the future
            return true;
        }
    }

    /**
     * Evaluate equation expressions like A+B=C or A+B+C=10.
     */
    private static boolean evaluateEquation(String expr) {
        try {
            String[] sides = expr.split("=");
            if (sides.length != 2) {
                return false;
            }

            double leftSide = evaluateArithmeticExpression(sides[0].trim());
            double rightSide = evaluateArithmeticExpression(sides[1].trim());

            return Math.abs(leftSide - rightSide) < 0.0001; // Use epsilon for double comparison
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Evaluates a simple arithmetic expression using a basic parsing algorithm.
     * Supports addition, subtraction, multiplication, division, and parentheses.
     *
     * @param expr The arithmetic expression to evaluate
     * @return The result of the evaluation
     * @throws Exception If the expression is invalid
     */
    private static double evaluateArithmeticExpression(String expr) throws Exception {
        return new ExpressionParser().parse(expr);
    }

    /**
     * A simple expression parser for basic arithmetic operations.
     */
    private static class ExpressionParser {
        private String expr;
        private int pos;
        private char ch;

        /**
         * Parse and evaluate an arithmetic expression.
         */
        public double parse(String expr) throws Exception {
            this.expr = expr;
            this.pos = 0;

            if (expr.isEmpty()) {
                return 0;
            }

            nextChar();
            double value = parseExpression();

            if (pos < expr.length()) {
                throw new Exception("Unexpected character: " + ch);
            }

            return value;
        }

        /**
         * Move to the next character in the expression.
         */
        private void nextChar() {
            ch = (pos < expr.length()) ? expr.charAt(pos++) : '\0';
        }

        /**
         * Skip whitespace in the expression.
         */
        private void skipWhitespace() {
            while (Character.isWhitespace(ch)) {
                nextChar();
            }
        }

        /**
         * Parse an expression (addition or subtraction).
         */
        private double parseExpression() throws Exception {
            double value = parseTerm();

            skipWhitespace();
            while (ch == '+' || ch == '-') {
                char operator = ch;
                nextChar();
                double term = parseTerm();

                if (operator == '+') {
                    value += term;
                } else {
                    value -= term;
                }

                skipWhitespace();
            }

            return value;
        }

        /**
         * Parse a term (multiplication or division).
         */
        private double parseTerm() throws Exception {
            double value = parseFactor();

            skipWhitespace();
            while (ch == '*' || ch == '/') {
                char operator = ch;
                nextChar();
                double factor = parseFactor();

                if (operator == '*') {
                    value *= factor;
                } else {
                    if (Math.abs(factor) < 1e-10) {
                        throw new ArithmeticException("Division by zero");
                    }
                    value /= factor;
                }

                skipWhitespace();
            }

            return value;
        }

        /**
         * Parse a factor (number, parenthesized expression, or negative factor).
         */
        private double parseFactor() throws Exception {
            skipWhitespace();

            // Handle parentheses
            if (ch == '(') {
                nextChar();
                double value = parseExpression();

                skipWhitespace();
                if (ch != ')') {
                    throw new Exception("Missing closing parenthesis");
                }

                nextChar();
                return value;
            }

            // Handle negative sign
            if (ch == '-') {
                nextChar();
                return -parseFactor();
            }

            // Handle numbers
            if (Character.isDigit(ch) || ch == '.') {
                StringBuilder sb = new StringBuilder();

                // Get all digits and possible decimal point
                while ((Character.isDigit(ch) || ch == '.') && ch != '\0') {
                    sb.append(ch);
                    nextChar();
                }

                return Double.parseDouble(sb.toString());
            }

            throw new Exception("Unexpected character: " + ch);
        }
    }

}