import org.scalatest.funsuite.AnyFunSuite
import org.nd4j.linalg.factory.Nd4j

class SlidingWindowTest extends AnyFunSuite {
  test("createSlidingWindowsWithPositionalEmbedding should create correct sliding windows") {
    val tokens = Array("word1", "word2", "word3", "word4")
    val embeddings = Array(
      Array(0.1, 0.2), Array(0.3, 0.4), Array(0.5, 0.6), Array(0.7, 0.8)
    )
    val windowSize = 2
    val result = SlidingWindowSpark.createSlidingWindowsWithPositionalEmbedding(tokens, embeddings, windowSize)

    assert(result.length == 2)
    assert(result.head._1 sameElements Array("word1", "word2"))
    assert(result.head._3 == "word3")
    assert(result.head._4.equals(Nd4j.create(Array(0.5, 0.6))))
  }
}
