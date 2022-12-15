package minesweeper

import kotlin.random.Random

var fieldMain = mutableListOf<MutableList<String>>()
var fieldCurrent = mutableListOf<MutableList<String>>()
var userInputMines = 0
val userInputSize = 9
var minesCount = 0
var starCount = 0
var unopenedCells = 0
var winOrLoseState = false

/**
 * displays a game state with the required formatting
 */
fun displayField(field: MutableList<MutableList<String>>) {
    println("\n │123456789│")
    println("—│—————————│")
    for (y in 0 until field.size) {
        print("${y + 1}|")
        for (x in 0 until field[0].size) {
            print(field[y][x])
        }
        print("|\n")
    }
    println("—│—————————│")
}

/**
 * Creates new game field with randomly placed mines
 */
fun newField(size: Int, mines: Int) {
    fieldMain = MutableList(size) { MutableList(size) { "." } }
    fieldCurrent = MutableList(size) { MutableList(size) { "." } }
    var minesLeft = mines
    // randomly placing mines
    do {
        val randomX = Random.nextInt(0, size)
        val randomY = Random.nextInt(0, size)
        if (fieldMain[randomY][randomX] == ".") {
            fieldMain[randomY][randomX] = "X"
            minesLeft--
        }
    } while (minesLeft != 0)
    // analyzing every cell for mines
    for (y in 0 until size) {
        for (x in 0 until size) {
            // checking if the cell is empty
            if (fieldMain[y][x] != "X") {
                var minesAroundCount = 0
                // checking cells around the current cell
                for (yi in 0..2) {
                    for (xi in 0..2) {
                        // filtering out of range cells
                        if (
                            y - 1 + yi in 0 until size &&
                            x - 1 + xi in 0 until size
                        ) {
                            if (fieldMain[y - 1 + yi][x - 1 + xi] == "X") {
                                minesAroundCount++
                            }
                        }
                    }
                }
                // setting numbers next to a mine
                if (minesAroundCount > 0) {
                    fieldMain[y][x] = minesAroundCount.toString()
                }
            }
        }
    }
}

/**
 * Invokes on an empty cell move, opens all possible cells around recursively
 */
fun openCells(xMatrix: Int, yMatrix: Int) {
    fieldCurrent[yMatrix][xMatrix] = "/"
    unopenedCells--
    // checking all cells around the current cell
    val ySub = yMatrix - 1
    val xSub = xMatrix - 1
    for (yi in 0..2) {
        for (xi in 0..2) {
            // filtering out of range cells
            if (
                ySub + yi in 0 until userInputSize &&
                xSub + xi in 0 until userInputSize
            ) {
                // filtering central cell and already opened cells
                if (
                    yi == 1 &&
                    xi == 1 ||
                    "12345678".contains(fieldCurrent[ySub + yi][xSub + xi]) ||
                    fieldCurrent[ySub + yi][xSub + xi] == "/"
                ) continue
                // opening a cell with a number
                if ("12345678".contains(fieldMain[ySub + yi][xSub + xi])) {
                    fieldCurrent[ySub + yi][xSub + xi] = fieldMain[ySub + yi][xSub + xi]
                    unopenedCells--
                // recursive call on an unexplored empty cell
                } else if (fieldMain[ySub + yi][xSub + xi] == ".") {
                    openCells(xSub + xi, ySub + yi)
                }
            }
        }
    }
}

/**
 * Prompts coordinates and move or mark action for a desired cell
 */
fun userMove(): Pair<Int, Int> {
    var x: Int
    var y: Int
    do {
        try{
            print("Set/unset mines marks or claim a cell as free: ")
            val (xInput, yInput, claim) = readln().split(" ")
            x = xInput.toInt()
            y = yInput.toInt()
            // making a step on the cell
            if (
                x in 1..userInputSize &&
                y in 1..userInputSize &&
                fieldCurrent[y - 1][x - 1] == "." &&
                claim == "free"
            ) {
                // the cell with a mine
                if (fieldMain[y - 1][x - 1] == "X") {
                    fieldCurrent[y - 1][x - 1] = fieldMain[y - 1][x - 1]
                    displayField(fieldCurrent)
                    println("You stepped on a mine and failed!")
                    winOrLoseState = true
                    break
                // the cell with a number
                } else if ("12345678".contains(fieldMain[y - 1][x - 1])) {
                    fieldCurrent[y - 1][x - 1] = fieldMain[y - 1][x - 1]
                    unopenedCells--
                    break
                // the cell is empty
                } else if (fieldMain[y - 1][x - 1] == ".") {
                    openCells(x - 1, y - 1)
                    break
                }
            // marking or unmarking the cell
            } else if (
                x in 1..userInputSize &&
                y in 1..userInputSize &&
                claim == "mine"
            ) {
                if (fieldCurrent[y - 1][x - 1] == ".") {
                    fieldCurrent[y - 1][x - 1] = "*"
                    starCount++
                    if (fieldMain[y - 1][x - 1] == "X") {
                        minesCount++
                    }
                    break
                } else if (fieldCurrent[y - 1][x - 1] == "*") {
                    fieldCurrent[y - 1][x - 1] = "."
                    starCount--
                    if (fieldMain[y - 1][x - 1] == "X") {
                        minesCount--
                    }
                    break
                }
            }
        } catch (e: Exception) {
            println("Wrong input!")
        }
    } while (true)
    return Pair(x, y)
}

/**
 * Checks for winning state
 */
fun winCheck() {
    if (minesCount == userInputMines && starCount == minesCount || // 1st condition to win
        unopenedCells == userInputMines // 2nd condition to win
    ) {
        winOrLoseState = true
        displayField(fieldCurrent)
        println("Congratulations! You found all the mines!")
    }
}

/**
 * Minesweeper game
 */
fun minesweeper() {
    // setting the mines
    do {
        try {
            print("How many mines do you want on the field? ")
            val userInput = readln().toInt()
            if (userInput in 1..(userInputSize * userInputSize)) {
                userInputMines = userInput
                unopenedCells = userInputSize * userInputSize
                newField(userInputSize, userInputMines)
                displayField(fieldCurrent)
                break
            }
        } catch (e: Exception) {
            println("Wrong input!")
        }
    } while (true)
    // playing the game
    do {
        userMove()
        winCheck()
        if (winOrLoseState) break
        displayField(fieldCurrent)
    } while (true)
}

fun main() {
    minesweeper()
}