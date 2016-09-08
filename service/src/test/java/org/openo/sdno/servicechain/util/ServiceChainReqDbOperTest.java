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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.servicechain.model.ServiceChainReqModelInfo;

import mockit.Mock;
import mockit.MockUp;

public class ServiceChainReqDbOperTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test()
    public void testUpdateEmptyException() {
        MockUp<ServiceChainReqDbOper> mock = new MockUp<ServiceChainReqDbOper>() {

            @Mock
            private ResultRsp<List<ServiceChainReqModelInfo>> queryByFilter(String serviceChainId,
                    String queryResultFields) throws ServiceException {
                ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = new ResultRsp<List<ServiceChainReqModelInfo>>();
                return queryDbRsp;
            }
        };

        try {
            ServiceChainReqDbOper.update("serviceChainId", "actionState");
            assertTrue(false);
        } catch(ServiceException e) {
            assertTrue(true);
        }

        mock.tearDown();
    }

    @Test
    public void testQueryReturnNull() throws ServiceException {

        MockUp<ServiceChainReqDbOper> mock = new MockUp<ServiceChainReqDbOper>() {

            @Mock
            private ResultRsp<List<ServiceChainReqModelInfo>> queryByFilter(String serviceChainId,
                    String queryResultFields) throws ServiceException {
                ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = new ResultRsp<List<ServiceChainReqModelInfo>>();
                return queryDbRsp;
            }
        };

        assertNull(ServiceChainReqDbOper.query("serviceChainId"));
        mock.tearDown();
    }

    @Test
    public void testDeleteNotExist() throws ServiceException {

        MockUp<ServiceChainReqDbOper> mock = new MockUp<ServiceChainReqDbOper>() {

            @Mock
            private ResultRsp<List<ServiceChainReqModelInfo>> queryByFilter(String serviceChainId,
                    String queryResultFields) throws ServiceException {
                ResultRsp<List<ServiceChainReqModelInfo>> queryDbRsp = new ResultRsp<List<ServiceChainReqModelInfo>>();
                return queryDbRsp;
            }
        };

        ServiceChainReqDbOper.delete("serviceChainId");
        mock.tearDown();
    }

}
