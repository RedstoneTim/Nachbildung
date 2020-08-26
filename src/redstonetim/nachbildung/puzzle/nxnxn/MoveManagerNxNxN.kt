package redstonetim.nachbildung.puzzle.nxnxn

import redstonetim.nachbildung.puzzle.Puzzle

class MoveManagerNxNxN(val n: Int) : Puzzle.MoveManager {
    companion object {
        private val standardMoveRegex = Regex("^([().]|(((\\d+-\\d+|\\d*)[RUFLDB]w|[RUFLDBMESmesxyz]|\\d*[RUFLDB]|(\\d+-\\d+|\\d*)[rufldb])(\\d*)('?)))")
        private val moveRegex222 = Regex("^([().]|([RUFLDBxyz])(\\d*)('?))")
        private val rotations = listOf("x", "y", "z")
        private val cachedMoves = hashMapOf<String, HashMap<Int, MoveNxNxN>>()

        private val translationTableX = arrayOf(
                moveFrom("R", 1), moveFrom("L", 1),
                moveFrom("B", 1), moveFrom("F", 1),
                moveFrom("U", 1), moveFrom("D", 1),
                moveFrom("M", 1), moveFrom("S", 1), moveFrom("E", -1),
                moveFrom("x", 1), moveFrom("z", -1), moveFrom("y", 1)
        )
        private val translationTableY = arrayOf(
                moveFrom("F", 1), moveFrom("B", 1),
                moveFrom("U", 1), moveFrom("D", 1),
                moveFrom("L", 1), moveFrom("R", 1),
                moveFrom("S", -1), moveFrom("E", 1), moveFrom("M", 1),
                moveFrom("z", 1), moveFrom("y", 1), moveFrom("x", -1)
        )
        private val translationTableZ = arrayOf(
                moveFrom("D", 1), moveFrom("U", 1),
                moveFrom("R", 1), moveFrom("L", 1),
                moveFrom("F", 1), moveFrom("B", 1),
                moveFrom("E", -1), moveFrom("M", 1), moveFrom("S", 1),
                moveFrom("y", -1), moveFrom("x", 1), moveFrom("z", 1)
        )
        private val translationMoveStrings = arrayListOf("R", "L", "U", "D", "F", "B", "M", "E", "S", "x", "y", "z")
        private val translationMoves = translationMoveStrings.map { moveFrom(it, 1) }
        private val cachedTables = hashMapOf("x" to translationTableX, "y" to translationTableY, "z" to translationTableZ)

        fun moveFrom(moveType: String, movePower: Int): Puzzle.Move {
            return cachedMoves[moveType]?.get(movePower)
                    ?: MoveNxNxN(moveType, movePower).also { cachedMoves.getOrPut(moveType) { hashMapOf() }[movePower] = it }
        }
    }

    private val moveRegex = if (n < 3) moveRegex222 else standardMoveRegex
    private val moveTypeGroup = if (n < 3) 2 else 3
    private val movePowerNumberGroup = if (n < 3) 3 else 6
    private val movePowerInvertGroup = if (n < 3) 4 else 7

    // TODO: Replace those weird apostrophes some people use with standard ones
    override fun parseMove(singleMoveString: String): Puzzle.Move? {
        val matchResult = moveRegex.matchEntire(singleMoveString)
        if (matchResult != null) {
            when (matchResult.value){
                "("-> return Puzzle.Move.openingParenthesisMove
                ")" -> return Puzzle.Move.closingParenthesisMove
                "." -> return Puzzle.Move.pauseMove
            }
            val matchGroups = matchResult.groups
            val movePower = (matchGroups[movePowerNumberGroup]?.value?.toIntOrNull()
                    ?: 1) * (if (matchGroups[movePowerInvertGroup]?.value == "\'") -1 else 1)
            val moveType = matchGroups[moveTypeGroup]?.value
            if (moveType != null) {
                return moveFrom(moveType, movePower)
            }
        }
        return null
    }

    override fun parseMoves(moveString: String): List<Puzzle.Move>? {
        val moveList = arrayListOf<Puzzle.Move>()
        var moves = moveString.trim()
        var matchResult = moveRegex.find(moves)
        var openParenthesis = false
        while (matchResult != null) {
            parseMove(matchResult.value)?.let {
                if (it == Puzzle.Move.openingParenthesisMove) {
                    if (openParenthesis) return@parseMoves null else openParenthesis = true
                } else if (it == Puzzle.Move.closingParenthesisMove) {
                    if (openParenthesis) openParenthesis = false else return@parseMoves null
                }
                moveList.add(it)
            }
            moves = moves.substring(matchResult.range.last + 1).trim()
            matchResult = moveRegex.find(moves)
        }
        return if (moves.isBlank() && !openParenthesis) moveList else null
    }

    // TODO: Make sure all method calls to this only use rotations, specifically the one in the parseSteps method of [Steps]
    // TODO: Test slice moves (they seem to be a bit sketchy, you know)
    override fun offsetMoves(moves: List<Puzzle.Move>, offsetRotations: List<Puzzle.Move>): List<Puzzle.Move> {
        if (offsetRotations.isEmpty()) return moves

        val rotationMoveString = offsetRotations.joinToString { "${it.moveType}${it.movePower}" }
        val cachedTable = cachedTables[rotationMoveString]
        val table = if (cachedTable == null) {
            val returnedTable = translationMoves.toTypedArray()
            for (rotation in offsetRotations) {
                var i = -1
                // TODO: Have x1, y1 and z1 translation tables?
                val translationTable = when(rotation.moveType) {
                    "y" -> translationTableY
                    "z" -> translationTableZ
                    else -> translationTableX
                }
                while (++i < rotation.simplifiedMovePower) {
                    var j = -1
                    while (++j < returnedTable.size) {
                        val oldMove = returnedTable[j]
                        val translationMove = translationTable[translationMoveStrings.indexOf(oldMove.moveType)]
                        returnedTable[j] = moveFrom(translationMove.moveType, oldMove.movePower * translationMove.movePower)
                    }
                }
            }
            cachedTables[rotationMoveString] = returnedTable
            returnedTable
        } else {
            cachedTable
        }
        val offsetMoves = ArrayList<Puzzle.Move>(moves.size)
        for (move in moves) {
            var i = -1
            if (move.isParenthesis) {
                offsetMoves.add(move)
                continue
            }
            while (++i < translationMoveStrings.size) {
                val translationMove = translationMoveStrings[i]
                val moveType = move.moveType
                offsetMoves.add(moveFrom(
                        if (moveType.contains(translationMove)) {
                            moveType.replace(translationMove, table[i].moveType)
                        } else if (moveType.contains(translationMove.toLowerCase())) {
                            moveType.replace(translationMove.toLowerCase(), table[i].moveType.toLowerCase())
                        } else {
                            continue
                        },
                        move.movePower * table[i].movePower))
                break
            }
        }
        return offsetMoves
    }

    class MoveNxNxN internal constructor(override val moveType: String, override val movePower: Int) : Puzzle.Move {
        override val simplifiedMovePower: Int = (movePower - (movePower / 4 - 1) * 4) % 4 // TODO: Improve
        override val isRotation: Boolean = rotations.contains(moveType)

        val name: String = when {
            movePower == 0 -> ""
            movePower == 1 -> moveType
            movePower == -1 -> "$moveType'"
            movePower < 0 -> "$moveType${kotlin.math.abs(movePower)}'"
            else -> "$moveType$movePower"
        }

        override fun toString(): String = name
    }
}