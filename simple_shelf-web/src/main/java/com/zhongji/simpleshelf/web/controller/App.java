package com.zhongji.simpleshelf.web.controller;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class App {
    public static void main(String[] args) {
        TreeNode a = new TreeNode("A");
        TreeNode b = new TreeNode("B");
        TreeNode c = new TreeNode("C");
        TreeNode d = new TreeNode("D");
        TreeNode e = new TreeNode("E");
        TreeNode f = new TreeNode("F");
        TreeNode g = new TreeNode("G");
        List<TreeNode> subs = new ArrayList<>();
        subs.add(b);
        subs.add(c);
        subs.add(d);
        List<TreeNode> subs2 = new ArrayList<>();
        subs2.add(e);
        List<TreeNode> subs3 = new ArrayList<>();
        subs3.add(f);
        List<TreeNode> subs4 = new ArrayList<>();
        subs4.add(g);
        a.setSubs(subs);
        c.setSubs(subs2);
        d.setSubs(subs3);
        e.setSubs(subs4);
        Map<Integer, List<TreeNode>> data = new App().bestTaskChoose(a);
        data.forEach((k, v) -> {
            System.out.println("第" + k + "层:");
            for (TreeNode treeNode : v) {
                System.out.println(treeNode.val);
            }
        });

    }

    private int maxSize = 0;

    private Map<Integer, List<TreeNode>> bestTaskChoose(TreeNode root) {
        Map<Integer, List<TreeNode>> data = new HashMap<>();
        newLevelGet(root, new LinkedList<>(), 0, data);
        data.put(maxSize, new ArrayList<>());
        data.get(maxSize).add(root);
        return data;
    }


    /**
     * @param root  节点
     * @param nodes 路径节点详情列表
     * @param level 某条路径当前的层数
     * @param data  路径
     * @return 每条全路径的长度
     */

    private int newLevelGet(TreeNode root, LinkedList<TreeNode> nodes, int level,
                            Map<Integer, List<TreeNode>> data) {
        if (CollectionUtils.isEmpty(root.getSubs())) {
            maxSize = Math.max(maxSize, nodes.size());
            return nodes.size();
        }
        if (!nodes.contains(root)) {
            nodes.addFirst(root);
        }
        int tempSize = 0;
        for (TreeNode sub : root.getSubs()) {
            //之前的都+1
            nodes.addFirst(sub);
            level++;
            tempSize = newLevelGet(sub, nodes, level, data);
            if (data.get(tempSize - level) == null) {
                data.put(tempSize - level, new ArrayList<>());
            }
            data.get(tempSize - level).add(sub);
            nodes.remove(sub);
            level--;
        }
        return tempSize;
    }


    static class TreeNode {
        private String val;
        private List<TreeNode> subs;

        public TreeNode(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public List<TreeNode> getSubs() {
            return subs;
        }

        public void setSubs(List<TreeNode> subs) {
            this.subs = subs;
        }
    }


}
