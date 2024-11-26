package DaaProject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OptimalCameraPositionMDS {

    static class Node {
        int id;
        double x, y, z; // Coordinates of the node

        Node(int id, double x, double y, double z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    static class Graph {
        List<Node> nodes = new ArrayList<>();
        Map<Integer, Set<Integer>> adjacencyList = new HashMap<>();

        void addNode(Node node) {
            nodes.add(node);
            adjacencyList.put(node.id, new HashSet<>());
        }

        // Method to add an undirected edge between two nodes
        void addEdge(int node1Id, int node2Id) {
            adjacencyList.get(node1Id).add(node2Id);
            adjacencyList.get(node2Id).add(node1Id);
        }

        // Calculate the Euclidean distance between two nodes
        static double distance(Node n1, Node n2) {
            return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2) + Math.pow(n1.z - n2.z, 2));
        }

        // Build a graph based on view range (create edges between nodes within view range)
        void buildGraph(double viewRange) {
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    if (distance(nodes.get(i), nodes.get(j)) <= viewRange) {
                        addEdge(nodes.get(i).id, nodes.get(j).id);
                    }
                }
            }
        }

        // Greedy Approximation Algorithm for Minimum Dominating Set
        Set<Integer> findMinimumDominatingSet() {
            Set<Integer> dominatingSet = new HashSet<>();
            Set<Integer> covered = new HashSet<>();
            Set<Integer> allNodes = new HashSet<>(adjacencyList.keySet());

            while (!covered.containsAll(allNodes)) {
                // Find the node with the highest number of uncovered neighbors
                int bestNode = -1;
                int maxUncoveredNeighbors = -1;

                for (int node : allNodes) {
                    if (dominatingSet.contains(node)) continue;

                    Set<Integer> uncoveredNeighbors = new HashSet<>(adjacencyList.get(node));
                    uncoveredNeighbors.removeAll(covered);

                    if (uncoveredNeighbors.size() > maxUncoveredNeighbors) {
                        maxUncoveredNeighbors = uncoveredNeighbors.size();
                        bestNode = node;
                    }
                }

                // Add the selected node to the dominating set
                dominatingSet.add(bestNode);

                // Mark the selected node and its neighbors as covered
                covered.add(bestNode);
                covered.addAll(adjacencyList.get(bestNode));
            }

            return dominatingSet;
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
            final double R = 6371;
            double lat = Math.toDegrees(Math.asin(z / R));
            double lon = Math.toDegrees(Math.atan2(y, x));
            return new double[]{lat, lon};
        }
    }

    public static void main(String[] args) {
        System.out.println("Programming Started");

        try (FileWriter csvWriter = new FileWriter("resultsMDS.csv")) {
            csvWriter.append("Node Size,Average Runtime (ms)\n");

            Random random = new Random();

            // Run the algorithm 100 times for each node size from 5 to 10
            for (int trial = 0; trial < 100; trial++) {
                Graph graph = new Graph();
                int size = random.nextInt(6) + 5; // Generates a number between 5 and 10
                // Adding nodes to the graph with random latitude and longitude
                for (int i = 0; i < size; i++) {
                    double lat = random.nextDouble() * 180 - 90; // Latitude between -90 and +90
                    double lon = random.nextDouble() * 360 - 180; // Longitude between -180 and +180
                    graph.addNode(Graph.latLonToCartesian(i + 1, lat, lon));
                }

                System.out.println("Building the graph based on view range...");
                double viewRange = 8.0; // Example view range
                graph.buildGraph(viewRange);

                long startTime = System.nanoTime();
                graph.findMinimumDominatingSet(); // Finding optimal camera positions
                long endTime = System.nanoTime();

                // Calculate runtime in milliseconds
                double runtime = (endTime - startTime) / 1_000_000.0;

                System.out.printf("Trial %d | Node Size: %d | Runtime: %.2f ms%n", trial + 1, size, runtime);

                // Write runtime to CSV file
                csvWriter.append(String.valueOf(size)).append(",").append(String.valueOf(runtime)).append("\n");

            }

            System.out.println("Runtime results saved to resultsMDS.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}