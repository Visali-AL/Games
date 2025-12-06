package pips_dlx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import pips.MathExpressionHelper;

public class DLXPipsSolver {

    private static final JsonNode input;
    private static final List<String> nodesList = new ArrayList<>();
    private static final List<Pair<Integer, Integer>> dominoList = new ArrayList<>();
    private static final boolean isDebugMode = false;
    private static final boolean solveAll = true;

    static {
        input = readInput();
    }

    public static JsonNode readInput() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("src/main/resources/pips_medium.json");
            return mapper.readTree(file);
        } catch (IOException e) {
            System.out.println("Failed to read input JSON. Check for file existence and format");
            return null;
        }
    }

    private static void getNodesFromPuzzle(JsonNode puzzle) {
        if (puzzle != null && puzzle.has("node_details")) {
            JsonNode nodeDetails = puzzle.get("node_details");
            nodeDetails.fields().forEachRemaining(entry -> nodesList.add(entry.getKey()));
            if (isDebugMode) {
                System.out.println("Number of nodes in the puzzle: " + nodesList.size());
            }
        }
    }

    private static void getDominoesFromPuzzle(JsonNode puzzle) {
        if (puzzle != null && puzzle.has("node_details")) {
            JsonNode dominoes = puzzle.get("dominoes");
            if (dominoes.isArray()) {
                for (JsonNode domino : dominoes) {
                    int pip1 = domino.get(0).asInt();
                    int pip2 = domino.get(1).asInt();
                    dominoList.add(Pair.of(pip1, pip2));
                }
            }
            if (isDebugMode) {
                System.out.println("Number of dominoes in the puzzle: " + dominoList.size());
            }
        }
    }

    public static void main(String[] args) {
        if (input != null && input.has("pips_medium_puzzles")) {
            JsonNode allPuzzles = input.get("pips_medium_puzzles");
            for (JsonNode puzzle : allPuzzles) {
                System.out.println("DLX PIPS Solver is running...");
                long start = System.currentTimeMillis();
                getNodesFromPuzzle(puzzle);
                getDominoesFromPuzzle(puzzle);
                buildExactCoverMatrix(puzzle);
                long end = System.currentTimeMillis();
                System.out.println("\nDLX PIPS Solver finished in " + (end - start) + " ms.");
                clearGameState();
            }
        }
    }

    private static void clearGameState() {
        nodesList.clear();
        dominoList.clear();
    }

    private static boolean assignDominoToNode(int left, int right, String currentNode, String peerNode, HashMap<String, Integer> assignedValues, JsonNode puzzle) {
        String subjectExpr = puzzle.get("node_details").get(currentNode).get("expression").asText();
        String followerExpr = puzzle.get("node_details").get(peerNode).get("expression").asText();

        HashMap<String, Integer> tempAssignedValues = new HashMap<>(assignedValues);
        tempAssignedValues.put(currentNode, left);
        tempAssignedValues.put(peerNode, right);

        return MathExpressionHelper.satisfies(subjectExpr, tempAssignedValues) &&
                MathExpressionHelper.satisfies(followerExpr, tempAssignedValues);
    }

    private static void buildExactCoverMatrix(JsonNode puzzle) {
        int columns = nodesList.size() + dominoList.size();
        List<int[]> exactCoverMatrix = new ArrayList<>();
        Set<String> peersSet = new HashSet<>();

        for (String node : nodesList) {
            JsonNode nodeDetails = puzzle.get("node_details").get(node);
            nodeDetails.get("peers").forEach(peerNode -> {
                if (peersSet.contains(node + peerNode.asText()) || peersSet.contains(peerNode.asText() + node)) {
                    return;
                }
                peersSet.add(node + peerNode.asText());
                for (Pair<Integer, Integer> domino : dominoList) {
                    if (assignDominoToNode(domino.getLeft(), domino.getRight(), node, peerNode.asText(), new HashMap<>(), puzzle) ||
                            (domino.getLeft().intValue() != domino.getRight().intValue() && assignDominoToNode(domino.getRight(), domino.getLeft(), node, peerNode.asText(), new HashMap<>(), puzzle))) {
                        int[] row = new int[columns];
                        int nodeIndex = nodesList.indexOf(node);
                        int peerIndex = nodesList.indexOf(peerNode.asText());
                        int dominoIndex = dominoList.indexOf(domino) + nodesList.size();

                        row[nodeIndex] = 1;
                        row[peerIndex] = 1;
                        row[dominoIndex] = 1;

                        exactCoverMatrix.add(row);
                    }
                }
            });
        }

        if (exactCoverMatrix.isEmpty()) {
            System.out.println("No valid placements found.");
            return;
        }

        int[][] matrix = exactCoverMatrix.toArray(new int[0][]);

        if (isDebugMode) {
            System.out.println("\nExact cover matrix: " + matrix.length + " rows x " + columns + " columns");
            for (int i = 0; i < matrix.length; i++) {
                System.out.println("Row " + i + ": " + Arrays.toString(matrix[i]));
            }
        }

        solveUsingDLX(matrix);
    }

    private static void solveUsingDLX(int[][] matrix) {

        DLX dlx = new DLX(matrix);
        List<List<Integer>> solutions = new ArrayList<>();

        if(!solveAll) {
            List<Integer> firstSolution = dlx.solveFirst();
            if(firstSolution != null) {
                solutions.add(firstSolution);
            }
        } else {
            solutions = dlx.solve();
            System.out.println("\nTotal solutions found: " + (solutions != null ? solutions.size() : 0));
        }

        if (solutions != null && !solutions.isEmpty()) {
            for (List<Integer> solution : solutions) {
                System.out.println("\nSolution found! Selected rows: " + solution);
                System.out.println("\nDomino placements:");
                for (int rowIndex : solution) {
                    int[] row = matrix[rowIndex];
                    List<String> coveredNodes = new ArrayList<>();
                    Pair<Integer, Integer> usedDomino = null;

                    for (int col = 0; col < row.length; col++) {
                        if (row[col] == 1) {
                            if (col < nodesList.size()) {
                                coveredNodes.add(nodesList.get(col));
                            } else {
                                usedDomino = dominoList.get(col - nodesList.size());
                            }
                        }
                    }
                    System.out.println("  Nodes " + coveredNodes + " <- Domino " + usedDomino);
                }
            }
        } else {
            System.out.println("\nNo solution exists.");
        }
    }
}
