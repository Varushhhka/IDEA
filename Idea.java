package IDEA_TEST;

public class Idea{
    private int[] subKeys;

    public Idea (byte[] key, boolean encrypt){
        int[] tempKeys = generateSubKeys(key);
        if (encrypt) {
            subKeys = tempKeys;
        } else {
            subKeys = invertSubKeys(tempKeys);
        }
    }

    public Idea (String charKey, boolean encrypt){
        this(generateByteKeyFromCharKey(charKey), encrypt);
    }

    public void crypt (byte[] data, int pos) {
        int x0 = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
        int x1 = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
        int x2 = ((data[pos + 4] & 0xFF) << 8) | (data[pos + 5] & 0xFF);
        int x3 = ((data[pos + 6] & 0xFF) << 8) | (data[pos + 7] & 0xFF);

        int p = 0;
        for (int round = 0; round < 8; round++){
            int y0 = mul(x0, subKeys[p++]);
            int y1 = add(x1, subKeys[p++]);
            int y2 = add(x2, subKeys[p++]);
            int y3 = mul(x3, subKeys[p++]);
            int t0 = mul(y0 ^ y2, subKeys[p++]);
            int t1 = add(y1 ^ y3, t0);
            int t2 = mul(t1, subKeys[p++]);
            int t3 = add(t0, t2);
            x0 = y0 ^ t2;
            x1 = y2 ^ t2;
            x2 = y1 ^ t3;
            x3 = y3 ^ t3;
        }

        int r0 = mul(x0, subKeys[p++]);
        int r1 = add(x2, subKeys[p++]);
        int r2 = add(x1, subKeys[p++]);
        int r3 = mul(x3, subKeys[p]);

        data[pos] = (byte)(r0 >> 8);
        data[pos + 1] = (byte)r0;
        data[pos + 2] = (byte)(r1 >> 8);
        data[pos + 3] = (byte)r1;
        data[pos + 4] = (byte)(r2 >> 8);
        data[pos + 5] = (byte)r2;
        data[pos + 6] = (byte)(r3 >> 8);
        data[pos + 7] = (byte)r3;
    }

    static int add(int a, int b){
        return (a + b) & 0xFFFF;
    }

    static int addInv(int x){
        return (0x10000 - x) & 0xFFFF;
    }

    static int mul(int a, int b){
        long r = (long)a * b;
        if (r != 0){
            return (int)(r % 0x10001) & 0xFFFF;
        } else if (a == 0){
            return (1 - b) & 0xFFFF;
        } else if (b == 0){
            return (1 - a) & 0xFFFF;
        } else {
            return 0x0001;
        }
    }

    static int mulInv(int x){
        if (x <= 1){
            return x;
        }
        int y = 0x10001;
        int a = 1;
        int b = 0;
        while (true){
            b += y / x * a;
            y %= x;
            if (y == 1){
                return 0x10001 - b;
            }
            a += x / y * b;
            x %= y;
            if (x == 1){
                return a;
            }
        }
    }

    static int[] generateSubKeys(byte[] userKey){
        int[] keys = new int[52];
        for (int i = 0; i < 8; i++){
            keys[i] = ((userKey[2 * i] & 0xFF) << 8) | (userKey[2 * i + 1] & 0xFF);
        }
        for (int i = 8; i < 52; i++){
            keys[i] = ((keys[(i + 1) % 8 != 0 ? i - 7 : i - 15] << 9) |
                       (keys[(i + 2) % 8 < 2 ? i - 14 : i - 6] >> 7)) & 0xFFFF;
        }
        return keys;
    }

    static int[] invertSubKeys(int[] key){
        int[] invKeys = new int[52];
        int p = 0;
        invKeys[48] = mulInv(key[p++]);
        invKeys[49] = addInv(key[p++]);
        invKeys[50] = addInv(key[p++]);
        invKeys[51] = mulInv(key[p++]);
        int i;
        for (int r = 7; r >= 0; r--){
            i = r * 6;
            invKeys[i + 4] = key[p++];
            invKeys[i + 5] = key[p++];
            invKeys[i] = mulInv(key[p++]);
            if (r > 0) {
                invKeys[i + 2] = addInv(key[p++]);
                invKeys[i + 1] = addInv(key[p++]);
            } else {
                invKeys[i + 1] = addInv(key[p++]);
                invKeys[i + 2] = addInv(key[p++]);
            }
            invKeys[i + 3] = mulInv(key[p++]);
        }
        return invKeys;
    }

    static byte[] generateByteKeyFromCharKey(String charKey){
        int[] a = new int[8];
        for (int p = 0; p < charKey.length(); p++){
            int c = charKey.charAt(p);
            c -= 0x21;
            for (int i = 7; i >= 0; i--){
                c += a[i] * 94;
                a[i] = c & 0xFFFF;
                c >>= 16;
            }
        }
        byte[] key = new byte[16];
        for (int i = 0; i < 8; i++){
            key[i * 2] = (byte)(a[i] >> 8);
            key[i * 2 + 1] = (byte)a[i];
        }
        return key;
    }

}
