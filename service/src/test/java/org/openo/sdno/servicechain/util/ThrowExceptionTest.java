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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;

public class ThrowExceptionTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testCheckRspThrowExceptionInputNull() {

        try {
            ThrowException.checkRspThrowException(null);
            assertTrue(false);
        } catch(ServiceException e) {
            assertEquals(HttpCode.ERR_FAILED, e.getHttpCode());

        }
    }

}
