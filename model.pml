// Definition of Non-critical section.
inline non_critical_section() {
  inNonCritical = 1; 
  /*   printf("MSC: %d in non-CS\n", _pid); */
  do                            /* non-deterministically choose how
                                   long to stay, even forever */
    :: true ->
         skip
    :: true ->
         break
  od ; 
  inNonCritical = 0 ; 
}

// Definition of Monitor
bool lock = false;

typedef Condition {
   bool gate;
   byte waiting;
}

inline enterMon() {
    atomic {
        !lock;
        lock = true; 
    }
}

inline leaveMon() {
   lock = false;
}

inline waitC(C) {
    atomic {
        C.waiting++;
        lock = false; /* Exit monitor */ 
        C.gate; /* Wait for gate */
        lock = true; /* IRR */
        C.gate = false; /* Reset gate */ 
        C.waiting--;
    }
}

inline signalC(C) {
    atomic {
        if
        /* Signal only if waiting */
        :: (C.waiting > 0) ->
            C.gate = true;
            !lock; /* IRR - wait for released lock */ 
            lock = true; /* Take lock again */
        :: else
        fi; 
    }
}

#define emptyC(C) (C.waiting == 0)

int readers = 0;
int compromiseReaders = 0;
int writers = 0;

int lockLow = 0;
int lockHigh = 9;

Condition WriteAccessOk;
Condition ReadWriteAccessOk;
Condition ReadAccessOk;
Condition SingleElementAccessOk;

inline exclusiveRead() {
    enterMon();
    if 
    :: (writers > 0) || !emptyC(WriteAccessOk) || !emptyC(ReadWriteAccessOk) -> waitC(ReadAccessOk);
    :: else -> skip;
    fi
    readers = readers + 1;
    signalC(ReadAccessOk);
    leaveMon();
}

inline leaveExclusiveRead() {
    enterMon();
    readers = readers - 1;
    if 
    ::  (readers == 0) && (compromiseReaders == 0)  ->  if
                                                        ::  emptyC(ReadWriteAccessOk)   ->  signalC(WriteAccessOk);
                                                        ::  else    -> signalC(ReadWriteAccessOk);
                                                        fi
    ::  else -> skip;
    fi
    leaveMon();
}

inline getElement(index) {
    enterMon();
    do 
    ::  (writers > 0) && (index >= lockLow && index <= lockHigh) ->  waitC(SingleElementAccessOk);
    ::  else -> break;
    od
    readSingleElement = 1;
    readSingleElement = 0;
    if
    ::  emptyC(SingleElementAccessOk) ->    skip;
    ::  else -> signalC(SingleElementAccessOk);
    fi
    leaveMon();
}

inline enterExclusiveWrite(isReader, low, high) {
    enterMon();
    if 
    ::  (isReader) ->   readers = readers - 1;
                        if 
                        :: (readers > 0) || (compromiseReaders == 0) || (writers > 0) -> waitC(ReadWriteAccessOk);
                        :: else -> skip;
                        fi
    ::  else -> if 
                ::  (readers > 0) || (compromiseReaders > 0) || (writers > 0) -> waitC(WriteAccessOk);
                ::  else  -> skip;
                fi
    fi
    lockLow = low;
    lockHigh = high;
    writers = writers + 1;
    leaveMon();
}

inline updateLockRange(low, high) {
    enterMon();
    lockLow = low;
    lockHigh = high;
    if 
    :: emptyC(SingleElementAccessOk) -> skip;
    :: else -> signalC(SingleElementAccessOk);
    fi
    leaveMon();
}

inline enterCompromiseRead() {
    enterMon();
    compromiseReaders = compromiseReaders + 1;
    leaveMon();
}

inline leaveCompromiseReaders() {
    enterMon();
    compromiseReaders = compromiseReaders - 1;
    if 
    :: readers == 0 && writers == 0 ->  if 
                                        ::  emptyC(ReadAccessOk) && (compromiseReaders == 0) -> signalC(WriteAccessOk)
                                        ::  else -> signalC(ReadAccessOk)
                                        fi
    :: else -> skip;
    fi
    leaveMon();
}

inline leaveExclusiveWrite() {
    enterMon();
    writers = writers - 1;
    lockLow = 0;
    lockHigh = 9;
    if 
    ::  emptyC(ReadAccessOk) && emptyC(SingleElementAccessOk) && (compromiseReaders == 0) -> signalC(WriteAccessOk);
    ::  else -> if
                :: emptyC(SingleElementAccessOk) -> signalC(ReadAccessOk)
                :: else -> signalC(SingleElementAccessOk)
                fi
    fi
    leaveMon();
}

// Can be called as a writer only.
inline writeElement() {
    writeSingleElement = 1;
    writeSingleElement = 0;
}


active [2] proctype Insert() {
    int insertElement;
    bit inNonCritical = 0;
    do 
    ::  non_critical_section();
        enterExclusiveWrite(false, 0, 9);
        insertElement = 1;
        updateLockRange(4, 9);
        insertElement = 0;
        leaveExclusiveWrite();  
    od
}

