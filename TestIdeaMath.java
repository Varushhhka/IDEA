package IDEA_TEST;

import java.util.Arrays;

public class TestIdeaMath {
    public static void main(String[] args) throws Exception{
        System.out.println("Начало тестирования");
        testAdd();
        testAddInv();
        testMul();
        testMulInv();
        testGenerateSubKeys(decodeHex("0001 0002 0003 0004 0005 0006 0007 0008"));
        testInvertSubKeys(decodeHex("0001 0002 0003 0004 0005 0006 0007 0008"));
        testGenerateByteKeyFromCharKey();
        System.out.println("Тестирование завершено успешно!");
    }

    private static void testAdd() throws Exception{
        boolean result = true;
        if (Idea.add(0x1234, 0x5678) != 0x68AC){
            result = false;
        }
        if (Idea.add(0xFFFF, 1) != 0){
            result = false;
        }
        if (Idea.add(0, 0) != 0){
            result = false;
        }

        if (! result){
            throw new Exception("Некорректное сложение по модулю 65536");
        }
    }

    private static void testAddInv() throws Exception {
        boolean result = true;
        int[] testValues = {0, 1, 0x1234, 0xFFFF};
        for (int i = 0; i < 4; i++) {
            if (Idea.add(Idea.addInv(testValues[i]), testValues[i]) != 0){
                result = false;
                break;
            }
        }
        if (!result){
            throw new Exception("Некорректное нахождение противоположного значения по модулю 65536");
        }
    }

    private static void testMul() throws Exception{
        boolean result = true;
        if (Idea.mul(0x1234, 0x5678) != (int)((0x1234L * 0x5678L) % 0x10001)){
            result = false;
        }
        if (Idea.mul(0, 0x1234) != Idea.mul(0x1234, 0) || Idea.mul(0, 0x1234) != ((1 - 0x1234) & 0xFFFF)){
            result = false;
        }
        if (Idea.mul(0, 0) != 1) {
            result = false;
        }

        if (!result){
            throw new Exception("Некорректное умножение по модулю 65537");
        }
    }

    private static void testMulInv() throws Exception{
        boolean result = true;
        int[] testValues = {1, 2, 0x1234, 0x5678, 0xFFFF};
        for (int i = 0; i < 5; i++){
            if (Idea.mul(Idea.mulInv(testValues[i]), testValues[i]) != 1){
                result = false;
                break;
            }
        }
        if (!result){
            throw new Exception("Некорректное нахождение обратного значения по модулю 65537");
        }
    }

    private static void testGenerateSubKeys(byte[] key) throws Exception{
        int[] testKeys = {1, 2, 3, 4, 5, 6,
                            7, 8, 1024, 1536, 2048, 2560,
                            3072, 3584, 4096, 512, 16, 20,
                            24, 28, 32, 4, 8, 12,
                            10240, 12288, 14336, 16384, 2048, 4096,
                            6144, 8192, 112, 128, 16, 32,
                            48, 64, 80, 96, 0, 8192,
                            16384, 24576, 32768, 40960, 49152, 57345,
                            128, 192, 256, 320};
        int[] subKeys = Idea.generateSubKeys(key);
        boolean result = true;
        for (int i = 0; i < 52; i++){
            if (subKeys[i] != testKeys[i]){
                result = false;
                break;
            }
        }
        if (!result){
            throw new Exception("Некорректное нахождение подключей для зашифрования");
        }
    }

    private static void testInvertSubKeys(byte[] key) throws Exception{
        int[] testKeys = {65025, 65344, 65280, 26010, 49152, 57345,
                        65533, 32768, 40960, 52428, 0, 8192,
                        42326, 65456, 65472, 21163, 16, 32,
                        21835, 65424, 57344, 65025, 2048, 4096,
                        13101, 51200, 53248, 65533, 8, 12,
                        19115, 65504, 65508,49153, 16, 20,
                        43670, 61440, 61952, 65409, 2048, 2560,
                        18725, 64512, 65528, 21803, 5, 6,
                        1, 65534, 65533, 49153};
        int[] subKeys = Idea.invertSubKeys(Idea.generateSubKeys(key));
        boolean result = true;
        for (int i = 0; i < 52; i++){
            if (subKeys[i] != testKeys[i]){
                result = false;
                break;
            }
        }
        if (!result){
            throw new Exception("Некорректное нахождение подключей для расшифрования");
        }
    }

    private static void testGenerateByteKeyFromCharKey()throws Exception{
        String charKey = "MySecretKey";
        byte[] key1 = Idea.generateByteKeyFromCharKey(charKey);
        byte[] key2 = Idea.generateByteKeyFromCharKey(charKey);

        boolean result = true;
        if (key1.length != 16 || key1.length != key2.length){
            result = false;
        }

        if (!result){
            throw new Exception("Некорректное создание ключей из пароля (длина)");
        }

        for (int i = 0; i < 16; i++) {
            if (key1[i] != key2[i]) {
                result = false;
                break;
            }
        }

        if (!result){
            throw new Exception("Некорректное создание ключей из пароля (несовпадение)");
        }
    }


    private static byte[] decodeHex (String s){
        byte[] buf = new byte[s.length() / 2];
        int p1 = 0;
        int p2 = 0;
        while (p1 < s.length()){
            if (s.charAt(p1) == ' '){
                p1++;
                continue;
            }
            buf[p2++] = (byte)Integer.parseInt(s.substring(p1, p1 + 2), 16);
            p1 += 2;
        }
        return Arrays.copyOf(buf, p2);
    }
}