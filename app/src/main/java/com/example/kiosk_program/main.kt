package com.example.kiosk_program

import android.annotation.SuppressLint
import android.content.ClipData.Item
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import java.time.LocalDateTime
import java.util.Random
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow

val menus: MutableList<Menu> = ArrayList()
val foods: MutableList<Food> = ArrayList()
val deliveries: MutableList<Delivery> = ArrayList()
val orders: MutableList<Order> = ArrayList()
var money: Double = 0.0
var now = LocalDateTime.now()
var start = LocalDateTime.of(now.year, now.month, now.dayOfMonth, 1, 10, 0)
var end = LocalDateTime.of(now.year, now.month, now.dayOfMonth, 1, 45, 0)


@SuppressLint("NewApi")
suspend fun main() {
    init()

    while(true) {
        displayMenu()
        var selectNumber = getPureNumber()
        if(selectNumber == 0) {
            println("3초뒤에 종료합니다.")
            globalDelay(3000)
            return
        }

        var selectedFood = selectMenu(selectNumber)
        globalDelay(3000)
        selectedFood?.let { food ->

            addOrder(food)
        } ?: run {

            println("\n현재 잔액: $money \n")
        }

    }

}

fun init() {
    money = 100.0

    // 메뉴 추가
    menus.add(Menu("PremiumWhopper", "프리미엄 와퍼"))
    menus.add(Menu("Whopper", "와퍼"))
    menus.add(Menu("Sidemenu", "사이드메뉴"))
    menus.add(Menu("Drinks", "음료"))
    menus.add(Menu("Order", "장바구니를 확인 후 주문합니다."))
    menus.add(Menu("Cancle", "진행중인 주문을 취소합니다."))

    // 프리미엄 와퍼 종류 추가
    foods.add(Food("쾨트로치즈와퍼", "진짜 불맛을 즐겨라, 4가지 고품격 치즈와 불에 직접 구운 와퍼 패티의 만남!", 7.9, "PremiumWhopper"))
    foods.add(Food("통새우와퍼", "불맛 가득 순쇠고기, 갈릭페퍼 통새우, 스파이시토마토소스가 더해진 프리미엄 버거", 7.9, "PremiumWhopper"))
    foods.add(Food("몬스터와퍼", "불맛 가득 순쇠고기, 치킨, 베이컨에 화끈한 디아블로 소스의 압도적인 맛", 9.3, "PremiumWhopper"))
    foods.add(Food("블랙바비큐콰트로치즈와퍼", "콰트로치즈와퍼가 바비큐소스를 만나다!", 9.3, "PremiumWhopper"))

    // 와퍼 종류 추가
    foods.add(Food("와퍼", "불에 직접 구운 순 쇠고기 패티에 싱싱한 야채가 한가득~ 버거킹의 대표 메뉴!", 7.1, "Whopper"))
    foods.add(Food("치즈와퍼", "불에 직접 구운 순 쇠고기 패티가 들어간 와퍼에 고소한 치즈까지!", 7.7, "Whopper"))
    foods.add(Food("불고기와퍼", "불에 직접 구운 순 쇠고기 패티가 들어간 와퍼에 달콤한 불고기 소스까지!", 7.4, "Whopper"))
    foods.add(Food("와퍼주니어", "불에 직접 구운 순 쇠고기 패티가 들어간 와퍼의 주니어 버전~ 작지만 꽉 찼다!", 4.7, "Whopperd"))

    // 사이드메뉴 종류 추가
    foods.add(Food("감자튀김", "세계최고의 감자만 엄선해서 버거킹만의 비법으로 바삭하게!", 2.1, "Sidemenu"))
    foods.add(Food("너겟킹", "바삭~ 촉촉~ 한입에 쏙 부드러운 너겟킹!", 2.2, "Sidemenu"))
    foods.add(Food("코울슬로", "아삭아삭한 양배추와 상큼한 드레싱의 코울슬로", 2.1, "Sidemenu"))
    foods.add(Food("바삭킹", "매콤하게! 바삭하게 튀긴 치킨윙", 3.0, "Sidemenu"))


    // 음료 종류 추가
    foods.add(Food("콜라", "코카-콜라로 더 짜릿하게!", 2.0, "Drinks"))
    foods.add(Food("스프라이트", "나를 깨우는 상쾌함!", 2.0, "Drinks"))
    foods.add(Food("오렌지주즈", "미닛메이드 오렌지", 2.8, "Drinks"))
    foods.add(Food("아메리카노", "자연을 담은 버거킹 RA인증커피", 1.5, "Drinks"))


    checkOrder()
    addDelivery()
}

fun getPureNumber(): Int {
    var userInput: String?
    var number: Int?

    while(true) {
        print("번호를 입력해주세요")
        userInput = readLine()
        number = userInput?.toIntOrNull()

        if(number != null) {
            return number
        } else {
            println("올바른 숫자를 입력해주세요")
        }
    }
}

