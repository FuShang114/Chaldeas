package com.example.demo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用树生成器
 * 用于生成调用树和拓扑图数据
 */
public class CallTreeGenerator {

    /**
     * 生成调用树
     * @param methodCalls 方法调用记录
     * @return 调用树结构
     */
    public static CallTreeNode generateCallTree(Map<String, JavaAgentInterceptor.MethodCall> methodCalls) {
        if (methodCalls == null || methodCalls.isEmpty()) {
            return null;
        }
        
        // 构建调用树节点映射
        Map<String, CallTreeNode> nodeMap = new HashMap<>();
        
        // 首先创建所有节点
        for (Map.Entry<String, JavaAgentInterceptor.MethodCall> entry : methodCalls.entrySet()) {
            JavaAgentInterceptor.MethodCall methodCall = entry.getValue();
            String key = methodCall.getClassName() + "." + methodCall.getMethodName();
            
            CallTreeNode node = new CallTreeNode();
            node.setName(key);
            node.setClassName(methodCall.getClassName());
            node.setMethodName(methodCall.getMethodName());
            node.setInput(methodCall.getInput());
            node.setOutput(methodCall.getOutput());
            node.setTimestamp(methodCall.getTimestamp());
            
            nodeMap.put(key, node);
        }
        
        // 构建树结构（简化实现，实际项目中需要分析调用关系）
        // 这里假设第一个方法是根节点
        CallTreeNode root = null;
        for (CallTreeNode node : nodeMap.values()) {
            if (root == null) {
                root = node;
            } else {
                // 简化实现，将所有节点作为根节点的子节点
                root.addChild(node);
            }
        }
        
        return root;
    }

    /**
     * 生成拓扑图数据
     * @param methodCalls 方法调用记录
     * @return 拓扑图数据
     */
    public static TopologyData generateTopologyData(Map<String, JavaAgentInterceptor.MethodCall> methodCalls) {
        TopologyData data = new TopologyData();
        
        if (methodCalls == null || methodCalls.isEmpty()) {
            return data;
        }
        
        // 构建节点和边
        Map<String, TopologyNode> nodeMap = new HashMap<>();
        
        // 首先创建所有节点
        for (Map.Entry<String, JavaAgentInterceptor.MethodCall> entry : methodCalls.entrySet()) {
            JavaAgentInterceptor.MethodCall methodCall = entry.getValue();
            String className = methodCall.getClassName();
            
            // 只添加类节点，不添加方法节点
            if (!nodeMap.containsKey(className)) {
                TopologyNode node = new TopologyNode();
                node.setName(className);
                node.setSymbolSize(calculateSymbolSize(className));
                data.addNode(node);
                nodeMap.put(className, node);
            }
        }
        
        // 构建边（简化实现，实际项目中需要分析调用关系）
        // 这里假设方法调用顺序就是依赖关系
        List<TopologyNode> nodes = data.getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            TopologyEdge edge = new TopologyEdge();
            edge.setSource(nodes.get(i).getName());
            edge.setTarget(nodes.get(i + 1).getName());
            data.addEdge(edge);
        }
        
        return data;
    }

    /**
     * 计算节点大小
     * @param className 类名
     * @return 节点大小
     */
    private static int calculateSymbolSize(String className) {
        // 根据类名长度或包路径深度计算节点大小
        int depth = className.split("\\.").length;
        return 40 + (depth * 5);
    }

    /**
     * 调用树节点
     */
    public static class CallTreeNode {
        private String name;
        private String className;
        private String methodName;
        private Object[] input;
        private Object output;
        private long timestamp;
        private List<CallTreeNode> children = new ArrayList<>();

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public Object[] getInput() {
            return input;
        }

        public void setInput(Object[] input) {
            this.input = input;
        }

        public Object getOutput() {
            return output;
        }

        public void setOutput(Object output) {
            this.output = output;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public List<CallTreeNode> getChildren() {
            return children;
        }

        public void addChild(CallTreeNode child) {
            this.children.add(child);
        }
    }

    /**
     * 拓扑图数据
     */
    public static class TopologyData {
        private List<TopologyNode> nodes = new ArrayList<>();
        private List<TopologyEdge> edges = new ArrayList<>();

        // Getters and Setters
        public List<TopologyNode> getNodes() {
            return nodes;
        }

        public void addNode(TopologyNode node) {
            this.nodes.add(node);
        }

        public List<TopologyEdge> getEdges() {
            return edges;
        }

        public void addEdge(TopologyEdge edge) {
            this.edges.add(edge);
        }
    }

    /**
     * 拓扑图节点
     */
    public static class TopologyNode {
        private String name;
        private int symbolSize;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSymbolSize() {
            return symbolSize;
        }

        public void setSymbolSize(int symbolSize) {
            this.symbolSize = symbolSize;
        }
    }

    /**
     * 拓扑图边
     */
    public static class TopologyEdge {
        private String source;
        private String target;

        // Getters and Setters
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}
