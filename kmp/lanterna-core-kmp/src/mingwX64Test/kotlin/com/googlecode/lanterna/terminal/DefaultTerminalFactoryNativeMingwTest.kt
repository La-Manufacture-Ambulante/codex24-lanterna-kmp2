package com.googlecode.lanterna.terminal

import kotlin.test.Test
import kotlin.test.assertSame

class DefaultTerminalFactoryNativeMingwTest {
    @Test
    fun jvmStyleSettersRemainFluent() {
        val factory = DefaultTerminalFactory()
        assertSame(factory, factory.setForceTextTerminal(false))
        assertSame(factory, factory.setPreferTerminalEmulator(false))
        assertSame(factory, factory.setTelnetPort(-1))
        assertSame(factory, factory.setForceAWTOverSwing(false))
        assertSame(factory, factory.setAutoOpenTerminalEmulatorWindow(false))
        assertSame(factory, factory.setTerminalEmulatorTitle("native"))
    }
}
