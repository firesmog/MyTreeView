package com.readboy.mytreeview.utils;


import android.text.TextUtils;

import com.readboy.mytreeview.bean.AtlasBean;
import com.readboy.mytreeview.bean.AtlasMapping;
import com.readboy.mytreeview.bean.AtlasNode;
import com.readboy.mytreeview.bean.Link;
import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.utils.log.LogUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AtlasUtil {
    private static List<Long> knowledgePointId = new ArrayList<>();
    private static List<Node> results = new ArrayList<>();

    //找到图谱中的所有知识点
    public static List<Node> findKnowledgeNode(AtlasBean bean) {
        if (null == bean) {
            LogUtils.d("findFirstQueNode findKnowledgeNode is null");
            return null;
        }
        List<Node> result = new ArrayList<>();
        List<Node> org = bean.getData().getMapping().getNodes();
        for (Node node : org) {
            if (node.getType() == 1) {
                result.add(node);
            }
        }
        return result;
    }

    //找到图谱中的所有根节点
    public static List<Node> findRootKnowledgeNode(AtlasBean bean) {
        if (null == bean) {
            LogUtils.d("findFirstQueNode findKnowledgeNode is null");
            return null;
        }

        List<Node> nodeList = bean.getData().getMapping().getNodes();
        List<Link> links = bean.getData().getMapping().getLinks();

        if(null == nodeList || nodeList.size() == 1){
            return nodeList;
        }



        Iterator<Node> iterator = nodeList.iterator();
        inner:
        while (iterator.hasNext()){
               Node node = iterator.next();
               for (Link link : links) {
                   if(link.getTargetid() == node.getId()){
                       iterator.remove();
                       continue inner;
                   }
               }
        }
        return nodeList;
    }



    //根据nodeId 获取子节点
    public static LinkedList<Node> getSubNodeAccordId(List<Link> links, long nodeId, HashMap<Long,Node> nodeMap) {
        LinkedList<Node> nodes = new LinkedList<>();
        for (Link link : links) {
            if(link.getSourceid() == nodeId){
                //targetId 才是子view
                nodes.add(nodeMap.get(link.getTargetid()));
            }
        }
        Collections.sort(nodes,new Node());
        return nodes;
    }



    public static Node getParentNodeAccordId(List<Link> links, long nodeId, HashMap<Long,Node> nodeMap) {
        Node parent = new Node();
        for (Link link : links) {
            if(link.getTargetid() == nodeId){
                //targetId 才是子view
                parent = nodeMap.get(link.getSourceid());
            }
        }
        return parent;
    }

    public static LinkedList<Node> getBrotherNodeAccordId( Node node, HashMap<Long,Node> nodeMap) {
        LinkedList<Node> brothers = new LinkedList<>();
        for (Map.Entry<Long, Node> longNodeEntry : nodeMap.entrySet()) {
            Node brother = longNodeEntry.getValue();
            if(brother.getFloor() == node.getFloor()){
                brothers.add(brother);
            }
        }

        Collections.sort(brothers,new Node());
        return brothers;
    }


    public static int getOrderInNodes(AtlasMapping mapping, long id){
        List<Long>  orders = mapping.getNodeOrder().get(0).getOrder();
        return orders.indexOf(id);

    }

    //按照后台提供的已学考点（或知识点）的id，找出所有需要展示的链路上的节点（可重复）保存起来
    public static List<Node> findQuestionNode(AtlasBean bean,List<Long> ids) {
        if (bean == null) {
            LogUtils.d("findFirstQueNode findQuestionNode is null");
            return null;
        }
        results = new ArrayList<>();
        knowledgePointId = new ArrayList<>();
        List<Node> knowledgeNodes = findKnowledgeNode(bean);
        if(ids == null || ids.size() == 0){
            return knowledgeNodes;
        }
        results.addAll(knowledgeNodes);
        List<Node> org = bean.getData().getMapping().getNodes();
        List<Link> links = bean.getData().getMapping().getLinks();

        //先将已经学过的知识点筛选出来，全部考点都需要展示
        for (Node knowledgeNode : knowledgeNodes) {
            for (Long id : ids) {
                if(id == knowledgeNode.getId()){
                    knowledgePointId.add(id);
                    results.addAll(findAllPointAccordKnowledge(id,org));
                }
            }
        }

        //再将非知识点的考点链路上的考点筛选出来。
        for (Long id : ids) {
            //1、跳过已经筛选过的知识点，只对考点做处理
            if(knowledgePointId.contains(id)){
                LogUtils.d("findQuestionNode id1111 = " + id);
                continue ;
            }

            //2、若当前考点属于已经筛选过的知识点，则跳过该考点
            Node curQueNode = findQueNodeAccordId(id,org);
            if(curQueNode != null && knowledgePointId.contains(curQueNode.getKeypoint())){
                LogUtils.d("findQuestionNode id2222= " + curQueNode.toString());
                continue;
            }

            findAllPointAccordQue(id,org,links);
        }

        return results;
    }

    //知识点已学，需获取该知识点所有已学节点链路
    private  static List<Node> findAllPointAccordKnowledge(long id,List<Node> org){
        List<Node> results = new ArrayList<>();
        for (Node node : org) {
            if(node.getKeypoint() == id){
                results.add(node);
                LogUtils.d("findQuestionNode id0000 = " + node.toString() );
            }
        }
        return results;
    }

    //倒叙递归（直至节点为知识点），获取该考点链路上的所有节点
    private  static void findAllPointAccordQue(long id,List<Node> org, List<Link> links){
        Node curNode = findQueNodeAccordId(id,org);
        if(curNode == null || curNode.getType() == 1){
            LogUtils.d("findAllPointAccordQue end");
            return;
        }
        LogUtils.d("findQuestionNode id333 = " + curNode.toString());
        results.add(curNode);
        for (Link link : links) {
            if(link.getTargetid() == id){
                findAllPointAccordQue(link.getSourceid(),org,links);
                LogUtils.d("findQuestionNode id555 = " + id + ",source id = " + link.getSourceid() + ",target id = " + link.getTargetid()  );
            }
        }
    }

    //通过id找出节点
    private static Node findQueNodeAccordId(long id,List<Node> org){
        for (Node node1 : org) {
            if(node1.getId() == id){
                return node1;
            }
        }
        return null;
    }

    //筛选从后套获取的图谱数据
    public static AtlasBean getFilterAtlasBean(AtlasBean bean,List<Long> ids){
        List<Node> filterNode = findQuestionNode(bean,ids);
        if (filterNode == null) {
            LogUtils.d("getFilterAtlasBean is null");
            return null;
        }

        List<Node> org = bean.getData().getMapping().getNodes();
        Iterator<Node> it = org.iterator();

        for (Node node : filterNode) {
            LogUtils.d("getFilterAtlasBean next1111 = " + node.getName() );
        }


        while(it.hasNext()){
            Node next = it.next();
            LogUtils.d("getFilterAtlasBean next = " + next.getName() );

            if(!filterNode.contains(next)){
                it.remove();
            }
        }
        return bean;
    }

    public static AtlasBean setNodeFloor(AtlasBean bean,List<Node> root){
        AtlasMapping map = bean.getData().getMapping();
        Deque<Node> deque = new ArrayDeque<>();
        HashMap<Long,Node> hashMap = new HashMap();



        for (Node node : map.getNodes()) {
            hashMap.put(node.getId(),node);
        }


        for (Node node : root) {
            deque.add(node);
            while (!deque.isEmpty()) {
                Node poll = deque.poll();
                List<Node> childNodes = AtlasUtil.getSubNodeAccordId(map.getLinks(),poll.getId(),hashMap);
                if( childNodes.size() > 0){
                    int floor = poll.getFloor();
                    for (Node ch : childNodes) {
                        if(null != ch){
                          ch.setFloor(floor+ 1);
                            deque.push(ch);
                      }
                    }
                }
            }
        }
        return bean;

    }
}
