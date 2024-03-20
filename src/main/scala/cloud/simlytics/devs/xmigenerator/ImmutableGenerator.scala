package cloud.simlytics.devs.xmigenerator

import scala.xml.Node
import Generator._

class ImmutableGenerator(val className: String, val pkg: String, val immutablesPkg: String, val variables: List[String], val isSimState: Boolean = false) {

  def buildHeader(): String = {
    val immutable: String = isSimState match {
      case false => "@Value.Immutable"
      case true => "@Value.Immutable\n@Value.Modifiable"
    }
    s"""
       |package ${pkg};
       |
       |import ${immutablesPkg}.*;
       |import java.util.List;
       |import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
       |import com.fasterxml.jackson.databind.annotation.JsonSerialize;
       |import org.immutables.value.Value;
       |import javax.annotation.Nullable;
       |
       |${immutable}
       |@JsonSerialize(as = ${className}.class)
       |@JsonDeserialize(as = ${className}.class)
       |""".stripMargin
  }

  def buildMethods(): String = {
    variables.map { v =>
      val (typeName, varName) = splitTypeAndName(v)
      s"    public abstract ${typeName} get${upperFirstLetter(varName)}();"
    }.mkString("\n")
  };

  def buildUpdateState(): String = {
    isSimState match {
      case false => ""
      case true =>
    s"""    public ${className} updateState(${className} ${lowerFirstLetter(className)}) {
       |        ${className} updated${className} = ${className}.copyOf(this);
       |${
      variables.map { v =>
        val (typeName, varName) = splitTypeAndName(v)
        val condition =         s"        if (${lowerFirstLetter(className)}.get${upperFirstLetter(varName)}() != null) {\n"
        val updateLine =
            s"            updated${className} = updated$className.with${upperFirstLetter(varName)}(${lowerFirstLetter(className)}.get${upperFirstLetter(varName)}());\n        }"
        condition + updateLine + "\n"
      }.mkString("\n")}
       |        return updated${className};
       |    }
       |""".stripMargin
    }
  }

  def build(): String = {
    s"""
       |${buildHeader()}
       |public abstract class Abstract${className} {
       |
       |${buildMethods()}
       |}
       |""".stripMargin
  }
}
