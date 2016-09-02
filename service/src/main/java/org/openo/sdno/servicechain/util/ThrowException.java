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

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.overlayvpn.result.SvcExcptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThrowException class.<br>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Aug 22, 2016
 */
public class ThrowException {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrowException.class);

    private ThrowException() {
    }

    /**
     * It is used to throw exception when the UUID is existed. <br>
     * 
     * @param uuid The connection id
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwUuidIsExisted(String uuid) throws ServiceException {
        LOGGER.error(String.format("uuid id (%s) is existed", uuid));
        String message = "uuid id (" + uuid + ") is existed";
        String advice = "uuid id is existed, please modify data and try again";
        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_PARAMETER_INVALID, message, message, message,
                advice);
    }

    /**
     * It is used to throw exception when the resource is not existed. <br>
     * 
     * @param description The description
     * @throws ServiceException Throw 400 error
     * @since SDNO 0.5
     */
    public static void throwResNotExist(String description) throws ServiceException {
        LOGGER.error(description);
        String message = description;
        String advice = description + ", please modify data and try again";

        SvcExcptUtil.throwBadReqSvcExptionWithInfo(ErrorCode.OVERLAYVPN_RESOURCE_NOT_EXIST, message, message, message,
                advice);
    }

    /**
     * It is used to check the operation result. <br>
     * 
     * @param result The operation result
     * @throws ServiceException Throw 500 error
     * @since SDNO 0.5
     */
    public static void checkRspThrowException(ResultRsp<?> result) throws ServiceException {
        if(result == null) {
            LOGGER.error("operation failed! ErrorCode = " + ErrorCode.OVERLAYVPN_FAILED);
            throw new ServiceException(ErrorCode.OVERLAYVPN_FAILED, HttpCode.ERR_FAILED);
        }

        if(!result.isSuccess()) {
            SvcExcptUtil.throwSvcExptionByResultRsp(result);
        }
    }
}
