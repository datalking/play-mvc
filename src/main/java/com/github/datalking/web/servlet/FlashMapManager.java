package com.github.datalking.web.servlet.flash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/28/18
 */
public interface FlashMapManager {

    FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response);

    void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response);

}
