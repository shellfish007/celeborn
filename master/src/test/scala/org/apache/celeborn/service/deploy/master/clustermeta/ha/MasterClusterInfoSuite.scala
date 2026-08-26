/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.celeborn.service.deploy.master.clustermeta.ha

import org.apache.celeborn.CelebornFunSuite
import org.apache.celeborn.common.CelebornConf
import org.apache.celeborn.common.protocol.TransportModuleConstants

class MasterClusterInfoSuite extends CelebornFunSuite {

  private def sslEnabledKey(module: String): String = s"celeborn.ssl.$module.enabled"

  private val ratisKey = sslEnabledKey(TransportModuleConstants.RATIS_MODULE)
  private val rpcServiceKey = sslEnabledKey(TransportModuleConstants.RPC_SERVICE_MODULE)

  test("ratis ssl is disabled when neither ratis nor rpc_service module is enabled") {
    val conf = new CelebornConf()
    assert(!MasterClusterInfo.ratisSslEnabled(conf))
    // The module only matters when ssl is enabled, but it must still default to the
    // legacy module so behavior is unchanged for existing deployments.
    assert(MasterClusterInfo.ratisSslModule(conf) === TransportModuleConstants.RPC_SERVICE_MODULE)
  }

  test("ratis ssl falls back to rpc_service when only rpc_service is enabled") {
    val conf = new CelebornConf()
    conf.set(rpcServiceKey, "true")
    assert(MasterClusterInfo.ratisSslEnabled(conf))
    assert(MasterClusterInfo.ratisSslModule(conf) === TransportModuleConstants.RPC_SERVICE_MODULE)
  }

  test("ratis ssl uses the dedicated ratis module when only ratis is enabled") {
    val conf = new CelebornConf()
    conf.set(ratisKey, "true")
    assert(MasterClusterInfo.ratisSslEnabled(conf))
    assert(MasterClusterInfo.ratisSslModule(conf) === TransportModuleConstants.RATIS_MODULE)
  }

  test("the dedicated ratis module takes precedence when both modules are enabled") {
    val conf = new CelebornConf()
    conf.set(ratisKey, "true")
    conf.set(rpcServiceKey, "true")
    assert(MasterClusterInfo.ratisSslEnabled(conf))
    assert(MasterClusterInfo.ratisSslModule(conf) === TransportModuleConstants.RATIS_MODULE)
  }

  test("ratis ssl is enabled by the ratis module even when rpc_service is explicitly disabled") {
    val conf = new CelebornConf()
    conf.set(ratisKey, "true")
    conf.set(rpcServiceKey, "false")
    assert(MasterClusterInfo.ratisSslEnabled(conf))
    assert(MasterClusterInfo.ratisSslModule(conf) === TransportModuleConstants.RATIS_MODULE)
  }
}
