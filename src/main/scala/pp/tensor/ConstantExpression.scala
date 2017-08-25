package pp.tensor

import breeze.math.Field

import scala.reflect.ClassTag

case class ConstantExpression[V: ClassTag : Field, K <: Nat, CK <: Nat]
(
  value: Tensor[V, K, CK]
) extends Expression[V, K, CK] {

  val ringV = implicitly[Field[V]]
  val ctV = implicitly[ClassTag[V]]

  val shape = TensorShape(value.dom, value.mod)

  override def eval() = value

  override def grad[M <: Nat : ToInt](variable: Variable[V, M]) = {
    new ConstantExpression[V, K, Plus[M, CK]](Tensor[V, K, Plus[M, CK]](shape.dom, Domain.join(variable.dom, shape.mod)))
  }
}
