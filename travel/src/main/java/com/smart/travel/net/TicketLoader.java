package com.smart.travel.net;

import android.content.Context;

import com.smart.travel.RadarFragment;
import com.smart.travel.model.RadarItem;
import com.smart.travel.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/8/31.
 */
public class TicketLoader {

    public static List<RadarItem> load(Context context, int page) throws Exception {
        HttpRequest request = new HttpRequest();
        String jsonString = request.doGet("http://121.40.165.84/travel/Index/get_travel_v2?p=" + page + "&tab=type:" + URLEncoder.encode("特价优惠", "utf-8"));

        if (page == 1) {
            FileUtils.writeFile(context, RadarFragment.RADAR_LISTVIEW_HISTORY_FILE, jsonString.getBytes("utf-8"));
        }

        return parse(jsonString);
    }

    public static List<RadarItem> parse(String jsonString) throws Exception {
        List<RadarItem> listItems = new ArrayList<>(32);

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
            String tag = dataObject.getString("tag");
            int showType = dataObject.getInt("show_type");

            RadarItem radarItem = new RadarItem();
            radarItem.setId(id);
            radarItem.setTitle(title);
            radarItem.setPubdate(pubdate);
            radarItem.setImage(image);
            radarItem.setUrl(url);
            radarItem.setType(type);
            radarItem.setAuthor(author);
            radarItem.setTag(tag);
            radarItem.setShowType(showType);

            listItems.add(radarItem);
        }

        return listItems;
    }

}
