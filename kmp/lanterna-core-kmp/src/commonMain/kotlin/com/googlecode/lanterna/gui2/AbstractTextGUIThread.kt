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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import java.io.EOFException
import java.io.IOException
import java.util.Queue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

/**
 * Abstract implementation of [TextGUIThread] with common logic for both available concrete implementations.
 */
abstract class AbstractTextGUIThread/**
 * Sets up this [AbstractTextGUIThread] for operations on the supplies [TextGUI]
 * @param textGUI Text GUI this [TextGUIThread] implementations will be operating on
 */
    (protected val textGUI:TextGUI?):TextGUIThread {
protected val customTasks:Queue<Runnable?>?
protected var exceptionHandler:ExceptionHandler? = null

init{
this.exceptionHandler = object:ExceptionHandler() {
@Override
 fun onIOException(e:IOException?):Boolean {
e!!.printStackTrace()
return true
}

@Override
 fun onRuntimeException(e:RuntimeException?):Boolean {
e!!.printStackTrace()
return true
}
}
this.customTasks = LinkedBlockingQueue()
}

@Override
@Throws(IllegalStateException::class)
 fun invokeLater(runnable:Runnable?) {
customTasks!!.add(runnable)
}

@Override
 fun setExceptionHandler(exceptionHandler:ExceptionHandler?) {
if (exceptionHandler == null)
{
throw IllegalArgumentException("Cannot call setExceptionHandler(null)")
}
this.exceptionHandler = exceptionHandler
}

@Override
@Synchronized @Throws(IOException::class)
 fun processEventsAndUpdate():Boolean {
if (getThread() !== Thread.currentThread())
{
throw IllegalStateException("Calling processEventAndUpdate outside of GUI thread")
}
try
{
textGUI!!.processInput()
while (!customTasks!!.isEmpty())
{
val r = customTasks!!.poll()
if (r != null)
{
r!!.run()
}
}
if (textGUI!!.isPendingUpdate())
{
textGUI!!.updateScreen()
return true
}
return false
}
catch (e:EOFException) {
 // Always re-throw EOFExceptions so the UI system knows we've closed the terminal
            throw e
}
catch (e:IOException) {
if (exceptionHandler != null)
{
exceptionHandler!!.onIOException(e)
}
else
{
throw e
}
}
catch (e:RuntimeException) {
if (exceptionHandler != null)
{
exceptionHandler!!.onRuntimeException(e)
}
else
{
throw e
}
}

return true
}

@Override
@Throws(IllegalStateException::class, InterruptedException::class)
 fun invokeAndWait(runnable:Runnable?) {
val guiThread = getThread()
if (guiThread == null || Thread.currentThread() === guiThread)
{
runnable!!.run()
}
else
{
val countDownLatch = CountDownLatch(1)
invokeLater({ try
{
runnable!!.run()
}

finally
{
countDownLatch.countDown()
} })
countDownLatch.await()
}
}
}
