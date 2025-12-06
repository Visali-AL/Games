package pips_dlx;

import com.google.gson.*;

import java.io.*;
import java.util.*;

public class PipsJsonConverter {

    public static void main(String[] args) {
        String inputFile = "src/main/resources/pips_general.json";
        String outputFile = "src/main/resources/pips_modified.json";

        try {
            convert(inputFile, outputFile);
            System.out.println("Conversion complete. Output written to: " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convert(String inputFile, String outputFile) throws IOException {
        // Read input file
        JsonObject input;
        try (FileReader reader = new FileReader(inputFile)) {
            input = JsonParser.parseReader(reader).getAsJsonObject();
        }

        // Get the puzzle data (assuming "hard" key, but could be configurable)
        JsonObject puzzle = input.getAsJsonObject("hard");

        // Extract dominoes
        JsonArray dominoes = puzzle.getAsJsonArray("dominoes");

        // Extract regions
        JsonArray regions = puzzle.getAsJsonArray("regions");

        // Create a map from cell coordinates to node names
        Map<String, String> cellToNode = new LinkedHashMap<>();
        List<String> allNodes = new ArrayList<>();

        // Collect all unique cells from all regions
        Set<String> allCells = new LinkedHashSet<>();
        for (JsonElement regionEl : regions) {
            JsonObject region = regionEl.getAsJsonObject();
            JsonArray indices = region.getAsJsonArray("indices");
            for (JsonElement indexEl : indices) {
                JsonArray index = indexEl.getAsJsonArray();
                String cellKey = index.get(0).getAsInt() + "," + index.get(1).getAsInt();
                allCells.add(cellKey);
            }
        }

        // Sort cells by row, then by column (row-major order)
        List<String> sortedCells = new ArrayList<>(allCells);
        sortedCells.sort((a, b) -> {
            String[] partsA = a.split(",");
            String[] partsB = b.split(",");
            int rowA = Integer.parseInt(partsA[0]);
            int rowB = Integer.parseInt(partsB[0]);
            if (rowA != rowB) return rowA - rowB;
            int colA = Integer.parseInt(partsA[1]);
            int colB = Integer.parseInt(partsB[1]);
            return colA - colB;
        });

        // Assign node names (A, B, C, ...) in sorted order
        // Also store cell coordinates for each node
        Map<String, int[]> nodeToCell = new LinkedHashMap<>();
        int nodeIndex = 0;
        for (String cell : sortedCells) {
            String nodeName = getNodeName(nodeIndex++);
            cellToNode.put(cell, nodeName);
            allNodes.add(nodeName);
            String[] parts = cell.split(",");
            nodeToCell.put(nodeName, new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
        }

        // Build node details
        JsonObject nodeDetails = new JsonObject();

        // First pass: create all node details with empty peers
        for (String nodeName : allNodes) {
            JsonObject nodeDetail = new JsonObject();
            nodeDetail.add("peers", new JsonArray());
            nodeDetails.add(nodeName, nodeDetail);
        }

        // Second pass: compute peers based on adjacency (up, down, left, right)
        for (String nodeName : allNodes) {
            int[] coords = nodeToCell.get(nodeName);
            int row = coords[0];
            int col = coords[1];

            JsonArray peers = nodeDetails.getAsJsonObject(nodeName).getAsJsonArray("peers");

            // Check all 4 adjacent cells
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // up, down, left, right
            for (int[] dir : directions) {
                String adjacentCell = (row + dir[0]) + "," + (col + dir[1]);
                if (cellToNode.containsKey(adjacentCell)) {
                    String peerName = cellToNode.get(adjacentCell);
                    if (!containsString(peers, peerName)) {
                        peers.add(peerName);
                    }
                }
            }
        }

        // Third pass: set expressions and partners from regions
        for (JsonElement regionEl : regions) {
            JsonObject region = regionEl.getAsJsonObject();
            JsonArray indices = region.getAsJsonArray("indices");
            String type = region.has("type") ? region.get("type").getAsString() : "sum";
            Integer target = region.has("target") ? region.get("target").getAsInt() : null;

            // Get all nodes in this region
            List<String> regionNodes = new ArrayList<>();
            for (JsonElement indexEl : indices) {
                JsonArray index = indexEl.getAsJsonArray();
                String cellKey = index.get(0).getAsInt() + "," + index.get(1).getAsInt();
                regionNodes.add(cellToNode.get(cellKey));
            }

            // Build expression based on type and target
            String expression = buildExpression(regionNodes, type, target);

            // For each node in the region, set expression and partners
            for (int i = 0; i < regionNodes.size(); i++) {
                String nodeName = regionNodes.get(i);
                JsonObject nodeDetail = nodeDetails.getAsJsonObject(nodeName);

                // Set expression
                nodeDetail.addProperty("expression", expression);

                // Set partners in expression (skip for ANY/empty type)
                if (!type.equals("empty")) {
                    JsonArray partners = new JsonArray();
                    for (int j = 0; j < regionNodes.size(); j++) {
                        if (i != j) {
                            partners.add(regionNodes.get(j));
                        }
                    }
                    nodeDetail.add("partners_in_expression", partners);
                }
            }
        }

        // Build output JSON
        JsonObject output = new JsonObject();
        JsonArray puzzlesArray = new JsonArray();
        JsonObject puzzleOutput = new JsonObject();

        puzzleOutput.addProperty("no_of_nodes", allNodes.size());

        JsonArray nodesArray = new JsonArray();
        for (String node : allNodes) {
            nodesArray.add(node);
        }
        puzzleOutput.add("nodes", nodesArray);

        puzzleOutput.add("node_details", nodeDetails);
        puzzleOutput.add("dominoes", dominoes);

        // Add cell mapping for reference (helps understand which node is which cell)
        JsonObject cellMapping = new JsonObject();
        for (Map.Entry<String, String> entry : cellToNode.entrySet()) {
            cellMapping.addProperty(entry.getValue(), entry.getKey());
        }
        puzzleOutput.add("cell_mapping", cellMapping);

        puzzlesArray.add(puzzleOutput);
        output.add("pips_medium_puzzles", puzzlesArray);

        // Write output file with UTF-8 encoding, disable HTML escaping for = sign
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), java.nio.charset.StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
            gson.toJson(output, writer);
        }
    }

    private static String getNodeName(int index) {
        // Generate node names: A, B, C, ... Z, AA, AB, ...
        StringBuilder sb = new StringBuilder();
        int remaining = index;
        do {
            sb.insert(0, (char) ('A' + (remaining % 26)));
            remaining = remaining / 26 - 1;
        } while (remaining >= 0);
        return sb.toString();
    }

    private static String buildExpression(List<String> nodes, String type, Integer target) {
        if (type.equals("empty")) {
            return "ANY";
        }

        if (nodes.size() == 1) {
            // Single node: X=target
            return nodes.get(0) + "=" + target;
        }

        // Multiple nodes: X+Y+Z=target (for sum type)
        if (type.equals("sum")) {
            return String.join("+", nodes) + "=" + target;
        }

        // For other types like "less" or "greater", adjust as needed
        if (type.equals("less") || type.equals("<")) {
            return String.join("+", nodes) + "<" + target;
        }

        if (type.equals("greater") || type.equals(">")) {
            return String.join("+", nodes) + ">" + target;
        }

        // Default to sum
        return String.join("+", nodes) + "=" + target;
    }

    private static boolean containsString(JsonArray array, String value) {
        for (JsonElement el : array) {
            if (el.getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}