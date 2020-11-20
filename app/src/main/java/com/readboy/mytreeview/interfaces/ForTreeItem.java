package com.readboy.mytreeview.interfaces;

import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.model.TreeModel;

import java.io.Serializable;

/**
 * Created by owant on 09/03/2017.
 */

public interface ForTreeItem{
    void next(int msg, Node next);
}
