package pages

import play.api.libs.json.JsPath

case object RenovationAllowanceBalancingChargePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "renovationAllowanceBalancingCharge"
}
