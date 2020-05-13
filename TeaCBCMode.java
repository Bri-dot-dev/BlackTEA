package Brian.TeaCBC;

import javax.swing.*;
import java.sql.Driver;

public class TeaCBCMode{

    static int sum = 0x0; //setting sum value to hex 0
    static int delta = 0x9e3779b9; //Constant Variable Delta (apart of TEA algorithm)
    private int key[]; // empty array being stored with users md5 hash

    // key is null when application is opened
    public TeaCBCMode() {
        this.key = null;
    }
    // user key is being taken in as the key for tea algorithm (md5 hash)
    public void setKey (String keyStr) {
        this.key = charArrToIntArr(strToCharArr(keyStr));
    }

    public String stringDecrypt(String inputStr, int [] lastBlock) {
        char[] inputCharArr = strToCharArr(inputStr);
        int[] inputIntArr = charArrToIntArr(inputCharArr);
        int[] decryptedIntArr = intArrDecrypt(inputIntArr, lastBlock);
        char[] decryptedCharArr = intArrToCharArr(decryptedIntArr);
        String decryptedString = charArrToStr(decryptedCharArr);
        // decrypted string is returned
        return decryptedString;
    }
    // Encryption method for integer array
    public int [] intArrDecrypt (int[] encryptedIntArr, int []lastBlock) {
        int[] buffer = new int[encryptedIntArr.length];
        for(int i=0; i < encryptedIntArr.length; i+= 2) {
            int [] encryptedInts = {encryptedIntArr[i], encryptedIntArr[i+1]};
            int [] decryptedTwo = twoIntDecrypt(encryptedInts, lastBlock);
            buffer[i] = decryptedTwo[0];
            buffer[i+1] = decryptedTwo[1];
        }
        int[] decryptedArr;
        int i;
        if (buffer[0] == 0) {
            decryptedArr = new int[encryptedIntArr.length - 1];
            i = 1;
        } else {
            decryptedArr = new int[encryptedIntArr.length];
            i = 0;
        }
        for (int j=0; j < decryptedArr.length; j++) {
            decryptedArr[j] = buffer[i];
            i++;
        }
        // giving back the generated decrypted array
        return decryptedArr;
    }

    // encrypting of the string
    public String stringEncrypt(String inputStr, int [] lastBlock) {
        // setting array of characters to string of characters and taking input string
        char [] inputCharArr = strToCharArr(inputStr);
        // setting array of characters to string of characters array and taking input characters array
        int [] inputIntArr = charArrToIntArr(inputCharArr);
        // setting encrypted array of integers to integer array (encrypt) and taking input array and previous block
        int [] encryptedIntArr = intArrEncrypt(inputIntArr, lastBlock);
        // setting encrypted array of chars now to integer array taking integer array (encrypt)
        char [] encryptedCharArr = intArrToCharArr(encryptedIntArr);
        // finally returning the character array array taking encrypted array of characters
        return charArrToStr(encryptedCharArr);
    }

    // helper method Taking in output array and converting to character array
    private char[] strToCharArr(String str) {
        char[] outputArr = new char[str.length()];
        for (int i=0; i<str.length(); i++) {
            outputArr[i] = str.charAt(i);
        }
        // return array
        return outputArr;
    }
    // helper method for character array to string
    private String charArrToStr(char[] charArr) {
        return new String(charArr);
    }
    // Encryption method for integer array
    public int [] intArrEncrypt (int[] intArr, int []lastBlock) {
        int[] encryptedArr;
        int i;
        int j;
        // splitting
        if (intArr.length % 2 == 0) {
            encryptedArr = new int[intArr.length];
            j = 0;
            i = 0;
        } else {
            encryptedArr = new int[intArr.length + 1];
            int [] plainInts = {0, intArr[0]};
            int [] encryptedTwo = twoIntEncrypt(plainInts, lastBlock);
            encryptedArr[0] = encryptedTwo[0];
            encryptedArr[1] = encryptedTwo[1];
            i = 1;
            j = 2;
        }
        while (i < intArr.length) {
            int [] plainInts = {intArr[i], intArr[i+1]};
            int [] encryptedTwo = twoIntEncrypt(plainInts, lastBlock);
            encryptedArr[j] = encryptedTwo[0];
            encryptedArr[j+1] = encryptedTwo[1];
            i += 2;
            j += 2;
        }
        // giving back final encrypted array
        return encryptedArr;
    }

