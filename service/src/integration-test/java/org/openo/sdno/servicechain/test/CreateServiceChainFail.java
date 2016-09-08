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

package org.openo.sdno.servicechain.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.servicechain.mocoserver.SbiAdapterCreateServiceChainFail;
import org.openo.sdno.servicechain.util.HttpRest;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;

public class CreateServiceChainFail extends TestManager {

    private static SbiAdapterCreateServiceChainFail sbiAdapterServer1 = new SbiAdapterCreateServiceChainFail();

    private static final String CREATE_SERVICECHAIN_FAIL_TESTCASE =
            "src/integration-test/resources/testcase/createservicechainfail1.json";

    private static final String DELETE_SERVICECHAIN_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/deleteservicechainsuccess1.json";

    private int status = 500;

    @BeforeClass
    public static void setup() throws ServiceException {
        sbiAdapterServer1.start();
    }

    @AfterClass
    public static void tearDown() throws ServiceException {
        sbiAdapterServer1.stop();
    }

    @Test
    public void testCreateServiceChainFailAsAdapterRsp500() throws ServiceException {
        try {

            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_SERVICECHAIN_FAIL_TESTCASE);
            HttpRequest createRequest = httpCreateObject.getRequest();

            execTestCase(createRequest, new CheckerAdapterReturnErr());

        } finally {

            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_SERVICECHAIN_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("uuid", deleteRequest.getUri(), "servicechainId1"));
            HttpRest.doSend(deleteRequest);

        }
    }

    @Test
    public void testCreateServiceChainFailAsAdapterRsp404() throws ServiceException {
        try {

            status = 404;
            sbiAdapterServer1.setCreateErrStatus(status);

            // test create
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_SERVICECHAIN_FAIL_TESTCASE);
            HttpRequest createRequest = httpCreateObject.getRequest();

            execTestCase(createRequest, new CheckerAdapterReturnErr());

        } finally {

            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_SERVICECHAIN_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("uuid", deleteRequest.getUri(), "servicechainId1"));
            HttpRest.doSend(deleteRequest);

        }
    }

    private class CheckerAdapterReturnErr implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(404 == status) {
                if(HttpCode.ERR_FAILED == response.getStatus()
                        && response.getData().contains(ErrorCode.RESTFUL_COMMUNICATION_FAILED)) {
                    return true;
                }
            } else {
                if(HttpCode.ERR_FAILED == response.getStatus()) {
                    return true;
                }
            }

            return false;
        }
    }

}
