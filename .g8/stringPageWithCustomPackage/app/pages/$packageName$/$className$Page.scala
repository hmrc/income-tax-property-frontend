package pages.$packageName$

import play.api.libs.json.JsPath
import pages.QuestionPage

case object $className$Page extends QuestionPage[String] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
