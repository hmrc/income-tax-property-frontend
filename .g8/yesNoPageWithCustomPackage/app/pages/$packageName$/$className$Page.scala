package pages.$packageName$

import pages.QuestionPage
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[Boolean] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
