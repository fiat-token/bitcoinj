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
    private static final String VTKN_SCHEME = "vtkn";

    public VtknTestNetParams() {
        super();
        id = ID_VTKNTESTNET;
        packetMagic = 0x110a034eL;
        port = 9045;

        // base58Prefixes
        addressHeader = 111; // start with 'm' or 'n'
        p2shHeader = 196; // start with 2
        dumpedPrivateKeyHeader = 239; // EF
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
                genesis.setTime(1510768800L);

                Transaction t = new Transaction(this);
                try {
                    // A script containing the difficulty bits and the following message:
                    //
                    //   "Lo choc per gli Azzurri fuori dai Mondiali"
                    byte[] bytes = Utils.HEX.decode
                            ("04ffff00"+"1d0104"+"2a"+
                                    "4c6f2063686f632070657220676c6920417a7a757272692066756f726920646169204d6f6e6469616c69");
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
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("0274b9539ccf659745550818b5782d950eca2d7a0ad21fd7ab5f06348cc8ba965b"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("0269a3bc44d5c01aef34db1c883df236187ef49c875662b39f55d16dc1fda56422"));
                    Script.writeBytes(challengeBytes, Utils.HEX.decode("0301914408f03b2aaa24e1ba628813fa6cb795eb0b40d4768568000b68b6f8e075"));
                    challengeBytes.write(ScriptOpCodes.OP_3);
                    challengeBytes.write(ScriptOpCodes.OP_CHECKMULTISIG);

                    genesis.setChallenge(new Script(challengeBytes.toByteArray()));
                } catch (Exception e) {
                    // Cannot happen.
                    throw new RuntimeException(e);
                }
                checkState(genesis.getHashAsString().toLowerCase().equals("fecda7660014b377f1043fdbe176431cd110242c59d273c5a815466dda41344d"));
            }
            return genesis;
        }
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_VTKNTESTNET;
    }

    @Override
    public String getUriScheme() {
        return VTKN_SCHEME;
    }

}
