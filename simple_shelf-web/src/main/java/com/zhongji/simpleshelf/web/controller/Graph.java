package com.zhongji.simpleshelf.web.controller;

import java.util.*;


public class Graph {
    private Map<String, List<String>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // 添加顶点
    public void addVertex(String vertex) {
        if (adjacencyList.containsKey(vertex)) {
            return;
        }
        adjacencyList.put(vertex, new ArrayList<>());
    }

    // 添加边
    public void addEdge(String source, String destination) {
        adjacencyList.get(source).add(destination);
        // 如果是无向图，同时添加反向边
        adjacencyList.get(destination).add(source);
    }


    public void addShortestEdge(String source, String destination) {
        //source是数字
        if (adjacencyList.get(source).contains(destination)) {
            adjacencyList.get(source).add(destination);
        }
        adjacencyList.get(source).add(destination);
        // 如果是无向图，同时添加反向边
        adjacencyList.get(destination).add(source);
    }

    // 打印图
    public void printGraph() {
        for (String vertex : adjacencyList.keySet()) {
            System.out.print("Vertex " + vertex + " is connected to: ");
            for (String neighbor : adjacencyList.get(vertex)) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Graph graph = new Graph();

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        graph.addEdge("3", "A");
        graph.addEdge("1", "B");
        graph.addEdge("1", "C");
        graph.addEdge("1", "E");
        graph.addEdge("2", "D");

        graph.printGraph();
    }
}


