package pips_dlx;

import java.util.ArrayList;
import java.util.List;

/**
 * Dancing Links (DLX) implementation for solving exact cover problems.
 * Based on Donald Knuth's Algorithm X with dancing links optimization.
 */
public class DLX {

    private final ColumnNode header;
    private final List<DancingNode> solution = new ArrayList<>();
    private final List<List<Integer>> allSolutions = new ArrayList<>();
    private final int[][] originalMatrix;

    public DLX(int[][] matrix) {
        this.originalMatrix = matrix;
        this.header = buildLinks(matrix);
    }

    private ColumnNode buildLinks(int[][] matrix) {
        int numCols = matrix[0].length;

        ColumnNode headerNode = new ColumnNode(-1);
        List<ColumnNode> columnNodes = new ArrayList<>();

        // Create column headers
        for (int i = 0; i < numCols; i++) {
            ColumnNode col = new ColumnNode(i);
            columnNodes.add(col);
            headerNode.hookRight(col);
        }

        // Create nodes for each 1 in the matrix
        for (int row = 0; row < matrix.length; row++) {
            DancingNode prev = null;
            for (int col = 0; col < numCols; col++) {
                if (matrix[row][col] == 1) {
                    ColumnNode colHeader = columnNodes.get(col);
                    DancingNode newNode = new DancingNode(colHeader, row);
                    colHeader.up.hookDown(newNode);
                    colHeader.size++;

                    if (prev == null) {
                        prev = newNode;
                    } else {
                        prev.hookRight(newNode);
                    }
                }
            }
        }

        return headerNode;
    }

    public List<List<Integer>> solve() {
        search(0);
        return allSolutions;
    }

    public List<Integer> solveFirst() {
        if (searchFirst(0)) {
            List<Integer> result = new ArrayList<>();
            for (DancingNode node : solution) {
                result.add(node.rowIndex);
            }
            return result;
        }
        return null;
    }

    private void search(int depth) {
        if (header.right == header) {
            // Found a solution
            List<Integer> sol = new ArrayList<>();
            for (DancingNode node : solution) {
                sol.add(node.rowIndex);
            }
            allSolutions.add(sol);
            return;
        }

        ColumnNode col = selectColumn();
        cover(col);

        for (DancingNode row = col.down; row != col; row = row.down) {
            solution.add(row);

            for (DancingNode node = row.right; node != row; node = node.right) {
                cover(node.column);
            }

            search(depth + 1);

            row = solution.remove(solution.size() - 1);
            col = row.column;

            for (DancingNode node = row.left; node != row; node = node.left) {
                uncover(node.column);
            }
        }

        uncover(col);
    }

    private boolean searchFirst(int depth) {
        if (header.right == header) {
            return true;
        }

        ColumnNode col = selectColumn();
        cover(col);

        for (DancingNode row = col.down; row != col; row = row.down) {
            solution.add(row);

            for (DancingNode node = row.right; node != row; node = node.right) {
                cover(node.column);
            }

            if (searchFirst(depth + 1)) {
                return true;
            }

            row = solution.remove(solution.size() - 1);
            col = row.column;

            for (DancingNode node = row.left; node != row; node = node.left) {
                uncover(node.column);
            }
        }

        uncover(col);
        return false;
    }

    // MRV heuristic: choose column with minimum remaining values
    private ColumnNode selectColumn() {
        ColumnNode minCol = null;
        int minSize = Integer.MAX_VALUE;

        for (ColumnNode col = (ColumnNode) header.right; col != header; col = (ColumnNode) col.right) {
            if (col.size < minSize) {
                minSize = col.size;
                minCol = col;
            }
        }
        return minCol;
    }

    private void cover(ColumnNode col) {
        col.right.left = col.left;
        col.left.right = col.right;

        for (DancingNode row = col.down; row != col; row = row.down) {
            for (DancingNode node = row.right; node != row; node = node.right) {
                node.down.up = node.up;
                node.up.down = node.down;
                node.column.size--;
            }
        }
    }

    private void uncover(ColumnNode col) {
        for (DancingNode row = col.up; row != col; row = row.up) {
            for (DancingNode node = row.left; node != row; node = node.left) {
                node.column.size++;
                node.down.up = node;
                node.up.down = node;
            }
        }

        col.right.left = col;
        col.left.right = col;
    }

    public int[][] getOriginalMatrix() {
        return originalMatrix;
    }

    // Inner classes for dancing links structure
    private static class DancingNode {
        DancingNode left, right, up, down;
        ColumnNode column;
        int rowIndex;

        DancingNode() {
            left = right = up = down = this;
        }

        DancingNode(ColumnNode col, int rowIndex) {
            this();
            this.column = col;
            this.rowIndex = rowIndex;
        }

        void hookDown(DancingNode node) {
            node.down = this.down;
            node.down.up = node;
            node.up = this;
            this.down = node;
        }

        void hookRight(DancingNode node) {
            node.right = this.right;
            node.right.left = node;
            node.left = this;
            this.right = node;
        }
    }

    private static class ColumnNode extends DancingNode {
        int size = 0;
        int index;

        ColumnNode(int index) {
            super();
            this.index = index;
            this.column = this;
        }
    }
}