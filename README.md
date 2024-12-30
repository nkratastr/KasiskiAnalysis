# Vigenère Cipher Cryptanalysis Tool

A Java-based implementation of the Kasiski examination method for breaking Vigenère ciphers. This tool combines pattern analysis and Index of Coincidence to determine the most probable key length and decrypt the ciphertext.

## Features

- Automated key length detection using Kasiski examination
- Index of Coincidence validation
- Pattern frequency analysis
- Weighted scoring system (60% pattern probability, 40% IoC)
- Interactive command-line interface
- Support for multiple attempts with different key lengths

## Usage

1. Run the program
2. Input your ciphertext (paste and press Enter twice)
3. Review the analysis results
4. If not satisfied with the decryption, try alternative key lengths

## Sample Analysis

### Example Input
```
Ciphertext:
PPQCAXQVEKGYBNKMAZUYBNGBALJONITSZMJYIMVRAGVOHTVRAUCTKSGDDWUOXITLAZUVAVVRAZCVKBQPIWPOU
LWPLKKQLSOKLVVAKBXKKBLLFPRVQKKLXZKVQIXPKLWPVXZTRGPKTHLXIUVZZCKVRVVFNAPCHBOUODHSGPKTC
MZXPISLAZUVAVVRAZCVKBQPIWKKKUYTGPKTCMZXPISLAZUVAVVRAZCVKBQPIWKKK
```

### Analysis Results
```
Key Length: 5 (Score: 92.45%)
Found Key: CIPHER
```

### Decrypted Output
```
Plaintext:
THEVIGINEREENCRYPTIONMETHODISAPOLYALPHABETICSUBSTITUTIONCIPHERTHATUSESADIFFERENTALPHABETF
OREACHCHARACTERINTHEENCRYPTIONKEYTHESEQUENCEOFSUBSTITUTIONALPHAPETSISDEPENDENTONTHEKEYAN
DONCETHECIPHERISBROKENTHEENTIREPLAINTEXTCANBERECOVERED
```

This example demonstrates the successful decryption of a Vigenère-encrypted message about the cipher itself. The program correctly identified the key length of 5 and found the key "CIPHER", revealing the original text that describes the Vigenère encryption method.

## References & Citations

- **Vigenère Cipher**: Developed by Blaise de Vigenère in the 16th century. First published in the book "Traicté des Chiffres" (1586). [Read more](https://en.wikipedia.org/wiki/Vigen%C3%A8re_cipher)

- **Kasiski Examination**: First published by Friedrich Kasiski in his 1863 book "Die Geheimschriften und die Dechiffrir-Kunst". This method reveals the length of the encryption key by analyzing repeated patterns in the ciphertext. [Read more](https://en.wikipedia.org/wiki/Kasiski_examination)

- **Index of Coincidence**: Developed by William F. Friedman in 1920. Published in "The Index of Coincidence and Its Applications in Cryptography". Used to measure the variation in letter frequencies and determine the key length. [Read more](https://en.wikipedia.org/wiki/Index_of_coincidence)

## Project Structure

- `src/`: Source code files
  - `App.java`: Main application
  - `service/`: Analysis and decryption logic
  - `entity/`: Data models
  - `interfaces/`: Abstractions
- `bin/`: Compiled class files

## Dependencies

- Java SE 8 or higher

## License

MIT License - Feel free to use and modify as needed.
