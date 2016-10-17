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

package org.openo.sdno.servicechain.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPath;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceChainPathRsp;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServiceClassifer;
import org.openo.sdno.overlayvpn.model.netmodel.servicechain.NetServicePathHop;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPath;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceChainPathRsp;
import org.openo.sdno.overlayvpn.model.servicechain.ServiceClassifer;
import org.openo.sdno.overlayvpn.model.servicechain.ServicePathHop;
import org.openo.sdno.overlayvpn.result.ResultRsp;
import org.openo.sdno.servicechain.sbi.inf.ServiceChainSbiService;
import org.openo.sdno.servicechain.service.inf.ServiceChainService;
import org.openo.sdno.servicechain.util.NetServiceChainDataDbOper;
import org.openo.sdno.servicechain.util.ThrowException;
import org.springframework.stereotype.Service;

/**
 * ServiceChain service implements class.<br>
 * 
 * @author
 * @version SDNO 0.5 August 23, 2016
 */
@Service
public class ServiceChainSvcImpl implements ServiceChainService {

    @Resource
    private ServiceChainSbiService serviceChainSbiService;

    @Override
    public ResultRsp<ServiceChainPathRsp> create(HttpServletRequest req, HttpServletResponse resp, ServiceChainPath sfp)
            throws ServiceException {

        // build SBI data
        NetServiceChainPath neScp = new NetServiceChainPath();
        buildNetServiceChainPath(neScp, sfp);

        NetServiceChainDataDbOper.insert(neScp, sfp.getUuid());
        NetServiceChainDataDbOper.update(sfp.getUuid(), ActionStatus.CREATE_EXCEPTION.getName());

        ResultRsp<NetServiceChainPathRsp> sbiRsp = serviceChainSbiService.create(req, resp, neScp);

        ThrowException.checkRspThrowException(sbiRsp);

        NetServiceChainDataDbOper.update(sfp.getUuid(), ActionStatus.NORMAL.getName());

        ResultRsp<ServiceChainPathRsp> resultRsp = new ResultRsp<ServiceChainPathRsp>(sbiRsp);
        ServiceChainPathRsp serviceChainPathRsp = new ServiceChainPathRsp();
        serviceChainPathRsp.setUuid(sbiRsp.getData().getUuid());
        serviceChainPathRsp.setCreateTime(sbiRsp.getData().getCreateTime());
        serviceChainPathRsp.setOperationId(sbiRsp.getData().getOperationId());
        resultRsp.setData(serviceChainPathRsp);
        return resultRsp;
    }

    @Override
    public ResultRsp<ServiceChainPathRsp> delete(HttpServletRequest req, HttpServletResponse resp, String uuid)
            throws ServiceException {

        // query data
        NetServiceChainPath sfp = NetServiceChainDataDbOper.query(uuid);
        if(null == sfp) {
            return new ResultRsp<ServiceChainPathRsp>(ErrorCode.OVERLAYVPN_SUCCESS);
        }

        // update actionState to exception firstly
        NetServiceChainDataDbOper.update(uuid, ActionStatus.DELETE_EXCEPTION.getName());

        // call the service method to perform delete operation
        ResultRsp<NetServiceChainPathRsp> resultRsp = serviceChainSbiService.delete(req, resp,  uuid, sfp);

        ThrowException.checkRspThrowException(resultRsp);

        NetServiceChainDataDbOper.delete(uuid);

        return new ResultRsp<ServiceChainPathRsp>(ErrorCode.OVERLAYVPN_SUCCESS);
    }

    public void setServiceChainSbiService(ServiceChainSbiService serviceChainSbiService) {
        this.serviceChainSbiService = serviceChainSbiService;
    }

    private void buildNetServiceChainPath(NetServiceChainPath neScp, ServiceChainPath scp) {
        neScp.setUuid(scp.getUuid());
        neScp.setName("tenantName");
        neScp.setDescription(scp.getDescription());
        neScp.setSymmetric(scp.getSymmetric());
        neScp.setTransportType(scp.getTransportType());
        neScp.setScfNeId(scp.getScfNeId());

        List<NetServicePathHop> neSph = new ArrayList<>();
        List<NetServiceClassifer> neSc = new ArrayList<>();

        List<ServicePathHop> sphList = scp.getServicePathHops();
        if(null != sphList) {
            for(ServicePathHop sph : sphList) {
                NetServicePathHop tmp = new NetServicePathHop();
                tmp.setHopNumber(sph.getHopNumber());
                tmp.setSfgId(sph.getSfgId());
                tmp.setSfiId(sph.getSfiId());

                tmp.setSffId(scp.getScfNeId());
                neSph.add(tmp);
            }
        }

        List<ServiceClassifer> scList = scp.getClassifiers();
        if(null != scList) {
            for(ServiceClassifer sc : scList) {
                NetServiceClassifer tmp = new NetServiceClassifer();
                tmp.setInterfaceName(sc.getInterfaceName());
                tmp.setRules(sc.getRules());
                tmp.setZone(sc.getZone());
                neSc.add(tmp);
            }
        }

        neScp.setServicePathHops(neSph);
        neScp.setClassifiers(neSc);
    }
}
