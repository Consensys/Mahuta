package net.consensys.mahuta.core.test.utils;

import net.andreinc.mockneat.MockNeat;

public abstract class TestUtils {

    protected static final MockNeat mockNeat = MockNeat.threadLocal();
}
