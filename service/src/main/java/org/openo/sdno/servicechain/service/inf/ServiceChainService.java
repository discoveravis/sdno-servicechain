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

package org.openo.sdno.servicechain.service.inf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.framework.container.service.IService;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPath;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPathRsp;
import org.openo.sdno.overlayvpn.result.ResultRsp;

/**
 * ServiceChainService interface.<br>
 * <p>
 * </p>
 * 
 * @author
 * @version SDNO 0.5 Aug 23, 2016
 */
public interface ServiceChainService extends IService {

    /**
     * Create service chain operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param sfp The ServiceFunctionPath object
     * @return The object of ResultRsp
     * @throws ServiceException When create service chain failed
     * @since SDNO 0.5
     */
    ResultRsp<ServiceChainPathRsp> create(HttpServletRequest req, HttpServletResponse resp, ServiceChainPath sfp)
            throws ServiceException;

    /**
     * Delete ServiceFunctionPath operation. <br>
     * 
     * @param req HttpServletRequest Object
     * @param resp HttpServletResponse Object
     * @param uuid The uuid of ServiceFunctionPath
     * @return The object of ResultRsp
     * @throws ServiceException When delete ServiceFunctionPath failed
     * @since SDNO 0.5
     */
    ResultRsp<ServiceChainPathRsp> delete(HttpServletRequest req, HttpServletResponse resp, String uuid)
            throws ServiceException;
}
