package redstonetim.nachbildung.puzzle.nxnxn

import javafx.scene.web.WebView
import redstonetim.nachbildung.puzzle.Puzzle
import java.net.URI

class PuzzleNxNxNVisualization(val n: Int): Puzzle.PuzzleVisualization {
    override val node = WebView()
    private val link = URI.create("http://cube.rider.biz/visualcube.php?fmt=svg&pzl=$n&r=y20x-34&view=trans&size=250&alg=x2")
    private var lastQuery = link.toString()

    init {
        node.maxWidth = 300.0
        node.maxHeight = 300.0
    }

    override fun update(scramble: String, solution: String) {
        val query = "${this.link.query} $scramble $solution"
        if (query != lastQuery) {
            node.engine.load(URI(this.link.scheme, this.link.authority,
                    this.link.path, query, this.link.fragment).toString())
            lastQuery = query
        }
    }

    // TODO: Finish
//    override val node = Box(100.0, 100.0, 100.0)
//    private var transform: Transform = Rotate()
//    private var rotate: Rotate = Rotate(100.0, Rotate.X_AXIS)
//
//    init {
//        node.transforms.setAll(transform)
//    }
//
//    private fun rotateByX(angle: Double) {
//        rotate = Rotate(angle, Rotate.X_AXIS)
//        transform = transform.createConcatenation(rotate)
//        node.transforms.setAll(transform)
//    }
//
//    private fun rotateByY(angle: Double) {
//        rotate = Rotate(angle, Rotate.Y_AXIS)
//        transform = transform.createConcatenation(rotate)
//        node.transforms.setAll(transform)
//    }
//
//    private fun rotateByZ(angle: Double) {
//        rotate = Rotate(angle, Rotate.Z_AXIS)
//        transform = transform.createConcatenation(rotate)
//        node.transforms.setAll(transform)
//    }
//
//    override fun update(scramble: String, solution: String) {
//    }

    override fun representsPuzzle(puzzle: Puzzle): Boolean = (puzzle is PuzzleNxNxN) && (puzzle.n == n)
}