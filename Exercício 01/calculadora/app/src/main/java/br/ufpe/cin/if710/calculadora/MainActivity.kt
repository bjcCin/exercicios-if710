package br.ufpe.cin.if710.calculadora

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    private var textTela: String = ""
    private var resultado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            textTela = savedInstanceState.getString("Expressao")
            text_calc.setText(textTela)
            resultado = savedInstanceState.getString("Resultado")
            text_info.text = resultado
        }

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        //Salvando as variáveis ao chamar o OnDestroy() da Activity
        savedInstanceState?.putString("Expressao", textTela)
        savedInstanceState?.putString("Resultado", resultado)

        super.onSaveInstanceState(savedInstanceState)
    }



    fun adicionarNumero(view: View){
        //Botão de Adicionar expressão no edit text
        textTela  = "${textTela}${(view as TextView).text}"
        text_calc.setText(textTela)

    }

    fun limparExpressao(view: View){
        //Botão C na calculadora
        textTela = ""
        text_calc.setText(textTela)
        resultado = ""
        text_info.text = resultado
    }

    fun calcularResultado(view: View){
        //Try Catch para capturar a ação e impedir que a aplicação dê crash
        if(textTela != ""){
            try {
                resultado = "${eval(textTela)}"
            } catch (e: RuntimeException){
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
            text_info.text = resultado
        } else Toast.makeText(applicationContext, "Expressão vazia!", Toast.LENGTH_SHORT).show()

    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {

                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double = 0.0
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }


}