    //Encryption Method - using tea with cbc mode
    public int[] twoIntEncrypt(int[] plainText, int[] lastBlock) {
        // Handling Errors With Key Input
        if (key == null) {
            // Handling errors for no key input
            String keyError = "Key is not defined!";
            JOptionPane.showMessageDialog(new JFrame(), keyError, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);

        }
        // XOR the textBlock with the previously encrypted textBlock - CBC MODE FUNCTION
        int l = plainText[0] ^ lastBlock[0];
        int r = plainText[1] ^ lastBlock[1];
        // setting sum to zero
        sum = 0;

        for (int i = 0; i < 32; i++) {

            sum += delta;

            l += ((r << 4) + key[0]) ^ (r + sum) ^ ((r >> 5) + key[1]);

            r += ((l << 4) + key[2]) ^ (l + sum) ^ ((l >> 5) + key[3]);

        }
        int textBlock[] = new int[2];
        textBlock[0] = l;
        textBlock[1] = r;

        return textBlock;
    }

    // Decryption Method - using tea with cbc mode
    public int[] twoIntDecrypt(int[] cipherText, int lastBlock[]) {
      // Handling errors for no key input
        if (key == null) {
            String keyError = "Key is not defined!";
            JOptionPane.showMessageDialog(new JFrame(), keyError, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        // Diving the block into left and right sub blocks
        int l = cipherText[0];
        int r = cipherText[1];

        sum = delta << 5; // initialize the sum variable

        for (int i = 0; i < 32; i++) {

            r -= ((l << 4) + key[2]) ^ (l + sum) ^ ((l >> 5) + key[3]);

            l -= ((r << 4) + key[0]) ^ (r + sum) ^ ((r >> 5) + key[1]);
            sum -= delta;
        }
        // XOR the result of TEA Algorithm with the previous block - CBC MODE FUNCTION
        int block[] = new int[2];
        block[0] = l ^ lastBlock[0];
        block[1] = r ^ lastBlock[1];

        return block;
    }

    // calculating the array of characters to integer array - helper method
    public int[] charArrToIntArr(char[] charArr) {
        int intArrLen = (int)Math.ceil((double)charArr.length / 4);
        int[] intArr = new int[intArrLen];
        // Calculating number of padding bytes needed using javas ternary operator - 4 bytes
        int numPaddingBytes = charArr.length % 4 != 0 ? 4 - charArr.length % 4 : 0;
        String binaryStr = "";
        int i = 0;
        while (i < numPaddingBytes) {
            // set the binary string with padded zeros
            binaryStr += "00000000";
            i++;
        }
        while (i < numPaddingBytes + charArr.length) {
            if ((i % 4 == 0) && (! binaryStr.equals(""))) {
                intArr[i / 4 - 1] = Integer.parseUnsignedInt(binaryStr, 2);
                binaryStr = "";
            }
            // Add to String
            String unpaddedStr_i = Integer.toBinaryString(charArr[i - numPaddingBytes]);
            binaryStr += padBinaryCharStr(unpaddedStr_i);

            // Increment i
            i++;
        }
        // Add last
        intArr[intArrLen - 1] = Integer.parseUnsignedInt(binaryStr, 2);
        // returning the array of integers
        return intArr;

    }
    //Convert int array to an array of characters - helper method
    public char[] intArrToCharArr(int[] intArr) {
        // Determine starting point of non empty byte
        int i = 0;
        boolean found = false;
        int startingIndex = 0;
        String firstIntStr = padBinaryIntStr(Integer.toBinaryString(intArr[0]));
        while ((i < 32) && (! found)) {
            // System.out.println("char_" + i + " = " + firstIntStr.charAt(i));
            if (firstIntStr.charAt(i) != '0') {
                found = true;
                // System.out.println("found when i=" + i);
                startingIndex = i - i % 8;
            }
            // Increment i
            i++;
        }
        // storing in big string value
        String bigString = "";
        for (int myInt : intArr) {
            bigString += padBinaryIntStr(Integer.toBinaryString(myInt));
        }
        char[] charArr = new char[intArr.length * 4 - startingIndex / 8];
        i = startingIndex;
        int j = 0;
        while (i < bigString.length()) {
            // Take substring and convert to char
            // System.out.println("this substr = " + bigString.substring(i, i + 8));
            charArr[j] = (char) Integer.parseUnsignedInt(bigString.substring(i, i + 8), 2);

            // Increment i and j
            i += 8;
            j++;
        }
        // giving the character array
        return charArr;
    }

    // Handling padding issues - padding binary character string
    String padBinaryCharStr(String unpaddedCharStr) {
        // System.out.println("unpadded=" + unpaddedCharStr);
        String paddedCharStr = "";
        for (int i=0; i<(8 - unpaddedCharStr.length()); i++) {
            paddedCharStr += "0";
        }
        paddedCharStr += unpaddedCharStr;
        // giving back the padded value
        return paddedCharStr;
    }

    // Handling padding with integer string
    String padBinaryIntStr(String unpaddedIntStr) {
        // System.out.println("unpadded=" + unpaddedIntStr);
        String paddedIntStr = "";
        // accounting for 32 bits
        for (int i=0; i<(32 - unpaddedIntStr.length()); i++) {
            paddedIntStr += "0";
        }
        paddedIntStr += unpaddedIntStr;
        // giving back the padded value
        return paddedIntStr;
    }
}
