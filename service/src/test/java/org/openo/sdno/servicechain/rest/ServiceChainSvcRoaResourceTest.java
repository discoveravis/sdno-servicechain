/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.baseservice.roa.util.restclient.RestfulParametes;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.baseservice.util.RestUtils;
import org.openo.sdno.framework.container.resthelper.RestfulProxy;
import org.openo.sdno.framework.container.util.JsonUtil;
import org.openo.sdno.overlayvpn.brs.invdao.NetworkElementInvDao;
import org.openo.sdno.overlayvpn.brs.model.NetworkElementMO;
import org.openo.sdno.overlayvpn.dao.common.InventoryDao;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPathRsp;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPath;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPathRsp;
import org.openo.sdno.overlayvpn.model.servicechain.ServicePathHop;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.servicechain.model.NetServiceChainData;
import org.openo.sdno.servicechain.model.ServiceChainReqModelInfo;
import org.openo.sdno.servicechain.util.ServiceChainReqDbOper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/applicationContext.xml",
                "classpath*:META-INF/spring/service.xml", "classpath*:spring/service.xml"})
public class ServiceChainSvcRoaResourceTest {

    @Mocked
    HttpServletRequest request;

    @Mocked
    HttpServletResponse response;

    @Autowired
    ServiceChainSvcRoaResource serviceChainSvc;

    @Before
    public void setUp() throws Exception {
        new MockInventoryDao();
        new MockRestfulProxy();
    }

    @Test
    public void testHealthCheckSuccess() throws ServiceException {

        try {
            serviceChainSvc.healthCheck(request, response);
            assertTrue(true);
        } catch(ServiceException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testQuery() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {

                List<ServiceChainReqModelInfo> serviceChainReqModelInfoList = new ArrayList<>();
                ServiceChainReqModelInfo serviceChainReqModelInfo = new ServiceChainReqModelInfo();
                serviceChainReqModelInfo.setUuid("serviceChainReqModelInfoUuid");
                ServiceChainPath serviceChainPath = new ServiceChainPath();
                serviceChainPath.setUuid("uuid");
                serviceChainReqModelInfo.setData(JsonUtil.toJson(serviceChainPath));
                serviceChainReqModelInfoList.add(serviceChainReqModelInfo);

                ResultRsp<List<ServiceChainReqModelInfo>> resp = new ResultRsp<List<ServiceChainReqModelInfo>>(
                        ErrorCode.OVERLAYVPN_SUCCESS, serviceChainReqModelInfoList);
                return resp;
            }

        };

        ServiceChainPath serviceChainPath = serviceChainSvc.query(request, response, "serviceChainReqModelInfoUuid");
        assertEquals(serviceChainPath.getUuid(), "uuid");
    }

