package redstonetim.nachbildung.method

import redstonetim.nachbildung.step.Step

open class MethodSubstep(val name: String, val identifier: String = name, val countStep: (List<Step>, Int) -> Boolean) {
    companion object: HashMap<String, MethodSubstep>() {
        private val LAST_PAIR_REGEX = Regex(".*LS|.*LP|.*LE")
        private val OLS_REGEX = Regex("$LAST_PAIR_REGEX|(O|PO|OCE|OCPE|OC\\(P\\)E|OC|CO|EO|2G|ZZ|ZB|1L)LL|OLLCP|OLL\\(CP\\)")
        private val LEFT_BLOCK_REGEX = Regex("Left (pair|square|block)")
        private val RIGHT_BLOCK_REGEX = Regex("Right (pair|square|block)")

        init {
            // Add OLS
            MethodSubstep("OLS", "Pair OLS") { steps, currentIndex ->
                val name = steps[currentIndex].name
                name.matches(OLS_REGEX) || (name == "4th pair")
            }.register()
            MethodSubstep("OLS", "RB OLS") { steps, currentIndex ->
                val name = steps[currentIndex].name
                name.matches(OLS_REGEX) || (name == "Right pair")
            }.register()
            MethodSubstep("OLS", "EOLine OLS") { steps, currentIndex ->
                val currentName = steps[currentIndex].name
                when {
                    currentName.matches(OLS_REGEX) -> true
                    currentName == "Right pair" -> {
                        var i = -1
                        while (++i < currentIndex) {
                            val name = steps[i].name
                            if ((name == "Left pair") || (name == "Left block")) {
                                return@MethodSubstep true
                            }
                        }
                        false
                    }
                    currentName == "Left pair" -> {
                        var i = -1
                        while (++i < currentIndex) {
                            val name = steps[i].name
                            if ((name == "Right pair") || (name == "Right block")) {
                                return@MethodSubstep true
                            }
                        }
                        false
                    }
                    else -> false
                }
            }.register()

            // EOLine left and right block
            MethodSubstep("Left block", "EOLine Left block") { steps, currentIndex ->
                val currentName = steps[currentIndex].name
                when {
                    currentName.matches(LEFT_BLOCK_REGEX) -> true
                    currentName.matches(LAST_PAIR_REGEX) -> {
                        var i = -1
                        while (++i < currentIndex) {
                            val name = steps[i].name
                            if ((name == "Right pair") || (name == "Right block")) {
                                return@MethodSubstep true
                            }
                        }
                        false
                    }
                    else -> false
                }
            }.register()
            MethodSubstep("Right block", "EOLine Right block") { steps, currentIndex ->
                val currentName = steps[currentIndex].name
                when {
                    currentName.matches(RIGHT_BLOCK_REGEX) -> true
                    currentName.matches(LAST_PAIR_REGEX) -> {
                        var i = -1
                        while (++i < currentIndex) {
                            val name = steps[i].name
                            if ((name == "Left pair") || (name == "Left block")) {
                                return@MethodSubstep true
                            }
                        }
                        false
                    }
                    else -> false
                }
            }.register()
        }
    }

    open fun test(steps: List<Step>, currentIndex: Int): Boolean {
        return countStep.invoke(steps, currentIndex)
    }

    fun register() {
        put(identifier, this)
    }
}