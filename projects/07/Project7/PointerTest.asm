
@3030
D=A
@SP
A=M
M=D
@SP
M=M+1

@SP
M=M-1
@SP
A=M
D=M
@THIS
M=D

@3040
D=A
@SP
A=M
M=D
@SP
M=M+1

@SP
M=M-1
@SP
A=M
D=M
@THAT
M=D

@32
D=A
@SP
A=M
M=D
@SP
M=M+1

@THIS
D=M
@2
D=D+A
@addr
M=D
@SP
M=M-1
@SP
A=M
D=M
@addr
A=M
M=D

@46
D=A
@SP
A=M
M=D
@SP
M=M+1

@THAT
D=M
@6
D=D+A
@addr
M=D
@SP
M=M-1
@SP
A=M
D=M
@addr
A=M
M=D

@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1

@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1

@THIS
D=M
@2
A=A+D
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M-D
@SP
M=M+1

@THAT
D=M
@6
A=A+D
D=M
@SP
A=M
M=D
@SP
M=M+1

@SP
M=M-1
@SP
A=M
D=M
@SP
M=M-1
A=M
M=M+D
@SP
M=M+1
