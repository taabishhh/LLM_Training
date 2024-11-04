//import org.scalatest.funsuite.AnyFunSuite
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
//
//class TransformerModelTest extends AnyFunSuite {
//  test("createModel should return model with correct layer configuration") {
//    val inputSize = 1536
//    val hiddenSize = 64
//    val outputSize = 768
//    val model: MultiLayerNetwork = TransformerModel.createModel(inputSize, hiddenSize, outputSize)
//
//    assert(model.getLayer(0).conf().getNIn == inputSize)
//    assert(model.getLayer(0).conf().getNOut == hiddenSize)
//    assert(model.getLayer(1).conf().getNIn == hiddenSize)
//    assert(model.getLayer(1).conf().getNOut == outputSize)
//  }
//}
