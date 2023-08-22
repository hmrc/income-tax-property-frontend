/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package service

import models.UserAnswers
import play.api.Logging
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject()(connector: SessionRepository)(implicit ec: ExecutionContext) extends Logging {

  def createNewEmptySession(id: String): Future[Boolean] = connector.set(UserAnswers(id))

  def get(id: String): Future[Option[UserAnswers]] = connector.get(id)

  def set(userAnswers: UserAnswers): Future[Boolean] = connector.set(userAnswers)

  def remove(id: String): Future[Boolean] = connector.clear(id)

  def keepAlive(id: String): Future[Boolean] = connector.keepAlive(id)

}
