import org.scalatest.funsuite.AnyFunSuite
import org.nd4j.linalg.factory.Nd4j

class PositionalEmbeddingTest extends AnyFunSuite {
  test("computePositionalEmbedding should return correct shape based on window size and embedding dimension") {
    val windowSize = 3
    val embeddingDim = 4
    val positionalEmbedding = SlidingWindowSpark.computePositionalEmbedding(windowSize, embeddingDim)

    assert(positionalEmbedding.shape()(0) == windowSize)
    assert(positionalEmbedding.shape()(1) == embeddingDim)
  }
}
