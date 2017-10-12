package org.bitcoinj.core;

import com.google.common.base.Joiner;
import org.bitcoinj.script.Script;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class SignedBlock extends Block {

    private long height;
    private Script challenge;
    private ECKey.ECDSASignature signature;

    public SignedBlock(NetworkParameters params, long setVersion) {
        super(params, setVersion);
    }

    public SignedBlock(NetworkParameters params, long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time,
                       long difficultyTarget, long nonce, List<Transaction> transactions, long height, Script challenge,
                       ECKey.ECDSASignature signature) {
        super(params,version,prevBlockHash,merkleRoot,time,difficultyTarget,nonce,transactions);
        this.height = height;
        this.challenge = challenge;
        this.signature = signature;
    }

    protected final void copyBitcoinHeaderTo(final SignedBlock block) {
        block.nonce = nonce;
        block.prevBlockHash = prevBlockHash;
        block.merkleRoot = getMerkleRoot();
        block.version = version;
        block.time = time;
        block.difficultyTarget = difficultyTarget;
        block.transactions = null;
        block.hash = getHash();
        block.height = height;
        block.challenge = challenge;
        block.signature = signature;
    }


    @Override
    protected void parse() throws ProtocolException {
        // header
        cursor = offset;
        version = readUint32();
        prevBlockHash = readHash();
        merkleRoot = readHash();
        time = readUint32();
        difficultyTarget = readUint32();
        nonce = readUint32();
        hash = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(payload, offset, cursor - offset));

        height = readUint32();

        long len = readVarInt();
        byte[] challangeRaw = readBytes((int) len);
        challenge = new Script(challangeRaw);

        len = readVarInt();
        byte[] signatureRaw = readBytes((int) len);
        signature = ECKey.ECDSASignature.decodeFromDER(signatureRaw);

        headerBytesValid = serializer.isParseRetainMode();

        // transactions
        parseTransactions(offset + HEADER_SIZE);
        length = cursor - offset;
    }


    // default for testing
    void writeHeader(OutputStream stream) throws IOException {
        // try for cached write first
        if (headerBytesValid && payload != null && payload.length >= offset + HEADER_SIZE) {
            stream.write(payload, offset, HEADER_SIZE);
            return;
        }
        // fall back to manual write
        Utils.uint32ToByteStreamLE(version, stream);
        stream.write(prevBlockHash.getReversedBytes());
        stream.write(getMerkleRoot().getReversedBytes());
        Utils.uint32ToByteStreamLE(time, stream);
        Utils.uint32ToByteStreamLE(difficultyTarget, stream);
        Utils.uint32ToByteStreamLE(nonce, stream);
        Utils.uint32ToByteStreamLE(height, stream);
        stream.write(challenge.getProgram());
        stream.write(signature.encodeToDER());
    }

    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(" block: \n");
        s.append("   hash: ").append(getHashAsString()).append('\n');
        s.append("   version: ").append(version);
        String bips = Joiner.on(", ").skipNulls().join(isBIP34() ? "BIP34" : null, isBIP66() ? "BIP66" : null,
                isBIP65() ? "BIP65" : null);
        if (!bips.isEmpty())
            s.append(" (").append(bips).append(')');
        s.append('\n');
        s.append("   previous block: ").append(getPrevBlockHash()).append("\n");
        s.append("   merkle root: ").append(getMerkleRoot()).append("\n");
        s.append("   time: ").append(time).append(" (").append(Utils.dateTimeFormat(time * 1000)).append(")\n");
        s.append("   difficulty target (nBits): ").append(difficultyTarget).append("\n");
        s.append("   nonce: ").append(nonce).append("\n");
        s.append("   height: ").append(height).append("\n");
        s.append("   challenge: ").append(challenge).append("\n");
        s.append("   signature: ").append(signature).append("\n");
        if (transactions != null && transactions.size() > 0) {
            s.append("   with ").append(transactions.size()).append(" transaction(s):\n");
            for (Transaction tx : transactions) {
                s.append(tx);
            }
        }
        return s.toString();
    }

    public ECKey.ECDSASignature getSignature() {
        return signature;
    }

    public void setSignature(ECKey.ECDSASignature signature) {
        this.signature = signature;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public Script getChallenge() {
        return challenge;
    }

    public void setChallenge(Script challenge) {
        this.challenge = challenge;
    }
}
