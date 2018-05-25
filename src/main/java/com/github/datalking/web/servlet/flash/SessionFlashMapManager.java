package com.github.datalking.web.servlet.flash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author yaoo on 4/28/18
 */
public class SessionFlashMapManager extends AbstractFlashMapManager {

    private static final String FLASH_MAPS_SESSION_ATTRIBUTE = SessionFlashMapManager.class.getName() + ".FLASH_MAPS";

    @Override
    protected List<FlashMap> retrieveFlashMaps(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null ? (List<FlashMap>) session.getAttribute(FLASH_MAPS_SESSION_ATTRIBUTE) : null);
    }

    @Override
    protected void updateFlashMaps(List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response) {
        request.getSession().setAttribute(FLASH_MAPS_SESSION_ATTRIBUTE, flashMaps);
    }

}
