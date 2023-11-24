package pages

import play.api.libs.json.JsPath

case object OtherAllowablePropertyExpensesPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "otherAllowablePropertyExpenses"
}
