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

package org.openo.sdno.servicechain.model;

import org.openo.sdno.overlayvpn.inventory.sdk.model.annotation.MOResType;
import org.openo.sdno.overlayvpn.model.common.enums.ActionStatus;
import org.openo.sdno.overlayvpn.model.uuid.AbstUuidModel;

/**
 * NetServiceChainData class.<br>
 * 
 * @author
 * @version SDNO 0.5 September 1, 2016
 */
@MOResType(infoModelName = "netservicechaindata")
public class NetServiceChainData extends AbstUuidModel {

    private String nbiServiceChainId;

    private String actionState = ActionStatus.NORMAL.getName();

    private String data;

    public String getNbiServiceChainId() {
        return nbiServiceChainId;
    }

    public void setNbiServiceChainId(String nbiServiceChainId) {
        this.nbiServiceChainId = nbiServiceChainId;
    }

    public String getActionState() {
        return actionState;
    }

    public void setActionState(String actionState) {
        this.actionState = actionState;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
