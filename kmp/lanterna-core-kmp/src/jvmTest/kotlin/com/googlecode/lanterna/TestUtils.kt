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

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL

object TestUtils {
    fun downloadGPL(): String? {
        try {
            val url = URL("http://www.gnu.org/licenses/gpl.txt")
            url.openStream().use({ inputStream ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(32 * 1024)
                var readBytes = 0
                while (readBytes != -1) {
                    readBytes = inputStream!!.read(buffer)
                    if (readBytes > 0) {
                        byteArrayOutputStream.write(buffer, 0, readBytes)
                    }
                }
                return String(byteArrayOutputStream.toByteArray())
            })
        } catch (e: Exception) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            e!!.printStackTrace(printWriter)
            return stringWriter.toString()
        }
    }
}
