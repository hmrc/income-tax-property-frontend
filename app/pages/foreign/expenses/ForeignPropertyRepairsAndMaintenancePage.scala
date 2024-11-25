package pages.foreign.expenses

import pages.QuestionPage
import play.api.libs.json.JsPath

case object ForeignPropertyRepairsAndMaintenancePage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "foreignPropertyRepairsAndMaintenance"
}
