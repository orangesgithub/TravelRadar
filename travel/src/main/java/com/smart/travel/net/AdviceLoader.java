package com.smart.travel.net;

import com.smart.travel.model.RadarItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/31.
 */
public class AdviceLoader {

    public static List<RadarItem> load(int page) throws Exception {
        HttpRequest request = new HttpRequest();
        String jsonString = request.doGet("http://121.40.165.84/travel//Index/get_travel_v2?p=" + page + "&tab=type:" + URLEncoder.encode("锦囊", "utf-8"));

        List<RadarItem> listItems = new ArrayList<>();

        JSONObject parentObj = new JSONObject(jsonString);
        JSONObject variablesObj = parentObj.getJSONObject("Variables");

        if (!variablesObj.has("data")) {
            return listItems;
        }

        JSONArray jsonArray = variablesObj.getJSONArray("data");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject dataObject = jsonArray.getJSONObject(i);
            int id = dataObject.getInt("id");
            String title = dataObject.getString("title");
            String pubdate = dataObject.getString("pubdate");
            String image = dataObject.getString("image");
            String url = dataObject.getString("url");
            String type = dataObject.getString("type");
            String author = dataObject.getString("author");
            int showType = dataObject.getInt("show_type");

            RadarItem finderItem = new RadarItem();
            finderItem.setId(i);
            finderItem.setTitle(title);
            finderItem.setPubdate(pubdate);
            finderItem.setImage(image);
            finderItem.setUrl(url);
            finderItem.setType(type);
            finderItem.setAuthor(author);
            finderItem.setShowType(showType);

            listItems.add(finderItem);
        }

        return listItems;
    }

}