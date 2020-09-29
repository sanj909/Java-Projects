package blockchain;

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.concurrent.*;
import java.util.Random;

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Block implements Serializable {
    private final long minerId;
    private final int id;
    private final long timestamp;
    private int magicNumber = 0;
    private final String previousHash;
    private String hash; // Create a string containing the above four elements of the block and then get the hash of this string
    private final String data;
    private final long genTime; // Non-essential
    private final int newLeadingZeros; // Non-essential
    private static final long serialVersionUID = 2L; // For serialization

    public Block(long minerId, int id, String previousHash, String data, int leadingZeros) {
        this.minerId = minerId;
        this.id = id;
        this.timestamp = new Date().getTime();
        this.previousHash = previousHash;

        // Mining, i.e. repeatedly trying magic numbers to find the next hash
        this.hash = StringUtil.applySha256(id + timestamp + previousHash + magicNumber);

        String startOfHash = "";
        for (int i = 0; i < leadingZeros; i++) {
            startOfHash += "0";
        }

        while (!hash.substring(0, leadingZeros).startsWith(startOfHash)) {
            this.magicNumber = new Random().nextInt();
            this.hash = StringUtil.applySha256(id + timestamp + previousHash + magicNumber);
        }
        //

        this.data = data;
        this.genTime = new Date().getTime() - timestamp;
        this.newLeadingZeros = genTime < 5000 ? leadingZeros + 1 : leadingZeros - 1;
    }

    @Override
    public String toString() {
        return String.format("Block:\nCreated by miner # %d\nId: %d\nTimestamp: %d\nMagic number: %d\nHash of the previous block:\n%s\n" +
                "Hash of the block:\n%s\nBlock data: %s\nBlock was generating for %.3f seconds\nN was increased to: %d\n",
                minerId, id, timestamp, magicNumber, previousHash, hash, data, ((double) genTime) / 1000, newLeadingZeros);
    }

    public int getId() {
        return id;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getGenTime() {
        return genTime;
    }
}

class Blockchain implements Serializable{
    final ArrayList<Block> chain;
    private int leadingZeros = 0;
    private static final long serialVersionUID = 1L; // For serialization

    public Blockchain() {
        this.chain = new ArrayList<>();
    }

    public void addBlock(Block block) {
        chain.add(block);
    }

    public boolean isValid() {
        try {
            boolean isValid = true;
            for (int i = chain.size() - 1; i > 0; i--) {
                if (!(chain.get(i).getPreviousHash().equals(chain.get(i - 1).getHash()))) {
                    isValid = false;
                    break;
                }
            }
            return isValid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getLastGenTime() {
        return chain.get(chain.size() - 1).getGenTime();
    }

    @Override
    public String toString() {
        String chainAsString = "";
        for (Block block : chain) {
            chainAsString += block.toString() + "\n";
        }
        return chainAsString;
    }

    public ArrayList<Block> getChain() {
        return chain;
    }

    public int getLeadingZeros() {
        return leadingZeros;
    }

    public void incrementLeadingZeros() {
        leadingZeros++;
    }

    public void decrementLeadingZeros() {
        leadingZeros--;
    }
}

class SerializationUtils {
    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}

class mineRequest implements Callable {
    private Blockchain blockchain;
    private List<String> messages;

    public mineRequest(Blockchain blockchain, List<String> messages) {
        this.blockchain = blockchain;
        this.messages = messages;
    }

    @Override
    public Blockchain call() {
        ArrayList<Block> chain = blockchain.getChain();
        int leadingZeros = blockchain.getLeadingZeros();
        long minerId = Thread.currentThread().getId();

        try {
            if (chain.isEmpty()) {
                //System.out.printf("Thread %d is working on block 1 in blockchain %d\n", minerId, blockchain.hashCode());
                Block block = new Block(minerId, 1, "0", "no messages", leadingZeros); // Genesis block
                synchronized (Blockchain.class) {
                    if (chain.isEmpty()) { // If a genesis block has already been added while this thread was waiting due to synchronized, we don't want to add another genesis block.
                        blockchain.addBlock(block);
                    }
                }
            } else { // Adding a non-genesis block
                int previousId = chain.get(chain.size() - 1).getId(); // Id of last block in chain
                String previousHash = chain.get(chain.size() - 1).getHash(); // Hash of last block in chain
                //System.out.printf("\nThread %d is working on block %d in blockchain %d", minerId, previousId + 1, blockchain.hashCode());

                // While block below is being mined, it can't accept new messages. These are added to the next block
                Block block = new Block(minerId, previousId + 1, previousHash, messages.toString(), leadingZeros); // Newly mined block
                synchronized (Blockchain.class) {
                    if (chain.size() == previousId) { // If a block has  been added while this thread was waiting due to synchronized, we don't want to add another block with the same id.
                        blockchain.addBlock(block);
                    }
                }
            }
            return blockchain;
        } catch (Exception e) {
            System.out.println("Error in callable mineRequest");
            return null;
        }
    }
}

class RandomString {
    public static final String chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

    public static String getRandomString() {
        RandomString rs = new RandomString();
        return rs.generateString(new Random(), chars, 10);
    }

    public String generateString(Random random, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }
}

public class Main {
    public static void main(String[] args) {
        String fileName = "/Users/Sanjit/IdeaProjects/Blockchain/Blockchain/task/src/blockchain/blockchain.txt";

        /*
        //Looks for a saved blockchain and creates a new one if it doesn't exist
        if (new File(fileName).exists()) { // If a valid blockchain exists, add blocks to it
            try {
                Blockchain blockchain = (Blockchain) SerializationUtils.deserialize(fileName);
                if (blockchain.isValid()) {
                    generateBlocks(blockchain, fileName);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else { // Create a new blockchain and add blocks to it
            Blockchain blockchain = new Blockchain();
            generateBlocks(blockchain, fileName);
        }
         */

        Blockchain blockchain = new Blockchain();
        List<String> messages = new ArrayList<>();
        generateBlocks(blockchain, fileName, messages);
        System.out.println(blockchain.toString());
        // Known issues:
        // Sometimes, two blocks contain the same message
        // If we loop n times, sometimes more than n blocks are added
        // Sometimes no increment or decrement occurs
    }

    public static void generateBlocks(Blockchain blockchain, String fileName, List<String> messages) {
        try {
            ExecutorService miners = Executors.newFixedThreadPool(4);
            List<String> allMessages = new ArrayList<>();

            for (int i = 0; i < 5; i++) { // If we loop n times, sometimes more than n blocks are added
                List<Callable<Blockchain>> mineRequest = List.of(new mineRequest(blockchain, messages),
                        new mineRequest(blockchain, messages),
                        new mineRequest(blockchain, messages),
                        new mineRequest(blockchain, messages)); // One mine request for each thread to make each thread mine the same block
                miners.invokeAny(mineRequest); // Changes are made to the original blockchain, no need for thread to have a clone

                messages.clear(); // A block should contain messages received during the creation of the previous block
                messages.add(RandomString.getRandomString()); // Simulates a message written by a user

                if (blockchain.getLastGenTime() < 5000) { // Sometimes no increment or decrement occurs
                    blockchain.incrementLeadingZeros();
                } else {
                    blockchain.decrementLeadingZeros();
                }

                /*
                try { // Serializes the blockchain to a file
                    SerializationUtils.serialize(blockchain, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 */
            }
            miners.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
