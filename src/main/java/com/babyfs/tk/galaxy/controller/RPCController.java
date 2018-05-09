package com.babyfs.tk.galaxy.controller;

import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.galaxy.server.IServer;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.base.annotation.InternalAccess;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 内部rpc接口,供提供rpc服务的server引用
 */
@Controller
@RequestMapping(value = "/internal/rpc")
public class RPCController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCController.class);

    @Inject
    private IServer rpcService;

    @RequestMapping(value = "/invoke", method = RequestMethod.POST)
    @InternalAccess
    public void invokeRpcMethod(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            ServiceResponse<byte[]> handle = rpcService.handle(getBody(request));
            if (handle.isFailure()) {
                sendError(response, handle.getCode(), handle.getMsg());
            } else {
                byte[] data = handle.getData();
                ResponseUtil.writeByteResult(response, data);
            }
        } catch (Exception e) {
            LOGGER.error("rpc server error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "rpc server error,excepiton:" + e.getMessage());
        }
    }


    protected void sendError(HttpServletResponse response, int status, String error) {
        try {
            response.sendError(status, error);
        } catch (IOException e) {
            //ignore it
        }
    }

    private static byte[] getBody(HttpServletRequest request) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(request.getContentLength());) {
            ByteStreams.copy(request.getInputStream(), outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
}
