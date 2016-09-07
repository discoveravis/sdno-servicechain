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
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPath;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.servicechain.model.NetServiceChainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.sf.json.JSONObject;

/**
 * NetServiceChainDataDbOper class.<br>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Sep 1, 2016
 */
public class NetServiceChainDataDbOper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetServiceChainDataDbOper.class);

    private static final String NBI_SERVICE_CHAIN_ID = "nbiServiceChainId";

    private static final String ACTION_STATE = "actionState";

    /**
     * Constructor.<br>
     * 
     * @since SDNO 0.5
     */
    private NetServiceChainDataDbOper() {

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
        ResultRsp<List<NetServiceChainData>> queryDbRsp = queryByFilter(serviceChainId, NBI_SERVICE_CHAIN_ID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            return false;
        }

        return true;
    }

    /**
     * It is used to insert the original data. <br>
     * 
     * @param netServiceChainPath The original data
     * @param nbiServiceChainId NBI ServiceChainId
     * @throws ServiceException When insert failed.
     * @since SDNO 0.5
     */
    public static void insert(NetServiceChainPath netServiceChainPath, String nbiServiceChainId)
            throws ServiceException {
        NetServiceChainData dbData = new NetServiceChainData();

        dbData.setUuid(netServiceChainPath.getUuid());
        dbData.setNbiServiceChainId(nbiServiceChainId);
        dbData.setActionState(ActionStatus.CREATING.getName());

        String requestJsonString = JsonUtil.toJson(netServiceChainPath);
        dbData.setData(requestJsonString);

        new InventoryDaoUtil<NetServiceChainData>().getInventoryDao().insert(dbData);
    }

    /**
     * It is used to update status. <br>
     * 
     * @param nbiServiceChainId The NBI serviceChain id
     * @param actionState The status
     * @throws ServiceException When update failed.
     * @since SDNO 0.5
     */
    public static void update(String nbiServiceChainId, String actionState) throws ServiceException {
        ResultRsp<List<NetServiceChainData>> queryDbRsp = queryByFilter(nbiServiceChainId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            String errMsg = "update error, serviceChainId (" + nbiServiceChainId + ") is not found";
            LOGGER.error(errMsg);
            ThrowException.throwResNotExist(errMsg);
        }

        NetServiceChainData dbData = queryDbRsp.getData().get(0);
        dbData.setActionState(actionState);

        new InventoryDaoUtil<NetServiceChainData>().getInventoryDao().update(dbData, ACTION_STATE);
    }

    /**
     * It is used to query the original data. <br>
     * 
     * @param nbiServiceChainId serviceChain uuid
     * @return The object of OverlayVpn
     * @throws ServiceException When query failed
     * @since SDNO 0.5
     */
    public static NetServiceChainPath query(String nbiServiceChainId) throws ServiceException {
        ResultRsp<List<NetServiceChainData>> queryDbRsp = queryByFilter(nbiServiceChainId, null);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("query error, nbiServiceChainId (" + nbiServiceChainId + ") is not found");
            return null;
        }

        NetServiceChainData dbData = queryDbRsp.getData().get(0);

        return JsonUtil.fromJson(dbData.getData(), NetServiceChainPath.class);
    }

    /**
     * It is used to delete the original data. <br>
     * 
     * @param nbiServiceChainId NBI serviceChainId uuid
     * @throws ServiceException When delete failed.
     * @since SDNO 0.5
     */
    public static void delete(String nbiServiceChainId) throws ServiceException {
        ResultRsp<List<NetServiceChainData>> queryDbRsp = queryByFilter(nbiServiceChainId, NBI_SERVICE_CHAIN_ID);
        if(CollectionUtils.isEmpty(queryDbRsp.getData())) {
            LOGGER.warn("delete error, serviceChainId (" + nbiServiceChainId + ") is not found");
            return;
        }

        NetServiceChainData reqModelInfo = queryDbRsp.getData().get(0);
        new InventoryDaoUtil<NetServiceChainData>().getInventoryDao().delete(NetServiceChainData.class,
                reqModelInfo.getUuid());
    }

    private static ResultRsp<List<NetServiceChainData>> queryByFilter(String serviceChainId, String queryResultFields)
            throws ServiceException {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        if(StringUtils.hasLength(serviceChainId)) {
            filterMap.put(NBI_SERVICE_CHAIN_ID, Arrays.asList(serviceChainId));
        }

        String filter = JSONObject.fromObject(filterMap).toString();

        return new InventoryDaoUtil<NetServiceChainData>().getInventoryDao().queryByFilter(NetServiceChainData.class,
                filter, queryResultFields);
    }
}
