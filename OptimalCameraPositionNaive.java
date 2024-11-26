package DaaProject;

import java.util.*;

public class OptimalCameraPositionNaive {

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

        void addNode(Node node) {
            nodes.add(node);
        }

        // Method to calculate the Euclidean distance between two nodes
        static double distance(Node n1, Node n2) {
            return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2) + Math.pow(n1.z - n2.z, 2));
        }

        // Method to count the number of nodes within view range of a given position
        // and return their IDs
        static List<Integer> countNodesInViewRange(double x, double y, double z, List<Node> nodes, double viewRange) {
            List<Integer> nodeIdsInView = new ArrayList<>();
            for (Node node : nodes) {
                if (distance(new Node(-1, x, y, z), node) <= viewRange) {
                    nodeIdsInView.add(node.id);
                }
            }
            return nodeIdsInView;
        }

        // Method to find the optimal position for the camera using a grid traversal
        Node findOptimalCameraPosition(double viewRange, double gridStep) {
            if (nodes.isEmpty()) return null;

            // Determine the boundaries of the grid based on the farthest nodes
            double minX = nodes.stream().mapToDouble(node -> node.x).min().orElse(0);
            double minY = nodes.stream().mapToDouble(node -> node.y).min().orElse(0);
            double minZ = nodes.stream().mapToDouble(node -> node.z).min().orElse(0);
            double maxX = nodes.stream().mapToDouble(node -> node.x).max().orElse(0);
            double maxY = nodes.stream().mapToDouble(node -> node.y).max().orElse(0);
            double maxZ = nodes.stream().mapToDouble(node -> node.z).max().orElse(0);

            Node optimalNode = new Node(-1, (minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
            int maxCoveredNodes = 0;

            // Traverse each point on the grid
            for (double x = minX; x <= maxX; x += gridStep) {
                for (double y = minY; y <= maxY; y += gridStep) {
                    for (double z = minZ; z <= maxZ; z += gridStep) {
                        List<Integer> visibleNodeIds = countNodesInViewRange(x, y, z, nodes, viewRange);
                        int coveredNodesCount = visibleNodeIds.size();
                        if (coveredNodesCount > maxCoveredNodes ||
                                (coveredNodesCount == maxCoveredNodes && distance(optimalNode, new Node(-1, x, y, z)) <
                                        distance(optimalNode, new Node(-1,
                                                (minX + maxX) / 2,
                                                (minY + maxY) / 2,
                                                (minZ + maxZ) / 2)))) {
                            maxCoveredNodes = coveredNodesCount;
                            optimalNode = new Node(-1, x, y, z);
                        }
                    }
                }
            }

            return optimalNode;
        }

        // Method to convert latitude and longitude to Cartesian coordinates
        static Node latLonToCartesian(int id, double lat, double lon) {
            final double R = 6371; // Earth's radius in km
            double x = R * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lon));
            double y = R * Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lon));
            double z = R * Math.sin(Math.toRadians(lat));
            return new Node(id, x, y, z);
        }

        // Method to convert Cartesian coordinates to latitude and longitude
        static double[] cartesianToLatLon(double x, double y, double z) {
            final double R = 6371; // Earth's radius in km
            double lat = Math.toDegrees(Math.asin(z / R));
            double lon = Math.toDegrees(Math.atan2(y, x));
            return new double[]{lat, lon};
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Graph graph = new Graph();

        // Adding nodes to the graph (latitude and longitude)
        System.out.println("Enter the number of nodes:");
        int numNodes = scanner.nextInt();

        for (int i = 0; i < numNodes; i++) {
            System.out.println("Enter latitude and longitude for node " + (i + 1) + ":");

            while (!scanner.hasNextDouble()) {
                System.out.println("Invalid input. Please enter a valid latitude:");
                scanner.next();
            }

            double lat = scanner.nextDouble();

            while (!scanner.hasNextDouble()) {
                System.out.println("Invalid input. Please enter a valid longitude:");
                scanner.next();
            }

            double lon = scanner.nextDouble();

            graph.addNode(Graph.latLonToCartesian(i + 1, lat, lon));
        }

        System.out.println("Finding optimal camera position...");
        double viewRange = 8.0; // Example view range
        double gridStep = 0.5;  // Example grid step size
        Node optimalNode = graph.findOptimalCameraPosition(viewRange, gridStep);

        if (optimalNode != null) {
            List<Integer> visibleNodeIds = Graph.countNodesInViewRange(optimalNode.x, optimalNode.y,
                    optimalNode.z,
                    graph.nodes,
                    viewRange);

            System.out.printf("Optimal camera position is at coordinates: (%.6f°, %.6f°)%n",
                    Graph.cartesianToLatLon(optimalNode.x, optimalNode.y,
                            optimalNode.z)[0],
                    Graph.cartesianToLatLon(optimalNode.x,
                            optimalNode.y,
                            optimalNode.z)[1]);

            System.out.printf("Number of nodes in view range: %d%n", visibleNodeIds.size());

            if (!visibleNodeIds.isEmpty()) {
                System.out.print("IDs of nodes in view range: ");
                System.out.println(visibleNodeIds);
            } else {
                System.out.println("No nodes are within view range.");
            }

        } else {
            System.out.println("No optimal position found.");
        }

        scanner.close();
    }
}