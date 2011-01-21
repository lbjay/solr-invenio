package org.ads.solr.test;

import com.jcraft.jzlib.*;
import java.util.zip.*;
import java.io.*;
import org.ads.solr.InvenioBitSet;
import org.apache.commons.codec.binary.Base64;

class ZLibBitSetTest {
    public static void main(String[] args)
    {
        InvenioBitSet bitset = new InvenioBitSet(100);
        bitset.set(99, true);
        bitset.set(37, true);
        bitset.set(33, true);
        bitset.set(45, true);

        InvenioBitSet bitset2 = new InvenioBitSet(100);
        bitset2.set(56, true);

        bitset.or(bitset2);

        ByteArrayInputStream bytes_in = new ByteArrayInputStream(bitset.toByteArray());
        ZInputStream zIn = new ZInputStream(bytes_in, JZlib.Z_BEST_COMPRESSION);

        System.out.println("total bytes: " + zIn.getTotalIn());
//        Deflater def = new Deflater();
 //       def.setInput(bitset.toByteArray());
  //      def.finish();

        ByteArrayOutputStream bytes_out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = zIn.read(buf, 0, 1024)) != -1) {
                System.out.println(len);
                bytes_out.write(buf, 0, len);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("total bytes out: " + zIn.getTotalOut());

        byte[] compressedBitset = bytes_out.toByteArray();
        System.out.println(new String(compressedBitset));
        byte[] base64out = Base64.encodeBase64(compressedBitset);
        System.out.println("output: " + base64out.toString());
//        DeflaterOutputStream deflater = new DeflaterOutputStream(bytes_out);
//
//        byte[] bitset_bytes = bitset.toByteArray();
//
//        try {
//            deflater.write(bitset_bytes, 0, bitset_bytes.length);
//            deflater.close();
//        } catch (IOException e) {
//            System.out.println("Exception during deflater.write: " + e);
//        }
//
////        System.out.println("bytes_out.toString(): " + bytes_out.toString());
////        byte[] output = out.toByteArray();
//        byte[] base64out = Base64.encodeBase64(bytes_out.toByteArray());
//        System.out.println("output: " + base64out.toString());


    }
}
