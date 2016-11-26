# 
# linux
#

.SUFFIXES:  .cpp .o

#OS_BINEXT   = .exe
CXX         = g++
OS_CXXOPTS  = -pthread -D_GNU_SOURCE
#DDEBUG      = -g -DDEBUG
CXXOPTS     = $(OS_CXXOPTS) $(DDEBUG) -I../include -Wall -O2

OBJS = main.o HexagonGraph.o Random.o common.o

.cpp.o:
	$(CXX) -c $(CXXOPTS) $<

all: mobilclient$(OS_BINEXT)

mobilclient$(OS_BINEXT): $(OBJS) ../lib/libptypes.a 
	$(CXX) $(CXXOPTS) $(OBJS) ../lib/libptypes.a -o $@

main.o: main.cpp include.h

HexagonGraph.o: HexagonGraph.cpp include.h

Random.o: Random.cpp include.h

common.o: common.cpp include.h

clean:
	rm -f *.o
	rm -f core *.core
	rm -f mobilclient$(OS_BINEXT)
