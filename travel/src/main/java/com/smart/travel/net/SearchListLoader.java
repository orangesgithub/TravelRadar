package com.smart.travel.net;

import com.smart.travel.helper.DocHelper;
import com.smart.travel.model.SearchItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfan10x on 2015/9/8.
 */
public class SearchListLoader {

    public static List<SearchItem> load() throws Exception {
        HttpRequest request = new HttpRequest();
        String text = text = request.doGet("http://121.40.165.84/travel/NodesList.plist");
        return parse(text);
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
