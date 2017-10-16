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
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.spongycastle.util.Arrays;

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

/*
    public static final String[] DNSPEERS = {
            //"test.signer1.eternitywall.com",
            "test.signer2.eternitywall.com"
    };
    public static NetworkParameters NETWORK_PARAMETERS = VtknTestNetParams.get();
*/

    public static final String[] DNSPEERS = {
            "relay1.eternitywall.com",
            "relay2.eternitywall.comm",
            "relay3.eternitywall.com",
            "relay4.eternitywall.com"
    };
    public static NetworkParameters NETWORK_PARAMETERS = RegTestParams.get();

    public static final int PEER_DISCOVERY_TIMEOUT_MS = 10 * (int) 1000;
    public static final int PEER_TIMEOUT_MS = 15 * (int) 1000;


    static PeerGroup peerGroup;
    static BlockStore blockStore;
    static BlockChain blockChain;

    public static void main(String[] args) throws Exception {

        BriefLogFormatter.init();

        checkConnection();

        peerGroup = setupNetwork();

        peerGroup.start();
        peerGroup.waitForPeers(1);

        checkSPVBlockStore();



        Thread.sleep(60*1000);
        closeNetwork();


    }

    public static void checkGenesis(){
        System.out.println(NETWORK_PARAMETERS.getGenesisBlock().toString());
    }

    public static void checkBlock(){
        /*byte[] genesis = DatatypeConverter.parseHexBinary("00000020d116864d883055de54c7228cd16a6d506b7c10b2afa84d5a4f07772ae639514ec3ab4a1f8bb06e668928f34feadb0454d02f79757337eec1bd59d3f3f239370c9341de59ffff001d0000000004000000695121029c2ddd78a0f95f4e787554bfb4f74b2d7dcf40f79f443e5ef350e4a739470f392103a183d14e5acf94aa29c2a200c1bf3de571e83eb734d95bbae890cb3601f0c4512102b255a2efbcf1582855fa4d9bb4a5f677668ed910bef61e986b39cc8d3ebaf28453ae48004630440220291532acc6ee849d9a4f94aa743ab5e42ccc73592e8f19d1aa6b40d4061bfc410220110dca34c0908637002c19585c191efe450e1c276604d1e6720e827b206726c5");
        Block block = new Block(NETWORK_PARAMETERS,genesis);
        System.out.println(block.toString());
        System.out.println(block.getHash().toString());

        byte[] raw = block.getSignature().getProgram();
        byte[] buffer = new byte[raw.length-2];
        System.arraycopy(raw,2,buffer,0,raw.length-2);
        ECKey.ECDSASignature signature = ECKey.ECDSASignature.decodeFromDER(buffer);
        System.out.println(signature.toString());

        final Script script = block.getChallenge();
        final List<ECKey> pubKeys = script.getPubKeys();
        for(ECKey current : pubKeys) {
            System.out.println("Current " + current.getPublicKeyAsHex());
            final byte[] reverse = Arrays.reverse(block.getHash().getBytes());
            final boolean verify = ECKey.verify(reverse, signature, current.getPubKey());
            System.out.println("Verify " + verify);
        }*/
    }

    public static void checkMemoryBlockStore(){

        blockStore = new MemoryBlockStore(NETWORK_PARAMETERS);
        try {
            blockStore.getChainHead(); // detect corruptions as early as pos
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }

        try {
            blockChain = new BlockChain(NETWORK_PARAMETERS, blockStore);
        } catch (final BlockStoreException x) {
            throw new Error("blockchain cannot be created", x);
        }


    }

    public static void checkSPVBlockStore(){

        File blockChainFile = new File("wallet.dat");
        blockChainFile.delete();
        final boolean blockChainFileExists = blockChainFile.exists();
        if (!blockChainFileExists) {
            System.out.println("blockchain does not exist, resetting wallet");
        }


        //blockStore = new MemoryBlockStore(NETWORK_PARAMETERS);
        //blockChain = new BlockChain(NETWORK_PARAMETERS, blockStore);

        try {
            blockStore = new SPVBlockStore(NETWORK_PARAMETERS, blockChainFile);
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
        } catch (final BlockStoreException x) {
            blockChainFile.delete();
            final String msg = "blockstore cannot be created";
            throw new Error(msg, x);
        }

        try {
            blockChain = new BlockChain(NETWORK_PARAMETERS, blockStore);
        } catch (final BlockStoreException x) {
            throw new Error("blockchain cannot be created", x);
        }


    }
    public static void checkConnection() throws Exception {
        System.out.println("Connecting to node");



        // check blocks from peer
        /*peerGroup.waitForPeers(1).get();
        Peer peer = peerGroup.getConnectedPeers().get(0);
        Sha256Hash blockHash = NETWORK_PARAMETERS.getGenesisBlock().getHash();;
        Future<Block> future = peer.getBlock(blockHash);
        System.out.println("Waiting for node to send us the requested block: " + blockHash);
        Block block = future.get();
        System.out.println(block);*/





        // check blockstore


        //MyDownload myDownload = new MyDownload();
        //peerGroup.startBlockChainDownload(myDownload);

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

    private static PeerGroup setupNetwork() throws InterruptedException, ExecutionException {
        PeerGroup peerGroup = new PeerGroup(NETWORK_PARAMETERS, blockChain /* no chain */);
        peerGroup.setUserAgent("PeerMonitor", "1.0");
        peerGroup.setMaxConnections(4);
        peerGroup.setDownloadTxDependencies(0); // recursive implementation causes StackOverflowError
        peerGroup.setMinBroadcastConnections(1);
        peerGroup.setMaxConnections(DNSPEERS.length);
        peerGroup.setConnectTimeoutMillis(PEER_TIMEOUT_MS);
        peerGroup.setPeerDiscoveryTimeoutMillis(PEER_DISCOVERY_TIMEOUT_MS);
        peerGroup.setMaxPeersToDiscoverCount(1);

        peerGroup.addConnectedEventListener(peerConnectedEventListener);
        peerGroup.addDisconnectedEventListener(peerDisconnectedEventListener);

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

        for(InetAddress peer : peers){
            try {
                if (peer==null) {
                    throw new Exception("");
                }
                peerGroup.addAddress(new PeerAddress(NETWORK_PARAMETERS, peer, NETWORK_PARAMETERS.getPort()));
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("exception on dnsres " + e);
            }
        }

        return peerGroup;

    }

}
