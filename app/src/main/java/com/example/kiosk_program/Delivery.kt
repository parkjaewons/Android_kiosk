package com.example.kiosk_program

class Delivery(menu: String, price: Double) {
    var menu: String
    var price: Double


    init {
        this.menu = menu
        this.price = price

    }

    fun displayInfo() {
        println("이름: $menu, 금액: $price")
    }
}