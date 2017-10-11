/*
 * Copyright 2013 Google Inc.
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

package org.bitcoinj.params;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the old version 2 testnet. This is not useful to you - it exists only because some unit tests are
 * based on it.
 */
public class VtknTestNetParams extends AbstractBitcoinNetParams {
    public static final int VTKNTESTNET_MAJORITY_WINDOW = 100;
    public static final int VTKNTESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 75;
    public static final int VTKNTESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 51;

    public VtknTestNetParams() {
        super();
        id = ID_VTKNTESTNET;
        packetMagic = 0xbfda034eL;
        port = 9045;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x207fFFFFL);
        dumpedPrivateKeyHeader = 239;
        genesisBlock.setTime(1504224000L);
        genesisBlock.setDifficultyTarget(0x207fFFFFL);
        genesisBlock.setNonce(50);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
        String genesisHash = genesisBlock.getHashAsString();
        //checkState(genesisHash.equals("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"));
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;

        majorityEnforceBlockUpgrade = VTKNTESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = VTKNTESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = VTKNTESTNET_MAJORITY_WINDOW;
    }

    private static VtknTestNetParams instance;
    public static synchronized VtknTestNetParams get() {
        if (instance == null) {
            instance = new VtknTestNetParams();
        }
        return instance;
    }

    private static Block genesis;
    @Override
    public Block getGenesisBlock() {
        synchronized (RegTestParams.class) {
            if (genesis == null) {
                genesis = super.getGenesisBlock();
                genesis.setNonce(2);
                genesis.setDifficultyTarget(0x207fFFFFL);
                genesis.setTime(1296688602L);
                //checkState(genesis.getHashAsString().toLowerCase().equals("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206"));
            }
            return genesis;
        }
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_VTKNTESTNET;
    }

}
