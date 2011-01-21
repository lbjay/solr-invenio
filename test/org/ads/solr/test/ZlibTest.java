package org.ads.solr.test;

import org.ads.solr.InvenioBitSet;
import java.util.zip.*;
import org.apache.commons.codec.binary.Base64;

class ZLibTest {
    public static void main(String[] args)
    {
        try {
            // Encode a String into bytes
            String inputString = args[0];
            System.out.println("input: " + inputString);
            byte[] input = inputString.getBytes("UTF-8");

            // Compress the bytes
            byte[] output = new byte[100];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);

            System.out.println(output.length);
            byte[] encoded = Base64.encodeBase64(output, true);
            System.out.println(encoded.length);
            String outputString = new String(encoded, "UTF-8");
            System.out.println("output: " + outputString);
            
            // Decompress the bytes
//            Inflater decompresser = new Inflater();
//            decompresser.setInput(output, 0, compressedDataLength);
//            byte[] result = new byte[100];
//            int resultLength = decompresser.inflate(result);
//            decompresser.end();

            // Decode the bytes into a String
//            String outputString = new String(result, 0, resultLength, "UTF-8");
        } catch(java.io.UnsupportedEncodingException ex) {
            // handle
//        } catch (java.util.zip.DataFormatException ex) {
            // handle
        }
    }
}
