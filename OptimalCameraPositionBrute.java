package DaaProject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OptimalCameraPositionBrute {

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

        void addNode(Node node) {
            nodes.add(node);
        }

        // Brute force optimized: Find the optimal camera position by evaluating only node positions
        Node findOptimalCameraPosition(double viewRange) {
            if (nodes.isEmpty()) return null;

            Node optimalNode = null;
            int maxCoveredNodes = 0;

            // Loop through each node and check how many nodes it can cover
            for (Node candidateNode : nodes) {
                int coveredNodesCount = 0;

                // Check the distance from this candidate node to every other node
                for (Node targetNode : nodes) {
                    if (distance(candidateNode, targetNode) <= viewRange) {
                        coveredNodesCount++;
                    }
                }

                // Update the optimal node based on the maximum coverage
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
        System.out.println("Optimized Brute Force Camera Placement Started");

        try (FileWriter csvWriter = new FileWriter("resultsNaive.csv")) {
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

            System.out.println("Brute Force runtime results saved to resultsNaive.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
