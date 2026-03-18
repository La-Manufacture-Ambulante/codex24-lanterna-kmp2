package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.internal.io.IOException
import kotlin.test.Test
import kotlin.test.assertFailsWith
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

    @Test
    fun emulatorModeIsRejectedOnNative() {
        val factory = DefaultTerminalFactory().setPreferTerminalEmulator(true)
        assertFailsWith<IOException> { factory.createTerminal() }
    }

    @Test
    fun telnetModeIsRejectedOnNative() {
        val factory = DefaultTerminalFactory().setTelnetPort(2323)
        assertFailsWith<IOException> { factory.createTerminal() }
    }
}
