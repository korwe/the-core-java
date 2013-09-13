/*
 * Copyright (c) 2011.  Korwe Software
 *  
 *  This file is part of TheCore.
 *  
 *  TheCore is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  TheCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.korwe.thecore.webcore;

import com.jolbox.bonecp.BoneCPDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceProvider  {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);

    public static BoneCPDataSource createDataSource() {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException e) {
            LOG.error("No postgres driver", e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/webcore");
        ds.setUsername("webcore");
        ds.setPassword("webcore");
        ds.setPartitionCount(2);
        ds.setMinConnectionsPerPartition(2);
        ds.setMaxConnectionsPerPartition(128);
        ds.setAcquireIncrement(5);
        ds.setIdleMaxAgeInSeconds(60);
        ds.setMaxConnectionAgeInSeconds(120);
        return ds;
    }
}