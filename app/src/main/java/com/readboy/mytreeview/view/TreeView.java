package com.readboy.mytreeview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.control.MoveAndScaleHandler;
import com.readboy.mytreeview.interfaces.TreeViewItemClick;
import com.readboy.mytreeview.interfaces.TreeViewItemLongClick;
import com.readboy.mytreeview.model.TreeModel;
import com.readboy.mytreeview.utils.AtlasUtil;
import com.readboy.mytreeview.utils.ViewUtil;
import com.readboy.mytreeview.utils.log.LogUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.readboy.mytreeview.utils.DensityUtils.dp2px;

public class TreeView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener{
    private  Paint mPaint;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    public TreeModel mTreeModel;
    private TreeLayoutManager mTreeLayoutManager;
    private GestureDetector mGestureDetector;

    //点击事件
    private TreeViewItemClick mTreeViewItemClick;
    //长按
    private TreeViewItemLongClick mTreeViewItemLongClick;

    private MoveAndScaleHandler mMoveAndScaleHandler;


    //最近点击的item
    private Node mCurrentFocus;
    public TreeView(Context context) {
        super(context);
    }

    public TreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setClipChildren(false);
        setClipToPadding(false);
        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        mMoveAndScaleHandler = new MoveAndScaleHandler(context, this);
        //点击空白处
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d("LZY","double click here " );
                return true;
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = getChildCount();
        for (int i = 0; i < size; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        LogUtils.d("current Node next rootTreeViewLayout= " + mHeight + ",width = " + mWidth);

        if (mTreeLayoutManager != null && mTreeModel != null) {
            //树形结构的分布
            mTreeLayoutManager.onTreeLayout(this);
            ViewBox viewBox = mTreeLayoutManager.onTreeLayoutCallBack();
            boxCallBackChange();
            setMeasuredDimension(viewBox.right+Math.abs(viewBox.left),viewBox.bottom+Math.abs(viewBox.top));

        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            childAt.layout(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom());
        }
    }

