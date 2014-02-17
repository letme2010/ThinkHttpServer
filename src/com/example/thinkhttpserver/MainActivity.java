
package com.example.thinkhttpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.thinkhttpserver.LtHTTPD.IDelegate;

public class MainActivity extends Activity implements IDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LtHTTPD httpd = new LtHTTPD(this);
        try {
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public String getResourceTextByName(String aResName) {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream resText;
            resText = getAssets().open(aResName);
            BufferedReader in = new BufferedReader(new InputStreamReader(resText));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();

            return buf.toString();
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getNotFoundResPageText() {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream resText;
            resText = getAssets().open("404.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(resText));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();

            return buf.toString();
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Bitmap getTextImage() {
        return BitmapFactory.decodeResource(this.getResources(), R.drawable.gundam);
    }

    private class Node {
        private List<Node> mChildren = new ArrayList<MainActivity.Node>();

        private Map<String, Object> mAttrMap = new HashMap<String, Object>();

        public List<Node> getChildren() {
            return mChildren;
        }

        public Map<String, Object> getAttrMap() {
            return mAttrMap;
        }

        public String toJSONString() {
            try {

                JSONObject retJSON = new JSONObject();

                Set<String> attrKeys = mAttrMap.keySet();
                for (String attrKey : attrKeys) {
                    retJSON.put(attrKey, mAttrMap.get(attrKey));
                }

                JSONArray childArray = new JSONArray();
                for (Node node : this.mChildren) {
                    childArray.put(new JSONObject(node.toJSONString()));
                }
                retJSON.put("children", childArray);

                return retJSON.toString();

            } catch (Exception e) {
                return null;
            }
        }

    }

    private Node view2Node(View aView) {
        if (null == aView) {
            return null;
        }

        Node ret = new Node();
        ret.getAttrMap().put("left", aView.getLeft());
        ret.getAttrMap().put("right", aView.getRight());
        ret.getAttrMap().put("top", aView.getTop());
        ret.getAttrMap().put("bottom", aView.getBottom());
        ret.getAttrMap().put("class", aView.getClass().getSimpleName());

        if (aView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)aView;
            List<Node> children = ret.getChildren();
            for (int i = 0, len = viewGroup.getChildCount(); i < len; i++) {
                children.add(view2Node(viewGroup.getChildAt(i)));
            }
        }

        return ret;
    }

    @Override
    public String getViewHieracryJSON() {
        Window window = this.getWindow();
        View decorView = window.getDecorView();
        return this.view2Node(decorView).toJSONString();
    }
}
