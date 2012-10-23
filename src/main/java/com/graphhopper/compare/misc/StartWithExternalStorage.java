/*
 *  Copyright 2012 Peter Karich info@jetsli.de
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.compare.misc;

import com.graphhopper.compare.neo4j.Neo4JStorage;
import com.graphhopper.reader.OSMReader;
import com.graphhopper.routing.util.RoutingAlgorithmSpecialAreaTests;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.Storage;
import com.graphhopper.util.CmdArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Karich
 */
public class StartWithExternalStorage {

    public static void main(String[] args) throws Exception {
        new StartWithExternalStorage().start(CmdArgs.read(args));
    }
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void start(CmdArgs args) throws Exception {
        int initSize = args.getInt("osmreader.size", 5000000);
        // TODO subnetworks are not deleted and so for 5 routing queries all nodes are traversed
        // (but could be even good to warm caches ;))
        final Storage s = new Neo4JStorage(args.get("neo4j.storage", "neo4j.db"), initSize);
//        final Storage s = new TinkerStorage(readCmdArgs.get("storage", "tinker.db"), initSize);
        OSMReader reader = new OSMReader(s, initSize);
        Graph g = OSMReader.osm2Graph(reader, args);
        logger.info("finished with locations:" + g.getNodes() + " now warm up ...");
        // warm up caches:
        RoutingAlgorithmSpecialAreaTests tester = new RoutingAlgorithmSpecialAreaTests(g);
        String algo = args.get("osmreader.algo", "dijkstra");
        tester.runShortestPathPerf(50, algo);

        logger.info(".. and go!");
        tester.runShortestPathPerf(200, algo);
    }
}
