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

package org.openo.sdno.servicechain.sbi.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulOptions;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPath;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPathRsp;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.rest.ResponseUtils;
import org.openo.sdno.servicechain.sbi.inf.ServiceChainSbiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceChainSbiSvc class.<br>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Aug 24, 2016
 */
public class ServiceChainSbiImpl implements ServiceChainSbiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceChainSbiImpl.class);

    public static final String SERVICECHAIN_ADAPTER_BASE_URL = "/openoapi/sbi-servicechain/v1/paths";

    private static final String SERVICE_CHAIN_PATH_KEY = "serviceChainPath";

    private static final int TIMEOUT = 300;

    @Override
    public ResultRsp<NetServiceChainPathRsp> create(HttpServletRequest req, HttpServletResponse resp,
            NetServiceChainPath sfp) throws ServiceException {

        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader("Content-Type", "application/json;charset=UTF-8");

        Map<String, NetServiceChainPath> netServiceChainPathMap = new HashMap<String, NetServiceChainPath>();
        netServiceChainPathMap.put(SERVICE_CHAIN_PATH_KEY, sfp);

        restfulParametes.setRawData(JsonUtil.toJson(netServiceChainPathMap));

        RestfulOptions restOptions = new RestfulOptions();
        restOptions.setRestTimeout(TIMEOUT);
        RestfulResponse response = RestfulProxy.post(SERVICECHAIN_ADAPTER_BASE_URL, restfulParametes);

        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<NetServiceChainPathRsp>(ErrorCode.RESTFUL_COMMUNICATION_FAILED);
        }

        return getResultRspFromResponse(response, sfp);
    }

    @Override
    public ResultRsp<NetServiceChainPathRsp> delete(HttpServletRequest req, HttpServletResponse resp, String uuid)
            throws ServiceException {

        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader("Content-Type", "application/json;charset=UTF-8");

        String url = MessageFormat.format(SERVICECHAIN_ADAPTER_BASE_URL + "/{0}", uuid);

        RestfulOptions restOptions = new RestfulOptions();
        restOptions.setRestTimeout(TIMEOUT);
        RestfulResponse response = RestfulProxy.delete(url, restfulParametes, restOptions);

        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<NetServiceChainPathRsp>(ErrorCode.RESTFUL_COMMUNICATION_FAILED, null, null,
                    "connect to os controller failed", "connect to os controller failed, please check");
        }

        String rspContent = ResponseUtils.transferResponse(response);

        return JsonUtil.fromJson(rspContent, new TypeReference<ResultRsp<NetServiceChainPathRsp>>() {});
    }

    private ResultRsp<NetServiceChainPathRsp> getResultRspFromResponse(RestfulResponse response,
            NetServiceChainPath sfp) {
        try {
            String content = ResponseUtils.transferResponse(response);
            ResultRsp<NetServiceChainPathRsp> resultObj =
                    JsonUtil.fromJson(content, new TypeReference<ResultRsp<NetServiceChainPathRsp>>() {});
            LOGGER.info("ServiceFunctionPath. ServiceChainSvcImpl operation finish, result = " + resultObj.toString());

            return resultObj;
        } catch(ServiceException e) {
            LOGGER.error("ServiceFunctionPath except information: ", e);
            return new ResultRsp<NetServiceChainPathRsp>(e.getId(), e.getExceptionArgs());
        }
    }
}
