package IDEA_TEST;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Arrays;

public class IdeaFileEncryption{
    public enum Mode {ECB, OFB};

    public static void cryptFile (String inputFileName, String outputFileName, String charKey, boolean encrypt, Mode mode) throws IOException {
        Idea idea;
        byte[] iv = new byte[8];
        if (mode == Mode.OFB){
            idea = new Idea(charKey, true);
            new SecureRandom().nextBytes(iv);
        } else {
            idea = new Idea(charKey, encrypt);
        }
        BlockStreamCrypter bsc = new BlockStreamCrypter(idea, encrypt, mode, iv);;
        FileChannel in = FileChannel.open(Paths.get(inputFileName), StandardOpenOption.READ);
        FileChannel out = FileChannel.open(Paths.get(outputFileName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        long inFileSize = in.size();
        long inDataLen;
        long outDataLen;

        if (encrypt){
            if (mode == Mode.OFB) {
                out.write(ByteBuffer.wrap(iv));
                inDataLen = in.size();
                outDataLen = inDataLen;
                bsc = new BlockStreamCrypter(idea, true, mode, iv);
            } else {
                inDataLen = inFileSize;
                outDataLen = (inFileSize + 7) / 8 * 8;
            }
        } else {
            if (mode == Mode.OFB){
                ByteBuffer ivBuf = ByteBuffer.allocate(8);
                in.read(ivBuf);
                iv = ivBuf.array();
                bsc = new BlockStreamCrypter(idea, true, mode, iv);
            }
            inDataLen = inFileSize - 8;
            outDataLen = inDataLen;
        }

        pumpData(in, inDataLen, out, outDataLen, bsc, mode);

        if (encrypt && mode != Mode.OFB){
            writeDataLength(out, inDataLen, bsc);
        } else if (!encrypt && mode != Mode.OFB){
            long outFileSize = readDataLength(in, bsc);
            if (outFileSize != outDataLen){
                out.truncate(outFileSize);
            }
        }
        in.close();
        out.close();
    }

    private static void pumpData(FileChannel in, long inDataLen, FileChannel out,
                                 long outDataLen, BlockStreamCrypter bsc, Mode mode) throws IOException {
        int bufSize = 0x200000;
        ByteBuffer buf = ByteBuffer.allocate(bufSize);
        long filePos = 0;
        while (filePos < inDataLen){
            int reqLen = (int)Math.min(inDataLen - filePos, bufSize);
            buf.position(0);
            buf.limit(reqLen);

            int trLen = in.read(buf);
            int chunkLen;
            if (mode == Mode.OFB) {
                chunkLen = trLen;
            } else {
                chunkLen = (trLen + 7) / 8 * 8;
                Arrays.fill(buf.array(), trLen, chunkLen, (byte) 0);
            }

            for (int pos = 0; pos < chunkLen; pos += 8) {
                bsc.crypt(buf.array(), pos);
            }

            reqLen = (int)Math.min(outDataLen - filePos, chunkLen);
            buf.position(0);
            buf.limit(reqLen);
            out.write(buf);
            filePos += chunkLen;
        }
    }

    private static long readDataLength (FileChannel in, BlockStreamCrypter bsc)
            throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8);
        in.read(buf);
        byte[] a = buf.array();
        bsc.crypt(a, 0);

        if (a[0] != 0 || a[1] != 0 || (a[7] & 7) != 0){
            return -1;
        }
        return (long)(a[7] & 0xFF) >> 3 |
                (long)(a[6] & 0xFF) << 5 |
                (long)(a[5] & 0xFF) << 13 |
                (long)(a[4] & 0xFF) << 21 |
                (long)(a[3] & 0xFF) << 29 |
                (long)(a[2] & 0xFF) << 37;
    }

    private static void writeDataLength (FileChannel in, long dataLength, BlockStreamCrypter bsc)
            throws IOException {
        byte[] a = new byte[8];
        a[7] = (byte)(dataLength << 3);
        a[6] = (byte)(dataLength >> 5);
        a[5] = (byte)(dataLength >> 13);
        a[4] = (byte)(dataLength >> 21);
        a[3] = (byte)(dataLength >> 29);
        a[2] = (byte)(dataLength >> 37);

        bsc.crypt(a, 0);
        ByteBuffer buf = ByteBuffer.wrap(a);
        in.write(buf);
    }
}
