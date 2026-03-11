/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Structure

import java.util.Arrays

/**
 * Interface to Posix libc
 */
 interface PosixLibC:Library {
 fun tcgetattr(fd:Int, termios_p:termios?):Int 
 fun tcsetattr(fd:Int, optional_actions:Int, termios_p:termios?):Int 
 fun ioctl(fd:Int, request:Int, winsize:winsize?):Int 
 fun signal(sig:Int, fn:sig_t?):sig_t? 

 interface sig_t:Callback {
 fun invoke(signal:Int) 
}

 class termios:Structure() {
 var c_iflag:Int = 0           // input mode flags
 var c_oflag:Int = 0           // output mode flags
 var c_cflag:Int = 0           // control mode flags
 var c_lflag:Int = 0           // local mode flags
 var c_line:Byte = 0           // line discipline
 var c_cc:ByteArray? = null           // control characters
 var c_ispeed:Int = 0          // input speed
 var c_ospeed:Int = 0          // output speed
init{
c_cc = ByteArray(NCCS)
}

protected override fun getFieldOrder():List? {
return Arrays.asList(
"c_iflag", 
"c_oflag", 
"c_cflag", 
"c_lflag", 
"c_line", 
"c_cc", 
"c_ispeed", 
"c_ospeed"
)
}

@Override
public override fun toString():String? {
return ("termios{" + 
"c_iflag=" + c_iflag + 
", c_oflag=" + c_oflag + 
", c_cflag=" + c_cflag + 
", c_lflag=" + c_lflag + 
", c_line=" + c_line + 
", c_cc=" + Arrays.toString(c_cc) + 
", c_ispeed=" + c_ispeed + 
", c_ospeed=" + c_ospeed + 
'}'.toString())
}
}

 class winsize:Structure() {
 var ws_row:Short = 0
 var ws_col:Short = 0
 var ws_xpixel:Short = 0
 var ws_ypixel:Short = 0

@Override
protected override fun getFieldOrder():List? {
return Arrays.asList("ws_row", "ws_col", "ws_xpixel", "ws_ypixel")
}

@Override
public override fun toString():String? {
return ("winsize{" + 
"ws_row=" + ws_row + 
", ws_col=" + ws_col + 
", ws_xpixel=" + ws_xpixel + 
", ws_ypixel=" + ws_ypixel + 
'}'.toString())
}
}

companion object {

 // Constants
     val STDIN_FILENO = 0
 val STDOUT_FILENO = 1
 val TCSANOW = 0
 val NCCS = 32

 // Constants for c_lflag (beware of octal numbers below!!)
    @SuppressWarnings("OctalInteger")
 val ISIG = 1
@SuppressWarnings("OctalInteger")
 val ICANON = 2
@SuppressWarnings("OctalInteger")
 val ECHO = 8

 // Signals
     val SIGWINCH = 28

 // Constants for ioctl
     val TIOCGWINSZ = 0x5413
}
}
