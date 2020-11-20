package com.readboy.mytreeview.view;


import android.icu.text.UFormat;
import android.view.View;

import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.interfaces.ForTreeItem;
import com.readboy.mytreeview.model.TreeModel;
import com.readboy.mytreeview.utils.AtlasUtil;
import com.readboy.mytreeview.utils.log.LogUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by owant on 08/03/2017.
 */
public class RightTreeLayoutManager implements TreeLayoutManager {

    final int msg_standard_layout = 1;
    final int msg_correct_layout = 2;
    final int msg_box_call_back = 3;
    private int curIndex;

    private ViewBox mViewBox;
    private int mDy;
    private int mDx;
    private int mHeight;

    public RightTreeLayoutManager(int dx, int dy, int height) {
        mViewBox = new ViewBox();

        this.mDx = dx;
        this.mDy = dy;
        this.mHeight = height;
    }

    @Override
    public void onTreeLayout(final TreeView treeView) {
        final TreeModel mTreeModel = treeView.getTreeModel();
        List<Node> rootNode = mTreeModel.getRootNode();

        //todo Lzy这里添加next布局
        mTreeModel.addForTreeItem(new ForTreeItem() {
            @Override
            public void next(int msg,Node next,int index) {
                doNext(msg, next,treeView,index);
            }
        });

        //遍历每个根节点，逐个处理
        for (Node node : rootNode) {
            if(rootNode.indexOf(node) != 1){
                continue;
            }
            View rootView = treeView.findNodeViewFromNodeModel(node);
            int index = rootNode.indexOf(node);

            if (rootView != null) {
                //根节点位置
                rootTreeViewLayout((NodeView) rootView,treeView);
            }

            //基本布局
            mTreeModel.ergodicTreeInWith(msg_standard_layout,node,index);
            //纠正
            mTreeModel.ergodicTreeInWith(msg_correct_layout,node,index);
            mViewBox.clear();
            mTreeModel.ergodicTreeInDeep(msg_box_call_back,node,index);
            return;
        }
    }




    @Override
    public ViewBox onTreeLayoutCallBack() {
        if (mViewBox != null) {
            return mViewBox;
        } else {
            return null;
        }
    }

    private void doNext(int msg,  Node next,TreeView treeView,int index) {
        View view = treeView.findNodeViewFromNodeModel(next);


        if (msg == msg_standard_layout) {
            //标准分布
            standardLayout(treeView, (NodeView) view);
        } else if (msg == msg_correct_layout) {
            //纠正
           correctLayout(treeView, (NodeView) view,index);
        } else if (msg == msg_box_call_back) {

            //View的大小变化
            int left = view.getLeft();
            int top = view.getTop();
            int bottom = view.getBottom();
            int right = view.getRight();

            //     *******
            //     *     *
            //     *     *
            //     *******

            if (left < mViewBox.left) {
                mViewBox.left = left;
            }
            if (top < mViewBox.top) {
                mViewBox.top = top;
            }
            if (bottom > mViewBox.bottom) {
                mViewBox.bottom = bottom;
            }
            if (right > mViewBox.right) {
                mViewBox.right = right;
            }
        }
    }

    /**
     * 布局纠正
     *
     * @param treeView
     * @param next
     */
    public void correctLayout(TreeView treeView, NodeView next,int index) {

        TreeModel mTree = treeView.getTreeModel();
        LinkedList<Node> subNodeList = AtlasUtil.getSubNodeAccordId(mTree.getLinkList(),next.getNodeId(),mTree.getNodeMap());
        int count = subNodeList.size();
        if (next.getParent() != null && count >= 2) {
            Node tn = subNodeList.get(0);
            Node bn = subNodeList.get(count - 1);

            //这里计算的是偏移距离
            int topDr = next.getTop() - treeView.findNodeViewFromNodeModel(tn).getBottom() + mDy;
            int bnDr = treeView.findNodeViewFromNodeModel(bn).getTop() - next.getBottom() + mDy;

            //上移动
            ArrayList<Node> allLowNodes = mTree.getAllLowNodes(bn);
            ArrayList<Node> allPreNodes = mTree.getAllPreNodes(tn);

            for (Node low : allLowNodes) {
                NodeView view = (NodeView) treeView.findNodeViewFromNodeModel(low);
                moveNodeLayout(treeView, view, bnDr,index);
            }

            for (Node pre : allPreNodes) {
                NodeView view = (NodeView) treeView.findNodeViewFromNodeModel(pre);
                moveNodeLayout(treeView, view, -topDr,index);

            }
        }
    }