    @Test
    public void testCreateSuccess() throws ServiceException {

        new MockUp<RestUtils>() {

            @Mock
            String getRequestBody(HttpServletRequest request) {
                ServiceChainPath serviceChainPath = buildServiceChainPath();

                Map<String, ServiceChainPath> serviceChainPathMap = new HashMap<>();
                serviceChainPathMap.put("serviceChainPath", serviceChainPath);

                return JsonUtil.toJson(serviceChainPathMap);
            }
        };

        new MockUp<ServiceChainReqDbOper>() {

            @Mock
            void update(String serviceChainId, String actionState) throws ServiceException {

            }
        };

        new MockNeDao();

        ResultRsp<ServiceChainPathRsp> resultRsp = serviceChainSvc.create(request, response);
        assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Test
    public void testDeleteSuccess() throws ServiceException {
        new MockUp<InventoryDao<T>>() {

            @Mock
            ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {
                if(ServiceChainReqModelInfo.class.equals(clazz)) {
                    ServiceChainReqModelInfo serviceChainReqModelInfo = new ServiceChainReqModelInfo();
                    serviceChainReqModelInfo.setData(JsonUtil.toJson(new ServiceChainReqModelInfo()));

                    ResultRsp<List<ServiceChainReqModelInfo>> resp = new ResultRsp<List<ServiceChainReqModelInfo>>(
                            ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(serviceChainReqModelInfo));
                    return resp;
                } else if(NetServiceChainData.class.equals(clazz)) {
                    NetServiceChainData netServiceChainData = new NetServiceChainData();
                    netServiceChainData.setData(JsonUtil.toJson(new NetServiceChainData()));

                    ResultRsp<List<NetServiceChainData>> resp = new ResultRsp<List<NetServiceChainData>>(
                            ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(netServiceChainData));
                    return resp;
                }

                return null;
            }

        };

        new MockNeDao();

        ResultRsp<ServiceChainPathRsp> resultRsp = serviceChainSvc.delete(request, response, "uuid");
        assertEquals(resultRsp.getErrorCode(), ErrorCode.OVERLAYVPN_SUCCESS);
    }

    @Test
    public void testDeleteDbDataNotExist() throws ServiceException {
        MockUp<ServiceChainReqDbOper> mock = new MockUp<ServiceChainReqDbOper>() {

            @Mock
            public ServiceChainPath query(String serviceChainId) throws ServiceException {

                return null;
            }
        };

        assertEquals(ErrorCode.OVERLAYVPN_SUCCESS, serviceChainSvc.delete(request, response, "uuid").getErrorCode());

        mock.tearDown();
    }

    private final class MockInventoryDao<T> extends MockUp<InventoryDao<T>> {

        @Mock
        ResultRsp queryByFilter(Class clazz, String filter, String queryResultFields) throws ServiceException {
            if(ServiceChainReqModelInfo.class.equals(clazz)) {
                ServiceChainReqModelInfo serviceChainReqModelInfo = null;

                ResultRsp<List<ServiceChainReqModelInfo>> resp = new ResultRsp<List<ServiceChainReqModelInfo>>(
                        ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(serviceChainReqModelInfo));
                resp.setData(null);
                return resp;
            } else if(NetServiceChainData.class.equals(clazz)) {
                NetServiceChainData netServiceChainData = new NetServiceChainData();
                netServiceChainData.setData(JsonUtil.toJson(new NetServiceChainData()));

                ResultRsp<List<NetServiceChainData>> resp = new ResultRsp<List<NetServiceChainData>>(
                        ErrorCode.OVERLAYVPN_SUCCESS, Arrays.asList(netServiceChainData));
                return resp;
            }

            return null;
        }

        @Mock
        ResultRsp<String> batchDelete(Class clazz, List<String> uuids) throws ServiceException {
            return new ResultRsp<String>();
        }

        @Mock
        public ResultRsp update(Class clazz, List oriUpdateList, String updateFieldListStr) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<T> insert(T data) throws ServiceException {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        @Mock
        public ResultRsp<List<T>> batchInsert(List<T> dataList) {
            return new ResultRsp(ErrorCode.OVERLAYVPN_SUCCESS);
        }
    }

    private final class MockRestfulProxy extends MockUp<RestfulProxy> {

        @Mock
        RestfulResponse post(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            Map<String, NetServiceChainPathRsp> sbiRsp = new HashMap<>();
            sbiRsp.put("serviceChainPath", new NetServiceChainPathRsp());

            response.setStatus(HttpStatus.SC_OK);
            response.setResponseJson(JsonUtil.toJson(sbiRsp));

            return response;
        }

        @Mock
        RestfulResponse delete(String uri, RestfulParametes restParametes) throws ServiceException {
            RestfulResponse response = new RestfulResponse();

            ResultRsp<String> sbiRsp = new ResultRsp<String>(ErrorCode.OVERLAYVPN_SUCCESS);
            response.setStatus(HttpStatus.SC_OK);
            response.setResponseJson(JsonUtil.toJson(sbiRsp));

            return response;
        }

    }

    private class MockNeDao extends MockUp<NetworkElementInvDao> {

        @Mock
        public NetworkElementMO query(String neId) throws ServiceException {
            NetworkElementMO ne = new NetworkElementMO();

            ne.setNativeID(neId + "1");
            ne.setId(neId);

            List<String> controllerIDList = new ArrayList<>();
            controllerIDList.add(neId + "2");
            ne.setControllerID(controllerIDList);

            return ne;
        }
    }

    private ServiceChainPath buildServiceChainPath() {
        ServiceChainPath serviceChainPath = new ServiceChainPath();
        serviceChainPath.setUuid("serviceChainPathUuid");
        serviceChainPath.setName("serviceChainPathName");
        serviceChainPath.setSfcName("sfcName");
        serviceChainPath.setSfcId("sfcId");
        serviceChainPath.setPopId("popId");
        serviceChainPath.setScfNeId("scfNeId");
        serviceChainPath.setSymmetric(false);

        List<ServicePathHop> servicePathHopList = new ArrayList<>();
        ServicePathHop servicePathHop = new ServicePathHop();
        servicePathHopList.add(servicePathHop);
        servicePathHop.setHopNumber(1);
        servicePathHop.setSfgId("sfgId");
        servicePathHop.setSfiId("sfiId");
        serviceChainPath.setServicePathHops(servicePathHopList);

        return serviceChainPath;
    }

}
