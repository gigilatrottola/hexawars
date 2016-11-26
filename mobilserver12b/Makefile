# 
# Linux
#

.SUFFIXES:  .cxx .cpp .o

#OS_BINEXT   = .exe
CXX         = g++
OS_CXXOPTS  = -pthread -D_GNU_SOURCE
#DDEBUG      = -g -DDEBUG
#DDEBUG      = -DDEBUG
CXXOPTS     = $(OS_CXXOPTS) $(DDEBUG) -I../include -I/usr/include/mysql -Wall -O2
BINDEST     = ../bin


OBJS =  wshare.o request.o clients.o sysutils.o urlutils.o log.o \
	mimetable.o config.o utils.o modules.o \
	mod_file.o mod_wstat.o mod_about.o \
	mod_mobil.o Settings.o LogTail.o \
	GameData.o GameManager.o HexagonGraph.o \
	QuizGameData.o QuizGameManager.o \
	QuizGraph.o Random.o \
	Sessions.o SessionUser.o WaitingQueues.o \
	HexaWarGameData.o HexaWarGameManager.o \
	SQL.o common.o \
	pmysql.o

.cxx.o:
	$(CXX) -c $(CXXOPTS) $<

.cpp.o:
	$(CXX) -c $(CXXOPTS) $<


all: mobilserver$(OS_BINEXT)


mobilserver$(OS_BINEXT): $(OBJS) ../lib/libptypes.a /usr/lib/x86_64-linux-gnu/libmysqlclient.a
	$(CXX) $(CXXOPTS) $(OBJS) ../lib/libptypes.a /usr/lib/x86_64-linux-gnu/libmysqlclient.a -lz -ldl -o $@
	mkdir -p $(BINDEST) ; cp $@ $(BINDEST)/
	strip $(BINDEST)/$@

#mobilserver$(OS_BINEXT): $(OBJS) ../lib/libptypes.a
#	$(CXX) $(CXXOPTS) $(OBJS) ../lib/libptypes.a -o $@
#	mkdir -p $(BINDEST) ; cp $@ $(BINDEST)/
#	strip $(BINDEST)/$@


wshare.o: wshare.cxx clients.h request.h sysutils.h urlutils.h log.h config.h

request.o: request.cxx request.h modules.h clients.h sysutils.h urlutils.h log.h config.h

clients.o: clients.cxx clients.h log.h config.h

sysutils.o: sysutils.cxx sysutils.h utils.h

urlutils.o: urlutils.cxx urlutils.h

utils.o: utils.cxx utils.h sysutils.h urlutils.h config.h

log.o: log.cxx log.h config.h

config.o: config.cxx config.h sysutils.h

mimetable.o: mimetable.cxx config.h

modules.o: modules.cxx modules.h request.h

mod_file.o: mod_file.cxx config.h sysutils.h utils.h request.h clients.h

mod_wstat.o: mod_wstat.cxx config.h utils.h request.h clients.h

mod_about.o: mod_about.cxx config.h utils.h request.h

mod_mobil.o: mod_mobil.cxx config.h utils.h request.h log.h include.h

#mod_getpost.o: mod_getpost.cxx config.h utils.h request.h log.h include.h

pmysql.o: pmysql.cxx pmysql.h

GameData.o: GameData.cpp include.h

GameManager.o: GameManager.cpp include.h

HexagonGraph.o: HexagonGraph.cpp include.h

QuizGameData.o: QuizGameData.cpp include.h

QuizGameManager.o: QuizGameManager.cpp include.h

QuizGraph.o: QuizGraph.cpp include.h templates.hpp

Random.o: Random.cpp include.h

Settings.o: Settings.cpp include.h

LogTail.o: LogTail.cpp include.h

Sessions.o: Sessions.cpp include.h

SessionUser.o: SessionUser.cpp include.h

WaitingQueues.o: WaitingQueues.cpp include.h

HexaWarGameData.o: HexaWarGameData.cpp include.h

HexaWarGameManager.o: HexaWarGameManager.cpp include.h

SQL.o: SQL.cpp include.h

common.o: common.cpp include.h


clean: clean-src
	rm -f $(BINDEST)/mobilserver$(OS_BINEXT)

clean-src:
	rm -f *.o
	rm -f core *.core
	rm -f mobilserver$(OS_BINEXT)
