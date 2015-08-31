package com.smart.travel.net;

import com.smart.travel.model.TravelItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/31.
 */
public class AdviceLoader {

    public static List<TravelItem> load(int page) throws Exception {
        HttpRequest request = new HttpRequest();
        String jsonString = request.doGet("http://121.40.165.84/travel//Index/get_travel_v2?p=" + page + "&tab=type:" + URLEncoder.encode("锦囊", "utf-8"));

        List<TravelItem> listItems = new ArrayList<>();

        JSONObject parentObj = new JSONObject(jsonString);
        JSONObject variablesObj = parentObj.getJSONObject("Variables");
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

            TravelItem travelItem = new TravelItem();
            travelItem.setId(i);
            travelItem.setTitle(title);
            travelItem.setPubdate(pubdate);
            travelItem.setImage(image);
            travelItem.setUrl(url);
            travelItem.setType(type);
            travelItem.setAuthor(author);
            travelItem.setShowType(showType);

            listItems.add(travelItem);
        }

        return listItems;
    }

}
