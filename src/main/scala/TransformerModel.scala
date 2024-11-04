import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration} // Import necessary configurations for the neural network
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork // Import the MultiLayerNetwork class
import org.nd4j.linalg.activations.Activation // Import activation functions
import org.nd4j.linalg.learning.config.Adam // Import Adam optimizer for learning

object TransformerModel {
  // Method to create and initialize a multi-layer neural network model
  def createModel(inputSize: Int, hiddenSize: Int, outputSize: Int): MultiLayerNetwork = {
    // Build the neural network configuration
    val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .updater(new Adam(0.001)) // Set the learning rate using Adam optimizer
      .list() // Start defining the layers of the network
      .layer(0, new org.deeplearning4j.nn.conf.layers.DenseLayer.Builder() // First hidden layer
        .nIn(inputSize) // Number of inputs to the layer
        .nOut(hiddenSize) // Number of outputs from the layer
        .activation(Activation.RELU) // Activation function for this layer
        .build()) // Build the layer
      .layer(1, new org.deeplearning4j.nn.conf.layers.OutputLayer.Builder() // Output layer
        .nIn(hiddenSize) // Number of inputs to the output layer
        .nOut(outputSize) // Number of outputs from the layer (e.g., classes)
        .activation(Activation.SOFTMAX) // Activation function for output layer
        .build()) // Build the layer
      .build() // Build the complete configuration

    // Create the multi-layer network model with the specified configuration
    val model = new MultiLayerNetwork(conf)
    model.init() // Initialize the model
    model // Return the initialized model
  }
}
