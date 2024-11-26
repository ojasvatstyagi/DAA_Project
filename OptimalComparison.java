package DaaProject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OptimalComparison {

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

    static class Graph {
        List<Node> nodes = new ArrayList<>();
        Map<Integer, Set<Integer>> adjacencyList = new HashMap<>();

        void addNode(Node node) {
            nodes.add(node);
            adjacencyList.put(node.id, new HashSet<>());
        }

        void addEdge(int node1Id, int node2Id) {
            adjacencyList.get(node1Id).add(node2Id);
            adjacencyList.get(node2Id).add(node1Id);
        }

        static double distance(Node n1, Node n2) {
            return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2) + Math.pow(n1.z - n2.z, 2));
        }

        void buildGraph(double viewRange) {
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    if (distance(nodes.get(i), nodes.get(j)) <= viewRange) {
                        addEdge(nodes.get(i).id, nodes.get(j).id);
                    }
                }
            }
        }

        static Node latLonToCartesian(int id, double lat, double lon) {
            final double R = 6371; // Earth's radius in km
            double x = R * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lon));
            double y = R * Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lon));
            double z = R * Math.sin(Math.toRadians(lat));
            return new Node(id, x, y, z);
        }

        Set<Integer> findMinimumDominatingSet() {
            Set<Integer> dominatingSet = new HashSet<>();
            Set<Integer> covered = new HashSet<>();
            Set<Integer> allNodes = new HashSet<>(adjacencyList.keySet());

            while (!covered.containsAll(allNodes)) {
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

                dominatingSet.add(bestNode);
                covered.add(bestNode);
                covered.addAll(adjacencyList.get(bestNode));
            }

            return dominatingSet;
        }

        // Placeholder methods for finding optimal camera positions
        Node findOptimalCameraPositionNaive(double viewRange) {
            return findOptimalCameraPosition(viewRange); // Implement naive logic
        }

        Node findOptimalCameraPositionKdTree(double viewRange) {
            return findOptimalCameraPosition(viewRange); // Implement KD-Tree logic
        }

        private Node findOptimalCameraPosition(double viewRange) {
            if (nodes.isEmpty()) return null;

            Node optimalNode = null;
            int maxCoveredNodes = 0;

            for (Node candidateNode : nodes) {
                int coveredNodesCount = (int) nodes.stream()
                        .filter(targetNode -> distance(candidateNode, targetNode) <= viewRange)
                        .count();

                if (coveredNodesCount > maxCoveredNodes) {
                    maxCoveredNodes = coveredNodesCount;
                    optimalNode = candidateNode;
                }
            }

            return optimalNode;
        }
    }

    public static void main(String[] args) {
        System.out.println("Programming Started");

        try (FileWriter csvWriter = new FileWriter("results.csv")) {
            csvWriter.append("Trial,Node Size,Algorithm,Runs Time (ms)\n");

            Random random = new Random();
            final int TRIALS = 100;

            for (int trial = 0; trial < TRIALS; trial++) {
                Graph graph = createRandomGraph(random);
                double viewRange = 8.0; // Example view range
                graph.buildGraph(viewRange);

                runAlgorithmsAndRecordTime(csvWriter, trial + 1, graph, viewRange);
            }

            System.out.println("Runtime results saved to results.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Graph createRandomGraph(Random random) {
        Graph graph = new Graph();
        int size = random.nextInt(6) + 5; // Generates a number between 5 and 10

        for (int i = 0; i < size; i++) {
            double lat = random.nextDouble() * 180 - 90; // Latitude between -90 and +90
            double lon = random.nextDouble() * 360 - 180; // Longitude between -180 and +180
            graph.addNode(Graph.latLonToCartesian(i + 1, lat, lon));
        }

        return graph;
    }

    private static void runAlgorithmsAndRecordTime(FileWriter csvWriter, int trialNum,
                                                   Graph graph, double viewRange) throws IOException {

        String[] algorithms = {"Minimum Dominating Set", "Naive Camera Position", "KD-Tree Camera Position"};

        for (String algorithm : algorithms) {
            long startTime = System.nanoTime();

            switch (algorithm) {
                case "Minimum Dominating Set":
                    graph.findMinimumDominatingSet();
                    break;
                case "Naive Camera Position":
                    graph.findOptimalCameraPositionNaive(viewRange);
                    break;
                case "KD-Tree Camera Position":
                    graph.findOptimalCameraPositionKdTree(viewRange);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
            }

            long endTime = System.nanoTime();

            // Calculate runtime in milliseconds
            double runtimeMs = (endTime - startTime) / 1_000_000.0;

            csvWriter.append(String.format("%d,%d,%s,%.2f\n", trialNum,
                    graph.nodes.size(), algorithm, runtimeMs));

            System.out.printf("Trial %d | Node Size: %d | Algorithm: %s | Runtime: %.2f ms%n",
                    trialNum, graph.nodes.size(), algorithm, runtimeMs);
        }
    }
}