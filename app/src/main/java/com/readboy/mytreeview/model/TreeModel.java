package com.readboy.mytreeview.model;


import android.text.TextUtils;
import android.util.Log;

import com.readboy.mytreeview.bean.Link;
import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.interfaces.ForTreeItem;
import com.readboy.mytreeview.utils.AtlasUtil;
import com.readboy.mytreeview.utils.log.LogUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TreeModel {
    private List<Node> rootNode;
    private List<Link> linkList;
    private LinkedHashMap<Long,Node> nodeMap;
    private transient ForTreeItem mForTreeItem;


    public TreeModel(List<Node> rootNode, List<Link> linkList) {
        this.rootNode = rootNode;
        this.linkList = linkList;
    }

    public TreeModel() {
    }

    public List<Node> getRootNode() {
        return rootNode;
    }

    public void setRootNode(List<Node> rootNode) {
        this.rootNode = rootNode;
    }

    public List<Link> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<Link> linkList) {
        this.linkList = linkList;
    }


    public LinkedHashMap<Long, Node> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(LinkedHashMap<Long, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public void addForTreeItem(ForTreeItem forTreeItem) {
        this.mForTreeItem = forTreeItem;
    }

    public void ergodicTreeInWith(int msg,Node node,int index) {
        Deque<Node> queue = new ArrayDeque<>();
        Node curNode = node;
        queue.add(node);
        while (!queue.isEmpty()) {
            curNode =  queue.poll();
            if (mForTreeItem != null) {
                mForTreeItem.next(msg, curNode,index);
            }
            LinkedList<Node> childNodes = (LinkedList<Node>) AtlasUtil.getSubNodeAccordId(linkList,curNode.getId(),nodeMap);;
            if (childNodes.size() > 0) {
                queue.addAll(childNodes);
            }
        }
    }

    public ArrayList<Node> getAllLowNodes(Node addNode) {
        ArrayList<Node> array = new ArrayList<>();
        Node parentNode = AtlasUtil.getParentNodeAccordId(linkList,addNode.getId(),nodeMap);
        while (parentNode != null && !TextUtils.isEmpty(parentNode.getName())) {
            Node lowNode = getLowNode(parentNode);
            while (lowNode != null) {
                array.add(lowNode);
                lowNode = getLowNode(lowNode);
            }
            parentNode = AtlasUtil.getParentNodeAccordId(linkList,parentNode.getId(),nodeMap);
        }
        return array;
    }

    public ArrayList<Node> getAllPreNodes(Node addNode) {
        ArrayList<Node> array = new ArrayList<>();
        Node parentNode = AtlasUtil.getParentNodeAccordId(linkList,addNode.getId(),nodeMap);
        while (parentNode != null && !TextUtils.isEmpty(parentNode.getName())) {
            Node lowNode = getPreNode(parentNode);
            while (lowNode != null) {
                array.add(lowNode);
                LogUtils.d("lowNode = 11111 " + lowNode.getName() + ", addNode = " + addNode.getName());

                lowNode = getPreNode(lowNode);
            }
            parentNode = AtlasUtil.getParentNodeAccordId(linkList,parentNode.getId(),nodeMap);
        }
        return array;
    }

    private Node getPreNode(Node midPreNode) {

        Node parentNode = AtlasUtil.getParentNodeAccordId(linkList,midPreNode.getId(),nodeMap);
        Node find = null;

        if (parentNode != null && AtlasUtil.getSubNodeAccordId(linkList,parentNode.getId(),nodeMap).size() > 0) {

            Deque<Node> queue = new ArrayDeque<>();
            Node rootNode = parentNode;
            queue.add(rootNode);

            while (!queue.isEmpty()) {
                rootNode = (Node) queue.poll();
                //到了该元素
                if (rootNode == midPreNode) {
                    //返回之前的值
                    break;
                }

                find = rootNode;
                LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(linkList,rootNode.getId(),nodeMap);
                if (childNodes.size() > 0) {
                    for (Node item : childNodes) {
                        queue.add(item);
                    }
                }
            }

            if (find != null && (find.getFloor()%2) != (midPreNode.getFloor()%2)) {
                find = null;
            }
        }
        return find;
    }


    /**
     * 同一个父节点的上下
     * 当新增一个节点时，该节点上面的元素及view都不发生变化，仅需要修改该节点下面的坐标
     * @param midPreNode
     * @return
     */
    private Node getLowNode(Node midPreNode) {
        Node find = null;
        Node parentNode = AtlasUtil.getParentNodeAccordId(linkList,midPreNode.getId(),nodeMap);

        if (parentNode != null && !TextUtils.isEmpty(parentNode.getName()) && AtlasUtil.getSubNodeAccordId(linkList,parentNode.getId(),nodeMap).size() >= 2) {
            Log.d("LZYYYY","midPreNode = " + midPreNode.toString() + ", parent = " + parentNode.toString());
            Deque<Node> queue = new ArrayDeque<>();
            Node rootNode = parentNode;
            queue.add(rootNode);
            boolean up = false;
            while (!queue.isEmpty()) {
                rootNode = (Node) queue.poll();
                if (up) {
                    if (rootNode.getFloor() == midPreNode.getFloor()) {
                        find = rootNode;
                    }
                    break;
                }

                //到了该元素
                if (rootNode == midPreNode) up = true;
                LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(linkList,rootNode.getId(),nodeMap);
                if (childNodes.size() > 0) {
                    for (Node item : childNodes) {
                        queue.add(item);
                    }
                }
            }
        }
        return find;
    }



    public void ergodicTreeInDeep(int msg,Node rootNode,int index) {
        Stack<Node> stack = new Stack<>();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            Node pop = stack.pop();
            if (mForTreeItem != null) {
                mForTreeItem.next(msg, pop,index);
            }
            LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(linkList,pop.getId(),nodeMap);
            for (Node item : childNodes) {
                stack.add(item);
            }
        }
    }
}
