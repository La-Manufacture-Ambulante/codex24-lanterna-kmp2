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

package com.googlecode.lanterna

import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.MouseCaptureMode

/**
 * This class provides a unified way for the test program to get their terminal
 * objects
 * @author Martin
 */
 class TestTerminalFactory:DefaultTerminalFactory {

 constructor() {}

 constructor(args:Array<String?>?) {
parseArgs(args)
}

 fun parseArgs(args:Array<String?>?) {
if (args == null) {
return 
}
for (arg in args!!)
{
if (arg == null) {
continue
}
val tok = arg!!.split("=", 2)
arg = tok!![0] // only the part before "="
val par = if (tok!!.size > 1) tok!![1] else ""
if ("--text-terminal".equals(arg) || "--no-swing".equals(arg))
{
setPreferTerminalEmulator(false)
setForceTextTerminal(true)
}
else if ("--awt".equals(arg))
{
setForceTextTerminal(false)
setPreferTerminalEmulator(true)
setForceAWTOverSwing(true)
}
else if ("--swing".equals(arg))
{
setForceTextTerminal(false)
setPreferTerminalEmulator(true)
setForceAWTOverSwing(false)
}
else if ("--mouse-click".equals(arg))
{
setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE)
}
else if ("--mouse-drag".equals(arg))
{
setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG)
}
else if ("--mouse-move".equals(arg))
{
setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE)
}
else if ("--telnet-port".equals(arg))
{
var port = 1024 // default for option w/o param
try
{
port = Integer.parseInt(par)
}
catch (e:NumberFormatException) {}

setTelnetPort(port)
}
else if ("--with-timeout".equals(arg))
{
var inputTimeout = 40 // default for option w/o param
try
{
inputTimeout = Integer.parseInt(par)
}
catch (e:NumberFormatException) {}

setInputTimeout(inputTimeout)
}
}
}
}
