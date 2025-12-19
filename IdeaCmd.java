package IDEA_TEST;

public class IdeaCmd{
    public static void main (String[] args) throws Exception{
        if (args.length == 0){
            System.out.println("Синтаксис: [-e - шифрование | -d - расшифрование] -k пароль имя_входного_файла имя_выходного_файла");
            return;
        }
        if (args.length != 7){
            throw new Exception("Не правильное количество аргументов");
        }
        boolean encrypt;
        if ("-e".equals(args[0])){
            encrypt = true;
        } else if ("-d".equals(args[0])){
            encrypt = false;
        } else {
            throw new Exception("Первым аргументом командной строки дожно быть: -e - шифрование или -d - расшифрование");
        }
        String cryptMode = args[2];
        String charKey = args[4];
        String inputFileName = args[5];
        String outputFileName = args[6];
        if (cryptMode.compareTo("OFB") == 0) {
            IdeaFileEncryption.cryptFile(inputFileName, outputFileName, charKey, encrypt, IdeaFileEncryption.Mode.OFB);
        } else {
            IdeaFileEncryption.cryptFile(inputFileName, outputFileName, charKey, encrypt, IdeaFileEncryption.Mode.ECB);
        }
    }
}

// -e -r ECB -k "MySecretKey" test.txt crypt.txt
// -d -r ECB -k "MySecretKey" crypt.txt result.txt
// -e -r OFB -k "MySecretKey" test.txt crypt.txt
// -d -r OFB -k "MySecretKey" crypt.txt result.txt