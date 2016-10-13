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

package org.openo.sdno.servicechain.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.inventory.sdk.util.InventoryDaoUtil;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPath;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.util.objreflectoper.UuidAllocUtil;
import org.openo.sdno.servicechain.model.ServiceChainReqModelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * ServiceChainReqDbOper class.<br>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 August 22, 2016
 */
public class ServiceChainReqDbOper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceChainReqDbOper.class);

    private static final String SERVICE_CHAIN_ID = "serviceChainId";

    private static final String ACTION_STATE = "actionState";

    /**
     * Constructor.<br>
     * 
     * @since SDNO 0.5
     */
    private ServiceChainReqDbOper() {

    }

    /**
     * It is used to check the special record is existed or not. <br>
     * 
     * @param serviceChainId The serviceChainId field in ServiceChainReqModelInfo
     * @return true if the record is existed
     * @throws ServiceException When check failed.
     * @since SDNO 0.5
     */
    public static boolean checkRecordIsExisted(String serviceChainId) throws ServiceException {
        ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = queryByFilter(serviceChainId, SERVICE_CHAIN_ID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            return false;
        }

        return true;
    }

    /**
     * It is used to insert the original data. <br>
     * 
     * @param serviceChainPath The original data
     * @throws ServiceException When insert failed.
     * @since SDNO 0.5
     */
    public static void insert(ServiceChainPath serviceChainPath) throws ServiceException {
        ServiceChainReqModelInfo reqModelInfo = new ServiceChainReqModelInfo();

        UuidAllocUtil.allocUuid(reqModelInfo);

        reqModelInfo.setServiceChainId(serviceChainPath.getUuid());
        reqModelInfo.setActionState(ActionStatus.CREATING.getName());

        String requestJsonString = JsonUtil.toJson(serviceChainPath);
        reqModelInfo.setData(requestJsonString);

        new InventoryDaoUtil<ServiceChainReqModelInfo>().getInventoryDao().insert(reqModelInfo);
    }

    /**
     * It is used to update status. <br>
     * 
     * @param serviceChainId The serviceChain id
     * @param actionState The status
     * @throws ServiceException When update failed.
     * @since SDNO 0.5
     */
    public static void update(String serviceChainId, String actionState) throws ServiceException {
        ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = queryByFilter(serviceChainId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            String errMsg = "update error, serviceChainId (" + serviceChainId + ") is not found";
            LOGGER.error(errMsg);
            ThrowException.throwResNotExist(errMsg);
        }

        ServiceChainReqModelInfo reqModelInfo = queryDbRsp.getData().get(0);
        reqModelInfo.setActionState(actionState);

        new InventoryDaoUtil<ServiceChainReqModelInfo>().getInventoryDao().update(reqModelInfo, ACTION_STATE);
    }

    /**
     * It is used to query the original data. <br>
     * 
     * @param serviceChainId serviceChain UUID
     * @return The object of OverlayVpn
     * @throws ServiceException When query failed
     * @since SDNO 0.5
     */
    public static ServiceChainPath query(String serviceChainId) throws ServiceException {
        ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = queryByFilter(serviceChainId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("query error, serviceChainId (" + serviceChainId + ") is not found");
            return null;
        }

        ServiceChainReqModelInfo reqModelInfo = queryDbRsp.getData().get(0);

        return JsonUtil.fromJson(reqModelInfo.getData(), ServiceChainPath.class);
    }

    /**
     * It is used to delete the original data. <br>
     * 
     * @param serviceChainId serviceChainId UUID
     * @throws ServiceException When delete failed.
     * @since SDNO 0.5
     */
    public static void delete(String serviceChainId) throws ServiceException {
        ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = queryByFilter(serviceChainId, SERVICE_CHAIN_ID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("delete error, serviceChainId (" + serviceChainId + ") is not found");
            return;
        }

        ServiceChainReqModelInfo reqModelInfo = queryDbRsp.getData().get(0);
        new InventoryDaoUtil<ServiceChainReqModelInfo>().getInventoryDao().delete(ServiceChainReqModelInfo.class,
                reqModelInfo.getUuid());
    }

    private static ResultRsp<List<ServiceChainReqModelInfo>> queryByFilter(String serviceChainId,
            String queryResultFields) throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        if(StringUtils.hasLength(serviceChainId)) {
            filterMap.put(SERVICE_CHAIN_ID, Arrays.asList(serviceChainId));
        }

        String filter = JSONObject.fromObject(filterMap).toString();

        return new InventoryDaoUtil<ServiceChainReqModelInfo>().getInventoryDao()
                .queryByFilter(ServiceChainReqModelInfo.class, filter, queryResultFields);
    }
}