    /**
     * 标准分布
     * todo Lzy 布局精髓所在
     * @param treeView
     * @param rootView
     */
   /* private void standardLayout(TreeView treeView, NodeView rootView) {
        Node treeNode = rootView.getNode();
        TreeModel mTreeModel =  treeView.getTreeModel();
        if (treeNode != null) {
            //所有的子节点
            LinkedList<Node> childNodes = (LinkedList<Node>) AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),treeNode.getId(),mTreeModel.getNodeMap());
            int size = childNodes.size();
            int mid = size / 2;
            int r = size % 2;

            //基线
            //        b
            //    a-------
            //        c
            //
            int left = rootView.getRight() + mDx;
            int top = rootView.getTop() + rootView.getMeasuredHeight() / 2;

            int right = 0;
            int bottom = 0;

            if (size == 0) {
                return;
            } else if (size == 1) {
                NodeView midChildNodeView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(0));
                top = top - midChildNodeView.getMeasuredHeight() / 2;
                right = left + midChildNodeView.getMeasuredWidth();
                bottom = top + midChildNodeView.getMeasuredHeight();
                midChildNodeView.layout(left, top, right, bottom);


            } else {
                int topLeft = left;
                int topTop = top;
                int topRight = 0;
                int topBottom = 0;

                int bottomLeft = left;
                int bottomTop = top;
                int bottomRight = 0;
                int bottomBottom = 0;

                if (r == 0) {//偶数
                    for (int i = mid - 1; i >= 0; i--) {
                        //找出上下两个子节点
                        NodeView topView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(i));
                        NodeView bottomView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(size - i - 1));


                        //从中间往上下两边扩散画图
                        if (i == mid - 1) {
                            topTop = topTop - mDy / 2 - topView.getMeasuredHeight();
                            topRight = topLeft + topView.getMeasuredWidth();
                            topBottom = topTop + topView.getMeasuredHeight();

                            bottomTop = bottomTop + mDy / 2;
                            bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                            bottomBottom = bottomTop + bottomView.getMeasuredHeight();
                        } else {
                            topTop = topTop - mDy - topView.getMeasuredHeight();
                            topRight = topLeft + topView.getMeasuredWidth();
                            topBottom = topTop + topView.getMeasuredHeight();

                            bottomTop = bottomTop + mDy;
                            bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                            bottomBottom = bottomTop + bottomView.getMeasuredHeight();
                        }

                        topView.layout(topLeft, topTop, topRight, topBottom);
                        bottomView.layout(bottomLeft, bottomTop, bottomRight, bottomTop);

                        bottomTop = bottomView.getBottom();
                    }

                } else {
                    NodeView midView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(mid));
                    midView.layout(left, top - midView.getMeasuredHeight() / 2, left + midView.getMeasuredWidth(),
                            top - midView.getMeasuredHeight() / 2 + midView.getMeasuredHeight());

                    topTop = midView.getTop();
                    bottomTop = midView.getBottom();

                    for (int i = mid - 1; i >= 0; i--) {
                        NodeView topView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(i));
                        NodeView bottomView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(size - i - 1));

                        topTop = topTop - mDy - topView.getMeasuredHeight();
                        topRight = topLeft + topView.getMeasuredWidth();
                        topBottom = topTop + topView.getMeasuredHeight();

                        bottomTop = bottomTop + mDy;
                        bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                        bottomBottom = bottomTop + bottomView.getMeasuredHeight();

                        topView.layout(topLeft, topTop, topRight, topBottom);
                        bottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);
                        bottomTop = bottomView.getBottom();
                    }
                }
            }
        }
    }*/



    private void standardLayout(TreeView treeView, NodeView rootView) {
        Node treeNode = rootView.getNode();
        TreeModel mTreeModel =  treeView.getTreeModel();
        if (treeNode != null) {
            //所有的子节点
            LinkedList<Node> childNodes = (LinkedList<Node>) AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),treeNode.getId(),mTreeModel.getNodeMap());
            int size = childNodes.size();
            int mid = size / 2;
            int r = size % 2;

