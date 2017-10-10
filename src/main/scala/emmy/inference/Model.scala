package emmy.inference

import emmy.autodiff.{EvaluationContext, Variable}
import emmy.distribution.Observation

trait Model[V] {

  // new API - the Model contains a distribution over all variables
  // These distributions are updated in accordance with Bayes' Rule, when new evidence (observations) comes in

  def update[U[_], S](o: Seq[Observation[U, V, S]]): Model[V] = this

  def sample(ec: EvaluationContext[V]): ModelSample[V]
}

trait ModelSample[V] {

  def getSampleValue[U[_], S](n: Variable[U, V, S]): U[V]
}
