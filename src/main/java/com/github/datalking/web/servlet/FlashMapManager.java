package com.github.datalking.web.servlet;

import com.github.datalking.web.servlet.flash.FlashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 定义了保存FlashMap和获取FlashMap的方法
 *
 * @author yaoo on 4/28/18
 */
public interface FlashMapManager {

    FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response);

    void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response);

}
