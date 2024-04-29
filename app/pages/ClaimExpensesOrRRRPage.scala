package pages

import play.api.libs.json.JsPath

case object ClaimExpensesOrRRRPage extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "claimExpensesOrRRR"
}
