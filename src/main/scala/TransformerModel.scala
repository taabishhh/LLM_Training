import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Adam


object TransformerModel {
  def createModel(inputSize: Int, hiddenSize: Int, outputSize: Int): MultiLayerNetwork = {
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .updater(new Adam(0.001))
      .list()
      .layer(0, new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()
        .nIn(inputSize)
        .nOut(hiddenSize)
        .activation(Activation.RELU)
        .build())
      .layer(1, new org.deeplearning4j.nn.conf.layers.OutputLayer.Builder()
        .nIn(hiddenSize)
        .nOut(outputSize)
        .activation(Activation.SOFTMAX)
        .build())
      .build()

    val model = new MultiLayerNetwork(conf)
    model.init()
    model
  }
}
