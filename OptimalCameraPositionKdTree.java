package DaaProject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OptimalCameraPositionKdTree {

    static class Node {
        int id;
        double x, y, z;

        Node(int id, double x, double y, double z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    static class KDTree {
        Node node;
        KDTree left, right;
        int depth;

        public KDTree(List<Node> nodes, int depth) {
            if (nodes.isEmpty()) return;

            this.depth = depth;
            int axis = depth % 3;

            // Sort nodes based on the current axis
            nodes.sort(Comparator.comparingDouble(n -> axis == 0 ? n.x : axis == 1 ? n.y : n.z));
            int medianIndex = nodes.size() / 2;
            node = nodes.get(medianIndex);

            // Split nodes into left and right sublists
            List<Node> leftNodes = nodes.subList(0, medianIndex);
            List<Node> rightNodes = nodes.subList(medianIndex + 1, nodes.size());

            if (!leftNodes.isEmpty()) this.left = new KDTree(leftNodes, depth + 1);
            if (!rightNodes.isEmpty()) this.right = new KDTree(rightNodes, depth + 1);
        }

        // Method to count nodes within a certain view range
        public List<Integer> rangeQuery(Node target, double viewRange) {
            List<Integer> result = new ArrayList<>();
            rangeQueryHelper(target, viewRange, result);
            return result;
        }

        private void rangeQueryHelper(Node target, double viewRange, List<Integer> result) {
            if (node == null) return;

            double distance = Graph.distance(node, target);
            if (distance <= viewRange) {
                result.add(node.id);
            }

            int axis = depth % 3;
            double delta = axis == 0 ? target.x - node.x : axis == 1 ? target.y - node.y : target.z - node.z;
            double deltaSquared = delta * delta;

            if (delta < 0) {
                if (left != null) left.rangeQueryHelper(target, viewRange, result);
                if (right != null && deltaSquared <= viewRange * viewRange) right.rangeQueryHelper(target, viewRange, result);
            } else {
                if (right != null) right.rangeQueryHelper(target, viewRange, result);
                if (left != null && deltaSquared <= viewRange * viewRange) left.rangeQueryHelper(target, viewRange, result);
            }
        }
    }

    static class Graph {
        List<Node> nodes = new ArrayList<>();
        KDTree kdTree;

        void addNode(Node node) {
            nodes.add(node);
        }

        // Build k-d tree for the nodes
        void buildKDTree() {
            kdTree = new KDTree(nodes, 0);
        }

        // Optimized: Find the optimal camera position using the node positions only
        Node findOptimalCameraPosition(double viewRange) {
            if (nodes.isEmpty()) return null;

            Node optimalNode = null;
            int maxCoveredNodes = 0;

            // Check each node as a candidate for camera placement
            for (Node candidateNode : nodes) {
                List<Integer> visibleNodeIds = kdTree.rangeQuery(candidateNode, viewRange);
                int coveredNodesCount = visibleNodeIds.size();

                // Update the optimal node based on coverage
                if (coveredNodesCount > maxCoveredNodes) {
                    maxCoveredNodes = coveredNodesCount;
                    optimalNode = candidateNode;
                }
            }

            return optimalNode;
        }

        // Calculate Euclidean distance between two nodes
        static double distance(Node n1, Node n2) {
            return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2) + Math.pow(n1.z - n2.z, 2));
        }

        // Latitude/Longitude to Cartesian conversion
        static Node latLonToCartesian(int id, double lat, double lon) {
            final double R = 6371; // Earth's radius in km
            double x = R * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lon));
            double y = R * Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lon));
            double z = R * Math.sin(Math.toRadians(lat));
            return new Node(id, x, y, z);
        }

        static double[] cartesianToLatLon(double x, double y, double z) {
            final double R = 6371; // Earth's radius in km
            double lat = Math.toDegrees(Math.asin(z / R));
            double lon = Math.toDegrees(Math.atan2(y, x));
            return new double[]{lat, lon};
        }
    }

    public static void main(String[] args) {
        System.out.println("Optimized KD-Tree Camera Placement Started");

        try (FileWriter csvWriter = new FileWriter("resultsKdTree.csv")) {
            csvWriter.append("Node Size,Average Runtime (ms)\n");

            Random random = new Random();

            // Run the algorithm 100 times for different node sizes
            for (int trial = 0; trial < 100; trial++) {
                List<Double> runtimes = new ArrayList<>();

                int size = random.nextInt(6) + 5; // Node size between 5 and 10
                Graph graph = new Graph();

                // Add random nodes with lat/lon coordinates
                for (int i = 0; i < size; i++) {
                    double lat = random.nextDouble() * 180 - 90; // Latitude between -90 and +90
                    double lon = random.nextDouble() * 360 - 180; // Longitude between -180 and +180
                    graph.addNode(Graph.latLonToCartesian(i + 1, lat, lon));
                }

                graph.buildKDTree();

                long startTime = System.nanoTime();
                Node optimalNode = graph.findOptimalCameraPosition(10.0); // Example view range
                long endTime = System.nanoTime();

                double runtime = (endTime - startTime) / 1_000_000.0; // Runtime in milliseconds
                runtimes.add(runtime);

                // Calculate average runtime
                double averageRuntime = runtimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                csvWriter.append(String.valueOf(size)).append(",").append(String.valueOf(averageRuntime)).append("\n");
                System.out.printf("Trial %d | Node Size: %d | Runtime: %.2f ms%n", trial + 1, size, runtime);
            }

            System.out.println("Optimized runtime results saved to resultsKdTree.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
