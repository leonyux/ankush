/*******************************************************************************
 * ===========================================================
 * Ankush : Big Data Cluster Management Solution
 * ===========================================================
 * 
 * (C) Copyright 2014, by Impetus Technologies
 * 
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (LGPL v3) as
 * published by the Free Software Foundation;
 * 
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this software; if not, write to the Free Software Foundation, 
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.impetus.ankush.common.controller.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.impetus.ankush.common.exception.ControllerException;
import com.impetus.ankush.common.mail.MailConf;
import com.impetus.ankush.common.service.AppConfService;
import com.impetus.ankush.common.service.impl.AnkushApplicationConf;
import com.impetus.ankush.common.utils.ResponseWrapper;

/**
 * The Class AppConfController.
 */
@Controller
@RequestMapping("/app")
public class AppConfController extends BaseController {

	/** The app conf service. */
	private AppConfService appConfService;

	/**
	 * Sets the config service.
	 *
	 * @param appConfService the new config service
	 */
	@Autowired
	public void setConfigService(
			@Qualifier("appConfService") AppConfService appConfService) {
		this.appConfService = appConfService;
	}

	/**
	 * Gets the ankush app conf.
	 *
	 * @return the ankush app conf
	 */
	@RequestMapping(value = { "/conf/request", "/conf" }, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ResponseWrapper<Map>> getAnkushAppConf(HttpServletRequest request) {
		boolean unauthenticated = request.getRequestURI().endsWith(
				"/conf/request");
		return wrapResponse(appConfService.getCommonConfiguration(unauthenticated),
				HttpStatus.OK, HttpStatus.OK.toString(),
				"Get application configuration.");
	}

	/**
	 * Sets the ankush app conf.
	 *
	 * @param ankushConfig the ankush config
	 * @param request the request
	 * @return the response entity
	 */
	@RequestMapping(value = { "/conf/request", "/conf" }, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ResponseWrapper<Map>> setAnkushAppConf(
			@RequestBody AnkushApplicationConf ankushConfig,
			HttpServletRequest request) {
		boolean unauthenticated = request.getRequestURI().endsWith(
				"/conf/request");

		if (!unauthenticated) {
			// Getting Logged User Info
			try {
				String userName = ((com.impetus.ankush.common.domain.User) SecurityContextHolder
						.getContext().getAuthentication().getPrincipal())
						.getUsername();
				ankushConfig.setLoggedUser(userManager
						.getUserByUsername(userName));
			} catch (Exception e) {
				log.error("Error in getting logged user Info : "
						+ e.getMessage());
			}
		}
		Map result = appConfService.manageCommonConfiguration(ankushConfig,unauthenticated);
		return wrapResponse(result, HttpStatus.OK, HttpStatus.OK.toString(),
				"Set application configuration.");
	}
	
	@RequestMapping(value = { "/metadata/{file}" }, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ResponseWrapper<Map>> getMetadata(
			@PathVariable("file") String file) {
		Map result = appConfService.getMetadata(file);
		String error = (String) result.get("error");
		if (error != null) {
			throw new ControllerException(HttpStatus.INTERNAL_SERVER_ERROR,
					HttpStatus.INTERNAL_SERVER_ERROR.toString(), error);
		}
		return wrapResponse(result, HttpStatus.OK, HttpStatus.OK.toString(),
				"Get application configuration.");
	}

	/**
	 * Tests Mail conf.
	 *
	 * @return Mail send status
	 */
	@RequestMapping(value = { "/conf/testMailConf/{to:.+}"}, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ResponseWrapper<Map>> testMailConf(HttpServletRequest request,
			@RequestBody MailConf mailConf, @PathVariable String to) {
		return wrapResponse(appConfService.testMailConf(mailConf, to),
				HttpStatus.OK, HttpStatus.OK.toString(),
				"Mail configuration test status");
	}
	
	/**
	 * Tests Mail conf.
	 *
	 * @return Mail send status
	 */
	@RequestMapping(value = { "/conf/mailConfVerified/{to:.+}"}, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ResponseWrapper<Map>> testMailConf(HttpServletRequest request,
			@PathVariable String to) {
		return wrapResponse(appConfService.updateMailConfVerificationStatus(to),
				HttpStatus.OK, HttpStatus.OK.toString(),
				"Mail configuration verification status updated");
	}
}
