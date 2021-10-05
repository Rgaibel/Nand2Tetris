// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.



	//initialize product with 0 in register 2

	@R2 //mem. location R2

	M=0 //store product in R2 starting at 0
	
	

	//initialize i with value in register 0
	
	@R0
	
	D=M
	
	@i //some mem. loc.

	M=D //i = value in R0
	
	
	
(LOOP)
	//if i <= 0 goto END
	
	@i

	D=M //D=i

	@END

	D;JLE
	
	
	
	//loop through adding value in register 1 to value in register 2 and decremening value in i
	
	@R1 //mem. loc. R1

	D=M //D=R1

	@R2 //mem. loc. R2

	M=M+D //M=R2+R1 adds R1 to R2 each iteration to perform multiplication

	@i //mem. loc. i

	M=M-1 //Decrement value at i

	@LOOP //unconditional goto LOOP

	0;JMP //unconditional jump
	
	

(END)

	//Terminate Program
	
	@END
	
	0;JMP