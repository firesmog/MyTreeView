package com.readboy.mytreeview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.readboy.mytreeview.R;
import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.constants.Constants;
import com.readboy.mytreeview.utils.log.LogUtils;

import static android.graphics.drawable.GradientDrawable.OVAL;
import static android.graphics.drawable.GradientDrawable.RECTANGLE;

public class NodeView extends RelativeLayout {
    private  String nodeBackground;
    private  int nameColor;
    private  int numberColor;
    private  String name;
    private  String nodeShape;
    private  int order;
    private  float nodeRadius;
    private  float nameSize;
    private  float orderSize;
    private  float marginSize;
    private  float tvOrderWidth;
    private TextView tvName;
    private TextView tvOrder;
    private long nodeId;

    private Context context;
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
        name = node.getName();
        order = node.getOrder();
        nodeBackground = node.getShape().getColor();
        nodeShape = node.getShape().getType();
        nodeId = node.getId();
        initTvName();
        initTvOrder();

    }

    public NodeView(Context context) {
        super(context);
        this.context = context;

    }

    public NodeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray array = null;
        try {
            array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NodeView, 0, 0);
            order =  array.getInteger(R.styleable.NodeView_order, 1);
            name =  array.getString(R.styleable.NodeView_name);
            nodeShape =  array.getString(R.styleable.NodeView_nodeShape);
            nodeBackground = array.getString(R.styleable.NodeView_nodeBackground);
            nameColor = array.getColor(R.styleable.NodeView_nameColor, Color.BLUE);
            numberColor = array.getColor(R.styleable.NodeView_orderColor, Color.BLUE);
            nodeRadius = array.getDimension(R.styleable.NodeView_nodeRadius, 14);
            nameSize = array.getDimension(R.styleable.NodeView_nameSize, 14);
            marginSize = array.getDimension(R.styleable.NodeView_marginSize, -1);
            orderSize = array.getDimension(R.styleable.NodeView_orderSize, 14);
            tvOrderWidth = array.getDimension(R.styleable.NodeView_tvOrderWidth, 90);
        } finally {
            array.recycle();
        }

        initView(context);
    }


    private void initView(Context context){
        View inflate = LayoutInflater.from(context).inflate(R.layout.node_view_layout, this);
        tvName = (TextView)inflate.findViewById(R.id.tv_name);
        tvOrder = (TextView)inflate.findViewById(R.id.tv_order);

        LogUtils.d("initView initView = "  + this.getWidth() );
    }

    private void initTvName(){
        if(!TextUtils.isEmpty(name)){
            tvName.setText(name);
        }
        tvName.setTextSize(nameSize);
        tvName.setTextColor(nameColor);
    }

    @SuppressLint("WrongConstant")
    private void initTvOrder(){
        tvOrder.setText(String.valueOf(order));
        tvOrder.setTextColor(numberColor);
        tvOrder.setTextSize(orderSize);
        //创建Drawable对象
        int[] colors = {0xFFFF9326,0xFFFFC54E};
        GradientDrawable drawable=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors);

        if(Constants.SHAPE_RECTANGLE.equals(nodeShape)){
            drawable.setShape(RECTANGLE);
            drawable.setGradientType(RECTANGLE);

        }else {
            drawable.setShape(OVAL);
            drawable.setGradientRadius(nodeRadius);
            tvOrder.setWidth((int) tvOrderWidth);
            tvOrder.setHeight((int) tvOrderWidth);
            drawable.setGradientType(OVAL);

        }

        tvOrder.setBackground(drawable);

        if(marginSize != -1){
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)tvOrder.getLayoutParams();
            lp.topMargin = (int)marginSize;
            tvOrder.setLayoutParams(lp);
        }
    }


    public TextView getTvNameTextView(){
        return tvName;
    }

    public TextView getTvOrderTextView(){
        return tvOrder;
    }

    public String getNodeBackground() {
        return nodeBackground;
    }

    public void setNodeBackground(String nodeBackground) {
        this.nodeBackground = nodeBackground;
    }

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public int getNumberColor() {
        return numberColor;
    }

    public void setNumberColor(int numberColor) {
        this.numberColor = numberColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeShape() {
        return nodeShape;
    }

    public void setNodeShape(String nodeShape) {
        this.nodeShape = nodeShape;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public float getNodeRadius() {
        return nodeRadius;
    }

    public void setNodeRadius(float nodeRadius) {
        this.nodeRadius = nodeRadius;
    }

    public float getNameSize() {
        return nameSize;
    }

    public void setNameSize(float nameSize) {
        this.nameSize = nameSize;
    }

    public float getOrderSize() {
        return orderSize;
    }

    public void setOrderSize(float orderSize) {
        this.orderSize = orderSize;
    }

    public float getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(float marginSize) {
        this.marginSize = marginSize;
    }

    public float getTvOrderWidth() {
        return tvOrderWidth;
    }

    public void setTvOrderWidth(float tvOrderWidth) {
        this.tvOrderWidth = tvOrderWidth;
    }

    public TextView getTvName() {
        return tvName;
    }

    public void setTvName(TextView tvName) {
        this.tvName = tvName;
    }

    public TextView getTvOrder() {
        return tvOrder;
    }

    public void setTvOrder(TextView tvOrder) {
        this.tvOrder = tvOrder;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }
}
