package com.smart.travel.net;

import android.util.Log;

import com.smart.travel.model.RadarItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/31.
 */
public class SearchLoader {

    private static final String TAG = "SearchLoader";

    public static List<RadarItem> load(int page, String keyword) throws Exception {
        HttpRequest request = new HttpRequest();
        String jsonString = null;
        if ("全部".equals(keyword)) {
            jsonString = request.doGet("http://121.40.165.84/travel/Index/get_travel_v2?p="
                    + page);
            Log.d(TAG, "Search URL: http://121.40.165.84/travel/Index/get_travel_v2?p="
                    + page);
        } else {
            jsonString = request.doGet("http://121.40.165.84/travel/Index/get_travel_v2?p="
                    + page + "&tab=keyword:" + URLEncoder.encode(keyword, "utf-8"));
            Log.d(TAG, "Search URL: http://121.40.165.84/travel/Index/get_travel_v2?p="
                    + page + "&tab=keyword:" + keyword);
        }

        List<RadarItem> listItems = new ArrayList<>();

        JSONObject parentObj = new JSONObject(jsonString);
        JSONObject variablesObj = parentObj.getJSONObject("Variables");

        if (variablesObj.getString("data").trim().equals("null")) {
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

            RadarItem item = new RadarItem();
            item.setId(id);
            item.setTitle(title);
            item.setPubdate(pubdate);
            item.setImage(image);
            item.setUrl(url);
            item.setType(type);
            item.setAuthor(author);
            item.setShowType(showType);

            listItems.add(item);
        }

        return listItems;
    }

}