active [2] proctype Delete() {
    int searchDeleteIndex;
    int deleteElement;
    int index;
    bit inNonCritical = 0;
    do
    ::  non_critical_section();
        exclusiveRead();
        searchDeleteIndex = 1;
        searchDeleteIndex = 0;
        if 
        :: true -> index = 1;
        :: true -> index = 2;
        :: true -> index = 3;
        :: true -> index = 4;
        :: true -> index = 5;
        :: true -> index = 6;
        :: true -> index = 7;
        :: true -> index = 8;
        :: true -> index = 9;
        fi
        enterExclusiveWrite(true, index, index);
        deleteElement = 1;
        deleteElement = 0;
        leaveExclusiveWrite();
    od
}

active [2] proctype Print() {
    int readSingleElement;
    int index;
    bit inNonCritical = 0;
    do
    ::  non_critical_section();
        index = 0;
        do
        ::  (index < 10) ->     enterCompromiseRead();
                                getElement(index);
                                leaveCompromiseReaders();
                                index = index + 1;
        ::  else -> break;
        od
    od
}

active [2] proctype Search() {
    int searchElement;
    bit inNonCritical = 0;
    do
    ::  non_critical_section();
        exclusiveRead();
        searchElement = 1;
        searchElement = 0;
        leaveExclusiveRead();
    od
}

ltl insert_delete_mutex {[] !(Insert[0]:insertElement && Delete[2]:deleteElement) }
ltl insert_deletesearch_mutex {[] !(Insert[0]:insertElement && Delete[2]:searchDeleteIndex) }
ltl insert_search_mutex {[] !(Insert[0]:insertElement && Search[6]:searchElement)}

ltl delete_search_mutex {[] !(Delete[2]:deleteElement && Search[6]:searchElement)}
ltl deletesearch_search_mutex {[]<> (Delete[2]:searchDeleteIndex && Search[6]:searchElement)}
ltl deletesearch_print_mutex {[]<> (Delete[2]:searchDeleteIndex && Print[4]:readSingleElement)}

ltl print_insert_mutex {[] ((Print[4]:readSingleElement && Insert[0]:insertElement) -> (Print[4]:index < lockLow || Print[4]:index > lockHigh))}
ltl print_delete_mutex {[] ((Print[4]:readSingleElement && Delete[2]:deleteElement) -> (Print[4]:index != Delete[2]:index))}
ltl print_delete_interleave {[]<> (compromiseReaders == 1 && Delete[2]:deleteElement)}
ltl print_search_mutex {[]<> (Print[4]:readSingleElement && Search[6]:searchElement)}

ltl delete_mutex {[] !(Delete[2]:deleteElement && Delete[3]:deleteElement)}
ltl search_mutex {[]<> (Search[6]:searchElement && Search[7]:searchElement)}
ltl insert_mutex {[]! (Insert[0]:insertElement && Insert[1]:insertElement)}
ltl print_mutex {[]<> (Print[4]:readSingleElement && Print[5]:readSingleElement)}

ltl delete_ee {[](Delete[2]:inNonCritical -> <> Delete[2]:deleteElement)}
ltl search_ee {[](Search[6]:inNonCritical -> <> Search[6]:searchElement)}
ltl print_ee {[](Print[4]:inNonCritical -> <> Print[4]:readSingleElement)}
ltl insert_ee {[](Insert[0]:inNonCritical -> <> Insert[0]:insertElement)}

ltl writers {[] !(writers > 1)}
ltl readers {[]<> (readers > 1)}

ltl delete_goes_first { [] ((readers == 0 && ReadWriteAccessOk.waiting == 1 && WriteAccessOk.waiting == 1) -> 
(ReadWriteAccessOk.waiting == 1 && WriteAccessOk.waiting == 1) U (ReadWriteAccessOk.waiting == 0 && WriteAccessOk.waiting == 1))};
ltl delete_goes_first_2 { [] ((readers == 0 && ReadWriteAccessOk.waiting == 2 && WriteAccessOk.waiting == 1) -> 
(ReadWriteAccessOk.waiting == 2 && WriteAccessOk.waiting == 1) U (ReadWriteAccessOk.waiting == 1 && WriteAccessOk.waiting == 1))};
ltl delete_goes_first_3 { [] ((readers == 0 && ReadWriteAccessOk.waiting == 2 && WriteAccessOk.waiting == 2) -> 
(ReadWriteAccessOk.waiting == 2 && WriteAccessOk.waiting == 2) U (ReadWriteAccessOk.waiting == 1 && WriteAccessOk.waiting == 2))};

ltl print_writer_interleave {[]((writers == 0 && compromiseReaders > 0) -> ((Insert[0]:insertElement == 0 && Insert[1]:insertElement == 0) U (compromiseReaders == 0)))};
ltl print_writer_interleave_2 {[]<> (writers == 1 && compromiseReaders > 0)};
