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
import org.bitcoinj.core.listeners.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.Arrays;
import sun.applet.Main;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Downloads the block given a block hash from the localhost node and prints it out.
 */
public class VtknTest {

    public static final String[] DNSPEERS = {
            "relay1.eternitywall.com",
            "relay2.eternitywall.com",
            "relay3.eternitywall.com",
            "relay4.eternitywall.com"
    };
    public static NetworkParameters NETWORK_PARAMETERS = NetworkParameters.regTests();

    public static final int PEER_DISCOVERY_TIMEOUT_MS = 10 * (int) 1000;
    public static final int PEER_TIMEOUT_MS = 15 * (int) 1000;


    static PeerGroup peerGroup;
    static BlockStore blockStore;
    static BlockChain blockChain;
    static Wallet wallet;
    static ECKey ecKey;

    public static void main(String[] args) throws Exception {

        BriefLogFormatter.init();
        //Context.propagate(new Context(NETWORK_PARAMETERS, 100, Coin.ZERO, false));

        // print genesis
        System.out.println("genesis");
        System.out.println(NETWORK_PARAMETERS.getGenesisBlock().toString());

        // build wallet & key
        wallet = new Wallet(NETWORK_PARAMETERS);
        ecKey = wallet.currentReceiveKey();

        // blockstorage
        blockStore = new MemoryBlockStore(NETWORK_PARAMETERS);
        try {
            blockStore.getChainHead(); // detect corruptions as early as pos
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }

        // blockchain
        try {
            blockChain = new BlockChain(NETWORK_PARAMETERS, blockStore);
        } catch (final BlockStoreException x) {
            throw new Error("blockchain cannot be created", x);
        }

        // peergroup
        PeerGroup peerGroup = new PeerGroup(NETWORK_PARAMETERS, blockChain);
        peerGroup.setUserAgent("PeerMonitor", "1.0");
        peerGroup.setMaxConnections(2);
        //peerGroup.setMaxPeersToDiscoverCount(0);
        peerGroup.addConnectedEventListener(peerConnectedEventListener);
        peerGroup.addDisconnectedEventListener(peerDisconnectedEventListener);

        // peergroup connection
        for(InetAddress peer : getPeerAddresses()){
            try {
                if (peer==null) {
                    throw new Exception(peer.getHostAddress());
                }
                peerGroup.addAddress(new PeerAddress(NETWORK_PARAMETERS, peer, NETWORK_PARAMETERS.getPort()));
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("exception on dnsres " + e.getMessage());
            }
        }

        //start
        MyDownload myDownload = new MyDownload();
        peerGroup.startBlockChainDownload(myDownload);
        peerGroup.start();

        Thread.sleep(60*1000);
        closeNetwork();


    }


    public static SPVBlockStore getSPVBlockStore(){

        File blockChainFile = new File("wallet.dat");
        blockChainFile.delete();
        final boolean blockChainFileExists = blockChainFile.exists();
        if (!blockChainFileExists) {
            System.out.println("blockchain does not exist, resetting wallet");
        }

        try {
            SPVBlockStore blockStore = new SPVBlockStore(NETWORK_PARAMETERS, blockChainFile);
            //blockStore.getChainHead(); // detect corruptions as early as possible

            /*final long earliestKeyCreationTime = wallet.getEarliestKeyCreationTime();

            if (!blockChainFileExists && earliestKeyCreationTime > 0) {
                try {
                    final InputStream checkpointsInputStream = getAssets().open(Constants.Files.CHECKPOINTS_FILENAME);
                    CheckpointManager.checkpoint(Constants.NETWORK_PARAMETERS, checkpointsInputStream, blockStore,
                            earliestKeyCreationTime);
                    watch.stop();
                    log.info("checkpoints loaded from '{}', took {}", Constants.Files.CHECKPOINTS_FILENAME, watch);
                } catch (final IOException x) {
                    log.error("problem reading checkpoints, continuing without", x);
                }
            }*/
            return blockStore;
        } catch (final BlockStoreException x) {
            blockChainFile.delete();
            final String msg = "blockstore cannot be created";
            throw new Error(msg, x);
        }

    }


    static PeerConnectedEventListener peerConnectedEventListener = new PeerConnectedEventListener() {
        @Override
        public void onPeerConnected(final Peer peer, int peerCount) {
            System.out.println("onPeerConnected "+peer+ " : "+String.valueOf(peerCount));
        }
    };

    static PeerDisconnectedEventListener peerDisconnectedEventListener = new PeerDisconnectedEventListener() {
        @Override
        public void onPeerDisconnected(final Peer peer, int peerCount) {
            System.out.println("onPeerDisconnected "+peer+ " : "+String.valueOf(peerCount));
        }
    };

    public static class MyDownload extends DownloadProgressTracker {
        @Override
        public void onChainDownloadStarted(Peer peer, int blocksLeft) {
            super.onChainDownloadStarted(peer, blocksLeft);
            System.out.println("onChainDownloadStarted "+peer.getAddr().toString());
        }

        @Override
        protected void startDownload(int blocks) {
            super.startDownload(blocks);
            System.out.println("startDownload");
        }

        @Override
        protected void doneDownload() {
            System.out.println("doneDownload");
        }

        @Override
        public void onBlocksDownloaded(Peer peer, Block block, FilteredBlock filteredBlock, int blocksLeft) {
            super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
            System.out.println("onBlocksDownloaded "+peer.toString()+" : "+block.toString());
        }

        @Override
        public Message onPreMessageReceived(Peer peer, Message m) {
            System.out.println("onPreMessageReceived "+peer.toString()+" : "+m.toString());
            return super.onPreMessageReceived(peer, m);
        }

        @Nullable
        @Override
        public List<Message> getData(Peer peer, GetDataMessage m) {
            System.out.println("getData "+peer.toString()+" : "+m.toString());
            //Log.i("onChainDownloadStarted", peer.toString());
            return null;
        }

    }

    private static void closeNetwork() {
        peerGroup.removeConnectedEventListener(peerConnectedEventListener);
        peerGroup.removeDisconnectedEventListener(peerDisconnectedEventListener);
        peerGroup.stop();
    }

    private static List<InetAddress> getPeerAddresses() throws InterruptedException, ExecutionException {

        // Resolve InetAddress of the peers
        final List<InetAddress> peers = new ArrayList<>();
        final List<Thread> threads = new ArrayList<>();

        for (final String dns : DNSPEERS){
            System.out.println("asking dns ip for " + dns );
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peers.add( InetAddress.getByName(dns) );
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        System.out.println("exception on dnsres " + e);
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }

        for(Thread thread : threads){
            thread.join();
        }
        System.out.println("Thread finishing");


        return peers;

    }

}
