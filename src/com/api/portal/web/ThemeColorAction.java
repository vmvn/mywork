package com.api.portal.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @Comment :
 * @author : qingsg
 * @date : 2017年3月10日
 * @version 1.0
 */
@Path("/portal/themecolor")
public class ThemeColorAction {
	@POST
	@Path("/themecolor")
	@Produces(MediaType.TEXT_PLAIN)
	public String getThemeColorJson(@Context HttpServletRequest request, @Context HttpServletResponse response) throws Exception {
		return "{}";
	}
}
