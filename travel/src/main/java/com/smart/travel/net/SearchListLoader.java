package com.smart.travel.net;

import android.content.Context;

import com.smart.travel.helper.DocHelper;
import com.smart.travel.model.SearchItem;
import com.smart.travel.utils.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/9/8.
 */
public class SearchListLoader {

    private static final String SEARCH_LIST_CACHE_FILE = "radar_search_list.xml";

    public static List<SearchItem> load(Context context) throws Exception {
        HttpRequest request = new HttpRequest();
        try {
            String text = request.doGet("http://121.40.165.84/travel/NodesList.plist");
            FileUtils.writeFile(context, SEARCH_LIST_CACHE_FILE, text.getBytes("utf-8"));

            return parse(text);
        } catch (Exception e) {
            if (!FileUtils.fileExists(context, SEARCH_LIST_CACHE_FILE)) {
                throw e;
            }
            String text = FileUtils.readFile(context, SEARCH_LIST_CACHE_FILE);
            return parse(text);
        }
    }

    private static List<SearchItem> parse(String text) throws Exception {
        List<SearchItem> searchItems = new ArrayList<>();

        Document doc = DocHelper.getDocument(text, true);
        Element root = doc.getDocumentElement();
        Element elArray1 = DocHelper.getElementByTagName(root, "array");
        List<Element> elArrays = DocHelper.getElementsByTagName(elArray1, "array");

        for (int i = 0; i < elArrays.size(); i++) {
            SearchItem searchItem = new SearchItem();

            if (i == 0) {
                searchItem.setClassify("出发地");
            } else if (i == 1) {
                searchItem.setClassify("节假日");
            } else if (i == 2) {
                searchItem.setClassify("热门旅游城市");
            } else if (i == 3) {
                searchItem.setClassify("旅游主题");
            }

            List<Element> elDicts = DocHelper.getElementsByTagName(elArrays.get(i), "dict");
            for (Element elDict : elDicts) {
                List<Element> elStrings = DocHelper.getElementsByTagName(elDict, "string");
                String keyword = elStrings.get(0).getTextContent().trim();
                String title = elStrings.get(1).getTextContent().trim();
                keyword = keyword.substring(keyword.indexOf(':') + 1);
                searchItem.addItem(title, keyword);
            }

            searchItems.add(searchItem);
        }

        return searchItems;
    }

}
