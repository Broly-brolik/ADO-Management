package com.example.aguadeoromanagement

import java.time.format.DateTimeFormatter

class Constants(){
    companion object{
        val accessFormater = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val accessFormaterDateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
        val fromAccessFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val forUsFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val forUsFormatterDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        val url = "http://195.15.223.234/aguadeoro/connect.php"
        val imageUrl = "http://195.15.223.234/aguadeoro/06_inventory%20toc%20opy/"
        val password = "opensesame"
    }
}