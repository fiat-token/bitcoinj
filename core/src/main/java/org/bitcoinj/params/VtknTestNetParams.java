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

import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;

import java.io.ByteArrayOutputStream;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.core.Coin.FIFTY_COINS;
import static org.bitcoinj.core.Coin.ZERO;

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
        packetMagic = 0x110a034eL;
        //port = 9045;
        port = 18444;
        addressHeader = 65; // start with T
        p2shHeader = 58; // start with Q

        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x207fFFFFL);
        dumpedPrivateKeyHeader = 239;
        genesisBlock.setTime(1504224000L);
        genesisBlock.setDifficultyTarget(0x207fFFFFL);
        genesisBlock.setNonce(100);
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
        synchronized (AbstractBitcoinNetParams.class) {
            if (genesis == null) {
                genesis = new Block(this, Block.BLOCK_VERSION_GENESIS);
                genesis.setNonce(100);
                genesis.setDifficultyTarget(0x207fFFFFL);
                genesis.setTime(1504224000L);

                Transaction t = new Transaction(this);
                try {
                    // A script containing the difficulty bits and the following message:
                    //
                    //   "Virtual Token birth"
                    byte[] bytes = Utils.HEX.decode
                            ("04ffff00"+"1d0104"+"13"+
                                    "5669727475616c20546f6b656e206269727468");
                    //byte[] bytes = Utils.HEX.decode
                    //        ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");

                    t.addInput(new TransactionInput(this, t, bytes));
                    ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
                    scriptPubKeyBytes.write(ScriptOpCodes.OP_RETURN);
                    t.addOutput(new TransactionOutput(this, t, ZERO, scriptPubKeyBytes.toByteArray()));
                } catch (Exception e) {
                    // Cannot happen.
                    throw new RuntimeException(e);
                }
                genesis.addTransaction(t);


                try {
                    ByteArrayOutputStream challengeBytes = new ByteArrayOutputStream();
                    challengeBytes.write(ScriptOpCodes.OP_1);
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("029c2ddd78a0f95f4e787554bfb4f74b2d7dcf40f79f443e5ef350e4a739470f39"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("03a183d14e5acf94aa29c2a200c1bf3de571e83eb734d95bbae890cb3601f0c451"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("02b255a2efbcf1582855fa4d9bb4a5f677668ed910bef61e986b39cc8d3ebaf284"));
                    challengeBytes.write(ScriptOpCodes.OP_3);
                    challengeBytes.write(ScriptOpCodes.OP_CHECKMULTISIG);

                    genesis.setChallenge(new Script(challengeBytes.toByteArray()));
                } catch (Exception e) {
                    // Cannot happen.
                    throw new RuntimeException(e);
                }
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
