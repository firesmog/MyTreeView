package com.readboy.mytreeview.bean;

public class Node {
    private String name;
    private Shape shape;
    private double x;
    private double y;
    private Font font;
    private int type;
    private long id;
    private long keypoint;

    public long getKeypoint() {
        return keypoint;
    }

    public void setKeypoint(long keypoint) {
        this.keypoint = keypoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", shape=" + shape +
                ", x=" + x +
                ", y=" + y +
                ", font=" + font +
                ", type=" + type +
                ", id=" + id +
                '}';
    }
}