            //基线
            //        b
            //    a-------
            //        c
            //
            int left = rootView.getRight() + mDx;
            int top = rootView.getTop() + rootView.getMeasuredHeight() / 2;

            int right = 0;
            int bottom = 0;

            if (size == 0) {
                return;
            } else if (size == 1) {
                NodeView midChildNodeView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(0));
                top = top - midChildNodeView.getMeasuredHeight() / 2;
                right = left + midChildNodeView.getMeasuredWidth();
                bottom = top + midChildNodeView.getMeasuredHeight();
                midChildNodeView.layout(left, top, right, bottom);


            } else {
                int topLeft = left;
                int topTop = top;
                int topRight = 0;
                int topBottom = 0;

                int bottomLeft = left;
                int bottomTop = top;
                int bottomRight = 0;
                int bottomBottom = 0;

                if (r == 0) {//偶数
                    for (int i = mid - 1; i >= 0; i--) {
                        //找出上下两个子节点
                        NodeView topView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(i));
                        NodeView bottomView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(size - i - 1));
                        LogUtils.d("topppp = " + topView.getName() + ", bottom  == " + bottomView.getName());


                        //从中间往上下两边扩散画图
                        if (i == mid - 1) {
                            topTop = topTop - mDy / 2 - topView.getMeasuredHeight();
                            topRight = topLeft + topView.getMeasuredWidth();
                            topBottom = topTop + topView.getMeasuredHeight();

                            bottomTop = bottomTop + mDy / 2;
                            bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                            bottomBottom = bottomTop + bottomView.getMeasuredHeight();
                        } else {
                            topTop = topTop - mDy - topView.getMeasuredHeight();
                            topRight = topLeft + topView.getMeasuredWidth();
                            topBottom = topTop + topView.getMeasuredHeight();

                            bottomTop = bottomTop + mDy;
                            bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                            bottomBottom = bottomTop + bottomView.getMeasuredHeight();
                        }

                        topView.layout(topLeft, topTop, topRight, topBottom);
                        bottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);

                        bottomTop = bottomView.getBottom();
                    }

                } else {
                    NodeView midView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(mid));
                    midView.layout(left, top - midView.getMeasuredHeight() / 2, left + midView.getMeasuredWidth(),
                            top - midView.getMeasuredHeight() / 2 + midView.getMeasuredHeight());

                    topTop = midView.getTop();
                    bottomTop = midView.getBottom();

                    for (int i = mid - 1; i >= 0; i--) {
                        NodeView topView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(i));
                        NodeView bottomView = (NodeView) treeView.findNodeViewFromNodeModel(childNodes.get(size - i - 1));

                        topTop = topTop - mDy - topView.getMeasuredHeight();
                        topRight = topLeft + topView.getMeasuredWidth();
                        topBottom = topTop + topView.getMeasuredHeight();

                        bottomTop = bottomTop + mDy;
                        bottomRight = bottomLeft + bottomView.getMeasuredWidth();
                        bottomBottom = bottomTop + bottomView.getMeasuredHeight();

                        topView.layout(topLeft, topTop, topRight, topBottom);
                        bottomView.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);
                        bottomTop = bottomView.getBottom();
                    }
                }
            }
        }
    }

    /**
     * 移动
     *
     * @param rootView
     * @param dy
     */
    private void moveNodeLayout(TreeView superTreeView, NodeView rootView, int dy,int index) {
        Deque<Node> queue = new ArrayDeque<>();
        Node rootNode = rootView.getNode();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            rootNode = queue.poll();
            rootView = (NodeView) superTreeView.findNodeViewFromNodeModel(rootNode);
            int l = rootView.getLeft();
            int t = rootView.getTop() + dy;
            rootView.layout(l, t, l + rootView.getMeasuredWidth(), t + rootView.getMeasuredHeight());

            LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(superTreeView.getTreeModel().getLinkList(),rootNode.getId(),superTreeView.getTreeModel().getNodeMap());
            for (Node item : childNodes) {
                queue.add(item);
            }
        }
    }


    /**
     * root节点的定位
     *
     * @param rootView
     */
    private void rootTreeViewLayout(NodeView rootView,TreeView treeView) {
        int lr = mDy;
        int tr = mDx  ;
        int rr = lr + rootView.getMeasuredWidth();
        int br = tr + rootView.getMeasuredHeight() ;

        rootView.layout(lr, tr, rr, br);

    }
}
