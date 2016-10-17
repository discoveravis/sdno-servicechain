/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.servicechain.rest;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.util.RestUtils;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.consts.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPath;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPathRsp;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.check.CheckStrUtil;
import org.openo.sdno.servicechain.service.inf.ServiceChainService;
import org.openo.sdno.servicechain.util.ServiceChainReqDbOper;
import org.openo.sdno.servicechain.util.ThrowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The rest interface of ServiceChain. <br>
 * 
 * @author
 * @version SDNO 0.5 June 16, 2016
 */
@Service
@Path("/sdnoservicechain/v1/paths")
public class ServiceChainSvcRoaResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceChainSvcRoaResource.class);

    private static final String SERVICE_CHAIN_PATH_KEY = "serviceChainPath";

    @Resource
    private ServiceChainService serviceChainService;

    public ServiceChainService getServiceChainService() {
        return serviceChainService;
    }

    public void setServiceChainService(ServiceChainService serviceChainService) {
        this.serviceChainService = serviceChainService;
    }

    /**
     * Rest interface to perform create ServiceFunctionPath operation. <br>
     * 
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @return ResultRsp
     * @throws ServiceException When create failed
     * @since SDNO 0.5
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<ServiceChainPathRsp> create(@Context HttpServletRequest req, @Context HttpServletResponse resp)
            throws ServiceException {
        long infterEnterTime = System.currentTimeMillis();

        // get request data
        String requestBody = RestUtils.getRequestBody(req);
        if(StringUtils.isEmpty(requestBody)) {
            LOGGER.error("Request body is null");
            throw new ServiceException("Request body is null");
        }

        Map<String, ServiceChainPath> serviceChainPathMap =
                JsonUtil.fromJson(requestBody, new TypeReference<Map<String, ServiceChainPath>>() {});
        if(null == serviceChainPathMap || null == serviceChainPathMap.get(SERVICE_CHAIN_PATH_KEY)) {
            LOGGER.error("No service chain path data in request");
            throw new ServiceException("No service chain path data in request");
        }

        ServiceChainPath serviceChainPath = serviceChainPathMap.get(SERVICE_CHAIN_PATH_KEY);

        String sfpUuid = serviceChainPath.getUuid();

        CheckStrUtil.checkUuidStr(sfpUuid);

        if(ServiceChainReqDbOper.checkRecordIsExisted(sfpUuid)) {
            ThrowException.throwUuidIsExisted(sfpUuid);
        }

        // save the request data
        ServiceChainReqDbOper.insert(serviceChainPath);
        // update actionState to exception firstly
        ServiceChainReqDbOper.update(sfpUuid, ActionStatus.CREATE_EXCEPTION.getName());

        // call the service method to perform create operation
        ResultRsp<ServiceChainPathRsp> resultRsp = serviceChainService.create(req, resp, serviceChainPath);

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // update actionState to normal
        ServiceChainReqDbOper.update(sfpUuid, ActionStatus.NORMAL.getName());

        // well all-is-well, set the response status as success and return result
        resp.setStatus(HttpCode.CREATE_OK);

        LOGGER.info("Exit create method. cost time = " + (System.currentTimeMillis() - infterEnterTime));

        return resultRsp;

    }

    /**
     * Rest interface to perform delete ServiceFunctionPath operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param uuid The uuid of ServiceFunctionPath
     * @return The object of ResultRsp
     * @throws ServiceException When delete ServiceFunctionPath failed
     * @since SDNO 0.5
     */
    @DELETE
    @Path("/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResultRsp<ServiceChainPathRsp> delete(@Context HttpServletRequest req, @Context HttpServletResponse resp,
            @PathParam("uuid") String uuid) throws ServiceException {

        long infterEnterTime = System.currentTimeMillis();

        // query data
        ServiceChainPath sfp = ServiceChainReqDbOper.query(uuid);
        if(null == sfp) {
            LOGGER.info("Exit delete method. cost time = " + (System.currentTimeMillis() - infterEnterTime));
            return new ResultRsp<ServiceChainPathRsp>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        // update actionState to exception firstly
        ServiceChainReqDbOper.update(uuid, ActionStatus.DELETE_EXCEPTION.getName());

        // call the service method to perform delete operation
        ResultRsp<ServiceChainPathRsp> resultRsp = serviceChainService.delete(req, resp, uuid);

        // check the response for error code and throw an exception in case of failure
        ThrowException.checkRspThrowException(resultRsp);

        // delete data
        ServiceChainReqDbOper.delete(uuid);

        LOGGER.info("Exit delete method. cost time = " + (System.currentTimeMillis() - infterEnterTime));

        return resultRsp;
    }
}