    /**
     * 绘制VIew本身的内容，通过调用View.onDraw(canvas)函数实现
     * 绘制自己的孩子通过dispatchDraw（canvas）实现
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mTreeModel != null) {
            for (Node node : mTreeModel.getRootNode()) {
                drawTreeLine(canvas,node);
                ViewBox viewBox = mTreeLayoutManager.onTreeLayoutCallBack();
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return mMoveAndScaleHandler.onTouchEvent(event);
    }

    /**
     * 绘制树形的连线
     *
     * @param canvas
     * @param root
     */
    private void drawTreeLine(Canvas canvas, Node root) {
        NodeView fatherView = (NodeView) findNodeViewFromNodeModel(root);
        if (fatherView != null) {
            LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),root.getId(),mTreeModel.getNodeMap());
            for (Node node : childNodes) {
                //连线
                LogUtils.d("drawTreeLine father = "  + fatherView.getName() + ", order = " + fatherView.getOrder());
                ViewUtil.drawLineToView(canvas, fatherView, (NodeView)findNodeViewFromNodeModel(node),mPaint,mContext);
                //递归
                drawTreeLine(canvas, node);
            }
        }
    }



    private void boxCallBackChange() {
        ViewBox viewBox = mTreeLayoutManager.onTreeLayoutCallBack();
        int beforeGap = 0;

        List<Node> nodes = getTreeModel().getRootNode();

        //移动节点
        for (int i = 0 ; i <  nodes.size(); i++){
            Node node = nodes.get(i);
            int min = findMinTopNode(node);
            //todo Lzy移动根节点，相当于该树所有节点向下移动了该距离
            if(i == 0){
                moveNodeLayout(this, (NodeView) findNodeViewFromNodeModel(node), Math.abs(min));
                continue;
            }

            ViewBox box1 = ((RightTreeLayoutManager)mTreeLayoutManager).getBoxHashMap().get(i-1);
            LogUtils.d("maxNode = beforeGap =" + box1.toString() + "name  = " + node.getName());

            LogUtils.d("");
            int curGap =  box1.bottom - min + ((RightTreeLayoutManager)mTreeLayoutManager).getmDy();
            if(min < box1.bottom){
                moveNodeLayout(this, (NodeView) findNodeViewFromNodeModel(node), curGap + beforeGap   );
                beforeGap = curGap + beforeGap;
               // LogUtils.d("maxNode = beforeGap =" +curGap + "name  = " + node.getName());

            }else {
                LogUtils.d("maxNode = beforeGap =" + beforeGap + "name  = " + node.getName());
                moveNodeLayout(this, (NodeView) findNodeViewFromNodeModel(node), curGap + beforeGap);

            }
        }

        Node lastRootNode = nodes.get(nodes.size() -1);
        NodeView maxNode= findMaxTopNode(lastRootNode);
        viewBox.bottom = maxNode.getBottom();
        viewBox.right = ((RightTreeLayoutManager)mTreeLayoutManager).getMaxRight();
    }




    private NodeView findMaxTopNode(Node rootNode){
        Deque<Node> queue = new ArrayDeque<>();
        NodeView result = new NodeView(mContext);
        Node curNode = new Node();
        queue.add(rootNode);
        int max = findNodeViewFromNodeModel(rootNode).getTop();
        while (!queue.isEmpty()) {
            curNode =  queue.poll();
            NodeView curView = (NodeView) findNodeViewFromNodeModel(curNode);
            int curTop = curView.getTop();
            if(curTop > max){
                max = curTop;
                result = curView;
            }
            LinkedList<Node> childNodes = (LinkedList<Node>) AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),curNode.getId(),mTreeModel.getNodeMap());;
            if (childNodes.size() > 0) {
                queue.addAll(childNodes);
            }
        }
        return result;
    }

    private int findMinTopNode(Node rootNode){
        Deque<Node> queue = new ArrayDeque<>();
        Node curNode = new Node();
        queue.add(rootNode);
        int min = findNodeViewFromNodeModel(rootNode).getTop();

        while (!queue.isEmpty()) {
            curNode =  queue.poll();
            NodeView curView = (NodeView) findNodeViewFromNodeModel(curNode);
            int curTop = curView.getTop();
            if(curTop < min){
                min = curTop;
                LogUtils.d("maxNode = beforeGap1111 =" + min  + "name  = " + rootNode.getName());

            }
            LinkedList<Node> childNodes = (LinkedList<Node>) AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),curNode.getId(),mTreeModel.getNodeMap());;
            if (childNodes.size() > 0) {
                queue.addAll(childNodes);
            }
        }
        return min;
    }
    /**
     * 移动
     *
     * @param rootView
     * @param dy
     */
    private void moveNodeLayout(TreeView superTreeView, NodeView rootView, int dy) {

        if (dy == 0) {
            return;
        }

        Deque<Node> queue = new ArrayDeque<>();
        Node rootNode = rootView.getNode();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            rootNode = queue.poll();
            rootView = (NodeView) superTreeView.findNodeViewFromNodeModel(rootNode);
            int l = rootView.getLeft();
            int t = rootView.getTop() + dy;
            rootView.layout(l, t, l + rootView.getMeasuredWidth(), t + rootView.getMeasuredHeight());

            LinkedList<Node> childNodes = AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),rootNode.getId(),mTreeModel.getNodeMap());
            for (Node item : childNodes) {
                queue.add(item);
            }
        }
    }

    public TreeModel getTreeModel() {
        return mTreeModel;
    }

    public void setTreeModel(TreeModel treeModel) {
        mTreeModel = treeModel;
        clearAllNoteViews();
        addNoteViews();
    }


    /**
     * 清除所有的NoteView
     */
    private void clearAllNoteViews() {
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View childView = getChildAt(i);
                if (childView instanceof NodeView) {
                    removeView(childView);
                }
            }
        }
    }

    /**
     * 添加所有的NoteView
     */
    private void addNoteViews() {
        if (mTreeModel != null) {
            List<Node> rootNode = mTreeModel.getRootNode();
            Deque<Node> deque = new ArrayDeque<>();

            for (Node model : rootNode) {
                deque.add(model);
                while (!deque.isEmpty()) {
                    Node poll = deque.poll();
                    addNodeViewToGroup(poll);
                    List<Node> childNodes = AtlasUtil.getSubNodeAccordId(mTreeModel.getLinkList(),poll.getId(),mTreeModel.getNodeMap());
                    if( childNodes.size() > 0){
                       for (Node ch : childNodes) {
                           deque.push(ch);
                       }
                   }
                }
            }
        }
    }

    private View addNodeViewToGroup(Node poll) {
        final NodeView nodeView = new NodeView(mContext,null);
        nodeView.setFocusable(true);
        nodeView.setClickable(true);
        nodeView.setSelected(false);
        nodeView.setNode(poll);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nodeView.setLayoutParams(lp);
        nodeView.getTvOrder().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                performTreeItemClick(view);
            }
        });
        nodeView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                preformTreeItemLongClick(v);
                return true;
            }
        });

        this.addView(nodeView);
        return nodeView;
    }

    private void preformTreeItemLongClick(View v) {
        if (mTreeViewItemLongClick != null) {
            mTreeViewItemLongClick.onLongClick(v);
        }
    }

    private void performTreeItemClick(View view) {
        if (mTreeViewItemClick != null) {
            mTreeViewItemClick.onItemClick(view);
        }
    }


    /**
     * 模型查找NodeView
     *
     * @param nodeModel
     * @return
     */
    public View findNodeViewFromNodeModel(Node nodeModel) {
        View view = null;
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View childView = getChildAt(i);
            if (childView instanceof NodeView) {
                Node treeNode = ((NodeView) childView).getNode();
                if (treeNode == nodeModel) {
                    view = childView;
                    continue;
                }
            }
        }
        return view;
    }

    /**
     * 设置树形结构分布管理器
     *
     * @param treeLayoutManager
     */
    public void setTreeLayoutManager(TreeLayoutManager treeLayoutManager) {
        mTreeLayoutManager = treeLayoutManager;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


    public TreeLayoutManager getmTreeLayoutManager() {
        return mTreeLayoutManager;
    }

    public void setmTreeLayoutManager(TreeLayoutManager mTreeLayoutManager) {
        this.mTreeLayoutManager = mTreeLayoutManager;
    }

    public TreeViewItemClick getmTreeViewItemClick() {
        return mTreeViewItemClick;
    }

    public void setmTreeViewItemClick(TreeViewItemClick mTreeViewItemClick) {
        this.mTreeViewItemClick = mTreeViewItemClick;
    }

    public TreeViewItemLongClick getmTreeViewItemLongClick() {
        return mTreeViewItemLongClick;
    }

    public void setmTreeViewItemLongClick(TreeViewItemLongClick mTreeViewItemLongClick) {
        this.mTreeViewItemLongClick = mTreeViewItemLongClick;
    }

    public Node getmCurrentFocus() {
        return mCurrentFocus;
    }

    public void setmCurrentFocus(Node mCurrentFocus) {
        this.mCurrentFocus = mCurrentFocus;
    }
}
