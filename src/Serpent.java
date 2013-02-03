import edu.rit.util.Hex;
import edu.rit.util.Packing;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Serpent implements BlockCipher {

    private int keySize;
    private byte[] key;
    private long[] prekeys;

    public Serpent() {
        prekeys = new long[140];
    }

    /**
     * Returns this block cipher's block size in bytes.
     *
     * @return  Block size.
     */
    public int blockSize() {
        return 16;
    }

    /**
     * Returns this block cipher's key size in bytes.
     *
     * @return  Key size.
     */
    public int keySize() {
        return 32;
    }

    /**
     * Set the key for this block cipher. If <TT>key</TT> is an array of bytes
     * whose length is less than <TT>keySize()</TT>, it will be padded to 
     * <TT>keySize()</TT>
     *
     * @param  key  Key.
     */
    public void setKey(byte[] key) {
        if (key.length != keySize()) {
            this.key = new byte[keySize()];
            for( int i = 0; i < key.length; i++ ) {
                this.key[i] = key[i];
            }
            for( int i = key.length; i < keySize(); i++ ) {
                if( i == key.length ) {
                    this.key[i] = (byte)0x80;
                }else {
                    this.key[i] = (byte)0x00;
                }
            }
        }else {
            this.key = key;
        }

        //prekey initialization from K
        for( int i = 0; i < 32; i+=4 ) {
            prekeys[i] = Packing.packLongLittleEndian( new byte[]{this.key[i],this.key[i+1],this.key[i+2],this.key[i+3], 
                0x00, 0x00, 0x00, 0x00}, 0 );
        }
        //Build out prekey array
        for( int i = 8; i < prekeys.length; i++ ) {
            ByteBuffer help = ByteBuffer.allocate(Integer.SIZE);
            help.putLong((long)i);
            help.order(ByteOrder.LITTLE_ENDIAN);
            long valI = help.getLong();
            help.order(ByteOrder.BIG_ENDIAN);
            help.putLong((long)0x9e3779b9);
            help.order(ByteOrder.LITTLE_ENDIAN);
            long phi = help.getLong();
            //(x << n) | (x >>> (32 - n))
            prekeys[i] = prekeys[i-8] ^ prekeys[i-5] ^ prekeys[i-3] ^ prekeys[i-1] ^ 
                valI ^ phi;
            prekeys[i] = (prekeys[i] << 11) | (prekeys[i] >>> (21));
        }

    }

    /**
     * Encrypt the given plaintext. <TT>text</TT> must be an array of bytes
     * whose length is equal to <TT>blockSize()</TT>. On input, <TT>text</TT>
     * contains the plaintext block. The plaintext block is encrypted using the
     * key specified in the most recent call to <TT>setKey()</TT>. On output,
     * <TT>text</TT> contains the ciphertext block.
     *
     * @param  text  Plaintext (on input), ciphertext (on output).
     */
    public void encrypt(byte[] text) {

    }


    private void initPermutation(byte[] data) {
        
    }

    private void finalPermutation(byte[] data) {

    }

    private static long[] s0 = new long[]
        {3,8,15,1,10,6,5,11,14,13,4,2,7,0,9,12};
    private static long[] s1 = new long[]
        {15,12,2,7,9,0,5,10,1,11,14,8,6,13,3,4};
    private static long[] s2 = new long[]
        {8,6,7,9,3,12,10,15,13,1,14,4,0,11,5,2};
    private static long[] s3 = new long[]
        {0,15,11,8,12,9,6,3,13,1,2,4,10,7,5,14};
    private static long[] s4 = new long[]
        {1,15,8,3,12,0,11,6,2,5,4,10,9,14,7,13};
    private static long[] s5 = new long[]
        {15,5,2,11,4,10,9,12,0,3,14,8,13,6,7,1};
    private static long[] s6 = new long[]
        {7,2,12,5,8,4,6,11,14,9,1,15,13,3,10,0};
    private static long[] s7 = new long[]
        {1,13,15,0,14,8,2,11,7,4,12,10,9,3,5,6};
    private static long[][] sBoxes = new long[][]
        {s0,s1,s2,s3,s4,s5,s6,s7};

    /**
     * Perform S-Box manipulation to the given byte array of <TT>blocksize()</TT> length.
     *
     * @param data Input bit sequence
     * @param round Number of the current round, used to determine which S-Box to use.
     */
    private byte[] sBox(byte[] data, int round) {
        long[] toUse = sBoxes[round%8];
        byte[] output = new byte[blockSize()];
        for( int i = 0; i < blockSize(); i++ ) {
            //Break signed-ness
            int curr = data[i]&0xFF;
            byte low4 = (byte)(curr>>>4);
            byte high4 = (byte)(curr&0x0F);
            output[i] = (byte)((toUse[low4]<<4) ^ (toUse[high4]));
        }
        return output;
    }

//  private byte[] getRoundKey(int round) {
//  }

    public static void main( String[] args ) {
        //sBoxTest();
        setKeyTest();
    }

    private static void setKeyTest() {
        Serpent serpent = new Serpent();
        byte[] test1 = new byte[] {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,
            (byte)0x88,(byte)0x99,(byte)0xAA,(byte)0xBB,(byte)0xCC,(byte)0xDD,(byte)0xEE,(byte)0xFF};
        serpent.setKey( test1 );
        byte[] test2 = new byte[] {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,
            (byte)0x88,(byte)0x99,(byte)0xAA,(byte)0xBB,(byte)0xCC,(byte)0xDD,(byte)0xEE,(byte)0xFF,
            0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,
            (byte)0x88,(byte)0x99,(byte)0xAA,(byte)0xBB,(byte)0xCC,(byte)0xDD,(byte)0xEE,(byte)0xFF};
        serpent.setKey( test2 );
    }

    private static void sBoxTest(){
        Serpent serpent = new Serpent();
        byte[] test0 = new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        byte[] test1 = new byte[] {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,
            (byte)0x88,(byte)0x99,(byte)0xAA,(byte)0xBB,(byte)0xCC,(byte)0xDD,(byte)0xEE,(byte)0xFF};
        byte[] test2 = new byte[] {0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};
        byte[] test3 = new byte[] {0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10};
        System.out.println( Hex.toString(test0) );
        System.out.println( Hex.toString(serpent.sBox(test0,0)) );
        System.out.println( Hex.toString(test1) );
        System.out.println( Hex.toString(serpent.sBox(test1,0)) );
        System.out.println( Hex.toString(test2) );
        System.out.println( Hex.toString(serpent.sBox(test2,0)) );
        System.out.println( Hex.toString(test3) );
        System.out.println( Hex.toString(serpent.sBox(test3,0)) );
    }
}//Serpent.java
