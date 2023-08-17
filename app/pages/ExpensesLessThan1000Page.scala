package pages

import play.api.libs.json.JsPath

case object ExpensesLessThan1000Page extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "expensesLessThan1000"
}
