package com.example.lexicalanalyzer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var input: String = ""
    private var identifiers: MutableList<String> = mutableListOf()
    private var separators: MutableList<String> = mutableListOf()
    private var operators: MutableList<String> = mutableListOf()
    private var comments: MutableList<String> = mutableListOf()
    private var result: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAnalyse.setOnClickListener {
            etCode.isEnabled = false
            analyseInput()
        }

        etCode.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                input = s.toString()
            }
        })

        btnReset.setOnClickListener {
            identifiers.clear()
            operators.clear()
            separators.clear()
            comments.clear()
            etCode.isEnabled = true
            tvResult.text = ""
            etCode.setText("")
            result = ""
        }
    }

    private fun analyseInput() {
        var token = ""
        var commentStarted = false
        input.forEachIndexed {index, it ->
            Log.e("bla:", it.toString())
            if(index < input.length - 1 && it == '/' && input[index + 1] == '#') {
                commentStarted = true
            }
            if(commentStarted) {
                if(index > 1 && it == '/' && input[index-1] == '#') {
                    result += "('/##/', $COMMENT)\n"
                    comments.add("/##/")
                    commentStarted = false
                }
            } else {
                when(it){
                    '\n' -> {
                        setIdentifier(token)
                        token = ""
                    }
                    ' '  -> {
                        setIdentifier(token)
                        separators.add(" ")
                        result += "(' ', $SEPARATOR)\n"
                        token = ""
                    }
                    '+', '-', '*', '/', '=' -> {
                        setIdentifier(token)
                        operators.add("$it")
                        result += "('$it', $OPERATOR)\n"
                        token = ""
                    }
                    else -> {
                        token += it
                        if(index == input.length - 1) {
                            setIdentifier(token)
                        }
                    }
                }
            }
        }

        result = "a)\n$result\n" + "b)\n${provideGroupResult()}"
        tvResult.text = result
    }

    private fun provideGroupResult() : String {
        var data = ""
        data = provideIdentifierDataString() + provideOperatorsDataString()  + provideSeparatorsDataString() + provideCommentsDataString()
        return data
    }

    private fun provideCommentsDataString(): String {
        var commentsData = ""
        if(comments.isEmpty()) return "- $COMMENTS [0]\n"
        commentsData = "- $COMMENTS[1]: '/##/'[${comments.size}]\n"
        return commentsData
    }

    private fun provideSeparatorsDataString(): String {
        var separatorsData = ""
        if(separators.isEmpty()) return "- $SEPARATORS [0],\n"
        separatorsData = "- $SEPARATORS [${separators.distinct().size}]: ' '[${separators.size}],\n"
        return separatorsData
    }

    private fun provideOperatorsDataString() : String {
        var operatorsData = ""
        var operatorsResultString = ""
        val operatorsMap = mutableMapOf<String, Int>()

        operators.forEach {
            if(operatorsMap.containsKey(it)) {
                operatorsMap[it] =  operatorsMap.getValue(it)  + 1
            } else {
                operatorsMap[it] = 1
            }
        }
        if(operatorsMap.isEmpty()) return "- $OPERATORS [0],\n"

        operatorsMap.forEach { (t, u) ->
            operatorsResultString +=  "'${t}'[${u}], "
        }

        operatorsData = "- $OPERATORS [${operatorsMap.size}]: $operatorsResultString \n"
        return operatorsData
    }

    private fun provideIdentifierDataString() : String {
        var identifiersData = ""
        var identifiersResultString = ""
        val identifiersMap = mutableMapOf<String, Int>()

        identifiers.forEach {
            if(identifiersMap.containsKey(it)) {
                identifiersMap[it] =  identifiersMap.getValue(it)  + 1
            } else {
                identifiersMap[it] = 1
            }
        }
        if(identifiersMap.isEmpty()) return "- $IDENTIFIERS [0],\n"

        identifiersMap.forEach { (t, u) ->
            identifiersResultString +=  "'${t}'[${u}], "
        }

        identifiersData = "- $IDENTIFIERS [${identifiersMap.size}]: $identifiersResultString \n"
        return identifiersData
    }

    private fun setIdentifier(token: String) {
        if(token.length > 1 && token[0] == 'i' && token.none{ char -> char !in 'A'..'Z' && char !in 'a'..'z'}) {
            identifiers.add("$token")
            result += "('$token', $IDENTIFIER)\n"
        }
    }
}