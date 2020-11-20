package com.readboy.mytreeview.view;

public class ViewBox {
    private int curIndex;
    public int top;
    public int left;
    public int right;
    public int bottom;

    public void clear() {
        this.top = 0;
        this.left = 0;
        this.right = 0;
        this.bottom = 0;
    }

    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    @Override
    public String toString() {
        return "ViewBox{" +
                "curIndex=" + curIndex +
                ", top=" + top +
                ", left=" + left +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }
}
