// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
	
(BEGIN)

	@SCREEN //SCREEN address 16384
	
	D=A-1 //store screen - 1 address in D
	
	@s
	
	M=D// store SCREEN address in s

	@KBD //KBD address 24576
	
	D=M //store KBD address in D
	
	@CLEAR
	
	D;JEQ //jump to CLEAR loop if not pressing anything
	
(DRAW)
	
	D=-1 //black pixel

	@s
	
	A=M
	
	M=D
	
	@s
	
	M=M+1 //increment s to next pixel
	
	@KBD //check if we're at the end of screen buffer
	
	D=A
	
	@s // if we're at the end of the screen 's' will be the value at KBD address
	
	D=D-M
	
	@DRAW
	
	D;JGT // if we haven't filled the whole screen 's' is less than the value of KBD so we will keep drawing
	
	@BEGIN
	
	0;JMP//go back to beginning if done drawing
	
	
(CLEAR)

	D=0 //white pixel

	@s
	
	A=M
	
	M=D
	
	@s
	
	M=M+1 //increment s to next pixel
	
	@KBD //check if we're at the end of screen buffer
	
	D=A
	
	@s // if we're at the end of the screen 's' will be the value at KBD address
	
	D=D-M
	
	@CLEAR
	
	D;JGT // if we haven't filled the whole screen 's' is less than the value of KBD so we will keep drawing
	
	@BEGIN
	
	0;JMP//go back to beginning if done drawing

















