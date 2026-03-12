package com.googlecode.lanterna.issue

import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Created by martin on 2017-04-15.
 */
object Issue305 {
/**
     * @param args the command line arguments
     */
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal = DefaultTerminalFactory().createTerminal()!!
        System.out.println("Class: " + terminal!!::class.java)

        terminal!!.close()

        var br: BufferedReader? = null
        try {
            br = BufferedReader(InputStreamReader(System.`in`))

            while (true) {
                System.out.print("Enter something: ")
                val input = br!!.readLine()

                if ("q".equals(input)) {
                    System.out.println("Exit!")
                    System.exit(0)
                }

                System.out.println("input : " + input!!)
                System.out.println("-----------\n")
            }
        } catch (e: IOException) {
            e!!.printStackTrace()
        } finally
        {
            if (br != null) {
                try {
                    br!!.close()
                } catch (e: IOException) {
                    e!!.printStackTrace()
                }
            }
        }
    }
}
