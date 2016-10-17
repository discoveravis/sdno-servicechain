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

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.type.TypeReference;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
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
 * 
 * @author
 * @version SDNO 0.5 August 24, 2016
 */
public class ServiceChainSbiImpl implements ServiceChainSbiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceChainSbiImpl.class);

    public static final String SERVICECHAIN_ADAPTER_BASE_URL = "/openoapi/sbi-servicechain/v1/paths";

    private static final String SERVICE_CHAIN_PATH_KEY = "serviceChainPath";

    @Override
    public ResultRsp<NetServiceChainPathRsp> create(HttpServletRequest req, HttpServletResponse resp,
            NetServiceChainPath sfp) throws ServiceException {

        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader("Content-Type", "application/json;charset=UTF-8");
        restfulParametes.putHttpContextHeader("X-Driver-Parameter",
                "extSysID=" + getControllerIdByNe(sfp.getScfNeId()));

        Map<String, NetServiceChainPath> netServiceChainPathMap = new HashMap<String, NetServiceChainPath>();
        netServiceChainPathMap.put(SERVICE_CHAIN_PATH_KEY, sfp);

        restfulParametes.setRawData(JsonUtil.toJson(netServiceChainPathMap));

        RestfulResponse response = RestfulProxy.post(SERVICECHAIN_ADAPTER_BASE_URL, restfulParametes);

        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<NetServiceChainPathRsp>(ErrorCode.RESTFUL_COMMUNICATION_FAILED);
        }

        return getResultRspFromResponse(response, sfp);
    }

    @Override
    public ResultRsp<NetServiceChainPathRsp> delete(HttpServletRequest req, HttpServletResponse resp, String uuid,
            NetServiceChainPath sfp) throws ServiceException {

        RestfulParametes restfulParametes = new RestfulParametes();
        restfulParametes.putHttpContextHeader("Content-Type", "application/json;charset=UTF-8");
        restfulParametes.putHttpContextHeader("X-Driver-Parameter",
                "extSysID=" + getControllerIdByNe(sfp.getScfNeId()));

        String url = MessageFormat.format(SERVICECHAIN_ADAPTER_BASE_URL + "/{0}", uuid);

        RestfulResponse response = RestfulProxy.delete(url, restfulParametes);

        if(response.getStatus() == HttpCode.NOT_FOUND) {
            return new ResultRsp<NetServiceChainPathRsp>(ErrorCode.RESTFUL_COMMUNICATION_FAILED, null, null,
                    "Connect to os controller failed", "Connect to os controller failed, please check");
        }

        ResponseUtils.transferResponse(response);

        return new ResultRsp<NetServiceChainPathRsp>(ErrorCode.OVERLAYVPN_SUCCESS);
    }

    private ResultRsp<NetServiceChainPathRsp> getResultRspFromResponse(RestfulResponse response,
            NetServiceChainPath sfp) {
        try {
            String content = ResponseUtils.transferResponse(response);
            Map<String, NetServiceChainPathRsp> netServiceChainPathRspMap =
                    JsonUtil.fromJson(content, new TypeReference<Map<String, NetServiceChainPathRsp>>() {});

            ResultRsp<NetServiceChainPathRsp> resultObj = new ResultRsp<NetServiceChainPathRsp>();
            resultObj.setData(netServiceChainPathRspMap.get(SERVICE_CHAIN_PATH_KEY));
            LOGGER.info("ServiceFunctionPath. ServiceChainSvcImpl operation finish, result = " + resultObj.toString());

            return resultObj;
        } catch(ServiceException e) {
            LOGGER.error("ServiceFunctionPath exception information: ", e);
            return new ResultRsp<NetServiceChainPathRsp>(e.getId(), e.getExceptionArgs());
        }
    }

    private String getControllerIdByNe(String neId) throws ServiceException {

        NetworkElementInvDao neInvDao = new NetworkElementInvDao();
        NetworkElementMO curNetworkElement = neInvDao.query(neId);

        if(null == curNetworkElement || CollectionUtils.isEmpty(curNetworkElement.getControllerID())) {
            LOGGER.error("Cur NetworkElement does not exist or does not has controller");
            throw new ServiceException("Cur NetworkElement does not exist or does not has controller");
        }

        return curNetworkElement.getControllerID().get(0);
    }
}