fun selectMenu(cateNumber: Int): Food? {
    var menu = menus[cateNumber-1]
    var categoryName = menu.name

    if(categoryName != "Order" && categoryName != "Cancel") { // BURGERKING MENU
        var filteredFoods = foods.filter { it.category == categoryName }
        displayShakeMenuDetail(categoryName)

        while(true) {
            var selectFoodNumber = getPureNumber()
            if(selectFoodNumber > filteredFoods.size || selectFoodNumber < 0) {
                println("올바른 숫자를 입력해주세요")
            } else if(selectFoodNumber == 0) {
                return null
            } else {
                return filteredFoods[selectFoodNumber-1]
            }
        }
    } else { // ORDER MENU
        when(categoryName) {
            "Order" -> {
                var totalOrderPrice = displayOrderMenuDetail(categoryName)
                if(totalOrderPrice < 0.0) {
                    println("주문 내역이 존재하지 않습니다.")
                    return null
                }

                println("1. 주문\t\t 2. 메뉴판")

                while(true) {
                    var selectOrderNumber = getPureNumber()
                    when(selectOrderNumber) {
                        1 -> {
                            var isMainatainance = isMainatainance()

                            if(isMainatainance.first) {
                                println("현재 시각은 ${isMainatainance.second.hour}시 ${isMainatainance.second.minute}분입니다.")
                                println("은행 점검 시간은 ${start.hour}시 ${start.minute}분 ~ ${end.hour}시 ${end.minute}분이므로 결제할 수 없습니다.")
                            } else if(money >= totalOrderPrice) {

                                orders.clear()
                                money -= totalOrderPrice
                                println("결제를 완료했습니다. ${isMainatainance.second.toString()}")
                            } else {
                                println("현재 잔액은 ${money}W 으로 ${totalOrderPrice - money}W이 부족해서 주문할 수 없습니다.")
                            }
                            return null
                        }
                        2 -> {
                            println("메뉴판으로 이동합니다.")
                            return null
                        }
                        else -> {
                            println("올바른 숫자를 입력해주세요")
                        }
                    }
                }
            }
            "Cancel" -> {
                orders.clear()
                println("메뉴판으로 이동합니다.")
                return null
            }
            else -> {
                return null
            }
        }
    }
}

// 전체 메뉴판
fun displayMenu() {
    println("[ BURGERKING MENU ]")

    val maxNameLength = menus.maxOfOrNull { it.name.length } ?: 0
    var menuSize = menus.size
    var count = 1
    for(idx in 1..menuSize) {
        val menu = menus[idx-1]
        val name = menu.name
        if(name == "Order") println("[ ORDER MENU ]")
        val desc = menu.description
        val padding = " ".repeat(maxNameLength - name.length)
        println("$idx. $name$padding | $desc")
        count++
    }
    println("0. 종료 | 프로그램 종료")
}

// 디테일한 BURGER 메뉴판
fun displayShakeMenuDetail(categoryName: String) {

    println("\n[ $categoryName MENU ]")

    var filteredFoods = foods.filter { it.category == categoryName }

    // 메뉴 이름의 여백을 맞추기 위함
    // 가장 긴 이름의 길이 얻어옴
    val maxNameLength = filteredFoods.maxOfOrNull { it.name.toString().length } ?: 0
    val maxPriceLength = filteredFoods.maxOfOrNull { it.price.toString().length } ?: 0
    var foodSize = filteredFoods.size
    for(i in 1..foodSize) {
        val food = filteredFoods[i-1]
        val name = food.name
        val price = food.price
        val desc = food.description
        val namePadding = " ".repeat(maxNameLength - name.length)
        val pricePadding = " ".repeat(maxPriceLength - price.toString().length)
        println("$i. $name$namePadding | W $price$pricePadding | $desc")
    }
    val backPadding = " ".repeat(maxNameLength - "0. back".length)
    println("0. back$backPadding | 뒤로가기")
}

// 디테일한 Order 메뉴판
fun displayOrderMenuDetail(categoryName: String): Double {
    var orderSize = orders.size
    if(orderSize > 0) {
        println("\n아래와 같이 주문 하시겠습니까?\n")

        println("[ Orders ]")
        for(i in 0 until orderSize) {
            orders[i].food.displayInfo()
        }

        println("[ Total ]")
        val totalOrderPrice = orders.fold(0.0) { accumulator, order ->
            accumulator + order.food.price
        }
        println("W $totalOrderPrice")
        return totalOrderPrice
    } else {
        return -1.0
    }
}

fun addOrder(food: Food) {
    food.displayInfo()
    println("위 메뉴를 장바구니에 추가하시겠습니까?")
    println("1. 확인\t\t 2. 취소")

    while(true) {
        var selectOrderNumber = getPureNumber()
        when(selectOrderNumber) {
            1 -> {
                orders.add(Order(food))
                println("${food.name}를 장바구니에 추가했습니다.")
                return
            }
            2 -> {
                println("구매를 취소했습니다.")
                return
            }
            else -> {
                println("올바른 숫자를 입력해주세요")
            }
        }
    }
}

suspend fun globalDelay(time: Long) {
    delay(time)
}

fun isMainatainance(): Pair<Boolean, LocalDateTime> {
    var now = LocalDateTime.now()

    return Pair(now.toLocalTime() >= start.toLocalTime() && now.toLocalTime() <= end.toLocalTime(), now)
}

fun checkOrder() {
    var timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            println("\n 현재 주문 대기수: ${orders.size}")
        }
    }, 0, 5000)
}

fun addDelivery() {
    var timer = Timer()
    val sampleMenu = arrayOf("프리미엄와퍼", "와퍼", "사이드", "음료수")
    timer.schedule(object : TimerTask() {
        override fun run() {
            val menuNumber = (0..3).random()
            val menu = sampleMenu[menuNumber]
            val price = Math.random() * 80 + 30


            deliveries.add(Delivery(menu, price))
        }
    }, 0, 10000)
}