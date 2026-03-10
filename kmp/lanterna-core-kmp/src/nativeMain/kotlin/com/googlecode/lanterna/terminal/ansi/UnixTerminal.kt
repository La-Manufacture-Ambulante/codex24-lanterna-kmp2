package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.internal.io.IOException

class UnixTerminal @Throws(IOException::class) constructor(
    terminalCtrlCBehaviour: CtrlCBehaviour = CtrlCBehaviour.CTRL_C_KILLS_APPLICATION,
) : UnixLikeTerminal(terminalCtrlCBehaviour)
