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

import static org.bitcoinj.core.Coin.ZERO;

/**
 * Parameters for the old version 2 testnet. This is not useful to you - it exists only because some unit tests are
 * based on it.
 */
public class VtknNetParams extends AbstractBitcoinNetParams {
    public static final int VTKNTESTNET_MAJORITY_WINDOW = 100;
    public static final int VTKNTESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 75;
    public static final int VTKNTESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 51;
    private static final String VTKN_SCHEME = "vtkn";

    public VtknNetParams() {
        super();
        id = ID_VTKNNET;
        packetMagic = 0x9b6e894eL;
        port = 9044;

        // base58Prefixes
        addressHeader = 70; // start with V
        p2shHeader = 63; // start with S
        dumpedPrivateKeyHeader = 171; // AB
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };

        // time & interval
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x207fFFFFL);

        // genesis
        genesisBlock = getGenesisBlock();

        spendableCoinbaseDepth = 0;
        subsidyDecreaseBlockCount = 210000;

        //checkState(genesisHash.equals("00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008"));
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderPub = 0x0488B21E; //{0x04, 0x88, 0xB2, 0x1E}
        bip32HeaderPriv = 0x0488ADE4; //{0x04, 0x88, 0xAD, 0xE4}


        majorityEnforceBlockUpgrade = VTKNTESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = VTKNTESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = VTKNTESTNET_MAJORITY_WINDOW;
    }

    private static VtknNetParams instance;
    public static synchronized VtknNetParams get() {
        if (instance == null) {
            instance = new VtknNetParams();
        }
        return instance;
    }

    private static Block genesis;
    @Override
    public Block getGenesisBlock() {
        synchronized (AbstractBitcoinNetParams.class) {
            if (genesis == null) {
                genesis = new Block(this, Block.BLOCK_VERSION_GENESIS);
                genesis.setNonce(1);
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
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("02a64ef3743aca9ebdc3493b9639e72efb9379ec893fb16082e2e5f26507a7c064"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("0367eb592715900598704846574f95a2c89866cf531a1387499015a3976d115d49"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("02d1fe19f7cdaee44674687850d80e5176db94f4574cdb4d2740574d7d5727c004"));
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
        return PAYMENT_PROTOCOL_ID_VTKNNET;
    }

    @Override
    public String getUriScheme() {
        return VTKN_SCHEME;
    }

}
