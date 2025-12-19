package IDEA_TEST;
import java.util.Arrays;

public class TestIdeaVectors{
    public static void main (String[] args) throws Exception{
        System.out.println("Начало тестирования");
        testOldEcbVectors();
        testNewEcbVectors();
        System.out.println("Тестирование завершено успешно!");
    }

    // Тест на старых тесторых векторах Test_Cases_IDEA.txt (29-01-1999)
    private static void testOldEcbVectors() throws Exception{
        String key = "729A 27ED 8F5C 3E8B AF16 560D 14C9 0B43";
        testEcb(key, "D53F ABBF 94FF 8B5F", "1D0C B2AF 1654 820A");
        testEcb(key, "848F 8367 8093 8169", "D7E0 4682 26D0 FC56");
        testEcb(key, "8194 40CA 2065 D112", "264A 8BBA 6695 9075");
        testEcb(key, "6889 F564 7AB2 3D59", "F963 468B 52F4 5D4D");
        testEcb(key, "DF8C 6FC6 37E3 DAD1", "2935 8CC6 C838 28AE");
        testEcb(key, "AC48 5624 2B12 1589", "95CD 92F4 4BAC B72D");
        testEcb(key, "CBE4 65F2 32F9 D85C", "BCE2 4DC8 D096 1C44");
        testEcb(key, "6C2E 3617 DA2B AC35", "1569 E062 7007 B12E");
    }

    // Тест на новых тестовых векторах NewTestCases.txt (21-05-1999)
    private static void testNewEcbVectors() throws Exception{
        String key = "0000 27ED 8F5C 3E8B AF16 560D 14C9 0B43";
        testEcb(key, "D53F ABBF 94FF 8B5F", "1320 F99B FE05 2804");
        testEcb(key, "848F 8367 8093 8169", "4821 B99F 61AC EBB7");
        testEcb(key, "8194 40CA 2065 D112", "C886 0009 3B34 8575");
        testEcb(key, "6889 F564 7AB2 3D59", "61D5 3970 46F9 9637");
        testEcb(key, "DF8C 6FC6 37E3 DAD1", "EF48 99B4 8DE5 907C");
        testEcb(key, "AC48 5624 2B12 1589", "85C6 B232 294C 2F27");
        testEcb(key, "CBE4 65F2 32F9 D85C", "B67A C767 C0C0 6A55");
        testEcb(key, "6C2E 3617 DA2B AC35", "B222 9067 630F 7045");
    }

    private static void testEcb (String hexKey, String hexIn, String hexOut) throws Exception{
        byte[] key = decodeHex(hexKey);
        byte[] in = decodeHex(hexIn);
        byte[] out = decodeHex(hexOut);

        Idea idea = new Idea(key, true);
        byte[] buf = in.clone();
        idea.crypt(buf, 0);
        if (!Arrays.equals(buf, out)){
            throw new Exception("Некорректное шифрование");
        }

        idea = new Idea(key, false);
        buf = out.clone();
        idea.crypt(buf, 0);
        if (!Arrays.equals(buf, in)){
            throw new Exception("Некорректное расшифрование");
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
