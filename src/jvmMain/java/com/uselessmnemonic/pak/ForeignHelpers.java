package com.uselessmnemonic.pak;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.NoSuchElementException;

final class ForeignHelpers {
    static final private Linker linker = Linker.nativeLinker();
    static final private SymbolLookup stdLookup = linker.defaultLookup();

    static final ValueLayout ULONG;
    static final long ULONG_MAX;
    static final long UINT_MAX = 0x00000000FFFFFFFFL;

    static {
        var nativeLong = linker.canonicalLayouts().get("long");
        var nativeLongSize = nativeLong.byteSize();
        if (nativeLongSize == 4) {
            ULONG = ValueLayout.JAVA_INT;
            ULONG_MAX = 0x00000000FFFFFFFFL;
        } else if (nativeLongSize == 8) {
            ULONG = ValueLayout.JAVA_LONG;
            ULONG_MAX = 0xFFFFFFFFFFFFFFFFL;
        } else {
            var msg = String.format("Pak cannot be used on this platform because 'long' is %d bytes", nativeLongSize);
            throw new RuntimeException(msg);
        }
    }

    static final private MethodHandle strlen = stdLookup.find("strlen").map(it ->
        linker.downcallHandle(it, FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS))
    ).orElseThrow();

    /**
     * Retrieves the first library found with any of the given names.
     */
    static SymbolLookup libraryLookupOneOf(Arena arena, String ...names) {
        for (var name : names) try {
            return SymbolLookup.libraryLookup(name, arena);
        } catch (IllegalArgumentException _) {}
        throw new NoSuchElementException(
            String.format("no library available under any of %s", Arrays.toString(names))
        );
    }

    /**
     * Calls C strlen on the given segment, which is expected to point to a null-terminated string.
     */
    static long strlen(MemorySegment segment) throws Throwable {
        return (long) strlen.invokeExact(segment);
    }

    /**
     * Creates struct layout while automatically inserting padding
     */
    static StructLayout autoPad(MemoryLayout... elements) {
        var list = new java.util.ArrayList<MemoryLayout>();
        long runningSize = 0;
        long largestAlignment = 1;
        for (var e : elements) {
            long rem = runningSize % e.byteAlignment();
            if (rem != 0) {
                list.add(MemoryLayout.paddingLayout(rem));
                runningSize += rem;
            }
            list.add(e);
            runningSize += e.byteSize();
            if (e.byteAlignment() > largestAlignment) {
                largestAlignment = e.byteAlignment();
            }
        }
        var tail = runningSize % largestAlignment;
        if (tail != 0) {
            list.add(MemoryLayout.paddingLayout(tail));
        }
        return MemoryLayout.structLayout(list.toArray(new MemoryLayout[0]));
    }

    private ForeignHelpers() {}
}
