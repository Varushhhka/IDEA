package IDEA_TEST;

public class BlockStreamCrypter{
    Idea idea;
    boolean encrypt;
    IdeaFileEncryption.Mode mode;
    byte[] prev;

    BlockStreamCrypter (Idea idea, boolean encrypt, IdeaFileEncryption.Mode mode, byte[] iv){
        this.idea = idea;
        this.encrypt = encrypt;
        this.mode = mode;
        this.prev = iv.clone();
    }

    void crypt (byte[] data, int pos){
        switch (mode){
            case ECB:{
                idea.crypt(data, pos);
                break;
            } case OFB:{
                byte[] keystream = prev.clone();
                idea.crypt(keystream, 0);

                for (int i = 0; i < 8; i++) {
                    data[pos + i] ^= keystream[i];
                }

                System.arraycopy(keystream, 0, prev, 0, 8);
                break;
            }
        }
    }
}
