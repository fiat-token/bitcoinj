/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.examples;

import org.bitcoinj.core.*;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.params.VtknTestNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Downloads the block given a block hash from the localhost node and prints it out.
 */
public class VtknTest {

    public static final String[] DNSPEERS = {
            "test.signer1.eternitywall.com"
    };
    public static NetworkParameters NETWORK_PARAMETERS = VtknTestNetParams.get();

    public static void main(String[] args) throws Exception {

        BriefLogFormatter.init();
        System.out.println("Connecting to node");
        final NetworkParameters params = VtknTestNetParams.get();

        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.start();


        // Resolve InetAddress of the peers
        final List<InetAddress> peers = new ArrayList<>();
        final List<Thread> threads = new ArrayList<>();

        for (final String dns : DNSPEERS){
            System.out.print("asking dns ip for " + dns );
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //byte[] ipAddr = new byte[] { 10, 0, 2, 2 };
                        //peers.add(InetAddress.getByAddress(ipAddr));
                        peers.add( InetAddress.getByName(dns) );
                    } catch (UnknownHostException e) {
                        System.out.print("exception on dnsres " + e);
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }

        for(Thread thread : threads){
            thread.join();
        }
        System.out.print("Thread finishing");

        for(InetAddress peer : peers){
            peerGroup.addAddress(new PeerAddress(Constants.NETWORK_PARAMETERS, peer, Constants.NETWORK_PARAMETERS.getPort() ));
        }


        PeerAddress addr = new PeerAddress(InetAddress.getLocalHost(), params.getPort());
        peerGroup.addAddress(addr);
        peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);

        Sha256Hash blockHash = Sha256Hash.wrap(args[0]);
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);
        peerGroup.stopAsync();
    }
}
