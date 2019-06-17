package com.datable.excelbox.core.util;

import com.datable.excelbox.core.model.MonarchsOf3KingdomsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来模拟测试数据的工具类
 *
 * @author jinyaoo
 */
public final class MockDataUtil {

    /**
     * 所有一个对象列表，对象所有属性成员变量均为字符串
     *
     * @param size 对象数量，即生成excel的行数
     * @return 对象列表或空列表
     */
    public static List<MonarchsOf3KingdomsModel> mockAllStringPropObjectList(int size) {
        List<MonarchsOf3KingdomsModel> objList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            MonarchsOf3KingdomsModel obj = new MonarchsOf3KingdomsModel("name" + i, "career" + i, "title" + i, "note" + i);
            objList.add(obj);
        }
        return objList;
    }


}
