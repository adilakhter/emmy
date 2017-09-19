package emmy.inference

import breeze.numerics.abs
import emmy.autodiff._
import emmy.distribution.Normal
import emmy.inference.{AEVBModel, AEVBSampler}
import org.scalatest.FlatSpec

import scala.util.Random
import scalaz.Scalaz.Id

class AEVBModelSpec extends FlatSpec {

  "The AEVB model" should "update mu for each (minibatch of) data point(s)" in {
    val mu = Normal(0.0, 1.0).sample

    val initialModel = AEVBModel[Double](Seq(mu))
    val dist = Normal(mu, 0.2)

    val finalModel = (0 until 10).foldLeft(initialModel) {
      case (model, _) =>
        val data = for {_ <- 0 until 100} yield {
          0.3 + Random.nextGaussian() * 0.2
        }

        val observations = data.map { d => dist.observe(d) }
        model.update(observations)
    }
    val sampler = finalModel.globalVars.head._2.asInstanceOf[AEVBSampler[Id, Double, Any]]
    assert(abs(sampler.mu - 0.3) < 0.01)
  }

  it should "update sigma for each (minibatch of) data point(s)" in {
    val logSigma = Normal(0.5, 1.0).sample

    val initialModel = AEVBModel[Double](Seq(logSigma))
    val dist = Normal(0.3, exp(logSigma))

    val finalModel = (0 until 100).foldLeft(initialModel) {
      case (model, _) =>
        val data = for {_ <- 0 until 100} yield {
          0.3 + Random.nextGaussian() * scala.math.exp(0.2)
        }

        val observations = data.map { d => dist.observe(d) }
        model.update(observations)
    }
    val sampler = finalModel.globalVars.head._2.asInstanceOf[AEVBSampler[Id, Double, Any]]
    assert(abs(sampler.mu - 0.2) < 0.05)
  }

  def printVariable[U[_], V, S](model: AEVBModel[Double], name: String, variable: Variable[U, V, S]): Unit = {
    val dist = model.distributionOf(variable)
    println(s"$name: mu = ${dist._1}, sigma = ${dist._2}")
  }

  it should "be able to implement linear regression" in {
    // data generation - to be reproduced
    val data = {
      val alpha = 1.5
      val sigma = 1.0
      val beta = List(1.0, 2.5)

      (for {_ <- 0 until 200} yield {
        val X = List(Random.nextGaussian(), 0.2 * Random.nextGaussian())
        val Y = alpha + X(0) * beta(0) + X(1) * beta(1) + Random.nextGaussian() * sigma
        (X, Y)
      }).toList
    }

    // model parameters with their priors
    val a = Normal(0.0, 1.0).sample
    val b = Normal(List(0.0, 0.0), List(1.0, 1.0)).sample
    val e = Normal(0.0, 1.0).sample
    val model = AEVBModel[Double](Seq[Node](a, b, e))

    println("Prior model:")
    printVariable(model, "a", a)
    printVariable(model, "b", b)
    printVariable(model, "e", e)

    // infer parameter values from observations
    val observations = data.map {
      case (x, y) =>
        val s = a + sum(x * b)
        Normal(s, e).observe(y)
    }
    val newModel = model.update(observations)
    println("Posterior model:")
    printVariable(newModel, "a", a)
    printVariable(newModel, "b", b)
    printVariable(newModel, "e", e)

  }

}