import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.Adam

object TransformerModel {
  // Method to create and initialize a simple neural network model
  def createModel(inputSize: Int, hiddenSize: Int, outputSize: Int): MultiLayerNetwork = {

    // Define the model configuration
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      // Set the optimizer for the model to Adam with a learning rate of 0.001
      .updater(new Adam(0.001))
      .list() // Start building a list of layers for a sequential model

      // Add the first dense layer (hidden layer) with ReLU activation
      .layer(0, new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder()
        .nIn(inputSize) // Number of input nodes (features)
        .nOut(hiddenSize) // Number of output nodes in the hidden layer
        .activation(Activation.RELU) // Activation function for hidden layer
        .build())

      // Add the output layer with Softmax activation for classification tasks
      .layer(1, new org.deeplearning4j.nn.conf.layers.OutputLayer.Builder()
        .nIn(hiddenSize) // Number of input nodes from the hidden layer
        .nOut(outputSize) // Number of output nodes (classes)
        .activation(Activation.SOFTMAX) // Activation function for output layer
        .build())

// Build the configuration for the
