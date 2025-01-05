package com.uselessmnemonic.pak;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.NoSuchElementException;

final class ForeignHelpers {
    static final private Linker linker = Linker.nativeLinker();
    static final private SymbolLookup stdLookup = linker.defaultLookup();

    static final private MethodHandle strlenHandle = stdLookup.find("strlen").map(it ->
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
            "no library available under any of %s".formatted(Arrays.toString(names))
        );
    }

    /**
     * Calls C strlen on the given segment, which is expected to point to a null-terminated string.
     */
    static long strlen(MemorySegment segment) throws Throwable {
        return (long) strlenHandle.invokeExact(segment);
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
