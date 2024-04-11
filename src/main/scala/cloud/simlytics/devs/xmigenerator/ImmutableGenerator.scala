package cloud.simlytics.devs.xmigenerator

import scala.xml.Node
import Generator._
import XmlParser._

class ImmutableGenerator(val className: String, val pkg: String, val immutablesPkg: String, val variables: List[Parameter], val isSimState: Boolean = false, superclass: Option[String] = None) {

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
       |import java.util.Map;
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
      val comment = buildComment(v.comment)
      s"${comment}    public abstract ${v.parameterType} get${upperFirstLetter(v.name)}();"
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
        val condition =         s"        if (${lowerFirstLetter(className)}.get${upperFirstLetter(v.name)}() != null) {\n"
        val updateLine =
            s"            updated${className} = updated$className.with${upperFirstLetter(v.name)}(${lowerFirstLetter(className)}.get${upperFirstLetter(v.name)}());\n        }"
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
       |public abstract class Abstract${className} ${superclass.map(sc => s"extends ${sc}").getOrElse("")} {
       |
       |${buildMethods()}
       |}
       |""".stripMargin
  }
}
