/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.adapters.nxos;

import java.util.Arrays;
import java.util.Optional;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.adapters.collection.CollectionSetWithAgent;
import org.opennms.netmgt.telemetry.adapters.nxos.proto.TelemetryBis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ExtensionRegistry;

/**
 * The Class NxosGpbAdapter.
 */
public class NxosGpbAdapter extends AbstractNxosAdapter {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NxosGpbAdapter.class);

    /** The Constant extension registry. */
    private static final ExtensionRegistry s_registry = ExtensionRegistry.newInstance();

    static {
        TelemetryBis.registerAllExtensions(s_registry);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.telemetry.adapters.nxos.AbstractNxosAdapter#handleMessage(org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage, org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog)
     */
    @Override
    public Optional<CollectionSetWithAgent> handleMessage(TelemetryMessage message, TelemetryMessageLog messageLog) throws Exception {

        final TelemetryBis.Telemetry msg = TelemetryBis.Telemetry.parseFrom(Arrays.copyOfRange(message.getByteArray(), 6, message.getByteArray().length), s_registry);

        CollectionAgent agent = getCollectionAgent(messageLog, msg.getNodeIdStr());

        if (agent == null) {
            LOG.warn("Unable to find node and inteface for system id: {}", msg.getNodeIdStr());
            return Optional.empty();
        }
        return getCollectionSetWithAgent(agent, msg);
    }

}
