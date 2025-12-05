package pips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PipsSolver {

    private static final JsonNode input;
    private static final LinkedHashMap<String, JsonNode> nodePartnerOrder = new LinkedHashMap<>();
    private static final HashMap<Integer, List<Domino>> availableDominoes = new HashMap<>();
    private static boolean debugEnabled = false;
    static final int MAX_DOMINO_VALUE = 7;
    private static boolean isPuzzleSolved = false;

    static {
        input = readInput();
    }

    private static JsonNode readInput() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("src/main/resources/pips_medium.json");
            return mapper.readTree(file);
        } catch (IOException e) {
            System.out.println("Failed to read input JSON. Check for file existence and format");
            return null;
        }
    }

    private static boolean isJsonLoaded() {
        return input != null;
    }

    private void orderDominoes() {
        if (input != null && input.has("pips_medium_puzzles")) {
            JsonNode puzzle = input.get("pips_medium_puzzles");
            JsonNode dominoes = puzzle.get(0).get("dominoes");
            if (dominoes != null && dominoes.isArray()) {
                for (JsonNode domino : dominoes) {
                    if (domino.isArray() && domino.size() == 2) {
                        int pip1 = domino.get(0).asInt();
                        int pip2 = domino.get(1).asInt();

                        availableDominoes.computeIfAbsent(pip1, k -> new ArrayList<>()).add(new Domino(pip1, pip2));
                        availableDominoes.computeIfAbsent(pip2, k -> new ArrayList<>()).add(new Domino(pip2, pip1));
                    }
                }
            }
        }
    }

    private boolean solvePipsPuzzle(String currentNode, LinkedHashSet<String> localNodePartnerOrder, HashMap<Integer, List<Domino>> availableDominoes, HashMap<String, Integer> assignedValues) {

        for(JsonNode node: nodePartnerOrder.get(currentNode).get("peers")) {
            String nodeName = node.asText();
            if(!assignedValues.containsKey(nodeName)) {
                if(debugEnabled)
                    System.out.println("\n******** Solving for "+ currentNode + nodeName+" ********");
                for(int pip = 0; pip <= MAX_DOMINO_VALUE; pip++) {
                    List<Domino> dominoesWithPip = availableDominoes.get(pip);
                    if(dominoesWithPip!= null && !dominoesWithPip.isEmpty()) {
                        for (Domino d : dominoesWithPip) {
                            if(debugEnabled)
                                System.out.println("\nUsing "+ d +" for "+ currentNode + nodeName);
                            if(assignDominoToNode(d, currentNode, node.asText(), assignedValues)) {
                                if(debugEnabled)
                                    System.out.println("Assigned "+ d +" to "+ currentNode + nodeName);
                                HashMap<String, Integer> updatedAssignedValues = new HashMap<>(assignedValues);
                                updatedAssignedValues.put(currentNode, d.pip1());
                                updatedAssignedValues.put(nodeName, d.pip2());

                                HashMap<Integer, List<Domino>> updatedAvailableDominoes = new HashMap<>();
                                for (Map.Entry<Integer, List<Domino>> entry : availableDominoes.entrySet()) {
                                    updatedAvailableDominoes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                                }
                                int pip1 = d.pip1(); int pip2 = d.pip2();
                                updatedAvailableDominoes.get(pip1).remove(d);
                                updatedAvailableDominoes.get(pip2).removeIf(domino -> domino.pip1() == pip2 && domino.pip2() == pip1);

                                LinkedHashSet<String> updatedLocalNodePartnerOrder = new LinkedHashSet<>(localNodePartnerOrder);
                                updatedLocalNodePartnerOrder.removeAll(updatedAssignedValues.keySet());
                                if(updatedLocalNodePartnerOrder.isEmpty() && !isPuzzleSolved) {
                                    isPuzzleSolved = true;
                                    System.out.println("********* Puzzle Solved *********" + updatedAssignedValues);
                                    System.out.println("Remaining domino: " + updatedAvailableDominoes);
                                    return true;
                                }
                                for(JsonNode peerNode: nodePartnerOrder.get(nodeName).get("peers")) {
                                    if(updatedAssignedValues.containsKey(peerNode.asText()))
                                        continue;
                                    String peerNodeName = peerNode.asText();
                                    boolean result = solvePipsPuzzle(peerNodeName, updatedLocalNodePartnerOrder, updatedAvailableDominoes, updatedAssignedValues);
                                    if(result) {
                                        if(updatedAssignedValues.size() == nodePartnerOrder.size()) {
                                            System.out.println("********* Puzzle Solved *********" + updatedAssignedValues);
                                            return true;
                                        }
                                    }
                                }

                                if(!updatedLocalNodePartnerOrder.isEmpty()) {
                                    boolean result = solvePipsPuzzle(updatedLocalNodePartnerOrder.stream().toList().get(0), updatedLocalNodePartnerOrder, updatedAvailableDominoes, updatedAssignedValues);
                                    if (result && !isPuzzleSolved) {
                                        isPuzzleSolved = true;
                                        return true;
                                    }
                                }
                            } else {
                                if(debugEnabled)
                                    System.out.println("Failed to assign "+ d +" to "+ nodeName);
                            }
                        }
                    }
                }
                if(debugEnabled)
                    System.out.println("Going to next peer of "+ currentNode);
            }
        }
        if(debugEnabled)
            System.out.println("Backtracking from "+ currentNode);
        return false;
    }

    private boolean assignDominoToNode(Domino d, String currentNode, String peerNode, HashMap<String, Integer> assignedValues) {
        JsonNode subject = nodePartnerOrder.get(currentNode);
        JsonNode follower = nodePartnerOrder.get(peerNode);

        String subjectExpr = subject.get("expression").asText();
        String followerExpr = follower.get("expression").asText();

        HashMap<String, Integer> tempAssignedValues = new HashMap<>(assignedValues);
        tempAssignedValues.put(currentNode, d.pip1());
        tempAssignedValues.put(peerNode, d.pip2());

        return  MathExpressionHelper.satisfies(subjectExpr, tempAssignedValues) &&
               MathExpressionHelper.satisfies(followerExpr, tempAssignedValues);
    }

    private void sortNodesBasedOnPartners() {
        if (input != null && input.has("pips_medium_puzzles")) {
            JsonNode puzzle = input.get("pips_medium_puzzles");
            JsonNode nodeDetails = puzzle.get(0).get("node_details");
            if (nodeDetails != null && nodeDetails.isObject()) {
                nodeDetails.fields().forEachRemaining(entry -> nodePartnerOrder.put(entry.getKey(), entry.getValue()));
                List<Map.Entry<String, JsonNode>> entries = new ArrayList<>(nodePartnerOrder.entrySet());
                entries.sort(Comparator.comparingInt(e -> e.getValue().has("partners_in_expression") ? e.getValue().get("partners_in_expression").size() : Integer.MAX_VALUE));
                nodePartnerOrder.clear();
                for (Map.Entry<String, JsonNode> entry : entries) {
                    nodePartnerOrder.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void printNodePartnerOrder() {
        System.out.println("********* Sorted nodes based on partners_in_expression count *********");
        System.out.println(nodePartnerOrder.keySet());
        if(debugEnabled) {
            for (Map.Entry<String, JsonNode> entry : nodePartnerOrder.entrySet()) {
                String nodeName = entry.getKey();
                JsonNode node = entry.getValue();
                int partnersCount = node.has("partners_in_expression") ? node.get("partners_in_expression").size() : -1;
                System.out.println("Node: " + nodeName + ", partners_in_expression count: " + partnersCount);
                System.out.println(node.toPrettyString());
            }
        }
    }

    public static void main(String[] args) {
        PipsSolver solver = new PipsSolver();
        if(isJsonLoaded()) {

            solver.sortNodesBasedOnPartners();
            solver.printNodePartnerOrder();
            solver.orderDominoes();
            solver.printDominoes();

            assert !nodePartnerOrder.isEmpty();
            String currentNode = nodePartnerOrder.entrySet().iterator().next().getKey();
            LinkedHashSet<String> availableNodes = new LinkedHashSet<>( nodePartnerOrder.keySet());
            availableNodes.remove(currentNode);

            solver.solvePipsPuzzle(currentNode, availableNodes, new HashMap<>(availableDominoes), new HashMap<>());
        }
    }

    private void printDominoes() {
        System.out.println("********* Available Dominoes grouped by pip *********");
        for (Map.Entry<Integer, List<Domino>> entry : availableDominoes.entrySet()) {
            System.out.println("Pip: " + entry.getKey() + " found in Dominoes: " + entry.getValue());
        }
    }
}

record Domino(int pip1, int pip2) {}
