/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IndexSpec extends AnyFreeSpec with Matchers with OptionValues {

  "indexPathBindable" - {
    val binder = Index.indexPathBindable
    val key    = "index"

    "bind a valid index" in {
      binder.bind(key, "1") mustEqual Right(Index(1))
    }

    "bind the highest allowed value" in {
      binder.bind(key, "195") mustEqual Right(Index(195))
    }

    "fail to bind a negative value" in {
      binder.bind(key, "-1") mustEqual Left("Index binding failed")
    }

    "fail to bind an 0" in {
      binder.bind(key, "0") mustEqual Left("Index binding failed")
    }

    "fail to bind an index over the total number of countries in the world (195)" in {
      binder.bind(key, "196") mustEqual Left("Index binding failed")
    }

    "unbind an index" in {
      binder.unbind(key, Index(1)) mustEqual "1"
    }

  }

}