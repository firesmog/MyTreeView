package com.readboy.mytreeview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.readboy.mytreeview.bean.AtlasBean;
import com.readboy.mytreeview.bean.AtlasMapping;
import com.readboy.mytreeview.bean.Node;
import com.readboy.mytreeview.model.TreeModel;
import com.readboy.mytreeview.utils.AssertsUtil;
import com.readboy.mytreeview.utils.AtlasUtil;
import com.readboy.mytreeview.utils.DensityUtils;
import com.readboy.mytreeview.utils.log.LogUtils;
import com.readboy.mytreeview.view.RightTreeLayoutManager;
import com.readboy.mytreeview.view.TreeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TreeView editMapTreeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        editMapTreeView = (TreeView) findViewById(R.id.edit_map_tree_view);
        initData();
    }





    private void initData(){
        Gson gson = new Gson();
        String json = AssertsUtil.getFromAssets(MainActivity.this,"json.txt");
        AtlasBean data = gson.fromJson(json,AtlasBean.class);

        List<Node> orgNodes = new ArrayList<>();
        orgNodes.addAll(data.getData().getMapping().getNodes());

        List<Node> rootNode = getRealRootNode(AtlasUtil.findRootKnowledgeNode(data),data);
        data.getData().getMapping().setNodes(orgNodes);

        AtlasUtil.setNodeFloor(data,rootNode);
        LinkedHashMap<Long, Node> models = getNodeList(data);

        TreeModel model = new TreeModel();
        model.setLinkList(data.getData().getMapping().getLinks());
        model.setRootNode(rootNode);
        model.setNodeMap(models);
        editMapTreeView.setTreeModel(model);
        int dx = DensityUtils.dp2px(getApplicationContext(), 150);
        int dy = DensityUtils.dp2px(getApplicationContext(), 30);
        int screenHeight = DensityUtils.dp2px(getApplicationContext(), 720);
        editMapTreeView.setTreeLayoutManager(new RightTreeLayoutManager(dx, dy, screenHeight));
    }

    private List<Node> getRealRootNode(List<Node> nodes,AtlasBean data){
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            node.setFloor(0);
            node.setOrder(AtlasUtil.getOrderInNodes(data.getData().getMapping(),node.getId()));
            result.add(node);
        }
        return result;
    }

    private LinkedHashMap<Long,Node> getNodeList(AtlasBean data){
        LinkedHashMap<Long,Node> map = new LinkedHashMap<>();
        AtlasMapping mapping = data.getData().getMapping();
        List<Node> nodes =  mapping.getNodes();
        for (Node node : nodes) {
            Node model = new Node();
            model.setFont(node.getFont());
            model.setId(node.getId());
            model.setName(node.getName());
            model.setKeypoint(node.getKeypoint());
            model.setShape(node.getShape());
            model.setType(node.getType());
            model.setX(node.getX());
            model.setY(node.getY());
            model.setOrder(AtlasUtil.getOrderInNodes(mapping,node.getId()) + 1);
            model.setFloor(node.getFloor());
            map.put(node.getId(),model);
        }
        return map;
    }
}
