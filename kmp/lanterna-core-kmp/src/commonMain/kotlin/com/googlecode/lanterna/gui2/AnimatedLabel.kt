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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.internal.compat.Timer
import com.googlecode.lanterna.internal.compat.TimerTask
import com.googlecode.lanterna.internal.compat.WeakHashMap
import com.googlecode.lanterna.internal.compat.WeakReference

/**
 * This is a special label that contains not just a single text to display but a number of frames that are cycled
 * through.
 */
class AnimatedLabel(firstFrameText: String?) : Label(firstFrameText) {
    private val frames: MutableList<Array<String>> = ArrayList()
    private var combinedMaximumPreferredSize: TerminalSize = TerminalSize.ZERO
    private var currentFrame: Int = 0

    init {
        val lines = splitIntoMultipleLines(firstFrameText ?: "")
        frames.add(lines)
        ensurePreferredSize(lines)
    }

    override fun calculatePreferredSize(): TerminalSize {
        return (super.calculatePreferredSize() ?: TerminalSize.ZERO).max(combinedMaximumPreferredSize) ?: TerminalSize.ZERO
    }

    fun addFrame(text: String?): AnimatedLabel {
        val lines = splitIntoMultipleLines(text ?: "")
        frames.add(lines)
        ensurePreferredSize(lines)
        return this
    }

    private fun ensurePreferredSize(lines: Array<String>) {
        combinedMaximumPreferredSize =
            combinedMaximumPreferredSize.max(getBounds(lines, combinedMaximumPreferredSize)!!) ?: combinedMaximumPreferredSize
    }

    fun nextFrame() {
        currentFrame++
        if (currentFrame >= frames.size) {
            currentFrame = 0
        }
        super.lines = frames[currentFrame]
        invalidate()
    }

    override fun onRemoved(container: Container?) {
        stopAnimation()
    }

    fun startAnimation(millisecondsPerFrame: Long): AnimatedLabel {
        if (timer == null) {
            timer = Timer("AnimatedLabel")
        }
        val animationTimerTask = AnimationTimerTask(this)
        scheduledTasks[this] = animationTimerTask
        timer!!.scheduleAtFixedRate(animationTimerTask, millisecondsPerFrame, millisecondsPerFrame)
        return this
    }

    fun stopAnimation(): AnimatedLabel {
        removeTaskFromTimer(this)
        return this
    }

    private class AnimationTimerTask(label: AnimatedLabel) : TimerTask() {
        private val labelRef: WeakReference<AnimatedLabel> = WeakReference(label)

        override fun run() {
            val animatedLabel = labelRef.get()
            if (animatedLabel == null) {
                cancel()
                canCloseTimer()
            } else {
                if (animatedLabel.basePane == null) {
                    animatedLabel.stopAnimation()
                } else {
                    animatedLabel.nextFrame()
                }
            }
        }
    }

    companion object {
        private var timer: Timer? = null
        private val scheduledTasks: WeakHashMap<AnimatedLabel, TimerTask> = WeakHashMap()

        fun createClassicSpinningLine(speed: Int = 150): AnimatedLabel {
            val animatedLabel = AnimatedLabel("-")
            animatedLabel.addFrame("\\")
            animatedLabel.addFrame("|")
            animatedLabel.addFrame("/")
            animatedLabel.startAnimation(speed.toLong())
            return animatedLabel
        }

        private fun removeTaskFromTimer(animatedLabel: AnimatedLabel) {
            scheduledTasks[animatedLabel]?.cancel()
            scheduledTasks.remove(animatedLabel)
            canCloseTimer()
        }

        private fun canCloseTimer() {
            if (scheduledTasks.isEmpty()) {
                timer?.cancel()
                timer = null
            }
        }
    }
}
