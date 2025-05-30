# Huffman file compressor
## Description
This project was part of a problem set for Dartmouth's COSC10 course, "Problem Solving via Object-Oriented Programming." It uses Huffman encoding/binary trees in order to compress and decompress text files losslessly. The program was able to compress a 3.2MB text file of "War and Peace" to a lossless 1.8MB.
## How It Works
The compressor works in typical Huffman encoding style, where a binary tree is created to order all of the characters in the text file in order of frequency, and then code the characters in their position in the tree such that the most frequent characters get the shortest encoding.
