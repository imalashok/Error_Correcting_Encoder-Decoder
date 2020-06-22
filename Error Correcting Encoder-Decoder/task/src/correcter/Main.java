package correcter;

import java.util.Random;
import java.util.Scanner;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        File FileOriginal = new File("send.txt");
        File FileReceived = new File("received.txt");
        File FileEncoded = new File("encoded.txt");
        File FileDecoded = new File("decoded.txt");
        byte[] byteArray;
        StringBuilder encodedText = new StringBuilder();
        StringBuilder parityText = new StringBuilder();
        StringBuilder corruptedText = new StringBuilder();
        StringBuilder decodedText = new StringBuilder();

        System.out.print("Write a mode: ");
        switch (new Scanner(System.in).nextLine()) {
            case "encode":
                try (FileInputStream inputStream = new FileInputStream(FileOriginal)) {
                    byteArray = inputStream.readAllBytes();
                    encodedText = encodeBinaryTextHamming(byteArray);
                    //encodedText = encodeBinaryText(byteArray);
                    //parityText = updateBinaryStringWithParityBits(encodedText);
                    //saveStringBuilderToFile(parityText, FileEncoded);
                    saveStringBuilderToFile(encodedText, FileEncoded);
                } catch (IOException e) {
                    System.out.println("Wrong input file.");
                }
                break;
            case "send":
                try (FileInputStream inputStream = new FileInputStream(FileEncoded)) {
                    byteArray = inputStream.readAllBytes();
                    parityText = convertToBinaryText(byteArray);
                    corruptedText = simulateBinaryCorruptionTransmission(parityText);
                    saveStringBuilderToFile(corruptedText, FileReceived);
                } catch (IOException e) {
                    System.out.println("Wrong Encoded text file.");
                }
                break;
            case "decode":
                try (FileInputStream inputStream = new FileInputStream(FileReceived)) {
                    byteArray = inputStream.readAllBytes();
                    corruptedText = convertToBinaryText(byteArray);
                    //decodedText = decodeBinaryCorruptedText(corruptedText);
                    decodedText = decodeBinaryCorruptedTextHamming(corruptedText);
                    saveStringBuilderToFile(decodedText, FileDecoded);
                } catch (IOException e) {
                    System.out.println("Wrong Received text file.");
                }
                break;
            default:
                System.out.println("Wrong command");
                break;
        }
    }

    public static StringBuilder convertToBinaryText(byte[] byteArray) {
        StringBuilder stringBinaryOriginal = new StringBuilder();
        for (byte currentByte : byteArray) {
            stringBinaryOriginal.append(String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0')).append(" ");
        }
        return stringBinaryOriginal;
    }

    public static StringBuilder encodeBinaryText(byte[] byteArray) {
        StringBuilder stringBinaryEncoded = new StringBuilder();
        int cnt = 0;
        for (byte currentByte : byteArray) {
            StringBuilder stringBinaryOriginal = new StringBuilder(String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0'));
            for (int i = 0; i < stringBinaryOriginal.length(); i++) {
                if (cnt >= 6) {
                    cnt = 0;
                    stringBinaryEncoded.append(".. ");
                }
                stringBinaryEncoded.append(stringBinaryOriginal.charAt(i)).append(stringBinaryOriginal.charAt(i));
                cnt += 2;
            }
        }

        int countOfAdditionalDots = 8 - stringBinaryEncoded.length() % 9;

        stringBinaryEncoded.append(".".repeat(countOfAdditionalDots));

        return stringBinaryEncoded;
    }

    public static StringBuilder encodeBinaryTextHamming(byte[] byteArray) {
        StringBuilder stringBinaryEncoded = new StringBuilder();
        for (byte currentByte : byteArray) {
            StringBuilder stringBinaryOriginal = new StringBuilder(String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0'));

            int p11 = (Integer.parseInt(stringBinaryOriginal.substring(0, 1)) + Integer.parseInt(stringBinaryOriginal.substring(1, 2)) + Integer.parseInt(stringBinaryOriginal.substring(3, 4))) % 2 == 0 ? 0 : 1;
            int p12 = (Integer.parseInt(stringBinaryOriginal.substring(0, 1)) + Integer.parseInt(stringBinaryOriginal.substring(2, 3)) + Integer.parseInt(stringBinaryOriginal.substring(3, 4))) % 2 == 0 ? 0 : 1;
            int p14 = (Integer.parseInt(stringBinaryOriginal.substring(1, 2)) + Integer.parseInt(stringBinaryOriginal.substring(2, 3)) + Integer.parseInt(stringBinaryOriginal.substring(3, 4))) % 2 == 0 ? 0 : 1;
            int p21 = (Integer.parseInt(stringBinaryOriginal.substring(4, 5)) + Integer.parseInt(stringBinaryOriginal.substring(5, 6)) + Integer.parseInt(stringBinaryOriginal.substring(7, 8))) % 2 == 0 ? 0 : 1;
            int p22 = (Integer.parseInt(stringBinaryOriginal.substring(4, 5)) + Integer.parseInt(stringBinaryOriginal.substring(6, 7)) + Integer.parseInt(stringBinaryOriginal.substring(7, 8))) % 2 == 0 ? 0 : 1;
            int p24 = (Integer.parseInt(stringBinaryOriginal.substring(5, 6)) + Integer.parseInt(stringBinaryOriginal.substring(6, 7)) + Integer.parseInt(stringBinaryOriginal.substring(7, 8))) % 2 == 0 ? 0 : 1;

            stringBinaryEncoded.append(p11)
                            .append(p12)
                            .append(stringBinaryOriginal.charAt(0))
                            .append(p14)
                            .append(stringBinaryOriginal.substring(1, 4))
                            .append("0 ")
                            .append(p21)
                            .append(p22)
                            .append(stringBinaryOriginal.charAt(4))
                            .append(p24)
                            .append(stringBinaryOriginal.substring(5, 8))
                            .append("0 ");
        }
        return  stringBinaryEncoded;
    }

    public static StringBuilder updateBinaryStringWithParityBits(StringBuilder stringBuilder) {
        StringBuilder stringBinaryEncodedWithParity = new StringBuilder(stringBuilder);
        int numberOfBytesInString = (stringBuilder.length() + 1) / 9;
        int i = 0;

        //fill the last byte with unused '0' bits.
        if (numberOfBytesInString > 8) {
            if (stringBinaryEncodedWithParity.charAt(stringBuilder.length() - 6) == '.') {
                stringBinaryEncodedWithParity.replace( stringBuilder.length() - 6, stringBuilder.length() - 2, "0000");
            }
            if (stringBinaryEncodedWithParity.charAt(stringBuilder.length() - 4) == '.') {
                stringBinaryEncodedWithParity.replace(stringBuilder.length() - 4, stringBuilder.length() - 2, "00");
            }
        }

        for (int j = 0; j < numberOfBytesInString; j++) {
            int parity = Integer.parseInt(stringBinaryEncodedWithParity.substring(i, i + 1)) ^
                         Integer.parseInt(stringBinaryEncodedWithParity.substring(i + 2, i + 3)) ^
                         Integer.parseInt(stringBinaryEncodedWithParity.substring(i + 4, i + 5));

            stringBinaryEncodedWithParity.replace(i + 6, i + 7, Integer.toString(parity));
            stringBinaryEncodedWithParity.replace(i + 7, i + 8, Integer.toString(parity));
            i += 9;
        }
        return stringBinaryEncodedWithParity;
    }

    public static StringBuilder simulateBinaryCorruptionTransmission(StringBuilder stringBuilder) {
        Random random = new Random();
        StringBuilder corruptedString = new StringBuilder();

        for (String byteString : stringBuilder.toString().split(" ")) {
            StringBuilder byteStringBuilder = new StringBuilder(byteString);
            int index = random.nextInt(8);
            if (byteStringBuilder.charAt(index) == '0') {
                byteStringBuilder.replace(index, index + 1, "1");
            } else {
                byteStringBuilder.replace(index, index + 1, "0");
            }
            corruptedString.append(byteStringBuilder).append(" ");
        }
        corruptedString.deleteCharAt(corruptedString.length() - 1);

        return corruptedString;
    }

    public static StringBuilder decodeBinaryCorruptedText(StringBuilder stringBuilder) {
        StringBuilder fixedString = new StringBuilder();
        StringBuilder decodedString = new StringBuilder();

        for (String byteString : stringBuilder.toString().split(" ")) {
            StringBuilder byteStringBuilder = new StringBuilder(byteString);

            if (byteStringBuilder.charAt(0) != byteStringBuilder.charAt(1)) {
                if (byteStringBuilder.charAt(7) == '0') {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 0) {
                        byteStringBuilder.replace(0, 2, "00");
                    } else if (Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 2) {
                        byteStringBuilder.replace(0, 2, "00");
                    } else {
                        byteStringBuilder.replace(0, 2, "11");
                    }
                } else {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 1) {
                        byteStringBuilder.replace(0, 2, "00");
                    } else {
                        byteStringBuilder.replace(0, 2, "11");
                    }
                }
            } else if (byteStringBuilder.charAt(2) != byteStringBuilder.charAt(3)) {
                if (byteStringBuilder.charAt(7) == '0') {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 0) {
                        byteStringBuilder.replace(2, 4, "00");
                    } else if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 2) {
                        byteStringBuilder.replace(2, 4, "00");
                    } else {
                        byteStringBuilder.replace(2, 4, "11");
                    }
                } else {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(4, 5)) == 1) {
                        byteStringBuilder.replace(2, 4, "00");
                    } else {
                        byteStringBuilder.replace(2, 4, "11");
                    }
                }
            } else if (byteStringBuilder.charAt(4) != byteStringBuilder.charAt(5)) {
                if (byteStringBuilder.charAt(7) == '0') {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) == 0) {
                        byteStringBuilder.replace(4, 6, "00");
                    } else if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) == 2) {
                        byteStringBuilder.replace(4, 6, "00");
                    } else {
                        byteStringBuilder.replace(4, 6, "11");
                    }
                } else {
                    if (Integer.parseInt(byteStringBuilder.toString().substring(0, 1)) + Integer.parseInt(byteStringBuilder.toString().substring(2, 3)) == 1) {
                        byteStringBuilder.replace(4, 6, "00");
                    } else {
                        byteStringBuilder.replace(4, 6, "11");
                    }
                }
            }
            fixedString.append(byteStringBuilder).append(" ");
        }
        fixedString.deleteCharAt(fixedString.length() - 1);

        decodedString = decodeBinaryText(fixedString);
        return decodedString;
    }

    public static StringBuilder decodeBinaryText(StringBuilder stringBuilder) {
        StringBuilder temp = new StringBuilder();
        StringBuilder temp2 = new StringBuilder();
        StringBuilder decodedString = new StringBuilder();
        int numberOfBytesInString = (stringBuilder.length() + 1) / 9;
        int cnt = 0;

        if (numberOfBytesInString > 0) {
            for (int i = 0; i < numberOfBytesInString; i++) {
                temp.append(stringBuilder.substring(cnt, cnt + 6));
                cnt += 9;
            } // deletes parity bits and spaces

            for (int i = 0; i < temp.length(); i += 2) {
                temp2.append(temp.charAt(i));
            } // takes one unique bit from duplicate pair of bits

            for (int j = 0, index = 0; j < temp2.length() / 8; j++, index += 8) {
                decodedString.append(temp2.substring(index, index + 8)).append(" ");
            } //splits 8-bits groups
        }
        return decodedString;
    }

    public static StringBuilder decodeBinaryCorruptedTextHamming(StringBuilder stringBuilder) {
        StringBuilder tempString = new StringBuilder();
        StringBuilder decodedString = new StringBuilder();

        for (String byteWord: stringBuilder.toString().trim().split("\\s+")) {
            int numberOfParityErrors = 0;
            int sumOfParityIndexes = 0;
            StringBuilder temp = new StringBuilder(byteWord);
            // byte = (p1, p2, d1, p4, d5, d6, d7, 0) = 8 bits
            int p1 = (Integer.parseInt(byteWord.substring(2, 3)) + Integer.parseInt(byteWord.substring(4, 5)) + Integer.parseInt(byteWord.substring(6, 7))) % 2 == 0 ? 0 : 1;
            int p2 = (Integer.parseInt(byteWord.substring(2, 3)) + Integer.parseInt(byteWord.substring(5, 6)) + Integer.parseInt(byteWord.substring(6, 7))) % 2 == 0 ? 0 : 1;
            int p4 = (Integer.parseInt(byteWord.substring(4, 5)) + Integer.parseInt(byteWord.substring(5, 6)) + Integer.parseInt(byteWord.substring(6, 7))) % 2 == 0 ? 0 : 1;

            if (p1 != Integer.parseInt(byteWord.substring(0, 1))) {
                numberOfParityErrors++;
                sumOfParityIndexes += 1;
            }
            if (p2 != Integer.parseInt(byteWord.substring(1, 2))) {
                numberOfParityErrors++;
                sumOfParityIndexes += 2;
            }
            if (p4 != Integer.parseInt(byteWord.substring(3, 4))) {
                numberOfParityErrors++;
                sumOfParityIndexes += 4;
            }

            if (numberOfParityErrors > 1) {
                int bitAtErrorIndex = Integer.parseInt(temp.substring(sumOfParityIndexes - 1, sumOfParityIndexes));
                temp.replace(sumOfParityIndexes - 1, sumOfParityIndexes, Integer.toString(Math.abs(bitAtErrorIndex - 1)));
            }

            tempString.append(temp.substring(2, 3)).append(temp.substring(4, 7));
        }

        for (int j = 0, index = 0; j < tempString.length() / 8; j++, index += 8) {
            decodedString.append(tempString.substring(index, index + 8)).append(" ");
        } //splits 8-bits groups

        return decodedString;
    }

    public static void saveStringBuilderToFile(StringBuilder stringBuilder, File outputFile) {
        int numberOfBytesInString = (stringBuilder.length() + 1) / 9;
        int index = 0;

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            for (int i = 0; i < numberOfBytesInString; i++) {
                outputStream.write(Integer.parseInt(stringBuilder.toString().substring(index, index + 8), 2));
                index += 9;
            }
        } catch (IOException e) {
            System.out.println("Wrong output file.");
        }
    }
}