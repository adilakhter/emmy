package emmy.autodiff


case class UnaryExpression[U[_], V, S](up: Expression[U, V, S], rf: UnaryValueFunc[V])
                                      (implicit
                                 val vt: ValueOps[U, V, S],
                                 val ops: ContainerOps.Aux[U, S])
  extends Expression[U, V, S] {

  override val shape = up.shape

  override val parents = Seq(up)

  override def apply(ec: EvaluationContext[V]) = {
    ops.map(ec(up))(rf.apply)
  }

  override def grad[W[_], T](gc: GradientContext[V], v: Variable[W, V, T])(implicit wOps: ContainerOps.Aux[W, T]) = {
    val opsW = implicitly[ContainerOps[W]]
    val ug = gc(up, v)
    opsW.map(ug) { v =>
      vt.times(v, ops.map(gc(up))(rf.grad))
    }
  }
}
