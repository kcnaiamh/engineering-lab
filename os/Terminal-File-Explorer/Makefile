COM = gcc
CFLAGS =

all: main_program

main_program: main.o copy.o cut.o delete.o explore.o help.o rename.o
	$(COM) $(CFLAGS) main.o copy.o cut.o delete.o explore.o help.o rename.o -o fff

%.o: %.c
	$(COM) $(CFLAGS) -c $^

clean:
	rm *.o *.out