package com.mohammadbablees.calculator

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private var expression = ""
    private var answer = 0.0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val operations = findViewById<TextView>(R.id.operations)
        val theme = findViewById<ImageButton>(R.id.btn_theme)

        val clearButton = findViewById<Button>(R.id.btnClear)
        val backSpaceButton = findViewById<ImageButton>(R.id.btnBackspace)
        val divideButton = findViewById<Button>(R.id.btnDivide)
        val percentButton = findViewById<Button>(R.id.btnPercent)

        val sevenButton = findViewById<Button>(R.id.btn7)
        val eightButton = findViewById<Button>(R.id.btn8)
        val nineButton = findViewById<Button>(R.id.btn9)
        val fourButton = findViewById<Button>(R.id.btn4)
        val fiveButton = findViewById<Button>(R.id.btn5)
        val sixButton = findViewById<Button>(R.id.btn6)
        val oneButton = findViewById<Button>(R.id.btn1)
        val twoButton = findViewById<Button>(R.id.btn2)
        val threeButton = findViewById<Button>(R.id.btn3)
        val zeroButton = findViewById<Button>(R.id.btn_zero)

        val multiplyButton = findViewById<Button>(R.id.btnMultiply)
        val plusButton = findViewById<Button>(R.id.btn_plus)
        val minusButton = findViewById<Button>(R.id.btn_minus)
        val bracketsButton = findViewById<Button>(R.id.btn_brackets)
        val dotButton = findViewById<Button>(R.id.btn_dot)
        val equalButton = findViewById<Button>(R.id.btn_equal)

        // سطر واحد، مع تصغير الخط تلقائيًا ليناسب الشاشة.
        operations.maxLines = 1
        operations.isSingleLine = true
        operations.setHorizontallyScrolling(false)
        operations.ellipsize = null
        operations.setAutoSizeTextTypeUniformWithConfiguration(
            12,
            36,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )

        fun updateDisplay() {
            operations.text = expression.ifEmpty { "0" }
        }

        fun append(value: String) {
            if (expression.length + value.length > 30) return

            expression += value
            updateDisplay()
        }

        fun clear() {
            expression = ""
            answer = 0.0
            updateDisplay()
        }

        fun backSpace() {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                updateDisplay()
            }
        }

        fun equal() {
            try {
                if (expression.isBlank()) return

                answer = calculate(expression)
                operations.text = answer.toString()

                // يسمح بمتابعة الحساب باستخدام الناتج.
                expression = answer.toString()
            } catch (e: Exception) {
                operations.text = "خطأ"
                expression = ""
            }
        }

        oneButton.setOnClickListener { append("1") }
        twoButton.setOnClickListener { append("2") }
        threeButton.setOnClickListener { append("3") }
        fourButton.setOnClickListener { append("4") }
        fiveButton.setOnClickListener { append("5") }
        sixButton.setOnClickListener { append("6") }
        sevenButton.setOnClickListener { append("7") }
        eightButton.setOnClickListener { append("8") }
        nineButton.setOnClickListener { append("9") }
        zeroButton.setOnClickListener { append("0") }

        plusButton.setOnClickListener { append("+") }
        minusButton.setOnClickListener { append("-") }
        multiplyButton.setOnClickListener { append("*") }
        divideButton.setOnClickListener { append("/") }

        percentButton.setOnClickListener { append("%") }

        bracketsButton.setOnClickListener {
            val openCount = expression.count { it == '(' }
            val closeCount = expression.count { it == ')' }

            if (openCount > closeCount) {
                append(")")
            } else {
                append("(")
            }
        }

        dotButton.setOnClickListener { append(".") }

        clearButton.setOnClickListener { clear() }
        backSpaceButton.setOnClickListener { backSpace() }
        equalButton.setOnClickListener { equal() }
        theme.setOnClickListener {
            val isNightMode =
                (resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES

            AppCompatDelegate.setDefaultNightMode(
                if (isNightMode) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }

        updateDisplay()
    }

    private fun calculate(source: String): Double {
        val text = source
            .replace('×', '*')
            .replace('÷', '/')

        var position = 0

        fun skipSpaces() {
            while (position < text.length && text[position].isWhitespace()) {
                position++
            }
        }

        lateinit var parseExpression: () -> Double
        lateinit var parseTerm: () -> Double
        lateinit var parseUnary: () -> Double
        lateinit var parsePrimary: () -> Double

        parseExpression = {
            var value = parseTerm()

            while (true) {
                skipSpaces()

                if (position >= text.length ||
                    (text[position] != '+' && text[position] != '-')
                ) {
                    break
                }

                val operator = text[position]
                position++

                val right = parseTerm()
                value = if (operator == '+') value + right else value - right
            }

            value
        }

        parseTerm = {
            var value = parseUnary()

            while (true) {
                skipSpaces()

                if (position >= text.length ||
                    (text[position] != '*' && text[position] != '/')
                ) {
                    break
                }

                val operator = text[position]
                position++

                val right = parseUnary()

                if (operator == '/' && right == 0.0) {
                    throw IllegalArgumentException("لا يمكن القسمة على صفر")
                }

                value = if (operator == '*') value * right else value / right
            }

            value
        }

        parseUnary = {
            skipSpaces()

            if (position < text.length &&
                (text[position] == '+' || text[position] == '-')
            ) {
                val sign = text[position]
                position++

                val value = parseUnary()
                if (sign == '-') -value else value
            } else {
                parsePrimary()
            }
        }

        parsePrimary = {
            skipSpaces()

            var value: Double

            if (position < text.length && text[position] == '(') {
                position++
                value = parseExpression()
                skipSpaces()

                if (position >= text.length || text[position] != ')') {
                    throw IllegalArgumentException("قوس غير مغلق")
                }

                position++
            } else {
                val start = position
                var hasDecimalPoint = false

                while (position < text.length) {
                    val character = text[position]

                    when {
                        character.isDigit() -> position++
                        character == '.' && !hasDecimalPoint -> {
                            hasDecimalPoint = true
                            position++
                        }
                        else -> break
                    }
                }

                if (position == start) {
                    throw IllegalArgumentException("رقم غير صالح")
                }

                value = text.substring(start, position).toDouble()
            }

            skipSpaces()


            if (position < text.length && text[position] == '%') {
                value /= 100.0
                position++
            }

            value
        }

        val result = parseExpression()
        skipSpaces()

        if (position != text.length) {
            throw IllegalArgumentException("التعبير غير صالح")
        }

        return result
    }
}
