package com.babyfs.tk.galaxy.controller;

import com.babyfs.tk.galaxy.server.IRpcService;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.base.annotation.InternalAccess;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 内部rpc接口,供提供rpc服务的server引用
 */
@Controller
@RequestMapping(value = "/internal/rpc")
public class RPCController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCController.class);

    @Inject
    private IRpcService rpcService;

    @RequestMapping(value = "/invoke", method = RequestMethod.POST)
    @InternalAccess
    public void invokeRpcMethod(final HttpServletRequest request, final HttpServletResponse response) {
        int size = request.getContentLength();
        try(ServletInputStream sis = request.getInputStream()) {
            byte[] buffer = new byte[size];
            sis.read(buffer, 0, size);
            ResponseUtil.writeByteResult(response, rpcService.invoke(buffer));
        } catch (IOException e) {
            LOGGER.error("rpc server error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "rpc server error");
        }
    }


    protected void sendError(HttpServletResponse response, int status, String error) {
        try {
            response.sendError(status, error);
        } catch (IOException e) {
            //ignore it
        }
    }

}
