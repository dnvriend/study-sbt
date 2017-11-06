trait CustomLoader {
  import scala.reflect.api
  import scala.reflect.api.{TypeCreator, Universe}
  import scala.reflect.runtime.universe._

  def createTypeTagForST(simpleTypeName: String, classLoader: Option[ClassLoader] = None): TypeTag[_] = {
    val currentMirror = classLoader
      .map(cl => scala.reflect.runtime.universe.runtimeMirror(cl))
      .getOrElse(scala.reflect.runtime.currentMirror)
    val typSym = currentMirror.staticClass(simpleTypeName)
    val tpe = internal.typeRef(NoPrefix, typSym, List.empty)
    val ttag = TypeTag(currentMirror, new TypeCreator {
      override def apply[U <: Universe with Singleton](m: api.Mirror[U]): U#Type = {
        assert(m == currentMirror, s"TypeTag[$tpe] defined in $currentMirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
    ttag
  }

  def createTypeTagForHKT(higherKindedTypeName: String = "scala.collection.immutable.List",
                          parameterSymbol: String = "scala.Int",
                          classLoader: Option[ClassLoader] = None): TypeTag[_] = {
    val currentMirror = classLoader
      .map(cl => scala.reflect.runtime.universe.runtimeMirror(cl))
      .getOrElse(scala.reflect.runtime.currentMirror)
    val typSym = currentMirror.staticClass(higherKindedTypeName)
    val paramSym = currentMirror.staticClass(parameterSymbol)
    val tpe = internal.typeRef(NoPrefix, typSym, List(paramSym.selfType))
    val ttag = TypeTag(currentMirror, new TypeCreator {
      override def apply[U <: Universe with Singleton](m: api.Mirror[U]): U#Type = {
        assert(m == currentMirror, s"TypeTag[$tpe] defined in $currentMirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
    ttag
  }
}